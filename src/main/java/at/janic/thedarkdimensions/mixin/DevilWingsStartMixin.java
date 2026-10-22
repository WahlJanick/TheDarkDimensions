package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.BackEquipment;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class DevilWingsStartMixin {
    @ModifyExpressionValue(method="tryToStartFallFlying",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;isInLiquid()Z"))
    private boolean darkdimensions$startInLava(boolean liquid){return liquid&&!BackEquipment.lavaFlight((Player)(Object)this);}
}
