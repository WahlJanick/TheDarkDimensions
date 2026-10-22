package at.janic.thedarkdimensions.client;
import at.janic.thedarkdimensions.resonance.RunicBenchMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
public final class RunicBenchScreen extends AbstractContainerScreen<RunicBenchMenu> {
    public RunicBenchScreen(RunicBenchMenu menu,Inventory inv,Component title){super(menu,inv,title,176,166);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float a){super.extractBackground(g,x,y,a);g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,Identifier.withDefaultNamespace("textures/gui/container/grindstone.png"),leftPos,topPos,0,0,176,166,256,256);}
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,title,8,6,0xff404040,false);g.text(font,playerInventoryTitle,8,72,0xff404040,false);}
}
