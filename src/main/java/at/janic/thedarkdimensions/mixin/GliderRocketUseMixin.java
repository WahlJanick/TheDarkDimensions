package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.BackEquipment;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FireworkRocketItem.class)
public abstract class GliderRocketUseMixin {
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void darkdimensions$noWoolBoost(Level level,Player p,InteractionHand hand,CallbackInfoReturnable<InteractionResult> cir){if(BackEquipment.rocketBlocked(p))cir.setReturnValue(InteractionResult.FAIL);}
}
