package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import java.util.*;

final class ElementalStoneTests {
    static void walls(GameTestHelper h){
        var level=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);int index=0;
        for(var theme:ElementalStoneContent.ORES.entrySet()){
            var origin=new BlockPos(1536+index++*32,-24,1536);
            for(var p:BlockPos.betweenClosed(origin.offset(-8,-13,-8),origin.offset(8,13,8))){
                boolean air=Math.abs(p.getX()-origin.getX())<=3&&Math.abs(p.getY()-origin.getY())<=2&&Math.abs(p.getZ()-origin.getZ())<=3;
                level.setBlock(p,(air?Blocks.AIR:RevisionContent.HARD_STONE.get()).defaultBlockState(),18);
            }
            Map<String,BlockPos> ores=new LinkedHashMap<>();int n=0;
            for(String mineral:theme.getValue().keySet()){
                BlockPos p=n<7?origin.offset(4,0,n-3):origin.offset(4,1,0);n++;
                ores.put(mineral,p);level.setBlock(p,RevisionContent.ORES.get(mineral).get().defaultBlockState(),18);
            }
            var floor=origin.offset(0,-3,0);level.setBlock(floor,Blocks.DIAMOND_ORE.defaultBlockState(),18);
            var feature=level.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id(theme.getKey()+"_cave"))).value();
            h.assertTrue(feature.place(level,level.getChunkSource().getGenerator(),RandomSource.create(263),origin),"Elemental cave decorates existing walls");
            for(var ore:ores.entrySet())h.assertTrue(level.getBlockState(ore.getValue()).is(theme.getValue().get(ore.getKey()).get()),"Exposed ore is retained with matching stone background: "+theme.getKey()+" "+ore.getKey());
            h.assertTrue(level.getBlockState(floor).is(theme.getValue().get("diamond").get()),"Floor ore survives glowing, water or magma accents");
            h.assertTrue(level.getBlockState(origin).isAir(),"Decoration preserves cave air");
        }
        h.succeed();
    }
    static void loot(GameTestHelper h){
        var level=h.getLevel();var tool=new ItemStack(Items.DIAMOND_PICKAXE);var silk=tool.copy();silk.enchant(level.registryAccess().getOrThrow(Enchantments.SILK_TOUCH),1);
        var fortune=tool.copy();fortune.enchant(level.registryAccess().getOrThrow(Enchantments.FORTUNE),3);
        Map<String,Item> drops=Map.of("coal",Items.COAL,"copper",Items.RAW_COPPER,"iron",Items.RAW_IRON,"gold",Items.RAW_GOLD,"redstone",Items.REDSTONE,"lapis",Items.LAPIS_LAZULI,"diamond",Items.DIAMOND,"emerald",Items.EMERALD);
        for(var theme:ElementalStoneContent.ORES.values())for(var entry:theme.entrySet()){
            var state=entry.getValue().get().defaultBlockState();
            var plain=Block.getDrops(state,level,BlockPos.ZERO,null,null,tool);
            h.assertTrue(!plain.isEmpty()&&plain.stream().allMatch(s -> s.is(drops.get(entry.getKey()))),"Elemental ore drops the unchanged mineral");
            var preserved=Block.getDrops(state,level,BlockPos.ZERO,null,null,silk);
            h.assertTrue(preserved.size()==1&&preserved.getFirst().is(entry.getValue().get().asItem()),"Silk Touch preserves exact elemental ore");
            int base=0,boost=0;
            for(int i=0;i<50;i++){
                base+=Block.getDrops(state,level,BlockPos.ZERO,null,null,tool).stream().mapToInt(ItemStack::getCount).sum();
                boost+=Block.getDrops(state,level,BlockPos.ZERO,null,null,fortune).stream().mapToInt(ItemStack::getCount).sum();
            }
            h.assertTrue(boost>base,"Fortune increases drops for each elemental ore");
            h.assertTrue(!state.is(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL)||!new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(state),"High-tier ore needs correct mining tool");
        }
        h.succeed();
    }
}
