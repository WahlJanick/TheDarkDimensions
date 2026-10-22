package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;

public final class CrimsonRupture {
    private record Scar(UUID owner,int stacks,long last,long until){}
    private static final WeakHashMap<LivingEntity,Scar> SCARS=new WeakHashMap<>();
    public static int stacks(LivingEntity target,LivingEntity owner){var scar=SCARS.get(target);return scar!=null&&scar.owner.equals(owner.getUUID())&&scar.until>target.level().getGameTime()?scar.stacks:0;}
    public static void hit(LivingEntity target,LivingEntity owner,boolean finisher){
        if(!(target.level() instanceof ServerLevel level)||!target.isAlive())return;long now=level.getGameTime();var old=SCARS.get(target);if(old!=null&&old.owner.equals(owner.getUUID())&&old.last==now)return;int marks=stacks(target,owner);
        if(finisher){SCARS.put(target,new Scar(owner.getUUID(),0,now,now+80));target.hurtServer(level,new DamageSource(level.registryAccess().getOrThrow(RelicKatanas.PULSE),owner),Math.max(1,marks)*2);level.playSound(null,target.getX(),target.getY(),target.getZ(),SoundEvents.RESPAWN_ANCHOR_DEPLETE,SoundSource.PLAYERS,.45f,1.5f);SpellEffects.burst(level,new DustParticleOptions(0xff394d,1.2f),target.position().add(0,1,0),16);}
        else{int next=Math.min(2,marks+1);SCARS.put(target,new Scar(owner.getUUID(),next,now,now+80));draw(level,target,next);}
    }
    private static void draw(ServerLevel level,LivingEntity target,int marks){
        var center=target.position().add(0,target.getBbHeight()*.55,0);var scar=SCARS.get(target);var owner=scar==null?null:level.getEntity(scar.owner);Vec3 front=owner==null?new Vec3(0,0,1):owner.position().subtract(target.position()).multiply(1,0,1).normalize();if(front.lengthSqr()<.01)front=new Vec3(0,0,1);var side=new Vec3(-front.z,0,front.x);center=center.add(front.scale(target.getBbWidth()*.5+.06));var dust=new DustParticleOptions(0xe92738,.8f);
        for(int n=0;n<marks;n++){var at=center.add(side.scale(n==0?-.12:.12));SpellEffects.line(level,dust,at.add(side.scale(-.15)).add(0,-.35,0),at.add(side.scale(.08)).add(0,.05,0));SpellEffects.line(level,dust,at.add(side.scale(.08)).add(0,.05,0),at.add(side.scale(-.08)).add(0,.35,0));}
    }
    public static void init(){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.LevelTickEvent.Post event)->{if(!(event.getLevel() instanceof ServerLevel level))return;long now=level.getGameTime();var it=SCARS.entrySet().iterator();while(it.hasNext()){var entry=it.next();var target=entry.getKey();var scar=entry.getValue();if(target.level()!=level)continue;if(!target.isAlive()||target.isRemoved()||now>=scar.until){it.remove();continue;}if(scar.stacks>0&&now%5==0)draw(level,target,scar.stacks);}});}
}
