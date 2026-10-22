package at.janic.thedarkdimensions;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
public final class GlowingBee extends Bee {
    private long lastHarvest=-600;
    public GlowingBee(EntityType<? extends Bee> type,Level level) { super(type,level); }
    @Override public boolean isCurrentlyGlowing() { return true; }
    @Override public Bee getBreedOffspring(net.minecraft.server.level.ServerLevel level,AgeableMob partner) { return CaveContent.GLOWING_BEE.get().create(level,EntitySpawnReason.BREEDING); }
    @Override public InteractionResult mobInteract(Player player,InteractionHand hand) {
        var held=player.getItemInHand(hand);
        if(!held.is(Items.GLASS_BOTTLE)||isAngry()||isBaby())return super.mobInteract(player,hand);
        if(level().getGameTime()-lastHarvest<(Ashfall.active(level())?300:600))return InteractionResult.FAIL;
        if(!level().isClientSide()) {
            player.setItemInHand(hand,ItemUtils.createFilledResult(held,player,new ItemStack(CaveContent.GLOWING_HONEY.get())));
            lastHarvest=level().getGameTime();
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out);out.putLong("LastGlowHoneyHarvest",lastHarvest); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in);lastHarvest=in.getLongOr("LastGlowHoneyHarvest",-600); }
}
