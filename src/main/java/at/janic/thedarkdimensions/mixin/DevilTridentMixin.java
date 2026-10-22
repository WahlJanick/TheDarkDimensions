package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownTrident.class)
public abstract class DevilTridentMixin implements DevilProjectile {
    @Unique private void darkDimensions$identity(ItemStack stack) {((ThrownTrident)(Object)this).setData(DevilContent.DEVIL_PROJECTILE,stack.is(DevilContent.TRIDENT));}
    @Inject(method="<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",at=@At("TAIL"))
    private void darkDimensions$owner(Level level,LivingEntity owner,ItemStack stack,CallbackInfo ci) {darkDimensions$identity(stack);}
    @Inject(method="<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V",at=@At("TAIL"))
    private void darkDimensions$dispenser(Level level,double x,double y,double z,ItemStack stack,CallbackInfo ci) {darkDimensions$identity(stack);}
    @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
    private void darkDimensions$load(ValueInput input,CallbackInfo ci) {darkDimensions$identity(((ThrownTrident)(Object)this).getWeaponItem());}
    @Override public boolean darkDimensions$isDevil() {return ((ThrownTrident)(Object)this).getData(DevilContent.DEVIL_PROJECTILE);}
}
