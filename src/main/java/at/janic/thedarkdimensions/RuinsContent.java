package at.janic.thedarkdimensions;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class RuinsContent {
    public static final DeferredItem<Item> POCKET_UPGRADE=TheDarkDimensions.ITEMS.registerSimpleItem("pocket_key_upgrade_template",p -> p.rarity(Rarity.RARE).fireResistant());
    public static final DeferredItem<PocketKeyItem> IMPROVED_KEY=TheDarkDimensions.ITEMS.registerItem("improved_pocket_dimension_key",p -> new PocketKeyItem(p,true),p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<ForeignKeyItem> FOREIGN_KEY=TheDarkDimensions.ITEMS.registerItem("foreign_key",ForeignKeyItem::new,p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    public static final DeferredBlock<ReturnAnchorBlock> RETURN_ANCHOR=TheDarkDimensions.BLOCKS.registerBlock("return_anchor",ReturnAnchorBlock::new,() -> BlockBehaviour.Properties.ofFullCopy(Blocks.CRYING_OBSIDIAN).strength(3,1200).lightLevel(s -> 10).noOcclusion());
    public static final DeferredItem<BlockItem> RETURN_ANCHOR_ITEM=TheDarkDimensions.ITEMS.registerSimpleBlockItem(RETURN_ANCHOR);
    public static final DeferredItem<ClueItem> WEATHERED_PAPYRUS=TheDarkDimensions.ITEMS.registerItem("weathered_papyrus",ClueItem::new,p -> p.rarity(Rarity.UNCOMMON));
    public static void init(){}
    private RuinsContent(){}
    public static final class ClueItem extends Item {
        public ClueItem(Properties p){super(p);}
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<net.minecraft.network.chat.Component> builder,TooltipFlag flag){
            super.appendHoverText(stack,context,display,builder,flag);
            for(int i=1;i<=4;i++)builder.accept(net.minecraft.network.chat.Component.translatable("hint.thedarkdimensions.papyrus_"+i).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
