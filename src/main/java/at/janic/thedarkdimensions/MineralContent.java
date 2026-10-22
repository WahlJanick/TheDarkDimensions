package at.janic.thedarkdimensions;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;

public final class MineralContent {
    public static final int ESSENTIALIUM_TINT=0xff284cc4;
    public static final DeferredItem<Item> ESSENTIALIUM=TheDarkDimensions.ITEMS.registerSimpleItem("essentialium",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> CHRONUS_ORB=TheDarkDimensions.ITEMS.registerSimpleItem("chronus_orb",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredBlock<AmethystClusterBlock> ESSENTIALIUM_CLUSTER=cluster("essentialium_cluster",7,10,Blocks.AMETHYST_CLUSTER);
    public static final DeferredBlock<AmethystClusterBlock> SMALL_ESSENTIALIUM_BUD=cluster("small_essentialium_bud",3,8,Blocks.SMALL_AMETHYST_BUD);
    public static final DeferredBlock<AmethystClusterBlock> MEDIUM_ESSENTIALIUM_BUD=cluster("medium_essentialium_bud",4,6,Blocks.MEDIUM_AMETHYST_BUD);
    public static final DeferredBlock<AmethystClusterBlock> LARGE_ESSENTIALIUM_BUD=cluster("large_essentialium_bud",5,6,Blocks.LARGE_AMETHYST_BUD);
    public static final DeferredBlock<AmethystBlock> ESSENTIALIUM_BLOCK=TheDarkDimensions.BLOCKS.registerBlock("essentialium_block",AmethystBlock::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK));
    public static final DeferredBlock<EssentialiumBuddingBlock> BUDDING_ESSENTIALIUM=TheDarkDimensions.BLOCKS.registerBlock("budding_essentialium",EssentialiumBuddingBlock::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.BUDDING_AMETHYST).randomTicks());
    public static final DeferredBlock<Block> CHRONUS_ORB_ORE=TheDarkDimensions.BLOCKS.registerSimpleBlock("chronus_orb_ore",() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE));
    public static final DeferredBlock<SpelltableBlock> SPELLTABLE=TheDarkDimensions.BLOCKS.registerBlock("spelltable",SpelltableBlock::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.ENCHANTING_TABLE).noOcclusion());
    static {
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(ESSENTIALIUM_BLOCK);
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(BUDDING_ESSENTIALIUM);
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(CHRONUS_ORB_ORE);
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(SPELLTABLE);
    }
    private static DeferredBlock<AmethystClusterBlock> cluster(String name,float height,float width,Block template) {
        var block=TheDarkDimensions.BLOCKS.<AmethystClusterBlock>registerBlock(name,p -> new AshfallCrystalBlock(height,width,p),() -> BlockBehaviour.Properties.ofFullCopy(template).lightLevel(s -> AshfallCrystalBlock.light(s,template.defaultBlockState().getLightEmission())));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(block);return block;
    }
    public static void init() {}
    private MineralContent() {}
}
