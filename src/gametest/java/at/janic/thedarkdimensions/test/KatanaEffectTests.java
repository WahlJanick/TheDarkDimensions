package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;

final class KatanaEffectTests {
    static void impacts(GameTestHelper h) {
        var attacker=h.makeMockPlayer(GameType.SURVIVAL);
        var enemy=h.spawn(EntityTypes.ZOMBIE,new BlockPos(4,2,4));enemy.setNoAi(true);
        KatanaContent.FROSTGLEAM.get().hurtEnemy(new ItemStack(KatanaContent.FROSTGLEAM.get()),enemy,attacker);
        h.assertTrue(enemy.hasEffect(KatanaEffects.FROSTED)&&enemy.getEffect(KatanaEffects.FROSTED).getDuration()==80,"Frost hit applies four seconds of Frosted");
        h.assertTrue(enemy.getAttributeValue(Attributes.MOVEMENT_SPEED)<0.05,"Frosted slows movement by 80 percent");
        float before=enemy.getHealth();KatanaEffects.FROSTED.get().applyEffectTick(h.getLevel(),enemy,0);
        h.assertTrue(enemy.getHealth()<before,"Frosted inflicts light damage");
        KatanaContent.BLOODGRIEF.get().hurtEnemy(new ItemStack(KatanaContent.BLOODGRIEF.get()),enemy,attacker);
        h.assertTrue(enemy.getEffect(KatanaEffects.BLOOD).getDuration()==100,"Blood hit applies five seconds of bleeding");
        KatanaContent.SOULEATER.get().hurtEnemy(new ItemStack(KatanaContent.SOULEATER.get()),enemy,attacker);
        h.assertTrue(enemy.getEffect(KatanaEffects.SOULLESS).getDuration()==60,"Souleater applies three seconds of disorientation");
        enemy.setTarget(attacker);KatanaEffects.SOULLESS.get().applyEffectTick(h.getLevel(),enemy,0);
        h.assertTrue(enemy.getTarget()==null,"Disorientation interrupts mob targeting");h.succeed();
    }
    static void bloodKills(GameTestHelper h) {
        var enemy=h.spawn(EntityTypes.ZOMBIE,new BlockPos(4,2,4));enemy.setNoAi(true);enemy.setHealth(1);
        KatanaEffects.BLOOD.get().applyEffectTick(h.getLevel(),enemy,0);
        h.assertTrue(enemy.isDeadOrDying(),"Bleeding kills at one health unlike poison");h.succeed();
    }
    static void milk(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,100));
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,100));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.MILK_BUCKET));player.startUsingItem(InteractionHand.MAIN_HAND);
        player.getItemInHand(InteractionHand.MAIN_HAND).finishUsingItem(h.getLevel(),player);
        h.assertTrue(player.hasEffect(KatanaEffects.BLOOD),"Actual milk consumption cannot cure blood");
        h.assertTrue(!player.hasEffect(MobEffects.SLOWNESS),"Milk still cures ordinary effects");player.stopUsingItem();
        var enemy=h.spawn(EntityTypes.ZOMBIE,new BlockPos(4,2,4));enemy.setNoAi(true);enemy.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,3));
        h.runAfterDelay(5,() -> {h.assertTrue(!enemy.hasEffect(KatanaEffects.BLOOD),"Bleeding expires naturally");h.succeed();});
    }
}
