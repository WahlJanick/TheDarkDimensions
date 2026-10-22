package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;

public final class ImprovedPortalBlock extends NetherPortalBlock {
    public ImprovedPortalBlock(BlockBehaviour.Properties properties) { super(properties); }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effects, boolean precise) {
        if (entity.canUsePortal(false)) entity.setAsInsidePortal(this, pos);
    }
    @Override public TeleportTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos entry) {
        return PortalTravel.destination(level, entity, entry, false);
    }
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (direction.getAxis().isHorizontal() && direction.getAxis() != state.getValue(AXIS)) return state;
        if (neighbor.is(this)) return state;
        return PortalLogic.find(level, pos, this).isEmpty() ? Blocks.AIR.defaultBlockState() : state;
    }
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                pos.getZ() + random.nextDouble(), 0, 0.015, 0);
    }
}
