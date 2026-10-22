package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

public final class SolarForgeBlock extends BlastFurnaceBlock {
    public SolarForgeBlock(Properties p) { super(p); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new SolarForgeBlockEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        return createFurnaceTicker(level,type,SolarContent.SOLAR_FORGE_ENTITY.get());
    }
    @Override protected void openContainer(Level level,BlockPos pos,Player player) {
        if(level.getBlockEntity(pos) instanceof SolarForgeBlockEntity forge)player.openMenu(forge);
    }
}
