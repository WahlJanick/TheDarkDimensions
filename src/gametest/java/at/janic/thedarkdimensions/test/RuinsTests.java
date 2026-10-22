package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import net.minecraft.server.level.ServerPlayer;

final class RuinsTests {
    private static ServerPlayer player(GameTestHelper h){
        var p=(ServerPlayer)h.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var c=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(c);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(c,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        var pos=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,2,4)));p.teleportTo(pos.x,pos.y,pos.z);return p;
    }
    private static void remove(GameTestHelper h,ServerPlayer p){if(PocketDimensions.isPocket(p.level()))PocketDimensions.leave(p);h.getLevel().getServer().getPlayerList().remove(p);}
    static void recipes(GameTestHelper h){
        var key=new ItemStack(SolarContent.POCKET_KEY.get());key.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My key"));
        var input=new SmithingRecipeInput(new ItemStack(RuinsContent.POCKET_UPGRADE.get()),key,new ItemStack(Items.NETHERITE_BLOCK));
        var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel());
        h.assertTrue(found.isPresent(),"Upgrade template + key + Netherite block smithing");var result=found.get().value().assemble(input);
        h.assertTrue(result.is(RuinsContent.IMPROVED_KEY)&&result.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString().equals("My key"),"Upgraded key retains name");
        var menu=new net.minecraft.world.inventory.SmithingMenu(0,h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL).getInventory());
        h.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(RuinsContent.POCKET_UPGRADE.get())),"Smithing interface accepts new template");
        var bad=new SmithingRecipeInput(new ItemStack(RuinsContent.POCKET_UPGRADE.get()),key,new ItemStack(Items.NETHERITE_INGOT));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,bad,h.getLevel()).isEmpty(),"Upgrade requires a full block");
        var e=ItemStack.EMPTY;var iron=new ItemStack(Items.IRON_INGOT);var gold=new ItemStack(Items.GOLD_INGOT);var pearl=new ItemStack(Items.ENDER_PEARL);
        var foreign=CraftingInput.of(3,3,List.of(e,iron,e,e,iron,e,gold,pearl,gold));
        var r=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,foreign,h.getLevel());
        h.assertTrue(r.isPresent()&&r.get().value().assemble(foreign).is(RuinsContent.FOREIGN_KEY),"Cheaper foreign key uses iron, gold and pearl");
        var obsidian=new ItemStack(Items.OBSIDIAN);var anchor=CraftingInput.of(3,3,List.of(e,obsidian,e,obsidian,pearl,obsidian,e,obsidian,e));
        var a=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,anchor,h.getLevel());
        h.assertTrue(a.isPresent()&&a.get().value().assemble(anchor).is(RuinsContent.RETURN_ANCHOR_ITEM),"Anchor crafting recipe");h.succeed();
    }
    static void large(GameTestHelper h){
        var p=player(h);try{
            var back=p.position();h.assertTrue(PocketDimensions.toggle(p),"Normal room opens");var normal=PocketDimensions.data(p).get(p.getUUID());
            var marker=new BlockPos(normal.originX()+1,64,normal.originZ()+1);p.level().setBlock(marker,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
            h.assertTrue(PocketDimensions.toggle(p,true),"Improved key switches to separate room");
            h.assertTrue(p.level().dimension().equals(PocketDimensions.LARGE_LEVEL),"Upgraded room has a separate level");
            var large=PocketDimensions.data(p).get(p.getUUID(),true);int air=0;
            for(int x=0;x<20;x++)for(int y=64;y<84;y++)for(int z=0;z<20;z++)if(p.level().getBlockState(new BlockPos(large.originX()+x,y,large.originZ()+z)).isAir())air++;
            h.assertTrue(air==8000&&large.size()==20,"Exactly 20 x 20 x 20 usable blocks");
            h.assertTrue(h.getLevel().getServer().getLevel(PocketDimensions.LEVEL).getBlockState(marker).is(Blocks.DIAMOND_BLOCK),"Upgrade preserves smaller-room construction");
            var tag=PocketDimensions.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,PocketDimensions.data(p)).getOrThrow();var loaded=PocketDimensions.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,tag).getOrThrow();
            h.assertTrue(loaded.get(p.getUUID(),true).equals(large)&&loaded.visit(p.getUUID()).large(),"Room tier and visit survive restart");
            h.assertTrue(PocketDimensions.leave(p)&&p.position().distanceTo(back)<.01&&p.level()==h.getLevel(),"Switching rooms preserves outside-world return point");
            h.assertTrue(PocketDimensions.toggle(p)&&p.level().getBlockState(marker).is(Blocks.DIAMOND_BLOCK),"Normal key still returns to original room");
            var legacy=com.google.gson.JsonParser.parseString("{\"rooms\":[{\"owner\":\""+p.getUUID()+"\",\"slot\":0,\"created\":true,\"dimension\":\"minecraft:overworld\",\"x\":0,\"y\":64,\"z\":0,\"yaw\":0,\"pitch\":0}]}");
            var old=PocketDimensions.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,legacy).getOrThrow();
            h.assertTrue(old.get(p.getUUID()).size()==10&&old.visit(p.getUUID())==null,"Old save format migrates without resizing");
        }finally{remove(h,p);}h.succeed();
    }
    static void visitors(GameTestHelper h){
        var owner=player(h);var guest=player(h);boolean ownerRemoved=false;try{
            var ownerBack=owner.position();var guestBack=ownerBack.add(2,0,0);guest.teleportTo(guestBack.x,guestBack.y,guestBack.z);
            h.assertTrue(PocketDimensions.toggle(owner,true),"Owner creates large room");var room=PocketDimensions.current(owner);var anchor=new BlockPos(room.originX()+2,64,room.originZ()+2);
            owner.level().setBlock(anchor,RuinsContent.RETURN_ANCHOR.get().defaultBlockState(),3);var foreign=new ItemStack(RuinsContent.FOREIGN_KEY.get());owner.setItemInHand(InteractionHand.MAIN_HAND,foreign);
            var hit=new BlockHitResult(Vec3.atCenterOf(anchor),Direction.UP,anchor,false);
            h.assertTrue(owner.level().getBlockState(anchor).useItemOn(foreign,owner.level(),owner,InteractionHand.MAIN_HAND,hit).consumesAction()&&ForeignKeyItem.owner(foreign).equals(owner.getUUID())&&ForeignKeyItem.large(foreign),"Anchor links key to actual room owner and tier");
            var ops=owner.level().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            var encodedKey=ItemStack.CODEC.encodeStart(ops,foreign).getOrThrow();var loadedKey=ItemStack.CODEC.parse(ops,encodedKey).getOrThrow();
            h.assertTrue(ForeignKeyItem.owner(loadedKey).equals(owner.getUUID())&&ForeignKeyItem.large(loadedKey),"Foreign key binding survives item serialization");
            var keptOwnerRoom=PocketDimensions.data(owner).get(owner.getUUID(),true);guest.setItemInHand(InteractionHand.MAIN_HAND,foreign.copy());
            h.assertTrue(RuinsContent.FOREIGN_KEY.get().use(guest.level(),guest,InteractionHand.MAIN_HAND).consumesAction(),"Foreign key enters friend's room");
            h.assertTrue(PocketDimensions.current(guest).owner().equals(owner.getUUID().toString())&&PocketDimensions.data(owner).get(owner.getUUID(),true).equals(keptOwnerRoom),"Guest does not overwrite owner data");
            var snapshot=PocketDimensions.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,PocketDimensions.data(owner)).getOrThrow();
            var restored=PocketDimensions.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,snapshot).getOrThrow();h.assertTrue(restored.visit(guest.getUUID()).owner().equals(owner.getUUID().toString()),"Guest session survives logout/restart");
            guest.teleportTo(room.originX()+31,64,room.originZ()+1);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(guest));
            h.assertTrue(guest.level()==owner.level()&&PocketDimensions.current(guest).contains(guest.position()),"Guest is contained in the visited room, not their own");
            guest.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.assertTrue(guest.level().getBlockState(anchor).useItemOn(ItemStack.EMPTY,guest.level(),guest,InteractionHand.MAIN_HAND,hit)==net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND,"Empty hand invokes anchor interaction");
            h.assertTrue(guest.level().getBlockState(anchor).useWithoutItem(guest.level(),guest,hit).consumesAction()&&guest.level()==h.getLevel()&&guest.position().distanceTo(guestBack)<.01,"Anchor returns guest to their own original position without a key");
            h.assertTrue(PocketDimensions.leave(owner)&&owner.position().distanceTo(ownerBack)<.01,"Owner keeps independent return point");
            h.getLevel().getServer().getPlayerList().remove(owner);ownerRemoved=true;
            h.assertTrue(PocketDimensions.enter(guest,owner.getUUID(),true,false),"Room remains accessible while owner is offline");
            h.assertTrue(!PocketDimensions.enter(guest,UUID.randomUUID(),true,false),"Foreign key never creates a missing owner's room");
        }finally{remove(h,guest);if(!ownerRemoved)remove(h,owner);}h.succeed();
    }
    static void ruins(GameTestHelper h){
        var l=h.getLevel();var template=l.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id("mummy_ruins"));var origin=h.absolutePos(new BlockPos(0,30,-40));
        int cx=origin.getX()>>4,cz=origin.getZ()>>4;
        for(int x=cx;x<=cx+1;x++)for(int z=cz;z<=cz+1;z++)l.setChunkForced(x,z,true);
        for(var pos:BlockPos.betweenClosed(origin.offset(-1,-1,-1),origin.offset(11,9,11)))l.setBlock(pos,Blocks.SANDSTONE.defaultBlockState(),2);
        h.assertTrue(template.getSize().equals(new net.minecraft.core.Vec3i(11,9,11)),"Small buried chamber dimensions");
        h.assertTrue(template.placeInWorld(l,origin,origin,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),net.minecraft.util.RandomSource.create(263),2),"Ruins template places with entities and block data");
        for(int x:new int[]{2,8}){
            var chest=l.getBlockEntity(origin.offset(x,1,8));h.assertTrue(chest instanceof net.minecraft.world.level.block.entity.ChestBlockEntity,"Two burial loot chests");
            h.assertTrue(((net.minecraft.world.level.block.entity.ChestBlockEntity)chest).getLootTable().equals(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("chests/mummy_ruins"))),"Chests use ruin loot");
        }
        h.assertTrue(l.getBlockState(origin.offset(7,0,6)).is(Blocks.TNT)&&l.getBlockState(origin.offset(7,1,6)).is(Blocks.STONE_PRESSURE_PLATE),"Explosive pressure-plate trap");
        var dispenser=(net.minecraft.world.level.block.entity.DispenserBlockEntity)l.getBlockEntity(origin.offset(1,1,6));h.assertTrue(dispenser!=null&&dispenser.getItem(0).is(Items.ARROW)&&dispenser.getItem(0).getCount()==8,"Arrow trap loads real inventory");
        
        var structure=l.registryAccess().getOrThrow(ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id("mummy_ruins"))).value();h.assertTrue(structure.biomes().contains(l.registryAccess().getOrThrow(net.minecraft.world.level.biome.Biomes.DESERT)),"Structure generation registered for desert");
        var structureSet=l.registryAccess().getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,TheDarkDimensions.id("mummy_ruins"))).value();h.assertTrue(structureSet.structures().getFirst().structure().value()==structure,"Structure placement set supports /locate");
        var generator=l.getChunkSource().getGenerator();var randomState=l.getChunkSource().randomState();
        var climate=randomState.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
        var context=new net.minecraft.world.level.levelgen.structure.Structure.GenerationContext(l.registryAccess(),generator,generator.getBiomeSource(),climate,randomState,l.getStructureTemplateManager(),263L,new net.minecraft.world.level.ChunkPos(10,10),l,structure.biomes()::contains);
        var stub=structure.findValidGenerationPoint(context);
        h.assertTrue(stub.isPresent()&&!stub.get().getPiecesBuilder().isEmpty(),"Actual jigsaw generation produces a burial chamber");
        var box=stub.get().getPiecesBuilder().getBoundingBox();
        int surface=generator.getFirstFreeHeight(stub.get().position().getX(),stub.get().position().getZ(),net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,l,randomState);
        h.assertTrue(box.minY()<surface&&box.maxY()>=surface,"Chamber sits below surface with exposed entrance: "+box+" / "+surface);
        h.assertTrue(l.getBlockState(origin.offset(1,1,6)).getValue(DispenserBlock.FACING)==Direction.EAST,"Arrow trap aims across the chamber");
        h.runAfterDelay(30,()->l.setBlock(origin.offset(3,1,6),l.getBlockState(origin.offset(3,1,6)).setValue(PressurePlateBlock.POWERED,true),3));
        h.runAfterDelay(50,() -> {
            var mobs=l.getEntitiesOfClass(Mummy.class,new AABB(Vec3.atLowerCornerOf(origin),Vec3.atLowerCornerOf(origin.offset(11,9,11))));
            h.assertTrue(dispenser.getItem(0).getCount()<8,"Pressure plate wiring actually fires an arrow");
            h.assertTrue(mobs.size()==2&&mobs.stream().allMatch(net.minecraft.world.entity.Mob::isPersistenceRequired),"Two persistent Mummies inhabit tomb");
            for(int x=cx;x<=cx+1;x++)for(int z=cz;z<=cz+1;z++)l.setChunkForced(x,z,false);
            h.succeed();
        });
    }
    static void loot(GameTestHelper h){
        var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("chests/mummy_ruins")));
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(h.getLevel()).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,Vec3.ZERO).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CHEST);
        int templates=0,essences=0;
        for(int i=0;i<2000;i++){
            var drops=table.getRandomItems(params,net.minecraft.util.RandomSource.create(i));
            h.assertTrue(drops.stream().anyMatch(s -> s.is(TheDarkDimensions.ASHES))&&drops.stream().anyMatch(s -> s.is(RuinsContent.WEATHERED_PAPYRUS)),"Every chest supplies Ashes and dimensional clue");
            if(drops.stream().anyMatch(s -> s.is(RuinsContent.POCKET_UPGRADE)))templates++;
            if(drops.stream().anyMatch(s -> s.is(SolarContent.SOLAR_ESSENCE)))essences++;
        }
        h.assertTrue(templates>30&&templates<95&&essences>65&&essences<145,"Templates and Solar Essence stay rare: "+templates+", "+essences);h.succeed();
    }
}
