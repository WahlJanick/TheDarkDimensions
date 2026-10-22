package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;

final class RecipeBalanceTests {
    static void burningStick(GameTestHelper h){
        var igniter=new ItemStack(Items.FLINT_AND_STEEL);igniter.setDamageValue(7);
        igniter.set(DataComponents.CUSTOM_NAME,Component.literal("Reusable igniter"));
        igniter.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MENDING),1);
        for(boolean reverse:List.of(false,true)){
            var input=CraftingInput.of(2,2,reverse?List.of(ItemStack.EMPTY,new ItemStack(Items.STICK),igniter,ItemStack.EMPTY):List.of(igniter,ItemStack.EMPTY,ItemStack.EMPTY,new ItemStack(Items.STICK)));
            var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
            h.assertTrue(found.isPresent()&&found.get().value() instanceof BurningStickRecipe,"Native manager loads visible shapeless recipe in either position");
            var recipe=found.get().value();var output=recipe.assemble(input);
            h.assertTrue(output.is(TheDarkDimensions.BURNING_STICK)&&output.getCount()==1&&!recipe.display().isEmpty()&&!recipe.placementInfo().isImpossibleToPlace(),"Burning Stick has actual recipe-book display and placement");
            var remainder=recipe.getRemainingItems(input);int slot=reverse?2:0;var returned=remainder.get(slot);
            h.assertTrue(returned.is(Items.FLINT_AND_STEEL)&&returned.getCount()==1&&returned.getDamageValue()==10&&returned.isEnchanted()&&returned.get(DataComponents.CUSTOM_NAME).getString().equals("Reusable igniter"),"Returns original igniter with three wear, preserving name and enchantment");
            h.assertTrue(igniter.getDamageValue()==7&&remainder.stream().filter(s->!s.isEmpty()).count()==1,"Preview never mutates input or returns consumed stick");
            igniter.setDamageValue(igniter.getMaxDamage()-4);
            h.assertTrue(recipe.getRemainingItems(input).get(slot).getDamageValue()==igniter.getMaxDamage()-1,"Igniter survives with one durability remaining");
            igniter.setDamageValue(igniter.getMaxDamage()-3);
            h.assertTrue(recipe.matches(input,h.getLevel())&&recipe.getRemainingItems(input).get(slot).isEmpty(),"Last craft succeeds and exhausted igniter breaks");
            igniter.setDamageValue(7);
        }
        var old=CraftingInput.of(3,1,List.of(igniter,new ItemStack(Items.STICK),new ItemStack(TheDarkDimensions.ASHES.get())));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,old,h.getLevel()).isEmpty(),"Extra Ashes cannot craft the obsolete recipe");h.succeed();
    }
    static void templates(GameTestHelper h){
        var a=new ItemStack(TheDarkDimensions.ASHES.get());var d=new ItemStack(Items.DIAMOND);var o=new ItemStack(Items.CRYING_OBSIDIAN);var t=new ItemStack(ToolContent.IMPROVING_TEMPLATE.get());
        var old=CraftingInput.of(3,3,List.of(a,a,a,a,d,a,a,o,a));
        var manager=h.getLevel().getServer().getRecipeManager();
        h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,old,h.getLevel()).isEmpty(),"Initial Improving Template recipe is removed");
        var input=CraftingInput.of(3,3,List.of(d,t,d,d,o,d,d,d,d));
        var found=manager.getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());h.assertTrue(found.isPresent(),"Found template can still be duplicated");
        var output=found.get().value().assemble(input);h.assertTrue(output.is(ToolContent.IMPROVING_TEMPLATE)&&output.getCount()==2,"Existing template plus seven diamonds and crying obsidian returns two templates");h.succeed();
    }
    static void netherLoot(GameTestHelper h){
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);
        for(var chest:List.of("nether_bridge","bastion_treasure","bastion_bridge","bastion_hoglin_stable","bastion_other","end_city_treasure","desert_pyramid")){
            var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.withDefaultNamespace("chests/"+chest)));
            int count=0;boolean other=false;var random=RandomSource.create(30102);
            for(int n=0;n<500;n++)for(var stack:table.getRandomItems(params,random)){
                if(stack.is(ToolContent.IMPROVING_TEMPLATE)){h.assertTrue(stack.getCount()==1,"One discovered template per chest");count++;}else other=true;
            }
            h.assertTrue(chest.equals("bastion_treasure")?count>130&&count<220:chest.startsWith("bastion_")||chest.equals("nether_bridge")?count>60&&count<145:count==0,"Correct Nether-only template chance in native loot: "+chest+" "+count+"/500");
            h.assertTrue(other,"Original chest loot remains available");
        }
        h.succeed();
    }
}
