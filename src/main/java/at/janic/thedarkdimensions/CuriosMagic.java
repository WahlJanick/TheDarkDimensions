package at.janic.thedarkdimensions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Loaded only when Curios is installed. */
public final class CuriosMagic {
    public static void register(){for(var entry:TheDarkDimensions.ITEMS.getEntries())if(entry.get() instanceof SpellBookItem||PerkContent.accessory(entry.get()))CuriosApi.registerCurio(entry.get(),new ICurioItem(){});}
    public static boolean equipped(Player p,net.minecraft.world.item.Item item){var inv=CuriosApi.getCuriosInventoryOrNull(p);return inv!=null&&inv.findFirstCurio(s->s.is(item)).isPresent();}
    public static ItemStack get(Player p){var inv=CuriosApi.getCuriosInventoryOrNull(p);if(inv==null)return ItemStack.EMPTY;return inv.getStacksHandler("spellbook").map(h->h.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);}
    public static void set(Player p,ItemStack s){var inv=CuriosApi.getCuriosInventoryOrNull(p);if(inv!=null)inv.getStacksHandler("spellbook").ifPresent(h->h.getStacks().setStackInSlot(0,s));}
}
