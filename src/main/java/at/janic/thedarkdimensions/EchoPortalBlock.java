package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;

public final class EchoPortalBlock extends ImprovedPortalBlockBase {
    public EchoPortalBlock(Properties properties) { super(properties); }
    @Override public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos entry) {
        return PortalTravel.destination(level, entity, entry, true);
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (direction.getAxis().isHorizontal() && direction.getAxis() != state.getValue(AXIS)) return state;
        if (neighbor.is(this)) return state;
        return PortalLogic.find(level, pos, this, DarkContent.IMPROVED_OBSIDIAN.get()).isEmpty() ? Blocks.AIR.defaultBlockState() : state;
    }
}
