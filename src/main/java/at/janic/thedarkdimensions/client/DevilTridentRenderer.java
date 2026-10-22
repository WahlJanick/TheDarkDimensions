package at.janic.thedarkdimensions.client;
import at.janic.thedarkdimensions.DevilProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.ThrownTridentRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.AABB;

/** Native tridents keep their model; only synced Devil tridents use the supplied CEM geometry. */
public final class DevilTridentRenderer extends EntityRenderer<ThrownTrident,DevilTridentRenderer.State> {
    private final TridentModel normal;
    private final DevilTridentModel devil;
    public static final class State extends ThrownTridentRenderState {boolean isDevil;}
    public DevilTridentRenderer(EntityRendererProvider.Context context) {super(context);normal=new TridentModel(context.bakeLayer(ModelLayers.TRIDENT));devil=new DevilTridentModel(context.bakeLayer(DevilTridentModel.LAYER));}
    @Override public State createRenderState() {return new State();}
    @Override public void extractRenderState(ThrownTrident entity,State state,float partial) {
        super.extractRenderState(entity,state,partial);state.xRot=entity.getXRot(partial);state.yRot=entity.getYRot(partial);state.isFoil=entity.isFoil();state.isDevil=((DevilProjectile)entity).darkDimensions$isDevil();
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector nodes,CameraRenderState camera) {
        Model<Unit> model=state.isDevil?devil:normal;var texture=state.isDevil?DevilTridentModel.TEXTURE:TridentModel.TEXTURE;
        pose.pushPose();pose.rotateDegrees(Axis.YP,state.yRot-90);pose.rotateDegrees(Axis.ZP,state.xRot+90);
        if(state.isFoil)nodes.submitModel(model,Unit.INSTANCE,pose,RenderTypes.entitySolidGlint(texture),state.lightCoords,OverlayTexture.NO_OVERLAY,state.outlineColor);
        else nodes.submitModel(model,Unit.INSTANCE,pose,texture,state.lightCoords,OverlayTexture.NO_OVERLAY,state.outlineColor);
        pose.popPose();super.submit(state,pose,nodes,camera);
    }
    @Override protected AABB getBoundingBoxForCulling(ThrownTrident entity,float partial) {return super.getBoundingBoxForCulling(entity,partial).inflate(1.5);}
}
