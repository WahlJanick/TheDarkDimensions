package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.finale.AshboundRegent;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

/** Uses the existing original 64x64 illager skin and exact native UV geometry. */
public final class AshboundRegentRenderer extends MobRenderer<AshboundRegent,AshboundRegentRenderer.State,AshboundRegentRenderer.Model> {
    public static final class State extends IllagerRenderState {public int pose;public float progress;}
    public static final class Model extends IllagerModel<State>{
        public Model(ModelPart root){super(root);}
        @Override public void setupAnim(State s){super.setupAnim(s);var right=root().getChild("right_arm");var left=root().getChild("left_arm");
            if(s.pose==AshboundRegent.SLASH){right.xRot=-.7f-s.progress*1.55f;right.zRot=-.25f;left.xRot=-.9f-s.progress*.5f;}
            else if(s.pose==AshboundRegent.SWING){right.xRot=-2.25f+s.progress*2.3f;right.zRot=-.25f;left.xRot=-1.4f+s.progress;}
            else if(s.pose==AshboundRegent.EXPOSED){right.xRot=.4f;right.zRot=-.35f;left.xRot=.35f;}
        }
    }
    public AshboundRegentRenderer(EntityRendererProvider.Context c){super(c,new Model(IllagerModel.createBodyLayer().bakeRoot()),.7f);}
    @Override public State createRenderState(){return new State();}
    @Override public Identifier getTextureLocation(State state){return TheDarkDimensions.id("textures/entity/ashbound_regent.png");}
    @Override public void extractRenderState(AshboundRegent boss,State state,float partial){super.extractRenderState(boss,state,partial);ArmedEntityRenderState.extractArmedEntityRenderState(boss,state,itemModelResolver,partial);state.mainArm=boss.getMainArm();state.isRiding=boss.isPassenger();state.isAggressive=boss.isAggressive();state.scale*=1.35f;state.pose=boss.combatPose();state.progress=boss.animationProgress(partial);state.armPose=boss.combatPose()==AshboundRegent.WARD||boss.combatPose()==AshboundRegent.RIFT?AbstractIllager.IllagerArmPose.SPELLCASTING:AbstractIllager.IllagerArmPose.NEUTRAL;}
}
