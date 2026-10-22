package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;

public final class AstralCrystalWandItem extends Item {
    public AstralCrystalWandItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos();
        if (context.getLevel().getBlockState(pos).is(Blocks.NETHER_PORTAL)) return upgrade(context.getLevel(), player, context.getHand(), pos);
        // Also allow clicking the obsidian frame directly beside the portal.
        if (context.getLevel().getBlockState(pos).is(Blocks.OBSIDIAN)) {
            for (Direction direction : Direction.values()) {
                BlockPos adjacent = pos.relative(direction);
                if (context.getLevel().getBlockState(adjacent).is(Blocks.NETHER_PORTAL)) return upgrade(context.getLevel(), player, context.getHand(), adjacent);
            }
            for(Direction direction:Direction.values())for(Direction.Axis axis:new Direction.Axis[]{Direction.Axis.X,Direction.Axis.Z}) {
                BlockPos adjacent=pos.relative(direction);
                var positions=PortalLogic.findEmpty(context.getLevel(),adjacent,axis);
                if(!positions.isEmpty())return activate(context.getLevel(),player,context.getHand(),adjacent,positions,axis);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var hit = player.pick(player.blockInteractionRange(), 0, false);
        if (hit instanceof net.minecraft.world.phys.BlockHitResult blockHit && level.getBlockState(blockHit.getBlockPos()).is(Blocks.NETHER_PORTAL)) {
            return upgrade(level, player, hand, blockHit.getBlockPos());
        }
        return InteractionResult.PASS;
    }

    private InteractionResult upgrade(Level level, Player player, InteractionHand hand, BlockPos start) {
        var positions = PortalLogic.find(level, start, Blocks.NETHER_PORTAL);
        if (positions.isEmpty()) return InteractionResult.FAIL;
        return activate(level,player,hand,start,positions,level.getBlockState(start).getValue(NetherPortalBlock.AXIS));
    }
    private InteractionResult activate(Level level,Player player,InteractionHand hand,BlockPos start,java.util.List<BlockPos> positions,Direction.Axis axis) {
        // Check every affected position before modifying anything, including adventure-mode restrictions.
        if (!player.mayBuild() || positions.stream().anyMatch(pos -> !level.mayInteract(player, pos))) return InteractionResult.FAIL;
        if (level instanceof ServerLevel server) {
            var improved = TheDarkDimensions.IMPROVED_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
            // Suppress shape/neighbor updates until the complete portal has been replaced.
            positions.forEach(pos -> level.setBlock(pos, improved, 18));
            positions.forEach(pos -> level.updateNeighborsAt(pos, TheDarkDimensions.IMPROVED_PORTAL.get()));
            if (!player.getAbilities().instabuild) player.getItemInHand(hand).hurtAndBreak(1,player,hand==InteractionHand.MAIN_HAND?net.minecraft.world.entity.EquipmentSlot.MAINHAND:net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            level.playSound(null, start, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1, 0.6f);
            server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5, 60, 0.7, 1, 0.7, 0.03);
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ExplorationContent.award(serverPlayer,"awaken_portal");
                serverPlayer.sendSystemMessage(Component.translatable("message.thedarkdimensions.portal_improved"), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
