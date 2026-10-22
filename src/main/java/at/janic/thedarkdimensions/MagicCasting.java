package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.effect.*;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class MagicCasting {
    private record Position(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,Vec3 pos,float yaw,float pitch,long tick){}
    private static final Map<ServerPlayer,Deque<Position>> HISTORY=new WeakHashMap<>();
    public static void record(ServerPlayer p){
        var q=HISTORY.computeIfAbsent(p,k->new ArrayDeque<>());
        if(!p.isAlive()){q.clear();return;}
        if(!q.isEmpty()&&!q.getLast().dimension.equals(p.level().dimension()))q.clear();
        q.add(new Position(p.level().dimension(),p.position(),p.getYRot(),p.getXRot(),p.level().getGameTime()));while(q.size()>101)q.removeFirst();
    }
    private static boolean hostile(LivingEntity e,Player p){return e!=p&&e.isAlive()&&!e.isAlliedTo(p)&&(e instanceof Monster||e instanceof Mob m&&m.getTarget()==p);}
    private static List<LivingEntity> enemies(ServerLevel l,Player p,double radius){return l.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(radius),e->hostile(e,p)&&p.hasLineOfSight(e));}
    private static boolean safe(ServerPlayer p,Vec3 pos){var l=p.level();var b=BlockPos.containing(pos);return l.hasChunkAt(b)&&l.getWorldBorder().isWithinBounds(b)&&l.noCollision(p,p.getBoundingBox().move(pos.subtract(p.position())))&&!l.getBlockState(b).is(net.minecraft.world.level.block.Blocks.LAVA);}
    public static boolean move(ServerPlayer p,double range){Vec3 dir=p.getLookAngle().multiply(1,0,1).normalize();if(dir.lengthSqr()<.01)return false;Vec3 end=p.position();for(double n=.25;n<=range;n+=.25){Vec3 next=p.position().add(dir.scale(n));if(!safe(p,next))break;end=next;}if(end.distanceToSqr(p.position())<.25)return false;p.teleportTo(end.x,end.y,end.z);return true;}
    private static boolean rewind(ServerPlayer p){var q=HISTORY.get(p);if(q==null)return false;long now=p.level().getGameTime();for(var it=q.descendingIterator();it.hasNext();){var s=it.next();if(now-s.tick>=60&&now-s.tick<=100&&s.dimension.equals(p.level().dimension())&&safe(p,s.pos)){p.teleportTo(s.pos.x,s.pos.y,s.pos.z);p.setYRot(s.yaw);p.setXRot(s.pitch);return true;}}return false;}
    private static LivingEntity aimed(ServerLevel l,Player p){Vec3 a=p.getEyePosition(),b=a.add(p.getLookAngle().scale(16));double nearest=p.pick(16,1,false).getLocation().distanceToSqr(a);LivingEntity best=null;for(var e:l.getEntitiesOfClass(LivingEntity.class,new AABB(a,b).inflate(1),e->hostile(e,p))){var hit=e.getBoundingBox().inflate(.3).clip(a,b);if(hit.isPresent()&&hit.get().distanceToSqr(a)<nearest){nearest=hit.get().distanceToSqr(a);best=e;}}return best;}
    private static void effect(Player p,net.minecraft.core.Holder<MobEffect> e,int duration,int amplifier){p.addEffect(new MobEffectInstance(e,duration,amplifier));}
    public static boolean cast(Player p,int source){
        ItemStack book=MagicBooks.get(p,source);if(!(book.getItem() instanceof SpellBookItem)||p.isSpectator()||!p.isAlive()||p.getCooldowns().isOnCooldown(new ItemStack(SpellContent.FIRE_BOOK.get())))return false;
        MagicData d=MagicData.read(book);int id=d.slots[d.selected];if(!d.knows(id)){p.sendOverlayMessage(Component.translatable("magic.thedarkdimensions.learn_hint"));return false;}
        if(!(p.level() instanceof ServerLevel l))return true;
        MagicSkill skill=MagicSkill.values()[id];int level=d.level(id),bonus=level-1,mastery=d.mastery(skill);var targets=enemies(l,p,MagicMastery.radius(skill,mastery,5+bonus*.5));LivingEntity aimed=aimed(l,p);float healthBefore=aimed==null?0:aimed.getHealth();
        Vec3 origin=p.position(),impact=aimed==null?p.pick(16,1,false).getLocation():aimed.position().add(0,aimed.getBbHeight()*.5,0);
        switch(skill){
            case FIRE_BOLT->{if(mastery>0&&p instanceof ServerPlayer sp)MagicMastery.fire(sp,mastery);else{Vec3 direction=p.getLookAngle();var ball=new SmallFireball(l,p,direction);ball.setPos(p.getEyePosition().add(direction.scale(.6)));ball.setDeltaMovement(direction.scale(.6+bonus*.05));l.addFreshEntity(ball);}}
            case FIRE_RING->{if(targets.isEmpty())return false;for(var e:targets){e.igniteForSeconds(3+bonus);if(e.hurtServer(l,l.damageSources().indirectMagic(p,p),3+bonus*.5f)&&p instanceof ServerPlayer sp)MagicExpansion.fireHit(sp,e);if(level==5){var v=e.position().subtract(p.position());e.knockback(.45,-v.x,-v.z,l.damageSources().indirectMagic(p,p),0);}}}
            case FIRE_GUARD->effect(p,MobEffects.FIRE_RESISTANCE,120+bonus*30,0);
            case WATER_HEAL->{p.heal(6+bonus);p.clearFire();if(level==5)p.removeEffect(MobEffects.POISON);}
            case WATER_WAVE->{if(targets.isEmpty())return false;for(var e:targets){var v=e.position().subtract(p.position());e.knockback(.8+bonus*.1,-v.x,-v.z,l.damageSources().indirectMagic(p,p),2);e.hurtServer(l,l.damageSources().indirectMagic(p,p),2+bonus*.5f);}}
            case WATER_GUARD->{effect(p,MobEffects.WATER_BREATHING,200+bonus*40,0);effect(p,MobEffects.RESISTANCE,80+bonus*20,0);}
            case LIGHTNING_STRIKE->{var hit=p.pick(16,1,false);if(hit.getType()==HitResult.Type.MISS&&aimed==null)return false;Vec3 location=aimed!=null?aimed.position():hit.getLocation();var bolt=EntityTypes.LIGHTNING_BOLT.create(l,EntitySpawnReason.EVENT);if(bolt==null)return false;bolt.setPos(location);bolt.setVisualOnly(true);l.addFreshEntity(bolt);for(var e:l.getEntitiesOfClass(LivingEntity.class,new AABB(location,location).inflate(2),e->hostile(e,p)))e.hurtServer(l,l.damageSources().lightningBolt(),6+bonus*.5f);}
            case CHAIN_LIGHTNING->{if(!(p instanceof ServerPlayer sp)||!MagicExpansion.chain(sp,aimed,level,mastery))return false;}
            case LIGHTNING_STEP->{if(!(p instanceof ServerPlayer sp)||!move(sp,MagicMastery.distance(skill,mastery,3+bonus*.25)))return false;for(var e:enemies(l,p,2))e.hurtServer(l,l.damageSources().lightningBolt(),2+bonus*.5f);}
            case CHRONUS_DASH,ENDER_BLINK->{if(!(p instanceof ServerPlayer sp)||!move(sp,MagicMastery.distance(skill,mastery,(skill==MagicSkill.ENDER_BLINK?4:3)+bonus*.25)))return false;}
            case TIME_SLOW->{if(targets.isEmpty())return false;for(var e:targets)e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,80+bonus*20,MagicMastery.boss(e)?0:1+bonus/2));}
            case REWIND->{if(!(p instanceof ServerPlayer sp)||!rewind(sp))return false;}
            case BLOOD_CUT->{if(aimed==null)return false;aimed.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,60+bonus*15,0));aimed.hurtServer(l,l.damageSources().indirectMagic(p,p),3+bonus*.5f);}
            case BLOOD_SIPHON->{if(aimed==null)return false;float before=aimed.getHealth();aimed.hurtServer(l,l.damageSources().indirectMagic(p,p),4+bonus*.5f);p.heal(Math.min(3+bonus*.5f,Math.max(0,before-aimed.getHealth())));}
            case BLOOD_GUARD->effect(p,MobEffects.ABSORPTION,120+bonus*20,bonus/2);
            case SOUL_BOLT->{if(aimed==null)return false;aimed.hurtServer(l,l.damageSources().indirectMagic(p,p),4+bonus*.5f);aimed.addEffect(new MobEffectInstance(KatanaEffects.SOULLESS,40+bonus*10,0));}
            case SOUL_FEAR->{if(targets.isEmpty())return false;for(var e:targets)e.addEffect(new MobEffectInstance(KatanaEffects.SOULLESS,60+bonus*15,0));}
            case SOUL_SIGHT->{effect(p,MobEffects.NIGHT_VISION,240+bonus*40,0);for(var e:targets)e.addEffect(new MobEffectInstance(MobEffects.GLOWING,120+bonus*20,0));}
            case ENDER_CHEST->{var chest=p.getEnderChestInventory();chest.setActiveChest(null);p.openMenu(new SimpleMenuProvider((n,inv,player)->ChestMenu.threeRows(n,inv,chest),Component.translatable("container.enderchest")));}
            case ENDER_WARD->{effect(p,MobEffects.SLOW_FALLING,160+bonus*20,0);effect(p,MobEffects.RESISTANCE,60+bonus*15,0);}
            case ELEMENTAL_BURST->{if(targets.isEmpty())return false;for(var e:targets)e.hurtServer(l,l.damageSources().indirectMagic(p,p),4+bonus*.5f);}
            case ELEMENTAL_BARRIER->effect(p,MobEffects.RESISTANCE,100+bonus*20,0);
            case ELEMENTAL_SURGE->{if(targets.isEmpty())return false;for(var e:targets){e.hurtServer(l,l.damageSources().lightningBolt(),4+bonus*.5f);e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40+bonus*10,0));}p.heal(2+bonus*.5f);}
            default->{if(!(p instanceof ServerPlayer sp)||!MagicExpansion.cast(sp,skill,level,aimed,mastery))return false;}
        }
        if(p instanceof ServerPlayer sp)MagicMastery.after(sp,skill,mastery,aimed,targets,origin,aimed==null?0:Math.max(0,healthBefore-aimed.getHealth()));
        SpellEffects.play(skill,l,p,origin,impact,targets,level,mastery);
        book=book.copy();d.uses[id]=Math.min(70,d.uses[id]+1);d.save(book);MagicBooks.set(p,source,book);
        if(p instanceof ServerPlayer serverPlayer)ExplorationContent.award(serverPlayer,"first_spell");
        int cooldown=PerkContent.cooldown(p,Math.round(skill.cooldown()*(1-.05f*bonus)),skill);if(p instanceof ServerPlayer sp)cooldown=MagicExpansion.cooldown(sp,skill,cooldown);cooldown=MagicMastery.cooldown(skill,mastery,cooldown);for(var e:MagicElement.values())p.getCooldowns().addCooldown(new ItemStack(MagicContent.book(e)),cooldown);p.getCooldowns().addCooldown(new ItemStack(MagicContent.MASTER_BOOK.get()),cooldown);if(p instanceof ServerPlayer server)at.janic.thedarkdimensions.resonance.ResonanceProgress.data(server).lastSpell=skill.element().ordinal();return true;
    }
}
