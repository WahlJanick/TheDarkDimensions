package at.janic.thedarkdimensions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class DefenseNetwork {
    public record Visual(int player,int kind,int direction) implements CustomPacketPayload {
        public static final Type<Visual> TYPE=new Type<>(TheDarkDimensions.id("katana_defense_visual"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Visual> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.player);b.writeVarInt(p.kind);b.writeVarInt(p.direction);},b->new Visual(b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<Visual> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("2").playToClient(Visual.TYPE,Visual.CODEC,(msg,ctx)->ctx.enqueueWork(()->at.janic.thedarkdimensions.client.DefenseClient.visual(msg)));}
}
