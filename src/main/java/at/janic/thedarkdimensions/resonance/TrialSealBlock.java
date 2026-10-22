package at.janic.thedarkdimensions.resonance;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;

public final class TrialSealBlock extends Block {
    public static final IntegerProperty INDEX=IntegerProperty.create("index",0,3);
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public TrialSealBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(INDEX,1).setValue(LIT,false).setValue(DormantRunestoneBlock.KIND,TrialKind.EMBER));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(INDEX,LIT,DormantRunestoneBlock.KIND);}
    private InteractionResult activate(Level l,BlockPos pos,Player player){if(player instanceof ServerPlayer p){var controller=TrialController.nearest(p.level(),pos,25);if(controller!=null&&controller.kind()==l.getBlockState(pos).getValue(DormantRunestoneBlock.KIND))controller.seal(p,pos);}return InteractionResult.SUCCESS;}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level l,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){return activate(l,pos,player);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level l,BlockPos pos,Player player,BlockHitResult hit){return activate(l,pos,player);}
}
