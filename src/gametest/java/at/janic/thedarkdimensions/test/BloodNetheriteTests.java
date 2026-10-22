package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;

final class BloodNetheriteTests {
    private static Item vanilla(String name) {return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(name));}
    private static void craft(GameTestHelper h,List<ItemStack> stacks,int width,int height,Item expected,int count) {
        var input=CraftingInput.of(width,height,stacks);var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(found.isPresent(),"Recipe exists for "+expected);var result=found.get().value().assemble(input);
        h.assertTrue(result.is(expected)&&result.getCount()==count,"Exact ingredient and output counts for "+expected);
    }
    static void recipes(GameTestHelper h) {
        var blood=new ItemStack(KatanaContent.CREEPERBLOOD.get());var ingot=new ItemStack(Items.NETHERITE_INGOT);
        craft(h,List.of(blood,blood,blood,blood,ingot,ingot),3,2,BloodNetheriteContent.INGOT.get(),1);
        var old=CraftingInput.of(3,3,List.of(blood,blood,blood,blood,ingot,ingot,ingot,ItemStack.EMPTY,ItemStack.EMPTY));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,old,h.getLevel()).isEmpty(),"Old three-Netherite recipe no longer crafts");
        var diamond=new ItemStack(Items.DIAMOND);var template=new ItemStack(BloodNetheriteContent.TEMPLATE.get());
        craft(h,List.of(diamond,template,diamond,diamond,new ItemStack(Items.NETHER_WART),diamond,diamond,diamond,diamond),3,3,BloodNetheriteContent.TEMPLATE.get(),2);
        craft(h,Collections.nCopies(9,new ItemStack(BloodNetheriteContent.INGOT.get())),3,3,BloodNetheriteContent.BLOCK.get().asItem(),1);
        craft(h,List.of(new ItemStack(BloodNetheriteContent.BLOCK.get())),1,1,BloodNetheriteContent.INGOT.get(),9);
        craft(h,List.of(new ItemStack(SpellContent.METALINIUM_INGOT.get()),new ItemStack(MineralContent.CHRONUS_ORB.get())),2,1,SpellContent.INFUSED_INGOT.get(),1);
        h.succeed();
    }
    private static void smith(GameTestHelper h,Item base,Item expected,Item template,Item addition) {
        var stack=new ItemStack(base);stack.setDamageValue(53);stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Legacy weapon"));
        stack.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),3);
        var input=new SmithingRecipeInput(new ItemStack(template),stack,new ItemStack(addition));
        var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel());
        h.assertTrue(found.isPresent(),"Smithing route exists for "+base);var result=found.get().value().assemble(input);
        h.assertTrue(result.is(expected)&&result.getDamageValue()==53&&result.isEnchanted()&&result.get(DataComponents.CUSTOM_NAME).getString().equals("Legacy weapon"),"Upgrade retains wear, enchantment and name");
    }
    static void upgrades(GameTestHelper h) {
        for(var entry:BloodNetheriteContent.TOOLS.entrySet()) {
            var base=entry.getKey().equals("warrior_axe")?ArmoryContent.WARRIOR_AXES.get("netherite").get():vanilla("netherite_"+entry.getKey());
            smith(h,base,entry.getValue().get(),BloodNetheriteContent.TEMPLATE.get(),BloodNetheriteContent.INGOT.get());
        }
        for(var entry:BloodNetheriteContent.ARMOR_ITEMS.entrySet())smith(h,vanilla("netherite_"+entry.getKey().getName()),entry.getValue().get(),BloodNetheriteContent.TEMPLATE.get(),BloodNetheriteContent.INGOT.get());
        for(var entry:BloodNetheriteContent.IMPROVED_TOOLS.entrySet()) {
            var base=BuiltInRegistries.ITEM.getValue(TheDarkDimensions.id("perfected_netherite_"+entry.getKey()));
            smith(h,base,entry.getValue().get(),BloodNetheriteContent.TEMPLATE.get(),BloodNetheriteContent.INGOT.get());
            smith(h,BloodNetheriteContent.TOOLS.get(entry.getKey()).get(),entry.getValue().get(),ToolContent.IMPROVING_TEMPLATE.get(),TheDarkDimensions.IMPROVED_STICK.get());
        }
        h.succeed();
    }
    static void stats(GameTestHelper h) {
        for(var entry:BloodNetheriteContent.TOOLS.entrySet()) {
            var kind=entry.getKey();var normal=new ItemStack(kind.equals("warrior_axe")?ArmoryContent.WARRIOR_AXES.get("netherite").get():vanilla("netherite_"+kind));var red=new ItemStack(entry.getValue().get());
            var a=normal.get(DataComponents.ATTRIBUTE_MODIFIERS);var b=red.get(DataComponents.ATTRIBUTE_MODIFIERS);
            h.assertTrue(b.compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==a.compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)+1,"Blood tool adds one damage: "+kind);
            h.assertTrue(b.compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND)==a.compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND),"Attack tempo retained");
            if(!kind.equals("sword")) {
                var block=(kind.equals("hoe")?Blocks.HAY_BLOCK:kind.equals("shovel")?Blocks.DIRT:kind.contains("axe")&&!kind.equals("pickaxe")?Blocks.OAK_LOG:Blocks.STONE).defaultBlockState();
                float delta=red.get(DataComponents.TOOL).getMiningSpeed(block)-normal.get(DataComponents.TOOL).getMiningSpeed(block);
                h.assertTrue(Math.abs(delta-(kind.equals("warrior_axe")?1.15:1))<0.001,"Mining speed increased exactly one, retaining Warrior multiplier");
            }
            h.assertTrue(red.getMaxDamage()==normal.getMaxDamage()&&red.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(BloodNetheriteContent.INGOT.get())),"Native durability and blood ingot repairs");
        }
        for(var entry:BloodNetheriteContent.ARMOR_ITEMS.entrySet()) {
            var red=new ItemStack(entry.getValue().get());var normal=new ItemStack(vanilla("netherite_"+entry.getKey().getName()));var slot=entry.getKey().getSlot();
            h.assertTrue(red.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ARMOR,0,slot)==normal.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ARMOR,0,slot)+2,"Each armor piece adds two armor");
            h.assertTrue(Math.abs(red.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.KNOCKBACK_RESISTANCE,0,slot)-0.15)<0.001,"Armor has 0.15 knockback resistance");
            h.assertTrue(red.is(net.minecraft.tags.ItemTags.TRIMMABLE_ARMOR),"Blood armor accepts native trims");
        }
        for(var entry:BloodNetheriteContent.IMPROVED_TOOLS.values())h.assertTrue(new ItemStack(entry.get()).is(ToolContent.IMPROVED_TOOLS),"Improved Blood tools retain reach tag");
        h.succeed();
    }
    static void bastionLoot(GameTestHelper h) {
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);
        for(String kind:List.of("treasure","bridge","hoglin_stable","other")) {
            var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.withDefaultNamespace("chests/bastion_"+kind)));
            int templates=0;boolean vanillaLoot=false;var random=net.minecraft.util.RandomSource.create(263);
            for(int n=0;n<500;n++) {
                var loot=table.getRandomItems(params,random);
                for(var stack:loot)if(stack.is(BloodNetheriteContent.TEMPLATE)) {templates+=stack.getCount();h.assertTrue(stack.getCount()==1,"Bastion adds one template");} else vanillaLoot=true;
            }
            h.assertTrue(kind.equals("treasure")?templates==500:templates>20&&templates<80,"Actual Bastion template chance: "+kind+" "+templates+"/500");
            h.assertTrue(vanillaLoot,"Native Bastion loot preserved");
        }
        var unrelated=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.withDefaultNamespace("chests/end_city_treasure")));
        h.assertTrue(unrelated.getRandomItems(params,net.minecraft.util.RandomSource.create(263)).stream().noneMatch(s -> s.is(BloodNetheriteContent.TEMPLATE)),"Blood template is limited to Bastions");h.succeed();
    }
}
