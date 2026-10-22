package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

final class SpellEffectsTests {
    static void broadcast(GameTestHelper h){
        var p=(ServerPlayer)h.makeMockServerPlayer(GameType.SURVIVAL);
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        var channel=new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));
        try{
            var at=h.absoluteVec(new net.minecraft.world.phys.Vec3(4.5,2,4.5));p.teleportTo(at.x,at.y,at.z);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpellContent.WATER_BOOK.get()));p.setHealth(10);
            channel.runPendingTasks();while(channel.readOutbound()!=null){}
            h.assertTrue(MagicCasting.cast(p,0)&&p.getHealth()==16,"Healing succeeds before effect broadcast");
            channel.runPendingTasks();int particles=0,sounds=0;Object packet;
            while((packet=channel.readOutbound())!=null){if(packet instanceof ClientboundLevelParticlesPacket)particles++;if(packet instanceof ClientboundSoundPacket)sounds++;}
            h.assertTrue(particles>=2&&sounds>=1,"Successful spell sends particles and positional sound to nearby client");
            h.assertTrue(!MagicCasting.cast(p,0),"Cooldown rejects second spell");channel.runPendingTasks();
            while((packet=channel.readOutbound())!=null)h.assertTrue(!(packet instanceof ClientboundLevelParticlesPacket)&&!(packet instanceof ClientboundSoundPacket),"Rejected spell has no audiovisual effect");
        }finally{h.getLevel().getServer().getPlayerList().remove(p);channel.finishAndReleaseAll();}
        h.succeed();
    }
}
