package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

/** Result is a preview; only taking it commits either transaction. */
public final class RunicBenchMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;private final Player owner;private boolean updating;
    public final Container input=new SimpleContainer(2){@Override public void setChanged(){super.setChanged();RunicBenchMenu.this.slotsChanged(this);}};
    public final Container result=new SimpleContainer(1);
    public final ContainerData progress=new SimpleContainerData(1);
    public RunicBenchMenu(int id,Inventory inventory){this(id,inventory,ContainerLevelAccess.NULL);}
    public RunicBenchMenu(int id,Inventory inventory,ContainerLevelAccess access){
        super(ResonanceContent.BENCH_MENU.get(),id);this.owner=inventory.player;this.access=access;
        addSlot(new Slot(input,0,49,19){@Override public boolean mayPlace(ItemStack s){return PerkContent.isWeapon(s);}});
        addSlot(new Slot(input,1,49,40){@Override public boolean mayPlace(ItemStack s){return AncientRunes.ingredient(s)>=0||s.is(MineralContent.CHRONUS_ORB);}});
        addSlot(new Slot(result,0,129,34){
            @Override public boolean mayPlace(ItemStack s){return false;}
            @Override public boolean mayPickup(Player p){return p==owner&&stillValid(p)&&!preview().isEmpty()&&ItemStack.isSameItemSameComponents(preview(),getItem());}
            @Override public void onTake(Player p,ItemStack stack){
                int removed=input.getItem(1).is(MineralContent.CHRONUS_ORB)?AncientRunes.rune(input.getItem(0)):-1;
                updating=true;input.removeItem(0,1);input.removeItem(1,1);result.setItem(0,ItemStack.EMPTY);updating=false;
                if(removed>=0&&!p.level().isClientSide()){var rune=new ItemStack(ResonanceContent.RUNES.get(removed).get());if(!p.getInventory().add(rune))p.drop(rune,false,net.minecraft.util.Prediction.SERVER_ONLY);}
                slotsChanged(input);super.onTake(p,stack);
            }
        });
        addStandardInventorySlots(inventory,8,84);addDataSlots(progress);
    }
    public ItemStack preview(){
        var weapon=input.getItem(0);var addition=input.getItem(1);if(!PerkContent.isWeapon(weapon))return ItemStack.EMPTY;
        if(addition.is(MineralContent.CHRONUS_ORB))return AncientRunes.unbind(weapon);
        int rune=AncientRunes.ingredient(addition);if(rune<0)return ItemStack.EMPTY;
        int mask=owner instanceof ServerPlayer p?ResonanceProgress.data(p).completed:progress.get(0);
        if(rune==3?mask!=7:(mask&(1<<rune))==0)return ItemStack.EMPTY;
        return AncientRunes.bind(weapon,rune);
    }
    @Override public void slotsChanged(Container c){if(updating||owner==null||owner.level().isClientSide())return;result.setItem(0,preview());broadcastChanges();}
    @Override public void broadcastChanges(){if(owner instanceof ServerPlayer p)progress.set(0,ResonanceProgress.data(p).completed);super.broadcastChanges();}
    @Override public boolean stillValid(Player p){return p.isAlive()&&stillValid(access,p,ResonanceContent.BENCH.get());}
    @Override public ItemStack quickMoveStack(Player p,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;var slot=slots.get(index);if(!slot.hasItem()||!slot.mayPickup(p))return ItemStack.EMPTY;var stack=slot.getItem();var before=stack.copy();boolean moved;
        if(index<3)moved=moveItemStackTo(stack,3,39,true);
        else if(PerkContent.isWeapon(stack))moved=moveItemStackTo(stack,0,1,false);
        else if(AncientRunes.ingredient(stack)>=0||stack.is(MineralContent.CHRONUS_ORB))moved=moveItemStackTo(stack,1,2,false);
        else moved=index<30?moveItemStackTo(stack,30,39,false):moveItemStackTo(stack,3,30,false);
        if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(p,before);return before;
    }
    @Override public void removed(Player p){super.removed(p);if(!p.level().isClientSide()){updating=true;result.clearContent();clearContainer(p,input);updating=false;}}
}
