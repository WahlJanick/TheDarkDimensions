package at.janic.thedarkdimensions;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class SolarContent {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheDarkDimensions.MOD_ID);
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES=DeferredRegister.create(Registries.PARTICLE_TYPE,TheDarkDimensions.MOD_ID);
    public static final DeferredItem<Item> SOLAR_ESSENCE=TheDarkDimensions.ITEMS.registerSimpleItem("solar_essence",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<PocketKeyItem> POCKET_KEY=TheDarkDimensions.ITEMS.registerItem("pocket_dimension_key",PocketKeyItem::new,p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredBlock<SolarForgeBlock> SOLAR_FORGE=TheDarkDimensions.BLOCKS.registerBlock("solar_forge",SolarForgeBlock::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE));
    public static final DeferredItem<BlockItem> SOLAR_FORGE_ITEM=TheDarkDimensions.ITEMS.registerSimpleBlockItem(SOLAR_FORGE);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SolarForgeBlockEntity>> SOLAR_FORGE_ENTITY=BLOCK_ENTITIES.register("solar_forge",() -> new BlockEntityType<>(SolarForgeBlockEntity::new,SOLAR_FORGE.get()));
    public static final DeferredBlock<Block> POCKET_BOUNDARY=TheDarkDimensions.BLOCKS.registerSimpleBlock("pocket_boundary",() -> BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BRICKS).strength(-1,3600000).noLootTable().lightLevel(s -> 15));
    public static final DeferredItem<StormveilItem> STORMVEIL=TheDarkDimensions.ITEMS.registerItem("stormveil",StormveilItem::new,p -> p.sword(ToolMaterial.NETHERITE,4,-2.5f).durability(1600).enchantable(20).repairable(SOLAR_ESSENCE.get()).fireResistant().rarity(Rarity.EPIC));
    public static final ResourceKey<DamageType> STORMVEIL_ARC=ResourceKey.create(Registries.DAMAGE_TYPE,TheDarkDimensions.id("stormveil_arc"));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,SimpleParticleType> STORMVEIL_SWEEP=PARTICLES.register("stormveil_sweep",() -> new SimpleParticleType(false));
    public static final DeferredHolder<net.minecraft.core.particles.ParticleType<?>,SimpleParticleType> STORMVEIL_CRIT=PARTICLES.register("stormveil_crit",() -> new SimpleParticleType(false));
    public static void init(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);PARTICLES.register(bus);PocketDimensions.init();
    }
}
