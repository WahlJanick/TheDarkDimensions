package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

public final class ChronusMageRenderer extends MobRenderer<RealmMobs.ChronusMage,IllagerRenderState,IllagerModel<IllagerRenderState>> {
    public ChronusMageRenderer(EntityRendererProvider.Context context){super(context,new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)),.5f);}
    @Override public IllagerRenderState createRenderState(){return new IllagerRenderState();}
    @Override public Identifier getTextureLocation(IllagerRenderState state){return TheDarkDimensions.id("textures/entity/chronus_mage.png");}
    @Override public void extractRenderState(RealmMobs.ChronusMage mob,IllagerRenderState state,float partialTicks){
        super.extractRenderState(mob,state,partialTicks);state.mainArm=mob.getMainArm();state.isRiding=mob.isPassenger();state.isAggressive=mob.isAggressive();
        state.armPose=mob.isCharging()?AbstractIllager.IllagerArmPose.SPELLCASTING:AbstractIllager.IllagerArmPose.CROSSED;
    }
}
