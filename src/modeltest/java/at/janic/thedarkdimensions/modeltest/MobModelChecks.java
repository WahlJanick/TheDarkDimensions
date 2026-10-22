package at.janic.thedarkdimensions.modeltest;

import at.janic.thedarkdimensions.client.PackMobModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.model.monster.spider.SpiderModel;
import java.util.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.google.gson.Gson;
import java.nio.file.*;

public final class MobModelChecks {
    public static void main(String[] args) throws Exception {
        RelicModelChecks.run();
        var bases=Map.of("ashen_wraith",BlazeModel.createBodyLayer(),"bonewing",PhantomModel.createBodyLayer(),"lantern_wisp",PhantomModel.createBodyLayer(),"veilwood_spider",SpiderModel.createSpiderBodyLayer(),"cinder_broodmother",SpiderModel.createSpiderBodyLayer(),"dune_stalker",SpiderModel.createSpiderBodyLayer(),"drowned_sentinel",SkeletonModel.createBodyLayer(),"briar_whisperer",IllagerModel.createBodyLayer());
        int total=0;
        Map<String,List<float[]>> preview=new LinkedHashMap<>();
        Map<Integer,Map<String,List<float[]>>> worldPreviews=new LinkedHashMap<>();worldPreviews.put(30,new LinkedHashMap<>());worldPreviews.put(150,new LinkedHashMap<>());
        for(var entry:bases.entrySet()){
            var model=new PackMobModel(entry.getKey(),entry.getValue().bakeRoot());
            if(entry.getKey().equals("bonewing")||entry.getKey().equals("lantern_wisp")){
                var body=model.root().getChild("body");
                if(!body.hasChild("head")||!body.getChild("left_wing").hasChild("left_wing_tip")||!body.getChild("tail").hasChild("tail2"))throw new AssertionError("Phantom hierarchy flattened");
            }
            var state=new PackMobModel.State();state.kind=entry.getKey();state.ageInTicks=40;state.flapTime=40;state.walkAnimationPos=3;state.walkAnimationSpeed=.5f;state.cameraYaw=60;state.cameraPitch=20;
            for(boolean aggressive:List.of(false,true)){
                state.aggressive=aggressive;state.charging=aggressive;model.setupAnim(state);int[] count={0};
                if(entry.getKey().equals("cinder_broodmother")){
                    var leg=model.root().getChild("leg1");
                    if(leg.xScale!=.5f||leg.getChild("imported").xScale!=1)throw new AssertionError("Broodmother leg scaled twice");
                }
                if(entry.getKey().equals("veilwood_spider")||entry.getKey().equals("dune_stalker")||entry.getKey().equals("cinder_broodmother")){
                    var imported=model.root().getChild("leg1").getChild("imported");
                    if(imported.xRot!=imported.getInitialPose().xRot())throw new AssertionError("Leg geometry animated twice");
                }
                model.root().visit(new PoseStack(),(pose,path,index,cube)->{
                    count[0]++;for(var polygon:cube.polygons)for(var vertex:polygon.vertices()){
                        if(!Float.isFinite(vertex.u())||!Float.isFinite(vertex.v())||!Float.isFinite(vertex.x())||!Float.isFinite(vertex.y())||!Float.isFinite(vertex.z()))throw new AssertionError(entry.getKey()+" invalid native mesh");
                    }
                });
                if(count[0]==0)throw new AssertionError("Empty model: "+entry.getKey());total+=count[0];
                for(var part:model.root().getAllParts())if(!Float.isFinite(part.xRot)||!Float.isFinite(part.yRot)||!Float.isFinite(part.zRot))throw new AssertionError("Invalid pose");
                List<float[]> vertices=new ArrayList<>();
                model.renderToBuffer(new PoseStack(),new Collector(vertices),0,0,-1);
                if(vertices.isEmpty()||vertices.size()%4!=0)throw new AssertionError("Invalid visible mesh: "+entry.getKey());
                preview.put(entry.getKey()+(aggressive?"_combat":"_idle"),vertices);
                for(int angle:worldPreviews.keySet()){
                    state.cameraYaw=180-angle;state.cameraPitch=12;model.setupAnim(state);
                    var pose=entityPose(state);List<float[]> worldVertices=new ArrayList<>();model.renderToBuffer(pose,new Collector(worldVertices),0,0,-1);
                    worldPreviews.get(angle).put(entry.getKey()+(aggressive?"_combat":"_idle"),worldVertices);
                }
            }
            System.out.println("Native mesh and both animation states passed: "+entry.getKey());
        }
        System.out.println("All 8 imported mob models passed; visited "+total+" native cubes across idle/combat states.");
        var sovereign=new at.janic.thedarkdimensions.client.AshenSovereignRenderer.Model(IllagerModel.createBodyLayer().bakeRoot());
        for(int pose=0;pose<=5;pose++)for(float progress:new float[]{0,.5f,1}){
            var state=new at.janic.thedarkdimensions.client.AshenSovereignRenderer.State();state.duelPose=pose;state.progress=progress;
            state.armPose=net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.NEUTRAL;
            sovereign.setupAnim(state);
            for(var part:sovereign.root().getAllParts())if(!Float.isFinite(part.xRot)||!Float.isFinite(part.yRot)||!Float.isFinite(part.zRot)||part.xScale!=1||part.yScale!=1||part.zScale!=1)throw new AssertionError("Invalid Sovereign pose or altered vanilla scale");
            List<float[]> vertices=new ArrayList<>();sovereign.renderToBuffer(new PoseStack(),new Collector(vertices),0,0,-1);
            if(vertices.isEmpty()||vertices.size()%4!=0)throw new AssertionError("Invalid Sovereign visible mesh");
            preview.put("ashen_sovereign_"+pose+"_"+progress,vertices);
        }
        System.out.println("Sovereign native illager model passed all six poses at start, midpoint and end.");
        var regent=new at.janic.thedarkdimensions.client.AshboundRegentRenderer.Model(IllagerModel.createBodyLayer().bakeRoot());
        for(int pose=0;pose<=5;pose++)for(float progress:new float[]{0,.5f,1}){
            var state=new at.janic.thedarkdimensions.client.AshboundRegentRenderer.State();state.pose=pose;state.progress=progress;
            state.armPose=pose==2||pose==3?net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.SPELLCASTING:net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.NEUTRAL;
            regent.setupAnim(state);for(var part:regent.root().getAllParts())if(!Float.isFinite(part.xRot)||!Float.isFinite(part.yRot)||!Float.isFinite(part.zRot)||part.xScale!=1||part.yScale!=1||part.zScale!=1)throw new AssertionError("Invalid Regent pose or altered native UV geometry");
            List<float[]> vertices=new ArrayList<>();regent.renderToBuffer(new PoseStack(),new Collector(vertices),0,0,-1);
            if(vertices.isEmpty()||vertices.size()%4!=0)throw new AssertionError("Invalid Regent visible mesh");
        }
        System.out.println("Regent native illager mesh passed all six poses at start, midpoint and end.");
        for(String mage:List.of("fire_mage","chronus_mage"))for(boolean combat:List.of(false,true)){
            var model=new IllagerModel<net.minecraft.client.renderer.entity.state.IllagerRenderState>(IllagerModel.createBodyLayer().bakeRoot());
            var state=new net.minecraft.client.renderer.entity.state.IllagerRenderState();state.ageInTicks=40;state.walkAnimationPos=3;state.walkAnimationSpeed=.5f;
            state.armPose=combat?net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.SPELLCASTING:net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.CROSSED;
            model.setupAnim(state);List<float[]> vertices=new ArrayList<>();model.renderToBuffer(new PoseStack(),new Collector(vertices),0,0,-1);
            preview.put(mage+(combat?"_combat":"_idle"),vertices);
            var packState=new PackMobModel.State();packState.kind=mage;var pose=entityPose(packState);List<float[]> worldVertices=new ArrayList<>();model.renderToBuffer(pose,new Collector(worldVertices),0,0,-1);
            for(var views:worldPreviews.values())views.put(mage+(combat?"_combat":"_idle"),worldVertices);
        }
        System.out.println("Both original 64x64 mage skins use native illager geometry in crossed-arm and spellcasting poses.");
        Files.writeString(Path.of(".dev/mob-mesh-preview.json"),new Gson().toJson(preview));
        for(var entry:worldPreviews.entrySet())Files.writeString(Path.of(".dev/mob-world-mesh-"+entry.getKey()+".json"),new Gson().toJson(entry.getValue()));
        faceUvs();
        variantsAndNativeMotion();
        originalJointAnimations();
        watcherSkin(worldPreviews);
        for(var entry:worldPreviews.entrySet())Files.writeString(Path.of(".dev/mob-world-mesh-"+entry.getKey()+".json"),new Gson().toJson(entry.getValue()));
    }
    private static PoseStack entityPose(PackMobModel.State state){
        var pose=new PoseStack();pose.rotateDegrees(com.mojang.math.Axis.YP,180-state.bodyRot);
        if(state.kind.equals("bonewing"))pose.rotateDegrees(com.mojang.math.Axis.XP,state.xRot);
        pose.scale(-1,-1,1);
        float size=switch(state.kind){case "veilwood_spider"->.65f;case "dune_stalker"->.85f;case "cinder_broodmother"->1.3f;case "lantern_wisp"->.55f;default->1;};
        at.janic.thedarkdimensions.client.PackMobRenderer.applyScale(state,pose,size);pose.translate(0,-1.501f,0);return pose;
    }
    private static void faceUvs() throws Exception {
        var constructor=PackMobModel.class.getDeclaredConstructor(com.google.gson.JsonObject.class,ModelPart.class);constructor.setAccessible(true);
        // Independent CEM surface contract: pixel-space corners, not a comparison
        // with Minecraft's differently named and differently oriented box-UV faces.
        float[][][] corners={
            {{0,0,0},{2,0,0},{2,0,4},{0,0,4}},
            {{0,3,4},{2,3,4},{2,3,0},{0,3,0}},
            {{0,0,0},{0,0,4},{0,3,4},{0,3,0}},
            {{2,0,0},{0,0,0},{0,3,0},{2,3,0}},
            {{2,0,4},{2,0,0},{2,3,0},{2,3,4}},
            {{0,0,4},{2,0,4},{2,3,4},{0,3,4}}
        };
        float[][] rectangles={{6,4,8,0},{4,0,6,4},{6,4,10,7},{4,4,6,7},{0,4,4,7},{10,4,12,7}};
        for(String mirrorMode:List.of("","u","v","uv")){
            boolean mirror=mirrorMode.contains("u"),mirrorV=mirrorMode.contains("v");
            String json="""
                {"textureSize":[32,32],"base":"skeleton","models":[{"part":"body","id":"body","submodels":[{
                 "id":"uv_probe","textureSize":[16,16],"mirrorTexture":"%s","boxes":[{
                 "coordinates":[0,0,0,2,3,4],"uvDown":[4,0,6,4],"uvUp":[6,4,8,0],
                 "uvWest":[0,4,4,7],"uvNorth":[4,4,6,7],"uvEast":[6,4,10,7],"uvSouth":[10,4,12,7]}]}]}]}
                """.formatted(mirrorMode);
            var model=constructor.newInstance(com.google.gson.JsonParser.parseString(json).getAsJsonObject(),SkeletonModel.createBodyLayer().bakeRoot());
            int[] found={0};model.root().visit(new PoseStack(),(pose,path,index,cube)->{
                found[0]++;
                for(int i=0;i<6;i++)for(int v=0;v<4;v++){
                    var actual=cube.polygons[i].vertices()[v];var reference=corners[i][v];
                    int sourceFace=mirror&&(i==2||i==4)?6-i:mirrorV&&i<2?1-i:i;var rect=rectangles[sourceFace];
                    float u=(v==0||v==3)?rect[2]:rect[0];if(mirror)u=(v==0||v==3)?rect[0]:rect[2];
                    float uvV=(v<2)?rect[1]:rect[3];if(mirrorV)uvV=(v<2)?rect[3]:rect[1];
                    if(actual.x()!=reference[0]||actual.y()!=reference[1]||actual.z()!=reference[2]||actual.u()!=u/16||actual.v()!=uvV/16)throw new AssertionError("CEM face assignment, texture orientation or UV-only mirror incorrect: face "+i+" vertex "+v);
                }
            });
            if(found[0]!=1)throw new AssertionError("Missing UV probe cube");
        }
        String plane="""
            {"textureSize":[16,16],"base":"phantom","models":[{"part":"body","boxes":[{"coordinates":[0,0,0,4,0,4],"uvUp":[0,0,4,4]}]}]}
            """;
        var model=constructor.newInstance(com.google.gson.JsonParser.parseString(plane).getAsJsonObject(),PhantomModel.createBodyLayer().bakeRoot());
        model.root().visit(new PoseStack(),(pose,path,index,cube)->{
            if(cube.polygons.length!=1||cube.polygons[0].normal().y()!=-1)throw new AssertionError("A one-sided CEM uvUp wing must face upward in rendered entity space");
        });
        System.out.println("Original CEM face directions, all corner UVs, nested texture size, UV-only mirroring and one-sided wing planes passed.");
    }
    @SuppressWarnings("unchecked")
    private static Map<String,ModelPart> named(PackMobModel model) throws Exception {
        var f=PackMobModel.class.getDeclaredField("named");f.setAccessible(true);return (Map<String,ModelPart>)f.get(model);
    }
    private static void variantsAndNativeMotion() throws Exception {
        var scorpion=new PackMobModel("dune_stalker",SpiderModel.createSpiderBodyLayer().bakeRoot());
        var bogged=new PackMobModel("veilwood_spider",SpiderModel.createSpiderBodyLayer().bakeRoot());
        var skeleton=new PackMobModel("drowned_sentinel",SkeletonModel.createBodyLayer().bakeRoot());
        var flight=new PackMobModel("bonewing",PhantomModel.createBodyLayer().bakeRoot());
        var vanillaRoot=SpiderModel.createSpiderBodyLayer().bakeRoot();var vanilla=new SpiderModel(vanillaRoot);
        var phantomRoot=PhantomModel.createBodyLayer().bakeRoot();var phantom=new PhantomModel(phantomRoot);
        var nativeLegs=List.of("right_hind_leg","left_hind_leg","right_middle_hind_leg","left_middle_hind_leg","right_middle_front_leg","left_middle_front_leg","right_front_leg","left_front_leg");
        for(int frame=0;frame<12;frame++){
            var s=new PackMobModel.State();s.kind="dune_stalker";s.walkAnimationPos=frame*.7f;s.walkAnimationSpeed=.8f;s.yRot=35;s.xRot=-15;s.aggressive=frame%2==1;
            scorpion.setupAnim(s);vanilla.setupAnim(s);var parts=named(scorpion);
            for(int i=1;i<=8;i++){
                if(!parts.get("leg"+i+"_chill").visible)throw new AssertionError("Scorpion loses original leg geometry when aggressive");
                var actual=parts.get("leg"+i);var expected=vanillaRoot.getChild(nativeLegs.get(i-1));
                if(actual.xRot!=expected.xRot||actual.yRot!=expected.yRot||actual.zRot!=expected.zRot)throw new AssertionError("Imported spider joint deviates from native walking gait");
            }
            if(parts.get("head_chill").visible==s.aggressive||parts.get("head_aggro").visible!=s.aggressive)throw new AssertionError("Scorpion head variant fails to switch");
            s.kind="veilwood_spider";bogged.setupAnim(s);parts=named(bogged);
            if(!parts.get("head_aggro_rotation").visible)throw new AssertionError("Unconditional CEM head parent incorrectly hidden");
            for(int i=1;i<=8;i++)if(parts.get("leg"+i+"_chill").visible==s.aggressive||parts.get("leg"+i+"_aggro").visible!=s.aggressive)throw new AssertionError("Bogged spider does not select exactly one original leg variant");
            s.kind="drowned_sentinel";s.isInWater=frame%2==1;skeleton.setupAnim(s);parts=named(skeleton);
            for(String part:List.of("head","body","left_leg","right_leg"))if(parts.get(part+"_wet").visible!=s.isInWater||parts.get(part+"_dead").visible==s.isInWater)throw new AssertionError("Sentinel dry/wet originals overlap or disappear");
            s.kind="bonewing";s.flapTime=frame*7;flight.setupAnim(s);
            var nativeState=new net.minecraft.client.renderer.entity.state.PhantomRenderState();nativeState.flapTime=s.flapTime;phantom.setupAnim(nativeState);parts=named(flight);
            var body=phantomRoot.getChild("body");
            if(parts.get("left_wing").zRot!=body.getChild("left_wing_base").zRot||parts.get("right_wing_tip").zRot!=body.getChild("right_wing_base").getChild("right_wing_tip").zRot||parts.get("tail").xRot!=body.getChild("tail_base").xRot||parts.get("tail2").xRot!=body.getChild("tail_base").getChild("tail_tip").xRot)throw new AssertionError("Pack flier deviates from native linked wing and tail movement");
        }
        var s=new PackMobModel.State();s.kind="bonewing";s.phantomSize=2;var pose=new PoseStack();
        at.janic.thedarkdimensions.client.PackMobRenderer.applyScale(s,pose,1);
        var origin=pose.last().pose().transformPosition(new org.joml.Vector3f());
        if(Math.abs(origin.y()-1.3125f*1.3f)>1e-6||Math.abs(origin.z()-.1875f*1.3f)>1e-6)throw new AssertionError("Bonewing lost native Phantom origin or size");
        s.kind="dune_stalker";pose=new PoseStack();at.janic.thedarkdimensions.client.PackMobRenderer.applyScale(s,pose,.85f);
        origin=pose.last().pose().transformPosition(new org.joml.Vector3f());if(origin.lengthSquared()!=0)throw new AssertionError("Ground mobs received a flying-model origin");
        var wisp=new PackMobModel("lantern_wisp",PhantomModel.createBodyLayer().bakeRoot());
        for(float yaw:new float[]{0,60,150,250}){
            s=new PackMobModel.State();s.kind="lantern_wisp";s.bodyRot=37;s.xRot=20;s.cameraYaw=yaw;s.cameraPitch=12;s.flapTime=40;
            wisp.setupAnim(s);List<float[]> vertices=new ArrayList<>();wisp.renderToBuffer(entityPose(s),new Collector(vertices),0,0,-1);
            if(vertices.size()!=4)throw new AssertionError("Original ghost billboard mesh changed");
            var v=vertices.getFirst();var normal=new org.joml.Vector3f(v[5],v[6],v[7]).normalize();
            double y=Math.toRadians(yaw),p=Math.toRadians(s.cameraPitch);
            var viewer=new org.joml.Vector3f((float)(Math.sin(y)*Math.cos(p)),(float)Math.sin(p),(float)(-Math.cos(y)*Math.cos(p)));
            if(normal.dot(viewer)<.975f)throw new AssertionError("Wisp billboard becomes edge-on or faces away from camera at yaw "+yaw);
        }
        System.out.println("Original visibility variants, all eight persistent scorpion legs, native spider gait, linked Phantom wings/tails and renderer origins passed across 12 changing frames.");
        System.out.println("Wisp original billboard faces four different cameras with changing body/head rotation.");
    }
    private static void originalJointAnimations() throws Exception {
        var sentinel=new PackMobModel("drowned_sentinel",PackMobModel.bakeVanilla("drowned_sentinel"));
        var briar=new PackMobModel("briar_whisperer",PackMobModel.bakeVanilla("briar_whisperer"));
        var bonewing=new PackMobModel("bonewing",PackMobModel.bakeVanilla("bonewing"));
        var nativeSkeletonRoot=SkeletonModel.createBodyLayer().bakeRoot();var nativeSkeleton=new SkeletonModel(nativeSkeletonRoot);
        var nativeIllagerRoot=IllagerModel.createBodyLayer().bakeRoot();var nativeIllager=new IllagerModel(nativeIllagerRoot);
        for(int frame=0;frame<24;frame++){
            var state=new PackMobModel.State();state.walkAnimationPos=frame*.55f;state.walkAnimationSpeed=.85f;state.ageInTicks=frame*17;state.xRot=frame%2==0?40:-35;state.yRot=32;state.charging=frame%3==0;
            state.kind="drowned_sentinel";sentinel.setupAnim(state);
            var skeletonState=new net.minecraft.client.renderer.entity.state.SkeletonRenderState();skeletonState.walkAnimationPos=state.walkAnimationPos;skeletonState.walkAnimationSpeed=state.walkAnimationSpeed;skeletonState.ageInTicks=state.ageInTicks;skeletonState.xRot=state.xRot;skeletonState.yRot=state.yRot;skeletonState.isAggressive=state.charging;
            nativeSkeleton.setupAnim(skeletonState);
            for(String joint:List.of("head","left_arm","right_arm","left_leg","right_leg")){
                var part=named(sentinel).get(joint+"2");var expected=nativeSkeletonRoot.getChild(joint);
                close(part.xRot,expected.xRot,"Sentinel "+joint+" X");close(part.yRot,expected.yRot,"Sentinel "+joint+" Y");close(part.zRot,expected.zRot,"Sentinel "+joint+" Z");
            }
            state.kind="briar_whisperer";briar.setupAnim(state);
            var illagerState=new net.minecraft.client.renderer.entity.state.IllagerRenderState();illagerState.walkAnimationPos=state.walkAnimationPos;illagerState.walkAnimationSpeed=state.walkAnimationSpeed;illagerState.ageInTicks=state.ageInTicks;illagerState.yRot=state.yRot;illagerState.xRot=state.xRot;
            illagerState.armPose=state.charging?net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.SPELLCASTING:net.minecraft.world.entity.monster.illager.AbstractIllager.IllagerArmPose.CROSSED;
            nativeIllager.setupAnim(illagerState);var parts=named(briar);
            for(String side:List.of("left","right")){
                close(parts.get("whisp_"+side+"_leg").xRot,nativeIllagerRoot.getChild(side+"_leg").xRot,"Whisperer leg phase");
                close(parts.get(side+"_arm2").xRot,(float)Math.toRadians(42.5)+nativeIllagerRoot.getChild(side+"_arm").xRot/1.5f,"Whisperer crossed-arm compensation");
                close(parts.get(side+"_arm").zRot,nativeIllagerRoot.getChild(side+"_arm").zRot,"Whisperer casting arm");
            }
            close(parts.get("whisp_head").xRot,(float)(Math.toRadians(2)+Math.sin(Math.toRadians(37.5)+state.ageInTicks/20f/42)*.05),"Whisperer head source animation");
            close(parts.get("petal1").xRot,(float)(Math.toRadians(112.5)+Math.sin(Math.toRadians(40.5)+state.ageInTicks/20f/45)*.15),"Whisperer leaf clock in seconds");
            state.kind="bonewing";bonewing.setupAnim(state);var skull=named(bonewing).get("head2");
            close(skull.xRot,(float)Math.toRadians(45),"Bonewing skull source pose");close(skull.yRot,0,"Bonewing must not inherit a second look rotation");
        }
        System.out.println("Sentinel source-linked joints, Whisperer original animation equations and unmodified Bonewing skull passed 24 walking/casting/pitch frames.");
    }
    private static void close(float actual,float expected,String message){if(Math.abs(actual-expected)>1e-5)throw new AssertionError(message+": "+actual+" != "+expected);}
    private static void watcherSkin(Map<Integer,Map<String,List<float[]>>> previews) throws Exception {
        if(at.janic.thedarkdimensions.client.HollowWatcherRenderer.skinRenderType().pipeline()!=net.minecraft.client.renderer.RenderPipelines.ENTITY_SOLID)throw new AssertionError("Legacy transparent-white head requires opaque RGB rendering");
        var model=at.janic.thedarkdimensions.client.HollowWatcherRenderer.createModel();
        var image=javax.imageio.ImageIO.read(Path.of("src/main/resources/assets/thedarkdimensions/textures/entity/hollow_watcher.png").toFile());
        if(image.getWidth()!=64||image.getHeight()!=32)throw new AssertionError("Original Watcher skin layout changed");
        // All four side faces of the original head are white RGB with alpha 0.
        for(int y=8;y<16;y++)for(int x=0;x<32;x++)if(image.getRGB(x,y)!=0x00ffffff)throw new AssertionError("Original faceless Slenderman art changed");
        for(boolean combat:List.of(false,true)){
            var state=new net.minecraft.client.renderer.entity.state.EndermanRenderState();state.isCreepy=combat;state.yRot=25;state.walkAnimationPos=3;state.walkAnimationSpeed=.5f;
            model.setupAnim(state);if(model.hat.visible)throw new AssertionError("Unused inner hat overlays the faceless head");
            var pose=new PoseStack();pose.rotateDegrees(com.mojang.math.Axis.YP,180);pose.scale(-1,-1,1);pose.translate(0,-1.501f,0);
            List<float[]> vertices=new ArrayList<>();model.renderToBuffer(pose,new Collector(vertices),0,0,-1);
            if(vertices.size()!=6*6*4)throw new AssertionError("Watcher must render six native body parts without a black eye overlay");
            for(var view:previews.values())view.put("hollow_watcher"+(combat?"_combat":"_idle"),vertices);
        }
        System.out.println("Watcher original transparent-white Slenderman head uses opaque RGB rendering; six native parts and no unused hat passed.");
    }
    private record Collector(List<float[]> vertices) implements VertexConsumer {
        @Override public void addVertex(float x,float y,float z,int color,float u,float v,int overlay,int light,float nx,float ny,float nz){vertices.add(new float[]{x,y,z,u,v,nx,ny,nz});}
        public VertexConsumer addVertex(float x,float y,float z){return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){return this;}public VertexConsumer setColor(int c){return this;}
        public VertexConsumer setUv(float u,float v){return this;}public VertexConsumer setUv1(int u,int v){return this;}public VertexConsumer setUv2(int u,int v){return this;}public VertexConsumer setUv3(float u,float v){return this;}
        public VertexConsumer setNormal(float x,float y,float z){return this;}public VertexConsumer setLineWidth(float w){return this;}
    }
}
