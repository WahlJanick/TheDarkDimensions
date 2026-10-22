package at.janic.thedarkdimensions.mixin;

import at.janic.thedarkdimensions.*;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="com.zigythebird.playeranim.animation.PlayerAnimResources",remap=false)
public abstract class KatanaAnimationResourcesMixin {
    @Shadow @Final private static Map<Identifier,Object> ANIMATIONS;
    @Inject(method="onResourceManagerReload",at=@At("TAIL"))
    private void darkDimensions$finisher(ResourceManager resources,CallbackInfo ci){
        if(!net.neoforged.fml.ModList.get().isLoaded("bettercombat"))return;
        var resource=resources.getResource(Identifier.fromNamespaceAndPath("bettercombat","player_animations/two_handed_slash_horizontal_right.json"));
        if(resource.isEmpty())return;
        try(var input=resource.get().open()){
            var animation=KatanaFinisherAnimation.create(input);
            if(animation!=null)ANIMATIONS.put(TheDarkDimensions.id("katana_finisher"),animation);
        }catch(java.io.IOException|ReflectiveOperationException|com.google.gson.JsonParseException e){
            org.slf4j.LoggerFactory.getLogger("The Dark Dimensions").warn("Could not adapt the installed katana animation",e);
        }
    }
}
