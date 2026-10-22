package at.janic.thedarkdimensions.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.special.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;
import org.joml.Vector3fc;

public final class DevilTridentSpecialRenderer implements NoDataSpecialModelRenderer {
    private final DevilTridentModel model;
    public DevilTridentSpecialRenderer(DevilTridentModel model) {this.model=model;}
    @Override public void submit(PoseStack pose,SubmitNodeCollector nodes,int light,int overlay,boolean foil,int outline) {
        if(foil)nodes.submitModel(model,Unit.INSTANCE,pose,RenderTypes.entitySolidGlint(DevilTridentModel.TEXTURE),light,overlay,outline);
        else nodes.submitModel(model,Unit.INSTANCE,pose,DevilTridentModel.TEXTURE,light,overlay,outline);
    }
    @Override public void getExtents(Consumer<Vector3fc> output) {model.root().getExtentsForGui(new PoseStack(),output);}
    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC=MapCodec.unit(new Unbaked());
        @Override public MapCodec<Unbaked> type() {return CODEC;}
        @Override public DevilTridentSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {return new DevilTridentSpecialRenderer(new DevilTridentModel(context.entityModelSet().bakeLayer(DevilTridentModel.LAYER)));}
    }
}
