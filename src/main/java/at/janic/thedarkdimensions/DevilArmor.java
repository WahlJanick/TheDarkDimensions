package at.janic.thedarkdimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.*;

public final class DevilArmor {
    public static final ResourceKey<Enchantment> ENCHANTMENT=ResourceKey.create(Registries.ENCHANTMENT,TheDarkDimensions.id("devils_armor"));
    public static boolean complete(LivingEntity wearer) {
        var enchantment=wearer.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(ENCHANTMENT).orElse(null);
        if(enchantment==null)return false;
        for(var entry:BloodNetheriteContent.ARMOR_ITEMS.entrySet()) {
            var stack=wearer.getItemBySlot(entry.getKey().getSlot());
            if(!stack.is(entry.getValue())||EnchantmentHelper.getItemEnchantmentLevel(enchantment,stack)<1)return false;
        }
        return true;
    }
    public static boolean swimming(LivingEntity wearer) {return wearer.isInLava()&&complete(wearer)&&!(wearer.isFallFlying()&&BackEquipment.devilWings(wearer));}
    private DevilArmor() {}
}
