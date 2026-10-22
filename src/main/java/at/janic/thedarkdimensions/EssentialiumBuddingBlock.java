package at.janic.thedarkdimensions;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.Fluids;

/** Vanilla amethyst growth rules, using only Essentialium stages. */
public final class EssentialiumBuddingBlock extends AmethystBlock {
    public EssentialiumBuddingBlock(BlockBehaviour.Properties p) { super(p); }
    @Override protected void randomTick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
        if(random.nextInt(5)!=0)return;
        Direction direction=Direction.values()[random.nextInt(6)];BlockPos target=pos.relative(direction);
        var current=level.getBlockState(target);Block next=null;
        if(BuddingAmethystBlock.canClusterGrowAtState(current))next=MineralContent.SMALL_ESSENTIALIUM_BUD.get();
        else if(current.hasProperty(AmethystClusterBlock.FACING)&&current.getValue(AmethystClusterBlock.FACING)==direction) {
            if(current.is(MineralContent.SMALL_ESSENTIALIUM_BUD.get()))next=MineralContent.MEDIUM_ESSENTIALIUM_BUD.get();
            else if(current.is(MineralContent.MEDIUM_ESSENTIALIUM_BUD.get()))next=MineralContent.LARGE_ESSENTIALIUM_BUD.get();
            else if(current.is(MineralContent.LARGE_ESSENTIALIUM_BUD.get()))next=MineralContent.ESSENTIALIUM_CLUSTER.get();
        }
        if(next!=null)level.setBlockAndUpdate(target,next.defaultBlockState().setValue(AmethystClusterBlock.FACING,direction).setValue(AmethystClusterBlock.WATERLOGGED,current.getFluidState().is(Fluids.WATER)));
    }
}
