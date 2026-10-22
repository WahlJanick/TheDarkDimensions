package at.janic.thedarkdimensions;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.*;
import at.janic.thedarkdimensions.worldgen.*;

public final class DarkContent {
    public static final ResourceKey<Level> BURNED_REALM = ResourceKey.create(Registries.DIMENSION, TheDarkDimensions.id("burned_realm"));
    public static final ResourceKey<Level> ECHO_DEPTHS = ResourceKey.create(Registries.DIMENSION, TheDarkDimensions.id("echo_depths"));
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES = DeferredRegister.create(Registries.FEATURE_TYPE, TheDarkDimensions.MOD_ID);
    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, TheDarkDimensions.MOD_ID);
    public static final DeferredBlock<ColoredFallingBlock> BURNED_GRAVEL = TheDarkDimensions.BLOCKS.registerBlock("burned_gravel", p -> new ColoredFallingBlock(new net.minecraft.util.ColorRGBA(0xff252127),p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredItem<BlockItem> BURNED_GRAVEL_ITEM = TheDarkDimensions.ITEMS.registerSimpleBlockItem(BURNED_GRAVEL);
    public static final DeferredBlock<Block> BURNED_COBBLESTONE = block("burned_cobblestone", Blocks.COBBLESTONE);
    public static final DeferredBlock<Block> BURNED_BEDROCK = TheDarkDimensions.BLOCKS.registerSimpleBlock("burned_bedrock", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredBlock<RotatedPillarBlock> BURNED_LOG = TheDarkDimensions.BLOCKS.registerBlock("burned_log", RotatedPillarBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredItem<BlockItem> BURNED_LOG_ITEM = TheDarkDimensions.ITEMS.registerSimpleBlockItem(BURNED_LOG);
    public static final DeferredBlock<Block> BURNED_PLANKS = block("burned_planks", Blocks.OAK_PLANKS);
    public static final DeferredBlock<Block> BURNED_LEAVES = block("burned_leaves", Blocks.OAK_LEAVES);
    public static final DeferredBlock<Block> BURNED_BRICKS = block("burned_bricks", Blocks.STONE_BRICKS);
    public static final DeferredBlock<Block> BURNED_COAL_ORE = block("burned_coal_ore", Blocks.COAL_ORE);
    public static final DeferredBlock<Block> BURNED_IRON_ORE = block("burned_iron_ore", Blocks.IRON_ORE);
    public static final DeferredBlock<Block> IMPROVED_AMETHYST_ORE = litBlock("improved_amethyst_crystal_ore", Blocks.DIAMOND_ORE, 5);
    public static final DeferredBlock<MagmaBlock> EMBER_STONE = TheDarkDimensions.BLOCKS.registerBlock("ember_stone", MagmaBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MAGMA_BLOCK).lightLevel(s -> 8).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredItem<BlockItem> EMBER_STONE_ITEM = TheDarkDimensions.ITEMS.registerSimpleBlockItem(EMBER_STONE);
    public static final DeferredBlock<Block> ECHO_CRYSTAL = litBlock("echo_crystal", Blocks.AMETHYST_BLOCK, 11);
    public static final DeferredBlock<Block> ECHO_RELIC = litBlock("echo_relic", Blocks.OBSIDIAN, 10);
    // No survival item and no recipe: this frame can only be discovered in the world.
    public static final DeferredBlock<Block> IMPROVED_OBSIDIAN = TheDarkDimensions.BLOCKS.registerSimpleBlock("improved_obsidian",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).strength(-1, 3600000).noLootTable().lightLevel(s -> 3));
    public static final DeferredBlock<EchoPortalBlock> ECHO_PORTAL = TheDarkDimensions.BLOCKS.registerBlock("echo_portal", EchoPortalBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).lightLevel(s -> 13));
    public static final DeferredItem<Item> IMPROVED_AMETHYST = TheDarkDimensions.ITEMS.registerSimpleItem("improved_amethyst");
    public static final DeferredItem<ImprovedEchoShardItem> IMPROVED_ECHO_SHARD = TheDarkDimensions.ITEMS.registerItem("improved_echo_shard", ImprovedEchoShardItem::new);
    public static final DeferredHolder<PoiType, PoiType> REALM_PORTAL_POI = POIS.register("realm_portal", () -> new PoiType(Set.copyOf(TheDarkDimensions.IMPROVED_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));
    public static final DeferredHolder<PoiType, PoiType> ECHO_PORTAL_POI = POIS.register("echo_portal", () -> new PoiType(Set.copyOf(ECHO_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));
    static {
        FEATURES.register("wandering_site",()->WanderingSitesFeature.CODEC);
        FEATURES.register("burned_tree", () -> BurnedTreeFeature.CODEC);
        FEATURES.register("echo_ruin", () -> EchoRuinFeature.CODEC);
        FEATURES.register("crystal_growth", () -> CrystalGrowthFeature.CODEC);
    }
    private static DeferredBlock<Block> block(String name, Block vanilla) {
        var b = TheDarkDimensions.BLOCKS.registerSimpleBlock(name, () -> BlockBehaviour.Properties.ofFullCopy(vanilla).mapColor(MapColor.COLOR_BLACK));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(b);
        return b;
    }
    private static DeferredBlock<Block> litBlock(String name, Block vanilla, int light) {
        var b = TheDarkDimensions.BLOCKS.registerSimpleBlock(name, () -> BlockBehaviour.Properties.ofFullCopy(vanilla).mapColor(MapColor.COLOR_BLACK).lightLevel(s -> light));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(b);
        return b;
    }
    public static void init(IEventBus bus) {
        // Saved blocks/items from earlier versions resolve directly to their replacements.
        for(var entry:java.util.Map.of("burned_dirt","minecraft:dirt","burned_stone","minecraft:stone","burned_deepslate","thedarkdimensions:hard_stone").entrySet()) {
            TheDarkDimensions.BLOCKS.addAlias(TheDarkDimensions.id(entry.getKey()),net.minecraft.resources.Identifier.parse(entry.getValue()));
            TheDarkDimensions.ITEMS.addAlias(TheDarkDimensions.id(entry.getKey()),net.minecraft.resources.Identifier.parse(entry.getValue()));
        }
        FEATURES.register(bus); POIS.register(bus);
    }
}
