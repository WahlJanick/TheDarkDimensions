package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;

final class MobExpansionTests {
    static void attributes(GameTestHelper h){
        var types=List.of(MobExpansionContent.CHRONUS_MAGE.get(),MobExpansionContent.DROWNED_SENTINEL.get(),MobExpansionContent.BRIAR_WHISPERER.get(),MobExpansionContent.VEILWOOD_SPIDER.get(),MobExpansionContent.DUNE_STALKER.get(),MobExpansionContent.CINDER_BROODMOTHER.get(),MobExpansionContent.ASHEN_WRAITH.get(),MobExpansionContent.BONEWING.get(),MobExpansionContent.LANTERN_WISP.get());
        for(var type:types){var mob=type.create(h.getLevel(),EntitySpawnReason.COMMAND);h.assertTrue(mob!=null&&mob.getMaxHealth()>0,"New mob has registered attributes: "+type);}
        var boss=MobExpansionContent.CINDER_BROODMOTHER.get().create(h.getLevel(),EntitySpawnReason.COMMAND);
        h.assertTrue(boss.getMaxHealth()==60&&boss.fireImmune(),"Broodmother has mini-boss health and fire immunity");
        var wisp=MobExpansionContent.LANTERN_WISP.get().create(h.getLevel(),EntitySpawnReason.COMMAND);
        h.assertTrue(wisp.isNoGravity()&&MobExpansionContent.LANTERN_WISP.get().getCategory()==MobCategory.CREATURE&&wisp.getTarget()==null,"Wisp is a peaceful flying creature");
        h.succeed();
    }
    static void venom(GameTestHelper h){
        for(var type:List.of(MobExpansionContent.VEILWOOD_SPIDER.get(),MobExpansionContent.DUNE_STALKER.get())){
            var cow=h.spawn(EntityTypes.COW,new BlockPos(5,2,5));cow.setNoAi(true);
            var spider=type.create(h.getLevel(),EntitySpawnReason.COMMAND);
            h.assertTrue(spider.doHurtTarget(h.getLevel(),cow),"Venom attack damages victim");
            h.assertTrue(cow.getEffect(MobEffects.POISON).getDuration()==60,"Venom lasts three seconds");
        }h.succeed();
    }
    static void projectiles(GameTestHelper h){
        var caster=h.spawn(MobExpansionContent.CHRONUS_MAGE.get(),new BlockPos(2,2,4));caster.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var cow=h.spawn(EntityTypes.COW,new BlockPos(6,2,4));cow.setNoAi(true);float before=cow.getHealth();
        caster.performRangedAttack(cow,1);
        h.runAfterDelay(5,()->h.assertTrue(caster.isCharging()&&cow.getHealth()==before,"Synced spell pose warns before a projectile is fired"));
        h.runAfterDelay(34,()->{
            h.assertTrue(cow.getHealth()<before&&cow.hasEffect(MobEffects.SLOWNESS),"Time bolt travels and applies damage plus Slowness on collision");
            var water=h.spawn(MobExpansionContent.DROWNED_SENTINEL.get(),new BlockPos(2,2,8));water.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
            var other=h.spawn(EntityTypes.COW,new BlockPos(6,2,8));other.setNoAi(true);float health=other.getHealth();water.performRangedAttack(other,1);
            h.runAfterDelay(34,()->{h.assertTrue(other.getHealth()<health&&!other.hasEffect(MobEffects.SLOWNESS),"Water projectile damages without the Chronus effect");h.succeed();});
        });
    }
    static void roots(GameTestHelper h){
        var caster=h.spawn(MobExpansionContent.BRIAR_WHISPERER.get(),new BlockPos(2,2,4));caster.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var cow=h.spawn(EntityTypes.COW,new BlockPos(7,2,4));cow.setNoAi(true);float before=cow.getHealth();caster.performRangedAttack(cow,1);
        h.runAfterDelay(5,()->h.assertTrue(cow.getHealth()==before,"Root attack has a visible windup before damage"));
        h.runAfterDelay(26,()->{h.assertTrue(cow.getHealth()<before&&cow.hasEffect(MobEffects.SLOWNESS),"Root eruption deals damage and briefly snares stationary victim");h.succeed();});
    }
    static void flame(GameTestHelper h){
        var boss=h.spawn(MobExpansionContent.CINDER_BROODMOTHER.get(),new BlockPos(2,2,4));boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var cow=h.spawn(EntityTypes.COW,new BlockPos(9,2,4));cow.setNoAi(true);float before=cow.getHealth();boss.setTarget(cow);
        h.onEachTick(()->boss.setTarget(cow)); // Nearby mock players must not replace the controlled test target.
        h.runAfterDelay(108,()->h.assertTrue(cow.getHealth()==before&&boss.windupTicks()>0,"Broodmother warns before releasing flames"));
        h.runAfterDelay(132,()->{h.assertTrue(cow.getHealth()<before&&cow.isOnFire(),"Warned flame eruption damages and ignites victim");h.succeed();});
    }
    private static boolean contains(GameTestHelper h,ResourceKey<Biome> key,EntityType<?> type,MobCategory category){
        var settings=h.getLevel().registryAccess().getOrThrow(key).value().getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS,MobSpawnSettings.EMPTY);
        return settings.getMobsToSpawn(category).unwrap().stream().anyMatch(e->e.value().type()==type);
    }
    static void biomes(GameTestHelper h){
        h.assertTrue(contains(h,Biomes.DESERT,MobExpansionContent.DUNE_STALKER.get(),MobCategory.MONSTER),"Desert includes Dune Stalker");
        h.assertTrue(contains(h,Biomes.SWAMP,MobExpansionContent.BRIAR_WHISPERER.get(),MobCategory.MONSTER)&&contains(h,Biomes.JUNGLE,MobExpansionContent.BRIAR_WHISPERER.get(),MobCategory.MONSTER),"Overworld plant enemy inhabits swamp and jungle");
        h.assertTrue(!contains(h,Biomes.PLAINS,MobExpansionContent.BRIAR_WHISPERER.get(),MobCategory.MONSTER),"No unrelated Overworld spawning");
        h.assertTrue(contains(h,VeilwoodContent.BIOME,MobExpansionContent.VEILWOOD_SPIDER.get(),MobCategory.MONSTER)&&!contains(h,VeilwoodContent.BIOME,VeilwoodContent.WATCHER.get(),MobCategory.MONSTER),"Spiders coexist with separately controlled unique Watcher");
        h.assertTrue(contains(h,ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("fire_cave")),MobExpansionContent.CINDER_BROODMOTHER.get(),MobCategory.MONSTER),"Mini-boss belongs to Fire Caves");
        h.succeed();
    }
    static void geodes(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(5,2,5));h.assertTrue(!MobExpansionContent.nearGeode(h.getLevel(),p),"Mage is not allowed near arbitrary Stone");
        h.setBlock(new BlockPos(7,2,5),MineralContent.BUDDING_ESSENTIALIUM.get());
        h.assertTrue(MobExpansionContent.nearGeode(h.getLevel(),p),"Native geode shell permits Chronus Mage");
        h.assertTrue(!MobExpansionContent.nearGeode(h.getLevel(),p.offset(30,0,0)),"Geode affinity is local");h.succeed();
    }
    static void dash(GameTestHelper h){
        for(int x=1;x<=12;x++)for(int z=1;z<=12;z++)h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        var mage=h.spawn(MobExpansionContent.CHRONUS_MAGE.get(),new BlockPos(7,2,7));mage.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        // Neighbouring structure tests can release shared forced chunks before the
        // 100-tick dash cooldown ends. Keep this AI fixture independently simulated.
        h.getLevel().getChunkSource().addTicketWithRadius(net.minecraft.server.level.TicketType.PORTAL,mage.chunkPosition(),2);
        h.onEachTick(()->{if(!mage.isRemoved())h.getLevel().getChunkSource().addTicketWithRadius(net.minecraft.server.level.TicketType.PORTAL,mage.chunkPosition(),2);});
        var target=h.spawn(EntityTypes.COW,new BlockPos(10,2,7));target.setNoAi(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);target.setHealth(100);mage.setTarget(target);double original=mage.getX();var targetPosition=target.position();double[] landed={0};
        // NoAI does not disable knockback; keep this proximity-test target stationary.
        h.onEachTick(()->{target.setPos(targetPosition);target.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);mage.setTarget(target);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(mage.getX()<original-1.5,"Chronus Mage dashes away from a nearby target"))
            .thenExecute(()->{h.assertTrue(h.getLevel().noCollision(mage)&&h.getLevel().getBlockState(mage.blockPosition().below()).isSolid(),"Dash ends in free space on solid ground");landed[0]=mage.getX();})
            .thenIdle(20).thenExecute(()->h.assertTrue(Math.abs(mage.getX()-landed[0])<.1,"Dash cooldown prevents immediate repeated movement")).thenSucceed();
    }
}
