package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;

public final class MobExpansionContent {
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.ChronusMage>> CHRONUS_MAGE=mob("chronus_mage",RealmMobs.ChronusMage::new,.6f,1.95f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.DrownedSentinel>> DROWNED_SENTINEL=mob("drowned_sentinel",RealmMobs.DrownedSentinel::new,.6f,1.99f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.BriarWhisperer>> BRIAR_WHISPERER=mob("briar_whisperer",RealmMobs.BriarWhisperer::new,.8f,2.4f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.VeilwoodSpider>> VEILWOOD_SPIDER=mob("veilwood_spider",RealmMobs.VeilwoodSpider::new,.7f,.5f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.DuneStalker>> DUNE_STALKER=mob("dune_stalker",RealmMobs.DuneStalker::new,1.2f,.8f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.CinderBroodmother>> CINDER_BROODMOTHER=mob("cinder_broodmother",RealmMobs.CinderBroodmother::new,2.2f,1.4f,true,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.AshenWraith>> ASHEN_WRAITH=mob("ashen_wraith",RealmMobs.AshenWraith::new,.65f,1.8f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.Bonewing>> BONEWING=mob("bonewing",RealmMobs.Bonewing::new,1.1f,.6f,false,MobCategory.MONSTER);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMobs.LanternWisp>> LANTERN_WISP=mob("lantern_wisp",RealmMobs.LanternWisp::new,.5f,.5f,false,MobCategory.CREATURE);
    public static final DeferredHolder<EntityType<?>,EntityType<RealmMagicBolt>> MAGIC_BOLT=TheDarkDimensions.ENTITIES.register("realm_magic_bolt",()->EntityType.Builder.<RealmMagicBolt>of(RealmMagicBolt::new,MobCategory.MISC).sized(.25f,.25f).clientTrackingRange(8).updateInterval(2).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("realm_magic_bolt"))));
    private static <T extends Mob> DeferredHolder<EntityType<?>,EntityType<T>> mob(String id,EntityType.EntityFactory<T> factory,float width,float height,boolean fire,MobCategory category){
        var holder=TheDarkDimensions.ENTITIES.register(id,()->{
            var builder=EntityType.Builder.of(factory,category).sized(width,height).clientTrackingRange(8);
            if(fire)builder.fireImmune();return builder.build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id(id)));
        });
        TheDarkDimensions.ITEMS.registerItem(id+"_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(holder.get()));return holder;
    }
    private static AttributeSupplier attributes(double health,double damage,double speed){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,health).add(Attributes.ATTACK_DAMAGE,damage).add(Attributes.MOVEMENT_SPEED,speed).add(Attributes.FOLLOW_RANGE,24).build();}
    public static void init(IEventBus bus){
        bus.addListener((EntityAttributeCreationEvent e)->{
            e.put(CHRONUS_MAGE.get(),attributes(26,3,.24));e.put(DROWNED_SENTINEL.get(),attributes(24,3,.22));e.put(BRIAR_WHISPERER.get(),attributes(30,2,.22));
            e.put(VEILWOOD_SPIDER.get(),attributes(12,2,.3));e.put(DUNE_STALKER.get(),attributes(18,3,.27));e.put(CINDER_BROODMOTHER.get(),attributes(60,5,.24));
            e.put(ASHEN_WRAITH.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.2).add(Attributes.FLYING_SPEED,.24).add(Attributes.FOLLOW_RANGE,24).build());
            e.put(BONEWING.get(),Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,18).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,32).build());
            e.put(LANTERN_WISP.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,8).add(Attributes.MOVEMENT_SPEED,.15).add(Attributes.FLYING_SPEED,.16).build());
        });
        bus.addListener((RegisterSpawnPlacementsEvent e)->{
            ground(e,DROWNED_SENTINEL.get(),true);ground(e,BRIAR_WHISPERER.get(),false);ground(e,VEILWOOD_SPIDER.get(),false);ground(e,DUNE_STALKER.get(),false);ground(e,CINDER_BROODMOTHER.get(),true);ground(e,ASHEN_WRAITH.get(),false);
            e.register(LANTERN_WISP.get(),SpawnPlacementTypes.NO_RESTRICTIONS,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,l,r,p,random)->l.getBlockState(p).isAir()&&l.getBlockState(p.above()).isAir(),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.LevelTickEvent.Post e)->{
            if(e.getLevel() instanceof ServerLevel level&&level.dimension().equals(DarkContent.BURNED_REALM)&&level.getGameTime()%200==0)specialSpawns(level);
        });
    }
    private static <T extends Monster> void ground(RegisterSpawnPlacementsEvent e,EntityType<T> type,boolean anyLight){
        e.register(type,SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,l,r,p,random)->{
            if(type==CINDER_BROODMOTHER.get()&&!l.getLevel().getEntitiesOfClass(RealmMobs.CinderBroodmother.class,new net.minecraft.world.phys.AABB(p).inflate(64)).isEmpty())return false;
            return anyLight?Monster.checkAnyLightMonsterSpawnRules(t,l,r,p,random):Monster.checkMonsterSpawnRules(t,l,r,p,random);
        },RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    public static boolean nearGeode(ServerLevel level,BlockPos pos){
        for(var p:BlockPos.betweenClosed(pos.offset(-6,-6,-6),pos.offset(6,6,6))){
            if(!level.hasChunkAt(p))continue;
            var state=level.getBlockState(p);
            if(state.is(MineralContent.ESSENTIALIUM_BLOCK.get())||state.is(MineralContent.BUDDING_ESSENTIALIUM.get()))return true;
        }
        return false;
    }
    public static boolean spawnAt(ServerLevel level,EntityType<? extends Mob> type,BlockPos pos){
        if(!level.hasChunkAt(pos)||level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!level.getFluidState(pos).isEmpty())return false;
        var mob=type.create(level,EntitySpawnReason.EVENT);if(mob==null)return false;
        mob.snapTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
        if(!level.noCollision(mob))return false;
        mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),EntitySpawnReason.EVENT,null);return level.addFreshEntity(mob);
    }
    private static void specialSpawns(ServerLevel level){
        var rules=level.getGameRules();
        if(level.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!rules.get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MOBS)||!rules.get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MONSTERS))return;
        for(var player:level.players()){
            if(player.isSpectator()||!player.isAlive()||level.getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(48),m->m instanceof net.minecraft.world.entity.monster.Enemy).size()>=12)continue;
            boolean surface=level.canSeeSky(player.blockPosition());
            if(!surface&&level.getRandom().nextFloat()<.3f&&level.getEntitiesOfClass(RealmMobs.ChronusMage.class,player.getBoundingBox().inflate(64)).size()<2){
                for(int i=0;i<4;i++){
                    var pos=player.blockPosition().offset(level.getRandom().nextInt(33)-16,level.getRandom().nextInt(13)-6,level.getRandom().nextInt(33)-16);
                    if(player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<64||!level.hasChunkAt(pos)||!level.getBlockState(pos.below()).isSolid()||!level.getBlockState(pos).isAir())continue;
                    if(nearGeode(level,pos)&&spawnAt(level,CHRONUS_MAGE.get(),pos))break;
                }
            }
            if(!surface)continue;
            double a=level.getRandom().nextDouble()*Math.PI*2;int x=player.getBlockX()+(int)(Math.cos(a)*30),z=player.getBlockZ()+(int)(Math.sin(a)*30);
            if(!level.hasChunkAt(new BlockPos(x,player.getBlockY(),z)))continue;
            var pos=level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,new BlockPos(x,0,z));
            if(level.getBiome(pos).is(VeilwoodContent.BIOME))continue;
            if(Ashfall.active(level)&&level.getRandom().nextFloat()<.5f&&level.getEntitiesOfClass(RealmMobs.AshenWraith.class,player.getBoundingBox().inflate(48)).size()<3)spawnAt(level,ASHEN_WRAITH.get(),pos.above(1));
            if(level.getGameTime()%600==0&&level.getRandom().nextFloat()<.15f&&level.getEntitiesOfClass(RealmMobs.Bonewing.class,player.getBoundingBox().inflate(64)).isEmpty())spawnAt(level,BONEWING.get(),pos.above(10));
        }
    }
}
