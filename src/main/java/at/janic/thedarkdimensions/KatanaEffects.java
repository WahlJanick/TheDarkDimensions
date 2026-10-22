package at.janic.thedarkdimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class KatanaEffects {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,net.minecraft.core.particles.SimpleParticleType> BLOOD_DROP=PARTICLES.register("blood_drop",()->new net.minecraft.core.particles.SimpleParticleType(false));
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<MobEffect,MobEffect> FROSTED=EFFECTS.register("frosted",() -> new Frosted());
    public static final DeferredHolder<MobEffect,MobEffect> BLOOD=EFFECTS.register("blood",() -> new Blood());
    public static final DeferredHolder<MobEffect,MobEffect> SOULLESS=EFFECTS.register("soulless",() -> new Soulless());
    public static void init(IEventBus bus) { PARTICLES.register(bus);EFFECTS.register(bus); }
    private static final class Frosted extends MobEffect {
        Frosted() {
            super(MobEffectCategory.HARMFUL,0x8ad8ff);
            addAttributeModifier(Attributes.MOVEMENT_SPEED,TheDarkDimensions.id("frosted_slowdown"),-0.8,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
        @Override public boolean shouldApplyEffectTickThisTick(int ticks,int amplifier) { return ticks%20==0; }
        @Override public boolean applyEffectTick(ServerLevel level,LivingEntity target,int amplifier) {
            target.hurtServer(level,target.damageSources().freeze(),0.5f);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,target.getX(),target.getY()+0.8,target.getZ(),3,0.3,0.4,0.3,0.01);
            return true;
        }
    }
    private static final class Blood extends MobEffect {
        Blood() { super(MobEffectCategory.HARMFUL,0xb51424); }
        @Override public net.minecraft.core.particles.ParticleOptions createParticleOptions(MobEffectInstance effect){return BLOOD_DROP.get();}
        @Override public boolean shouldApplyEffectTickThisTick(int ticks,int amplifier) { return ticks%25==0; }
        @Override public boolean applyEffectTick(ServerLevel level,LivingEntity target,int amplifier) {
            target.hurtServer(level,target.damageSources().magic(),1);
            level.sendParticles(BLOOD_DROP.get(),target.getX(),target.getY()+target.getBbHeight()*.55,target.getZ(),5,target.getBbWidth()*.3,target.getBbHeight()*.2,target.getBbWidth()*.3,.02);
            return true;
        }
    }
    private static final class Soulless extends MobEffect {
        Soulless() { super(MobEffectCategory.HARMFUL,0x79c5d1); }
        @Override public boolean shouldApplyEffectTickThisTick(int ticks,int amplifier) { return true; }
        @Override public void onEffectStarted(LivingEntity target,int amplifier) {
            if(target instanceof net.minecraft.world.entity.player.Player)
                target.addEffect(new MobEffectInstance(MobEffects.NAUSEA,60,0,false,false,false));
        }
        @Override public boolean applyEffectTick(ServerLevel level,LivingEntity target,int amplifier) {
            if(target instanceof Mob mob) {
                mob.setTarget(null);mob.getNavigation().stop();
                mob.setYRot(mob.getYRot()+(float)Math.sin(mob.tickCount*0.4)*12);
                mob.setYHeadRot(mob.getYRot());
            }
            return true;
        }
    }
    private KatanaEffects() {}
}
