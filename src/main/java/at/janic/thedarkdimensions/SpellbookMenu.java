package at.janic.thedarkdimensions;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class SpellbookMenu extends AbstractContainerMenu {
    public final Container display=new SimpleContainer(1);
    private final int source;
    private final String identity;
    public SpellbookMenu(int id,Inventory inv){this(id,inv,-1);}
    public SpellbookMenu(int id,Inventory inv,int source){
        super(MagicContent.BOOK_MENU.get(),id);this.source=source;
        ItemStack book=MagicBooks.get(inv.player,source);MagicData d=MagicData.read(book);identity=d.identity;
        if(source>=0){d.save(book);MagicBooks.set(inv.player,source,book);display.setItem(0,book.copy());}
        addSlot(new Slot(display,0,-10000,-10000){@Override public boolean mayPlace(ItemStack s){return false;}@Override public boolean mayPickup(Player p){return false;}});
        addStandardInventorySlots(inv,-10000,-10000);
    }
    public MagicData data(){return MagicData.read(display.getItem(0));}
    @Override public boolean stillValid(Player p){return source<0||p.isAlive()&&MagicBooks.get(p,source).getItem() instanceof SpellBookItem&&MagicData.read(MagicBooks.get(p,source)).identity.equals(identity);}
    @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}
    @Override public boolean clickMenuButton(Player p,int id){
        if(p.level().isClientSide()||source<0||!stillValid(p))return false;
        ItemStack book=MagicBooks.get(p,source).copy();MagicData d=MagicData.read(book);
        if(id>=100&&id<100+MagicSkill.COUNT){int skill=id-100;boolean alreadyKnown=d.knows(skill);
            if(p.isCreative()){
                if(d.knows(skill)&&d.level(skill)>=5)return false;
                d.elements|=1<<MagicSkill.values()[skill].element().ordinal();
                if(d.knows(skill))d.uses[skill]=new int[]{0,8,20,40,70}[d.level(skill)];
                else d.learned|=1L<<skill;
            }else{
            if(!d.canLearn(skill))return false;
            int ingot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(SpellContent.INFUSED_INGOT)){ingot=i;break;}
            if(ingot<0)return false;p.getInventory().removeItem(ingot,1);d.learned|=1L<<skill;
            }
            if(!alreadyKnown)for(int i=0;i<5;i++)if(d.slots[i]<0){d.slots[i]=skill;break;}
        }else if(id>=2000&&id<2000+MagicSkill.COUNT*2){int skill=(id-2000)/2,choice=(id-2000)%2+1;
            if(d.level(skill)!=5||d.masteries[skill]==choice)return false;
            if(d.masteries[skill]!=0&&!p.isCreative()){int ingot=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(SpellContent.INFUSED_INGOT)){ingot=i;break;}if(ingot<0)return false;p.getInventory().removeItem(ingot,1);}
            d.masteries[skill]=choice;
        }else if(id>=200&&id<205)d.selected=id-200;
        else if(id>=1000&&id<1000+MagicSkill.COUNT*5){int skill=(id-1000)/5,slot=(id-1000)%5;if(!d.knows(skill))return false;d.slots[slot]=skill;}
        else return false;
        d.save(book);MagicBooks.set(p,source,book);display.setItem(0,book.copy());broadcastChanges();return true;
    }
}
