package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class Mindshift {
    public static final int COOLDOWN=100;public static final float DAMAGE=5;
    public static final DeferredHolder<net.neoforged.neoforge.attachment.AttachmentType<?>,net.neoforged.neoforge.attachment.AttachmentType<Long>> READY=DevilContent.ATTACHMENTS.register("mindshift_ready",()->net.neoforged.neoforge.attachment.AttachmentType.builder(()->0L).serialize(com.mojang.serialization.Codec.LONG.fieldOf("ready")).copyOnDeath().sync(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());
    public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("mindshift_swap_immune"));
    public static final DeferredHolder<EntityType<?>,EntityType<MindshiftBolt>> BOLT=TheDarkDimensions.ENTITIES.register("mindshift_bolt",()->EntityType.Builder.<MindshiftBolt>of(MindshiftBolt::new,MobCategory.MISC).sized(.3125f,.3125f).clientTrackingRange(8).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("mindshift_bolt"))));
    public static void init(){BOLT.getId();READY.getId();}
    public static long remaining(ServerPlayer p){return Math.max(0,p.getData(READY)-p.level().getServer().overworld().getGameTime());}
    public static boolean mayHit(ServerPlayer owner,LivingEntity victim){return owner!=victim&&owner.isAlive()&&!owner.isSpectator()&&owner.level()==victim.level()&&victim.isAlive()&&!victim.isSpectator()&&!victim.isAlliedTo(owner)&&!(victim instanceof Player p&&(p.isCreative()||!owner.canHarmPlayer(p)))&&!(victim instanceof OwnableEntity tame&&tame.getOwner()==owner);}
    private static LivingEntity aim(ServerPlayer p){var start=p.getEyePosition();var end=start.add(p.getLookAngle().scale(24));double nearest=p.pick(24,1,false).getLocation().distanceToSqr(start);LivingEntity best=null;
        for(var e:p.level().getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(1),v->mayHit(p,v))){var at=e.getBoundingBox().inflate(.25).clip(start,end);if(at.isPresent()&&at.get().distanceToSqr(start)<nearest){nearest=at.get().distanceToSqr(start);best=e;}}return best;
    }
    public static boolean cast(ServerPlayer p){
        var stack=p.getMainHandItem();if(!stack.is(RelicKatanas.MINDSHIFTER)||!p.isAlive()||p.isSpectator()||p.containerMenu!=p.inventoryMenu||KatanaDefense.dodging(p))return false;
        long remaining=remaining(p);if(remaining>0){p.sendSystemMessage(Component.translatable("combat.thedarkdimensions.mindshift_cooldown",(remaining+19)/20),true);return false;}
        var bolt=BOLT.get().create(p.level(),EntitySpawnReason.EVENT);if(bolt==null)return false;var look=p.getLookAngle();bolt.setPos(p.getEyePosition().add(look.scale(.5)));bolt.launch(p,aim(p));bolt.setDeltaMovement(look.scale(.45));if(!p.level().addFreshEntity(bolt))return false;
        p.stopUsingItem();p.setData(READY,p.level().getServer().overworld().getGameTime()+COOLDOWN);if(!p.isCreative())stack.hurtAndBreak(3,p,EquipmentSlot.MAINHAND);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,SwingAnimation.DEFAULT,true);
        p.level().playSound(null,p.getX(),p.getY(),p.getZ(),SoundEvents.SHULKER_SHOOT,SoundSource.PLAYERS,.65f,1.2f);p.level().sendParticles(ParticleTypes.REVERSE_PORTAL,bolt.getX(),bolt.getY(),bolt.getZ(),12,.2,.2,.2,.025);visual(p,6);return true;
    }
    public static void visual(ServerPlayer p,int kind){net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new DefenseNetwork.Visual(p.getId(),kind,0));}
    private static boolean safe(ServerLevel level,LivingEntity e,LivingEntity other,Vec3 dest){
        var box=e.getBoundingBox().move(dest.subtract(e.position()));if(box.minY<level.getMinY()||box.maxY>=level.getMaxY()||!level.getWorldBorder().isWithinBounds(box))return false;
        var min=BlockPos.containing(box.minX,box.minY-.1,box.minZ);var max=BlockPos.containing(box.maxX,box.maxY,box.maxZ);
        for(var pos:BlockPos.betweenClosed(min,max)){if(!level.hasChunkAt(pos))return false;var state=level.getBlockState(pos);if(!state.getFluidState().isEmpty()||state.is(BlockTags.FIRE)||state.is(Blocks.CACTUS)||state.is(Blocks.MAGMA_BLOCK)||state.is(Blocks.POWDER_SNOW)||state.is(Blocks.SWEET_BERRY_BUSH)||state.is(Blocks.WITHER_ROSE)||state.is(BlockTags.PORTALS)||state.is(BlockTags.CAMPFIRES)&&state.getValue(net.minecraft.world.level.block.CampfireBlock.LIT))return false;}
        if(level.getBlockCollisions(e,box).iterator().hasNext()||!level.getBlockCollisions(e,box.move(0,-.08,0)).iterator().hasNext())return false;
        return level.getEntities(e,box,v->v!=other&&v.isAlive()&&v.canBeCollidedWith(e)).isEmpty();
    }
    private static boolean allowed(LivingEntity e,ServerLevel l,Vec3 dest){var event=new net.neoforged.neoforge.event.entity.EntityTeleportEvent(e,l,dest.x,dest.y,dest.z);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);return !event.isCanceled()&&event.getTarget().distanceToSqr(dest)<1e-8;}
    public static boolean swap(ServerPlayer p,LivingEntity victim){
        if(!mayHit(p,victim)||p.isPassenger()||p.isVehicle()||victim.isPassenger()||victim.isVehicle()||victim.typeHolder().is(IMMUNE)||p.distanceToSqr(victim)>32*32)return false;
        if(victim instanceof at.janic.thedarkdimensions.finale.AshboundRegent regent&&!regent.arena().contains(p.position()))return false;
        var level=p.level();var a=p.position();var b=victim.position();if(!safe(level,p,victim,b)||!safe(level,victim,p,a)||!allowed(p,level,b)||!allowed(victim,level,a))return false;
        // Recheck after cancellable hooks, then move both on the same server tick.
        if(!p.position().equals(a)||!victim.position().equals(b)||!safe(level,p,victim,b)||!safe(level,victim,p,a))return false;
        p.teleportTo(b.x,b.y,b.z);victim.teleportTo(a.x,a.y,a.z);p.setDeltaMovement(Vec3.ZERO);victim.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();victim.resetFallDistance();
        if(victim instanceof Mob mob)mob.getNavigation().stop();
        for(var at:List.of(a,b)){level.sendParticles(ParticleTypes.PORTAL,at.x,at.y+1,at.z,26,.35,.7,.35,.15);level.sendParticles(ParticleTypes.END_ROD,at.x,at.y+1,at.z,8,.25,.6,.25,.025);level.playSound(null,at.x,at.y,at.z,SoundEvents.ENDERMAN_TELEPORT,SoundSource.PLAYERS,.55f,1.25f);}visual(p,7);return true;
    }
}


