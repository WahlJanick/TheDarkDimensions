package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.DevilArmor;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class DevilArmorMovementMixin {
    @ModifyExpressionValue(method="travelInFluid",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
    private boolean darkDimensions$flow(boolean water) {return water||DevilArmor.swimming((LivingEntity)(Object)this);}
}
