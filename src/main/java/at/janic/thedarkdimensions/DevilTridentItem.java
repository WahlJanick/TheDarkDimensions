package at.janic.thedarkdimensions;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Uses native throwing, Loyalty, Channeling, Riptide and pickup logic. */
public final class DevilTridentItem extends TridentItem {
    public DevilTridentItem(Properties p) {super(p);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var result=super.use(level,player,hand);
        if(result.consumesAction())DevilContent.charm(player);
        return result;
    }
    @Override public boolean releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining) {
        boolean used=super.releaseUsing(stack,level,user,remaining);
        if(used)DevilContent.charm(user);
        return used;
    }
    @Override public void hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
        super.hurtEnemy(stack,target,attacker);DevilContent.impact(target,attacker);DevilContent.charm(attacker);
    }
}
