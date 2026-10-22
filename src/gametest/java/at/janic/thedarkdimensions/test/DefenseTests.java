package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

final class DefenseTests {
    record Fixture(ServerPlayer player,io.netty.channel.embedded.EmbeddedChannel channel) implements AutoCloseable{
        public void close(){player.level().getServer().getPlayerList().remove(player);channel.finishAndReleaseAll();}
    }
    static Fixture player(GameTestHelper h,Item weapon){
        for(int x=1;x<15;x++)for(int z=1;z<15;z++)h.setBlock(new BlockPos(x,1,z),Blocks.STONE);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"test-mock-player"),net.minecraft.server.level.ClientInformation.createDefault()){@Override public boolean isClientAuthoritative(){return false;}};var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);var channel=new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        p.setGameMode(GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.setInvulnerableTime(0);p.damageCooldownTime=0;
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());p.hasChangedDimension();
        var pos=h.absoluteVec(new Vec3(5.5,2,5.5));p.teleportTo(pos.x,pos.y,pos.z);p.setYRot(0);p.setOnGround(true);p.setHealth(20);p.getFoodData().setFoodLevel(20);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(weapon));
        // Embedded mock connections are not ticked by ServerConnectionListener.
        h.onEachTick(()->{if(!p.isRemoved())p.doTick();});return new Fixture(p,channel);
    }
    private static net.minecraft.world.entity.animal.cow.Cow enemy(GameTestHelper h){var cow=h.spawn(EntityTypes.COW,new Vec3(5.5,2,7.5));cow.setNoAi(true);cow.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(20);cow.setHealth(20);return cow;}
    static void perfectParry(GameTestHelper h){
        try(var f=player(h,KatanaContent.FROSTGLEAM.get())){
            var p=f.player;var mummy=h.spawn(TheDarkDimensions.MUMMY.get(),new Vec3(5.5,2,7.5));mummy.setNoAi(true);
            h.assertTrue(p.getMainHandItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND).consumesAction(),"Right click starts native katana guard");
            h.assertTrue(!mummy.doHurtTarget(h.getLevel(),p)&&p.getHealth()==20&&!p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)&&!p.hasEffect(net.minecraft.world.effect.MobEffects.HUNGER),"Perfect parry prevents damage and Mummy on-hit debuffs");
            h.assertTrue(mummy.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS),"Perfect parry staggers its attacker");p.stopUsingItem();float before=mummy.getHealth();
            mummy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
            h.assertTrue(before-mummy.getHealth()>6&&mummy.getEffect(KatanaEffects.FROSTED).getAmplifier()==1,"Counter adds damage and Frostgleam theme");
            float after=mummy.getHealth();mummy.damageCooldownTime=0;mummy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
            h.assertTrue(after-mummy.getHealth()>3&&after-mummy.getHealth()<=4,"The same armored attacker cannot receive a second counter bonus within the window");
            var cow=enemy(h);cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==16,"Counter is spent exactly once");
        }h.succeed();
    }
    static void normalParry(GameTestHelper h){
        var f=player(h,KatanaContent.BLOODGRIEF.get());var p=f.player;var enemy=enemy(h);KatanaDefense.guard(p);
        h.runAfterDelay(4,()->{try{
            h.assertTrue(!p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),4)&&p.getHealth()==20,"Normal parry cancels a frontal strike outside the perfect window");
            p.stopUsingItem();enemy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(enemy.getHealth()==16,"Ordinary parry has no perfect-counter damage bonus");h.succeed();
        }finally{f.close();}});
    }
    static void normalMasterParry(GameTestHelper h){
        var f=player(h,KatanaContent.SOULEATER.get());var p=f.player;var enemy=enemy(h);
        p.getMainHandItem().enchant(h.getLevel().registryAccess().getOrThrow(KatanaEnchantments.MASTER_PARRYING),1);KatanaDefense.guard(p);
        h.runAfterDelay(4,()->{try{
            h.assertTrue(!p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),12)&&p.getHealth()==20,"Normal parry prevents the complete hit, even with Master Parrying");p.stopUsingItem();
            enemy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(enemy.getHealth()==16&&!enemy.hasEffect(KatanaEffects.SOULLESS),"Normal parry grants no first counter or themed counter effect");enemy.damageCooldownTime=0;
            enemy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(enemy.getHealth()==12,"Master Parrying grants no second counter after a normal parry");h.succeed();
        }finally{f.close();}});
    }
    static void normalClearsPerfect(GameTestHelper h){
        var f=player(h,KatanaContent.SOULEATER.get());var p=f.player;var first=enemy(h);var next=h.spawn(EntityTypes.COW,new Vec3(6.5,2,7.5));next.setNoAi(true);
        KatanaDefense.guard(p);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(first),4);p.stopUsingItem();
        h.runAfterDelay(12,()->h.assertTrue(KatanaDefense.guard(p),"Second guard starts after the recovery"));
        h.runAfterDelay(16,()->{try{
            h.assertTrue(!p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(next),8)&&p.getHealth()==20,"A later normal parry still prevents damage");p.stopUsingItem();
            first.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
            h.assertTrue(first.getHealth()==16&&!first.hasEffect(KatanaEffects.SOULLESS),"Normal parry cannot retain the earlier perfect-parry window against its original enemy");h.succeed();
        }finally{f.close();}});
    }
    static void counterMiss(GameTestHelper h){
        try(var f=player(h,KatanaContent.SOULEATER.get())){
            var p=f.player;var target=enemy(h);KatanaDefense.guard(p);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(target),4);p.stopUsingItem();
            KatanaDefense.miss(p);target.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
            h.assertTrue(target.getHealth()==16&&!target.hasEffect(KatanaEffects.SOULLESS),"A missed swing spends the counter instead of saving it for a later hit");
        }h.succeed();
    }
    static void counterFailedAttack(GameTestHelper h){
        try(var f=player(h,KatanaContent.SOULEATER.get())){
            var p=f.player;var target=enemy(h);KatanaDefense.guard(p);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(target),4);p.stopUsingItem();
            target.setPermanentlyInvulnerable(true);p.attack(target);target.setPermanentlyInvulnerable(false);
            target.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
            h.assertTrue(target.getHealth()==16&&!target.hasEffect(KatanaEffects.SOULLESS),"A real attack attempt against an invulnerable enemy still consumes the single counter");
        }h.succeed();
    }
    static void counterRealAttack(GameTestHelper h){
        try(var f=player(h,KatanaContent.SOULEATER.get())){
            var p=f.player;var target=enemy(h);KatanaDefense.guard(p);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(target),4);p.stopUsingItem();
            float before=target.getHealth();p.attack(target);float counterDamage=before-target.getHealth();
            h.assertTrue(counterDamage>3&&target.hasEffect(KatanaEffects.SOULLESS),"Actual native player attack receives the one counter bonus");
            target.damageCooldownTime=0;before=target.getHealth();p.attack(target);float nextDamage=before-target.getHealth();
            h.assertTrue(Math.abs(counterDamage-nextDamage-3)<.05,"A second actual attack in the same tick gets exactly three less damage");
        }h.succeed();
    }
    static void counterExpiry(GameTestHelper h){
        var f=player(h,KatanaContent.SOULEATER.get());var p=f.player;var target=enemy(h);KatanaDefense.guard(p);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(target),4);p.stopUsingItem();
        h.runAfterDelay(21,()->{try{
            target.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);
            h.assertTrue(target.getHealth()==16&&!target.hasEffect(KatanaEffects.SOULLESS),"Counter opportunity expires after one second");h.succeed();
        }finally{f.close();}});
    }
    static void guardAndRear(GameTestHelper h){
        var f=player(h,KatanaContent.SOULEATER.get());var p=f.player;var enemy=enemy(h);KatanaDefense.guard(p);
        h.runAfterDelay(10,()->{try{
            p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),5);h.assertTrue(Math.abs(p.getHealth()-18)<.01,"Holding too early reduces sixty percent instead of granting an endless parry");
            p.setInvulnerableTime(0);p.damageCooldownTime=0;p.setYRot(180);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),5);h.assertTrue(Math.abs(p.getHealth()-13)<.01,"Back attacks bypass the katana's frontal guard");h.succeed();
        }finally{f.close();}});
    }
    static void recovery(GameTestHelper h){
        var f=player(h,KatanaContent.FROSTGLEAM.get());var p=f.player;
        h.assertTrue(KatanaDefense.guard(p),"Initial guard starts");p.stopUsingItem();h.assertTrue(!KatanaDefense.guard(p),"Click spam cannot refresh perfect timing");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SolarContent.STORMVEIL.get()));h.assertTrue(!KatanaDefense.guard(p),"Changing katana does not reset guard recovery");
        h.runAfterDelay(13,()->{try{h.assertTrue(KatanaDefense.guard(p)&&p.getUseItem().getUseAnimation()==ItemUseAnimation.BLOCK,"Stormveil can guard after recovery");h.succeed();}finally{f.close();}});
    }
    static void heavyAndProjectile(GameTestHelper h){
        try(var f=player(h,KatanaContent.FROSTGLEAM.get())){
            var p=f.player;var heavy=h.spawn(EntityTypes.RAVAGER,new Vec3(5.5,2,8.5));heavy.setNoAi(true);KatanaDefense.guard(p);
            p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(heavy),4);h.assertTrue(p.getHealth()==16,"Red heavy attacks cannot be parried even with perfect timing");
            p.setInvulnerableTime(0);p.damageCooldownTime=0;var arrow=h.spawn(EntityTypes.ARROW,new Vec3(5.5,3,7));arrow.setNoGravity(true);arrow.setOwner(heavy);
            p.hurtServer(h.getLevel(),h.getLevel().damageSources().arrow(arrow,heavy),4);h.assertTrue(p.getHealth()==12,"Katana guard does not become a projectile shield");
        }h.succeed();
    }
    static void perfectDodge(GameTestHelper h){
        var f=player(h,KatanaContent.BLOODGRIEF.get());var p=f.player;var enemy=enemy(h);var start=p.position();
        h.assertTrue(KatanaDefense.dodge(p,5),"Right sidestep starts on safe ground");h.assertTrue(!p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),4)&&p.getHealth()==20,"Last-moment dodge evades damage");
        h.assertTrue(!KatanaDefense.dodge(p,5),"Dodge cooldown rejects spamming");
        h.runAfterDelay(9,()->{try{
            h.assertTrue(p.position().distanceTo(start)>2&&p.position().distanceTo(start)<3,"Four short steps perform a real directional dodge");
            enemy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(enemy.getHealth()==13&&enemy.hasEffect(KatanaEffects.BLOOD),"Perfect dodge opens a Bloodgrief counter");h.succeed();
        }finally{f.close();}});
    }
    static void normalDodge(GameTestHelper h){
        var f=player(h,KatanaContent.SOULEATER.get());var p=f.player;var enemy=enemy(h);KatanaDefense.dodge(p,1);
        h.runAfterDelay(4,()->h.assertTrue(!p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),4)&&p.getHealth()==20,"Ordinary dodge avoids a strike within its short protection window"));
        h.runAfterDelay(10,()->{try{
            enemy.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(enemy.getHealth()==16,"Ordinary dodge grants no perfect-counter reward");
            p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),4);h.assertTrue(p.getHealth()==16,"Protection ends after eight ticks");h.succeed();
        }finally{f.close();}});
    }
    static void dodgeValidation(GameTestHelper h){
        try(var f=player(h,KatanaContent.FROSTGLEAM.get())){
            var p=f.player;var at=p.position();h.assertTrue(!KatanaDefense.dodge(p,999)&&p.position().equals(at),"Malformed direction cannot move a player");
            h.setBlock(new BlockPos(5,2,6),Blocks.STONE);h.setBlock(new BlockPos(5,3,6),Blocks.STONE);h.assertTrue(!KatanaDefense.dodge(p,7),"Swept collision rejects a dodge into a wall");
            h.setBlock(new BlockPos(5,2,6),Blocks.AIR);h.setBlock(new BlockPos(5,3,6),Blocks.AIR);h.setBlock(new BlockPos(5,1,6),Blocks.AIR);h.assertTrue(!KatanaDefense.dodge(p,7),"Dodges cannot begin over an unsupported gap");
            p.setOnGround(false);h.assertTrue(!KatanaDefense.dodge(p,5),"Airborne dodge is rejected");p.setOnGround(true);p.getFoodData().setFoodLevel(1);h.assertTrue(!KatanaDefense.dodge(p,5),"Exhausted players cannot spam dodges");
            p.getFoodData().setFoodLevel(20);h.assertTrue(KatanaDefense.dodge(p,5),"Rejected requests never consume recovery");
            p.hurtServer(h.getLevel(),h.getLevel().damageSources().fall(),4);h.assertTrue(p.getHealth()==16,"Dodge never protects from environmental damage");
            p.setInvulnerableTime(0);p.damageCooldownTime=0;p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var enemy=enemy(h);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(enemy),4);h.assertTrue(p.getHealth()==12,"Removing katana immediately removes defense protection");
        }h.succeed();
    }
    static void animations(GameTestHelper h){
        if(net.neoforged.fml.ModList.get().isLoaded("player_animation_library"))try{
            var loader=Class.forName("com.zigythebird.playeranimcore.loading.UniversalAnimLoader").getMethod("loadAnimations",java.io.InputStream.class);
            for(String name:java.util.List.of("katana_parry","katana_dodge_forward","katana_dodge_back","katana_dodge_left","katana_dodge_right"))try(var stream=DefenseTests.class.getResourceAsStream("/assets/thedarkdimensions/player_animations/"+name+".json")){
                h.assertTrue(stream!=null&&((java.util.Map<?,?>)loader.invoke(null,stream)).containsKey(name),"Installed PAL parses restrained defense animation: "+name);
            }
        }catch(Exception e){throw new IllegalStateException(e);}h.succeed();
    }
}


