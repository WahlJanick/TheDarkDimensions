package at.janic.thedarkdimensions;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;

/** Katana combat enchantments; utility enchantments keep their native item rules. */
public final class KatanaEnchantments {
    public static final ResourceKey<Enchantment> SLASHING=key("slashing"),MASTER_PARRYING=key("master_parrying");
    private static ResourceKey<Enchantment> key(String id){return ResourceKey.create(Registries.ENCHANTMENT,TheDarkDimensions.id(id));}
    private static final Set<ResourceKey<Enchantment>> BLOCKED=Set.of(Enchantments.SHARPNESS,Enchantments.SMITE,Enchantments.BANE_OF_ARTHROPODS,Enchantments.FIRE_ASPECT,Enchantments.KNOCKBACK,Enchantments.SWEEPING_EDGE);
    public static boolean supports(ItemStack stack,Holder<Enchantment> enchantment){return !enchantment.unwrapKey().map(BLOCKED::contains).orElse(false)&&enchantment.value().isSupportedItem(stack);}
    public static void migrate(ItemStack stack,Level level){
        var current=EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if(current.keySet().stream().noneMatch(e->e.unwrapKey().map(BLOCKED::contains).orElse(false)))return;
        var sharp=level.registryAccess().getOrThrow(Enchantments.SHARPNESS);var slash=level.registryAccess().getOrThrow(SLASHING);
        int old=current.getLevel(sharp);
        EnchantmentHelper.updateEnchantments(stack,mutable->{
            mutable.removeIf(e->e.unwrapKey().map(BLOCKED::contains).orElse(false));
            if(old>0)mutable.upgrade(slash,Math.max(1,Math.min(3,old-1)));
        });
    }
    public static boolean secondCounter(ItemStack weapon,Level level,RandomSource random){
        var enchantment=level.registryAccess().getOrThrow(MASTER_PARRYING);
        return KatanaDefense.katana(weapon)&&EnchantmentHelper.getItemEnchantmentLevel(enchantment,weapon)>0&&random.nextFloat()<.3f;
    }
    private KatanaEnchantments(){}
}
