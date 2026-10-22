package at.janic.thedarkdimensions;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public final class ThunderHammerItem extends Item {
    private final boolean awakened;
    public ThunderHammerItem(Properties p,boolean awakened) { super(p);this.awakened=awakened; }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(!awakened)return InteractionResult.PASS;
        var stack=player.getItemInHand(hand);
        if(player.getCooldowns().isOnCooldown(stack))return InteractionResult.FAIL;
        var from=player.getEyePosition();var direction=player.getLookAngle().scale(24);
        double limit=from.distanceToSqr(player.pick(24,1,false).getLocation());
        var target=ProjectileUtil.getEntityHitResult(player,from,from.add(direction),player.getBoundingBox().expandTowards(direction).inflate(1),e -> e instanceof LivingEntity&&e.isAlive(),limit);
        if(target==null)return InteractionResult.FAIL;
        if(level instanceof ServerLevel server) {
            var bolt=EntityTypes.LIGHTNING_BOLT.create(server,EntitySpawnReason.EVENT);
            if(bolt==null)return InteractionResult.FAIL;
            bolt.setPos(target.getEntity().position());bolt.setVisualOnly(true);server.addFreshEntity(bolt);
            target.getEntity().hurtServer(server,server.damageSources().lightningBolt(),8);
            player.getCooldowns().addCooldown(new ItemStack(ArmoryContent.AWAKENED_HAMMER.get()),100);
            player.getCooldowns().addCooldown(new ItemStack(SpellContent.LIGHTNING_BOOK.get()),100);
        }
        return InteractionResult.SUCCESS;
    }
}
