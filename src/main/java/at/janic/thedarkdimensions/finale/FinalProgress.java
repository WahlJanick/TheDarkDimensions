package at.janic.thedarkdimensions.finale;

import at.janic.thedarkdimensions.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FinalProgress {
    public static final class Data {
        public boolean won,found;public long ready;public String waypoint;
        public Data(){this(false,false,0,"");}public Data(boolean won,boolean found,long ready,String waypoint){this.won=won;this.found=found;this.ready=ready;this.waypoint=waypoint;}
        public static final Codec<Data> CODEC=RecordCodecBuilder.create(i->i.group(Codec.BOOL.fieldOf("won").forGetter(d->d.won),Codec.BOOL.fieldOf("found").forGetter(d->d.found),Codec.LONG.fieldOf("ready").forGetter(d->d.ready),Codec.STRING.fieldOf("waypoint").forGetter(d->d.waypoint)).apply(i,Data::new));
    }
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Data>> RECORDS=DevilContent.ATTACHMENTS.register("resonance_finale",()->AttachmentType.builder(Data::new).serialize(Data.CODEC.fieldOf("finale")).copyOnDeath().build());
    public static Data data(ServerPlayer p){return p.getData(RECORDS);}
    public static boolean perfectDodge(ServerPlayer p){
        var d=data(p);long now=p.level().getServer().overworld().getGameTime();if(!d.won||now<d.ready)return false;d.ready=now+160;p.addEffect(new MobEffectInstance(MobEffects.SPEED,60,0));CombatFeedback.play(p,"rune_chronus",p.position().add(0,1,0),0xb9a3ef);return true;
    }
    public static void init(){RECORDS.getId();net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e)->{if(e.getEntity() instanceof ServerPlayer p&&p.isAlive()&&p.tickCount%40==0){var realm=p.level().getServer().getLevel(DarkContent.BURNED_REALM);if(realm!=null)FinalLedger.data(realm).deliver(p);}});}
}
