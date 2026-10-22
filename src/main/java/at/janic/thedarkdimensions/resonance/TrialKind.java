package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

public enum TrialKind implements StringRepresentable {
    EMBER("cinder_crucible","fire_cave",0xe98d39),
    TIDE("sunken_reliquary","water_cave",0x63bdd7),
    RADIANCE("luminous_hollow","glowing_cave",0xeedb83);
    public final String structure,biome;public final int color;
    TrialKind(String structure,String biome,int color){this.structure=structure;this.biome=biome;this.color=color;}
    @Override public String getSerializedName(){return name().toLowerCase(java.util.Locale.ROOT);}
    public Item core(){return switch(this){case EMBER->TheDarkDimensions.FIRE_CORE.get();case TIDE->CaveContent.WATER_CORE.get();case RADIANCE->CaveContent.LIGHTNING_CORE.get();};}
    public Item sigil(){return ResonanceContent.SIGILS.get(ordinal()).get();}
    public String title(){return "resonance.thedarkdimensions."+structure;}
    public int mask(){return 1<<ordinal();}
}
