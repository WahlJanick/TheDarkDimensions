package at.janic.thedarkdimensions.worldgen;
import at.janic.thedarkdimensions.DarkContent;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Seeded asymmetric crowns with variable lobes, branching and irregular edges. */
public final class BurnedTreeFeature implements Feature {
    public static final MapCodec<BurnedTreeFeature> CODEC=MapCodec.unit(new BurnedTreeFeature());
    public MapCodec<? extends Feature> codec(){return CODEC;}
    public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin){
        if(!level.getBlockState(origin.below()).is(net.minecraft.tags.BlockTags.DIRT))return false;
        int height=5+random.nextInt(4);
        Map<BlockPos,BlockState> logs=new LinkedHashMap<>();
        for(int y=0;y<height;y++)logs.put(origin.above(y),DarkContent.BURNED_LOG.get().defaultBlockState());
        Direction arm=Direction.from2DDataValue(random.nextInt(4));int length=1+random.nextInt(2);
        for(int n=1;n<=length;n++)logs.put(origin.above(height-2).relative(arm,n),DarkContent.BURNED_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS,arm.getAxis()));
        Set<BlockPos> leaves=new LinkedHashSet<>();
        crown(leaves,random,origin.offset(random.nextInt(3)-1,height-1,random.nextInt(3)-1),2+random.nextInt(2),1+random.nextInt(2),2+random.nextInt(2));
        crown(leaves,random,origin.above(height-2).relative(arm,length),2,1,2);
        leaves.removeAll(logs.keySet());
        // Validate the complete shape before changing terrain; existing trees are preserved.
        for(BlockPos p:logs.keySet())if(!level.getBlockState(p).canBeReplaced())return false;
        for(BlockPos p:leaves)if(!level.getBlockState(p).canBeReplaced())return false;
        logs.forEach((p,s) -> level.setBlock(p,s,2));
        for(BlockPos p:leaves){
            int distance=logs.keySet().stream().mapToInt(p::distManhattan).min().orElse(7);
            if(distance<=6)level.setBlock(p,Blocks.ACACIA_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE,distance),2);
        }
        return true;
    }
    private static void crown(Set<BlockPos> leaves,RandomSource random,BlockPos centre,int rx,int ry,int rz){
        for(int x=-rx;x<=rx;x++)for(int y=-ry;y<=ry;y++)for(int z=-rz;z<=rz;z++){
            double edge=(double)(x*x)/(rx*rx)+(double)(y*y)/(ry*ry)+(double)(z*z)/(rz*rz);
            if(edge<=1.25&&(edge<0.65||random.nextFloat()>0.24f))leaves.add(centre.offset(x,y,z));
        }
    }
}
