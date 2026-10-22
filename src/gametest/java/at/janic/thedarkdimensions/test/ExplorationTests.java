package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

final class ExplorationTests {
    static void recipes(GameTestHelper h){
        var e=ItemStack.EMPTY;var f=new ItemStack(ExplorationContent.CHRONUS_FRAGMENT.get());
        var input=CraftingInput.of(3,3,List.of(e,new ItemStack(Items.GLASS),e,f,new ItemStack(SpellContent.INFUSED_INGOT.get()),f,e,new ItemStack(TheDarkDimensions.IMPROVED_STICK.get()),e));
        var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(ExplorationContent.HOURGLASS),"Fragments craft the Chronus focus");
        var book=CraftingInput.of(3,1,List.of(new ItemStack(Items.FEATHER),new ItemStack(TheDarkDimensions.ASHES.get()),new ItemStack(Items.BOOK)));
        var r=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,book,h.getLevel());
        h.assertTrue(r.isPresent()&&r.get().value().assemble(book).is(ExplorationContent.BESTIARY),"Native Bestiary has a shapeless recipe");
        var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ExplorationContent.HOURGLASS.get()));
        h.assertTrue(PerkContent.cooldown(p,100,MagicSkill.REWIND)==85&&PerkContent.cooldown(p,100,MagicSkill.FIRE_BOLT)==100,"Hourglass affects only Chronus skills");
        if(net.neoforged.fml.ModList.get().isLoaded("trinkets_updated")){
            var inv=eu.pb4.trinkets.api.TrinketsApi.getAttachment(p).getInventory(TrinketsMagic.FOCUS_SLOT);var slot=inv.getOrCreateSlotAccess(0);
            h.assertTrue(inv.slotType().validatorCheck(new ItemStack(ExplorationContent.HOURGLASS.get()),slot,p),"Trinkets focus accepts Hourglass");
            slot.set(new ItemStack(PerkContent.ASTRAL_PRISM.get()));h.assertTrue(PerkContent.cooldown(p,100,MagicSkill.REWIND)==85,"Chronus focus does not stack with Prism");
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);slot.set(new ItemStack(ExplorationContent.HOURGLASS.get()));h.assertTrue(PerkContent.cooldown(p,100,MagicSkill.CHRONUS_DASH)==85,"Equipped focus works without holding it");
        }h.succeed();
    }
    static void bestiary(GameTestHelper h){
        var p=(net.minecraft.server.level.ServerPlayer)h.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var c=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(c);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(c,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        try{
            var book=new ItemStack(ExplorationContent.BESTIARY.get());Bestiary.refresh(p,book);
            h.assertTrue(book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==Bestiary.SPECIES.size()+1,"Unseen creatures do not expose names or loot");
            Bestiary.discover(p,"chronus_mage");Bestiary.observeDrops(p,"chronus_mage",List.of("thedarkdimensions:chronus_orb"),true);Bestiary.refresh(p,book);
            var data=p.getData(Bestiary.RECORDS);h.assertTrue(data.entries.get("chronus_mage").kills()==1&&data.entries.get("chronus_mage").drops().equals(List.of("thedarkdimensions:chronus_orb")),"Only observed drop IDs and actual kill count are recorded");
            var encoded=Bestiary.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,data).getOrThrow();var loaded=Bestiary.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow();
            h.assertTrue(loaded.entries.equals(data.entries)&&loaded.entries!=data.entries,"Persistent records round-trip independently");
            var ops=p.level().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            var saved=ItemStack.CODEC.encodeStart(ops,book).getOrThrow();h.assertTrue(ItemStack.CODEC.parse(ops,saved).getOrThrow().get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size()==book.get(DataComponents.WRITTEN_BOOK_CONTENT).pages().size(),"Native written book pages serialize");
            var keeper=h.spawn(ExplorationContent.KEEPER.get(),new BlockPos(4,2,4));keeper.setNoAi(true);keeper.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),100);
            var record=data.entries.get("sanctum_keeper");h.assertTrue(record!=null&&record.kills()==1&&record.drops().contains("thedarkdimensions:chronus_fragment"),"Real death event records actual Keeper loot and kill");
            var defeated=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("exploration/keeper_defeated"));h.assertTrue(defeated!=null&&p.getAdvancements().getOrStartProgress(defeated).isDone(),"Actual Keeper defeat awards its progression challenge");
            for(String species:Bestiary.SPECIES)Bestiary.discover(p,species);
            var a=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("exploration/bestiary_complete"));h.assertTrue(a!=null&&p.getAdvancements().getOrStartProgress(a).isDone(),"Discovering every creature awards advancement");
            p.setItemInHand(InteractionHand.MAIN_HAND,book);h.assertTrue(ExplorationContent.BESTIARY.get().use(p.level(),p,InteractionHand.MAIN_HAND).consumesAction(),"Bestiary opens through native book interaction");
        }finally{h.getLevel().getServer().getPlayerList().remove(p);}h.succeed();
    }
    static void structures(GameTestHelper h){
        var l=h.getLevel();var origin=h.absolutePos(new BlockPos(0,30,-80));
        int cx=origin.getX()>>4,cz=origin.getZ()>>4;
        for(int x=cx;x<=cx+5;x++)for(int z=cz;z<=cz+2;z++)l.setChunkForced(x,z,true);
        for(String name:List.of("chronus_sanctum","sunken_archive")){
            var t=l.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id(name));var o=name.equals("chronus_sanctum")?origin:origin.offset(64,0,0);
            h.assertTrue(t.placeInWorld(l,o,o,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),net.minecraft.util.RandomSource.create(263),2),"Native structure template places: "+name);
            var chest=l.getBlockEntity(o.offset(name.equals("chronus_sanctum")?15:3,name.equals("chronus_sanctum")?1:4,name.equals("chronus_sanctum")?15:11));
            h.assertTrue(chest instanceof net.minecraft.world.level.block.entity.ChestBlockEntity&&((net.minecraft.world.level.block.entity.ChestBlockEntity)chest).getLootTable().identifier().equals(TheDarkDimensions.id("chests/"+name)),"Structure chest preserves loot table");
            var lectern=(net.minecraft.world.level.block.entity.LecternBlockEntity)l.getBlockEntity(o.offset(name.equals("chronus_sanctum")?6:7,name.equals("chronus_sanctum")?1:4,name.equals("chronus_sanctum")?14:12));
            h.assertTrue(lectern!=null&&lectern.getBook().has(DataComponents.WRITTEN_BOOK_CONTENT),"Structure lore loads as a real readable book");
            var key=ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id(name));var structure=l.registryAccess().getOrThrow(key).value();
            var set=l.registryAccess().getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,TheDarkDimensions.id(name))).value();h.assertTrue(set.structures().getFirst().structure().value()==structure,"Native structure set supports locating");
            var g=l.getChunkSource().getGenerator();var state=l.getChunkSource().randomState();var climate=state.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
            var source=new net.minecraft.world.level.biome.FixedBiomeSource(l.registryAccess().getOrThrow(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id(name.equals("chronus_sanctum")?"ash_wastes":"water_cave"))));
            var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(l.registryAccess(),g,source,climate,state,l.getStructureTemplateManager(),263L,new net.minecraft.world.level.ChunkPos(10,10),l,structure.biomes()::contains);
            var point=structure.findValidGenerationPoint(context);h.assertTrue(point.isPresent()&&!point.get().getPiecesBuilder().isEmpty(),"Native jigsaw produces structure in its permitted biome");
        }
        h.assertTrue(l.getBlockState(origin.offset(12,1,10)).is(Blocks.IRON_DOOR),"Crypt chamber is sealed");
        var dispenser=(net.minecraft.world.level.block.entity.DispenserBlockEntity)l.getBlockEntity(origin.offset(1,2,11));h.assertTrue(dispenser.getItem(0).getCount()==6,"Ruin trap is loaded");
        h.runAfterDelay(30,()->l.setBlock(origin.offset(3,2,11),l.getBlockState(origin.offset(3,2,11)).setValue(PressurePlateBlock.POWERED,true),3));
        h.runAfterDelay(50,()->{
            var keepers=l.getEntitiesOfClass(ExplorationContent.SanctumKeeper.class,new AABB(Vec3.atLowerCornerOf(origin),Vec3.atLowerCornerOf(origin.offset(27,16,23))));
            h.assertTrue(keepers.size()==1&&keepers.getFirst().isPersistenceRequired()&&keepers.getFirst().getMaxHealth()==44,"Sanctum has one persistent stronger Keeper; found "+keepers.size());
            var sentinels=l.getEntitiesOfClass(RealmMobs.DrownedSentinel.class,new AABB(Vec3.atLowerCornerOf(origin.offset(64,0,0)),Vec3.atLowerCornerOf(origin.offset(79,9,15))));h.assertTrue(sentinels.size()==2,"Two sentinels guard the Archive; found "+sentinels.size());
            h.assertTrue(dispenser.getItem(0).getCount()<6,"Pressure-plate trap really fires");for(int x=cx;x<=cx+5;x++)for(int z=cz;z<=cz+2;z++)l.setChunkForced(x,z,false);h.succeed();
        });
    }
    static void loot(GameTestHelper h){
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(h.getLevel()).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,Vec3.ZERO).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        var tables=h.getLevel().getServer().reloadableRegistries();var sanctum=tables.getLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("chests/chronus_sanctum")));var archive=tables.getLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("chests/sunken_archive")));int cores=0;
        for(int i=0;i<400;i++){
            h.assertTrue(sanctum.getRandomItems(params,net.minecraft.util.RandomSource.create(i)).stream().anyMatch(s->s.is(ExplorationContent.CHRONUS_FRAGMENT)),"Sanctum chests guarantee fragments");
            if(archive.getRandomItems(params,net.minecraft.util.RandomSource.create(i)).stream().anyMatch(s->s.is(CaveContent.WATER_CORE)))cores++;
        }h.assertTrue(cores>35&&cores<125,"Water Core chest loot remains uncommon: "+cores);h.succeed();
    }
    static void keeper(GameTestHelper h){
        var keeper=h.spawn(ExplorationContent.KEEPER.get(),new BlockPos(2,2,4));keeper.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var cow=h.spawn(EntityTypes.COW,new BlockPos(9,2,4));cow.setNoAi(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);keeper.setTarget(cow);
        h.onEachTick(()->keeper.setTarget(cow));
        h.runAfterDelay(150,()->{var effect=cow.getEffect(net.minecraft.world.effect.MobEffects.SLOWNESS);h.assertTrue(effect!=null&&effect.getAmplifier()==1,"Keeper releases its telegraphed stronger time field");h.succeed();});
    }
}
