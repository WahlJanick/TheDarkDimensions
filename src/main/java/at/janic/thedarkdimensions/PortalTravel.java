package at.janic.thedarkdimensions;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class PortalTravel {
    private PortalTravel() {}
    public static TeleportTransition destination(ServerLevel current, Entity entity, BlockPos entry, boolean echo) {
        ResourceKey<Level> outward = echo ? DarkContent.ECHO_DEPTHS : DarkContent.BURNED_REALM;
        boolean returning = current.dimension().equals(outward);
        String key = echo ? "tdd_echo_return" : "tdd_realm_return";
        CompoundTag saved = entity.getPersistentData().getCompoundOrEmpty(key);
        ResourceKey<Level> destination = outward;
        BlockPos near = entry;
        if (returning) {
            Identifier sourceId = Identifier.tryParse(saved.getStringOr("dimension", echo ? DarkContent.BURNED_REALM.identifier().toString() : "minecraft:overworld"));
            destination = sourceId == null ? (echo ? DarkContent.BURNED_REALM : Level.OVERWORLD) : ResourceKey.create(Registries.DIMENSION, sourceId);
            near = new BlockPos(saved.getIntOr("x", entry.getX()), saved.getIntOr("y", 80), saved.getIntOr("z", entry.getZ()));
        }
        ServerLevel target = current.getServer().getLevel(destination);
        if (target == null || target == current) return null;
        BlockPos bounded = target.getWorldBorder().clampToBounds(near);
        Block portal = echo ? DarkContent.ECHO_PORTAL.get() : TheDarkDimensions.IMPROVED_PORTAL.get();
        Block frame = echo ? DarkContent.IMPROVED_OBSIDIAN.get() : Blocks.OBSIDIAN;
        var poiType = echo ? DarkContent.ECHO_PORTAL_POI : DarkContent.REALM_PORTAL_POI;
        var pois = target.getPoiManager();
        pois.ensureLoadedAndValid(target, bounded, 32);
        BlockPos exit = pois.getInSquare(p -> p.is(poiType.getKey()), bounded, 32, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos).filter(target.getWorldBorder()::isWithinBounds)
                .filter(p -> !PortalLogic.find(target, p, portal, frame).isEmpty())
                .min(Comparator.comparingDouble(p -> p.distSqr(bounded))).orElse(null);
        if (exit == null) exit = createExit(target, bounded, portal, frame);
        if (exit == null) return null;
        var area = PortalLogic.find(target, exit, portal, frame);
        BlockPos bottom = area.stream().min(Comparator.<BlockPos>comparingInt(p -> p.getY()).thenComparingInt(p -> p.getX()).thenComparingInt(p -> p.getZ())).orElse(exit);
        var axis = target.getBlockState(bottom).getValue(NetherPortalBlock.AXIS);
        Vec3 arrival = Vec3.atBottomCenterOf(bottom).add(axis == Direction.Axis.X ? 0.5 : 0, 0.05, axis == Direction.Axis.Z ? 0.5 : 0);
        CompoundTag returnData = new CompoundTag();
        returnData.putString("dimension", current.dimension().identifier().toString());
        returnData.putInt("x", entry.getX()); returnData.putInt("y", entry.getY()); returnData.putInt("z", entry.getZ());
        return new TeleportTransition(target, arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(e -> {
                    e.setPortalCooldown();
                    e.placePortalTicket(BlockPos.containing(arrival));
                    if (!returning) e.getPersistentData().put(key, returnData);
                }));
    }
    /** Builds a lit return portal on a dry, cleared landing platform, without clearing solid player builds. */
    public static BlockPos createExit(ServerLevel level, BlockPos near, Block portal, Block frame) {
        BlockPos chosen = null;
        for (BlockPos candidate : BlockPos.spiralAround(near, 8, Direction.EAST, Direction.SOUTH)) {
            int y = Math.max(level.getSeaLevel() + 1, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate.getX(), candidate.getZ()));
            y = Mth.clamp(y, level.getMinY() + 2, level.getMaxY() - 6);
            BlockPos base = new BlockPos(candidate.getX(), y, candidate.getZ());
            if (!level.getWorldBorder().isWithinBounds(base.offset(-2, 0, -2)) || !level.getWorldBorder().isWithinBounds(base.offset(3, 4, 2))) continue;
            boolean clear = true;
            for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, 0, -2), base.offset(3, 4, 2))) {
                var state = level.getBlockState(p);
                if (!state.isAir() && !state.canBeReplaced()) { clear = false; break; }
            }
            if (clear) { chosen = base; break; }
        }
        // Dense forests and steep slopes can leave no clear spot at ground height.
        // Put the platform above the highest obstruction rather than silently failing travel.
        if (chosen == null) {
            int y = level.getSeaLevel() + 1;
            for (int x = -2; x <= 3; x++) for (int z = -2; z <= 2; z++)
                y = Math.max(y, level.getHeight(Heightmap.Types.MOTION_BLOCKING, near.getX()+x, near.getZ()+z)+1);
            BlockPos elevated = new BlockPos(near.getX(),y,near.getZ());
            if (y + 4 >= level.getMaxY() || !level.getWorldBorder().isWithinBounds(elevated.offset(-2,0,-2))
                    || !level.getWorldBorder().isWithinBounds(elevated.offset(3,4,2))) return null;
            chosen = elevated;
        }
        BlockPos base = chosen;
        Block platform = level.dimension().equals(Level.OVERWORLD) || level.dimension().equals(Level.NETHER) ? Blocks.OBSIDIAN : DarkContent.BURNED_BRICKS.get();
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-2, -1, -2), base.offset(3, -1, 2))) level.setBlock(p, platform.defaultBlockState(), 2);
        for (int x = -1; x <= 2; x++) for (int y = -1; y <= 3; y++) {
            BlockPos p = base.offset(x, y, 0);
            level.setBlock(p, x == -1 || x == 2 || y == -1 || y == 3 ? frame.defaultBlockState()
                    : portal.defaultBlockState().setValue(NetherPortalBlock.AXIS, Direction.Axis.X), 18);
        }
        return base;
    }
}
