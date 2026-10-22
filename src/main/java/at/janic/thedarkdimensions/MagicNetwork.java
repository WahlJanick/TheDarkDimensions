package at.janic.thedarkdimensions;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class MagicNetwork {
    public record Action(int action,int argument,int source) implements CustomPacketPayload {
        public static final Type<Action> TYPE=new Type<>(TheDarkDimensions.id("magic_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((b,p)->{b.writeVarInt(p.action);b.writeVarInt(p.argument);b.writeVarInt(p.source);},b->new Action(b.readVarInt(),b.readVarInt(),b.readVarInt()));
        @Override public Type<Action> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent e){e.registrar("3").playToServer(Action.TYPE,Action.CODEC,(msg,ctx)->{
        if(!(ctx.player() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;
        if(msg.action==3){PerkContent.dash(p);return;}
        if(msg.action==4){KatanaDefense.dodge(p,msg.argument);return;}
        if(msg.action==5){KatanaDefense.miss(p);return;}
        if(msg.action==6){BackEquipment.swap(p);return;}
        int source=MagicBooks.active(p);if(source<0)return;
        if(msg.action==0)p.openMenu(new SimpleMenuProvider((id,inv,player)->new SpellbookMenu(id,inv,source),Component.translatable("ui.thedarkdimensions.spellbook")));
        else if(msg.action==1&&p.containerMenu==p.inventoryMenu)MagicCasting.cast(p,source);
        else if(msg.action==2&&msg.source==source&&msg.argument>=0&&msg.argument<5&&p.containerMenu==p.inventoryMenu){var book=MagicBooks.get(p,source).copy();var d=MagicData.read(book);d.selected=msg.argument;d.save(book);MagicBooks.set(p,source,book);}
    });}
}
