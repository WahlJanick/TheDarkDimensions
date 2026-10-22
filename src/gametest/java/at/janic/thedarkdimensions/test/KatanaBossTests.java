package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

final class KatanaBossTests {
    private static AshenSovereign boss(GameTestHelper h){
        for(int x=1;x<15;x++)for(int z=1;z<15;z++)h.setBlock(x,1,z,Blocks.STONE);
        var boss=h.spawn(KatanaBossContent.SOVEREIGN.get(),new Vec3(5.5,2,7.5));
        // Structure tests release FORCED tickets shared by nearby fixtures. Keep combat
        // independently simulated with a short-lived ticket that expires after this test.
        h.getLevel().getChunkSource().addTicketWithRadius(net.minecraft.server.level.TicketType.PORTAL,boss.chunkPosition(),2);
        h.onEachTick(()->{if(!boss.isRemoved())h.getLevel().getChunkSource().addTicketWithRadius(net.minecraft.server.level.TicketType.PORTAL,boss.chunkPosition(),2);});
        return boss;
    }
    static void phases(GameTestHelper h){
        var b=boss(h);h.assertTrue(b.getMaxHealth()==120&&b.isPersistenceRequired()&&!b.removeWhenFarAway(100000)&&b.getMainHandItem().is(KatanaContent.SOULEATER),"Boss has persistent native attributes and a visible Souleater");
        b.setHealth(79);h.runAfterDelay(2,()->{h.assertTrue(b.phase()==2,"Second combat phase starts below two thirds health");b.setHealth(39);});
        h.runAfterDelay(4,()->{h.assertTrue(b.phase()==3,"Final combat phase starts below one third health");h.succeed();});
    }
    static void windup(GameTestHelper h){
        var b=boss(h);var cow=h.spawn(EntityTypes.COW,new Vec3(5.5,2,5.5));cow.setNoAi(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);var start=cow.position();
        h.onEachTick(()->{b.setTarget(cow);cow.setPos(start);cow.setDeltaMovement(Vec3.ZERO);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(b.combatPose()==AshenSovereign.SLASH,"Boss begins a synchronized, parryable sword windup"))
            .thenExecute(()->h.assertTrue(cow.getHealth()==100&&b.animationProgress(0)<1,"Starting the windup causes no instant damage"))
            .thenWaitUntil(()->h.assertTrue(cow.getHealth()<100,"Announced sword cut eventually resolves"))
            .thenExecute(()->h.assertTrue(cow.getHealth()==93,"First phase sword cut deals seven damage" )).thenSucceed();
    }
    static void parry(GameTestHelper h){
        var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get());var p=f.player();var b=boss(h);boolean[] guarded={false};
        h.onEachTick(()->{b.setTarget(p);if(!guarded[0]&&b.combatPose()==AshenSovereign.SLASH&&b.animationProgress(0)>=.88f){guarded[0]=KatanaDefense.guard(p);}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(b.combatPose()==AshenSovereign.EXPOSED,"A real timed player parry interrupts the boss attack"))
            .thenExecute(()->{try{
                h.assertTrue(guarded[0]&&p.getHealth()==20&&KatanaDefense.staggered(b),"Perfect parry prevents the slash and opens the boss guard");p.stopUsingItem();
                b.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
                h.assertTrue(b.getHealth()==113&&b.getEffect(KatanaEffects.FROSTED).getAmplifier()==1,"Boss can receive exactly the usual themed perfect counter");
            }finally{f.close();}}).thenSucceed();
    }
    static void heavy(GameTestHelper h,boolean dodge){
        var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get());var p=f.player();var b=boss(h);b.setHealth(35);boolean[] defending={false};
        var playerStart=p.position();var bossStart=b.position();
        h.onEachTick(()->{
            b.setTarget(p);if(!defending[0]){p.setHealth(20);p.damageCooldownTime=0;p.setPos(playerStart);p.setDeltaMovement(Vec3.ZERO);p.setOnGround(true);b.setPos(bossStart);b.setDeltaMovement(Vec3.ZERO);
                if(b.combatPose()==AshenSovereign.HEAVY&&b.animationProgress(0)>=.9f)defending[0]=dodge?KatanaDefense.dodge(p,5):KatanaDefense.guard(p);
            }
        });
        h.startSequence().thenWaitUntil(()->h.assertTrue(defending[0],"Player responds to the red heavy-attack windup; boss pose="+b.combatPose()+" phase="+b.phase()+" ticks="+b.tickCount+" progress="+b.animationProgress(0)+" target="+b.getTarget()+" health="+b.getHealth()+" removed="+b.isRemoved()+" player="+p.position()+" alive="+p.isAlive()+" weapon="+p.getMainHandItem()+" menu="+(p.containerMenu==p.inventoryMenu)))
            .thenWaitUntil(()->h.assertTrue(b.combatPose()==AshenSovereign.SWING,"Heavy attack resolves after its announcement"))
            .thenExecute(()->{try{h.assertTrue(dodge?p.getHealth()==20:p.getHealth()==7,dodge?"Directional dodge avoids the boss heavy attack":"Heavy attack bypasses even a timed katana guard");}finally{f.close();}}).thenSucceed();
    }
    static void loot(GameTestHelper h){
        var f=DefenseTests.player(h,KatanaContent.SOULEATER.get());var p=f.player();var b=boss(h);b.setNoAi(true);
        b.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),1000);
        h.runAfterDelay(3,()->{try{
            var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,b.getBoundingBox().inflate(5));
            var swords=drops.stream().filter(e->e.getItem().is(KatanaContent.SOULEATER)).toList();
            h.assertTrue(swords.size()==1&&swords.getFirst().getItem().getDamageValue()>0,"Boss drops one worn Souleater, without duplicating held equipment");
            h.assertTrue(drops.stream().filter(e->e.getItem().is(KatanaContent.SOUL_CORE)).mapToInt(e->e.getItem().getCount()).sum()==2,"Boss guarantees two Soul Cores");
            h.assertTrue(p.getData(Bestiary.RECORDS).entries.get("ashen_sovereign").kills()==1,"Actual boss kill appears in the Bestiary");
            var adv=h.getLevel().getServer().getAdvancements().get(TheDarkDimensions.id("exploration/sovereign_defeated"));h.assertTrue(adv!=null&&p.getAdvancements().getOrStartProgress(adv).isDone(),"Actual boss kill awards the challenge advancement");h.succeed();
        }finally{f.close();}});
    }
    static void structure(GameTestHelper h){
        var l=h.getLevel();var origin=h.absolutePos(new BlockPos(0,30,-32));int cx=origin.getX()>>4,cz=origin.getZ()>>4;
        for(int x=cx;x<=cx+2;x++)for(int z=cz;z<=cz+2;z++)l.setChunkForced(x,z,true);
        var template=l.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id("ashen_dojo"));
        var entityTags=template.save(new net.minecraft.nbt.CompoundTag()).getListOrEmpty("entities");
        h.assertTrue(entityTags.size()==1,"Dojo template contains exactly one entity");
        var bossTag=entityTags.getCompoundOrEmpty(0).getCompoundOrEmpty("nbt");
        var loaded=EntityType.create(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,l.registryAccess(),bossTag),l,new EntitySpawnRequest(EntitySpawnReason.STRUCTURE,false));
        h.assertTrue(loaded.isPresent()&&loaded.get() instanceof AshenSovereign,"Dojo entity NBT loads through the native entity codec");
        loaded.get().discard();
        h.assertTrue(template.placeInWorld(l,origin,origin,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),net.minecraft.util.RandomSource.create(29),2),"Native Dojo template places");
        var structure=l.registryAccess().getOrThrow(KatanaBossContent.DOJO).value();
        var set=l.registryAccess().getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,TheDarkDimensions.id("ashen_dojo"))).value();h.assertTrue(set.structures().getFirst().structure().value()==structure,"Dojo is registered in a locate-compatible structure set");
        var g=l.getChunkSource().getGenerator();var state=l.getChunkSource().randomState();var climate=state.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
        var source=new net.minecraft.world.level.biome.FixedBiomeSource(l.registryAccess().getOrThrow(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("ash_wastes"))));
        var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(l.registryAccess(),g,source,climate,state,l.getStructureTemplateManager(),29L,new net.minecraft.world.level.ChunkPos(10,10),l,structure.biomes()::contains);
        h.assertTrue(structure.findValidGenerationPoint(context).isPresent(),"Native jigsaw generation finds the Dojo in its permitted biome");
        h.runAfterDelay(50,()->{
            var bosses=l.getEntitiesOfClass(AshenSovereign.class,new AABB(Vec3.atLowerCornerOf(origin),Vec3.atLowerCornerOf(origin.offset(23,10,23))));
            var found=new ArrayList<String>();for(var entity:l.getAllEntities())if(entity instanceof AshenSovereign)found.add(entity.position()+" health="+((AshenSovereign)entity).getHealth());
            h.assertTrue(bosses.size()==1&&bosses.getFirst().isPersistenceRequired(),"Exactly one persistent Sovereign is placed in the courtyard; found "+bosses.size()+" origin="+origin+" all="+found);
            h.assertTrue(bosses.getFirst().getMainHandItem().is(KatanaContent.SOULEATER)&&bosses.getFirst().getHealth()==120,"Structure-loaded Sovereign retains his katana and health");
            h.assertTrue(bosses.getFirst().getDropChances().byEquipment(EquipmentSlot.MAINHAND)==0,"Structure-loaded equipment cannot add another katana drop");
            h.assertTrue(l.getBlockEntity(origin.offset(5,2,20)) instanceof net.minecraft.world.level.block.entity.LecternBlockEntity,"Dojo includes readable combat hints");
            for(int x=cx;x<=cx+2;x++)for(int z=cz;z<=cz+2;z++)l.setChunkForced(x,z,false);h.succeed();
        });
    }
}
