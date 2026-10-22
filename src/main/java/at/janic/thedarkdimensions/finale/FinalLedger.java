package at.janic.thedarkdimensions.finale;

import at.janic.thedarkdimensions.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.*;

public final class FinalLedger extends SavedData {
    public final Set<String> deaths,pending,awarded;
    public FinalLedger(){this(List.of(),List.of(),List.of());}private FinalLedger(List<String> deaths,List<String> pending,List<String> awarded){this.deaths=new HashSet<>(deaths);this.pending=new HashSet<>(pending);this.awarded=new HashSet<>(awarded);}
    public static final Codec<FinalLedger> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.listOf().fieldOf("deaths").forGetter(d->List.copyOf(d.deaths)),Codec.STRING.listOf().fieldOf("pending").forGetter(d->List.copyOf(d.pending)),Codec.STRING.listOf().fieldOf("awarded").forGetter(d->List.copyOf(d.awarded))).apply(i,FinalLedger::new));
    public static final SavedDataType<FinalLedger> TYPE=new SavedDataType<>(TheDarkDimensions.id("resonance_finale"),FinalLedger::new,CODEC);
    public static FinalLedger data(ServerLevel l){return l.getDataStorage().computeIfAbsent(TYPE);}
    public void defeated(UUID boss){deaths.add(boss.toString());setDirty();}
    public void offer(UUID player){if(!awarded.contains(player.toString()))pending.add(player.toString());setDirty();}
    public void deliver(ServerPlayer p){String id=p.getUUID().toString();if(awarded.contains(id)){FinalProgress.data(p).won=true;return;}if(!pending.remove(id))return;
        boolean first=!FinalProgress.data(p).won;awarded.add(id);FinalProgress.data(p).won=true;if(first){var trophy=new ItemStack(FinaleContent.TROPHY.get());if(!p.getInventory().add(trophy))p.drop(trophy,false,net.minecraft.util.Prediction.SERVER_ONLY);CombatFeedback.play(p,"regent_defeated",p.position().add(0,1,0),0xe4c982);p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("finale.thedarkdimensions.ability_unlocked"),false);}
        var advancement=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("resonance/regent_defeated"));if(advancement!=null)p.getAdvancements().award(advancement,"done");setDirty();p.inventoryMenu.broadcastChanges();
    }
}
