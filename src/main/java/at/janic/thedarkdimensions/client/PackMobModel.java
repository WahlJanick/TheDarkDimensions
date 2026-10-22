package at.janic.thedarkdimensions.client;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/** Native ModelParts imported from the supplied CEM geometry, including individual face UVs. */
public final class PackMobModel extends EntityModel<PackMobModel.State> {
    public static final class State extends LivingEntityRenderState {public boolean aggressive,charging;public String kind;public float cameraYaw,cameraPitch,flapTime;public int phantomSize;}
    private final Map<String,ModelPart> named=new HashMap<>();
    private final Map<ModelPart,String> names=new HashMap<>();
    private final net.minecraft.client.model.monster.spider.SpiderModel spider;
    private final net.minecraft.client.model.monster.phantom.PhantomModel phantom;
    private final net.minecraft.client.model.monster.skeleton.SkeletonModel<net.minecraft.client.renderer.entity.state.SkeletonRenderState> skeleton;
    private final net.minecraft.client.model.monster.illager.IllagerModel<net.minecraft.client.renderer.entity.state.IllagerRenderState> illager;
    private final Map<String,ModelPart> vanillaJoints;
    private final Map<String,String> visibilityRules;
    public PackMobModel(String id,ModelPart vanilla){this(load(id),vanilla);}
    /** Bake clean pivots; a shared vanilla layer may already be replaced by CEM mods. */
    public static ModelPart bakeVanilla(String id){return switch(load(id).get("base").getAsString()){
        case "spider"->net.minecraft.client.model.monster.spider.SpiderModel.createSpiderBodyLayer().bakeRoot();
        case "phantom"->net.minecraft.client.model.monster.phantom.PhantomModel.createBodyLayer().bakeRoot();
        case "skeleton"->net.minecraft.client.model.monster.skeleton.SkeletonModel.createBodyLayer().bakeRoot();
        case "illager"->net.minecraft.client.model.monster.illager.IllagerModel.createBodyLayer().bakeRoot();
        case "blaze"->net.minecraft.client.model.monster.blaze.BlazeModel.createBodyLayer().bakeRoot();
        default->throw new IllegalStateException("Unsupported imported mob base: "+id);
    };}
    private record Baked(ModelPart root,Map<String,ModelPart> named,ModelPart vanilla,Map<String,ModelPart> joints,String base,Map<String,String> visibility){}
    private PackMobModel(JsonObject json,ModelPart vanilla){this(build(json,vanilla));}
    private PackMobModel(Baked baked){super(baked.root);named.putAll(baked.named);named.forEach((name,part)->names.put(part,name));vanillaJoints=baked.joints;visibilityRules=baked.visibility;spider=baked.base.equals("spider")?new net.minecraft.client.model.monster.spider.SpiderModel(baked.vanilla):null;phantom=baked.base.equals("phantom")?new net.minecraft.client.model.monster.phantom.PhantomModel(baked.vanilla):null;skeleton=baked.base.equals("skeleton")?new net.minecraft.client.model.monster.skeleton.SkeletonModel(baked.vanilla):null;illager=baked.base.equals("illager")?new net.minecraft.client.model.monster.illager.IllagerModel(baked.vanilla):null;}
    private static JsonObject load(String id){
        try(var stream=PackMobModel.class.getResourceAsStream("/assets/thedarkdimensions/models/entity/"+id+".json")){
            if(stream==null)throw new IllegalStateException("Missing imported mob model: "+id);
            return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    private static Baked build(JsonObject json,ModelPart vanilla){
        int w=json.getAsJsonArray("textureSize").get(0).getAsInt(),h=json.getAsJsonArray("textureSize").get(1).getAsInt();
        Map<String,ModelPart> parts=new LinkedHashMap<>();
        Map<String,Map<String,ModelPart>> children=new HashMap<>();
        Map<String,ModelPart> named=new HashMap<>();
        Map<String,ModelPart> joints=new HashMap<>();
        for(var entry:json.getAsJsonArray("models")){
            var node=entry.getAsJsonObject();String name=node.get("part").getAsString();String nativeName=switch(name){case "neck"->"body0";case "body"->json.get("base").getAsString().equals("spider")?"body1":"body";case "headwear","hat"->"hat";case "left_wing"->"left_wing_base";case "right_wing"->"right_wing_base";case "tail"->"tail_base";case "tail2"->"tail_tip";default->name;};
            if(name.startsWith("leg"))nativeName=switch(name){case "leg1"->"right_hind_leg";case "leg2"->"left_hind_leg";case "leg3"->"right_middle_hind_leg";case "leg4"->"left_middle_hind_leg";case "leg5"->"right_middle_front_leg";case "leg6"->"left_middle_front_leg";case "leg7"->"right_front_leg";default->"left_front_leg";};
            if(name.startsWith("stick"))nativeName="part"+(Integer.parseInt(name.substring(5))-1);
            ModelPart original=find(vanilla,nativeName);Map<String,ModelPart> childParts=new LinkedHashMap<>();
            childParts.put("imported",node(node,w,h,named));children.put(name,childParts);
            ModelPart wrapper=new ModelPart(List.of(),childParts);
            // Source models are authored relative to the corresponding vanilla part pivots.
            if(original!=null){var pose=original.getInitialPose();wrapper.setInitialPose(pose);wrapper.loadPose(pose);}
            if(original!=null)joints.put(name,original);
            parts.put(name,wrapper);
            named.put(name,wrapper);
        }
        // CEM attaches to vanilla parts; wing tips, tails and the head retain their parent transforms.
        // Flattening these parts detached the Bonewing wings and applied pivots in the wrong space.
        if(json.get("base").getAsString().equals("phantom")){
            for(String child:List.of("head","left_wing","right_wing","tail"))attach(parts,children,"body",child);
            attach(parts,children,"left_wing","left_wing_tip");attach(parts,children,"right_wing","right_wing_tip");attach(parts,children,"tail","tail2");
        }
        if(json.get("base").getAsString().equals("illager")){
            attach(parts,children,"head","hat");attach(parts,children,"head","nose");
        }
        Map<String,String> visibility=new HashMap<>();
        if(json.has("visibilityRules"))json.getAsJsonObject("visibilityRules").entrySet().forEach(e->visibility.put(e.getKey(),e.getValue().getAsString()));
        return new Baked(new ModelPart(List.of(),parts),named,vanilla,joints,json.get("base").getAsString(),visibility);
    }
    private static void attach(Map<String,ModelPart> roots,Map<String,Map<String,ModelPart>> children,String parent,String child){
        var part=roots.remove(child);if(part!=null)children.get(parent).put(child,part);
    }
    private static ModelPart find(ModelPart root,String name){
        if(root.hasChild(name))return root.getChild(name);
        for(var p:root.getAllParts())if(p.hasChild(name))return p.getChild(name);
        return null;
    }
    private static float number(JsonObject obj,String key,float fallback){return obj.has(key)?obj.get(key).getAsFloat():fallback;}
    private static float[] vector(JsonObject obj,String key,int size){float[] out=new float[size];if(obj.has(key)){var a=obj.getAsJsonArray(key);for(int i=0;i<Math.min(size,a.size());i++)out[i]=a.get(i).getAsFloat();}return out;}
    private static ModelPart node(JsonObject node,int w,int h,Map<String,ModelPart> named){
        if(node.has("textureSize")){var size=node.getAsJsonArray("textureSize");w=size.get(0).getAsInt();h=size.get(1).getAsInt();}
        String invert=node.has("invertAxis")?node.get("invertAxis").getAsString():"";
        List<ModelPart.Cube> boxes=new ArrayList<>();
        if(node.has("boxes"))for(var entry:node.getAsJsonArray("boxes")){
            var box=entry.getAsJsonObject();float[] c=vector(box,"coordinates",6),uv=vector(box,"textureOffset",2);
            for(int i=0;i<3;i++)if(invert.indexOf("xyz".charAt(i))>=0)c[i]=-c[i]-c[i+3];
            boolean individual=!box.has("textureOffset");EnumSet<Direction> faces=EnumSet.noneOf(Direction.class);
            boolean mirror=node.has("mirrorTexture")&&node.get("mirrorTexture").getAsString().contains("u");
            boolean mirrorV=node.has("mirrorTexture")&&node.get("mirrorTexture").getAsString().contains("v");
            for(var direction:Direction.values())if(!individual||box.has("uv"+cemFace(direction,mirror,mirrorV)))faces.add(direction);
            float grow=number(box,"sizeAdd",0);
            var cube=new ModelPart.Cube((int)uv[0],(int)uv[1],c[0],c[1],c[2],c[3],c[4],c[5],grow,grow,grow,individual?false:mirror,w,h,faces);
            if(individual){int i=0;for(var direction:List.of(Direction.DOWN,Direction.UP,Direction.WEST,Direction.NORTH,Direction.EAST,Direction.SOUTH)){
                if(!faces.contains(direction))continue;float[] u=vector(box,"uv"+cemFace(direction,mirror,mirrorV),4);
                var old=cube.polygons[i];var vertices=old.vertices().clone();
                // CEM's Up/Down names refer to Y-negative/Y-positive respectively,
                // and West/East to X-positive/X-negative. Its horizontal quads
                // also begin on the opposite edge from Minecraft's box-UV quads.
                if(direction.getAxis()==Direction.Axis.Y)vertices=new ModelPart.Vertex[]{vertices[2],vertices[3],vertices[0],vertices[1]};
                // CEM mirrorTexture mirrors UVs, not the positions or face winding.
                if(mirror){float swap=u[0];u[0]=u[2];u[2]=swap;}
                if(mirrorV){float swap=u[1];u[1]=u[3];u[3]=swap;}
                cube.polygons[i++]=new ModelPart.Polygon(vertices,u[0],u[1],u[2],u[3],w,h,false,direction);
            }}
            boxes.add(cube);
        }
        Map<String,ModelPart> children=new LinkedHashMap<>();
        if(node.has("submodels"))for(var child:node.getAsJsonArray("submodels")){var o=child.getAsJsonObject();children.put(o.has("id")?o.get("id").getAsString():"part"+children.size(),node(o,w,h,named));}
        if(node.has("submodel")){var child=node.getAsJsonObject("submodel");children.put(child.has("id")?child.get("id").getAsString():"part"+children.size(),node(child,w,h,named));}
        var part=new ModelPart(boxes,children);float[] t=vector(node,"translate",3),r=vector(node,"rotate",3);
        for(int i=0;i<3;i++){if(invert.indexOf("xyz".charAt(i))>=0){t[i]=-t[i];r[i]=-r[i];}r[i]*=(float)Math.PI/180;}
        var pose=PartPose.offsetAndRotation(t[0],t[1],t[2],r[0],r[1],r[2]);part.setInitialPose(pose);part.loadPose(pose);
        float scale=number(node,"scale",1);part.xScale=part.yScale=part.zScale=scale;part.setInitialPose(new PartPose(t[0],t[1],t[2],r[0],r[1],r[2],scale,scale,scale));
        if(node.has("id"))named.put(node.get("id").getAsString(),part);
        return part;
    }
    private static String cemFace(Direction d,boolean mirrorU,boolean mirrorV){return switch(d){case DOWN->mirrorV?"Down":"Up";case UP->mirrorV?"Up":"Down";case WEST->mirrorU?"West":"East";case EAST->mirrorU?"East":"West";case NORTH->"North";case SOUTH->"South";};}
    @Override public void setupAnim(State state){
        super.setupAnim(state);
        if(spider!=null){
            // Let Minecraft pose the actual eight spider joints. A humanoid X-axis
            // swing on these legs detaches the feet and twists the pack's geometry.
            spider.setupAnim(state);
            vanillaJoints.forEach((name,joint)->named.get(name).loadPose(joint.storePose()));
        }
        if(phantom!=null){
            var flight=new net.minecraft.client.renderer.entity.state.PhantomRenderState();flight.flapTime=state.flapTime;flight.ageInTicks=state.ageInTicks;
            phantom.setupAnim(flight);
            vanillaJoints.forEach((name,joint)->named.get(name).loadPose(joint.storePose()));
        }
        if(skeleton!=null){
            var walking=new net.minecraft.client.renderer.entity.state.SkeletonRenderState();
            walking.walkAnimationPos=state.walkAnimationPos;walking.walkAnimationSpeed=state.walkAnimationSpeed;walking.ageInTicks=state.ageInTicks;walking.yRot=state.yRot;walking.xRot=state.xRot;walking.isAggressive=state.charging;
            skeleton.setupAnim(walking);
            // This CEM model keeps its visible joints under the body. Its source
            // animations COPY native angles onto those joints, rather than
            // adding a second, approximate humanoid animation.
            for(String joint:List.of("head","left_arm","right_arm","left_leg","right_leg"))copyAngles(vanillaJoints.get(joint),named.get(joint+"2"));
        }
        if(illager!=null){
            var walking=new net.minecraft.client.renderer.entity.state.IllagerRenderState();
            walking.walkAnimationPos=state.walkAnimationPos;walking.walkAnimationSpeed=state.walkAnimationSpeed;walking.ageInTicks=state.ageInTicks;walking.yRot=state.yRot;walking.xRot=state.xRot;
            walking.armPose=state.charging?net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.SPELLCASTING:net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.CROSSED;
            illager.setupAnim(walking);
            vanillaJoints.forEach((name,joint)->named.get(name).loadPose(joint.storePose()));
        }
        for(var entry:names.entrySet()){
            String name=entry.getValue();var p=entry.getKey();p.visible=!name.contains("health_bar")&&!name.contains("_armor")&&!name.equals("quiver")&&!name.equals("arrow");
            var rule=visibilityRules.get(name);
            if(rule!=null)p.visible&=switch(rule){case "dry"->!state.isInWater;case "wet"->state.isInWater;case "calm"->!state.aggressive;case "aggressive"->state.aggressive;default->throw new IllegalStateException("Unknown CEM body variant: "+rule);};
            // Move each joint once, rather than rotating both the joint and all of its geometry variants.
            if(state.kind.equals("cinder_broodmother")&&(name.equals("scale")||name.matches("leg[1-8]")&&p==named.get(name)))p.xScale=p.yScale=p.zScale=.5f;
            if(state.kind.equals("briar_whisperer")){
                if(name.equals("whisp_body_vexed"))p.visible=!state.aggressive;
                if(name.equals("whisp_body_vexed_angry"))p.visible=state.aggressive;
                // CEM time is measured in seconds, not Minecraft ticks.
                float time=state.ageInTicks/20;
                if(name.equals("whisp_head")){p.xRot=(float)Math.toRadians(2)+Mth.sin((float)Math.toRadians(37.5)+time/42)*.05f;p.yRot=vanillaJoints.get("head").yRot;p.zRot=vanillaJoints.get("head").zRot;}
                if(name.equals("whisp_top_head"))p.xRot=(float)Math.toRadians(-5)+Mth.sin((float)Math.toRadians(37.5)+time/42)*.05f;
                if(name.matches("petal[1-4]")){boolean odd=name.equals("petal1")||name.equals("petal3");p.xRot=(float)Math.toRadians(112.5)+Mth.sin((float)Math.toRadians(odd?40.5:37.5)+time/(odd?45:42))*(odd?.15f:.10f);}
                if(name.equals("arms"))p.visible=!state.charging;
                if(name.equals("right_arm")||name.equals("left_arm"))p.visible=state.charging;
                if(name.equals("right_arm2")||name.equals("left_arm2"))p.xRot=(float)Math.toRadians(42.5)+vanillaJoints.get(name.replace("2","")).xRot/1.5f;
                if(name.equals("whisp_left_leg")||name.equals("whisp_right_leg"))p.xRot=vanillaJoints.get(name.replace("whisp_","")).xRot;
            }
            if(state.kind.equals("dune_stalker")){
                if(name.equals("tail_start"))p.xRot=(float)Math.toRadians(42.5)+(state.aggressive?.15f:0);
                if(name.equals("tail_whopper"))p.xRot=(float)Math.toRadians(23.75);
            }
        }
        // Bonewing's head2 already has the original 45-degree skull pose and
        // follows its Phantom parent. Extra look rotation twists it twice.
        if(state.kind.equals("ashen_wraith")){var head=named.get("head2");if(head!=null){head.xRot+=state.xRot*(float)Math.PI/180;head.yRot+=state.yRot*(float)Math.PI/180;}}
        if(state.kind.equals("veilwood_spider")){
            var abdomen=named.get("abdomen_swing");if(abdomen!=null)abdomen.zRot+=state.walkAnimationSpeed*Mth.sin(state.walkAnimationPos)/4;
        }
        if(state.kind.equals("cinder_broodmother")){var abdomen=named.get("booty_sway");if(abdomen!=null)abdomen.yRot+=state.walkAnimationSpeed*Mth.sin(state.walkAnimationPos)/5;}
        if(state.kind.equals("dune_stalker")){
            var head=named.get("scorpion_head");if(head!=null){head.xRot+=state.xRot*(float)Math.PI/360;head.yRot+=state.yRot*(float)Math.PI/360;}
            var claws=named.get("arms");if(claws!=null){claws.xRot+=state.xRot*(float)Math.PI/540;claws.yRot+=state.yRot*(float)Math.PI/540;}
        }
        if(state.kind.equals("lantern_wisp")){
            var reverse=named.get("head_reverse");var head=named.get("head");if(reverse!=null&&head!=null){reverse.xRot=-head.xRot;reverse.yRot=-head.yRot;}
            var billboard=named.get("billboard");if(billboard!=null){billboard.yRot+=(180-state.bodyRot+state.cameraYaw)*(float)Math.PI/180;billboard.xRot-=state.cameraPitch*(float)Math.PI/180;}
        }
    }
    private static void copyAngles(ModelPart source,ModelPart target){if(source!=null&&target!=null){target.xRot=source.xRot;target.yRot=source.yRot;target.zRot=source.zRot;}}
}
