package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.tags.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.*;

/** Timing is measured on the server. Holding guard never renews its parry window. */
public final class KatanaDefense {
    public static final int PERFECT_PARRY=3,PARRY_WINDOW=8,GUARD_RECOVERY=12,DODGE_WINDOW=8,PERFECT_DODGE=2,DODGE_RECOVERY=35;
    public static final TagKey<EntityType<?>> UNPARRYABLE=TagKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("katana_unparryable"));
    private static final class State {
        final ItemStack weapon;final ResourceKey<Level> dimension;
        long guardStart=Long.MIN_VALUE,guardReady,dodgeStart=Long.MIN_VALUE,dodgeReady,counterUntil;
        boolean parried,dodgeReward,masterCounter,attackActive;int steps,direction;
        Vec3 movement;UUID counterTarget,attemptTarget,landedTarget,secondTarget;long attemptTick;DamageSource counterSource;
        State(ServerPlayer p){weapon=p.getMainHandItem();dimension=p.level().dimension();}
    }
    private static final WeakHashMap<ServerPlayer,State> STATES=new WeakHashMap<>();
    private static final WeakHashMap<LivingEntity,Long> STAGGERS=new WeakHashMap<>();
    // Recovery survives weapon swaps; changing equipment cannot reset either cooldown.
    private static final WeakHashMap<ServerPlayer,long[]> RECOVERY=new WeakHashMap<>();
    public static boolean katana(ItemStack stack){return stack.getItem() instanceof KatanaItem||stack.getItem() instanceof StormveilItem;}
    private static boolean available(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&p.containerMenu==p.inventoryMenu&&!p.isPassenger()&&!p.isFallFlying()&&katana(p.getMainHandItem());}
    private static State state(ServerPlayer p){var s=STATES.get(p);return s!=null&&available(p)&&s.weapon==p.getMainHandItem()&&s.dimension.equals(p.level().dimension())?s:null;}
    private static State prepare(ServerPlayer p){var s=state(p);if(s==null){s=new State(p);var recovery=RECOVERY.get(p);if(recovery!=null){s.guardReady=recovery[0];s.dodgeReady=recovery[1];}STATES.put(p,s);}return s;}
    public static boolean guard(ServerPlayer p){
        if(!available(p))return false;var s=prepare(p);long now=p.level().getGameTime();
        if(now<s.guardReady||dodging(p))return false;
        clearCounter(p,s);s.guardStart=now;s.guardReady=now+GUARD_RECOVERY;s.parried=false;
        RECOVERY.put(p,new long[]{s.guardReady,s.dodgeReady});p.startUsingItem(InteractionHand.MAIN_HAND);return true;
    }
    public static boolean dodging(ServerPlayer p){var s=state(p);long now=p.level().getGameTime();return s!=null&&s.dodgeStart!=Long.MIN_VALUE&&now-s.dodgeStart>=0&&now-s.dodgeStart<DODGE_WINDOW;}
    public static boolean dodge(ServerPlayer p,int direction){
        if(direction<0||direction>8||!available(p)||!p.onGround()||!p.getInBlockState().getFluidState().isEmpty()||p.getAbilities().flying||(!p.isCreative()&&p.getFoodData().getFoodLevel()<2))return false;
        var s=prepare(p);long now=p.level().getGameTime();if(now<s.dodgeReady||dodging(p))return false;
        int forward=direction/3-1,side=direction%3-1;if(forward==0&&side==0)forward=-1;
        Vec3 facing=Vec3.directionFromRotation(0,p.getYRot());Vec3 right=new Vec3(-facing.z,0,facing.x);
        Vec3 move=facing.scale(forward).add(right.scale(side)).normalize().scale(.65);
        if(!freeStep(p,move))return false;
        s.dodgeStart=now;s.dodgeReady=now+DODGE_RECOVERY;s.guardReady=Math.max(s.guardReady,now+DODGE_WINDOW);s.guardStart=Long.MIN_VALUE;
        s.movement=move;s.steps=4;s.direction=direction;s.dodgeReward=false;s.counterTarget=null;s.secondTarget=null;s.masterCounter=false;
        RECOVERY.put(p,new long[]{s.guardReady,s.dodgeReady});p.stopUsingItem();p.causeFoodExhaustion(.6f);
        feedback(p,2,direction,"dodge");p.level().playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.3f,1.5f);return true;
    }
    private static boolean freeStep(ServerPlayer p,Vec3 movement){
        var at=net.minecraft.core.BlockPos.containing(p.position().add(movement));
        return p.level().hasChunkAt(at)&&p.level().getWorldBorder().isWithinBounds(at)&&p.level().noCollision(p,p.getBoundingBox().expandTowards(movement))&&p.level().getBlockState(at.below()).isSolid();
    }
    private static boolean melee(DamageSource source){return source.getDirectEntity() instanceof LivingEntity&&(source.is(DamageTypes.MOB_ATTACK)||source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)||source.is(DamageTypes.PLAYER_ATTACK)||source.is(DamageTypeTags.IS_PLAYER_ATTACK))&&!source.is(DamageTypeTags.IS_PROJECTILE)&&!source.is(DamageTypeTags.IS_EXPLOSION);}
    private static boolean attack(DamageSource source){return source.getDirectEntity()!=null&&!source.is(DamageTypes.THORNS)&&!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)&&(source.getEntity() instanceof LivingEntity||source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile);}
    private static boolean facing(ServerPlayer p,LivingEntity attacker){
        var difference=attacker.position().subtract(p.position()).multiply(1,0,1);
        return difference.lengthSqr()>.001&&Vec3.directionFromRotation(0,p.getYRot()).dot(difference.normalize())>=.35;
    }
    private static boolean guarding(ServerPlayer p,State s){return s.guardStart!=Long.MIN_VALUE&&p.isUsingItem()&&p.getUsedItemHand()==InteractionHand.MAIN_HAND&&p.getUseItem()==s.weapon;}
    private static void stagger(LivingEntity target,int ticks){
        STAGGERS.put(target,target.level().getGameTime()+ticks);target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,ticks,4));
        if(target instanceof Mob mob)mob.getNavigation().stop();
    }
    public static boolean staggered(LivingEntity entity){return STAGGERS.getOrDefault(entity,Long.MIN_VALUE)>entity.level().getGameTime();}
    private static void counter(ServerPlayer p,State s,LivingEntity attacker,boolean perfectParry){
        s.counterTarget=attacker.getUUID();s.counterUntil=p.level().getGameTime()+20;s.attemptTarget=null;s.counterSource=null;s.secondTarget=null;
        s.masterCounter=perfectParry&&KatanaEnchantments.secondCounter(s.weapon,p.level(),p.getRandom());
    }
    private static boolean counterHit(ServerPlayer p,State s,LivingEntity target){return s!=null&&s.counterTarget!=null&&s.counterTarget.equals(target.getUUID())&&p.level().getGameTime()<s.counterUntil&&!guarding(p,s)&&!dodging(p);}
    private static void clearCounter(ServerPlayer p,State s){
        if(s.counterTarget!=null||s.secondTarget!=null)p.sendSystemMessage(Component.empty(),true);
        s.counterTarget=null;s.secondTarget=null;s.attemptTarget=null;s.landedTarget=null;s.counterSource=null;s.masterCounter=false;s.attackActive=false;s.counterUntil=0;
    }
    public static void attempted(ServerPlayer p,Entity entity){
        var s=state(p);
        if(s==null||s.counterTarget==null||guarding(p,s)||dodging(p))return;
        s.attackActive=true;
        s.attemptTarget=entity instanceof LivingEntity target&&counterHit(p,s,target)?target.getUUID():null;
        if(s.attemptTarget==null)s.masterCounter=false;
        s.attemptTick=p.level().getGameTime();s.counterTarget=null;p.sendSystemMessage(Component.empty(),true);
    }
    public static void miss(ServerPlayer p){var s=state(p);if(s!=null&&!guarding(p,s)&&!dodging(p)){if(s.counterTarget!=null)p.sendSystemMessage(Component.empty(),true);s.counterTarget=null;s.attemptTarget=null;s.secondTarget=null;s.masterCounter=false;}}
    private static void readySecond(ServerPlayer p,State s){
        if(s.secondTarget!=null&&p.level().getGameTime()<s.counterUntil){s.counterTarget=s.secondTarget;p.sendSystemMessage(Component.translatable("combat.thedarkdimensions.second_counter"),true);}
        s.secondTarget=null;
    }
    public static void finishedAttempt(ServerPlayer p){var s=state(p);if(s!=null){s.attackActive=false;s.masterCounter=false;s.attemptTarget=null;s.counterSource=null;s.landedTarget=null;readySecond(p,s);}}
    private static void incoming(LivingIncomingDamageEvent e){
        if(e.isCanceled()||e.getAmount()<=0||e.getEntity().level().isClientSide())return;var source=e.getSource();
        if(melee(source)&&source.getEntity() instanceof LivingEntity attacker){
            if(STAGGERS.getOrDefault(attacker,Long.MIN_VALUE)>attacker.level().getGameTime()){e.setCanceled(true);return;}
            if(attacker instanceof ServerPlayer p){var s=state(p);if(s!=null&&(guarding(p,s)||dodging(p))){e.setCanceled(true);return;}
                if(counterHit(p,s,e.getEntity())||s!=null&&s.attemptTarget!=null&&s.attemptTarget.equals(e.getEntity().getUUID())&&s.attemptTick==p.level().getGameTime()){
                    // Spend before damage resolution: a blocked hit or a sweeping attack cannot reuse the bonus.
                    s.counterTarget=null;s.attemptTarget=null;s.landedTarget=e.getEntity().getUUID();s.counterSource=source;e.setAmount(e.getAmount()+3);
                }else if(s!=null){s.counterTarget=null;s.attemptTarget=null;s.masterCounter=false;}
            }
        }
        if(!(e.getEntity() instanceof ServerPlayer p))return;var s=state(p);if(s==null)return;long now=p.level().getGameTime();
        if(dodging(p)&&attack(source)&&source.getEntity()!=p){
            e.setCanceled(true);
            if(!s.dodgeReward){
                s.dodgeReward=true;boolean perfect=now-s.dodgeStart<PERFECT_DODGE;
                if(perfect){if(source.getEntity() instanceof LivingEntity attacker)counter(p,s,attacker,false);at.janic.thedarkdimensions.resonance.AncientRunes.perfectDodge(p);feedback(p,3,s.direction,"perfect_dodge");spark(p,0xffdf79);}
            }return;
        }
        if(!guarding(p,s)||!melee(source)||!(source.getEntity() instanceof LivingEntity attacker)||attacker==p||!facing(p,attacker))return;
        if(attacker.typeHolder().is(UNPARRYABLE)||source.is(DamageTypeTags.BYPASSES_SHIELD)){feedback(p,5,0,"unparryable");spark(p,0xc22f3e);return;}
        long elapsed=now-s.guardStart;
        if(!s.parried&&elapsed>=0&&elapsed<PARRY_WINDOW){
            boolean perfect=elapsed<PERFECT_PARRY;s.parried=true;e.setCanceled(true);stagger(attacker,perfect?20:8);
            if(perfect)counter(p,s,attacker,true);else clearCounter(p,s);
            p.causeFoodExhaustion(.25f);s.weapon.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);
            feedback(p,perfect?1:0,0,perfect?"perfect_parry":"parry");spark(p,perfect?0x90e9ff:0xdfdfdf);
            if(!perfect)p.level().playSound(null,p.blockPosition(),SoundEvents.SHIELD_BLOCK.value(),SoundSource.PLAYERS,.65f,1.15f);
        }else{
            e.setAmount(e.getAmount()*.4f);p.causeFoodExhaustion(.4f);s.weapon.hurtAndBreak(Math.max(1,(int)Math.ceil(e.getOriginalAmount()/4)),p,EquipmentSlot.MAINHAND);
            feedback(p,5,0,"guard");p.level().playSound(null,p.blockPosition(),SoundEvents.SHIELD_BLOCK.value(),SoundSource.PLAYERS,.35f,.8f);
        }
    }
    private static void landed(LivingDamageEvent.Post e){
        if(!melee(e.getSource())||!(e.getSource().getEntity() instanceof ServerPlayer p))return;
        var s=state(p);var target=e.getEntity();if(s==null||s.counterSource!=e.getSource()||!target.getUUID().equals(s.landedTarget))return;
        s.counterSource=null;s.landedTarget=null;boolean second=s.masterCounter;s.masterCounter=false;if(e.getHealthDamage()<=0)return;
        feedback(p,4,0,"counter");KatanaFinisherEffects.play(p.level(),s.weapon,p,target);
        if(second&&target.isAlive()){
            s.secondTarget=target.getUUID();if(!s.attackActive)readySecond(p,s);
        }
        if(!target.isAlive())return;
        if(s.weapon.is(KatanaContent.FROSTGLEAM))target.addEffect(new MobEffectInstance(KatanaEffects.FROSTED,40,1),p);
        else if(s.weapon.is(KatanaContent.BLOODGRIEF))target.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,140),p);
        else if(s.weapon.is(KatanaContent.SOULEATER))target.addEffect(new MobEffectInstance(KatanaEffects.SOULLESS,80),p);
        else if(s.weapon.is(SolarContent.STORMVEIL))KatanaCombos.chain(p.level(),target,p);
    }
    private static void spark(ServerPlayer p,int color){var at=p.getEyePosition().add(p.getLookAngle().scale(.6));p.level().sendParticles(new DustParticleOptions(color,1.1f),at.x,at.y,at.z,12,.25,.25,.25,.01);}
    private static void feedback(ServerPlayer p,int kind,int direction,String message){
        if(kind==1)at.janic.thedarkdimensions.finale.CombatFeedback.play(p,"perfect_parry",p.getEyePosition(),0x9eeaff);
        if(kind==3){at.janic.thedarkdimensions.finale.CombatFeedback.play(p,"perfect_dodge",p.position().add(0,1,0),0xffdc93);at.janic.thedarkdimensions.finale.FinalProgress.perfectDodge(p);MagicExpansion.perfectDodge(p);}
        p.sendSystemMessage(Component.translatable("combat.thedarkdimensions."+message),true);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new DefenseNetwork.Visual(p.getId(),kind,direction));
    }
    private static void tick(ServerPlayer p){
        if(p.isAlive()&&!p.isSpectator()&&p.tickCount%10==0&&katana(p.getMainHandItem()))for(var mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(5),m->m.getTarget()==p&&m.typeHolder().is(UNPARRYABLE)&&p.hasLineOfSight(m))){
            p.level().sendParticles(p,new DustParticleOptions(0xc22f3e,.9f),false,false,mob.getX(),mob.getEyeY()+.25,mob.getZ(),2,.1,.1,.1,0);
        }
        var stored=STATES.get(p);if(stored==null)return;var s=state(p);
        if(s==null){STATES.remove(p);return;}
        if(s.counterTarget!=null&&p.level().getGameTime()>=s.counterUntil){s.counterTarget=null;s.secondTarget=null;s.masterCounter=false;p.sendSystemMessage(Component.empty(),true);}
        if(s.steps>0&&dodging(p)){
            if(!freeStep(p,s.movement)){s.steps=0;s.dodgeStart=Long.MIN_VALUE;return;}
            var to=p.position().add(s.movement);p.teleportTo(to.x,to.y,to.z);s.steps--;
            p.level().sendParticles(ParticleTypes.CLOUD,p.getX(),p.getY()+.2,p.getZ(),2,.1,.05,.1,.005);
        }
    }
    public static void init(IEventBus bus){
        bus.addListener(DefenseNetwork::register);var events=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        events.addListener(net.neoforged.bus.api.EventPriority.LOWEST,KatanaDefense::incoming);events.addListener(KatanaDefense::landed);
        events.addListener((net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock e)->{if(e.getEntity() instanceof ServerPlayer p)miss(p);});
        events.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e)->{if(e.getEntity() instanceof ServerPlayer p)tick(p);});
        events.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e)->{if(e.getEntity() instanceof ServerPlayer p){STATES.remove(p);RECOVERY.remove(p);}});
    }
}
