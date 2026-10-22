package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

public final class PackMobRenderer<T extends Mob> extends MobRenderer<T,PackMobModel.State,PackMobModel> {
    private final String kind;private final float size;
    private final java.util.Map<java.util.UUID,Long> aggressiveUntil=new java.util.LinkedHashMap<>(32,.75f,true){
        @Override protected boolean removeEldestEntry(java.util.Map.Entry<java.util.UUID,Long> entry){return size()>256;}
    };
    public PackMobRenderer(EntityRendererProvider.Context context,String kind,float size){
        super(context,new PackMobModel(kind,PackMobModel.bakeVanilla(kind)),.45f*size);this.kind=kind;this.size=size;
    }
    @Override public PackMobModel.State createRenderState(){return new PackMobModel.State();}
    @Override public Identifier getTextureLocation(PackMobModel.State state){return TheDarkDimensions.id("textures/entity/"+kind+".png");}
    @Override public void extractRenderState(T mob,PackMobModel.State state,float partialTicks){
        super.extractRenderState(mob,state,partialTicks);state.kind=kind;state.aggressive=mob.isAggressive();state.charging=mob instanceof RealmMobs.Caster caster&&caster.isCharging();
        if(kind.equals("veilwood_spider")){
            // Original var.glowing_eyes persists for ten seconds after combat.
            // A melee goal toggles aggression while approaching: switching the
            // two entirely different meshes on each toggle makes legs flicker.
            long tick=mob.level().getGameTime();
            if(state.aggressive)aggressiveUntil.put(mob.getUUID(),tick+200);
            state.aggressive=aggressiveUntil.getOrDefault(mob.getUUID(),Long.MIN_VALUE)>tick;
        }
        state.flapTime=state.ageInTicks;state.phantomSize=0;
        if(mob instanceof net.minecraft.world.entity.monster.Phantom phantom){state.flapTime+=phantom.getUniqueFlapTickOffset();state.phantomSize=phantom.getPhantomSize();}
        var camera=entityRenderDispatcher.camera;if(camera!=null){state.cameraYaw=camera.yRot();state.cameraPitch=camera.xRot();}
    }
    @Override protected void scale(PackMobModel.State state,PoseStack pose){applyScale(state,pose,size);}
    /** Preserve the original Phantom renderer origin instead of a ground-mob origin. */
    public static void applyScale(PackMobModel.State state,PoseStack pose,float size){
        float scale=size*(state.kind.equals("bonewing")?1+.15f*state.phantomSize:1);pose.scale(scale,scale,scale);
        if(state.kind.equals("bonewing")||state.kind.equals("lantern_wisp"))pose.translate(0,1.3125f,.1875f);
    }
    @Override protected void setupRotations(PackMobModel.State state,PoseStack pose,float bodyRot,float scale){
        super.setupRotations(state,pose,bodyRot,scale);
        if(kind.equals("bonewing"))pose.rotateDegrees(com.mojang.math.Axis.XP,state.xRot);
    }
    @Override protected int getBlockLightLevel(T mob,BlockPos pos){return kind.equals("lantern_wisp")?15:super.getBlockLightLevel(mob,pos);}
}
