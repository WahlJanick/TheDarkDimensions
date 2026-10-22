package at.janic.thedarkdimensions.worldgen;
import at.janic.thedarkdimensions.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import java.util.ArrayList;

/** Decorates existing vanilla noise/carver caves without carving repeated rooms. */
public final class ElementalCaveFeature implements Feature {
    public enum Kind implements StringRepresentable {
        FIRE, WATER, GLOWING;
        public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final MapCodec<ElementalCaveFeature> CODEC=StringRepresentable.fromEnum(Kind::values).fieldOf("kind").xmap(ElementalCaveFeature::new,f -> f.kind);
    private final Kind kind;
    public ElementalCaveFeature(Kind kind) { this.kind=kind; }
    public MapCodec<? extends Feature> codec() { return CODEC; }
    private static boolean terrain(net.minecraft.world.level.block.state.BlockState s) {
        return s.is(Blocks.STONE)||s.is(Blocks.DEEPSLATE)||s.is(RevisionContent.HARD_STONE.get());
    }
    @Override public boolean place(WorldGenLevel level,ChunkGenerator generator,RandomSource random,BlockPos origin) {
        Block lining=switch(kind) { case FIRE -> CaveContent.FIRE_CAVE_STONE.get();case WATER -> CaveContent.WATER_CAVE_STONE.get();case GLOWING -> CaveContent.GLOWING_CAVE_STONE.get(); };
        Block crystal=switch(kind) { case FIRE -> CaveContent.FIRE_CRYSTAL.get();case WATER -> CaveContent.WATER_CRYSTAL.get();case GLOWING -> CaveContent.GLOWING_CRYSTAL.get(); };
        var surfaces=new ArrayList<BlockPos>();var floors=new ArrayList<BlockPos>();
        for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)for(int y=-12;y<=12;y++) {
            BlockPos p=origin.offset(x,y,z);
            if(p.getY()<level.getMinY()+5||p.getY()>40||!(terrain(level.getBlockState(p))||ElementalStoneContent.themedOre(kind.getSerializedName(),level.getBlockState(p))!=null))continue;
            for(Direction d:Direction.values())if(level.getBlockState(p.relative(d)).isAir()) { surfaces.add(p);break; }
            if(terrain(level.getBlockState(p))&&level.getBlockState(p.above()).isAir()&&level.getBlockState(p.above(2)).isAir())floors.add(p.above());
        }
        if(surfaces.isEmpty())return false;
        for(BlockPos p:surfaces){
            Block ore=ElementalStoneContent.themedOre(kind.getSerializedName(),level.getBlockState(p));
            Block replacement=ore!=null?ore:(kind==Kind.GLOWING&&random.nextInt(4)==0?Blocks.GLOWSTONE:lining);
            level.setBlock(p,replacement.defaultBlockState(),2);
        }
        for(BlockPos p:floors) {
            if(random.nextInt(18)!=0)continue;
            Block material=switch(kind) {case FIRE -> Blocks.MAGMA_BLOCK;case WATER -> Blocks.WATER;case GLOWING -> CaveContent.GLOWING_HONEY_BLOCK.get();};
            level.setBlock(p.below(),material.defaultBlockState(),2);
        }
        for(BlockPos p:surfaces) {
            if(random.nextInt(9)!=0)continue;
            Direction facing=Direction.values()[random.nextInt(6)];BlockPos target=p.relative(facing);
            if(level.getBlockState(target).isAir()) {
                var state=crystal.defaultBlockState().setValue(AmethystClusterBlock.FACING,facing);
                if(state.canSurvive(level,target))level.setBlock(target,state,2);
            }
        }
        floors.removeIf(p -> !level.getBlockState(p).isAir()||!level.getBlockState(p.above()).isAir()||!level.getBlockState(p.below()).isSolid());
        if(!floors.isEmpty()) {
            int count=kind==Kind.GLOWING?2:1;
            for(int i=0;i<count;i++) {
                BlockPos p=floors.get(random.nextInt(floors.size()));
                if(!level.getBlockState(p).isAir()||!level.getBlockState(p.above()).isAir()||!level.getBlockState(p.below()).isSolid())continue;
                EntityType<? extends Mob> type=switch(kind) {case FIRE -> CaveContent.FIRE_MAGE.get();case WATER -> TheDarkDimensions.MUMMY.get();case GLOWING -> CaveContent.GLOWING_BEE.get();};
                Mob mob=type.create(level.getLevel(),EntitySpawnReason.STRUCTURE);
                if(mob!=null) {
                    mob.setPos(p.getX()+0.5,p.getY(),p.getZ()+0.5);
                    mob.finalizeSpawn(level,level.getCurrentDifficultyAt(p),EntitySpawnReason.STRUCTURE,null);
                    mob.setPersistenceRequired();level.addFreshEntity(mob);
                }
            }
        }
        return true;
    }
}
