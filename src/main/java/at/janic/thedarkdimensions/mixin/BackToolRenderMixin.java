package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.client.BackToolLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class BackToolRenderMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
    private void darkdimensions$backModel(Avatar avatar,AvatarRenderState state,float partial,CallbackInfo ci){if(avatar instanceof Player p)BackToolLayer.extract(p,state);else state.setRenderData(BackToolLayer.ITEM,null);}
}
