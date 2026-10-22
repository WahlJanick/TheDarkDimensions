package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.worldgen.SurfaceCrystalRocksFeature;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class SurfaceLightsTests {
    static void generation(GameTestHelper h) {
        var level=h.getLevel();var origin=h.absolutePos(new BlockPos(6,3,6));
        var shapes=new HashSet<String>();var seen=new HashSet<Block>();
        for(int seed=0;seed<5;seed++) {
            for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++) {
                level.setBlock(origin.offset(x,-1,z),Blocks.DIRT.defaultBlockState(),3);
                for(int y=0;y<6;y++)level.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
            }
            boolean placed=new SurfaceCrystalRocksFeature().place(level,level.getChunkSource().getGenerator(),RandomSource.create(263+seed),origin);
            h.assertTrue(placed,"Outcrop generates on dry burned surface");
            StringBuilder shape=new StringBuilder();int count=0;
            for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++)for(int y=0;y<6;y++) {
                var state=level.getBlockState(origin.offset(x,y,z));
                if(state.isAir())continue;
                count++;seen.add(state.getBlock());shape.append(x).append(',').append(y).append(',').append(z).append(';');
                h.assertTrue(state.getLightEmission(level,origin.offset(x,y,z))>=10,"Every outcrop block emits useful surface light");
            }
            h.assertTrue(count>=7&&count<140,"Natural-sized outcrop");shapes.add(shape.toString());
        }
        h.assertTrue(shapes.size()>1,"Different seeds vary outcrop shape");
        h.assertTrue(seen.containsAll(List.of(Blocks.GLOWSTONE,LightContent.GLOWSTONE_VEIN_STONE.get(),LightContent.GLOWSTONE_CRYSTAL_BLOCK.get(),LightContent.PALE_GLOWSTONE.get())),"Mixes glowstone and all three new luminous stones");
        h.succeed();
    }
    static void terrain(GameTestHelper h) {
        var level=h.getLevel();var origin=h.absolutePos(new BlockPos(6,3,6));
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++) {
            level.setBlock(origin.offset(x,-1,z),Blocks.GRAVEL.defaultBlockState(),3);
            for(int y=0;y<6;y++)level.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
        }
        level.setBlock(origin,Blocks.WATER.defaultBlockState(),3);
        h.assertTrue(!new SurfaceCrystalRocksFeature().place(level,level.getChunkSource().getGenerator(),RandomSource.create(1),origin),"Rejects water-covered surfaces");
        level.setBlock(origin,Blocks.AIR.defaultBlockState(),3);
        level.setBlock(origin.below(),Blocks.AIR.defaultBlockState(),3);
        h.assertTrue(!new SurfaceCrystalRocksFeature().place(level,level.getChunkSource().getGenerator(),RandomSource.create(1),origin),"Rejects unsupported air");
        level.setBlock(origin.below(),Blocks.GRAVEL.defaultBlockState(),3);
        level.setBlock(origin.offset(1,0,0),DarkContent.BURNED_LOG.get().defaultBlockState(),3);
        new SurfaceCrystalRocksFeature().place(level,level.getChunkSource().getGenerator(),RandomSource.create(263),origin);
        h.assertTrue(level.getBlockState(origin.offset(1,0,0)).is(DarkContent.BURNED_LOG.get()),"Leaves existing trees intact");
        h.succeed();
    }
    static void registration(GameTestHelper h) {
        var registries=h.getLevel().registryAccess();
        var feature=registries.lookupOrThrow(Registries.PLACED_FEATURE).getValue(TheDarkDimensions.id("surface_crystal_rocks"));
        var biomes=registries.lookupOrThrow(Registries.BIOME);
        for(String name:List.of("ash_wastes","charred_forest","crystal_hollows","echo_fields"))
            h.assertTrue(biomes.getValue(TheDarkDimensions.id(name)).getGenerationSettings().hasFeature(feature),"Surface biome includes lights: "+name);
        for(String name:List.of("normal_caves","fire_cave","water_cave","glowing_cave"))
            h.assertTrue(!biomes.getValue(TheDarkDimensions.id(name)).getGenerationSettings().hasFeature(feature),"Not injected into underground caves: "+name);
        for(var block:List.of(LightContent.GLOWSTONE_VEIN_STONE.get(),LightContent.GLOWSTONE_CRYSTAL_BLOCK.get(),LightContent.PALE_GLOWSTONE.get())) {
            var drops=Block.getDrops(block.defaultBlockState(),h.getLevel(),h.absolutePos(new BlockPos(4,3,4)),null,null,new ItemStack(Items.IRON_PICKAXE));
            h.assertTrue(drops.size()==1&&drops.getFirst().is(block.asItem()),"New luminous stones can be harvested");
        }
        h.succeed();
    }
}
