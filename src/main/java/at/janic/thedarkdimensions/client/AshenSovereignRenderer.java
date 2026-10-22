package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

/** The original 64x64 skin uses vanilla illager geometry and hand/item transforms. */
public final class AshenSovereignRenderer extends MobRenderer<AshenSovereign,AshenSovereignRenderer.State,AshenSovereignRenderer.Model> {
    public static final class State extends IllagerRenderState {public int duelPose;public float progress;}
    public static final class Model extends IllagerModel<State> {
        public Model(ModelPart root){super(root);}
        @Override public void setupAnim(State state){
            super.setupAnim(state);var right=root().getChild("right_arm");var left=root().getChild("left_arm");
            if(state.duelPose==AshenSovereign.SLASH||state.duelPose==AshenSovereign.HEAVY){
                right.xRot=-.7f-state.progress*1.55f;right.zRot=-.25f;right.yRot=-.2f;
                left.xRot=-.9f-state.progress*.5f;left.zRot=.25f;
            }else if(state.duelPose==AshenSovereign.GUARD){right.xRot=-1.45f;right.zRot=-.6f;left.xRot=-1;left.zRot=.4f;}
            else if(state.duelPose==AshenSovereign.SWING){right.xRot=-2.25f+state.progress*2.3f;right.zRot=-.25f;left.xRot=-1.4f+state.progress;}
            else if(state.duelPose==AshenSovereign.EXPOSED){right.xRot=.4f;right.zRot=-.35f;left.xRot=.35f;}
        }
    }
    public AshenSovereignRenderer(EntityRendererProvider.Context context){
        super(context,new Model(context.bakeLayer(ModelLayers.VINDICATOR)),.55f);addLayer(new ItemInHandLayer<>(this));
    }
    @Override public State createRenderState(){return new State();}
    @Override public Identifier getTextureLocation(State state){return TheDarkDimensions.id("textures/entity/ashen_sovereign.png");}
    @Override public void extractRenderState(AshenSovereign entity,State state,float partial){
        super.extractRenderState(entity,state,partial);ArmedEntityRenderState.extractArmedEntityRenderState(entity,state,itemModelResolver,partial);
        state.mainArm=entity.getMainArm();state.isRiding=entity.isPassenger();state.isAggressive=entity.isAggressive();
        state.duelPose=entity.combatPose();state.progress=entity.animationProgress(partial);
        state.armPose=AbstractIllager.IllagerArmPose.NEUTRAL;
    }
}
