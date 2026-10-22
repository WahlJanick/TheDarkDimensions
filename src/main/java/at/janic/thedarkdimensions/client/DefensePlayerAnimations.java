package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.TheDarkDimensions;
import com.zigythebird.playeranim.api.*;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.world.entity.Avatar;

/** Loaded only when PAL is installed. Defense takes priority over a pending attack animation. */
public final class DefensePlayerAnimations {
    private static final net.minecraft.resources.Identifier LAYER=TheDarkDimensions.id("katana_defense");
    private static final java.util.Set<Avatar> GUARDS=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    public static void init(){PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER,3000,avatar->new PlayerAnimationController(avatar,(controller,data,setter)->PlayState.STOP));}
    public static void play(Avatar avatar,int kind,int direction){
        var layer=PlayerAnimationAccess.getPlayerAnimationLayer(avatar,LAYER);if(!(layer instanceof PlayerAnimationController controller))return;
        if(kind==0||kind==1){GUARDS.add(avatar);controller.triggerAnimation(TheDarkDimensions.id("katana_parry"));}
        else if(kind==2){
            GUARDS.remove(avatar);
            int forward=direction/3-1,side=direction%3-1;
            String name=side!=0?(side>0?"right":"left"):(forward>0?"forward":"back");
            controller.triggerAnimation(TheDarkDimensions.id("katana_dodge_"+name));
        }
        else if(kind==4){GUARDS.remove(avatar);controller.stopTriggeredAnimation();}
        else if(kind==6||kind==7){GUARDS.remove(avatar);controller.triggerAnimation(TheDarkDimensions.id(kind==6?"mindshifter_cast":"mindshifter_shift"));}
    }
    public static void tick(){
        if(net.minecraft.client.Minecraft.getInstance().level==null){GUARDS.clear();return;}
        GUARDS.removeIf(avatar->{
            if(avatar.isUsingItem()&&!avatar.isRemoved())return false;
            var layer=PlayerAnimationAccess.getPlayerAnimationLayer(avatar,LAYER);if(layer instanceof PlayerAnimationController controller)controller.stopTriggeredAnimation();return true;
        });
    }
}
