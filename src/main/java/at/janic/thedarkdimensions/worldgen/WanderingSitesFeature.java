package at.janic.thedarkdimensions.worldgen;

import at.janic.thedarkdimensions.*;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Small, bounded discoveries; no terrain flattening, ores replaced or floating bridges. */
public final class WanderingSitesFeature implements Feature {
    public enum Kind implements StringRepresentable {CAMP,CACHE,BRIDGE,CRYSTAL;public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}}
    public static final MapCodec<WanderingSitesFeature> CODEC=StringRepresentable.fromEnum(Kind::values).fieldOf("kind").xmap(WanderingSitesFeature::new,f->f.kind);
    private final Kind kind;public WanderingSitesFeature(Kind kind){this.kind=kind;}
    @Override public MapCodec<? extends Feature> codec(){return CODEC;}
    private static boolean ground(BlockState s){return s.is(Blocks.STONE)||s.is(Blocks.DIRT)||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.GRAVEL)||s.is(RevisionContent.HARD_STONE.get());}
    @Override public boolean place(WorldGenLevel l,ChunkGenerator generator,RandomSource random,BlockPos origin){
        var plan=new LinkedHashMap<BlockPos,BlockState>();BlockPos chest=null;
        if(kind==Kind.CACHE){
            BlockPos floor=null;for(int dy=0;dy<12;dy++){var p=origin.below(dy);if(ground(l.getBlockState(p.below()))&&l.getBlockState(p).isAir()&&l.getBlockState(p.above()).isAir()){floor=p;break;}}if(floor==null)return false;
            // A shallow, open-front chamber built into an existing cave pocket.
            // Side/rear walls and a low roof distinguish it from a loose loot chest.
            for(int x=-2;x<=2;x++)for(int z=0;z<=3;z++){
                if(!ground(l.getBlockState(floor.offset(x,-1,z))))return false;
                plan.put(floor.offset(x,3,z),Blocks.STONE_BRICK_SLAB.defaultBlockState());
                if(Math.abs(x)==2||z==3)for(int y=0;y<3;y++)plan.put(floor.offset(x,y,z),(x+y+z)%3==0?Blocks.CRACKED_STONE_BRICKS.defaultBlockState():Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
            chest=floor.offset(0,0,2);plan.put(chest,Blocks.CHEST.defaultBlockState());plan.put(floor.offset(1,2,2),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        }else if(kind==Kind.BRIDGE){
            Direction d=random.nextBoolean()?Direction.EAST:Direction.SOUTH;BlockPos end=origin.relative(d,9);var endFloor=l.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,end);if(!ground(l.getBlockState(origin.below()))||!ground(l.getBlockState(endFloor.below()))||Math.abs(endFloor.getY()-origin.getY())>1)return false;
            int gaps=0;for(int n=2;n<8;n++)if(!l.getBlockState(origin.relative(d,n).below()).isSolid())gaps++;if(gaps<3)return false;
            for(int n=0;n<=9;n++){if(n==4||n==5)continue;for(int side=-1;side<=1;side++){var p=origin.relative(d,n).relative(d.getClockWise(),side);plan.put(p,DarkContent.BURNED_PLANKS.get().defaultBlockState());if(Math.abs(side)==1&&n%3==0)plan.put(p.above(),Blocks.ACACIA_FENCE.defaultBlockState());}}
        }else{
            if(!ground(l.getBlockState(origin.below())))return false;
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){var floor=l.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,origin.offset(x,0,z));if(Math.abs(floor.getY()-origin.getY())>1||!ground(l.getBlockState(floor.below())))return false;}
            if(kind==Kind.CAMP){
                int side=random.nextBoolean()?1:-1;for(int z=-2;z<=2;z++){plan.put(origin.offset(-2*side,0,z),DarkContent.BURNED_LOG.get().defaultBlockState());if(z!=0)plan.put(origin.offset(-2*side,1,z),Blocks.ACACIA_SLAB.defaultBlockState());}
                plan.put(origin,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));plan.put(origin.offset(2*side,0,2),Blocks.BARREL.defaultBlockState());chest=origin.offset(2*side,0,-2);plan.put(chest,Blocks.CHEST.defaultBlockState());plan.put(origin.offset(-2*side,1,0),Blocks.LANTERN.defaultBlockState());
            }else{
                for(int n=0;n<3;n++){int dx=n==0?0:random.nextBoolean()?2:-2,dz=n==0?0:random.nextBoolean()?2:-2;int height=n==0?4+random.nextInt(2):2+random.nextInt(2);for(int y=0;y<height;y++)plan.put(origin.offset(dx,y,dz),(y==0?Blocks.CALCITE:y==height-1?Blocks.GLOWSTONE:Blocks.AMETHYST_BLOCK).defaultBlockState());}plan.put(origin.offset(1,0,0),Blocks.CALCITE.defaultBlockState());
            }
        }
        for(var p:plan.keySet())if(!l.getBlockState(p).canBeReplaced()||!l.getFluidState(p).isEmpty())return false;
        plan.forEach((p,s)->l.setBlock(p,s,2));
        if(chest!=null&&l.getBlockEntity(chest) instanceof ChestBlockEntity be)be.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("chests/"+(kind==Kind.CAMP?"abandoned_camp":"hidden_cache"))),random.nextLong());
        return true;
    }
}
