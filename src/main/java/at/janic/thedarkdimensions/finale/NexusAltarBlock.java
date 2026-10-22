package at.janic.thedarkdimensions.finale;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class NexusAltarBlock extends BaseEntityBlock {
    public static final IntegerProperty MASK=IntegerProperty.create("attuned",0,7);
    public NexusAltarBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(MASK,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(MASK);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new NexusAltar(pos,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> type){return l.isClientSide()?null:createTickerHelper(type,FinaleContent.ALTAR_ENTITY.get(),(level,pos,state,be)->be.tickServer());}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(player instanceof ServerPlayer p&&l.getBlockEntity(pos) instanceof NexusAltar altar)altar.interact(p,hand);return InteractionResult.SUCCESS;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){if(player instanceof ServerPlayer p&&l.getBlockEntity(pos) instanceof NexusAltar altar)altar.interact(p,InteractionHand.MAIN_HAND);return InteractionResult.SUCCESS;}
}
