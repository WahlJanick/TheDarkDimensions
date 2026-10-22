package at.janic.thedarkdimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.network.chat.Component;

/** Location associates this portable block with its room; it needs no copied owner NBT. */
public final class ReturnAnchorBlock extends Block {
    public ReturnAnchorBlock(Properties p){super(p);}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return Block.column(12,0,16);}
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(stack.is(RuinsContent.FOREIGN_KEY)){
            if(player instanceof ServerPlayer p){
                var room=PocketDimensions.data(p).at(p.level(),pos);
                if(room==null){p.sendSystemMessage(Component.translatable("message.thedarkdimensions.anchor_outside"),true);return InteractionResult.FAIL;}
                ForeignKeyItem.bind(stack,room);p.sendSystemMessage(Component.translatable("message.thedarkdimensions.foreign_bound",room.size()),true);
            }return InteractionResult.SUCCESS;
        }
        // A held building block still places normally; empty-hand use triggers return.
        return stack.isEmpty()?InteractionResult.TRY_WITH_EMPTY_HAND:InteractionResult.PASS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(player instanceof ServerPlayer p){
            if(!PocketDimensions.isPocket(level)){p.sendSystemMessage(Component.translatable("message.thedarkdimensions.anchor_outside"),true);return InteractionResult.FAIL;}
            if(!PocketDimensions.leave(p))return InteractionResult.FAIL;
        }return InteractionResult.SUCCESS;
    }
}
