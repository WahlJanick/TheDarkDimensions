package at.janic.thedarkdimensions;

import at.janic.thedarkdimensions.worldgen.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.item.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class VeilwoodContent {
    public static final ResourceKey<Biome> BIOME=ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("veilwood"));
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> SOURCES=DeferredRegister.create(Registries.BIOME_SOURCE,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<EntityType<?>,EntityType<HollowWatcher>> WATCHER=TheDarkDimensions.ENTITIES.register("hollow_watcher",()->EntityType.Builder.of(HollowWatcher::new,MobCategory.MONSTER).sized(.6f,2.9f).eyeHeight(2.55f).clientTrackingRange(10).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("hollow_watcher"))));
    public static final DeferredItem<SpawnEggItem> EGG=TheDarkDimensions.ITEMS.registerItem("hollow_watcher_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(WATCHER.get()));
    public static final class Data extends SavedData {
        public final Map<String,Boolean> regions;
        public Data(){this(new HashMap<>());}public Data(Map<String,Boolean> regions){this.regions=new HashMap<>(regions);}
        public static final Codec<Data> CODEC=Codec.unboundedMap(Codec.STRING,Codec.BOOL).xmap(Data::new,d->d.regions);
        public static final SavedDataType<Data> TYPE=new SavedDataType<>(TheDarkDimensions.id("veilwood_watchers"),Data::new,CODEC);
    }
    public static Data data(ServerLevel level){return level.getDataStorage().computeIfAbsent(Data.TYPE);}
    public static boolean spawnOnce(ServerLevel level,BlockPos pos){
        if(level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!level.hasChunkAt(pos)||!level.getBiome(pos).is(BIOME))return false;
        String key=VeilwoodBiomeSource.regionKey(pos.getX(),pos.getZ());Data data=data(level);if(data.regions.containsKey(key))return false;
        var mob=WATCHER.get().create(level,EntitySpawnReason.EVENT);if(mob==null)return false;
        mob.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
        if(!level.getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.DIRT)||!level.noCollision(mob)||!level.getFluidState(pos).isEmpty())return false;
        mob.setForestHome(pos);if(!level.addFreshEntity(mob))return false;
        data.regions.put(key,true);data.setDirty();return true;
    }
    public static void init(IEventBus bus){
        SOURCES.register("veilwood",()->VeilwoodBiomeSource.CODEC);SOURCES.register(bus);
        DarkContent.FEATURES.register("veilwood_tree",()->VeilwoodTreeFeature.CODEC);
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(WATCHER.get(),Enderman.createAttributes().add(Attributes.MAX_HEALTH,40).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.FOLLOW_RANGE,32).build()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.LevelTickEvent.Post e)->{
            if(!(e.getLevel() instanceof ServerLevel level)||!level.dimension().equals(DarkContent.BURNED_REALM)||level.getGameTime()%40!=0)return;
            for(var player:level.players()){
                if(player.isSpectator()||!player.isAlive()||!level.getBiome(player.blockPosition()).is(BIOME))continue;
                if(data(level).regions.containsKey(VeilwoodBiomeSource.regionKey(player.getBlockX(),player.getBlockZ())))continue;
                for(int attempt=0;attempt<16;attempt++){
                    double a=level.getRandom().nextDouble()*Math.PI*2;int r=20+level.getRandom().nextInt(21),x=player.getBlockX()+(int)(Math.cos(a)*r),z=player.getBlockZ()+(int)(Math.sin(a)*r);
                    if(!level.hasChunkAt(new BlockPos(x,player.getBlockY(),z)))continue;
                    var pos=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(x,0,z));
                    if(!VeilwoodBiomeSource.regionKey(x,z).equals(VeilwoodBiomeSource.regionKey(player.getBlockX(),player.getBlockZ())))continue;
                    if(spawnOnce(level,pos))break;
                }
            }
        });
    }
}
