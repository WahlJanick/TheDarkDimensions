package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.ArmoryContent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class HammerGlidingMixin {
    @Inject(method="canGlideUsing",at=@At("HEAD"),cancellable=true)
    private static void darkDimensions$heldHammer(ItemStack stack,EquipmentSlot slot,CallbackInfoReturnable<Boolean> cir) {
        if((slot==EquipmentSlot.MAINHAND||slot==EquipmentSlot.OFFHAND)&&ArmoryContent.isHammer(stack))cir.setReturnValue(ArmoryContent.canGlide(stack));
    }
}
