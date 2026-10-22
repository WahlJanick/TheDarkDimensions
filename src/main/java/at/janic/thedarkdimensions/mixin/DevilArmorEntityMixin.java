package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.DevilArmor;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Entity.class)
public abstract class DevilArmorEntityMixin {
    @Inject(method="fireImmune",at=@At("HEAD"),cancellable=true)
    private void darkDimensions$fire(CallbackInfoReturnable<Boolean> cir) {
        if((Object)this instanceof LivingEntity wearer&&DevilArmor.complete(wearer))cir.setReturnValue(true);
    }
    @Inject(method="updateSwimming",at=@At("HEAD"),cancellable=true)
    private void darkDimensions$swim(CallbackInfo ci) {
        if((Object)this instanceof LivingEntity wearer&&DevilArmor.swimming(wearer)) {
            wearer.setSwimming(wearer.isSprinting()&&!wearer.isPassenger()&&(wearer.isSwimming()||wearer.isEyeInFluid(FluidTags.LAVA)));
            ci.cancel();
        }
    }
}
