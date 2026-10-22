package at.janic.thedarkdimensions;

import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Referenced only when Trinkets Updated is installed; no mandatory accessory dependency. */
public final class TrinketsMagic {
    public static final String BOOK_SLOT="chest/dark_spellbook", FOCUS_SLOT="chest/dark_focus", TALISMAN_SLOT="chest/dark_talisman";
    public static final String FEET_SLOT="feet/dark_windstep",BACK_SLOT="chest/dark_back_tool",GLIDER_SLOT="chest/dark_glider";
    public static ItemStack getSlot(Player p,String id,boolean effects){var inventory=TrinketsApi.getAttachment(p).getInventory(id);if(inventory==null||!inventory.isValidSlot(0))return ItemStack.EMPTY;var slot=inventory.getSlotAccess(0);return slot!=null&&(!effects||slot.canApplyEffects())?slot.get():ItemStack.EMPTY;}
    private static eu.pb4.trinkets.api.TrinketSlotAccess access(Player p,String id){var inv=TrinketsApi.getAttachment(p).getInventory(id);return inv!=null&&inv.isValidSlot(0)?inv.getOrCreateSlotAccess(0):null;}
    public static boolean setSlot(Player p,String id,ItemStack item){var slot=access(p,id);return slot!=null&&slot.set(item);}
    public static void hurtSlot(net.minecraft.server.level.ServerPlayer p,String id,int amount){var slot=access(p,id);if(slot!=null&&!slot.get().isEmpty())TrinketsApi.hurtAndBreakItemStack(slot.get(),amount,p,slot);}
    public static void registerPredicates(){TrinketsApi.registerTrinketPredicate(TheDarkDimensions.id("back_tool"),(stack,slot,entity)->BackEquipment.tool(stack));TrinketsApi.registerTrinketPredicate(TheDarkDimensions.id("back_glider"),(stack,slot,entity)->stack.has(net.minecraft.core.component.DataComponents.GLIDER));}
    public static boolean swapBack(net.minecraft.server.level.ServerPlayer p){
        var slot=access(p,BACK_SLOT);if(slot==null)return false;
        var hand=p.getMainHandItem();var back=slot.get();
        if(hand.isEmpty()&&back.isEmpty()||!hand.isEmpty()&&(!BackEquipment.tool(hand)||hand.getCount()!=1)||!back.isEmpty()&&!BackEquipment.tool(back))return false;
        if(!back.isEmpty()&&!eu.pb4.trinkets.api.TrinketSlotUtils.mayPickup(slot,back)||!hand.isEmpty()&&!eu.pb4.trinkets.api.TrinketSlotUtils.mayPlace(slot,hand))return false;
        if(!slot.set(hand.copy()))return false;p.stopUsingItem();p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,back.copy());p.inventoryMenu.broadcastChanges();return true;
    }
    public static ItemStack get(Player p){
        var inventory=TrinketsApi.getAttachment(p).getInventory(BOOK_SLOT);
        if(inventory==null||!inventory.isValidSlot(0))return ItemStack.EMPTY;
        var slot=inventory.getSlotAccess(0);
        return slot!=null&&slot.canApplyEffects()?slot.get():ItemStack.EMPTY;
    }
    public static void set(Player p,ItemStack book){
        var inventory=TrinketsApi.getAttachment(p).getInventory(BOOK_SLOT);
        if(inventory!=null&&inventory.isValidSlot(0))inventory.getOrCreateSlotAccess(0).set(book);
    }
    public static boolean equipped(Player p,Item item){
        return TrinketsApi.getAttachment(p).equipped(s->s.is(item),false).stream().anyMatch(slot->slot.canApplyEffects());
    }
}
