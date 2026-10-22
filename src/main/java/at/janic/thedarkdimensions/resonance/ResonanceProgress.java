package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ResonanceProgress {
    public record Site(int kind,String dimension,int x,int y,int z,boolean waypoint){
        public static final Codec<Site> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,2).fieldOf("kind").forGetter(Site::kind),Codec.STRING.fieldOf("dimension").forGetter(Site::dimension),Codec.INT.fieldOf("x").forGetter(Site::x),Codec.INT.fieldOf("y").forGetter(Site::y),Codec.INT.fieldOf("z").forGetter(Site::z),Codec.BOOL.fieldOf("waypoint").forGetter(Site::waypoint)).apply(i,Site::new));
        public Site saved(){return new Site(kind,dimension,x,y,z,true);}
    }
    public static final class Data {
        public int completed,lastSpell=-1;public final Set<String> biomes;public final Map<String,Site> sites;public final Map<String,Long> runeReady;
        public Data(){this(0,List.of(),Map.of(),Map.of(),-1);}
        public Data(int completed,List<String> biomes,Map<String,Site> sites,Map<String,Long> ready,int lastSpell){this.completed=completed&7;this.biomes=new TreeSet<>(biomes);this.sites=new LinkedHashMap<>(sites);this.runeReady=new HashMap<>(ready);this.lastSpell=lastSpell;}
        public static final Codec<Data> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,7).fieldOf("completed").forGetter(d->d.completed),Codec.STRING.listOf().fieldOf("biomes").forGetter(d->List.copyOf(d.biomes)),Codec.unboundedMap(Codec.STRING,Site.CODEC).fieldOf("sites").forGetter(d->d.sites),Codec.unboundedMap(Codec.STRING,Codec.LONG).optionalFieldOf("ready",Map.of()).forGetter(d->d.runeReady),Codec.INT.optionalFieldOf("last_spell",-1).forGetter(d->d.lastSpell)).apply(i,Data::new));
    }
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Data>> RECORDS=DevilContent.ATTACHMENTS.register("ancient_resonance",()->AttachmentType.builder(Data::new).serialize(Data.CODEC.fieldOf("progress")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> UNLOCKS=DevilContent.ATTACHMENTS.register("resonance_unlocks",()->AttachmentType.builder(()->0).sync(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static Data data(ServerPlayer p){return p.getData(RECORDS);}
    public static void discover(ServerPlayer p,TrialController trial){
        var d=data(p);var pos=trial.getBlockPos();String key=trial.identity();var level=trial.getLevel();if(level==null)return;
        if(d.sites.size()<512||d.sites.containsKey(key))d.sites.putIfAbsent(key,new Site(trial.kind().ordinal(),level.dimension().identifier().toString(),pos.getX(),pos.getY(),pos.getZ(),false));
    }
    public static boolean unlocked(ServerPlayer p,int rune){return rune>=0&&rune<4&&(rune==3?data(p).completed==7:(data(p).completed&(1<<rune))!=0);}
    public static void init(){
        RECORDS.getId();net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e)->{
            if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||p.tickCount%40!=0)return;
            var realm=p.level().getServer().getLevel(DarkContent.BURNED_REALM);if(realm!=null)TrialLedger.data(realm).deliver(p);
            if(p.level().dimension().equals(DarkContent.BURNED_REALM)){
                var key=p.level().getBiome(p.blockPosition()).unwrapKey();key.ifPresent(k->{if(k.identifier().getNamespace().equals(TheDarkDimensions.MOD_ID))data(p).biomes.add(k.identifier().getPath());});
            }
        });
    }
}
