package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.KatanaEnchantments;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/** Filter before the weighted draw, so previews and purchases use the same pool. */
@Mixin(EnchantmentMenu.class)
public abstract class KatanaEnchantingTableMixin {
    @ModifyArgs(method="getEnchantmentList",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/enchantment/EnchantmentHelper;selectEnchantment(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;"))
    private void darkDimensions$levelThirtyOnly(Args args){
        if((int)args.get(2)<30){Stream<Holder<Enchantment>> pool=args.get(3);args.set(3,pool.filter(e->!e.is(KatanaEnchantments.MASTER_PARRYING)));}
    }
}
