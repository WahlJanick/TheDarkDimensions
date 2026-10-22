package at.janic.thedarkdimensions.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Use Minecraft's own GUI art, including resource-pack replacements. */
final class VanillaUi {
    static final int LABEL=0xff404040;
    static final Identifier CONTAINER=Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    static final Identifier SLOT=Identifier.withDefaultNamespace("container/slot");
    static final Identifier SELECTION=Identifier.withDefaultNamespace("hud/hotbar_selection");
    private static final Identifier FURNACE=Identifier.withDefaultNamespace("textures/gui/container/furnace.png");

    private static void part(GuiGraphicsExtractor g,int x,int y,int width,int height,int u,int v,int sourceWidth,int sourceHeight){
        g.blit(RenderPipelines.GUI_TEXTURED,CONTAINER,x,y,u,v,width,height,sourceWidth,sourceHeight,256,256);
    }

    /** Nine slices from the chest border and its blank title strip; no slot art is stretched. */
    static void panel(GuiGraphicsExtractor g,int x,int y,int width,int height){
        part(g,x,y,4,4,0,0,4,4);
        part(g,x+4,y,width-8,4,4,0,168,4);
        part(g,x+width-4,y,4,4,172,0,4,4);
        part(g,x,y+4,4,height-8,0,4,4,4);
        part(g,x+4,y+4,width-8,height-8,4,4,168,4);
        part(g,x+width-4,y+4,4,height-8,172,4,4,4);
        part(g,x,y+height-4,4,4,0,218,4,4);
        part(g,x+4,y+height-4,width-8,4,4,218,168,4);
        part(g,x+width-4,y+height-4,4,4,172,218,4,4);
    }

    static void slot(GuiGraphicsExtractor g,int itemX,int itemY){g.blitSprite(RenderPipelines.GUI_TEXTURED,SLOT,itemX-1,itemY-1,18,18);}
    static void selectedSlot(GuiGraphicsExtractor g,int itemX,int itemY){g.blitSprite(RenderPipelines.GUI_TEXTURED,SELECTION,itemX-4,itemY-3,24,23);}
    static void arrow(GuiGraphicsExtractor g,int x,int y){g.blit(RenderPipelines.GUI_TEXTURED,FURNACE,x,y,79,34,24,17,256,256);}
}
