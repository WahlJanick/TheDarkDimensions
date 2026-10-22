package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Item-bound progress. All incoming and saved indices are checked before use. */
public final class MagicData {
    public int elements, selected;
    public long learned;
    public final int[] slots={-1,-1,-1,-1,-1}, uses=new int[MagicSkill.COUNT], masteries=new int[MagicSkill.COUNT];
    public String identity=UUID.randomUUID().toString();
    public static MagicData read(ItemStack book){
        var d=new MagicData(); var t=book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(!t.contains("magic_elements")){
            if(book.getItem() instanceof SpellBookItem b&&b.element()!=null){d.elements=1<<b.element().ordinal();d.learned=1<<(b.element().ordinal()*3);d.slots[0]=b.element().ordinal()*3;}
            return d;
        }
        d.elements=t.getIntOr("magic_elements",0)&255;
        d.learned=t.getLongOr("magic_learned_v2",Integer.toUnsignedLong(t.getIntOr("magic_learned",0))&0xffffffL)&((1L<<MagicSkill.COUNT)-1);
        d.selected=Math.clamp(t.getIntOr("magic_selected",0),0,4);
        d.identity=t.getStringOr("magic_book_id",d.identity);
        for(int i=0;i<MagicSkill.COUNT;i++){
            if((d.elements&(1<<MagicSkill.values()[i].element().ordinal()))==0)d.learned&=~(1L<<i);
            d.uses[i]=Math.clamp(t.getIntOr("magic_use_"+i,0),0,70);
            int choice=t.getIntOr("magic_mastery_"+i,0);d.masteries[i]=d.level(i)==5&&choice>=1&&choice<=2?choice:0;
        }
        for(int i=0;i<5;i++){int s=t.getIntOr("magic_slot_"+i,-1);if(d.knows(s))d.slots[i]=s;}
        return d;
    }
    public boolean knows(int id){return id>=0&&id<MagicSkill.COUNT&&(learned&(1L<<id))!=0;}
    public int level(int id){if(!knows(id))return 0;int u=uses[id];return u>=70?5:u>=40?4:u>=20?3:u>=8?2:1;}
    public int mastery(MagicSkill skill){return level(skill.ordinal())==5?Math.clamp(masteries[skill.ordinal()],0,2):0;}
    public boolean canLearn(int id){if(id<0||id>=MagicSkill.COUNT||knows(id))return false;var skill=MagicSkill.values()[id];return (elements&(1<<skill.element().ordinal()))!=0&&skill.requirements().stream().allMatch(r->level(r.skill().ordinal())>=r.level());}
    public void save(ItemStack book){CustomData.update(DataComponents.CUSTOM_DATA,book,t->{
        t.putInt("magic_elements",elements);t.putInt("magic_learned",(int)(learned&0xffffff));t.putLong("magic_learned_v2",learned);t.putInt("magic_selected",selected);t.putString("magic_book_id",identity);
        for(int i=0;i<5;i++)t.putInt("magic_slot_"+i,slots[i]);
        for(int i=0;i<MagicSkill.COUNT;i++){t.putInt("magic_use_"+i,uses[i]);t.putInt("magic_mastery_"+i,mastery(MagicSkill.values()[i]));}
    });}
}
