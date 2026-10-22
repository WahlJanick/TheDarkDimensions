package at.janic.thedarkdimensions.finale;

import at.janic.thedarkdimensions.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Blue slash: parry; red locked circle: dodge; golden ward: break with magic. */
public final class AshboundRegent extends Monster {
    public static final int IDLE=0,SLASH=1,RIFT=2,WARD=3,EXPOSED=4,SWING=5;
    public static final ResourceKey<DamageType> WAVE_DAMAGE=ResourceKey.create(Registries.DAMAGE_TYPE,TheDarkDimensions.id("regent_wave"));
    private static final EntityDataAccessor<Integer> POSE=SynchedEntityData.defineId(AshboundRegent.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(AshboundRegent.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Integer> DURATION=SynchedEntityData.defineId(AshboundRegent.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new ServerBossEvent(java.util.UUID.randomUUID(),getDisplayName(),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    private int timer,recovery=40,cycle,phase=1;private BlockPos home;private Vec3 impact;private boolean practice;
    public AshboundRegent(EntityType<? extends AshboundRegent> type,Level l){super(type,l);setPersistenceRequired();xpReward=80;setDropChance(EquipmentSlot.MAINHAND,0);}
    public void setPractice(){practice=true;xpReward=0;}public boolean isPractice(){return practice;}
    @Override public void checkDespawn(){if(!practice)super.checkDespawn();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(POSE,IDLE);b.define(START,0L);b.define(DURATION,1);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,24));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));}
    public int combatPose(){return entityData.get(POSE);}public int phase(){return phase;}public boolean hasArena(){return home!=null;}
    public void setArena(BlockPos pos,int players){home=pos.immutable();getAttribute(Attributes.MAX_HEALTH).setBaseValue(260+60*Math.clamp(players-1,0,3));setHealth(getMaxHealth());}
    public AABB arena(){return new AABB(home==null?blockPosition():home).inflate(22,10,22);}
    private boolean valid(LivingEntity target){return target!=null&&target.isAlive()&&distanceToSqr(target)<1600&&(home==null||arena().contains(target.position()))&&!(target instanceof Player p&&(p.isCreative()||p.isSpectator()));}
    public float animationProgress(float partial){return Math.max(0,Math.min(1,(level().getGameTime()-entityData.get(START)+partial)/Math.max(1f,entityData.get(DURATION))));}
    private void pose(int value,int ticks){entityData.set(POSE,value);entityData.set(START,level().getGameTime());entityData.set(DURATION,ticks);timer=ticks;getNavigation().stop();}
    public static boolean magical(DamageSource source){return source.is(DamageTypes.MAGIC)||source.is(DamageTypes.INDIRECT_MAGIC)||source.is(DamageTypes.LIGHTNING_BOLT)||source.is(DamageTypes.FIREBALL)||source.is(RelicKatanas.PULSE)||source.is(MagicExpansion.PULSE)||source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE);}
    public void ward(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e){
        if(e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        if(combatPose()==WARD){if(magical(e.getSource())&&e.getAmount()>0){pose(EXPOSED,40);recovery=50;CombatFeedback.play(this,"rune_radiance",position().add(0,1.5,0),0xffdf85);}else e.setAmount(e.getAmount()*.3f);}
        else if(combatPose()==EXPOSED)e.setAmount(e.getAmount()*1.2f);
    }
    @Override protected void customServerAiStep(ServerLevel l){
        super.customServerAiStep(l);bar.setProgress(Math.max(0,getHealth()/getMaxHealth()));bar.setName(getDisplayName());int next=getHealth()/getMaxHealth()>2f/3?1:getHealth()/getMaxHealth()>1f/3?2:3;
        if(next!=phase){phase=next;bar.setColor(next==3?BossEvent.BossBarColor.RED:BossEvent.BossBarColor.PURPLE);CombatFeedback.play(this,"regent_awaken",position().add(0,1,0),0xc19be8);}
        if(home!=null&&!arena().contains(position())){var at=Vec3.atBottomCenterOf(home.offset(0,0,6));if(l.noCollision(this,getBoundingBox().move(at.subtract(position()))))snapTo(at.x,at.y,at.z,0,0);}
        if(KatanaDefense.staggered(this)){if(combatPose()!=EXPOSED)pose(EXPOSED,20);getNavigation().stop();return;}
        var target=getTarget();if(!valid(target)){pose(IDLE,0);recovery=40;return;}getLookControl().setLookAt(target,30,30);setAggressive(true);
        if(combatPose()!=IDLE){
            getNavigation().stop();if(timer%5==0){
                if(combatPose()==RIFT&&impact!=null)for(int i=0;i<20;i++){double a=i*Math.PI/10;l.sendParticles(new DustParticleOptions(0xdf4b55,1),impact.x+Math.cos(a)*2.6,impact.y+.15,impact.z+Math.sin(a)*2.6,1,0,0,0,0);}
                else if(combatPose()==SLASH||combatPose()==WARD)l.sendParticles(new DustParticleOptions(combatPose()==SLASH?0x9eeaff:0xffd783,1),getX(),getEyeY()+.2,getZ(),4,.25,.2,.25,0);
            }
            if(--timer<=0){int attack=combatPose();if(attack==SLASH&&distanceToSqr(target)<20&&getSensing().hasLineOfSight(target))target.hurtServer(l,l.damageSources().mobAttack(this),8+phase*2);
                if(attack==RIFT&&impact!=null){var source=new DamageSource(l.registryAccess().getOrThrow(WAVE_DAMAGE),this);for(var victim:l.getEntitiesOfClass(LivingEntity.class,new AABB(impact,impact).inflate(2.6,2,2.6),v->valid(v)&&v!=this&&getSensing().hasLineOfSight(v)))if(victim.position().subtract(impact).multiply(1,0,1).lengthSqr()<6.76)victim.hurtServer(l,source,12+phase*2);l.sendParticles(ParticleTypes.REVERSE_PORTAL,impact.x,impact.y+.4,impact.z,20,1,.4,1,.015);}
                if(attack==SLASH){swing(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.component.SwingAnimation.DEFAULT);pose(SWING,6);}else pose(IDLE,0);impact=null;recovery=phase==3?24:38;
            }return;
        }
        if(recovery>0){recovery--;return;}
        int attack=cycle%3;if(attack==0&&distanceToSqr(target)>16){getNavigation().moveTo(target,1);return;}cycle++;
        if(attack==0){var delta=target.position().subtract(position());setYRot((float)(net.minecraft.util.Mth.atan2(delta.z,delta.x)*180/Math.PI)-90);setYHeadRot(getYRot());yBodyRot=getYRot();pose(SLASH,28-phase*2);playSound(SoundEvents.PLAYER_ATTACK_SWEEP,.6f,.7f);}
        else if(attack==1){impact=target.position();pose(RIFT,40-phase*2);playSound(SoundEvents.EVOKER_PREPARE_ATTACK,.5f,.7f);}
        else{pose(WARD,90);playSound(SoundEvents.AMETHYST_BLOCK_RESONATE,.6f,.65f);}
    }
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void remove(RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
    @Override public void die(DamageSource source){super.die(source);if(isDeadOrDying())bar.removeAllPlayers();}
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("RegentCycle",cycle);out.putBoolean("CreativePractice",practice);if(home!=null)out.store("Nexus",BlockPos.CODEC,home);}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);cycle=Math.max(0,in.getIntOr("RegentCycle",0));home=in.read("Nexus",BlockPos.CODEC).orElse(null);practice=in.getBooleanOr("CreativePractice",false);if(practice)xpReward=0;pose(IDLE,0);recovery=40;setPersistenceRequired();setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);setDropChance(EquipmentSlot.MAINHAND,0);}
}

