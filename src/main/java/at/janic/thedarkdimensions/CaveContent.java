package at.janic.thedarkdimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.food.Foods;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import at.janic.thedarkdimensions.worldgen.ElementalCaveFeature;

public final class CaveContent {
    public static final DeferredBlock<Block> FIRE_CAVE_STONE = block("fire_cave_stone",Blocks.STONE,2);
    public static final DeferredBlock<AmethystClusterBlock> FIRE_CRYSTAL = crystal("fire_crystal",10);
    public static final DeferredBlock<Block> WATER_CAVE_STONE = block("water_cave_stone",Blocks.STONE,0);
    public static final DeferredBlock<AmethystClusterBlock> WATER_CRYSTAL = crystal("water_crystal",6);
    public static final DeferredBlock<Block> GLOWING_CAVE_STONE = block("glowing_cave_stone",Blocks.STONE,5);
    public static final DeferredBlock<AmethystClusterBlock> GLOWING_CRYSTAL = crystal("glowing_crystal",14);
    public static final DeferredBlock<HoneyBlock> GLOWING_HONEY_BLOCK = TheDarkDimensions.BLOCKS.registerBlock("glowing_honey_block",HoneyBlock::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.HONEY_BLOCK).lightLevel(s -> 12));
    public static final DeferredItem<BlockItem> HONEY_BLOCK_ITEM = TheDarkDimensions.ITEMS.registerSimpleBlockItem(GLOWING_HONEY_BLOCK);
    public static final DeferredBlock<Block> SPELL_RUNE_STONE = block("spell_rune_stone",Blocks.STONE_BRICKS,8);
    public static final DeferredItem<Item> WATER_CORE = TheDarkDimensions.ITEMS.registerSimpleItem("water_core",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> LIGHTNING_CORE = TheDarkDimensions.ITEMS.registerSimpleItem("lightning_core",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SPELL_CORE = TheDarkDimensions.ITEMS.registerSimpleItem("spell_core",p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<Item> GLOWING_HONEY = TheDarkDimensions.ITEMS.registerSimpleItem("glowing_honey",p -> p.stacksTo(16).food(Foods.HONEY_BOTTLE,
            Consumables.defaultDrink().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.GLOWING,1200))).build()).usingConvertsTo(Items.GLASS_BOTTLE));
    public static final DeferredHolder<EntityType<?>,EntityType<FireMage>> FIRE_MAGE = TheDarkDimensions.ENTITIES.register("fire_mage",() -> EntityType.Builder.of(FireMage::new,MobCategory.MONSTER).sized(0.6f,1.95f).fireImmune().clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("fire_mage"))));
    public static final DeferredHolder<EntityType<?>,EntityType<GlowingBee>> GLOWING_BEE = TheDarkDimensions.ENTITIES.register("glowing_bee",() -> EntityType.Builder.of(GlowingBee::new,MobCategory.CREATURE).sized(0.7f,0.6f).clientTrackingRange(8).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("glowing_bee"))));
    public static final DeferredItem<SpawnEggItem> FIRE_MAGE_EGG=TheDarkDimensions.ITEMS.registerItem("fire_mage_spawn_egg",SpawnEggItem::new,p -> p.spawnEgg(FIRE_MAGE.get()));
    public static final DeferredItem<SpawnEggItem> GLOWING_BEE_EGG=TheDarkDimensions.ITEMS.registerItem("glowing_bee_spawn_egg",SpawnEggItem::new,p -> p.spawnEgg(GLOWING_BEE.get()));
    private static DeferredBlock<Block> block(String name,Block vanilla,int light) {
        var b=TheDarkDimensions.BLOCKS.registerSimpleBlock(name,() -> BlockBehaviour.Properties.ofFullCopy(vanilla).lightLevel(s -> light));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(b);return b;
    }
    public static void init(IEventBus bus) {
        ElementalStoneContent.init();
        // Vanilla cluster geometry, hitboxes, attachment and waterlogging.
        DarkContent.FEATURES.register("elemental_cave",() -> ElementalCaveFeature.CODEC);
        bus.addListener((net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent e) -> {
            e.register(FIRE_MAGE.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    net.minecraft.world.entity.monster.Monster::checkAnyLightMonsterSpawnRules,net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
            e.register(GLOWING_BEE.get(),net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    (type,level,reason,pos,random) -> level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolid(),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
        bus.addListener((EntityAttributeCreationEvent e) -> {
            e.put(FIRE_MAGE.get(),Witch.createAttributes().build());e.put(GLOWING_BEE.get(),Bee.createAttributes().build());
        });
    }
    private CaveContent() {}
    private static DeferredBlock<AmethystClusterBlock> crystal(String name,int light) {
        var b=TheDarkDimensions.BLOCKS.<AmethystClusterBlock>registerBlock(name,p -> new AshfallCrystalBlock(7.0f,10.0f,p),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER).lightLevel(s -> AshfallCrystalBlock.light(s,light)));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(b);return b;
    }
}
