package at.janic.thedarkdimensions;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
public final class SpellBookItem extends Item {
    public enum Spell { FIRE,LIGHTNING,ENDER_CHEST,WATER }
    private final MagicElement element;
    public SpellBookItem(Properties p,Spell s){this(p,switch(s){case FIRE->MagicElement.FIRE;case WATER->MagicElement.WATER;case LIGHTNING->MagicElement.LIGHTNING;case ENDER_CHEST->MagicElement.ENDER;});}
    public SpellBookItem(Properties p,MagicElement e){super(p);element=e;}
    public MagicElement element(){return element;}
    public Spell spell(){return element==MagicElement.FIRE?Spell.FIRE:element==MagicElement.WATER?Spell.WATER:element==MagicElement.LIGHTNING?Spell.LIGHTNING:Spell.ENDER_CHEST;}
    @Override public InteractionResult use(Level l,Player p,InteractionHand hand){return MagicCasting.cast(p,hand==InteractionHand.MAIN_HAND?0:1)?InteractionResult.SUCCESS:InteractionResult.FAIL;}
}
