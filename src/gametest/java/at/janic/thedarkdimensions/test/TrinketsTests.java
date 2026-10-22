package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;

final class TrinketsTests {
    static void books(GameTestHelper h){
        if(!net.neoforged.fml.ModList.get().isLoaded("trinkets_updated")){h.succeed();return;}
        var p=h.makeMockPlayer(GameType.SURVIVAL);
        var inventory=TrinketsApi.getAttachment(p).getInventory(TrinketsMagic.BOOK_SLOT);
        h.assertTrue(inventory!=null&&inventory.getContainerSize()==1,"Player receives one spellbook slot");
        var slot=inventory.getOrCreateSlotAccess(0);
        h.assertTrue(inventory.slotType().validatorCheck(new ItemStack(SpellContent.WATER_BOOK.get()),slot,p)&&!inventory.slotType().validatorCheck(new ItemStack(Items.DIRT),slot,p),"Tags admit books and reject ordinary items");
        TrinketsMagic.set(p,new ItemStack(SpellContent.WATER_BOOK.get()));
        h.assertTrue(MagicBooks.active(p)==3,"Trinkets book activates without a held book");
        p.setHealth(10);h.assertTrue(MagicCasting.cast(p,3)&&p.getHealth()==16,"Trinkets book casts water healing");
        h.assertTrue(MagicData.read(TrinketsMagic.get(p)).uses[3]==1,"Casting writes skill progress to actual Trinkets item");
        var menu=new SpellbookMenu(1,p.getInventory(),3);
        h.assertTrue(menu.stillValid(p)&&menu.clickMenuButton(p,1000+3*5+4)&&menu.clickMenuButton(p,204),"Skill tree assigns and selects fifth slot");
        h.assertTrue(MagicData.read(TrinketsMagic.get(p)).slots[4]==3&&MagicData.read(TrinketsMagic.get(p)).selected==4,"Wheel selection persists in equipped book");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpellContent.FIRE_BOOK.get()));h.assertTrue(MagicBooks.active(p)==0,"Held book takes precedence");
        TrinketsMagic.set(p,ItemStack.EMPTY);h.assertTrue(!menu.stillValid(p),"Removing equipped book invalidates its menu");h.succeed();
    }
    static void accessories(GameTestHelper h){
        if(!net.neoforged.fml.ModList.get().isLoaded("trinkets_updated")){h.succeed();return;}
        var p=h.makeMockPlayer(GameType.SURVIVAL);var attachment=TrinketsApi.getAttachment(p);
        var focus=attachment.getInventory(TrinketsMagic.FOCUS_SLOT);var talisman=attachment.getInventory(TrinketsMagic.TALISMAN_SLOT);
        h.assertTrue(focus!=null&&talisman!=null&&focus.getContainerSize()==1&&talisman.getContainerSize()==1,"Separate focus and talisman slots available");
        h.assertTrue(focus.slotType().validatorCheck(new ItemStack(PerkContent.ASTRAL_PRISM.get()),focus.getOrCreateSlotAccess(0),p)&&!focus.slotType().validatorCheck(new ItemStack(PerkContent.SAKURA_TALISMAN.get()),focus.getOrCreateSlotAccess(0),p),"Focus tag does not admit talismans");
        focus.getOrCreateSlotAccess(0).set(new ItemStack(PerkContent.ASTRAL_PRISM.get()));talisman.getOrCreateSlotAccess(0).set(new ItemStack(PerkContent.SAKURA_TALISMAN.get()));
        h.assertTrue(PerkContent.cooldown(p,100)==95&&PerkContent.comboWindow(p)==60,"Equipped Trinkets apply focus and combo perks");
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PerkContent.ASTRAL_PRISM.get()));h.assertTrue(PerkContent.cooldown(p,100)==95,"Duplicate focus never stacks");
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);focus.getOrCreateSlotAccess(0).set(ItemStack.EMPTY);talisman.getOrCreateSlotAccess(0).set(ItemStack.EMPTY);
        h.assertTrue(PerkContent.cooldown(p,100)==100&&PerkContent.comboWindow(p)==40,"Removing accessories removes their perks");h.succeed();
    }
}
