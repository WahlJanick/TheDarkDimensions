package at.janic.thedarkdimensions;

import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Dark Acacia retains the existing dimension's logs and planks in saved worlds. */
public final class DarkAcaciaContent {
    private static <T extends Block> DeferredBlock<T> block(String name, Block base, Function<BlockBehaviour.Properties,T> factory) {
        var block=TheDarkDimensions.BLOCKS.registerBlock(name,factory,() -> BlockBehaviour.Properties.ofFullCopy(base));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(block);
        return block;
    }
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_LOG=block("stripped_dark_acacia_log",Blocks.STRIPPED_ACACIA_LOG,RotatedPillarBlock::new);
    public static final DeferredBlock<RotatedPillarBlock> WOOD=block("dark_acacia_wood",Blocks.ACACIA_WOOD,RotatedPillarBlock::new);
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_WOOD=block("stripped_dark_acacia_wood",Blocks.STRIPPED_ACACIA_WOOD,RotatedPillarBlock::new);
    public static final DeferredBlock<StairBlock> STAIRS=block("dark_acacia_stairs",Blocks.ACACIA_STAIRS,p -> new StairBlock(DarkContent.BURNED_PLANKS.get().defaultBlockState(),p));
    public static final DeferredBlock<SlabBlock> SLAB=block("dark_acacia_slab",Blocks.ACACIA_SLAB,SlabBlock::new);
    public static final DeferredBlock<DoorBlock> DOOR=block("dark_acacia_door",Blocks.ACACIA_DOOR,p -> new DoorBlock(BlockSetType.ACACIA,p));
    public static final DeferredBlock<TrapDoorBlock> TRAPDOOR=block("dark_acacia_trapdoor",Blocks.ACACIA_TRAPDOOR,p -> new TrapDoorBlock(BlockSetType.ACACIA,p));
    public static final DeferredBlock<Block> CARVED=block("carved_dark_acacia_log",Blocks.ACACIA_PLANKS,Block::new);
    public static final DeferredBlock<Block> HALF_CARVED=block("half_carved_dark_acacia_log",Blocks.ACACIA_PLANKS,Block::new);
    public static final DeferredBlock<SaplingBlock> SAPLING=block("dark_acacia_sapling",Blocks.ACACIA_SAPLING,p -> new SaplingBlock(
        new TreeGrower("thedarkdimensions:dark_acacia",WeightedList.of(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("burned_tree"))),WeightedList.of(),WeightedList.of(),ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("burned_tree"))),p));
    public static void init() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) -> {
            if(!event.getItemStack().is(net.minecraft.tags.ItemTags.AXES)) return;
            var state=event.getLevel().getBlockState(event.getPos());
            Block target=state.is(DarkContent.BURNED_LOG.get())?STRIPPED_LOG.get():state.is(WOOD.get())?STRIPPED_WOOD.get():null;
            if(target==null) return;
            if(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
                level.setBlock(event.getPos(),target.defaultBlockState().setValue(RotatedPillarBlock.AXIS,state.getValue(RotatedPillarBlock.AXIS)),3);
                level.playSound(null,event.getPos(),net.minecraft.sounds.SoundEvents.AXE_STRIP.value(),net.minecraft.sounds.SoundSource.BLOCKS,1,1);
                event.getItemStack().hurtAndBreak(1,event.getEntity(),event.getHand()==net.minecraft.world.InteractionHand.MAIN_HAND?net.minecraft.world.entity.EquipmentSlot.MAINHAND:net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        });
    }
}
