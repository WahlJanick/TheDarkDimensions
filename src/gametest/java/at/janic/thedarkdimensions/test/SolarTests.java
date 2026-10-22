package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;

final class SolarTests {
    static void mummy(GameTestHelper h) {
        var mummy=h.spawn(TheDarkDimensions.MUMMY.get(),new Vec3(3,2,3));mummy.setNoAi(true);
        var target=h.spawn(EntityTypes.COW,new Vec3(4,2,3));target.setNoAi(true);
        h.assertTrue(mummy.doHurtTarget(h.getLevel(),target),"Mummy melee hit succeeds");
        h.assertTrue(target.hasEffect(MobEffects.HUNGER)&&target.getEffect(MobEffects.HUNGER).getDuration()==160,"Mummy applies eight seconds of hunger");
        h.assertTrue(target.hasEffect(MobEffects.POISON)&&target.getEffect(MobEffects.POISON).getDuration()==80,"Mummy applies four seconds of poison");
        var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.THIS_ENTITY,mummy).withParameter(LootContextParams.ORIGIN,mummy.position()).withParameter(LootContextParams.DAMAGE_SOURCE,h.getLevel().damageSources().generic()).create(LootContextParamSets.ENTITY);
        var loot=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("entities/mummy")));
        int solar=0;var random=net.minecraft.util.RandomSource.create(26315);
        for(int i=0;i<10000;i++){
            var drops=loot.getRandomItems(params,random);
            h.assertTrue(drops.stream().anyMatch(s -> s.is(TheDarkDimensions.ASHES)),"Ashes retained");
            for(var s:drops)if(s.is(SolarContent.SOLAR_ESSENCE)){h.assertTrue(s.getCount()==1,"Solar drops exactly one");solar++;}
        }
        h.assertTrue(solar>=430&&solar<=570,"Solar Essence has a five-percent drop chance: "+solar);h.succeed();
    }
    static void recipes(GameTestHelper h) {
        var n=new ItemStack(Items.NETHERITE_INGOT);var g=new ItemStack(Items.GOLD_INGOT);var s=new ItemStack(SolarContent.SOLAR_ESSENCE.get());var empty=ItemStack.EMPTY;
        check(h,CraftingInput.of(3,3,List.of(empty,n,empty,empty,n,empty,g,s,g)),SolarContent.POCKET_KEY.get());
        check(h,CraftingInput.of(1,3,List.of(new ItemStack(TheDarkDimensions.FIRE_CORE.get()),new ItemStack(Items.GHAST_TEAR),new ItemStack(TheDarkDimensions.IMPROVED_STICK.get()))),RevisionContent.ASTRAL_CRYSTAL_WAND.get());
        var oldRecipe=CraftingInput.of(1,3,List.of(new ItemStack(Items.FLINT_AND_STEEL),new ItemStack(Items.END_CRYSTAL),new ItemStack(TheDarkDimensions.IMPROVED_STICK.get())));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,oldRecipe,h.getLevel()).isEmpty(),"End-Crystal recipe is removed to avoid a progression cycle");
        check(h,CraftingInput.of(3,1,List.of(s,new ItemStack(CaveContent.LIGHTNING_CORE.get()),new ItemStack(RelicKatanas.BASIC.get()))),SolarContent.STORMVEIL.get());
        var input=new SmithingRecipeInput(s,new ItemStack(Items.BLAST_FURNACE),new ItemStack(Items.COAL_BLOCK));
        var r=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel());
        h.assertTrue(r.isPresent()&&r.get().value().assemble(input).is(SolarContent.SOLAR_FORGE_ITEM),"Solar Essence and Coal Block upgrade a blast furnace");
        var menu=new net.minecraft.world.inventory.SmithingMenu(0,h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL).getInventory());
        h.assertTrue(menu.getSlot(0).mayPlace(s),"Smithing template accepts Solar Essence");h.succeed();
    }
    static void check(GameTestHelper h,CraftingInput input,Item item){
        var r=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());h.assertTrue(r.isPresent()&&r.get().value().assemble(input).is(item),"Crafting produces "+item);
    }
    static void forge(GameTestHelper h) {
        var a=new BlockPos(2,2,2);var b=new BlockPos(4,2,2);
        h.setBlock(a,Blocks.BLAST_FURNACE);h.setBlock(b,SolarContent.SOLAR_FORGE.get());
        var slow=(AbstractFurnaceBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(a));
        var fast=(AbstractFurnaceBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(b));
        for(var furnace:List.of(slow,fast)){furnace.setItem(0,new ItemStack(Items.RAW_IRON,8));furnace.setItem(1,new ItemStack(Items.COAL));}
        int fastDone=0,slowDone=0;
        for(int tick=1;tick<=250;tick++){
            for(var furnace:List.of(slow,fast))AbstractFurnaceBlockEntity.serverTick(h.getLevel(),furnace.getBlockPos(),h.getLevel().getBlockState(furnace.getBlockPos()),furnace);
            if(fastDone==0&&!fast.getItem(2).isEmpty())fastDone=tick;
            if(slowDone==0&&!slow.getItem(2).isEmpty())slowDone=tick;
            if(slowDone>0)break;
        }
        h.assertTrue(fastDone>0&&slowDone==fastDone*2,"Solar Forge smelts twice as fast: "+fastDone+" vs "+slowDone);
        h.assertTrue(fast.getItem(2).getCount()==2&&slow.getItem(2).getCount()==1,"Double throughput produces actual ingots");
        h.assertTrue(fast.getItem(1).isEmpty()&&slow.getItem(1).isEmpty(),"Both consume normal fuel");h.succeed();
    }
    static void blade(GameTestHelper h) {
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var blade=new ItemStack(SolarContent.STORMVEIL.get());
        var attrs=blade.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        h.assertTrue(attrs.compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==9,"Stormveil has nine attack damage");
        h.assertTrue(attrs.compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND)==1.5,"Stormveil has 1.5 attack speed");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,blade);new GameplayEvents().playerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        h.assertTrue(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==5,"Stormveil has two extra blocks of reach");
        var cow=h.spawn(EntityTypes.COW,new Vec3(4,2,4));cow.setNoAi(true);
        cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(player),9);
        double before=cow.getHealth();SolarContent.STORMVEIL.get().hurtEnemy(blade,cow,player);
        h.assertTrue(cow.getHealth()==before-1,"One lightning damage lands despite melee immunity frames");
        h.assertTrue(!cow.isOnFire(),"Small electric impact does not ignite target");h.succeed();
    }
    static void pocket(GameTestHelper h) {
        var player=(net.minecraft.server.level.ServerPlayer)h.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        Vec3 back=Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,2,4)));player.teleportTo(back.x,back.y,back.z);
        try {
            h.assertTrue(PocketDimensions.toggle(player),"Key enters pocket");
            h.assertTrue(player.level().dimension().equals(PocketDimensions.LEVEL),"Pocket is a separate void dimension");
            var room=PocketDimensions.data(player).get(player.getUUID());var level=player.level();int air=0;
            for(int x=0;x<10;x++)for(int y=64;y<74;y++)for(int z=0;z<10;z++)if(level.getBlockState(new BlockPos(room.originX()+x,y,room.originZ()+z)).isAir())air++;
            h.assertTrue(air==1000,"Room provides exactly 10 x 10 x 10 usable blocks");
            var marker=new BlockPos(room.originX()+1,64,room.originZ()+1);level.setBlockAndUpdate(marker,Blocks.DIAMOND_BLOCK.defaultBlockState());
            var encoded=PocketDimensions.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,PocketDimensions.data(player)).getOrThrow();
            var loaded=PocketDimensions.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow();
            h.assertTrue(loaded.get(player.getUUID()).equals(room),"Saved data roundtrip retains owner, room and return position");
            h.assertTrue(PocketDimensions.toggle(player)&&player.level()==h.getLevel()&&player.position().distanceTo(back)<0.01,"Key returns to original position");
            h.assertTrue(PocketDimensions.toggle(player)&&player.level().getBlockState(marker).is(Blocks.DIAMOND_BLOCK),"Reentering preserves player construction");
            var other=new PocketDimensions.Room(UUID.randomUUID().toString(),room.slot()+1,true,"minecraft:overworld",0,64,0,0,0);PocketDimensions.build(player.level(),other);
            h.assertTrue(!room.contains(new Vec3(other.originX()+5,64,other.originZ()+5)),"Other players receive separate rooms");
            h.assertTrue(player.level().getBlockState(new BlockPos(room.originX()-1,64,room.originZ())).getDestroySpeed(player.level(),new BlockPos(room.originX()-1,64,room.originZ()))<0,"Room walls cannot be mined");
            h.assertTrue(PocketDimensions.leave(player),"Emergency exit works");
        } finally {if(player.level().dimension().equals(PocketDimensions.LEVEL))PocketDimensions.leave(player);h.getLevel().getServer().getPlayerList().remove(player);}
        h.succeed();
    }
}
