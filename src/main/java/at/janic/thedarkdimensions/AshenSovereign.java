package at.janic.thedarkdimensions;

import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;

/** A ground-based duellist. Damage is resolved only after its visible attack windup. */
public final class AshenSovereign extends Monster {
    public static final int IDLE=0,SLASH=1,HEAVY=2,GUARD=3,EXPOSED=4,SWING=5;
    public static final ResourceKey<DamageType> HEAVY_DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,TheDarkDimensions.id("sovereign_heavy"));
    private static final EntityDataAccessor<Integer> POSE=SynchedEntityData.defineId(AshenSovereign.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(AshenSovereign.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DURATION=SynchedEntityData.defineId(AshenSovereign.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(AshenSovereign.class,EntityDataSerializers.LONG);
    private final ServerBossEvent bossBar=new ServerBossEvent(java.util.UUID.randomUUID(),getDisplayName(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    private int timer,recovery=30,cycle;private LivingEntity lockedTarget;
    public AshenSovereign(EntityType<? extends AshenSovereign> type,Level level){
        super(type,level);xpReward=50;setPersistenceRequired();setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(KatanaContent.SOULEATER.get()));setDropChance(EquipmentSlot.MAINHAND,0);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(POSE,IDLE);b.define(PHASE,1);b.define(DURATION,0);b.define(START,0L);}
    public int combatPose(){return entityData.get(POSE);}public int phase(){return entityData.get(PHASE);}
    public float animationProgress(float partial){return Math.max(0,Math.min(1,(level().getGameTime()-entityData.get(START)+partial)/Math.max(1f,entityData.get(DURATION))));}
    private void pose(int value,int duration){entityData.set(POSE,value);entityData.set(START,level().getGameTime());entityData.set(DURATION,duration);timer=duration;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,14));goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private boolean valid(LivingEntity target){return target!=null&&target.isAlive()&&!(target instanceof Player p&&(p.isCreative()||p.isSpectator()))&&distanceToSqr(target)<900;}
    private boolean front(LivingEntity target){var delta=target.position().subtract(position()).multiply(1,0,1);return delta.lengthSqr()<.001||Vec3.directionFromRotation(0,getYRot()).dot(delta.normalize())>.25;}
    public void guard(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){
        if(combatPose()!=GUARD||KatanaDefense.staggered(this)||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)||!(e.getSource().getDirectEntity() instanceof LivingEntity attacker)||!front(attacker))return;
        if(!e.getSource().is(DamageTypes.PLAYER_ATTACK)&&!e.getSource().is(DamageTypes.MOB_ATTACK))return;
        e.setAmount(e.getAmount()*.35f);playSound(SoundEvents.SHIELD_BLOCK.value(),.65f,.9f);
        if(level() instanceof ServerLevel server)server.sendParticles(ParticleTypes.CRIT,getX(),getY()+1.2,getZ(),8,.25,.2,.25,.05);
    }
    @Override protected void customServerAiStep(ServerLevel level){
        super.customServerAiStep(level);bossBar.setProgress(getHealth()/getMaxHealth());bossBar.setName(getDisplayName());
        int next=getHealth()>80?1:getHealth()>40?2:3;
        if(next!=phase()){entityData.set(PHASE,next);bossBar.setColor(next==3?BossEvent.BossBarColor.RED:BossEvent.BossBarColor.PURPLE);playSound(SoundEvents.WITHER_SPAWN,.35f,1.35f);level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,getX(),getY()+1,getZ(),24,.5,.7,.5,.025);}
        var target=getTarget();setAggressive(valid(target));
        if(KatanaDefense.staggered(this)){
            if(combatPose()!=EXPOSED){pose(EXPOSED,20);lockedTarget=null;recovery=30;}
            getNavigation().stop();return;
        }
        if(recovery>0)recovery--;
        if(!valid(target)){getNavigation().stop();lockedTarget=null;if(combatPose()!=IDLE)pose(IDLE,0);return;}
        getLookControl().setLookAt(target,30,30);
        if(combatPose()==SLASH||combatPose()==HEAVY){
            getNavigation().stop();
            if(timer%4==0){int color=combatPose()==HEAVY?0xdc3545:0xaedff5;level.sendParticles(new DustParticleOptions(color,1),getX(),getEyeY()+.2,getZ(),4,.18,.12,.18,.01);}
            if(--timer<=0){strike(level,combatPose()==HEAVY);pose(SWING,6);recovery=phase()==3?18:phase()==2?24:30;}return;
        }
        if(combatPose()==SWING||combatPose()==EXPOSED){getNavigation().stop();if(--timer<=0)pose(IDLE,0);return;}
        if(combatPose()==GUARD){getNavigation().stop();if(--timer<=0)begin(target);return;}
        if(distanceToSqr(target)>9||!getSensing().hasLineOfSight(target)){getNavigation().moveTo(target,phase()==3?1.12:1);return;}
        getNavigation().stop();if(recovery==0){lockedTarget=target;if(phase()>1&&cycle%2==0){face(target);pose(GUARD,12);}else begin(target);}
    }
    private void face(LivingEntity target){
        var direction=target.position().subtract(position());setYRot((float)(net.minecraft.util.Mth.atan2(direction.z,direction.x)*180/Math.PI)-90);setYHeadRot(getYRot());yBodyRot=getYRot();
    }
    private void begin(LivingEntity target){
        lockedTarget=target;cycle++;boolean heavy=cycle%(phase()==3?2:phase()==2?3:4)==0;
        face(target);
        pose(heavy?HEAVY:SLASH,heavy?28-phase()*2:21-phase()*3);
        playSound(heavy?SoundEvents.IRON_GOLEM_ATTACK:SoundEvents.PLAYER_ATTACK_SWEEP,.55f,heavy?.65f:1.25f);
    }
    private void strike(ServerLevel level,boolean heavy){
        swing(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.component.SwingAnimation.DEFAULT);
        if(!valid(lockedTarget)||!getSensing().hasLineOfSight(lockedTarget))return;
        var source=heavy?new DamageSource(level.registryAccess().getOrThrow(HEAVY_DAMAGE),this):level.damageSources().mobAttack(this);
        float damage=heavy?10+phase():6+phase();
        if(heavy){
            for(var victim:level.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(3.6,1.5,3.6),v->v==lockedTarget||v instanceof Player)){
                if(!valid(victim)||!front(victim)||distanceToSqr(victim)>13||!getSensing().hasLineOfSight(victim))continue;
                if(victim.hurtServer(level,source,damage))victim.knockback(.55,getX()-victim.getX(),getZ()-victim.getZ(),source,damage);
            }
        }else if(distanceToSqr(lockedTarget)<=12&&front(lockedTarget))lockedTarget.hurtServer(level,source,damage);
        level.sendParticles(heavy?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.SWEEP_ATTACK,getX(),getY()+1,getZ(),heavy?14:2,.6,.2,.6,.015);
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP,.7f,heavy?.65f:1.1f);
    }
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bossBar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bossBar.removePlayer(p);}
    @Override public void die(DamageSource source){bossBar.removeAllPlayers();super.die(source);}
    @Override public void remove(RemovalReason reason){bossBar.removeAllPlayers();super.remove(reason);}
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("DuelCycle",cycle);}
    @Override protected void readAdditionalSaveData(ValueInput in){
        super.readAdditionalSaveData(in);cycle=Math.max(0,in.getIntOr("DuelCycle",0));lockedTarget=null;recovery=30;pose(IDLE,0);
        setPersistenceRequired();if(getMainHandItem().isEmpty())setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(KatanaContent.SOULEATER.get()));
        setDropChance(EquipmentSlot.MAINHAND,0);
    }
    @Override protected SoundEvent getAmbientSound(){return SoundEvents.VINDICATOR_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return SoundEvents.VINDICATOR_HURT;}
    @Override protected SoundEvent getDeathSound(){return SoundEvents.VINDICATOR_DEATH;}
}
