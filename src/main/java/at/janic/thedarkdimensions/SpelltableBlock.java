package at.janic.thedarkdimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.shapes.*;

/** Opens the spell-selection menu without a persistent block inventory. */
public final class SpelltableBlock extends Block {
    private static final VoxelShape SHAPE=Block.column(16,0,12);
    public SpelltableBlock(BlockBehaviour.Properties p) { super(p); }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return SHAPE; }
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit) {
        if(!level.isClientSide())player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p) -> new SpelltableMenu(id,inventory,net.minecraft.world.inventory.ContainerLevelAccess.create(level,pos)),net.minecraft.network.chat.Component.translatable("block.thedarkdimensions.spelltable")));
        return net.minecraft.world.InteractionResult.SUCCESS;
    }
}
