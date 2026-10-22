package at.janic.thedarkdimensions;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Copy every original component, including enchantments, damage and custom perks. */
public final class LuminousCoatingRecipe extends CustomRecipe {
    public static final LuminousCoatingRecipe INSTANCE=new LuminousCoatingRecipe();
    public static final RecipeSerializer<LuminousCoatingRecipe> SERIALIZER=new RecipeSerializer<>(MapCodec.unit(INSTANCE),StreamCodec.<RegistryFriendlyByteBuf,LuminousCoatingRecipe>unit(INSTANCE));
    private ItemStack weapon(CraftingInput input){
        if(input.ingredientCount()!=2)return ItemStack.EMPTY;
        ItemStack weapon=ItemStack.EMPTY;boolean coating=false;
        for(int i=0;i<input.size();i++){var stack=input.getItem(i);if(stack.isEmpty())continue;
            if(stack.is(PerkContent.LUMINOUS_COATING)&&!coating)coating=true;
            else if(weapon.isEmpty()&&PerkContent.isWeapon(stack)&&!PerkContent.coated(stack))weapon=stack;
            else return ItemStack.EMPTY;
        }
        return coating?weapon:ItemStack.EMPTY;
    }
    @Override public boolean matches(CraftingInput in,Level l){return !weapon(in).isEmpty();}
    @Override public ItemStack assemble(CraftingInput in){var original=weapon(in);if(original.isEmpty())return ItemStack.EMPTY;var result=original.copyWithCount(1);PerkContent.coat(result);return result;}
    @Override public RecipeSerializer<LuminousCoatingRecipe> getSerializer(){return PerkContent.COATING_RECIPE.get();}
}
