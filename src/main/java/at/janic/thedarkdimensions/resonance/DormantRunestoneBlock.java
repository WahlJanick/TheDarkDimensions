package at.janic.thedarkdimensions.resonance;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class DormantRunestoneBlock extends BaseEntityBlock {
    public static final EnumProperty<TrialKind> KIND=EnumProperty.create("kind",TrialKind.class);
    public DormantRunestoneBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(KIND,TrialKind.EMBER));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(KIND);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new TrialController(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState s,BlockEntityType<T> type){return level.isClientSide()?null:createTickerHelper(type,ResonanceContent.CONTROLLER.get(),(l,p,state,be)->be.serverTick());}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(player instanceof ServerPlayer p&&level.getBlockEntity(pos) instanceof TrialController trial){
            if(stack.is(ResonanceContent.JOURNAL)){FieldJournal.saveWaypoint(p,trial);return InteractionResult.SUCCESS;}
            trial.interact(p,hand);return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){if(player instanceof ServerPlayer p&&l.getBlockEntity(pos) instanceof TrialController trial)trial.interact(p,InteractionHand.MAIN_HAND);return InteractionResult.SUCCESS;}
}
