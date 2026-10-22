package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.finale.*;
import at.janic.thedarkdimensions.resonance.*;
import at.janic.thedarkdimensions.worldgen.WanderingSitesFeature;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.*;

final class FinaleTests {
    private static int count(ServerPlayer p,Item item){return p.getInventory().getNonEquipmentItems().stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    static void feedback(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.IRON_SWORD);var other=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=f.player();for(var channel:List.of(f.channel(),other.channel())){channel.runPendingTasks();while(channel.readOutbound()!=null){}}
            h.assertTrue(CombatFeedback.play(p,"trial_complete",p.position(),0)&&!CombatFeedback.play(p,"trial_complete",p.position(),0),"Repeated confirmed events are throttled");
            f.channel().runPendingTasks();int personal=0;Object packet;while((packet=f.channel().readOutbound())!=null)if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket s&&s.getSound().value()==CombatFeedback.EVENTS.get("trial_complete").get())personal++;
            h.assertTrue(personal==1,"Trial completion sends exactly one personal sound packet");other.channel().runPendingTasks();while((packet=other.channel().readOutbound())!=null)h.assertTrue(!(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket),"Party completion sounds are not broadcast repeatedly to neighbours");
            h.assertTrue(CombatFeedback.rare(new ItemStack(FinaleContent.TROPHY.get()))&&CombatFeedback.rare(new ItemStack(ResonanceContent.SIGILS.get(0).get()))&&!CombatFeedback.rare(new ItemStack(Items.COBBLESTONE)),"Rare treasure and Sigils have the subtle cue, ordinary blocks do not");
            for(String name:CombatFeedback.NAMES)h.assertTrue(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.containsKey(TheDarkDimensions.id(name)),"Registered custom cue "+name);
        }h.succeed();
    }
    static void receipts(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=f.player();UUID offline=UUID.randomUUID();var ledger=new FinalLedger();ledger.offer(p.getUUID());ledger.offer(p.getUUID());ledger.offer(offline);ledger.defeated(UUID.randomUUID());
            ledger=FinalLedger.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,FinalLedger.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,ledger).getOrThrow()).getOrThrow();ledger.deliver(p);
            h.assertTrue(FinalProgress.data(p).won&&count(p,FinaleContent.TROPHY.get())==1&&ledger.pending.contains(offline.toString()),"Saved ledger grants one personal trophy while offline reward remains pending");ledger.offer(p.getUUID());ledger.deliver(p);h.assertTrue(count(p,FinaleContent.TROPHY.get())==1,"Repeat requests cannot duplicate reward");
            var adv=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("resonance/regent_defeated"));h.assertTrue(adv!=null&&p.getAdvancements().getOrStartProgress(adv).isDone(),"Actual delivery awards the finale advancement");
        }h.succeed();
    }
    static void ability(GameTestHelper h){
        try(var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get())){
            var p=f.player();h.assertTrue(!FinalProgress.perfectDodge(p)&&!p.hasEffect(MobEffects.SPEED),"Unbeaten Regent grants no ability");FinalProgress.data(p).won=true;FinalProgress.data(p).found=true;FinalProgress.data(p).waypoint="test waypoint";
            h.assertTrue(FinalProgress.perfectDodge(p)&&p.getEffect(MobEffects.SPEED).getAmplifier()==0&&p.getEffect(MobEffects.SPEED).getDuration()==60,"Resonant Step grants three seconds of Speed I");p.removeEffect(MobEffects.SPEED);h.assertTrue(!FinalProgress.perfectDodge(p)&&!p.hasEffect(MobEffects.SPEED),"Eight-second cooldown prevents refreshed speed");
            var clone=(ServerPlayer)h.makeMockServerPlayer(GameType.SURVIVAL);clone.restoreFrom(p,false);h.assertTrue(FinalProgress.data(clone).won&&FinalProgress.data(clone).ready==FinalProgress.data(p).ready&&FinalProgress.data(clone).waypoint.equals("test waypoint"),"Native respawn retains ability, waypoint and cooldown");
            var encoded=FinalProgress.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,FinalProgress.data(p)).getOrThrow();h.assertTrue(FinalProgress.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow().won,"Ability serializes");var journal=new ItemStack(ResonanceContent.JOURNAL.get());FieldJournal.refresh(p,journal);h.assertTrue(journal.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().stream().anyMatch(page->page.raw().getString().contains("Resonant Step")),"Native journal explains permanent ability");
        }h.succeed();
    }
    private record Arena(ServerLevel level,NexusAltar altar){}
    private static Arena arena(GameTestHelper h,int index){
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);var origin=new BlockPos(6400+index*128,180,6400);
        for(int x=(origin.getX()-16)>>4;x<=(origin.getX()+64)>>4;x++)for(int z=(origin.getZ()-16)>>4;z<=(origin.getZ()+64)>>4;z++){realm.setChunkForced(x,z,true);realm.getChunk(x,z);}
        var template=realm.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id("resonance_nexus"));var settings=new StructurePlaceSettings();h.assertTrue(template.placeInWorld(realm,origin,origin,settings,RandomSource.create(32),18),"Native Nexus template places");var pos=template.filterBlocks(origin,settings,FinaleContent.ALTAR.get()).getFirst().pos();return new Arena(realm,(NexusAltar)realm.getBlockEntity(pos));
    }
    private static void move(ServerPlayer p,Arena a,int dx){var pos=a.altar.getBlockPos().offset(dx,0,2);p.teleportTo(a.level,pos.getX()+.5,pos.getY(),pos.getZ()+.5,Set.of(),0,0,true);p.setOnGround(true);p.getAbilities().invulnerable=true;}
    private static void attune(ServerPlayer p,Arena a){ResonanceProgress.data(p).completed=7;for(var sigil:ResonanceContent.SIGILS){p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(sigil.get()));a.altar.interact(p,InteractionHand.MAIN_HAND);}p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);}
    static void sigils(GameTestHelper h){var a=arena(h,0);var f=DefenseTests.player(h,Items.IRON_SWORD);h.runAfterDelay(10,()->{
        try(f){
            var p=f.player();move(p,a,0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResonanceContent.SIGILS.get(0).get()));a.altar.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(a.altar.mask()==0,"Borrowed Sigil cannot bypass personal trial completion");
            var gate=a.altar.getBlockPos().offset(0,0,-22);h.assertTrue(a.level.getBlockState(gate).is(FinaleContent.GATE),"Arena entrance is initially sealed");ResonanceProgress.data(p).completed=7;p.teleportTo(a.level,gate.getX()+.5,gate.getY(),gate.getZ()-1.5,Set.of(),0,0,true);
            for(var sigil:ResonanceContent.SIGILS){p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(sigil.get()));a.level.getBlockState(gate).useItemOn(p.getMainHandItem(),a.level,p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(gate),Direction.NORTH,gate,false));h.assertTrue(p.getMainHandItem().getCount()==1,"Native entrance interaction retains each unique Sigil");}
            h.assertTrue(a.altar.mask()==7&&a.level.getBlockState(gate).isAir(),"All three permanently open the actual entrance");
            var tag=a.altar.saveWithFullMetadata(a.level.registryAccess());var restored=(NexusAltar)BlockEntity.loadStatic(a.altar.getBlockPos(),a.altar.getBlockState(),tag,a.level.registryAccess());h.assertTrue(restored!=null&&restored.mask()==7,"Attunement survives native block-entity reload");a.altar.reset();h.assertTrue(a.altar.mask()==7&&a.level.getBlockState(gate).isAir(),"Failure never closes the gate or requires more Sigils");
            move(p,a,0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ResonanceContent.JOURNAL.get()));a.altar.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(FinalProgress.data(p).waypoint.contains(DarkContent.BURNED_REALM.identifier().toString()),"Journal explicitly saves the Nexus dimension and coordinates");
        }h.succeed();});
    }
    static void arenaBattle(GameTestHelper h){var a=arena(h,1);var first=DefenseTests.player(h,Items.IRON_SWORD);var second=DefenseTests.player(h,Items.IRON_SWORD);h.runAfterDelay(10,()->{
        try(first;second){
            var p=first.player();var friend=second.player();move(p,a,0);move(friend,a,2);attune(p,a);a.altar.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(a.altar.phase()==1,"Empty hand starts announced summoning");for(int i=0;i<81;i++)a.altar.tickServer();
            h.assertTrue(a.altar.phase()==2&&a.level.getEntity(a.altar.boss()) instanceof AshboundRegent,"Exactly one real Regent is spawned");var boss=(AshboundRegent)a.level.getEntity(a.altar.boss());h.assertTrue(boss.getMaxHealth()==320&&boss.hasArena(),"Cooperative health scales by sixty per extra player");
            var tag=a.altar.saveWithFullMetadata(a.level.registryAccess());var restored=(NexusAltar)BlockEntity.loadStatic(a.altar.getBlockPos(),a.altar.getBlockState(),tag,a.level.registryAccess());restored.setLevel(a.level);a.level.setBlockEntity(restored);h.assertTrue(restored.boss().equals(boss.getUUID())&&restored.phase()==2,"Reload keeps the same fight UUID and participants");
            friend.teleportTo(h.getLevel(),0,100,0,Set.of(),0,0,true);boss.hurtServer(a.level,a.level.damageSources().genericKill(),10000);restored.tickServer();
            h.assertTrue(restored.phase()==3&&FinalProgress.data(p).won&&FinalProgress.data(friend).won&&count(p,FinaleContent.TROPHY.get())==1&&count(friend,FinaleContent.TROPHY.get())==1,"Boss death grants each participant one reward even after dimension travel");
            restored.interact(p,InteractionHand.MAIN_HAND);for(int i=0;i<100;i++)restored.tickServer();h.assertTrue(restored.phase()==3&&count(p,FinaleContent.TROPHY.get())==1,"Completed arena cannot respawn or farm rewards");
        }finally{a.altar.reset();}h.succeed();});
    }
    private static AshboundRegent boss(GameTestHelper h){for(int x=1;x<15;x++)for(int z=1;z<15;z++)h.setBlock(x,1,z,Blocks.STONE);var b=h.spawn(FinaleContent.REGENT.get(),new Vec3(5.5,2,7.5));h.onEachTick(()->{if(!b.isRemoved())h.getLevel().getChunkSource().addTicketWithRadius(TicketType.PORTAL,b.chunkPosition(),2);});return b;}
    static void departedVictory(GameTestHelper h){var a=arena(h,3);var f=DefenseTests.player(h,Items.IRON_SWORD);h.runAfterDelay(10,()->{try(f){var p=f.player();move(p,a,0);attune(p,a);a.altar.interact(p,InteractionHand.MAIN_HAND);for(int i=0;i<81;i++)a.altar.tickServer();var b=a.level.getEntity(a.altar.boss());h.assertTrue(b instanceof AshboundRegent,"Regent exists before final participant leaves");p.teleportTo(h.getLevel(),0,100,0,Set.of(),0,0,true);((AshboundRegent)b).hurtServer(a.level,a.level.damageSources().genericKill(),10000);a.altar.tickServer();h.assertTrue(a.altar.phase()==3&&FinalProgress.data(p).won&&count(p,FinaleContent.TROPHY.get())==1,"Recorded boss death settles victory even with no participant still in arena");}finally{a.altar.reset();}h.succeed();});}
    static void retry(GameTestHelper h){var a=arena(h,2);var f=DefenseTests.player(h,Items.IRON_SWORD);h.runAfterDelay(10,()->{try(f){var p=f.player();move(p,a,0);attune(p,a);a.altar.interact(p,InteractionHand.MAIN_HAND);for(int i=0;i<81;i++)a.altar.tickServer();var first=a.level.getEntity(a.altar.boss());h.assertTrue(first instanceof AshboundRegent,"Initial fight exists");p.teleportTo(h.getLevel(),0,100,0,Set.of(),0,0,true);for(int i=0;i<301;i++)a.altar.tickServer();h.assertTrue(a.altar.phase()==0&&a.altar.mask()==7&&first.isRemoved()&&!FinalProgress.data(p).won&&count(p,FinaleContent.TROPHY.get())==0,"Leaving clears the failed fight without closing the arena or granting rewards");move(p,a,0);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.altar.interact(p,InteractionHand.MAIN_HAND);for(int i=0;i<81;i++)a.altar.tickServer();var second=a.level.getEntity(a.altar.boss());h.assertTrue(second instanceof AshboundRegent&&second!=first&&((AshboundRegent)second).getHealth()==260,"Unlocked arena retries with a new full-health boss at no further Sigil cost");}finally{a.altar.reset();}h.succeed();});}
    static void phases(GameTestHelper h){var b=boss(h);h.assertTrue(b.getMaxHealth()==260&&b.isPersistenceRequired(),"Regent is a persistent final boss");b.setHealth(170);h.runAfterDelay(2,()->{h.assertTrue(b.phase()==2,"Second phase below two thirds health");b.setHealth(80);});h.runAfterDelay(4,()->{h.assertTrue(b.phase()==3,"Third phase below one third health");h.succeed();});}
    static void parry(GameTestHelper h){var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get());var p=f.player();var b=boss(h);boolean[] guarded={false};h.onEachTick(()->{b.setTarget(p);if(!guarded[0]&&b.combatPose()==AshboundRegent.SLASH&&b.animationProgress(0)>=.9f)guarded[0]=KatanaDefense.guard(p);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(b.combatPose()==AshboundRegent.EXPOSED,"Timed Perfect Parry exposes the Regent")).thenExecute(()->{try{h.assertTrue(guarded[0]&&p.getHealth()==20&&KatanaDefense.staggered(b),"Blue strike is actually parryable without damage");}finally{f.close();}}).thenSucceed();}
    static void rift(GameTestHelper h,boolean dodge){var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get());var p=f.player();var b=boss(h);var start=p.position();var origin=b.position();boolean[] defended={false};h.onEachTick(()->{b.setTarget(p);if(!defended[0]){p.setHealth(20);p.damageCooldownTime=0;p.setPos(start);p.setOnGround(true);p.setDeltaMovement(Vec3.ZERO);b.setPos(origin);b.setDeltaMovement(Vec3.ZERO);if(b.combatPose()==AshboundRegent.RIFT&&b.animationProgress(0)>=.97f){if(dodge)FinalProgress.data(p).won=true;defended[0]=dodge?KatanaDefense.dodge(p,5):KatanaDefense.guard(p);}}});
        h.startSequence().thenWaitUntil(()->h.assertTrue(defended[0],"Player responds to announced Rift circle")).thenWaitUntil(()->h.assertTrue(b.combatPose()==AshboundRegent.IDLE,"Rift resolves")).thenExecute(()->{try{h.assertTrue(dodge?p.getHealth()==20:p.getHealth()<20,dodge?"Dodge prevents real Rift damage":"Red Rift cannot be parried");if(dodge)h.assertTrue(p.hasEffect(MobEffects.SPEED),"Real Perfect Dodge activates unlocked Resonant Step");}finally{f.close();}}).thenSucceed();}
    static void ward(GameTestHelper h){var b=boss(h);var cow=h.spawn(EntityTypes.COW,new Vec3(5.5,2,5.5));cow.setNoAi(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);var at=cow.position();h.onEachTick(()->{b.setTarget(cow);cow.setPos(at);cow.setDeltaMovement(Vec3.ZERO);});
        h.startSequence().thenWaitUntil(()->h.assertTrue(b.combatPose()==AshboundRegent.WARD,"Regent reaches announced golden ward")).thenExecute(()->{float before=b.getHealth();b.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(cow),10);h.assertTrue(Math.abs(before-b.getHealth()-3)<.01&&b.combatPose()==AshboundRegent.WARD,"Golden ward reduces physical damage seventy percent");b.damageCooldownTime=0;b.hurtServer(h.getLevel(),h.getLevel().damageSources().magic(),5);h.assertTrue(b.combatPose()==AshboundRegent.EXPOSED,"Magic breaks ward immediately");before=b.getHealth();b.damageCooldownTime=0;b.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(cow),10);h.assertTrue(Math.abs(before-b.getHealth()-12)<.01,"Broken ward grants an actual damage opening");}).thenSucceed();}
    static void sites(GameTestHelper h,WanderingSitesFeature.Kind kind){
        var l=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);var origin=new BlockPos(8000+kind.ordinal()*64,310,8000);for(int x=(origin.getX()-16)>>4;x<=(origin.getX()+32)>>4;x++)for(int z=(origin.getZ()-16)>>4;z<=(origin.getZ()+32)>>4;z++){l.setChunkForced(x,z,true);l.getChunk(x,z);}
        for(int x=-4;x<=14;x++)for(int z=-4;z<=14;z++){l.setBlock(origin.offset(x,-1,z),Blocks.STONE.defaultBlockState(),18);for(int y=0;y<10;y++)l.setBlock(origin.offset(x,y,z),Blocks.AIR.defaultBlockState(),18);}
        var feature=l.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("wandering_"+kind.getSerializedName()))).value();var g=l.getChunkSource().getGenerator();
        if(kind==WanderingSitesFeature.Kind.BRIDGE){h.assertTrue(!feature.place(l,g,RandomSource.create(32),origin),"Bridge rejects ordinary flat terrain");for(int n=2;n<8;n++)for(int width=-1;width<=1;width++){l.setBlock(origin.offset(n,-1,width),Blocks.AIR.defaultBlockState(),18);l.setBlock(origin.offset(width,-1,n),Blocks.AIR.defaultBlockState(),18);}}
        h.assertTrue(feature.place(l,g,RandomSource.create(32),origin),"Registered native "+kind+" feature places on suitable terrain");int chests=0,lights=0;for(var p:BlockPos.betweenClosed(origin.offset(-3,0,-3),origin.offset(10,6,10))){if(l.getBlockEntity(p) instanceof ChestBlockEntity chest){chests++;h.assertTrue(chest.getLootTable()!=null,"Discovery chest has native deferred loot");}if(l.getBlockState(p).is(Blocks.GLOWSTONE))lights++;}
        h.assertTrue(kind==WanderingSitesFeature.Kind.CAMP||kind==WanderingSitesFeature.Kind.CACHE?chests==1:chests==0,"Only camps and caches contain one chest");if(kind==WanderingSitesFeature.Kind.CRYSTAL)h.assertTrue(lights>=1,"Rare crystal formation has luminous tips");h.assertTrue(!feature.place(l,g,RandomSource.create(32),origin),"Placement rejects occupied cells without replacing existing site");h.succeed();
    }
    static void structure(GameTestHelper h){var l=h.getLevel();var key=ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id("resonance_nexus"));var structure=l.registryAccess().getOrThrow(key).value();var set=l.registryAccess().getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,TheDarkDimensions.id("resonance_nexus"))).value();h.assertTrue(set.structures().getFirst().structure().value()==structure,"Final arena is locate-compatible");var g=l.getChunkSource().getGenerator();var state=l.getChunkSource().randomState();var climate=state.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());var source=new net.minecraft.world.level.biome.FixedBiomeSource(l.registryAccess().getOrThrow(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("ash_wastes"))));var context=new Structure.GenerationContext(l.registryAccess(),g,source,climate,state,l.getStructureTemplateManager(),32L,new ChunkPos(10,10),l,structure.biomes()::contains);h.assertTrue(structure.findValidGenerationPoint(context).isPresent(),"Native jigsaw generation finds Nexus in permitted dimension biome");var template=l.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id("resonance_nexus"));h.assertTrue(template.getSize().getX()==49&&template.filterBlocks(BlockPos.ZERO,new StructurePlaceSettings(),FinaleContent.ALTAR.get()).size()==1,"Template contains one altar in a forty-nine-block arena");h.succeed();}
}
