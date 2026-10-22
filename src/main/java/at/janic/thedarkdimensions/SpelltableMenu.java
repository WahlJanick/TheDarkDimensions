package at.janic.thedarkdimensions;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** Server-authoritative spell selection: one book and one elemental core. */
public final class SpelltableMenu extends AbstractContainerMenu {
    public static final int WIDTH=176,HEIGHT=184;
    private final ContainerLevelAccess access;
    public final Container ingredients=new SimpleContainer(2);
    public final Container result=new SimpleContainer(1);
    public SpelltableMenu(int id,Inventory inventory) { this(id,inventory,ContainerLevelAccess.NULL); }
    public SpelltableMenu(int id,Inventory inventory,ContainerLevelAccess access) {
        super(SpellContent.SPELLTABLE_MENU.get(),id);this.access=access;
        for(int i=0;i<2;i++) {
            final int index=i;
            addSlot(new Slot(ingredients,i,27+49*i,35) {
                @Override public boolean mayPlace(ItemStack stack) { return validIngredient(index,stack); }
            });
        }
        addSlot(new Slot(result,0,134,35) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        addStandardInventorySlots(inventory,8,102);
    }
    private static boolean validIngredient(int slot,ItemStack stack) {
        return switch(slot) {case 0 -> stack.is(SpellContent.SPELL_BOOK)||stack.is(MagicContent.MASTER_BOOK);case 1 -> MagicElement.core(stack)!=null;default -> false;};
    }
    public boolean canLearn() {
        if(!result.isEmpty()||!validIngredient(0,ingredients.getItem(0))||!validIngredient(1,ingredients.getItem(1)))return false;
        MagicElement e=MagicElement.core(ingredients.getItem(1));
        return !ingredients.getItem(0).is(MagicContent.MASTER_BOOK)||(MagicData.read(ingredients.getItem(0)).elements&(1<<e.ordinal()))==0;
    }
    @Override public boolean clickMenuButton(Player player,int id) {
        if(player.level().isClientSide()||id!=0||!stillValid(player)||!canLearn())return false;
        ItemStack input=ingredients.getItem(0);MagicElement e=MagicElement.core(ingredients.getItem(1));
        ItemStack book=input.is(MagicContent.MASTER_BOOK)?input.copyWithCount(1):new ItemStack(MagicContent.book(e));
        MagicData data=input.is(MagicContent.MASTER_BOOK)?MagicData.read(input):new MagicData();data.elements|=1<<e.ordinal();data.save(book);
        ingredients.removeItem(0,1);ingredients.removeItem(1,1);result.setItem(0,book);
        ingredients.setChanged();result.setChanged();broadcastChanges();return true;
    }
    @Override public boolean stillValid(Player player) { return stillValid(access,player,MineralContent.SPELLTABLE.get()); }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack stack=slot.getItem(),before=stack.copy();
        boolean moved;
        if(index<3)moved=moveItemStackTo(stack,3,39,true);
        else if(validIngredient(0,stack))moved=moveItemStackTo(stack,0,1,false);
        else if(validIngredient(1,stack))moved=moveItemStackTo(stack,1,2,false);
        
        else if(index<30)moved=moveItemStackTo(stack,30,39,false);
        else moved=moveItemStackTo(stack,3,30,false);
        if(!moved)return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        if(stack.getCount()==before.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack);return before;
    }
    @Override public void removed(Player player) {
        super.removed(player);
        if(!player.level().isClientSide()) { clearContainer(player,ingredients);clearContainer(player,result); }
    }
}
