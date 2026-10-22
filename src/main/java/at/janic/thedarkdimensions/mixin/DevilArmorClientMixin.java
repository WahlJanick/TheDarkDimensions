package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.DevilArmor;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class DevilArmorClientMixin {
    @ModifyExpressionValue(method={"aiStep","shouldStopSwimSprinting"},at=@At(value="INVOKE",target="Lnet/minecraft/client/player/LocalPlayer;isInWater()Z"))
    private boolean darkDimensions$controls(boolean water) {return water||DevilArmor.swimming((LocalPlayer)(Object)this);}
    @Inject(method="isUnderWater",at=@At("HEAD"),cancellable=true)
    private void darkDimensions$underLava(CallbackInfoReturnable<Boolean> cir) {
        var player=(LocalPlayer)(Object)this;
        if(player.isEyeInFluid(FluidTags.LAVA)&&DevilArmor.complete(player))cir.setReturnValue(true);
    }
}
