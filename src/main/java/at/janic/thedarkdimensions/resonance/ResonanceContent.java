package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class ResonanceContent {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,TheDarkDimensions.MOD_ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURES=DeferredRegister.create(Registries.STRUCTURE_TYPE,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<StructureType<?>,StructureType<CaveTrialStructure>> TRIAL_STRUCTURE=STRUCTURES.register("cave_trial",()->()->CaveTrialStructure.CODEC);
    public static final DeferredBlock<DormantRunestoneBlock> RUNESTONE=TheDarkDimensions.BLOCKS.registerBlock("dormant_runestone",DormantRunestoneBlock::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS).strength(-1,3600000).noLootTable().lightLevel(s->6));
    public static final DeferredBlock<TrialSealBlock> SEAL=TheDarkDimensions.BLOCKS.registerBlock("trial_seal",TrialSealBlock::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS).strength(-1,3600000).noLootTable().lightLevel(s->s.getValue(TrialSealBlock.LIT)?12:3));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TrialController>> CONTROLLER=BLOCK_ENTITIES.register("dormant_runestone",()->new BlockEntityType<>(TrialController::new,RUNESTONE.get()));
    public static final DeferredBlock<RunicBenchBlock> BENCH=TheDarkDimensions.BLOCKS.registerBlock("runic_binding_bench",RunicBenchBlock::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(3.5f).noOcclusion());
    public static final DeferredHolder<MenuType<?>,MenuType<RunicBenchMenu>> BENCH_MENU=SpellContent.MENUS.register("runic_binding",()->new MenuType<>(RunicBenchMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final List<DeferredItem<Item>> SIGILS=List.of(simple("ember_trial_sigil"),simple("tide_trial_sigil"),simple("radiance_trial_sigil"));
    public static final List<DeferredItem<Item>> RUNES=List.of(simple("ember_rune"),simple("tide_rune"),simple("radiance_rune"),simple("chronus_rune"));
    public static final DeferredItem<FieldJournal> JOURNAL=TheDarkDimensions.ITEMS.registerItem("ashbound_field_journal",FieldJournal::new,p->p.stacksTo(1).rarity(Rarity.UNCOMMON));
    static {TheDarkDimensions.ITEMS.registerSimpleBlockItem(RUNESTONE);TheDarkDimensions.ITEMS.registerSimpleBlockItem(SEAL);TheDarkDimensions.ITEMS.registerSimpleBlockItem(BENCH);}
    private static DeferredItem<Item> simple(String id){return TheDarkDimensions.ITEMS.registerSimpleItem(id,p->p.rarity(Rarity.UNCOMMON));}
    public static void init(IEventBus bus){BLOCK_ENTITIES.register(bus);STRUCTURES.register(bus);ResonanceProgress.init();AncientRunes.init();TrialController.init();}
    private ResonanceContent(){}
}
