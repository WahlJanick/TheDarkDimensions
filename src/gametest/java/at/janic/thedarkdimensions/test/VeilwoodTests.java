package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.worldgen.VeilwoodBiomeSource;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.*;

final class VeilwoodTests {
    static void source(GameTestHelper h){
        var r=h.getLevel().registryAccess();var forest=r.getOrThrow(VeilwoodContent.BIOME);
        var plain=r.getOrThrow(Biomes.PLAINS);var source=new VeilwoodBiomeSource(new FixedBiomeSource(plain),forest);
        var random=RandomState.create(r.lookupOrThrow(Registries.NOISE),263L,r.getOrThrow(NoiseGeneratorSettings.OVERWORLD).value());
        var sampler=random.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
        var resolver=source.createResolver(sampler);int found=0;
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){
            var island=VeilwoodBiomeSource.island(x,z,sampler);
            h.assertTrue(!island.contains(x*512,z*512),"Forest islands never connect across region boundaries");
            if(!island.active())continue;found++;
            h.assertTrue(resolver.getNoiseBiome(island.x()>>2,75,island.z()>>2).is(VeilwoodContent.BIOME),"Seeded forest surface resolves to Veilwood");
            h.assertTrue(resolver.getNoiseBiome(x*128,75,z*128).is(Biomes.PLAINS),"Outside forest retains delegate biome");
        }
        h.assertTrue(found>10&&source.possibleBiomes().contains(forest),"Forest participates in biome searches");
        var ops=r.createSerializationContext(JsonOps.INSTANCE);
        var json=net.minecraft.world.level.biome.BiomeSource.CODEC.encodeStart(ops,source).getOrThrow();
        h.assertTrue(net.minecraft.world.level.biome.BiomeSource.CODEC.parse(ops,json).getOrThrow() instanceof VeilwoodBiomeSource,"Native biome source codec round trips");
        h.assertTrue(VeilwoodBiomeSource.regionKey(-1,-1).equals("-1,-1"),"Negative coordinates have distinct saved regions");h.succeed();
    }
    static void singleton(GameTestHelper h){
        var level=h.getLevel();var base=h.absolutePos(new BlockPos(5,3,5));
        // Biome lookup blends neighboring quart samples. Stay inside the chunk whose
        // biome palette this fixture replaces, regardless of the random test origin.
        var pos=new BlockPos((base.getX()&~15)+8,base.getY(),(base.getZ()&~15)+8);var chunk=level.getChunkAt(pos);var original=level.getBiome(pos);
        var forest=level.registryAccess().getOrThrow(VeilwoodContent.BIOME);var data=VeilwoodContent.data(level);var key=VeilwoodBiomeSource.regionKey(pos.getX(),pos.getZ());data.regions.remove(key);
        chunk.fillBiomesFromNoise((x,y,z)->forest);
        try{
            level.setBlock(pos.below(),Blocks.DIRT.defaultBlockState(),18);
            for(int y=0;y<4;y++)level.setBlock(pos.above(y),Blocks.AIR.defaultBlockState(),18);
            level.setBlock(pos,Blocks.STONE.defaultBlockState(),18);
            h.assertTrue(!VeilwoodContent.spawnOnce(level,pos)&&!data.regions.containsKey(key),"Obstructed spawn does not consume region");
            level.setBlock(pos,Blocks.AIR.defaultBlockState(),18);
            h.assertTrue(VeilwoodContent.spawnOnce(level,pos),"First valid forest spawn succeeds");
            h.assertTrue(!VeilwoodContent.spawnOnce(level,pos),"Second spawn in same region is rejected");
            var mobs=level.getEntitiesOfClass(HollowWatcher.class,new net.minecraft.world.phys.AABB(pos).inflate(5));
            h.assertTrue(mobs.size()==1&&mobs.getFirst().isPersistenceRequired(),"Exactly one persistent Watcher");mobs.getFirst().discard();
            h.assertTrue(!VeilwoodContent.spawnOnce(level,pos),"Removed Watcher cannot regenerate");
            var saved=VeilwoodContent.Data.CODEC.encodeStart(JsonOps.INSTANCE,data).getOrThrow();
            h.assertTrue(VeilwoodContent.Data.CODEC.parse(JsonOps.INSTANCE,saved).getOrThrow().regions.containsKey(key),"Spawn marker survives serialized reload");
        }finally{chunk.fillBiomesFromNoise((x,y,z)->original);data.regions.remove(key);data.setDirty();}
        h.succeed();
    }
    static void atmosphere(GameTestHelper h){
        var biome=h.getLevel().registryAccess().getOrThrow(VeilwoodContent.BIOME).value();
        var a=biome.getAttributes();
        h.assertTrue(a.applyModifier(net.minecraft.world.attribute.EnvironmentAttributes.FOG_END_DISTANCE,1024f)<=24,"Dense forest fog limits visibility");
        h.assertTrue(a.applyModifier(net.minecraft.world.attribute.EnvironmentAttributes.SKY_LIGHT_FACTOR,1f)<=.06f,"Forest darkens native sky lighting");h.succeed();
    }
}
