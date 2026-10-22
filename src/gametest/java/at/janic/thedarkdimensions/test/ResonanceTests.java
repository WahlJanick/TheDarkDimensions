package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.resonance.*;
import at.janic.thedarkdimensions.mixin.CooldownsAccess;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.phys.*;

final class ResonanceTests {
    private static int count(net.minecraft.world.entity.player.Player p,Item item){return p.getInventory().getNonEquipmentItems().stream().filter(s->s.is(item)).mapToInt(ItemStack::getCount).sum();}
    static void recipes(GameTestHelper h){
        var manager=h.getLevel().getServer().getRecipeManager();ItemStack e=new ItemStack(MineralContent.ESSENTIALIUM.get()),r=new ItemStack(TheDarkDimensions.EMPTY_RUNESTONE.get()),m=new ItemStack(SpellContent.METALINIUM_INGOT.get()),stone=new ItemStack(RevisionContent.HARD_STONE.get());
        var grid=CraftingInput.of(3,3,List.of(e,r,e,m,new ItemStack(MineralContent.SPELLTABLE.get()),m,stone,stone,stone));var found=manager.getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel());h.assertTrue(found.isPresent()&&found.get().value().assemble(grid).is(ResonanceContent.BENCH.get().asItem()),"Bench has exact nine-slot recipe");
        List<List<Item>> additions=List.of(List.of(TheDarkDimensions.FIRE_CORE.get(),KatanaContent.SOUL_ESSENCE.get()),List.of(CaveContent.WATER_CORE.get(),KatanaContent.JAPANESE_ESSENCE.get()),List.of(CaveContent.GLOWING_HONEY.get(),MineralContent.ESSENTIALIUM.get()),List.of(MineralContent.CHRONUS_ORB.get(),SpellContent.INFUSED_INGOT.get()));
        try(var f=DefenseTests.player(h,Items.DIAMOND_SWORD)){
            var p=f.player();var container=new SimpleContainer(1);var craft=new TransientCraftingContainer(p.inventoryMenu,3,3);var slot=new ResultSlot(p,craft,container,0,0,0);
            for(int n=0;n<4;n++){
                grid=CraftingInput.of(3,1,List.of(r,new ItemStack(additions.get(n).get(0)),new ItemStack(additions.get(n).get(1))));found=manager.getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel());h.assertTrue(found.isPresent()&&found.get().value().assemble(grid).is(ResonanceContent.RUNES.get(n)),"Native rune recipe "+n);
                container.setItem(0,found.get().value().assemble(grid));ResonanceProgress.data(p).completed=0;h.assertTrue(!slot.mayPickup(p),"Server crafting output is locked before trial");ResonanceProgress.data(p).completed=n==3?7:1<<n;h.assertTrue(slot.mayPickup(p),"Matching trial unlocks actual result slot");
            }
        }h.succeed();
    }
    static void bench(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.DIAMOND_SWORD)){
            var p=f.player();var weapon=p.getMainHandItem();weapon.setDamageValue(75);weapon.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keepsake"));weapon.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),2);PerkContent.coat(weapon);net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,weapon,t->t.putString("other_mod_data","preserve"));
            var before=weapon.copy();var menu=new RunicBenchMenu(1,p.getInventory());menu.input.setItem(0,weapon.copy());menu.input.setItem(1,new ItemStack(ResonanceContent.RUNES.get(0).get(),2));h.assertTrue(menu.result.isEmpty(),"Locked player cannot bind rune");ResonanceProgress.data(p).completed=1;menu.slotsChanged(menu.input);h.assertTrue(!menu.result.isEmpty()&&menu.input.getItem(1).getCount()==2,"Preview does not consume inputs");
            int existing=count(p,Items.DIAMOND_SWORD);var out=menu.quickMoveStack(p,2);h.assertTrue(!out.isEmpty()&&AncientRunes.rune(out)==0&&PerkContent.coated(out)&&out.isEnchanted()&&out.getDamageValue()==75&&out.getHoverName().getString().equals("Keepsake"),"Shift-click bind preserves all weapon components");
            h.assertTrue(menu.input.getItem(0).isEmpty()&&menu.input.getItem(1).getCount()==1&&count(p,Items.DIAMOND_SWORD)==existing+1,"Taking preview consumes exactly one weapon and rune");h.assertTrue(menu.quickMoveStack(p,2).isEmpty(),"Repeated output click cannot duplicate weapon");
            menu.input.setItem(0,out.copy());menu.input.setItem(1,new ItemStack(ResonanceContent.RUNES.get(1).get()));ResonanceProgress.data(p).completed=7;menu.slotsChanged(menu.input);h.assertTrue(menu.result.isEmpty(),"Existing rune cannot be overwritten");
            int runeCount=count(p,ResonanceContent.RUNES.get(0).get());menu.input.setItem(1,new ItemStack(MineralContent.CHRONUS_ORB.get(),2));var clean=menu.quickMoveStack(p,2);h.assertTrue(ItemStack.isSameItemSameComponents(clean,before)&&count(p,ResonanceContent.RUNES.get(0).get())==runeCount+1&&menu.input.getItem(1).getCount()==1,"Removal restores exact weapon, returns one rune, consumes one orb");h.assertTrue(menu.quickMoveStack(p,2).isEmpty(),"Removal result cannot be taken twice");
            menu.input.setItem(0,before.copy());menu.input.setItem(1,new ItemStack(ResonanceContent.RUNES.get(0).get()));int weapons=count(p,Items.DIAMOND_SWORD);menu.removed(p);h.assertTrue(count(p,Items.DIAMOND_SWORD)==weapons+1&&menu.result.isEmpty(),"Closing returns inputs but never the preview");
        }h.succeed();
    }
    static void fullInventory(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.DIAMOND_SWORD)){
            var p=f.player();ResonanceProgress.data(p).completed=7;for(int n=0;n<36;n++)p.getInventory().setItem(n,new ItemStack(Items.COBBLESTONE,64));
            var menu=new RunicBenchMenu(1,p.getInventory());menu.input.setItem(0,AncientRunes.bind(new ItemStack(Items.IRON_AXE),1));menu.input.setItem(1,new ItemStack(MineralContent.CHRONUS_ORB.get()));h.assertTrue(menu.quickMoveStack(p,2).isEmpty()&&menu.input.getItem(1).getCount()==1,"Full inventory rejects shift-take without consuming anything");
            p.containerMenu=menu;menu.clicked(2,0,ContainerInput.PICKUP,p);h.assertTrue(!menu.getCarried().isEmpty()&&AncientRunes.rune(menu.getCarried())<0&&menu.input.isEmpty(),"Cursor take works with full inventory");
            var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,p.getBoundingBox().inflate(3));h.assertTrue(drops.stream().filter(d->d.getItem().is(ResonanceContent.RUNES.get(1))).mapToInt(d->d.getItem().getCount()).sum()==1,"Exactly one extracted rune drops when inventory is full");p.containerMenu=p.inventoryMenu;
        }h.succeed();
    }
    private static net.minecraft.world.entity.animal.cow.Cow target(GameTestHelper h,int x){var mob=h.spawn(EntityTypes.COW,new BlockPos(x,2,9));mob.setNoAi(true);mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);mob.setHealth(100);return mob;}
    static void melee(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.DIAMOND_SWORD)){
            var p=f.player();var first=target(h,3);var second=target(h,5);p.setItemInHand(InteractionHand.MAIN_HAND,AncientRunes.bind(new ItemStack(Items.DIAMOND_SWORD),0));
            first.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);second.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);h.assertTrue(first.isOnFire()&&!second.isOnFire(),"Sweeping/multiple hits spend only one Ember proc per attacker");p.setItemInHand(InteractionHand.MAIN_HAND,AncientRunes.bind(new ItemStack(Items.IRON_AXE),0));second.damageCooldownTime=0;second.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);h.assertTrue(!second.isOnFire(),"Changing weapon cannot reset rune cooldown");
            p.setItemInHand(InteractionHand.MAIN_HAND,AncientRunes.bind(new ItemStack(Items.DIAMOND_SWORD),1));p.igniteForSeconds(10);second.damageCooldownTime=0;second.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);h.assertTrue(!p.isOnFire()&&p.getHealth()==20,"Tide extinguishes without healing");p.igniteForSeconds(10);second.damageCooldownTime=0;second.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);h.assertTrue(p.isOnFire(),"Tide has eight-second attacker cooldown");p.clearFire();
            var weapon=AncientRunes.bind(new ItemStack(Items.DIAMOND_SWORD),2);PerkContent.coat(weapon);p.setItemInHand(InteractionHand.MAIN_HAND,weapon);second.damageCooldownTime=0;second.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);var effect=second.getEffect(MobEffects.SLOWNESS);h.assertTrue(second.hasEffect(MobEffects.GLOWING)&&effect!=null&&effect.getDuration()==60&&effect.getAmplifier()==0,"Radiance works with coating on same hit for native fifteen-percent Slowness");
            var untouched=target(h,7);p.setItemInHand(InteractionHand.MAIN_HAND,AncientRunes.bind(new ItemStack(Items.DIAMOND_SWORD),0));ResonanceProgress.data(p).runeReady.clear();untouched.hurtServer(h.getLevel(),h.getLevel().damageSources().magic(),2);h.assertTrue(!untouched.isOnFire(),"Spell damage cannot trigger weapon runes");
        }h.succeed();
    }
    static void chronus(GameTestHelper h){
        try(var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get())){
            var p=f.player();p.setItemInHand(InteractionHand.MAIN_HAND,AncientRunes.bind(new ItemStack(KatanaContent.FROSTGLEAM.get()),3));p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(MagicContent.CHRONUS_BOOK.get()));var access=(CooldownsAccess)p.getCooldowns();var group=p.getCooldowns().getCooldownGroup(MagicBooks.get(p,1));
            p.getCooldowns().addCooldown(MagicBooks.get(p,1),80);ResonanceProgress.data(p).lastSpell=MagicElement.FIRE.ordinal();AncientRunes.perfectDodge(p);h.assertTrue(AncientRunes.remaining(p.getCooldowns(),group)==80,"Selecting Chronus cannot reduce a previous Fire spell cooldown");
            ResonanceProgress.data(p).lastSpell=MagicElement.CHRONUS.ordinal();h.assertTrue(KatanaDefense.dodge(p,1),"Real katana dodge starts");var enemy=target(h,5);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),4);h.assertTrue(AncientRunes.remaining(p.getCooldowns(),group)==60,"Perfect Dodge hook reduces active Chronus cooldown by exactly twenty ticks");AncientRunes.perfectDodge(p);h.assertTrue(AncientRunes.remaining(p.getCooldowns(),group)==60,"Chronus rune cannot repeatedly proc within ten seconds");
            ResonanceProgress.data(p).runeReady.clear();p.getCooldowns().addCooldown(MagicBooks.get(p,1),10);AncientRunes.perfectDodge(p);h.assertTrue(AncientRunes.remaining(p.getCooldowns(),group)==0&&!p.getCooldowns().isOnCooldown(MagicBooks.get(p,1)),"Reduction clamps to zero and removes expired cooldown");
        }h.succeed();
    }
    static void persistence(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=f.player();var data=ResonanceProgress.data(p);data.completed=5;data.biomes.add("fire_cave");data.sites.put("test",new ResonanceProgress.Site(0,DarkContent.BURNED_REALM.identifier().toString(),1,2,3,true));data.runeReady.put("ember",12345L);
            var json=ResonanceProgress.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,data).getOrThrow();var restored=ResonanceProgress.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow();h.assertTrue(restored.completed==5&&restored.sites.get("test").waypoint()&&restored.runeReady.get("ember")==12345L&&restored.biomes.contains("fire_cave"),"Progress, biomes, waypoints and recovery serialize independently");
            var clone=(ServerPlayer)h.makeMockServerPlayer(GameType.SURVIVAL);clone.restoreFrom(p,false);h.assertTrue(ResonanceProgress.data(clone).completed==5&&ResonanceProgress.data(clone).sites.get("test").waypoint(),"Native respawn copies journal and unlock attachment on death");
            var site=new TrialController(new BlockPos(10,20,30),ResonanceContent.RUNESTONE.get().defaultBlockState());site.setLevel(h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM));ResonanceProgress.discover(p,site);h.assertTrue(data.sites.get(site.identity()).dimension().equals(DarkContent.BURNED_REALM.identifier().toString()),"An online participant in another dimension records the trial's dimension, not their current location");
            p.teleportTo(h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM),3600,180,3600,Set.of(),0,0,true);h.assertTrue(ResonanceProgress.data(p).completed==5&&ResonanceProgress.data(p).sites.containsKey("test"),"Dimension change retains personal journal");var book=new ItemStack(ResonanceContent.JOURNAL.get());FieldJournal.refresh(p,book);h.assertTrue(book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()>=9,"Field Journal is a native readable book with saved waypoint");
        }h.succeed();
    }
    static void rewards(GameTestHelper h){
        try(var f=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=f.player();var ledger=new TrialLedger();UUID offline=UUID.randomUUID();ledger.offer(p.getUUID(),new TrialLedger.Reward("one",0,false));ledger.offer(p.getUUID(),new TrialLedger.Reward("one",0,true));ledger.offer(offline,new TrialLedger.Reward("two",1,true));
            var encoded=TrialLedger.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,ledger).getOrThrow();ledger=TrialLedger.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow();ledger.deliver(p);int sigils=count(p,TrialKind.EMBER.sigil()),materials=count(p,SpellContent.METALINIUM_INGOT.get());h.assertTrue(sigils==1&&materials==2&&ledger.pending.containsKey(offline.toString()),"Per-player pending rewards survive reload; offline participant remains queued");
            ledger.offer(p.getUUID(),new TrialLedger.Reward("one",0,true));ledger.deliver(p);h.assertTrue(count(p,TrialKind.EMBER.sigil())==1&&count(p,SpellContent.METALINIUM_INGOT.get())==2,"Same-site reward receipt prevents replay");ledger.offer(p.getUUID(),new TrialLedger.Reward("other",0,true));ledger.deliver(p);h.assertTrue(count(p,TrialKind.EMBER.sigil())==1&&count(p,SpellContent.METALINIUM_INGOT.get())==5,"Ashfall adds materials but never an extra elemental sigil");
            ledger.offer(p.getUUID(),new TrialLedger.Reward("tide",1,false));ledger.offer(p.getUUID(),new TrialLedger.Reward("light",2,false));ledger.deliver(p);h.assertTrue(ResonanceProgress.unlocked(p,3)&&p.getData(ResonanceProgress.UNLOCKS)==7,"All three trial completions unlock and sync Chronus rune");
        }h.succeed();
    }
    private record Arena(ServerLevel level,TrialController trial,List<BlockPos> panels){}
    private static Arena arena(GameTestHelper h,TrialKind kind,int variant){
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);var origin=new BlockPos(4096+kind.ordinal()*256+variant*64,180,4096);for(int x=(origin.getX()-16)>>4;x<=(origin.getX()+48)>>4;x++)for(int z=(origin.getZ()-16)>>4;z<=(origin.getZ()+48)>>4;z++){realm.setChunkForced(x,z,true);realm.getChunk(x,z);}
        var template=realm.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id(kind.structure+"_"+variant));var settings=new StructurePlaceSettings();h.assertTrue(template.placeInWorld(realm,origin,origin,settings,net.minecraft.util.RandomSource.create(31),18),"Native trial template places");
        var controllerPos=template.filterBlocks(origin,settings,ResonanceContent.RUNESTONE.get()).getFirst().pos();var trial=(TrialController)realm.getBlockEntity(controllerPos);trial.serverTick();var panels=template.filterBlocks(origin,settings,ResonanceContent.SEAL.get()).stream().filter(b->b.state().getValue(TrialSealBlock.INDEX)>0).sorted(Comparator.comparingInt(b->b.state().getValue(TrialSealBlock.INDEX))).map(StructureTemplate.StructureBlockInfo::pos).toList();return new Arena(realm,trial,panels);
    }
    private static void move(ServerPlayer p,ServerLevel level,BlockPos pos){p.teleportTo(level,pos.getX()+.5,pos.getY()+1,pos.getZ()+.5,Set.of(),0,0,true);p.getAbilities().invulnerable=true;p.setOnGround(true);}
    private static void start(GameTestHelper h,ServerPlayer p,Arena a){move(p,a.level,a.trial.getBlockPos().offset(0,0,2));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(a.trial.kind().core(),2));a.trial.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(p.getMainHandItem().getCount()==2&&a.trial.phase()==0,"First click only previews core cost");a.trial.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(p.getMainHandItem().getCount()==1&&a.trial.phase()==1,"Second click consumes exactly one core and arms trial");}
    private static void killWave(Arena a){for(UUID id:a.trial.enemies()){var entity=a.level.getEntity(id);if(entity instanceof LivingEntity mob){mob.setNoGravity(true);mob.hurtServer(a.level,a.level.damageSources().genericKill(),10000);}}}
    static void trial(GameTestHelper h,TrialKind kind){var initial=arena(h,kind,kind.ordinal());h.runAfterDelay(10,()->runTrial(h,kind,initial));}
    private static void runTrial(GameTestHelper h,TrialKind kind,Arena initial){
        var a=initial;try(var f=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=f.player();start(h,p,a);
            if(kind!=TrialKind.RADIANCE){for(var panel:a.panels){move(p,a.level,panel.offset(0,0,1));a.trial.seal(p,panel);}h.assertTrue(a.trial.seals()==7&&a.trial.phase()==2,"Three distinct seals start combat");move(p,a.level,a.trial.getBlockPos().offset(0,0,2));for(int tick=0;tick<700&&a.trial.phase()!=3;tick++){a.trial.serverTick();killWave(a);}h.assertTrue(a.trial.wave()==(kind==TrialKind.EMBER?3:2),"All announced waves are required: "+a.trial.diagnostic());}
            else{
                int expected=a.trial.expectedPanel();var panel=a.panels.get(expected-1);move(p,a.level,panel.offset(0,0,1));a.trial.seal(p,panel);h.assertTrue(a.trial.step()==1,"First correct light symbol advances progress");
                var wrong=a.panels.get(a.trial.expectedPanel()%3);move(p,a.level,wrong.offset(0,0,1));for(int i=0;i<8;i++)a.trial.seal(p,wrong);h.assertTrue(a.trial.step()==1&&a.trial.enemies().size()<=3,"Errors preserve steps and enforce enemy cap");killWave(a);
                var tag=a.trial.saveWithFullMetadata(a.level.registryAccess());var restored=(TrialController)BlockEntity.loadStatic(a.trial.getBlockPos(),a.trial.getBlockState(),tag,a.level.registryAccess());h.assertTrue(restored!=null&&restored.step()==1&&restored.identity().equals(a.trial.identity())&&restored.enemies().equals(a.trial.enemies()),"Controller reload preserves identity, puzzle progress and tracked enemies");restored.setLevel(a.level);a.level.setBlockEntity(restored);a=new Arena(a.level,restored,a.panels);
                for(int i=0;i<4;i++){panel=a.panels.get(a.trial.expectedPanel()-1);move(p,a.level,panel.offset(0,0,1));a.trial.seal(p,panel);}for(int i=0;i<30;i++)a.trial.serverTick();
            }
            h.assertTrue(a.trial.phase()==3&&ResonanceProgress.unlocked(p,kind.ordinal())&&count(p,kind.sigil())==1,"Actual trial grants exactly one matching sigil and unlock: "+a.trial.diagnostic());int loot=count(p,SpellContent.METALINIUM_INGOT.get());a.trial.interact(p,InteractionHand.MAIN_HAND);for(int i=0;i<50;i++)a.trial.serverTick();h.assertTrue(count(p,kind.sigil())==1&&count(p,SpellContent.METALINIUM_INGOT.get())==loot,"Completed controller never starts or rewards again");
            var book=new ItemStack(ResonanceContent.JOURNAL.get());ResonanceProgress.discover(p,a.trial);h.assertTrue(!ResonanceProgress.data(p).sites.get(a.trial.identity()).waypoint(),"Discovery does not automatically mark coordinates");move(p,a.level,a.trial.getBlockPos().offset(0,0,2));FieldJournal.saveWaypoint(p,a.trial);FieldJournal.refresh(p,book);h.assertTrue(ResonanceProgress.data(p).sites.get(a.trial.identity()).waypoint(),"Journal interaction explicitly saves discovered location");
        }finally{killWave(a);}h.succeed();
    }
    static void retry(GameTestHelper h){var initial=arena(h,TrialKind.EMBER,1);h.runAfterDelay(10,()->runRetry(h,initial));}
    private static void runRetry(GameTestHelper h,Arena initial){
        var a=initial;try(var f=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=f.player();start(h,p,a);for(var panel:a.panels){move(p,a.level,panel.offset(0,0,1));a.trial.seal(p,panel);}move(p,a.level,a.trial.getBlockPos().offset(0,0,2));for(int i=0;i<65;i++)a.trial.serverTick();h.assertTrue(!a.trial.enemies().isEmpty(),"Combat wave exists before failure");
            a.trial.retry();h.assertTrue(a.trial.phase()==0&&a.trial.seals()==7&&a.trial.enemies().isEmpty()&&a.trial.wave()==0,"Retry cleans enemies without losing seals or counting incomplete wave");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);a.trial.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(a.trial.phase()==2,"Retry does not require another core");
        }finally{a.trial.retry();}h.succeed();
    }
    static void multiplayer(GameTestHelper h){var initial=arena(h,TrialKind.EMBER,2);h.runAfterDelay(10,()->runMultiplayer(h,initial));}
    private static void runMultiplayer(GameTestHelper h,Arena initial){
        var a=initial;try(var first=DefenseTests.player(h,Items.IRON_SWORD);var second=DefenseTests.player(h,Items.IRON_SWORD)){
            var p=first.player();var friend=second.player();move(friend,a.level,a.trial.getBlockPos().offset(2,0,2));start(h,p,a);
            for(var panel:a.panels){move(p,a.level,panel.offset(0,0,1));a.trial.seal(p,panel);}move(p,a.level,a.trial.getBlockPos().offset(0,0,2));for(int n=0;n<65;n++)a.trial.serverTick();h.assertTrue(a.trial.enemies().size()==3,"Two players moderately increase first wave from two to three enemies");
            var memberIds=a.trial.enemies();var tag=a.trial.saveWithFullMetadata(a.level.registryAccess());var reloaded=(TrialController)BlockEntity.loadStatic(a.trial.getBlockPos(),a.trial.getBlockState(),tag,a.level.registryAccess());h.assertTrue(reloaded!=null&&reloaded.enemies().equals(memberIds)&&reloaded.wave()==1,"Active wave UUIDs survive block-entity reload");reloaded.setLevel(a.level);a.level.setBlockEntity(reloaded);a=new Arena(a.level,reloaded,a.panels);
            a.level.getServer().getPlayerList().remove(friend);
            for(int n=0;n<700&&a.trial.phase()!=3;n++){a.trial.serverTick();killWave(a);}
            h.assertTrue(a.trial.phase()==3&&count(p,TrialKind.EMBER.sigil())==1,"Remaining player can complete the encounter: "+a.trial.diagnostic());var ledger=TrialLedger.data(a.level);h.assertTrue(ledger.pending.containsKey(friend.getUUID().toString()),"Offline participant's reward stays queued");ledger.deliver(friend);h.assertTrue(count(friend,TrialKind.EMBER.sigil())==1&&ResonanceProgress.unlocked(friend,0),"Returning participant receives identical progress and one sigil");ledger.deliver(friend);h.assertTrue(count(friend,TrialKind.EMBER.sigil())==1,"Returning again does not duplicate rewards");
        }finally{killWave(a);}h.succeed();
    }
    static void structures(GameTestHelper h){
        var l=h.getLevel();var r=l.registryAccess();var random=l.getChunkSource().randomState();var climate=random.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
        for(var kind:TrialKind.values()){
            var structure=r.getOrThrow(ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id(kind.structure))).value();h.assertTrue(structure instanceof CaveTrialStructure&&structure.biomes().size()==1,"Trial has native locate-compatible structure registry entry and matching cave scope");Set<String> plans=new HashSet<>();
            for(int v=0;v<3;v++){var t=l.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id(kind.structure+"_"+v));var settings=new StructurePlaceSettings();var panels=t.filterBlocks(BlockPos.ZERO,settings,ResonanceContent.SEAL.get());h.assertTrue(panels.stream().filter(b->b.state().getValue(TrialSealBlock.INDEX)>0).count()==3&&panels.size()==4&&t.filterBlocks(BlockPos.ZERO,settings,ResonanceContent.RUNESTONE.get()).size()==1,"Every layout has controller and three distinct panels");plans.add(panels.toString());}h.assertTrue(plans.size()==3,"All three room layouts differ");
            var source=new net.minecraft.world.level.biome.FixedBiomeSource(r.getOrThrow(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id(kind.biome))));
            for(boolean cave:List.of(false,true)){
                var generator=new FlatLevelSource(((FlatLevelSource)l.getChunkSource().getGenerator()).settings()){@Override public NoiseColumn getBaseColumn(int x,int z,LevelHeightAccessor accessor,net.minecraft.world.level.levelgen.RandomState state){BlockState[] blocks=new BlockState[128];Arrays.fill(blocks,Blocks.STONE.defaultBlockState());if(cave)for(int y=-20;y<=-12;y++)blocks[y+64]=Blocks.AIR.defaultBlockState();return new NoiseColumn(-64,blocks);}};
                var context=new Structure.GenerationContext(r,generator,source,climate,random,l.getStructureTemplateManager(),31L,new ChunkPos(10,10),l,structure.biomes()::contains);var stub=structure.findValidGenerationPoint(context);h.assertTrue(stub.isPresent()==cave,"Worldgen requires a real solid floor with three blocks of cave air");if(cave)h.assertTrue(!stub.get().getPiecesBuilder().isEmpty(),"Cave floor produces real rotated jigsaw structure pieces");
            }
        }h.succeed();
    }
}
