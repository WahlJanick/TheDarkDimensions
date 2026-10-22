package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.FireMage;
import at.janic.thedarkdimensions.TheDarkDimensions;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

/** Vanilla illager geometry and UVs; supplied mage artwork is never tinted or resized. */
public final class FireMageRenderer extends MobRenderer<FireMage,IllagerRenderState,IllagerModel<IllagerRenderState>> {
    public FireMageRenderer(EntityRendererProvider.Context context){
        super(context,new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)),.5f);
    }
    @Override public IllagerRenderState createRenderState(){return new IllagerRenderState();}
    @Override public Identifier getTextureLocation(IllagerRenderState state){return TheDarkDimensions.id("textures/entity/fire_mage.png");}
    @Override public void extractRenderState(FireMage entity,IllagerRenderState state,float partialTicks){
        super.extractRenderState(entity,state,partialTicks);
        state.isRiding=entity.isPassenger();state.mainArm=entity.getMainArm();
        state.isAggressive=entity.isAggressive();
        state.armPose=state.isAggressive?AbstractIllager.IllagerArmPose.SPELLCASTING:AbstractIllager.IllagerArmPose.CROSSED;
    }
}
