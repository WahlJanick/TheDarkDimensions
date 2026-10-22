package at.janic.thedarkdimensions.client;
import at.janic.thedarkdimensions.TheDarkDimensions;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
/** Generated from the artist CEM: exact box sizes and texture offsets, adapted axes. */
public final class DevilTridentModel extends Model<Unit> {
    public static final Identifier TEXTURE=TheDarkDimensions.id("textures/entity/devils_trident.png");
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(TheDarkDimensions.id("devils_trident"),"main");
    public DevilTridentModel(ModelPart root) {super(root,RenderTypes::entitySolid);}
    public static LayerDefinition createLayer() {
        var mesh=new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("trident",CubeListBuilder.create()
            .texOffs(0,0).addBox(-0.5F,2.0F,-0.5F,1.0F,21.0F,1.0F)
            .texOffs(4,10).addBox(-1.5F,19.0F,-0.5F,1.0F,3.0F,1.0F)
            .texOffs(10,5).addBox(0.5F,19.0F,-0.5F,1.0F,3.0F,1.0F)
            .texOffs(12,14).addBox(1.5F,18.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(15,10).addBox(-2.5F,18.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(4,0).addBox(-1.5F,-2.0F,-0.5F,3.0F,4.0F,1.0F)
            .texOffs(14,7).addBox(-3.5F,0.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(8,14).addBox(2.5F,0.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(16,0).addBox(1.5F,-1.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(16,13).addBox(-2.5F,-1.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(9,9).addBox(-4.5F,-4.0F,-0.5F,2.0F,4.0F,1.0F)
            .texOffs(14,4).addBox(-3.5F,-6.0F,-0.5F,1.0F,2.0F,1.0F)
            .texOffs(12,0).addBox(-0.5F,-5.0F,-0.5F,1.0F,3.0F,1.0F)
            .texOffs(4,5).addBox(2.5F,-4.0F,-0.5F,2.0F,4.0F,1.0F)
            .texOffs(4,14).addBox(2.5F,-6.0F,-0.5F,1.0F,2.0F,1.0F),PartPose.ZERO);
        return LayerDefinition.create(mesh,32,32);
    }
}
