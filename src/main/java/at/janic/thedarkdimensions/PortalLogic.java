package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;

public final class PortalLogic {
    private PortalLogic() {}
    /** Finds a complete rectangular 2..21 by 3..21 portal with its obsidian frame. Corners are optional. */
    public static List<BlockPos> find(LevelReader level, BlockPos start, Block portal) {
        return find(level, start, portal, Blocks.OBSIDIAN);
    }
    public static List<BlockPos> find(LevelReader level, BlockPos start, Block portal, Block frame) {
        var state = level.getBlockState(start);
        if (!state.is(portal)) return List.of();
        var axis = state.getValue(NetherPortalBlock.AXIS);
        return find(level,start,portal,frame,axis);
    }
    /** Validate an unlit obsidian frame before the wand writes any portal blocks. */
    public static List<BlockPos> findEmpty(LevelReader level,BlockPos start,Direction.Axis axis) {
        return find(level,start,null,Blocks.OBSIDIAN,axis);
    }
    private static boolean interior(net.minecraft.world.level.block.state.BlockState state,Block portal,Direction.Axis axis) {
        return portal==null ? state.isAir()||state.is(Blocks.FIRE) : state.is(portal)&&state.getValue(NetherPortalBlock.AXIS)==axis;
    }
    private static List<BlockPos> find(LevelReader level,BlockPos start,Block portal,Block frame,Direction.Axis axis) {
        Direction horizontal = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        Set<BlockPos> found = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(start.immutable());
        while (!pending.isEmpty()) {
            BlockPos pos = pending.removeFirst();
            if (found.contains(pos)) continue;
            var next = level.getBlockState(pos);
            if (!interior(next,portal,axis)) continue;
            if (!found.add(pos.immutable()) || found.size() > 441) return List.of();
            pending.add(pos.above()); pending.add(pos.below());
            pending.add(pos.relative(horizontal)); pending.add(pos.relative(horizontal.getOpposite()));
        }
        int minH = Integer.MAX_VALUE, maxH = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (BlockPos pos : found) {
            int h = axis == Direction.Axis.X ? pos.getX() : pos.getZ();
            minH = Math.min(minH, h); maxH = Math.max(maxH, h);
            minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
        }
        int width = maxH - minH + 1, height = maxY - minY + 1;
        if (width < 2 || width > 21 || height < 3 || height > 21 || found.size() != width * height) return List.of();
        for (int h = minH; h <= maxH; h++) {
            if (!frame(level, at(start, axis, h, minY - 1), frame) || !frame(level, at(start, axis, h, maxY + 1), frame)) return List.of();
        }
        for (int y = minY; y <= maxY; y++) {
            if (!frame(level, at(start, axis, minH - 1, y), frame) || !frame(level, at(start, axis, maxH + 1, y), frame)) return List.of();
        }
        return List.copyOf(found);
    }
    private static boolean frame(LevelReader level, BlockPos pos, Block frame) { return level.getBlockState(pos).is(frame); }
    private static BlockPos at(BlockPos start, Direction.Axis axis, int h, int y) {
        return axis == Direction.Axis.X ? new BlockPos(h, y, start.getZ()) : new BlockPos(start.getX(), y, h);
    }
}
