package at.janic.thedarkdimensions;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
/** Light is stored in block state so server and client light engines agree. */
public final class AshfallCrystalBlock extends AmethystClusterBlock {
    public static final BooleanProperty ASHFALL=BooleanProperty.create("ashfall");
    public AshfallCrystalBlock(float height,float width,Properties p){super(height,width,p);registerDefaultState(defaultBlockState().setValue(ASHFALL,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(ASHFALL);}
    public static int light(BlockState s,int base){return s.getValue(ASHFALL)?Math.min(15,base+4):base;}
}
