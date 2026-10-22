package at.janic.thedarkdimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;

@Mod(TheDarkDimensions.MOD_ID)
public final class TheDarkDimensions {
    public static final String MOD_ID = "thedarkdimensions";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<ColoredFallingBlock> BLACK_SAND = BLOCKS.registerBlock("black_sand",
            p -> new ColoredFallingBlock(new ColorRGBA(0xff181818), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.COLOR_BLACK));
    public static final DeferredBlock<ImprovedPortalBlock> IMPROVED_PORTAL = BLOCKS.registerBlock("improved_nether_portal",
            ImprovedPortalBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).lightLevel(s -> 15));
    public static final DeferredItem<BlockItem> BLACK_SAND_ITEM = ITEMS.registerSimpleBlockItem(BLACK_SAND);
    public static final DeferredItem<Item> ASHES = ITEMS.registerSimpleItem("ashes");
    public static final DeferredItem<Item> BURNING_STICK = ITEMS.registerSimpleItem("burned_stick", p -> p.fireResistant());
    public static final DeferredItem<Item> BURNED_STICK = ITEMS.registerSimpleItem("improved_stick");
    public static final DeferredItem<Item> IMPROVED_STICK = ITEMS.registerSimpleItem("reinforced_stick");
    public static final DeferredItem<Item> FIRE_CORE = ITEMS.registerSimpleItem("fire_core", p -> p.fireResistant());
    public static final DeferredItem<Item> EMPTY_RUNESTONE = ITEMS.registerSimpleItem("empty_runestone");
    public static final DeferredItem<Item> ENHANCED_DIAMOND_SWORD = ITEMS.registerSimpleItem("enhanced_diamond_sword", p -> p.sword(ToolMaterial.DIAMOND, 3, -2.4f));
    public static final DeferredItem<Item> ENHANCED_DIAMOND_PICKAXE = ITEMS.registerSimpleItem("enhanced_diamond_pickaxe", p -> p.pickaxe(ToolMaterial.DIAMOND, 1, -2.8f));
    public static final DeferredItem<Item> ENHANCED_DIAMOND_AXE = ITEMS.registerSimpleItem("enhanced_diamond_axe", p -> p.axe(ToolMaterial.DIAMOND, 5, -3f));
    public static final DeferredItem<Item> ENHANCED_DIAMOND_SHOVEL = ITEMS.registerSimpleItem("enhanced_diamond_shovel", p -> p.shovel(ToolMaterial.DIAMOND, 1.5f, -3f));
    public static final DeferredItem<Item> ENHANCED_DIAMOND_HOE = ITEMS.registerSimpleItem("enhanced_diamond_hoe", p -> p.hoe(ToolMaterial.DIAMOND, -3, 0));
    public static final DeferredHolder<EntityType<?>, EntityType<Mummy>> MUMMY = ENTITIES.register("mummy",
            () -> EntityType.Builder.of(Mummy::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("mummy"))));
    public static final DeferredItem<SpawnEggItem> MUMMY_SPAWN_EGG = ITEMS.registerItem("mummy_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(MUMMY.get()));

    public static boolean enhanced(ItemStack stack) {
        return stack.is(ToolContent.IMPROVED_TOOLS);
    }

    static {
        TABS.register("main", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.thedarkdimensions"))
                .icon(() -> FIRE_CORE.get().getDefaultInstance()).displayItems((parameters, output) -> {
                    ITEMS.getEntries().stream().filter(item -> !java.util.Set.of("burned_coal_ore","burned_iron_ore","improved_amethyst_crystal_ore","burned_dirt","burned_gravel","burned_stone","burned_deepslate","burned_cobblestone","burned_bedrock","burned_leaves","burned_bricks","black_sand").contains(item.getId().getPath())).forEach(item -> output.accept(item.get()));
                }).build());
    }

    public TheDarkDimensions(IEventBus bus) {
        BurningStickRecipe.init(bus);
        KatanaEffects.init(bus);
        SpellContent.init(bus);
        MagicContent.init(bus);
        PerkContent.init(bus);
        MineralContent.init();
        LightContent.init();
        KatanaContent.init();
        RelicKatanas.init();
        EnderEyeRecipe.init(bus);
        EndProgression.init();
        ArmoryContent.init();
        BloodNetheriteContent.init();
        Mindshift.init();
        BackEquipment.init(bus);
        ExplorationContent.init(bus);
        at.janic.thedarkdimensions.resonance.ResonanceContent.init(bus);
        at.janic.thedarkdimensions.finale.FinaleContent.init(bus);
        KatanaCombos.init();
        KatanaDefense.init(bus);
        KatanaBossContent.init(bus);
        Ashfall.init();
        DevilContent.init(bus);
        CaveContent.init(bus);
        VeilwoodContent.init(bus);
        MobExpansionContent.init(bus);
        DarkContent.init(bus);
        DarkAcaciaContent.init();
        RevisionContent.init();
        RuinsContent.init();
        SolarContent.init(bus);
        ToolContent.init();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        TABS.register(bus);
        bus.addListener((EntityAttributeCreationEvent event) -> event.put(MUMMY.get(), net.minecraft.world.entity.monster.zombie.Zombie.createAttributes().build()));
        bus.addListener((RegisterSpawnPlacementsEvent event) -> event.register(MUMMY.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkSurfaceMonstersSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE));
        NeoForge.EVENT_BUS.register(new GameplayEvents());
    }
}
