package at.janic.thedarkdimensions;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
public final class FireMage extends Witch {
    public FireMage(EntityType<? extends Witch> type,Level level) { super(type,level); }
    @Override protected void customServerAiStep(ServerLevel level){super.customServerAiStep(level);setAggressive(getTarget()!=null);}
    @Override public void performRangedAttack(LivingEntity target,float power) {
        if(!(level() instanceof ServerLevel level))return;
        Vec3 from=getEyePosition().add(0,-0.2,0);
        var ball=new SmallFireball(level,this,target.getEyePosition().subtract(from).normalize());
        ball.setPos(from);level.addFreshEntity(ball);
        playSound(SoundEvents.BLAZE_SHOOT,0.8f,1);
    }
}
