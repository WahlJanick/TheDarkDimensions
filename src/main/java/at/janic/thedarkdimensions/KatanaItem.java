package at.janic.thedarkdimensions;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

/** Native sword hit callback runs after a successful strike, for players and mobs. */
public final class KatanaItem extends Item {
    private final Holder<MobEffect> effect;
    private final int duration;
    public KatanaItem(Properties properties,Holder<MobEffect> effect,int duration) { super(properties);this.effect=effect;this.duration=duration; }
    @Override public boolean supportsEnchantment(ItemStack stack,Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment){return KatanaEnchantments.supports(stack,enchantment);}
    @Override public void inventoryTick(ItemStack stack,net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.Entity owner,net.minecraft.world.entity.EquipmentSlot slot){super.inventoryTick(stack,level,owner,slot);KatanaEnchantments.migrate(stack,level);}
    @Override public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand){
        if(hand!=net.minecraft.world.InteractionHand.MAIN_HAND)return net.minecraft.world.InteractionResult.PASS;
        if(player.getMainHandItem().is(RelicKatanas.MINDSHIFTER)&&!player.isShiftKeyDown()){
            if(player instanceof net.minecraft.server.level.ServerPlayer server)return Mindshift.cast(server)?net.minecraft.world.InteractionResult.CONSUME:net.minecraft.world.InteractionResult.FAIL;
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if(player instanceof net.minecraft.server.level.ServerPlayer server)return KatanaDefense.guard(server)?net.minecraft.world.InteractionResult.CONSUME:net.minecraft.world.InteractionResult.FAIL;
        player.startUsingItem(hand);return net.minecraft.world.InteractionResult.CONSUME;
    }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.BLOCK;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 72000;}
    @Override public void hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
        super.hurtEnemy(stack,target,attacker);
        if(!target.level().isClientSide()) {
            boolean finisher=KatanaCombos.hit(stack,attacker,target);
            int ticks=finisher?(effect==KatanaEffects.BLOOD?160:effect==KatanaEffects.SOULLESS?90:40):duration;
            int amplifier=finisher&&effect==KatanaEffects.FROSTED?1:0;
            if(effect!=null&&target.isAlive())target.addEffect(new MobEffectInstance(effect,ticks,amplifier),attacker);
            RelicKatanas.hit(stack,target,attacker,finisher);
        }
    }
}
