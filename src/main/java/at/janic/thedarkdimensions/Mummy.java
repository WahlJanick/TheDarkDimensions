package at.janic.thedarkdimensions;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/** Wrapped desert undead; zombie AI with sunlight and drowning-conversion immunity. */
public final class Mummy extends Zombie {
    public Mummy(EntityType<? extends Zombie> type,Level level) { super(type,level); }
    @Override protected boolean isSunSensitive() { return false; }
    @Override protected boolean convertsInWater() { return false; }
    @Override protected int decreaseAirSupply(int air) { return air; }
    @Override public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.Entity target) {
        boolean hit=super.doHurtTarget(level,target);
        if(hit&&target instanceof net.minecraft.world.entity.LivingEntity living) {
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.HUNGER,160),this);
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,80),this);
        }
        return hit;
    }
}
