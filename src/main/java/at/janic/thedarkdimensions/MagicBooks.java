package at.janic.thedarkdimensions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class MagicBooks {
    public static ItemStack get(Player p,int source){return switch(source){case 0->p.getMainHandItem();case 1->p.getInventory().getItem(net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND);case 2->net.neoforged.fml.ModList.get().isLoaded("curios")?CuriosMagic.get(p):ItemStack.EMPTY;case 3->net.neoforged.fml.ModList.get().isLoaded("trinkets_updated")?TrinketsMagic.get(p):ItemStack.EMPTY;default->ItemStack.EMPTY;};}
    public static int active(Player p){for(int i=0;i<4;i++)if(get(p,i).getItem() instanceof SpellBookItem)return i;return -1;}
    public static void set(Player p,int source,ItemStack book){if(source==0)p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);else if(source==1)p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,book);else if(source==2&&net.neoforged.fml.ModList.get().isLoaded("curios"))CuriosMagic.set(p,book);else if(source==3&&net.neoforged.fml.ModList.get().isLoaded("trinkets_updated"))TrinketsMagic.set(p,book);}
}
