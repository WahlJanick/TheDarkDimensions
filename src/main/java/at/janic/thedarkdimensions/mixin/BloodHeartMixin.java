package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.KatanaEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class BloodHeartMixin {
    @Inject(method="extractHeart",at=@At("HEAD"),cancellable=true)
    private void darkDimensions$bloodHeart(GuiGraphicsExtractor graphics,Hud.HeartType type,int x,int y,boolean hardcore,boolean blink,boolean half,CallbackInfo ci) {
        var player=Minecraft.getInstance().player;
        if(player!=null&&player.hasEffect(KatanaEffects.BLOOD)&&type!=Hud.HeartType.CONTAINER&&type!=Hud.HeartType.ABSORBING) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,Hud.HeartType.NORMAL.getSprite(hardcore,half,blink),x,y,9,9,0xffbb182c);
            ci.cancel();
        }
    }
}
