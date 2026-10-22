package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

final class MagicTests {
    private static ServerPlayer player(GameTestHelper h){var p=(ServerPlayer)h.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);var c=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(c);h.getLevel().getServer().getPlayerList().placeNewPlayer(c,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));var pos=h.absoluteVec(new Vec3(4.5,2,4.5));p.teleportTo(pos.x,pos.y,pos.z);return p;}
    static void skills(GameTestHelper h){
        var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var b=new ItemStack(MagicContent.CHRONUS_BOOK.get());var d=new MagicData();d.elements=1<<MagicElement.CHRONUS.ordinal();d.save(b);p.setItemInHand(InteractionHand.MAIN_HAND,b);
        var menu=new SpellbookMenu(1,p.getInventory(),0);int dash=MagicSkill.CHRONUS_DASH.ordinal(),slow=dash+1;
        h.assertTrue(!menu.clickMenuButton(p,100+dash),"No learning without ingot");p.getInventory().setItem(1,new ItemStack(SpellContent.INFUSED_INGOT.get(),3));
        h.assertTrue(!menu.clickMenuButton(p,100+slow)&&!menu.clickMenuButton(p,100),"Predecessor and locked element are checked");
        h.assertTrue(menu.clickMenuButton(p,100+dash)&&p.getInventory().getItem(1).getCount()==2,"One ingot learns skill");
        h.assertTrue(!menu.clickMenuButton(p,100+dash)&&p.getInventory().getItem(1).getCount()==2,"Duplicate learning cannot consume ingot");
        d=MagicData.read(p.getMainHandItem());d.uses[dash]=8;d.save(p.getMainHandItem());h.assertTrue(menu.clickMenuButton(p,100+slow),"Leveled predecessor unlocks next skill");
        h.assertTrue(menu.clickMenuButton(p,1000+slow*5+4)&&menu.clickMenuButton(p,204),"Five slots assign and select");d=MagicData.read(p.getMainHandItem());h.assertTrue(d.slots[4]==slow&&d.selected==4,"Assignment saved to real book");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MagicContent.CHRONUS_BOOK.get()));h.assertTrue(!menu.stillValid(p)&&!menu.clickMenuButton(p,100+dash+2),"Swapped book invalidates captured GUI");h.succeed();
    }
    static void master(GameTestHelper h){
        var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var m=new SpelltableMenu(0,p.getInventory());var book=new ItemStack(MagicContent.MASTER_BOOK.get());
        var d=new MagicData();d.elements=1<<MagicElement.FIRE.ordinal();d.learned=1;d.slots[0]=0;d.uses[0]=40;d.save(book);
        m.ingredients.setItem(0,book);m.ingredients.setItem(1,new ItemStack(MagicContent.CHRONUS_CORE.get()));h.assertTrue(m.clickMenuButton(p,0),"Master unlocks second element");var out=m.result.getItem(0);d=MagicData.read(out);h.assertTrue(d.elements==9&&d.level(0)==4&&d.slots[0]==0,"Master retains existing progress and slots");
        m.result.setItem(0,ItemStack.EMPTY);m.ingredients.setItem(0,out);m.ingredients.setItem(1,new ItemStack(MagicContent.CHRONUS_CORE.get()));h.assertTrue(!m.clickMenuButton(p,0)&&!m.ingredients.getItem(1).isEmpty(),"Duplicate element rejected without cost");
        var c=new ItemStack(CaveContent.SPELL_CORE.get());var e=new ItemStack(MineralContent.ESSENTIALIUM.get());var ingot=new ItemStack(SpellContent.INFUSED_INGOT.get());
        var input=CraftingInput.of(3,3,List.of(e,c,e,c,new ItemStack(SpellContent.SPELL_BOOK.get()),c,ingot,c,ingot));var r=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());h.assertTrue(r.isPresent()&&r.get().value().assemble(input).is(MagicContent.MASTER_BOOK)&&MagicData.read(r.get().value().assemble(input)).elements==0,"Four cores, Essentialium, Chronus ingots and blank book craft an unbound master");
        for(var pair:List.of(List.of(CaveContent.SPELL_CORE.get(),MineralContent.CHRONUS_ORB.get(),MagicContent.CHRONUS_CORE.get()),List.of(KatanaContent.SOUL_CORE.get(),Items.ENDER_PEARL,MagicContent.ENDER_CORE.get()))){var in=CraftingInput.of(2,1,List.of(new ItemStack(pair.get(0)),new ItemStack(pair.get(1))));var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,in,h.getLevel());h.assertTrue(found.isPresent()&&found.get().value().assemble(in).is(pair.get(2)),"New core crafting recipe");}
        h.succeed();
    }
    static void persistence(GameTestHelper h){
        var b=new ItemStack(MagicContent.MASTER_BOOK.get());var d=new MagicData();d.elements=255;d.learned=0xffffff;d.selected=4;d.slots[4]=11;d.uses[11]=70;d.save(b);
        var ops=h.getLevel().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);var json=ItemStack.CODEC.encodeStart(ops,b).getOrThrow();var copy=ItemStack.CODEC.parse(ops,json).getOrThrow();var load=MagicData.read(copy);h.assertTrue(load.identity.equals(d.identity)&&load.slots[4]==11&&load.level(11)==5,"Book progress survives save/load and transfer");
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,copy,t->{t.putInt("magic_slot_4",9999);t.putInt("magic_selected",999);t.putInt("magic_use_11",9999);t.putInt("magic_elements",1);});load=MagicData.read(copy);h.assertTrue(load.selected==4&&load.slots[4]==-1&&!load.knows(11)&&load.uses[11]==70,"Corrupt indices and levels sanitized");
        var old=MagicData.read(new ItemStack(SpellContent.WATER_BOOK.get()));h.assertTrue(old.knows(3)&&old.slots[0]==3&&old.level(3)==1,"Legacy book keeps its basic spell");h.succeed();
    }
    static void casting(GameTestHelper h){
        var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var b=new ItemStack(SpellContent.WATER_BOOK.get());p.setItemInHand(InteractionHand.MAIN_HAND,b);p.setHealth(10);
        h.assertTrue(MagicCasting.cast(p,0),"Learned selected skill casts");var d=MagicData.read(p.getMainHandItem());h.assertTrue(d.uses[3]==1&&p.getHealth()==16,"Successful cast advances use counter");
        h.assertTrue(!MagicCasting.cast(p,0)&&MagicData.read(p.getMainHandItem()).uses[3]==1,"Cooldown cast does not advance");
        h.assertTrue(p.getCooldowns().isOnCooldown(new ItemStack(MagicContent.MASTER_BOOK.get()))&&p.getCooldowns().isOnCooldown(new ItemStack(MagicContent.CHRONUS_BOOK.get())),"Cooldown shared across all books");
        for(int n=0;n<24;n++){var x=new MagicData();x.elements=255;x.learned=1<<n;for(int u:new int[]{0,8,20,40,70}){x.uses[n]=u;h.assertTrue(x.level(n)==(u==0?1:u==8?2:u==20?3:u==40?4:5),"Each spell reaches five levels");}}
        for(int i=1;i<75;i++){p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(new ItemStack(SpellContent.FIRE_BOOK.get())));h.assertTrue(MagicCasting.cast(p,0),"Repeated successful casts advance skill");}
        h.assertTrue(MagicData.read(p.getMainHandItem()).level(3)==5&&MagicData.read(p.getMainHandItem()).uses[3]==70,"Actual spell usage caps at level five");
        h.succeed();
    }
    static void chronus(GameTestHelper h){
        var p=player(h);try{
            p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(MagicContent.CHRONUS_BOOK.get()));var start=p.position();
            for(int y=0;y<3;y++)h.setBlock(4,2+y,6,Blocks.STONE);
            h.assertTrue(MagicCasting.cast(p,0),"Dash moves toward free path");h.assertTrue(p.position().distanceTo(start)>.4&&p.getZ()<h.absolutePos(new BlockPos(4,2,6)).getZ(),"Dash stops before wall");
        }finally{h.getLevel().getServer().getPlayerList().remove(p);}h.succeed();
    }
    static void rewind(GameTestHelper h){
        var p=player(h);p.setNoGravity(true);var start=p.position();var book=new ItemStack(MagicContent.CHRONUS_BOOK.get());var data=new MagicData();data.elements=8;data.learned=1<<11;data.slots[0]=11;data.save(book);p.setItemInHand(InteractionHand.MAIN_HAND,book);
        MagicCasting.record(p);h.assertTrue(!MagicCasting.cast(p,0)&&MagicData.read(p.getMainHandItem()).uses[11]==0,"Rewind requires real position history");
        h.runAfterDelay(65,()->{try{
            p.teleportTo(start.x+2,start.y,start.z);h.assertTrue(MagicCasting.cast(p,0)&&p.position().distanceTo(start)<.01,"Rewind returns to position three seconds earlier");
            h.assertTrue(MagicData.read(p.getMainHandItem()).uses[11]==1,"Successful rewind advances skill");
            p.getCooldowns().removeCooldown(p.getCooldowns().getCooldownGroup(new ItemStack(SpellContent.FIRE_BOOK.get())));
            p.setHealth(0);MagicCasting.record(p);p.setHealth(20);h.assertTrue(!MagicCasting.cast(p,0),"Death clears rewind history");
        }finally{h.getLevel().getServer().getPlayerList().remove(p);}h.succeed();});
    }
    static void curios(GameTestHelper h){
        if(!net.neoforged.fml.ModList.get().isLoaded("curios")){h.succeed();return;}
        var p=player(h);try{
            CuriosMagic.set(p,new ItemStack(SpellContent.WATER_BOOK.get()));h.assertTrue(CuriosMagic.get(p).is(SpellContent.WATER_BOOK)&&MagicBooks.active(p)==2,"Registered Curios spellbook slot accepts book");
            p.setHealth(10);h.assertTrue(MagicCasting.cast(p,2)&&p.getHealth()==16,"Equipped book casts without holding");h.assertTrue(MagicData.read(CuriosMagic.get(p)).uses[3]==1,"Curios writeback preserves skill XP");
            var menu=new SpellbookMenu(1,p.getInventory(),2);h.assertTrue(menu.stillValid(p),"Curios book opens stable skill tree");
        }finally{h.getLevel().getServer().getPlayerList().remove(p);}h.succeed();
    }
}
