package at.janic.thedarkdimensions.worldgen;

import at.janic.thedarkdimensions.DarkContent;
import at.janic.thedarkdimensions.LightContent;
import com.mojang.serialization.MapCodec;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Irregular outcrops fitted to dry terrain, with several tapering crystal spires. */
public final class SurfaceCrystalRocksFeature implements Feature {
    public static final MapCodec<SurfaceCrystalRocksFeature> CODEC=MapCodec.unit(new SurfaceCrystalRocksFeature());
    @Override public MapCodec<? extends Feature> codec() { return CODEC; }
    private static boolean ground(BlockState state) {
        return state.is(DarkContent.BURNED_GRAVEL.get())
            ||state.is(Blocks.GRASS_BLOCK)||state.is(Blocks.DIRT)||state.is(Blocks.GRAVEL)||state.is(Blocks.STONE)||state.is(at.janic.thedarkdimensions.RevisionContent.HARD_STONE.get());
    }
    private static BlockState stone(RandomSource random,boolean tip) {
        if(tip)return (random.nextBoolean()?LightContent.GLOWSTONE_CRYSTAL_BLOCK.get():Blocks.GLOWSTONE).defaultBlockState();
        return switch(random.nextInt(10)) {
            case 0,1 -> Blocks.GLOWSTONE.defaultBlockState();
            case 2,3 -> LightContent.PALE_GLOWSTONE.get().defaultBlockState();
            case 4 -> LightContent.GLOWSTONE_CRYSTAL_BLOCK.get().defaultBlockState();
            default -> LightContent.GLOWSTONE_VEIN_STONE.get().defaultBlockState();
        };
    }
    @Override public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin) {
        if(!ground(level.getBlockState(origin.below()))||!level.getFluidState(origin).isEmpty())return false;
        int radius=2+random.nextInt(2);
        var planned=new LinkedHashMap<BlockPos,BlockState>();
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++) {
            if(x*x+z*z>radius*radius||random.nextInt(5)==0)continue;
            BlockPos floor=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,origin.offset(x,0,z));
            if(Math.abs(floor.getY()-origin.getY())>2||!ground(level.getBlockState(floor.below())))continue;
            if(!level.getFluidState(floor).isEmpty()||!level.getBlockState(floor).canBeReplaced())continue;
            planned.put(floor,stone(random,false));
            if(Math.abs(x)+Math.abs(z)<=radius-1) {
                int height=2+random.nextInt(4);
                for(int y=1;y<height;y++) {
                    BlockPos pos=floor.above(y);
                    if(!level.getFluidState(pos).isEmpty()||!level.getBlockState(pos).canBeReplaced())break;
                    planned.put(pos,stone(random,y==height-1));
                }
            }
        }
        // Never create a tiny floating fragment when a slope or obstacle blocks the outcrop.
        if(planned.size()<7)return false;
        planned.forEach((pos,state) -> level.setBlock(pos,state,2));
        return true;
    }
}
