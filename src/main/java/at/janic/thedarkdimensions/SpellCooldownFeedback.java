package at.janic.thedarkdimensions;

/** Observe native cooldown transitions. Joining with an already-ready book must stay silent. */
public final class SpellCooldownFeedback {
    private boolean cooling;
    public boolean tick(boolean usable,int remaining){if(!usable){cooling=false;return false;}boolean ready=cooling&&remaining==0;cooling=remaining>0;return ready;}
    public void reset(){cooling=false;}
}
