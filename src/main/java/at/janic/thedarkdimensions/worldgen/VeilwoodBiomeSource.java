package at.janic.thedarkdimensions.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.*;
import net.minecraft.world.level.biome.*;

/** Separate, seeded forest islands: a stable region identity without scanning or generating chunks. */
public final class VeilwoodBiomeSource extends BiomeSource {
    public static final int CELL_SIZE=512;
    public static final MapCodec<VeilwoodBiomeSource> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        BiomeSource.CODEC.fieldOf("delegate").forGetter(s->s.delegate),Biome.CODEC.fieldOf("forest").forGetter(s->s.forest)
    ).apply(i,VeilwoodBiomeSource::new));
    private final BiomeSource delegate;
    private final Holder<Biome> forest;
    public VeilwoodBiomeSource(BiomeSource delegate,Holder<Biome> forest){this.delegate=delegate;this.forest=forest;}
    @Override protected MapCodec<? extends BiomeSource> codec(){return CODEC;}
    @Override protected Stream<Holder<Biome>> collectPossibleBiomes(){return Stream.concat(delegate.possibleBiomes().stream(),Stream.of(forest));}
    public static String regionKey(int x,int z){return Math.floorDiv(x,CELL_SIZE)+","+Math.floorDiv(z,CELL_SIZE);}
    public record Island(int x,int z,int rx,int rz,boolean active){
        public boolean contains(int bx,int bz){double dx=(double)(bx-x)/rx,dz=(double)(bz-z)/rz;double angle=Math.atan2(dz,dx);return active&&dx*dx+dz*dz<1+.10*Math.sin(angle*3)+.07*Math.cos(angle*5);}
    }
    private static long mix(long v){v=(v^(v>>>30))*0xbf58476d1ce4e5b9L;v=(v^(v>>>27))*0x94d049bb133111ebL;return v^(v>>>31);}
    public static Island island(int cellX,int cellZ,Climate.Sampler sampler){
        int cx=cellX*CELL_SIZE+256,cz=cellZ*CELL_SIZE+256;var climate=sampler.sample(QuartPos.fromBlock(cx),16,QuartPos.fromBlock(cz));
        long hash=mix(climate.continentalness()^Long.rotateLeft(climate.humidity(),19)^Long.rotateLeft(climate.weirdness(),37)^ChunkPosKey(cellX,cellZ));
        return new Island(cx+(int)Math.floorMod(hash,81)-40,cz+(int)Math.floorMod(hash>>>8,81)-40,110+(int)Math.floorMod(hash>>>16,31),110+(int)Math.floorMod(hash>>>24,31),Math.floorMod(hash,3)==0);
    }
    private static long ChunkPosKey(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    @Override public BiomeResolver createResolver(Climate.Sampler sampler){
        var original=delegate.createResolver(sampler);Map<Long,Island> islands=new HashMap<>();
        return (qx,qy,qz)->{
            int x=QuartPos.toBlock(qx),z=QuartPos.toBlock(qz),cx=Math.floorDiv(x,CELL_SIZE),cz=Math.floorDiv(z,CELL_SIZE);
            Island island=islands.computeIfAbsent(ChunkPosKey(cx,cz),k->island(cx,cz,sampler));
            // Underground climate branches retain their original elemental/normal cave biomes.
            if(island.contains(x,z)&&sampler.sample(qx,qy,qz).depth()<Climate.quantizeCoord(.35f))return forest;
            return original.getNoiseBiome(qx,qy,qz);
        };
    }
}
