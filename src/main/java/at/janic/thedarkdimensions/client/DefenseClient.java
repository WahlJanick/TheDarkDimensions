package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.DefenseNetwork;
import net.minecraft.client.Minecraft;

/** Optional player animations supplement the native guard pose and authoritative movement. */
public final class DefenseClient {
    public static void init(){if(net.neoforged.fml.ModList.get().isLoaded("player_animation_library"))DefensePlayerAnimations.init();}
    public static void tick(){if(net.neoforged.fml.ModList.get().isLoaded("player_animation_library"))DefensePlayerAnimations.tick();}
    public static void visual(DefenseNetwork.Visual msg){
        var mc=Minecraft.getInstance();if(mc.level==null||msg.kind()<0||msg.kind()>7||msg.direction()<0||msg.direction()>8)return;
        var entity=mc.level.getEntity(msg.player());
        if(entity==mc.player&&msg.kind()==2&&mc.player.isUsingItem())mc.gameMode.releaseUsingItem(mc.player);
        if(entity instanceof net.minecraft.world.entity.Avatar avatar&&net.neoforged.fml.ModList.get().isLoaded("player_animation_library"))DefensePlayerAnimations.play(avatar,msg.kind(),msg.direction());
    }
}
