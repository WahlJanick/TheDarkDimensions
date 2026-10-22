package at.janic.thedarkdimensions.worldgen;
import at.janic.thedarkdimensions.DarkContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
public final class CrystalGrowthFeature implements Feature {
    public static final MapCodec<CrystalGrowthFeature> CODEC = MapCodec.unit(new CrystalGrowthFeature());
    public MapCodec<? extends Feature> codec() { return CODEC; }
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        boolean placed=false;
        for(int i=0;i<24;i++) {
            BlockPos p=origin.offset(random.nextInt(9)-4,random.nextInt(9)-4,random.nextInt(9)-4);
            if(level.isEmptyBlock(p)&&level.getBlockState(p.below()).isSolid()) {
                int height=1+random.nextInt(3);
                for(int y=0;y<height&&level.isEmptyBlock(p.above(y));y++) level.setBlock(p.above(y),DarkContent.ECHO_CRYSTAL.get().defaultBlockState(),2);
                placed=true;
            }
        }
        return placed;
    }
}
