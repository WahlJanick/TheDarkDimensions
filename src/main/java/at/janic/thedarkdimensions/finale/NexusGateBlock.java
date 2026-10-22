package at.janic.thedarkdimensions.finale;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Entrance relay: players can attune Sigils before crossing the sealed wall. */
public final class NexusGateBlock extends Block {
    public NexusGateBlock(Properties p){super(p);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(player instanceof ServerPlayer p)for(var candidate:BlockPos.betweenClosed(pos.offset(-24,-5,-24),pos.offset(24,5,24)))if(level.getBlockState(candidate).is(FinaleContent.ALTAR)&&level.getBlockEntity(candidate) instanceof NexusAltar altar){altar.interactFromGate(p,hand,pos);break;}
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){return useItemOn(ItemStack.EMPTY,state,level,pos,player,InteractionHand.MAIN_HAND,hit);}
}
