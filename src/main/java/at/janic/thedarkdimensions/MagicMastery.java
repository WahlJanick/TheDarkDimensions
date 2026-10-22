package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

/** Book-owned specializations. A cast snapshots its choice; later book swaps cannot alter fields. */
public final class MagicMastery {
    private record Shield(ServerLevel level,long until,MagicSkill skill,int choice){}
    private static final Map<ServerPlayer,Shield> SHIELDS=new WeakHashMap<>();
    public static String key(MagicSkill s,int choice){return "mastery.thedarkdimensions."+s.name().toLowerCase(java.util.Locale.ROOT)+"."+choice;}
    public static boolean boss(LivingEntity e){return e.typeHolder().is(Mindshift.IMMUNE)||e instanceof at.janic.thedarkdimensions.finale.AshboundRegent||e instanceof AshenSovereign;}
    private static void buff(LivingEntity e,net.minecraft.core.Holder<MobEffect> type,int ticks,int amplifier){e.addEffect(new MobEffectInstance(type,ticks,amplifier,false,false,true));}
    private static void slow(LivingEntity e,int ticks,int amplifier){buff(e,MobEffects.SLOWNESS,ticks,boss(e)?0:amplifier);}
    private static void damage(ServerPlayer p,LivingEntity e,float value){e.hurtServer(p.level(),new net.minecraft.world.damagesource.DamageSource(p.level().registryAccess().getOrThrow(MagicExpansion.PULSE),p,p),value);}
    public static List<LivingEntity> enemies(ServerPlayer p,Vec3 at,double radius){return p.level().getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(radius),e->MagicExpansion.hostile(e,p)&&e.position().distanceToSqr(at)<=radius*radius&&p.hasLineOfSight(e));}
    public static List<Player> friends(ServerPlayer p,double radius){return p.level().getEntitiesOfClass(Player.class,p.getBoundingBox().inflate(radius),e->e!=p&&e.isAlive()&&!e.isSpectator()&&e.isAlliedTo(p)&&e.distanceToSqr(p)<=radius*radius&&p.hasLineOfSight(e));}
    public static double radius(MagicSkill s,int choice,double base){return base+switch(s){case FIRE_RING,WATER_WAVE,TIME_SLOW,SOUL_FEAR,ELEMENTAL_BURST,ELEMENTAL_SURGE->choice==1?3:0;case HEALING_SPRING->choice==1?1.5:0;case STORM_FIELD,INERTIA_FIELD->choice==1?2:0;case VOID_PULL->choice==1?2:0;default->0;};}
    public static double distance(MagicSkill s,int choice,double base){return base+(choice==1&&switch(s){case CHRONUS_DASH,ENDER_BLINK,LIGHTNING_STEP,THUNDER_LEAP->true;default->false;}?2:0)+(s==MagicSkill.ASH_STEP&&choice==2?2:0);}
    public static int duration(MagicSkill s,int choice,int base){return switch(s){case ASH_STEP->base+(choice==1?60:0);case HEALING_SPRING->choice==2?100:base;case TIME_ANCHOR,AFTERIMAGE->choice==1?base*2:base;case STATIC_CHARGE->choice==1?base+120:base;case DIMENSION_FOLD->base+(choice==1?80:0);default->base;};}
    public static int cooldown(MagicSkill s,int choice,int base){return s==MagicSkill.ENDER_CHEST&&choice==1?Math.max(10,base/2):base;}
    /** Fire Bolt's mutually exclusive native ray/fan replace the projectile only after an explicit choice. */
    public static void fire(ServerPlayer p,int choice){
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();double range=choice==1?18:8;
        Vec3 end=p.pick(range,1,false).getLocation();
        var hits=new ArrayList<>(enemies(p,p.position(),range+2));hits.sort(Comparator.comparingDouble(p::distanceToSqr));int count=0;
        for(var e:hits){Vec3 center=e.getBoundingBox().getCenter();boolean hit;
            if(choice==1)hit=e.getBoundingBox().inflate(.3).clip(eye,end).isPresent();
            else {var offset=center.subtract(eye);hit=offset.length()<=range&&offset.normalize().dot(look)>=.70;}
            if(!hit)continue;if(++count>(choice==1?4:6))break;damage(p,e,choice==1?7:5);e.igniteForSeconds(choice==1?3:2);MagicExpansion.fireHit(p,e);
            SpellEffects.burst(p.level(),ParticleTypes.SMALL_FLAME,center,8);
        }
        if(choice==1)SpellEffects.line(p.level(),ParticleTypes.FLAME,eye,end);
        else for(int n=-3;n<=3;n++){var dir=look.yRot(n*.16f);var hit=p.level().clip(new net.minecraft.world.level.ClipContext(eye,eye.add(dir.scale(range)),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));SpellEffects.line(p.level(),ParticleTypes.SMALL_FLAME,eye,hit.getLocation());}
    }
    public static void after(ServerPlayer p,MagicSkill skill,int choice,LivingEntity target,List<LivingEntity> targets,Vec3 origin,float dealt){
        if(choice==0)return;
        switch(skill){
            case FIRE_RING->{if(choice==2)for(var e:targets)if(!boss(e)){Vec3 delta=p.position().subtract(e.position()).multiply(1,0,1);if(delta.lengthSqr()>.2)e.setDeltaMovement(delta.normalize().scale(.5).add(0,.12,0));e.syncVelocity=true;}}
            case FIRE_GUARD->{if(choice==1)buff(p,MobEffects.RESISTANCE,100,0);else for(var friend:friends(p,6))buff(friend,MobEffects.FIRE_RESISTANCE,240,0);}
            case WATER_HEAL->{if(choice==1){p.heal(4);p.removeEffect(MobEffects.WEAKNESS);}else for(var friend:friends(p,6)){friend.heal(6);friend.clearFire();}}
            case WATER_WAVE->{if(choice==2)for(var e:targets)slow(e,60,2);}
            case WATER_GUARD->{if(choice==1){buff(p,MobEffects.WATER_BREATHING,720,0);buff(p,MobEffects.DOLPHINS_GRACE,160,0);}else{buff(p,MobEffects.ABSORPTION,160,0);buff(p,MobEffects.RESISTANCE,100,1);}}
            case LIGHTNING_STRIKE->{if(target!=null){if(choice==1)damage(p,target,3);else enemies(p,target.position(),4).stream().filter(e->e!=target).limit(2).forEach(e->{damage(p,e,3);SpellEffects.line(p.level(),ParticleTypes.ELECTRIC_SPARK,target.getEyePosition(),e.getEyePosition());});}}
            case LIGHTNING_STEP->{if(choice==2)for(var e:enemies(p,p.position(),3))slow(e,60,2);}
            case CHRONUS_DASH->{if(choice==2){buff(p,MobEffects.SPEED,60,1);buff(p,MobEffects.ABSORPTION,60,0);}}
            case TIME_SLOW->{if(choice==2)for(var e:targets){slow(e,120,4);buff(e,MobEffects.WEAKNESS,80,0);}}
            case REWIND->{if(choice==1)p.heal(4);else for(var e:enemies(p,origin,4))slow(e,80,2);}
            case BLOOD_CUT->{if(target!=null){if(choice==1)bleed(target,180);else enemies(p,target.position(),3).stream().filter(e->e!=target).limit(3).forEach(e->bleed(e,80));}}
            case BLOOD_SIPHON->{if(dealt>0){if(choice==1)p.heal(Math.min(2,dealt));else for(var friend:friends(p,6))friend.heal(Math.min(3,dealt));}}
            case BLOOD_GUARD->{if(choice==1)buff(p,MobEffects.ABSORPTION,240,3);else SHIELDS.put(p,new Shield(p.level(),p.level().getGameTime()+200,skill,choice));}
            case SOUL_BOLT->{if(target!=null){if(choice==1)damage(p,target,3);else enemies(p,target.position(),3).stream().filter(e->e!=target).limit(3).forEach(e->buff(e,KatanaEffects.SOULLESS,40,0));}}
            case SOUL_FEAR->{if(choice==2)for(var e:targets)buff(e,MobEffects.WEAKNESS,120,1);}
            case SOUL_SIGHT->{if(choice==1)buff(p,MobEffects.NIGHT_VISION,1000,0);else for(var e:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(20),e->MagicExpansion.hostile(e,p)))buff(e,MobEffects.GLOWING,300,0);}
            case ENDER_CHEST->{if(choice==2)buff(p,MobEffects.RESISTANCE,100,0);}
            case ENDER_BLINK->{if(choice==2)buff(p,MobEffects.ABSORPTION,100,0);}
            case ENDER_WARD->{if(choice==1)buff(p,MobEffects.SLOW_FALLING,640,0);else SHIELDS.put(p,new Shield(p.level(),p.level().getGameTime()+160,skill,choice));}
            case ELEMENTAL_BURST->{if(choice==2)for(var e:targets){e.igniteForSeconds(2);slow(e,40,1);}}
            case ELEMENTAL_BARRIER->{if(choice==1)buff(p,MobEffects.RESISTANCE,120,1);else for(var friend:friends(p,6))buff(friend,MobEffects.RESISTANCE,160,0);}
            case ELEMENTAL_SURGE->{if(choice==2){p.heal(4);for(var e:targets)buff(e,MobEffects.WEAKNESS,60,0);}}
            default->{} // Persistent-field masteries are applied inside MagicExpansion using the captured choice.
        }
    }
    private static void bleed(LivingEntity e,int ticks){e.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,ticks,0));}
    public static void init(){
        var bus=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        bus.addListener((net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre e)->{if(!(e.getEntity() instanceof ServerPlayer p)||e.getNewDamage()<=0||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;var s=SHIELDS.get(p);if(s==null)return;SHIELDS.remove(p);if(s.level!=p.level()||s.until<=p.level().getGameTime())return;
            if(s.skill==MagicSkill.ENDER_WARD){e.setNewDamage(e.getNewDamage()*.5f);SpellEffects.burst(p.level(),ParticleTypes.REVERSE_PORTAL,p.getEyePosition(),12);}
            else if(e.getSource().getEntity() instanceof LivingEntity attacker&&MagicExpansion.hostile(attacker,p)){bleed(attacker,100);SpellEffects.burst(p.level(),KatanaEffects.BLOOD_DROP.get(),attacker.getEyePosition(),10);}
        });
        bus.addListener((net.neoforged.neoforge.event.tick.LevelTickEvent.Post e)->{if(e.getLevel() instanceof ServerLevel l)SHIELDS.entrySet().removeIf(x->!x.getKey().isAlive()||x.getKey().hasDisconnected()||x.getKey().level()!=x.getValue().level||x.getValue().level==l&&x.getValue().until<=l.getGameTime());});
    }
    private MagicMastery(){}
}
