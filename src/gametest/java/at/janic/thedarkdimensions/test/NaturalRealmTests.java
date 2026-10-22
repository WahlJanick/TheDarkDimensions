package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

final class NaturalRealmTests {
    static void aliases(GameTestHelper h){
        var replacements=Map.of("burned_stone",Blocks.STONE,"burned_dirt",Blocks.DIRT,"burned_deepslate",RevisionContent.HARD_STONE.get());
        for(var e:replacements.entrySet()){
            h.assertTrue(BuiltInRegistries.BLOCK.getValue(TheDarkDimensions.id(e.getKey()))==e.getValue(),"Old saved block ID resolves to replacement: "+e.getKey());
            h.assertTrue(BuiltInRegistries.ITEM.getValue(TheDarkDimensions.id(e.getKey()))==e.getValue().asItem(),"Old inventory stack ID resolves to replacement: "+e.getKey());
            h.assertTrue(!BuiltInRegistries.BLOCK.getKey(e.getValue()).equals(TheDarkDimensions.id(e.getKey())),"Old name no longer registers its own block");
            var state=com.mojang.serialization.JsonOps.INSTANCE;
            var json=new com.google.gson.JsonObject();json.addProperty("id",TheDarkDimensions.id(e.getKey()).toString());
            var decoded=net.minecraft.world.level.block.state.BlockState.CODEC.parse(state,json).getOrThrow();
            h.assertTrue(decoded.is(e.getValue()),"Serialized chunk block state migrates through registry aliases");
        }
        h.succeed();
    }
    static void trees(GameTestHelper h){
        var level=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);
        var feature=level.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("burned_tree"))).value();
        var origin=new BlockPos(1024,240,1024);Set<Set<String>> silhouettes=new HashSet<>();Set<String> first=null;
        for(int seed=0;seed<=16;seed++){
            for(var pos:BlockPos.betweenClosed(origin.offset(-5,0,-5),origin.offset(5,13,5)))level.setBlock(pos,Blocks.AIR.defaultBlockState(),18);
            level.setBlock(origin.below(),Blocks.DIRT.defaultBlockState(),18);
            int actualSeed=seed==16?0:seed;
            h.assertTrue(feature.place(level,level.getChunkSource().getGenerator(),RandomSource.create(actualSeed),origin),"Tree generates on Dirt");
            Set<String> leaves=new HashSet<>(),projection=new HashSet<>();
            for(var pos:BlockPos.betweenClosed(origin.offset(-5,0,-5),origin.offset(5,13,5)))if(level.getBlockState(pos).is(Blocks.ACACIA_LEAVES)){
                leaves.add((pos.getX()-origin.getX())+","+(pos.getY()-origin.getY())+","+(pos.getZ()-origin.getZ()));
                projection.add((pos.getX()-origin.getX())+","+(pos.getZ()-origin.getZ()));
                h.assertTrue(level.getBlockState(pos).getValue(net.minecraft.world.level.block.LeavesBlock.DISTANCE)<7,"Generated leaves have a supporting log");
            }
            h.assertTrue(leaves.size()>20,"Tree retains a substantial leafy crown");
            if(seed==0)first=leaves;
            if(seed==16)h.assertTrue(first.equals(leaves),"Identical seed recreates identical crown");else silhouettes.add(projection);
        }
        h.assertTrue(silhouettes.size()>=8,"Seeds vary the actual leaf outline, not only trunk height: "+silhouettes.size());h.succeed();
    }
}
