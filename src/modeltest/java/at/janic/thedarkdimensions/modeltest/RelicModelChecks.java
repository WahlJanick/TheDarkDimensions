package at.janic.thedarkdimensions.modeltest;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.resources.model.cuboid.*;
import net.minecraft.client.renderer.item.*;

final class RelicModelChecks {
    static void run() throws Exception {
        // Register only the native codecs used here; full NeoForge bootstrap requires a running client.
        register(ItemModels.class,"model",CuboidItemModelWrapper.Unbaked.MAP_CODEC);register(ItemModels.class,"select",SelectItemModel.Unbaked.MAP_CODEC);
        register(ItemModels.class,"condition",ConditionalItemModel.Unbaked.MAP_CODEC);
        register(net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties.class,"broken",net.minecraft.client.renderer.item.properties.conditional.Broken.MAP_CODEC);
        register(net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties.class,"main_hand",net.minecraft.client.renderer.item.properties.select.MainHand.TYPE);
        register(net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties.class,"display_context",net.minecraft.client.renderer.item.properties.select.DisplayContext.TYPE);
        var root=Path.of("src/main/resources/assets/thedarkdimensions");
        var drops=net.minecraft.client.particle.ParticleDescription.fromJson(JsonParser.parseString(Files.readString(root.resolve("particles/blood_drop.json"))).getAsJsonObject());
        if(!drops.getTextures().equals(List.of(net.minecraft.resources.Identifier.withDefaultNamespace("drip_fall"))))throw new AssertionError("Bleeding must use the native falling-drop sprite");
        var bossSkin=javax.imageio.ImageIO.read(root.resolve("textures/entity/ashbound_regent.png").toFile());if(bossSkin.getWidth()!=64||bossSkin.getHeight()!=64)throw new AssertionError("Editable Regent skin must retain the original illager UV size");
        System.out.println("Blood-drop particle description passed native parsing; independent Regent skin retains native 64x64 UV dimensions.");
        for(String name:List.of("wool_glider","devils_wings")){
            ClientItem.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(Files.readString(root.resolve("items/"+name+".json")))).getOrThrow();
            var info=net.minecraft.client.resources.model.EquipmentClientInfo.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(Files.readString(root.resolve("equipment/"+name+".json")))).getOrThrow();
            var layers=info.getLayers(net.minecraft.client.resources.model.EquipmentClientInfo.LayerType.WINGS);
            if(layers.size()!=1||layers.getFirst().usePlayerTexture()||!layers.getFirst().textureId().toString().equals("thedarkdimensions:"+name))throw new AssertionError("Supplied wing texture must use its own equipment layer");
        }
        var avatar=java.lang.classfile.ClassFile.of().parse(readClass("net/minecraft/client/renderer/entity/player/AvatarRenderer.class"));
        if(avatar.methods().stream().noneMatch(m->m.methodName().equalsString("extractRenderState")&&m.methodType().equalsString("(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V")))throw new AssertionError("Back-tool render extraction target missing");
        System.out.println("Both supplied gliders passed native item/equipment codecs; back-tool renderer injection descriptor exists.");
        for(String name:List.of("basic_katana","mindshifter","hellsblade")){
            var item=JsonParser.parseString(Files.readString(root.resolve("items/"+name+".json")));
            ClientItem.CODEC.parse(JsonOps.INSTANCE,item).getOrThrow();
        }
        for(String name:List.of("basic_katana","mindshifter","mindshifter_off","hellsblade","hellsblade_off")){
            try(var reader=Files.newBufferedReader(root.resolve("models/item/"+name+".json"))){var model=CuboidModel.fromStream(reader);if(!(model.geometry() instanceof UnbakedCuboidGeometry geometry)||geometry.elements().isEmpty())throw new AssertionError("Empty native relic model");
                if(!name.equals("basic_katana")&&geometry.elements().stream().noneMatch(element->element.lightEmission()==15))throw new AssertionError("Missing authored emissive overlay");
            }
        }
        for(String name:List.of("animation","animation_e")){
            var meta=JsonParser.parseString(Files.readString(root.resolve("textures/item/hellsblade/"+name+".png.mcmeta"))).getAsJsonObject().get("animation");
            var animation=net.minecraft.client.resources.metadata.animation.AnimationMetadataSection.CODEC.parse(JsonOps.INSTANCE,meta).getOrThrow();var frame=animation.calculateFrameSize(32,256);
            if(frame.width()!=32||frame.height()!=32||animation.defaultFrameTime()!=2)throw new AssertionError("Authored eight-frame animation timing changed");
        }
        System.out.println("Three relic item definitions, both handedness switches, five original meshes/emissive overlays and eight-frame Hellsblade animation passed native Minecraft codecs.");
    }
    private static byte[] readClass(String name) throws Exception {try(var stream=RelicModelChecks.class.getClassLoader().getResourceAsStream(name)){if(stream==null)throw new AssertionError("Missing client class: "+name);return stream.readAllBytes();}}
    @SuppressWarnings({"rawtypes","unchecked"}) private static void register(Class<?> owner,String name,Object codec) throws Exception {var field=owner.getDeclaredField("ID_MAPPER");field.setAccessible(true);((net.minecraft.util.ExtraCodecs.LateBoundIdMapper)field.get(null)).put(net.minecraft.resources.Identifier.withDefaultNamespace(name),codec);}
}
