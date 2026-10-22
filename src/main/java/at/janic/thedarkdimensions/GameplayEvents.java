package at.janic.thedarkdimensions;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class GameplayEvents {
    @SubscribeEvent
    public void devilFireProtection(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)&&DevilArmor.complete(event.getEntity()))event.setCanceled(true);
    }
    private static final AttributeModifier HAMMER_FLIGHT = new AttributeModifier(TheDarkDimensions.id("held_hammer_flight"), 1, AttributeModifier.Operation.ADD_VALUE);

    @SubscribeEvent
    public void hammerFlight(net.neoforged.neoforge.event.ItemAttributeModifierEvent event) {
        if (ArmoryContent.canGlide(event.getItemStack()))
            event.addModifier(net.neoforged.neoforge.common.NeoForgeMod.GLIDING_FLIGHT, HAMMER_FLIGHT, net.minecraft.world.entity.EquipmentSlotGroup.HAND);
    }

    @SubscribeEvent
    public void creeperDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        var mob=event.getEntity();
        var blood=mob instanceof net.minecraft.world.entity.monster.Creeper?KatanaContent.CREEPERBLOOD:
            (mob instanceof net.minecraft.world.entity.monster.zombie.Zombie || mob.getType().builtInRegistryHolder().is(ZOMBIE_BLOOD_SOURCES))?KatanaContent.ZOMBIEBLOOD:null;
        if(blood==null ||!(mob.level() instanceof ServerLevel level)
            ||!level.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS))return;
        // Add to the collected drops so normal cancellation and pickup rules still apply.
        if(mob.getRandom().nextFloat()<0.05f)
            event.getDrops().add(new ItemEntity(level,mob.getX(),mob.getY(),mob.getZ(),new ItemStack(blood.get())));
    }
    private static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> ZOMBIE_BLOOD_SOURCES =
        net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,TheDarkDimensions.id("zombie_blood_sources"));
    @SubscribeEvent
    public void milkCannotCureBlood(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove event) {
        if(event.getEffect().is(KatanaEffects.BLOOD.getKey())&&event.getEntity().getUseItem().is(net.minecraft.world.item.Items.MILK_BUCKET))event.setCanceled(true);
    }
    private static final AttributeModifier REACH = new AttributeModifier(TheDarkDimensions.id("enhanced_handle"), 2,
            AttributeModifier.Operation.ADD_VALUE);

    @SubscribeEvent
    public void playerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        boolean enhanced = TheDarkDimensions.enhanced(player.getMainHandItem());
        for (var attribute : java.util.List.of(Attributes.BLOCK_INTERACTION_RANGE, Attributes.ENTITY_INTERACTION_RANGE)) {
            var instance = player.getAttribute(attribute);
            if (instance == null) continue;
            if (enhanced && !instance.hasModifier(REACH.id())) instance.addTransientModifier(REACH);
            if (!enhanced && instance.hasModifier(REACH.id())) instance.removeModifier(REACH.id());
        }
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.getMainHandItem().is(TheDarkDimensions.BURNING_STICK) || player.getOffhandItem().is(TheDarkDimensions.BURNING_STICK)) {
            if (player.tickCount % 20 == 0) player.hurtServer(level, player.damageSources().inFire(), 1);
            if (player.tickCount % 4 == 0) level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1,
                    player.getZ(), 2, 0.3, 0.3, 0.3, 0.01);
        }
    }

    @SubscribeEvent
    public void itemTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ItemEntity item) || !(item.level() instanceof ServerLevel level)
                || !item.getItem().is(TheDarkDimensions.BURNING_STICK)) return;
        if (item.isInWater()) {
            // Replace the complete stack in place: preserves velocity, owner and pickup delay, with no duplication.
            item.setItem(new ItemStack(TheDarkDimensions.BURNED_STICK.get(), item.getItem().getCount()));
            item.clearFire();
            level.sendParticles(ParticleTypes.CLOUD, item.getX(), item.getY(), item.getZ(), 8, 0.1, 0.1, 0.1, 0.02);
        } else if (item.tickCount % 5 == 0) {
            level.sendParticles(ParticleTypes.FLAME, item.getX(), item.getY() + 0.15, item.getZ(), 1, 0.04, 0.06, 0.04, 0);
        }
    }
}
