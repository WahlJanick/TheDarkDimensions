package at.janic.thedarkdimensions;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.*;
import net.minecraft.network.chat.Component;

/** The bearer may visit an explicitly linked room, including when its owner is offline. */
public final class ForeignKeyItem extends Item {
    public ForeignKeyItem(Properties p){super(p);}
    public static void bind(ItemStack key,PocketDimensions.Room room){
        CustomData.update(DataComponents.CUSTOM_DATA,key,t -> {t.putString("pocket_owner",room.owner());t.putBoolean("pocket_large",room.large());});
    }
    public static UUID owner(ItemStack key){
        String id=key.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr("pocket_owner","");
        try{return UUID.fromString(id);}catch(IllegalArgumentException e){return null;}
    }
    public static boolean large(ItemStack key){return key.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBooleanOr("pocket_large",false);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer p){
            ItemStack key=p.getItemInHand(hand);UUID owner=owner(key);
            if(owner==null){p.sendSystemMessage(Component.translatable("message.thedarkdimensions.foreign_unlinked"),true);return InteractionResult.FAIL;}
            if(p.getCooldowns().isOnCooldown(key)||!PocketDimensions.enter(p,owner,large(key),false))return InteractionResult.FAIL;
            p.getCooldowns().addCooldown(key,20);
        }return InteractionResult.SUCCESS;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> builder,TooltipFlag flag){
        super.appendHoverText(stack,context,display,builder,flag);UUID owner=owner(stack);
        builder.accept(Component.translatable(owner==null?"tooltip.thedarkdimensions.foreign_unlinked":"tooltip.thedarkdimensions.foreign_linked",large(stack)?20:10).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        if(owner!=null)builder.accept(Component.literal(owner.toString()).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
    }
}
