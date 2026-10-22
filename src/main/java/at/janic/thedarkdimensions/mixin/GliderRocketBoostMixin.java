package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.BackEquipment;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketEntity.class)
public abstract class GliderRocketBoostMixin {
    @Shadow private LivingEntity attachedToEntity;
    @ModifyExpressionValue(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;isFallFlying()Z"))
    private boolean darkdimensions$noMidflightWoolBoost(boolean flying){return flying&&(attachedToEntity==null||!BackEquipment.rocketBlocked(attachedToEntity));}
}
