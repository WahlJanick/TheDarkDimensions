package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;

final class AshfallComboTests {
    static void resources(GameTestHelper h){
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);
        var bee=CaveContent.GLOWING_BEE.get().create(realm,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        Ashfall.stop(realm);
        var dry=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(bee,bee.damageSources().generic(),dry,false));
        h.assertTrue(dry.isEmpty(),"No bonus honey in dry weather");
        Ashfall.start(realm);
        var wet=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(bee,bee.damageSources().generic(),wet,false));
        h.assertTrue(wet.size()==1&&wet.getFirst().getItem().is(CaveContent.GLOWING_HONEY),"Ashfall guarantees an additional honey drop");
        var outside=CaveContent.GLOWING_BEE.get().create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        var other=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(outside,outside.damageSources().generic(),other,false));
        h.assertTrue(other.isEmpty(),"Bonus resources stay in the dimension");Ashfall.stop(realm);h.succeed();
    }
    static void weather(GameTestHelper h){
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);
        Ashfall.start(realm);h.assertTrue(Ashfall.active(realm)&&!Ashfall.active(h.getLevel()),"Ashfall stays in Burned Realm");
        var data=Ashfall.data(realm);long now=realm.getGameTime();
        h.assertTrue(data.end-now>=2400&&data.end-now<=4800&&data.next-data.end>=9600&&data.next-data.end<=18000,"Duration and schedule ranges");
        var restored=Ashfall.Data.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,Ashfall.Data.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,data).getOrThrow()).getOrThrow();
        h.assertTrue(restored.end==data.end&&restored.next==data.next,"Weather survives serialization");
        var pos=new BlockPos(2600,220,2600);realm.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
        var crystal=CaveContent.WATER_CRYSTAL.get().defaultBlockState();realm.setBlock(pos,crystal,3);
        Ashfall.updateChunk(realm,pos.getX()>>4,pos.getZ()>>4,true);
        h.assertTrue(realm.getBlockState(pos).getLightEmission()==10,"Water crystal rises from light 6 to 10");
        Ashfall.stop(realm);Ashfall.updateChunk(realm,pos.getX()>>4,pos.getZ()>>4,false);
        h.assertTrue(!Ashfall.active(realm)&&realm.getBlockState(pos).getLightEmission()==6,"Weather and crystal light return to normal");h.succeed();
    }
    private static void phase(net.minecraft.world.entity.player.Player p,int index){
        if(net.neoforged.fml.ModList.get().isLoaded("bettercombat"))try{Class.forName("net.bettercombat.logic.PlayerAttackProperties").getMethod("setComboCount",int.class).invoke(p,index);}catch(Exception ex){throw new IllegalStateException(ex);}
    }
    static void combos(GameTestHelper h){
        var user=h.makeMockPlayer(GameType.SURVIVAL);var target=h.spawn(EntityTypes.ZOMBIE,new BlockPos(4,2,4));target.setNoAi(true);target.setHealth(20);
        var blade=new ItemStack(KatanaContent.BLOODGRIEF.get());user.setItemInHand(InteractionHand.MAIN_HAND,blade);phase(user,0);
        blade.hurtEnemy(target,user);h.assertTrue(target.getEffect(KatanaEffects.BLOOD).getDuration()==100,"First blood strike uses baseline");
        h.runAfterDelay(2,() -> {phase(user,1);blade.hurtEnemy(target,user);blade.hurtEnemy(target,user);h.assertTrue(target.getEffect(KatanaEffects.BLOOD).getDuration()<=100,"A cleave does not consume two combo steps");});
        h.runAfterDelay(4,() -> {phase(user,2);blade.hurtEnemy(target,user);h.assertTrue(target.getEffect(KatanaEffects.BLOOD).getDuration()==160,"Third strike extends blood to eight seconds");});
        h.runAfterDelay(6,() -> {
            var frost=new ItemStack(KatanaContent.FROSTGLEAM.get());user.setItemInHand(InteractionHand.MAIN_HAND,frost);phase(user,0);frost.hurtEnemy(target,user);
            h.assertTrue(target.getEffect(KatanaEffects.FROSTED).getAmplifier()==0,"Changing weapon resets combo");
        });
        h.runAfterDelay(8,() -> {phase(user,1);user.getMainHandItem().hurtEnemy(target,user);});
        h.runAfterDelay(10,() -> {phase(user,2);user.getMainHandItem().hurtEnemy(target,user);h.assertTrue(target.getEffect(KatanaEffects.FROSTED).getAmplifier()==1,"Frost finisher roots briefly");});
        h.runAfterDelay(54,() -> {phase(user,0);user.getMainHandItem().hurtEnemy(target,user);h.assertTrue(target.getEffect(KatanaEffects.FROSTED).getAmplifier()==0,"Idle combo expires");h.succeed();});
    }
    static void chain(GameTestHelper h){
        var user=h.makeMockPlayer(GameType.SURVIVAL);var origin=h.spawn(EntityTypes.ZOMBIE,new BlockPos(3,2,3));
        var second=h.spawn(EntityTypes.ZOMBIE,new BlockPos(5,2,3));var third=h.spawn(EntityTypes.ZOMBIE,new BlockPos(7,2,3));
        var animal=h.spawn(EntityTypes.COW,new BlockPos(4,2,3));
        for(var entity:java.util.List.of(origin,second,third,animal)){entity.setNoAi(true);entity.setHealth(20);}
        h.assertTrue(KatanaCombos.chain(h.getLevel(),origin,user)==2,"Chain reaches two additional enemies");
        h.assertTrue(second.getHealth()<20&&third.getHealth()<20&&animal.getHealth()==animal.getMaxHealth()&&origin.getHealth()==20,"Chain damage is bounded and spares passive mobs");h.succeed();
    }
    static void compat(GameTestHelper h){
        if(net.neoforged.fml.ModList.get().isLoaded("bettercombat"))try{
            try(var stream=net.neoforged.fml.ModList.get().getModFileById("bettercombat").getFile().getContents().openFile("assets/bettercombat/player_animations/two_handed_slash_horizontal_right.json")){
                var animation=KatanaFinisherAnimation.create(stream);
                var bones=(java.util.Map<?,?>)animation.getClass().getMethod("boneAnimations").invoke(animation);
                try(var originalStream=net.neoforged.fml.ModList.get().getModFileById("bettercombat").getFile().getContents().openFile("assets/bettercombat/player_animations/two_handed_slash_horizontal_right.json")){
                    var original=(java.util.Map<?,?>)Class.forName("com.zigythebird.playeranimcore.loading.UniversalAnimLoader").getMethod("loadAnimations",java.io.InputStream.class).invoke(null,originalStream);
                    var base=original.values().iterator().next();var baseBones=(java.util.Map<?,?>)base.getClass().getMethod("boneAnimations").invoke(base);
                    h.assertTrue(bones.size()>=3&&bones.keySet().equals(baseBones.keySet()),"Runtime finisher retains all native grip and animation channels: "+bones.keySet());
                }
            }
            try(var stream=AshfallComboTests.class.getResourceAsStream("/assets/thedarkdimensions/player_animations/katana_finisher.json")){
                var parsed=(java.util.Map<?,?>)Class.forName("com.zigythebird.playeranimcore.loading.UniversalAnimLoader").getMethod("loadAnimations",java.io.InputStream.class).invoke(null,stream);
                h.assertTrue(parsed.containsKey("katana_finisher"),"Player Animation Library parses custom finisher");
                var animation=parsed.get("katana_finisher");
                var bones=(java.util.Map<?,?>)animation.getClass().getMethod("boneAnimations").invoke(animation);
                h.assertTrue(bones.size()>=3,"Finisher animates both arms and torso: "+bones.keySet());
            }
            var registry=Class.forName("net.bettercombat.logic.WeaponRegistry");
            var lookup=registry.getMethod("getAttributes",ItemStack.class);
            for(var item:java.util.List.of(KatanaContent.FROSTGLEAM.get(),KatanaContent.BLOODGRIEF.get(),KatanaContent.SOULEATER.get(),SolarContent.STORMVEIL.get(),RelicKatanas.BASIC.get(),RelicKatanas.MINDSHIFTER.get(),RelicKatanas.HELLSBLADE.get())){
                var attr=lookup.invoke(null,new ItemStack(item));h.assertTrue(attr!=null,"Better Combat loads katana attributes");
                var attacks=(Object[])attr.getClass().getMethod("attacks").invoke(attr);
                h.assertTrue(attacks.length==3,"Three attacks registered");
                h.assertTrue(attacks[2].getClass().getMethod("animation").invoke(attacks[2]).equals("thedarkdimensions:katana_finisher"),"Custom animation assigned to finisher");
            }
        }catch(Exception e){throw new IllegalStateException(e);}h.succeed();
    }
}
