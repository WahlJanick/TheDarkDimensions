package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.KatanaDefense;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Consume a counter even when the attempted attack returns before creating a damage event. */
@Mixin(Player.class)
public abstract class KatanaCounterAttemptMixin {
    @Inject(method="attack",at=@At("HEAD"))
    private void darkDimensions$counterAttempt(Entity target,CallbackInfo ci){
        if((Object)this instanceof ServerPlayer player)KatanaDefense.attempted(player,target);
    }
    @Inject(method="attack",at=@At("RETURN"))
    private void darkDimensions$counterAttemptFinished(Entity target,CallbackInfo ci){
        if((Object)this instanceof ServerPlayer player)KatanaDefense.finishedAttempt(player);
    }
}
