package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.NetherPortalBlock;

public final class ImprovedEchoShardItem extends Item {
    public ImprovedEchoShardItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().getBlockState(context.getClickedPos()).is(DarkContent.IMPROVED_OBSIDIAN.get())) return InteractionResult.PASS;
        if (!context.getLevel().dimension().equals(DarkContent.BURNED_REALM)) return InteractionResult.FAIL;
        var player = context.getPlayer();
        if (player == null || !player.mayBuild()) return InteractionResult.FAIL;
        for (var axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            for (int horizontal = -3; horizontal <= 0; horizontal++) for (int y = -4; y <= 0; y++) {
                BlockPos corner = relative(context.getClickedPos(), axis, horizontal, y);
                if (!isFrame(context.getLevel(), corner, axis)) continue;
                for (int h = 1; h <= 2; h++) for (int v = 1; v <= 3; v++) {
                    if (!context.getLevel().mayInteract(player, relative(corner, axis, h, v))) return InteractionResult.FAIL;
                }
                if (context.getLevel() instanceof ServerLevel level) {
                    for (int h = 1; h <= 2; h++) for (int v = 1; v <= 3; v++) {
                        level.setBlock(relative(corner, axis, h, v), DarkContent.ECHO_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis), 18);
                    }
                    if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
                    level.playSound(null, corner, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.5f, 0.7f);
                    level.sendParticles(ParticleTypes.END_ROD, corner.getX() + 1.5, corner.getY() + 2, corner.getZ() + 0.5, 50, 1, 1.5, 1, 0.03);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
    public static boolean isFrame(LevelReader level, BlockPos corner, Direction.Axis axis) {
        for (int h = 0; h < 4; h++) for (int v = 0; v < 5; v++) {
            var state = level.getBlockState(relative(corner, axis, h, v));
            if (h == 0 || h == 3 || v == 0 || v == 4) {
                if (!state.is(DarkContent.IMPROVED_OBSIDIAN.get())) return false;
            } else if (!state.isAir()) return false;
        }
        return true;
    }
    public static BlockPos relative(BlockPos corner, Direction.Axis axis, int horizontal, int y) {
        return axis == Direction.Axis.X ? corner.offset(horizontal, y, 0) : corner.offset(0, y, horizontal);
    }
}
