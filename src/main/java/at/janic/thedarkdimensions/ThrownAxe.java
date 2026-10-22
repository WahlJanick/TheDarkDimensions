package at.janic.thedarkdimensions;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.storage.*;

/** Arrow ground collision/pickup/save logic, but a recoverable weapon after hitting a mob. */
public final class ThrownAxe extends AbstractArrow implements ItemSupplier {
    private boolean dealtDamage;
    public ThrownAxe(EntityType<? extends ThrownAxe> type,Level level) { super(type,level); }
    public ThrownAxe(Level level,LivingEntity owner,ItemStack stack) { super(ArmoryContent.THROWN_AXE.get(),owner,level,stack,null); }
    @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(ArmoryContent.THROWABLE_AXE.get()); }
    @Override public ItemStack getWeaponItem(){return getPickupItemStackOrigin();}
    @Override public ItemStack getItem() { return getPickupItem(); }
    @Override public boolean isInGround() { return super.isInGround(); }
    @Override protected boolean canHitEntity(Entity entity) { return !dealtDamage&&super.canHitEntity(entity); }
    @Override protected void onHitEntity(EntityHitResult hit) {
        if(level() instanceof ServerLevel server) {
            dealtDamage=true;
            var owner=getOwner();
            hit.getEntity().hurtServer(server,damageSources().trident(this,owner==null?this:owner),8);
            setDeltaMovement(getDeltaMovement().scale(-0.1));
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) { super.addAdditionalSaveData(output);output.putBoolean("AxeDealtDamage",dealtDamage); }
    @Override protected void readAdditionalSaveData(ValueInput input) { super.readAdditionalSaveData(input);dealtDamage=input.getBooleanOr("AxeDealtDamage",false); }
}
