package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.BackEquipment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DevilWingsFlightMixin {
    @Inject(method="shouldTravelInFluid",at=@At("HEAD"),cancellable=true)
    private void darkdimensions$lavaAir(FluidState fluid,CallbackInfoReturnable<Boolean> cir){var e=(LivingEntity)(Object)this;if(e.isFallFlying()&&BackEquipment.lavaFlight(e))cir.setReturnValue(false);}
}
