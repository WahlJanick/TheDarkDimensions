package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Each creature has its own combat role; no vanilla raid or insomnia spawners are involved. */
public final class RealmMobs {
    public static class Caster extends Monster implements RangedAttackMob {
        private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> CHARGING=net.minecraft.network.syncher.SynchedEntityData.defineId(Caster.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
        private int castWindup;private LivingEntity castTarget;
        public Caster(EntityType<? extends Caster> type,Level level){super(type,level);xpReward=8;}
        @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(CHARGING,false);}
        public boolean isCharging(){return entityData.get(CHARGING);}
        protected void charge(boolean value){entityData.set(CHARGING,value);}
        @Override protected void customServerAiStep(ServerLevel level){
            super.customServerAiStep(level);setAggressive(getTarget()!=null);
            if(castWindup>0&&--castWindup==0){
                charge(false);
                if(castTarget!=null&&castTarget.isAlive()&&distanceToSqr(castTarget)<256&&getSensing().hasLineOfSight(castTarget))RealmMagicBolt.fire(level,this,castTarget,element());
                castTarget=null;
            }
        }
        @Override protected void registerGoals(){
            goalSelector.addGoal(0,new FloatGoal(this));
            goalSelector.addGoal(2,new RangedAttackGoal(this,.8,70,12));
            goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.6));
            goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,12));
            goalSelector.addGoal(7,new RandomLookAroundGoal(this));
            targetSelector.addGoal(1,new HurtByTargetGoal(this));
            targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        }
        public int element(){return 0;}
        @Override public void performRangedAttack(LivingEntity target,float power){
            if(level() instanceof ServerLevel&&castWindup==0){castTarget=target;castWindup=15;charge(true);playSound(SoundEvents.EVOKER_PREPARE_ATTACK,.6f,element()==1?1.2f:.9f);}
        }
    }
    public static class ChronusMage extends Caster {
        private int dashCooldown=100;
        public ChronusMage(EntityType<? extends ChronusMage> type,Level level){super(type,level);}
        @Override protected void customServerAiStep(ServerLevel level){
            super.customServerAiStep(level);
            if(dashCooldown>0)dashCooldown--;
            var target=getTarget();
            if(target!=null&&distanceToSqr(target)<25&&dashCooldown==0){
                Vec3 away=position().subtract(target.position()).multiply(1,0,1).normalize();
                for(int i=4;i>=2;i--){
                    Vec3 destination=position().add(away.scale(i));var pos=BlockPos.containing(destination);
                    if(!level.hasChunkAt(pos)||!level.getBlockState(pos.below()).isSolid()||!level.getFluidState(pos).isEmpty())continue;
                    Vec3 delta=destination.subtract(position());
                    if(!level.noCollision(this,getBoundingBox().expandTowards(delta)))continue;
                    level.sendParticles(ParticleTypes.PORTAL,getX(),getY()+1,getZ(),16,.3,.5,.3,.05);
                    setPos(destination);getNavigation().stop();dashCooldown=120;
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL,getX(),getY()+1,getZ(),16,.3,.5,.3,.02);break;
                }
            }
        }
    }
    public static final class DrownedSentinel extends Caster {
        public DrownedSentinel(EntityType<? extends DrownedSentinel> type,Level level){super(type,level);}
        @Override public int element(){return 1;}
        @Override protected int decreaseAirSupply(int air){return air;}
    }
    public static final class BriarWhisperer extends Caster {
        private Vec3 roots;private int windup;
        public BriarWhisperer(EntityType<? extends BriarWhisperer> type,Level level){super(type,level);}
        @Override public int element(){return 2;}
        @Override public void performRangedAttack(LivingEntity target,float power){
            if(windup!=0||!getSensing().hasLineOfSight(target))return;
            roots=target.position();windup=20;charge(true);playSound(SoundEvents.EVOKER_PREPARE_ATTACK,.65f,1.3f);
        }
        @Override protected void customServerAiStep(ServerLevel level){
            super.customServerAiStep(level);
            if(windup<=0||roots==null)return;
            if(windup%4==0)for(int i=0;i<8;i++){
                double a=i*Math.PI/4;level.sendParticles(ParticleTypes.HAPPY_VILLAGER,roots.x+Math.cos(a),roots.y+.1,roots.z+Math.sin(a),1,0,0,0,0);
            }
            if(--windup==0){
                charge(false);
                for(var victim:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(roots,roots).inflate(1.2,1.5,1.2))){
                    if(victim==this||victim instanceof BriarWhisperer||victim instanceof Player p&&(p.isCreative()||p.isSpectator()))continue;
                    if(getSensing().hasLineOfSight(victim)&&victim.hurtServer(level,damageSources().mobAttack(this),2))victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,50,1),this);
                }
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,roots.x,roots.y+.4,roots.z,20,.6,.3,.6,.01);roots=null;
            }
        }
    }
    public static class VenomSpider extends Spider {
        public VenomSpider(EntityType<? extends VenomSpider> type,Level level){super(type,level);}
        @Override public boolean doHurtTarget(ServerLevel level,Entity target){
            boolean hit=super.doHurtTarget(level,target);
            if(hit&&target instanceof LivingEntity living)living.addEffect(new MobEffectInstance(MobEffects.POISON,60),this);
            return hit;
        }
    }
    public static final class VeilwoodSpider extends VenomSpider {
        public VeilwoodSpider(EntityType<? extends VeilwoodSpider> type,Level level){super(type,level);}
    }
    public static final class DuneStalker extends VenomSpider {
        public DuneStalker(EntityType<? extends DuneStalker> type,Level level){super(type,level);}
    }
    public static final class CinderBroodmother extends Spider {
        private int cooldown=100,windup;
        private Vec3 strike;
        public CinderBroodmother(EntityType<? extends CinderBroodmother> type,Level level){super(type,level);xpReward=25;}
        public int windupTicks(){return windup;}
        @Override protected void customServerAiStep(ServerLevel level){
            super.customServerAiStep(level);
            if(cooldown>0)cooldown--;
            var target=getTarget();
            if(windup==0&&cooldown==0&&target!=null&&distanceToSqr(target)<100&&getSensing().hasLineOfSight(target)){
                strike=target.position();windup=25;cooldown=160;
                playSound(SoundEvents.BLAZE_SHOOT,.7f,.6f);
            }
            if(windup>0&&strike!=null){
                if(windup%5==0)for(int i=0;i<12;i++){
                    double a=i*Math.PI/6;level.sendParticles(ParticleTypes.FLAME,strike.x+Math.cos(a)*1.6,strike.y+.15,strike.z+Math.sin(a)*1.6,1,0,0,0,0);
                }
                if(--windup==0){
                    level.sendParticles(ParticleTypes.FLAME,strike.x,strike.y+1,strike.z,32,1,.6,1,.025);
                    for(var victim:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(strike,strike).inflate(1.7,2,1.7))){
                        if(victim==this||victim instanceof CinderBroodmother||victim instanceof Player p&&(p.isCreative()||p.isSpectator()))continue;
                        if(getSensing().hasLineOfSight(victim)&&victim.hurtServer(level,damageSources().mobAttack(this),5))victim.igniteForSeconds(3);
                    }
                    strike=null;
                }
            }
        }
    }
    public static final class Bonewing extends Phantom {
        public Bonewing(EntityType<? extends Bonewing> type,Level level){super(type,level);}
    }
    public static final class AshenWraith extends Monster {
        public AshenWraith(EntityType<? extends AshenWraith> type,Level level){
            super(type,level);setNoGravity(true);moveControl=new FlyingMoveControl<>(this,20,true);xpReward=7;
        }
        @Override protected PathNavigation createNavigation(Level level){return new FlyingPathNavigation(this,level);}
        @Override protected void registerGoals(){
            goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false));
            goalSelector.addGoal(5,new WaterAvoidingRandomFlyingGoal(this,.7));
            goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,10));
            targetSelector.addGoal(1,new HurtByTargetGoal(this));
            targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
        }
        @Override public void travel(Vec3 input){travelFlying(input,.05f);}
    }
    public static final class LanternWisp extends PathfinderMob {
        public LanternWisp(EntityType<? extends LanternWisp> type,Level level){
            super(type,level);setNoGravity(true);moveControl=new FlyingMoveControl<>(this,15,true);
        }
        @Override protected PathNavigation createNavigation(Level level){return new FlyingPathNavigation(this,level);}
        @Override protected void registerGoals(){
            goalSelector.addGoal(1,new AvoidEntityGoal<>(this,Player.class,5,.8,1.1));
            goalSelector.addGoal(5,new WaterAvoidingRandomFlyingGoal(this,.5));
        }
        @Override public void travel(Vec3 input){travelFlying(input,.05f);}
        @Override protected void customServerAiStep(ServerLevel level){
            super.customServerAiStep(level);
            if(tickCount%15==0)level.sendParticles(ParticleTypes.GLOW,getX(),getY()+.2,getZ(),2,.2,.2,.2,0);
        }
    }
}
