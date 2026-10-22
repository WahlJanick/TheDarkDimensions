package at.janic.thedarkdimensions;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public final class ThrowableAxeItem extends Item {
    public ThrowableAxeItem(Properties p) { super(p); }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var held=player.getItemInHand(hand);
        if(held.nextDamageWillBreak()||player.getCooldowns().isOnCooldown(held))return InteractionResult.FAIL;
        if(level instanceof ServerLevel server) {
            var thrown=held.copyWithCount(1);
            if(!player.hasInfiniteMaterials())thrown.setDamageValue(thrown.getDamageValue()+1);
            var axe=new ThrownAxe(server,player,thrown);
            axe.pickup=player.hasInfiniteMaterials()?AbstractArrow.Pickup.CREATIVE_ONLY:AbstractArrow.Pickup.ALLOWED;
            axe.shootFromRotation(player,player.getXRot(),player.getYRot(),0,1.8f,1);
            if(!server.addFreshEntity(axe))return InteractionResult.FAIL;
            if(!player.hasInfiniteMaterials())held.shrink(1);
            player.getCooldowns().addCooldown(new ItemStack(this),10);
            server.playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.TRIDENT_THROW.value(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);
        }
        return InteractionResult.SUCCESS;
    }
}
