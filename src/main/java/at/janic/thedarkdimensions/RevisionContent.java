package at.janic.thedarkdimensions;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class RevisionContent {
    public static final DeferredBlock<Block> HARD_STONE=TheDarkDimensions.BLOCKS.registerSimpleBlock("hard_stone",() -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(4.5f,9));
    public static final DeferredItem<BlockItem> HARD_STONE_ITEM=TheDarkDimensions.ITEMS.registerSimpleBlockItem(HARD_STONE);
    public static final Map<String,DeferredBlock<DropExperienceBlock>> ORES=new LinkedHashMap<>();
    public static final DeferredItem<AstralCrystalWandItem> ASTRAL_CRYSTAL_WAND=TheDarkDimensions.ITEMS.registerItem("astral_crystal_wand",AstralCrystalWandItem::new,p -> p.durability(64).rarity(Rarity.RARE));
    static {
        for(String kind:new String[]{"coal","copper","iron","gold","redstone","lapis","diamond","emerald"}) {
            int min=switch(kind){case "coal" -> 0;case "redstone","lapis" -> 1;case "diamond","emerald" -> 3;default -> 0;};
            int max=switch(kind){case "coal" -> 2;case "redstone" -> 5;case "lapis","diamond","emerald" -> 7;default -> 0;};
            var ore=TheDarkDimensions.BLOCKS.registerBlock("hard_stone_"+kind+"_ore",p -> new DropExperienceBlock(UniformInt.of(min,max),p),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(5.5f,9).requiresCorrectToolForDrops());
            TheDarkDimensions.ITEMS.registerSimpleBlockItem(ore);ORES.put(kind,ore);
        }
    }
    public static void init() {}
}
