package at.janic.thedarkdimensions;

import java.util.WeakHashMap;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.fml.ModList;

/** Successful strikes expire after two seconds; a sweeping strike counts only once. */
public final class KatanaCombos {
    private record State(net.minecraft.world.item.Item item,int count,long time,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension){}
    private static final WeakHashMap<LivingEntity,State> STATES=new WeakHashMap<>();
    public static void init(){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e) -> {
            var p=e.getEntity();if(p.level().isClientSide())return;var state=STATES.get(p);
            if(state!=null&&(!p.isAlive()||p.getMainHandItem().getItem()!=state.item||!p.level().dimension().equals(state.dimension)||p.level().getGameTime()-state.time>PerkContent.comboWindow(p)))STATES.remove(p);
        });
    }
    public static boolean hit(ItemStack weapon,LivingEntity user,LivingEntity target){
        long now=user.level().getGameTime();State old=STATES.get(user);
        if(old!=null&&old.item==weapon.getItem()&&old.time==now)return false;
        int count=old!=null&&old.item==weapon.getItem()&&old.dimension.equals(user.level().dimension())&&now-old.time<=PerkContent.comboWindow(user)?old.count+1:1;
        // Better Combat owns the displayed combo. Use its authoritative server phase to align the finisher.
        if(ModList.get().isLoaded("bettercombat")&&PerkContent.comboWindow(user)==40)try{
            var api=Class.forName("net.bettercombat.api.EntityPlayer_BetterCombat");
            if(api.isInstance(user)){
                Object hand=api.getMethod("getCurrentAttack").invoke(user);
                if(hand!=null){Object combo=hand.getClass().getMethod("combo").invoke(hand);count=(int)combo.getClass().getMethod("current").invoke(combo);}
            }
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("Incompatible Better Combat combo API",ex);}
        boolean finish=count==3;STATES.put(user,new State(weapon.getItem(),finish?0:count,now,user.level().dimension()));
        if(finish&&user.level() instanceof ServerLevel l){
            KatanaFinisherEffects.play(l,weapon,user,target);
            if(!ModList.get().isLoaded("bettercombat")&&target.isAlive())target.hurtServer(l,new net.minecraft.world.damagesource.DamageSource(l.registryAccess().getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,TheDarkDimensions.id("katana_finisher"))),user),2);
        }
        return finish;
    }
    public static int chain(ServerLevel l,LivingEntity origin,LivingEntity user){
        var visited=new java.util.HashSet<java.util.UUID>();visited.add(origin.getUUID());visited.add(user.getUUID());
        LivingEntity from=origin;int hits=0;
        for(int i=0;i<2;i++){
            LivingEntity previous=from;
            var next=l.getEntitiesOfClass(Mob.class,from.getBoundingBox().inflate(4),m -> m.isAlive()&&!visited.contains(m.getUUID())&&!m.isAlliedTo(user)&&(m instanceof Monster||m.getTarget()==user)&&previous.distanceToSqr(m)<=16&&previous.hasLineOfSight(m))
                .stream().min(java.util.Comparator.comparingDouble(previous::distanceToSqr)).orElse(null);
            if(next==null)break;
            visited.add(next.getUUID());
            next.hurtServer(l,new net.minecraft.world.damagesource.DamageSource(l.registryAccess().getOrThrow(SolarContent.STORMVEIL_ARC),user),2);
            var a=from.getEyePosition();var b=next.getEyePosition();
            for(int n=0;n<=12;n++){var v=a.lerp(b,n/12.0);l.sendParticles(ParticleTypes.ELECTRIC_SPARK,v.x,v.y,v.z,1,.03,.03,.03,0);}
            from=next;hits++;
        }
        return hits;
    }
    private KatanaCombos(){}
}
