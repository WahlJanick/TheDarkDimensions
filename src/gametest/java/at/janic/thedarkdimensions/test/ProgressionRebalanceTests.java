package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

final class ProgressionRebalanceTests {
    static void warriorUpgrades(GameTestHelper h) {
        var manager=h.getLevel().getServer().getRecipeManager();
        for(var tier:ArmoryContent.TIERS) {
            var base=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(tier.name()+"_axe")));
            base.setDamageValue(7);base.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Warrior keepsake"));
            base.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),2);
            var input=new SmithingRecipeInput(new ItemStack(ArmoryContent.WARRIOR_TEMPLATE.get()),base,new ItemStack(ArmoryContent.WARRIOR_STICK.get()));
            var found=manager.getRecipeFor(RecipeType.SMITHING,input,h.getLevel());h.assertTrue(found.isPresent(),"Warrior conversion exists for "+tier.name());
            var result=found.get().value().assemble(input);
            h.assertTrue(result.is(ArmoryContent.WARRIOR_AXES.get(tier.name()))&&result.getDamageValue()==7&&result.isEnchanted()&&result.get(DataComponents.CUSTOM_NAME).getString().equals("Warrior keepsake"),"Warrior conversion preserves components");
            if(tier.name().equals("diamond")) {
                input=new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),result,new ItemStack(Items.NETHERITE_INGOT));
                found=manager.getRecipeFor(RecipeType.SMITHING,input,h.getLevel());h.assertTrue(found.isPresent(),"Native Netherite upgrade also accepts Diamond Warrior Axe");
                result=found.get().value().assemble(input);h.assertTrue(result.is(ArmoryContent.WARRIOR_AXES.get("netherite"))&&result.getDamageValue()==7&&result.isEnchanted()&&result.get(DataComponents.CUSTOM_NAME).getString().equals("Warrior keepsake"),"Netherite progression preserves components");
            }
        }
        var i=new ItemStack(Items.IRON_INGOT);var b=new ItemStack(KatanaContent.CREEPERBLOOD.get());var z=new ItemStack(KatanaContent.ZOMBIEBLOOD.get());var l=new ItemStack(Items.LEATHER);var s=new ItemStack(TheDarkDimensions.IMPROVED_STICK.get());
        var grid=CraftingInput.of(3,3,List.of(i,b,i,l,s,l,i,z,i));var found=manager.getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel());
        h.assertTrue(found.isPresent()&&found.get().value().assemble(grid).is(ArmoryContent.WARRIOR_TEMPLATE),"Template accepts mixed blood, four iron, two leather and Improved Stick");
        var n=new ItemStack(Items.NETHERITE_INGOT);grid=CraftingInput.of(3,3,List.of(n,new ItemStack(Items.NETHERITE_BLOCK),n,n,s,ItemStack.EMPTY,ItemStack.EMPTY,new ItemStack(ArmoryContent.WARRIOR_STICK.get()),ItemStack.EMPTY));
        h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel()).isEmpty(),"Direct Netherite Warrior crafting is removed");
        var menu=new net.minecraft.world.inventory.SmithingMenu(0,h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL).getInventory());
        h.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(ArmoryContent.WARRIOR_TEMPLATE.get())),"Native smithing menu accepts Warrior template");h.succeed();
    }
    static void mixedBlood(GameTestHelper h) {
        var manager=h.getLevel().getServer().getRecipeManager();
        for(var blood:List.of(KatanaContent.CREEPERBLOOD.get(),KatanaContent.ZOMBIEBLOOD.get())) {
            var input=CraftingInput.of(2,1,List.of(new ItemStack(blood),new ItemStack(Items.GOLD_INGOT)));var found=manager.getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
            h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(KatanaContent.BLOOD_ESSENCE)&&found.get().value().assemble(input).getCount()==1,"Either blood crafts one essence per gold");
        }
        var gold=new ItemStack(Items.GOLD_INGOT);var input=CraftingInput.of(2,2,List.of(new ItemStack(KatanaContent.CREEPERBLOOD.get()),gold,new ItemStack(KatanaContent.ZOMBIEBLOOD.get()),gold));
        var found=manager.getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(KatanaContent.BLOOD_ESSENCE)&&found.get().value().assemble(input).getCount()==2,"Mixed blood produces two essences at identical unit cost");
        input=CraftingInput.of(2,1,List.of(new ItemStack(Items.ROTTEN_FLESH),gold));h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).isEmpty(),"Rotten flesh cannot replace blood");
        var oldBlank=CraftingInput.of(2,1,List.of(new ItemStack(Items.BOOK),new ItemStack(CaveContent.SPELL_CORE.get())));h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,oldBlank,h.getLevel()).isEmpty(),"Blank recipe no longer requires an elemental core");
        var oldMaster=new ArrayList<ItemStack>(Collections.nCopies(9,new ItemStack(CaveContent.SPELL_CORE.get())));oldMaster.set(4,new ItemStack(SpellContent.SPELL_BOOK.get()));
        h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,CraftingInput.of(3,3,oldMaster),h.getLevel()).isEmpty(),"Old eight-core master recipe is removed");h.succeed();
    }
    static void zombieDrops(GameTestHelper h) {
        List<EntityType<? extends LivingEntity>> types=List.of(EntityTypes.ZOMBIE,EntityTypes.HUSK,EntityTypes.DROWNED,EntityTypes.ZOMBIE_VILLAGER,EntityTypes.ZOMBIFIED_PIGLIN,EntityTypes.ZOMBIE_HORSE,EntityTypes.ZOMBIE_NAUTILUS,EntityTypes.ZOGLIN,TheDarkDimensions.MUMMY.get());
        var events=new GameplayEvents();
        for(var type:types) {
            var mob=h.spawn(type,new net.minecraft.core.BlockPos(4,2,4));mob.getRandom().setSeed(305);var reference=net.minecraft.util.RandomSource.create(305);int total=0;
            for(int i=0;i<400;i++) {
                var drops=new ArrayList<ItemEntity>();var original=new ItemEntity(h.getLevel(),0,0,0,new ItemStack(Items.ROTTEN_FLESH));drops.add(original);boolean expected=reference.nextFloat()<.05f;
                events.creeperDrops(new LivingDropsEvent(mob,mob.damageSources().genericKill(),drops,true));
                int blood=drops.stream().filter(e -> e.getItem().is(KatanaContent.ZOMBIEBLOOD)).mapToInt(e -> e.getItem().getCount()).sum();
                h.assertTrue(blood==(expected?1:0)&&drops.contains(original)&&drops.size()==(expected?2:1),"Exactly one Zombie Blood at five-percent threshold, preserving other loot: "+type);total+=blood;
            }
            h.assertTrue(total>0&&total<50,"Zombie blood remains rare");mob.discard();
        }
        for(var type:List.of(EntityTypes.COW,EntityTypes.SKELETON,EntityTypes.PHANTOM)) {
            var mob=h.spawn(type,new net.minecraft.core.BlockPos(4,2,4));var drops=new ArrayList<ItemEntity>();events.creeperDrops(new LivingDropsEvent(mob,mob.damageSources().genericKill(),drops,true));h.assertTrue(drops.isEmpty(),"Non-zombies cannot drop Zombie Blood");mob.discard();
        }
        var rules=h.getLevel().getGameRules();var rule=net.minecraft.world.level.gamerules.GameRules.MOB_DROPS;boolean before=rules.get(rule);
        try {
            rules.set(rule,false,h.getLevel().getServer());var mob=h.spawn(EntityTypes.ZOMBIE,new net.minecraft.core.BlockPos(4,2,4));
            for(int i=0;i<100;i++){var drops=new ArrayList<ItemEntity>();events.creeperDrops(new LivingDropsEvent(mob,mob.damageSources().genericKill(),drops,true));h.assertTrue(drops.isEmpty(),"Disabled mob drops suppress blood");}mob.discard();
        } finally { rules.set(rule,before,h.getLevel().getServer()); }
        h.succeed();
    }
}
