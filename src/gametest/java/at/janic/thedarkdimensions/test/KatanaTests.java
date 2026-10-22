package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

final class KatanaTests {
    private static void recipe(GameTestHelper h,List<ItemStack> stacks,int width,int height,Item result) {
        var input=CraftingInput.of(width,height,stacks);
        var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(result),"Recipe crafts "+result);
    }
    static void essences(GameTestHelper h) {
        recipe(h,List.of(new ItemStack(Items.CHERRY_SAPLING),new ItemStack(CaveContent.WATER_CORE.get())),2,1,KatanaContent.JAPANESE_ESSENCE.get());
        var blood=new ItemStack(KatanaContent.CREEPERBLOOD.get());
        recipe(h,List.of(blood,new ItemStack(Items.GOLD_INGOT)),2,1,KatanaContent.BLOOD_ESSENCE.get());
        recipe(h,List.of(new ItemStack(KatanaContent.BLOOD_ESSENCE.get()),new ItemStack(CaveContent.WATER_CORE.get())),2,1,KatanaContent.BLOOD_CORE.get());
        recipe(h,List.of(new ItemStack(Items.SOUL_SAND),new ItemStack(Items.BLACKSTONE)),2,1,KatanaContent.SOUL_ESSENCE.get());
        recipe(h,List.of(new ItemStack(KatanaContent.SOUL_ESSENCE.get()),new ItemStack(TheDarkDimensions.FIRE_CORE.get())),2,1,KatanaContent.SOUL_CORE.get());
        var incomplete=CraftingInput.of(2,2,List.of(blood,blood,blood,new ItemStack(Items.GOLD_INGOT)));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,incomplete,h.getLevel()).isEmpty(),"Blood Essence now requires exactly one blood ingredient plus gold");h.succeed();
    }
    static void katanas(GameTestHelper h) {
        var essences=List.of(KatanaContent.JAPANESE_ESSENCE.get(),KatanaContent.BLOOD_CORE.get(),KatanaContent.SOUL_CORE.get());
        var blades=List.of(KatanaContent.FROSTGLEAM.get(),KatanaContent.BLOODGRIEF.get(),KatanaContent.SOULEATER.get());
        for(int i=0;i<3;i++) {
            var essence=new ItemStack(essences.get(i));var stick=new ItemStack(RelicKatanas.BASIC.get());
            var middle=i==0?new ItemStack(Items.BLUE_ICE):essence;
            recipe(h,List.of(ItemStack.EMPTY,ItemStack.EMPTY,essence,ItemStack.EMPTY,middle,ItemStack.EMPTY,stick,ItemStack.EMPTY,ItemStack.EMPTY),3,3,blades.get(i));
            var incorrect=CraftingInput.of(3,3,List.of(ItemStack.EMPTY,ItemStack.EMPTY,essence,ItemStack.EMPTY,middle,ItemStack.EMPTY,new ItemStack(Items.STICK),ItemStack.EMPTY,ItemStack.EMPTY));
            h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,incorrect,h.getLevel()).isEmpty(),"Special katana requires Basic Katana");
            if(i==0) {
                var oldRecipe=CraftingInput.of(3,3,List.of(ItemStack.EMPTY,ItemStack.EMPTY,essence,ItemStack.EMPTY,essence,ItemStack.EMPTY,stick,ItemStack.EMPTY,ItemStack.EMPTY));
                h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,oldRecipe,h.getLevel()).isEmpty(),"Frostgleam requires Blue Ice instead of a second Japanese Essence");
            }
        }
        h.succeed();
    }
    static void stats(GameTestHelper h) {
        var blades=List.of(KatanaContent.FROSTGLEAM.get(),KatanaContent.BLOODGRIEF.get(),KatanaContent.SOULEATER.get());
        var player=h.makeMockPlayer(GameType.SURVIVAL);double reach=player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        double[] damage={6,8,11.5};int[] durability={750,1250,2100};
        for(int i=0;i<3;i++) {
            var stack=new ItemStack(blades.get(i));
            h.assertTrue(stack.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==damage[i],"Damage follows the three tiers");
            h.assertTrue(Math.abs(stack.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND)-(i<2?1.5:1.6))<0.001,"Requested attack speed");h.assertTrue(stack.getMaxDamage()==durability[i],"Increasing durability");
            h.assertTrue(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS).value().canEnchant(stack),"Accepts sword enchantments");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);new GameplayEvents().playerTick(new PlayerTickEvent.Post(player));
            h.assertTrue(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==reach+2,"Improved handle adds two blocks of range");
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);new GameplayEvents().playerTick(new PlayerTickEvent.Post(player));
        h.assertTrue(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==reach,"Range returns to normal when put away");h.succeed();
    }
    static void bloodDrops(GameTestHelper h) {
        var creeper=h.spawn(EntityTypes.CREEPER,new BlockPos(4,2,4));
        creeper.getRandom().setSeed(263);
        var reference=net.minecraft.util.RandomSource.create(263);int total=0;
        for(int i=0;i<2000;i++) {
            var collected=new ArrayList<ItemEntity>();
            var powder=new ItemEntity(h.getLevel(),0,0,0,new ItemStack(Items.GUNPOWDER));collected.add(powder);
            boolean expected=reference.nextFloat()<0.05f;
            new GameplayEvents().creeperDrops(new LivingDropsEvent(creeper,creeper.damageSources().genericKill(),collected,true));
            int count=collected.stream().filter(e -> e.getItem().is(KatanaContent.CREEPERBLOOD)).mapToInt(e -> e.getItem().getCount()).sum();
            h.assertTrue(count==(expected?1:0)&&collected.contains(powder),"Exactly one blood with 5% threshold; preserves vanilla drops");total+=count;
        }
        h.assertTrue(total>50&&total<150,"Sample matches five-percent drop rate");h.succeed();
    }
}
