package at.janic.thedarkdimensions.worldgen;

import at.janic.thedarkdimensions.DarkContent;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Tall dark-acacia trunks and overlapping irregular crowns form a dense canopy. */
public final class VeilwoodTreeFeature implements Feature {
    public static final MapCodec<VeilwoodTreeFeature> CODEC=MapCodec.unit(new VeilwoodTreeFeature());
    public MapCodec<? extends Feature> codec(){return CODEC;}
    public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin){
        int height=8+random.nextInt(5);
        for(int x=0;x<2;x++)for(int z=0;z<2;z++){
            if(!level.getBlockState(origin.offset(x,-1,z)).is(net.minecraft.tags.BlockTags.DIRT))return false;
            for(int y=0;y<height;y++){var state=level.getBlockState(origin.offset(x,y,z));if(!state.canBeReplaced()&&!(state.getBlock() instanceof LeavesBlock))return false;}
        }
        for(int x=0;x<2;x++)for(int z=0;z<2;z++)for(int y=0;y<height;y++)level.setBlock(origin.offset(x,y,z),DarkContent.BURNED_LOG.get().defaultBlockState(),2);
        for(int dy=-3;dy<=1;dy++)for(int x=-4;x<=5;x++)for(int z=-4;z<=5;z++){
            double edge=(x-.5)*(x-.5)/22+(z-.5)*(z-.5)/22+(dy+1)*(dy+1)/8.0;
            if(edge>1.2||edge>.6&&random.nextFloat()<.16)continue;
            var p=origin.offset(x,height+dy,z);var state=level.getBlockState(p);
            if(state.canBeReplaced()||state.getBlock() instanceof LeavesBlock)level.setBlock(p,Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.DISTANCE,1),2);
        }
        return true;
    }
}
