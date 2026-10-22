package at.janic.thedarkdimensions;

public enum MagicSkill {
    FIRE_BOLT("Feuerstoß"), FIRE_RING("Flammenring"), FIRE_GUARD("Feuerschutz"),
    WATER_HEAL("Wasserheilung"), WATER_WAVE("Gezeitenstoß"), WATER_GUARD("Wasserschutz"),
    LIGHTNING_STRIKE("Blitzschlag"), CHAIN_LIGHTNING("Kettenblitz"), LIGHTNING_STEP("Blitzschritt"),
    CHRONUS_DASH("Chronus-Dash"), TIME_SLOW("Zeitfeld"), REWIND("Rücksprung"),
    BLOOD_CUT("Blutschnitt"), BLOOD_SIPHON("Blutentzug"), BLOOD_GUARD("Blutpanzer"),
    SOUL_BOLT("Seelenstoß"), SOUL_FEAR("Seelenfurcht"), SOUL_SIGHT("Seelensicht"),
    ENDER_CHEST("Portable Enderchest"), ENDER_BLINK("Rissschritt"), ENDER_WARD("Enderschutz"),
    ELEMENTAL_BURST("Elementstoß"), ELEMENTAL_BARRIER("Elementbarriere"), ELEMENTAL_SURGE("Elementflut"),
    EMBER_MARK("Glutzeichen",MagicElement.FIRE,FIRE_BOLT,2,1,0,100),
    ASH_STEP("Aschenschritt",MagicElement.FIRE,FIRE_BOLT,2,1,2,100),
    INFERNO("Inferno",MagicElement.FIRE,EMBER_MARK,4,2,0,300),
    FROST_BIND("Frostfessel",MagicElement.WATER,WATER_WAVE,3,2,0,140),
    WATER_VEIL("Wasserschleier",MagicElement.WATER,WATER_HEAL,2,1,2,160),
    HEALING_SPRING("Heilquelle",MagicElement.WATER,WATER_VEIL,4,2,2,320),
    STATIC_CHARGE("Statische Ladung",MagicElement.LIGHTNING,LIGHTNING_STRIKE,2,1,0,100),
    THUNDER_LEAP("Donnersprung",MagicElement.LIGHTNING,LIGHTNING_STEP,3,2,2,140),
    STORM_FIELD("Gewitterfeld",MagicElement.LIGHTNING,STATIC_CHARGE,4,2,0,320),
    TIME_ANCHOR("Zeitanker",MagicElement.CHRONUS,REWIND,3,2,0,180),
    AFTERIMAGE("Nachbild",MagicElement.CHRONUS,CHRONUS_DASH,2,1,2,140),
    INERTIA_FIELD("Trägheitsfeld",MagicElement.CHRONUS,TIME_SLOW,3,2,1,240),
    STOLEN_MOMENT("Gestohlener Moment",MagicElement.CHRONUS,AFTERIMAGE,4,2,2,240),
    VOID_PULL("Leerenzug",MagicElement.ENDER,ENDER_BLINK,3,2,0,140),
    DIMENSION_FOLD("Dimensionsfalte",MagicElement.ENDER,ENDER_WARD,4,2,2,400);
    // Append skills; indices 0–23 are already stored in existing worlds and books.
    public static final int COUNT=values().length;
    public final String label;
    private final MagicElement element;
    private final MagicSkill parent;
    private final int required,column,row,cooldown;
    MagicSkill(String label){this(label,null,null,0,0,0,0);}
    MagicSkill(String label,MagicElement element,MagicSkill parent,int required,int column,int row,int cooldown){this.label=label;this.element=element;this.parent=parent;this.required=required;this.column=column;this.row=row;this.cooldown=cooldown;}
    public MagicElement element(){return element!=null?element:MagicElement.values()[ordinal()/3];}
    public MagicSkill parent(){
        return switch(this){
            case FIRE_RING->ASH_STEP;case FIRE_GUARD->FIRE_RING;
            case WATER_WAVE->WATER_HEAL;case WATER_GUARD->WATER_VEIL;case HEALING_SPRING->WATER_GUARD;
            case CHAIN_LIGHTNING->STATIC_CHARGE;case LIGHTNING_STEP->LIGHTNING_STRIKE;case STORM_FIELD->CHAIN_LIGHTNING;
            case TIME_SLOW,REWIND->CHRONUS_DASH;
            case AFTERIMAGE->TIME_SLOW;case ENDER_BLINK->ENDER_CHEST;case ENDER_WARD->ENDER_BLINK;
            default->ordinal()>=24?parent:ordinal()%3==0?null:values()[ordinal()-1];
        };
    }
    public int requiredLevel(){return switch(this){case FIRE_GUARD,CHAIN_LIGHTNING,ENDER_WARD,AFTERIMAGE->3;case HEALING_SPRING,STORM_FIELD->4;default->ordinal()>=24?required:switch(this){case FIRE_RING,WATER_GUARD->3;case LIGHTNING_STEP,TIME_SLOW,REWIND,ENDER_BLINK->2;default->ordinal()%3+1;};};}
    public record Requirement(MagicSkill skill,int level){}
    public java.util.List<Requirement> requirements(){if(parent()==null)return java.util.List.of();var primary=new Requirement(parent(),requiredLevel());var second=switch(this){case INFERNO->new Requirement(FIRE_RING,2);case HEALING_SPRING->new Requirement(FROST_BIND,2);case STORM_FIELD->new Requirement(THUNDER_LEAP,2);case STOLEN_MOMENT->new Requirement(TIME_ANCHOR,2);case DIMENSION_FOLD->new Requirement(VOID_PULL,2);default->null;};return second==null?java.util.List.of(primary):java.util.List.of(primary,second);}
    public int column(){return parent()==null?0:parent().column()+1;}
    public int row(){return switch(this){case EMBER_MARK,INFERNO,WATER_WAVE,FROST_BIND,STATIC_CHARGE,CHAIN_LIGHTNING,STORM_FIELD,REWIND,TIME_ANCHOR,VOID_PULL->0;case FIRE_GUARD,HEALING_SPRING,THUNDER_LEAP,STOLEN_MOMENT,DIMENSION_FOLD->2;case AFTERIMAGE->2;case INERTIA_FIELD->1;default->1;};}
    // Layout depth must not silently change established damage/FX/cooldown balance.
    public int tier(){if(ordinal()>=24)return column==0?1:column;return switch(this){case FIRE_RING,WATER_GUARD->2;case FIRE_GUARD,REWIND,LIGHTNING_STEP,ENDER_WARD->1;default->ordinal()%3;};}
    public int cooldown(){if(cooldown>0)return cooldown;return switch(this){case FIRE_BOLT->40;case WATER_HEAL->160;case LIGHTNING_STRIKE->100;case ENDER_CHEST->60;case REWIND->240;default->120+tier()*40;};}
}
