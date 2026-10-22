package at.janic.thedarkdimensions;

import at.janic.thedarkdimensions.worldgen.SurfaceCrystalRocksFeature;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Harvestable light sources for the dark surface. */
public final class LightContent {
    public static final DeferredBlock<Block> GLOWSTONE_VEIN_STONE=stone("glowstone_vein_stone",12);
    public static final DeferredBlock<Block> GLOWSTONE_CRYSTAL_BLOCK=stone("glowstone_crystal_block",15);
    public static final DeferredBlock<Block> PALE_GLOWSTONE=stone("pale_glowstone",10);
    private static DeferredBlock<Block> stone(String name,int light) {
        var block=TheDarkDimensions.BLOCKS.registerSimpleBlock(name,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(1.5f,6).lightLevel(state -> light));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(block);
        return block;
    }
    public static void init() { DarkContent.FEATURES.register("surface_crystal_rocks",() -> SurfaceCrystalRocksFeature.CODEC); }
    private LightContent() {}
}
