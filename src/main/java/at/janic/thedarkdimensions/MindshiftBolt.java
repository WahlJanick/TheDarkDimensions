package at.janic.thedarkdimensions;

import at.janic.thedarkdimensions.mixin.ShulkerBulletTargetAccess;
import net.minecraft.core.Direction;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.EntityHitResult;

/** Actual native Shulker flight/model, with a separate saved entity type and no levitation callback. */
public final class MindshiftBolt extends ShulkerBullet {
    private int age;
    public MindshiftBolt(EntityType<? extends MindshiftBolt> type,Level level){super(type,level);setNoGravity(true);}
    public void launch(ServerPlayer owner,LivingEntity target){setOwner(owner);if(target!=null){var access=(ShulkerBulletTargetAccess)(Object)this;access.darkdimensions$setTarget(EntityReference.<Entity>of(target));access.darkdimensions$selectDirection(Direction.Axis.Y,target);}}
    @Override public void checkDespawn(){}
    @Override public void tick(){if(!level().isClientSide()&&(!(getOwner() instanceof ServerPlayer p)||!p.isAlive()||p.level()!=level()||distanceToSqr(p)>32*32||++age>80)){discard();return;}super.tick();}
    @Override protected void onHitEntity(EntityHitResult hit){
        if(level() instanceof ServerLevel level&&getOwner() instanceof ServerPlayer p&&hit.getEntity() instanceof LivingEntity victim&&Mindshift.mayHit(p,victim)){
            if(victim.hurtServer(level,damageSources().mobProjectile(this,p),Mindshift.DAMAGE)&&victim.isAlive())Mindshift.swap(p,victim);
        }else if(level() instanceof ServerLevel level&&getOwner() instanceof ServerPlayer p&&hit.getEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part&&Mindshift.mayHit(p,part.parentMob)){
            part.hurtServer(level,damageSources().mobProjectile(this,p),Mindshift.DAMAGE);
        }
        // Never call ShulkerBullet.onHitEntity: that is the vanilla levitation application.
    }
    @Override protected void addAdditionalSaveData(ValueOutput out){super.addAdditionalSaveData(out);out.putInt("MindshiftAge",age);}
    @Override protected void readAdditionalSaveData(ValueInput in){super.readAdditionalSaveData(in);age=Math.clamp(in.getIntOr("MindshiftAge",0),0,80);setNoGravity(true);}
}

