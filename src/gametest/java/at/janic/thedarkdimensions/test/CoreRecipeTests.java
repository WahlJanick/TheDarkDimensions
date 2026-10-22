package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

final class CoreRecipeTests {
    private static CraftingInput cross(Item ingredient,Item middle){
        var s=new ItemStack(ingredient);var e=ItemStack.EMPTY;
        return CraftingInput.of(3,3,List.of(e,s,e,s,new ItemStack(middle),s,e,s,e));
    }
    private static CraftingRecipe crafts(GameTestHelper h,CraftingInput input,Item expected){
        var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(recipe.isPresent(),"Recipe is loaded for "+expected);
        var output=recipe.get().value().assemble(input);h.assertTrue(output.is(expected)&&output.getCount()==1,"Recipe crafts exactly one expected item");
        return recipe.get().value();
    }
    private static void rejects(GameTestHelper h,CraftingInput input){h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).isEmpty(),"Invalid or obsolete ingredient arrangement must not craft");}
    static void fire(GameTestHelper h){
        crafts(h,cross(Items.STONE,Items.DIAMOND),TheDarkDimensions.EMPTY_RUNESTONE.get());
        rejects(h,cross(Items.COBBLESTONE,Items.DIAMOND));
        var p=new ItemStack(Items.BLAZE_POWDER);var rune=new ItemStack(TheDarkDimensions.EMPTY_RUNESTONE.get());
        crafts(h,CraftingInput.of(3,3,List.of(p,p,p,p,rune,p,p,p,p)),TheDarkDimensions.FIRE_CORE.get());
        rejects(h,CraftingInput.of(3,3,List.of(p,p,p,p,rune,p,p,p,ItemStack.EMPTY)));
        var o=new ItemStack(Items.CRYING_OBSIDIAN);var s=new ItemStack(TheDarkDimensions.IMPROVED_STICK.get());
        rejects(h,CraftingInput.of(3,3,List.of(o,s,o,s,new ItemStack(Items.GOLD_BLOCK),s,o,o,o)));h.succeed();
    }
    static void water(GameTestHelper h){
        var input=cross(Items.WATER_BUCKET,TheDarkDimensions.EMPTY_RUNESTONE.get());
        var recipe=crafts(h,input,CaveContent.WATER_CORE.get());var leftovers=recipe.getRemainingItems(input);
        for(int i=0;i<9;i++)h.assertTrue((i==1||i==3||i==5||i==7)?leftovers.get(i).is(Items.BUCKET)&&leftovers.get(i).getCount()==1:leftovers.get(i).isEmpty(),"Each water bucket returns one empty bucket, while the Runestone is consumed");
        rejects(h,cross(Items.BUCKET,TheDarkDimensions.EMPTY_RUNESTONE.get()));
        rejects(h,cross(Items.WATER_BUCKET,Items.DIAMOND));h.succeed();
    }
    static void lightning(GameTestHelper h){
        var rods=Items.LIGHTNING_ROD.asList();h.assertTrue(rods.size()==8,"Four weathering stages and four waxed stages are present");
        for(var rod:rods)crafts(h,cross(rod,TheDarkDimensions.EMPTY_RUNESTONE.get()),CaveContent.LIGHTNING_CORE.get());
        var e=ItemStack.EMPTY;
        crafts(h,CraftingInput.of(3,3,List.of(e,new ItemStack(rods.get(0)),e,new ItemStack(rods.get(3)),new ItemStack(TheDarkDimensions.EMPTY_RUNESTONE.get()),new ItemStack(rods.get(5)),e,new ItemStack(rods.get(7)),e)),CaveContent.LIGHTNING_CORE.get());
        rejects(h,CraftingInput.of(1,2,List.of(new ItemStack(rods.getFirst()),new ItemStack(DarkContent.IMPROVED_ECHO_SHARD.get()))));
        rejects(h,cross(Items.COPPER_INGOT,TheDarkDimensions.EMPTY_RUNESTONE.get()));h.succeed();
    }
}
