package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class MagicKeys {
    private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(TheDarkDimensions.id("magic"));
    private static final KeyMapping.Category COMBAT=KeyMapping.Category.register(TheDarkDimensions.id("combat"));
    public static final KeyMapping OPEN=new KeyMapping("key.thedarkdimensions.open_spellbook",66,CATEGORY);
    public static final KeyMapping WHEEL=new KeyMapping("key.thedarkdimensions.spellwheel",86,CATEGORY);
    public static final KeyMapping CAST=new KeyMapping("key.thedarkdimensions.cast_spell",82,CATEGORY);
    public static final KeyMapping DASH=new KeyMapping("key.thedarkdimensions.windstep",71,CATEGORY);
    public static final KeyMapping DODGE=new KeyMapping("key.thedarkdimensions.katana_dodge",342,COMBAT);
    public static final KeyMapping SWAP_BACK=new KeyMapping("key.thedarkdimensions.swap_back",88,COMBAT);
    public static void send(int action,int argument,int source){ClientPacketDistributor.sendToServer(new MagicNetwork.Action(action,argument,source));}
    public static void init(IEventBus bus){
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty e)->{if(KatanaDefense.katana(e.getEntity().getMainHandItem()))send(5,0,-1);});
        bus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e)->{e.register(OPEN);e.register(WHEEL);e.register(CAST);e.register(DASH);e.register(DODGE);e.register(SWAP_BACK);});
        bus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e)->e.register(MagicContent.BOOK_MENU.get(),SpellbookScreen::new));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post e)->{
            var mc=Minecraft.getInstance();if(mc.player==null||mc.gui.screen()!=null)return;
            while(OPEN.consumeClick())send(0,0,-1);
            while(WHEEL.consumeClick()){int source=MagicBooks.active(mc.player);if(source>=0)mc.gui.setScreen(new SpellWheelScreen(MagicBooks.get(mc.player,source),source));}
            while(CAST.consumeClick())send(1,0,-1);
            while(DASH.consumeClick())send(3,0,-1);
            while(SWAP_BACK.consumeClick())send(6,0,-1);
            while(DODGE.consumeClick())if(KatanaDefense.katana(mc.player.getMainHandItem())){
                var input=mc.player.input.keyPresses;int forward=(input.forward()?1:0)-(input.backward()?1:0),side=(input.right()?1:0)-(input.left()?1:0);
                send(4,(forward+1)*3+side+1,-1);
            }
        });
    }
}
