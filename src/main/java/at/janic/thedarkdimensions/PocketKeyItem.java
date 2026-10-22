package at.janic.thedarkdimensions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class PocketKeyItem extends Item {
    private final boolean large;
    public PocketKeyItem(Properties p) {this(p,false);}
    public PocketKeyItem(Properties p,boolean large) { super(p);this.large=large; }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer serverPlayer) {
            if(player.getCooldowns().isOnCooldown(player.getItemInHand(hand)))return InteractionResult.FAIL;
            if(!PocketDimensions.toggle(serverPlayer,large))return InteractionResult.FAIL;
            player.getCooldowns().addCooldown(player.getItemInHand(hand),20);
        }
        return InteractionResult.SUCCESS;
    }
}
