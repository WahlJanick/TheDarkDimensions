package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.MineralContent;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.CrafterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrafterBlock.class)
public abstract class EnderEyeCrafterMixin {
    /** Crafter caches negative matches; this one progression-dependent recipe must be rechecked. */
    @Inject(method="getPotentialResults",at=@At("HEAD"),cancellable=true)
    private static void recheck(ServerLevel level,CraftingInput input,CallbackInfoReturnable<Optional<RecipeHolder<CraftingRecipe>>> result){
        boolean pearl=false,essentialium=false;for(int i=0;i<input.size();i++){pearl|=input.getItem(i).is(Items.ENDER_PEARL);essentialium|=input.getItem(i).is(MineralContent.ESSENTIALIUM);}
        if(pearl&&essentialium)result.setReturnValue(level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level));
    }
}
