package at.janic.thedarkdimensions.resonance;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pools.*;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

/** Find a real noise-cave floor before selecting one of the rotated ruin layouts. */
public final class CaveTrialStructure extends Structure {
    public static final MapCodec<CaveTrialStructure> CODEC=RecordCodecBuilder.mapCodec(i->i.group(settingsCodec(i),StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s->s.pool),StringRepresentable.fromEnum(TrialKind::values).fieldOf("kind").forGetter(s->s.kind)).apply(i,CaveTrialStructure::new));
    private final Holder<StructureTemplatePool> pool;public final TrialKind kind;
    public CaveTrialStructure(StructureSettings settings,Holder<StructureTemplatePool> pool,TrialKind kind){super(settings);this.pool=pool;this.kind=kind;}
    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c){
        int start=-46+c.random().nextInt(36);
        for(int n=0;n<9;n++){
            int x=c.chunkPos().getMinBlockX()+4+(n%3)*4,z=c.chunkPos().getMinBlockZ()+4+(n/3)*4;
            var column=c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState());
            for(int offset=0;offset<76;offset++){
                int y=-48+Math.floorMod(start+48+offset,76);
                if(!column.getBlock(y-1).isSolid()||!column.getBlock(y).isAir()||!column.getBlock(y+1).isAir()||!column.getBlock(y+2).isAir())continue;
                if(!c.validBiome().test(c.biomeSource().createResolver(c.climateSampler()).getNoiseBiome(x>>2,y>>2,z>>2)))continue;
                return JigsawPlacement.addPieces(c,pool,Optional.empty(),1,new BlockPos(x,y-1,z),false,Optional.empty(),new JigsawStructure.MaxDistance(40),PoolAliasLookup.EMPTY,DimensionPadding.ZERO,LiquidSettings.IGNORE_WATERLOGGING);
            }
        }
        return Optional.empty();
    }
    @Override public StructureType<?> type(){return ResonanceContent.TRIAL_STRUCTURE.get();}
}
