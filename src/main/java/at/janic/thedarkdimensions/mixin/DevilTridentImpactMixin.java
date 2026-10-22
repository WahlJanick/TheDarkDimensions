package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class DevilTridentImpactMixin {
    @Inject(method="doPostHurtEffects",at=@At("TAIL"))
    private void darkDimensions$hit(LivingEntity target,CallbackInfo ci) {
        if((Object)this instanceof ThrownTrident trident&&trident.getWeaponItem().is(DevilContent.TRIDENT))
            DevilContent.impact(target,trident.getOwner());
    }
}
