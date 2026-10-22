package at.janic.thedarkdimensions.worldgen;
import at.janic.thedarkdimensions.DarkContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
public final class EchoRuinFeature implements Feature {
    public static final MapCodec<EchoRuinFeature> CODEC = MapCodec.unit(new EchoRuinFeature());
    public MapCodec<? extends Feature> codec() { return CODEC; }
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (origin.getY()<generator.getSeaLevel()+1 || !level.getBlockState(origin.below()).isSolid()) return false;
        for(BlockPos p:BlockPos.betweenClosed(origin.offset(-3,0,-3),origin.offset(4,5,3)))
            if(!level.getBlockState(p).canBeReplaced()) return false;
        for(int x=-3;x<=4;x++) for(int z=-3;z<=3;z++) {
            for(int y=-3;y<0;y++) level.setBlock(origin.offset(x,y,z),net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState(),2);
        }
        for(int x=-1;x<=2;x++) for(int y=0;y<=4;y++)
            if(x==-1||x==2||y==0||y==4) level.setBlock(origin.offset(x,y,0),DarkContent.IMPROVED_OBSIDIAN.get().defaultBlockState(),2);
        level.setBlock(origin.offset(-2,0,2),DarkContent.ECHO_RELIC.get().defaultBlockState(),2);
        level.setBlock(origin.offset(3,0,2),net.minecraft.world.level.block.Blocks.MAGMA_BLOCK.defaultBlockState(),2);
        return true;
    }
}
