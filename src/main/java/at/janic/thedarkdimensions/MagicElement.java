package at.janic.thedarkdimensions;

import net.minecraft.world.item.ItemStack;

public enum MagicElement {
    FIRE("Feuer",0xffef8249), WATER("Wasser",0xff63bcff), LIGHTNING("Blitz",0xffffe36b),
    CHRONUS("Chronus",0xffb09be2), BLOOD("Blut",0xffd36078), SOUL("Seele",0xff65d6ce),
    ENDER("Ender",0xffcb88ef), ELEMENTAL("Elemental",0xffd9e3ed);
    public final String label; public final int color;
    MagicElement(String label,int color){this.label=label;this.color=color;}
    public static MagicElement core(ItemStack s){
        if(s.is(TheDarkDimensions.FIRE_CORE))return FIRE;
        if(s.is(CaveContent.WATER_CORE))return WATER;
        if(s.is(CaveContent.LIGHTNING_CORE))return LIGHTNING;
        if(s.is(MagicContent.CHRONUS_CORE))return CHRONUS;
        if(s.is(KatanaContent.BLOOD_CORE))return BLOOD;
        if(s.is(KatanaContent.SOUL_CORE))return SOUL;
        if(s.is(MagicContent.ENDER_CORE))return ENDER;
        if(s.is(CaveContent.SPELL_CORE))return ELEMENTAL;
        return null;
    }
}
