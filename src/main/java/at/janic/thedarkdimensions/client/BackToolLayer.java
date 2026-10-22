package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.BackEquipment;
import at.janic.thedarkdimensions.TheDarkDimensions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;

/** Render the actual item model, including other mods' tools and authored 3D katanas. */
public final class BackToolLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
    public static final ContextKey<ItemStackRenderState> ITEM=new ContextKey<>(TheDarkDimensions.id("back_tool"));
    public BackToolLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);}
    public static void extract(Player p,AvatarRenderState state){
        var stack=BackEquipment.stored(p);if(stack.isEmpty()){state.setRenderData(ITEM,null);return;}
        var item=new ItemStackRenderState();Minecraft.getInstance().getItemModelResolver().updateForLiving(item,stack,ItemDisplayContext.FIXED,p);
        state.setRenderData(ITEM,item);
    }
    @Override public void submit(PoseStack pose,SubmitNodeCollector nodes,int light,AvatarRenderState state,float yaw,float pitch){
        var item=state.getRenderData(ITEM);if(item==null||item.isEmpty()||state.isInvisible||state.isSpectator)return;
        pose.pushPose();getParentModel().body.translateAndRotate(pose);pose.translate(.08,.34,.28);pose.rotateDegrees(Axis.YP,180);pose.rotateDegrees(Axis.ZP,40);pose.scale(1.35f,1.35f,1.35f);
        item.submit(pose,nodes,light,OverlayTexture.NO_OVERLAY,state.outlineColor);pose.popPose();
    }
}
