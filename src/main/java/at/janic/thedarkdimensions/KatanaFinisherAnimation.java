package at.janic.thedarkdimensions;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Adapts the installed animation at reload time, retaining its authored grip and pack overrides. */
public final class KatanaFinisherAnimation {
    public static Object create(InputStream input) throws ReflectiveOperationException {
        JsonObject animation=JsonParser.parseReader(new InputStreamReader(input,StandardCharsets.UTF_8)).getAsJsonObject();
        animation.addProperty("name","katana_finisher");
        for(var entry:animation.getAsJsonObject("emote").getAsJsonArray("moves")){
            var move=entry.getAsJsonObject();int tick=move.get("tick").getAsInt();
            if(move.has("torso")){
                var torso=move.getAsJsonObject("torso");
                if(torso.has("yaw"))torso.addProperty("yaw",torso.get("yaw").getAsDouble()+(tick==9?-.22:tick>=14?.12:0));
            }
            if(tick==11)move.addProperty("easing","EASEOUTQUAD");
        }
        var bytes=new ByteArrayInputStream(animation.toString().getBytes(StandardCharsets.UTF_8));
        var loaded=(java.util.Map<?,?>)Class.forName("com.zigythebird.playeranimcore.loading.UniversalAnimLoader").getMethod("loadAnimations",InputStream.class).invoke(null,bytes);
        return loaded.get("katana_finisher");
    }
}
