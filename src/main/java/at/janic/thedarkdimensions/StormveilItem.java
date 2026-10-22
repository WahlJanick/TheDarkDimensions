package at.janic.thedarkdimensions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;

public final class StormveilItem extends Item {
    public StormveilItem(Properties p) { super(p); }
    @Override public boolean supportsEnchantment(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment){return KatanaEnchantments.supports(stack,enchantment);}
    @Override public void inventoryTick(ItemStack stack,net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.Entity owner,net.minecraft.world.entity.EquipmentSlot slot){super.inventoryTick(stack,level,owner,slot);KatanaEnchantments.migrate(stack,level);}
    @Override public net.minecraft.world.InteractionResult use(net.minecraft.world.level.Level level,net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand){
        if(hand!=net.minecraft.world.InteractionHand.MAIN_HAND)return net.minecraft.world.InteractionResult.PASS;
        if(player instanceof net.minecraft.server.level.ServerPlayer server)return KatanaDefense.guard(server)?net.minecraft.world.InteractionResult.CONSUME:net.minecraft.world.InteractionResult.FAIL;
        player.startUsingItem(hand);return net.minecraft.world.InteractionResult.CONSUME;
    }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack){return ItemUseAnimation.BLOCK;}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 72000;}
    @Override public void hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
        super.hurtEnemy(stack,target,attacker);
        if(target.level() instanceof ServerLevel level) {
            boolean finisher=KatanaCombos.hit(stack,attacker,target);
            if(finisher)KatanaCombos.chain(level,target,attacker);
            target.hurtServer(level,new net.minecraft.world.damagesource.DamageSource(level.registryAccess().getOrThrow(SolarContent.STORMVEIL_ARC),attacker),1);
            level.sendParticles(SolarContent.STORMVEIL_SWEEP.get(),target.getX(),target.getY()+1,target.getZ(),1,0,0,0,0);
            level.sendParticles(SolarContent.STORMVEIL_CRIT.get(),target.getX(),target.getY()+1,target.getZ(),5,0.25,0.4,0.25,0.03);
        }
    }
}
