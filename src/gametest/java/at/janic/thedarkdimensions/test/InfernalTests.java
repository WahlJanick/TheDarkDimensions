package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

final class InfernalTests {
    private static Player player(GameTestHelper h,boolean enchanted) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        for(var entry:BloodNetheriteContent.ARMOR_ITEMS.entrySet()) {
            var stack=new ItemStack(entry.getValue().get());
            if(enchanted)stack.enchant(h.getLevel().registryAccess().getOrThrow(DevilArmor.ENCHANTMENT),1);
            player.setItemSlot(entry.getKey().getSlot(),stack);
        }
        return player;
    }
    static void recipes(GameTestHelper h) {
        var blood=new ItemStack(KatanaContent.CREEPERBLOOD.get());
        for(var center:List.of(Items.GHAST_TEAR,Items.ENDER_EYE)) {
            var grid=new ArrayList<>(Collections.nCopies(9,blood));grid.set(4,new ItemStack(center));var input=CraftingInput.of(3,3,grid);
            var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
            h.assertTrue(found.isPresent(),"Eight Creeperblood around central ingredient crafts devil material");var out=found.get().value().assemble(input);
            h.assertTrue(out.is(center==Items.GHAST_TEAR?DevilContent.TEAR:DevilContent.EYE)&&out.getCount()==1,"Correct single Tear/Eye output");
        }
        var enchantment=h.getLevel().registryAccess().getOrThrow(DevilArmor.ENCHANTMENT);
        for(var item:BloodNetheriteContent.ARMOR_ITEMS.values())h.assertTrue(enchantment.value().isSupportedItem(new ItemStack(item.get())),"All four Blood armor pieces support enchantment");
        h.assertTrue(!enchantment.value().isSupportedItem(new ItemStack(Items.NETHERITE_CHESTPLATE))&&!enchantment.value().isSupportedItem(new ItemStack(DevilContent.TRIDENT.get())),"Enchantment restricted to Blood armor");
        h.assertTrue(enchantment.is(EnchantmentTags.IN_ENCHANTING_TABLE)&&enchantment.is(EnchantmentTags.TRADEABLE)&&enchantment.is(EnchantmentTags.ON_RANDOM_LOOT),"Enchantment obtainable through table, trading and loot");h.succeed();
    }
    static void protection(GameTestHelper h) {
        var wearer=player(h,true);h.assertTrue(DevilArmor.complete(wearer)&&wearer.fireImmune(),"Fully enchanted Blood armor grants fire immunity");float health=wearer.getHealth();
        for(var source:List.of(h.getLevel().damageSources().inFire(),h.getLevel().damageSources().onFire(),h.getLevel().damageSources().lava(),h.getLevel().damageSources().hotFloor()))wearer.hurtServer(h.getLevel(),source,10);
        h.assertTrue(wearer.getHealth()==health,"Fire, burning, lava and magma cannot hurt the complete wearer");
        wearer.hurtServer(h.getLevel(),h.getLevel().damageSources().generic(),4);h.assertTrue(wearer.getHealth()<health,"Non-fire damage still applies");
        var incomplete=player(h,true);incomplete.setItemSlot(EquipmentSlot.HEAD,new ItemStack(BloodNetheriteContent.ARMOR_ITEMS.get(ArmorType.HELMET).get()));
        h.assertTrue(!DevilArmor.complete(incomplete)&&!incomplete.fireImmune(),"One unenchanted piece disables immunity");incomplete.hurtServer(h.getLevel(),h.getLevel().damageSources().lava(),4);h.assertTrue(incomplete.getHealth()<20,"Incomplete armor still takes lava damage");
        var vanilla=new ItemStack(Items.NETHERITE_HELMET);vanilla.enchant(h.getLevel().registryAccess().getOrThrow(DevilArmor.ENCHANTMENT),1);wearer.setItemSlot(EquipmentSlot.HEAD,vanilla);
        h.assertTrue(!DevilArmor.complete(wearer),"Forced enchantment on vanilla armor cannot activate Blood set bonus");h.succeed();
    }
    private static void lava(GameTestHelper h,Player player) {
        for(int x=2;x<=7;x++)for(int z=2;z<=7;z++)for(int y=1;y<=5;y++)h.setBlock(x,y,z,Blocks.LAVA);
        player.setPos(h.absoluteVec(new Vec3(4.5,2,4.5)));player.setNoGravity(true);player.tick();
        h.assertTrue(player.isInLava(),"Player is actually touching lava");
    }
    static void swimming(GameTestHelper h) {
        var protectedPlayer=player(h,true);lava(h,protectedPlayer);protectedPlayer.setSprinting(true);protectedPlayer.updateSwimming();
        h.assertTrue(protectedPlayer.isSwimming(),"Full enchanted set enables submerged sprint swimming pose");
        protectedPlayer.setDeltaMovement(0,0,0.2);protectedPlayer.travel(Vec3.ZERO);double protectedSpeed=protectedPlayer.getDeltaMovement().z;
        var ordinary=player(h,false);lava(h,ordinary);ordinary.setSprinting(true);ordinary.updateSwimming();h.assertTrue(!ordinary.isSwimming(),"Unenchanted Blood armor retains normal lava movement");
        ordinary.setDeltaMovement(0,0,0.2);ordinary.travel(Vec3.ZERO);
        h.assertTrue(protectedSpeed>ordinary.getDeltaMovement().z+0.03,"Enchanted set uses water-like drag rather than slow lava drag");
        protectedPlayer.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);protectedPlayer.updateSwimming();h.assertTrue(!protectedPlayer.isSwimming(),"Removing a piece ends lava swimming");h.succeed();
    }
    static void riptide(GameTestHelper h) {
        var wearer=player(h,false);lava(h,wearer);var trident=new ItemStack(DevilContent.TRIDENT.get());trident.enchant(h.getLevel().registryAccess().getOrThrow(Enchantments.RIPTIDE),3);wearer.setItemInHand(InteractionHand.MAIN_HAND,trident);
        h.assertTrue(DevilContent.TRIDENT.get().use(h.getLevel(),wearer,InteractionHand.MAIN_HAND).consumesAction(),"Lava permits charging Devil Riptide");
        h.assertTrue(DevilContent.TRIDENT.get().releaseUsing(trident,h.getLevel(),wearer,71990)&&wearer.isAutoSpinAttack()&&wearer.getDeltaMovement().length()>1,"Lava Riptide actually launches the wearer without armor enchantment");
        var normal=player(h,false);lava(h,normal);var ordinary=new ItemStack(Items.TRIDENT);ordinary.enchant(h.getLevel().registryAccess().getOrThrow(Enchantments.RIPTIDE),3);normal.setItemInHand(InteractionHand.MAIN_HAND,ordinary);
        h.assertTrue(!Items.TRIDENT.use(h.getLevel(),normal,InteractionHand.MAIN_HAND).consumesAction()&&!Items.TRIDENT.releaseUsing(ordinary,h.getLevel(),normal,71990),"Vanilla Riptide does not gain lava support");
        var dry=player(h,false);dry.setPos(h.absoluteVec(new Vec3(9,8,9)));dry.tick();dry.setItemInHand(InteractionHand.MAIN_HAND,trident.copy());
        h.assertTrue(!DevilContent.TRIDENT.get().use(h.getLevel(),dry,InteractionHand.MAIN_HAND).consumesAction(),"Dry air still cannot trigger Devil Riptide");h.succeed();
    }
}
