package at.janic.thedarkdimensions;

import at.janic.thedarkdimensions.resonance.*;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.*;

/** A cooperative, permanent world milestone: the three trials precede the End. */
public final class EndProgression extends SavedData {
    public int completed;
    public EndProgression(){this(0);}private EndProgression(int mask){completed=mask&7;}
    public static final Codec<EndProgression> CODEC=Codec.intRange(0,7).fieldOf("completed_trials").codec().xmap(EndProgression::new,d->d.completed);
    public static final SavedDataType<EndProgression> TYPE=new SavedDataType<>(TheDarkDimensions.id("end_progression"),EndProgression::new,CODEC);
    public static EndProgression data(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);}
    public void complete(int kind){if(kind<0||kind>2)return;int mask=completed|1<<kind;if(mask!=completed){completed=mask;setDirty();}}
    public static boolean unlocked(ServerLevel level){return data(level).completed==7;}
    public static void init(){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event)->{
        if(!(event.getEntity() instanceof ServerPlayer p)||p.tickCount%40!=0)return;
        var data=data(p.level());
        // Import existing 0.31/0.32 trial receipts, including offline participants.
        if(data.completed!=7){var realm=p.level().getServer().getLevel(DarkContent.BURNED_REALM);int mask=ResonanceProgress.data(p).completed;if(realm!=null)for(int value:TrialLedger.data(realm).granted.values())mask|=value;int old=data.completed;data.completed|=mask&7;if(old!=data.completed)data.setDirty();}
        if(data.completed==7){var adv=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("resonance/end_unsealed"));if(adv!=null)p.getAdvancements().award(adv,"done");}
    });}
}
