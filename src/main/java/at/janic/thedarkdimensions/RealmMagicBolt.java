package at.janic.thedarkdimensions;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Slow dodgeable projectiles, with a visible particle trail and owner-aware damage. */
public final class RealmMagicBolt extends Snowball {
    private static final EntityDataAccessor<Integer> ELEMENT=SynchedEntityData.defineId(RealmMagicBolt.class,EntityDataSerializers.INT);
    public RealmMagicBolt(EntityType<? extends RealmMagicBolt> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ELEMENT,0);}
    public int element(){return entityData.get(ELEMENT);}
    public static void fire(ServerLevel level,LivingEntity caster,LivingEntity target,int element){
        var bolt=MobExpansionContent.MAGIC_BOLT.get().create(level,EntitySpawnReason.EVENT);if(bolt==null)return;
        bolt.setOwner(caster);bolt.entityData.set(ELEMENT,element);
        bolt.setItem(new ItemStack(element==1?CaveContent.WATER_CORE.get():element==2?Items.VINE:MineralContent.CHRONUS_ORB.get()));
        bolt.setPos(caster.getEyePosition().add(0,-.2,0));bolt.setNoGravity(true);
        Vec3 direction=target.getEyePosition().subtract(bolt.position());bolt.shoot(direction.x,direction.y,direction.z,.65f,0);
        level.addFreshEntity(bolt);caster.playSound(SoundEvents.EVOKER_CAST_SPELL,.55f,element==1?1.2f:.9f);
    }
    @Override public void tick(){
        super.tick();
        if(level() instanceof ServerLevel level){
            level.sendParticles(element()==1?ParticleTypes.SPLASH:element()==2?ParticleTypes.HAPPY_VILLAGER:ParticleTypes.REVERSE_PORTAL,getX(),getY(),getZ(),2,.04,.04,.04,0);
            if(tickCount>80)discard();
        }
    }
    @Override protected void onHitEntity(EntityHitResult hit){
        if(level() instanceof ServerLevel level&&hit.getEntity() instanceof LivingEntity victim&&victim!=getOwner()){
            if(victim instanceof Player p&&(p.isCreative()||p.isSpectator()))return;
            if(victim.hurtServer(level,damageSources().thrown(this,getOwner()),element()==2?2:3)){
                if(element()==0)victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,80,0),getOwner());
                if(element()==2)victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,50,1),getOwner());
            }
        }
    }
    @Override protected void addAdditionalSaveData(ValueOutput output){super.addAdditionalSaveData(output);output.putInt("element",element());}
    @Override protected void readAdditionalSaveData(ValueInput input){super.readAdditionalSaveData(input);entityData.set(ELEMENT,Math.clamp(input.getIntOr("element",0),0,2));}
}
