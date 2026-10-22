package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Radial selection using the same slots, frame and selection art as vanilla inventory UI. */
public final class SpellWheelScreen extends Screen {
    private static final int PANEL_WIDTH=220,PANEL_HEIGHT=208,RADIUS=52;
    private final MagicData data;private final int source;private int selected;
    public SpellWheelScreen(ItemStack book,int source){super(Component.translatable("ui.thedarkdimensions.spellwheel"));data=MagicData.read(book);this.source=source;selected=data.selected;}
    @Override public boolean isPauseScreen(){return false;}
    private void commit(){MagicKeys.send(2,selected,source);onClose();}
    @Override public boolean keyReleased(KeyEvent e){if(MagicKeys.WHEEL.matches(e)){commit();return true;}return super.keyReleased(e);}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){if(e.button()==0){commit();return true;}return super.mouseClicked(e,twice);}
    @Override public boolean keyPressed(KeyEvent e){if(e.key()>=49&&e.key()<=53){selected=e.key()-49;commit();return true;}return super.keyPressed(e);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float a){
        super.extractBackground(g,mx,my,a);
        VanillaUi.panel(g,(width-PANEL_WIDTH)/2,(height-PANEL_HEIGHT)/2,PANEL_WIDTH,PANEL_HEIGHT);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float a){
        int left=(width-PANEL_WIDTH)/2,top=(height-PANEL_HEIGHT)/2,cx=left+PANEL_WIDTH/2,cy=top+90;double dx=mx-cx,dy=my-cy;
        if(dx*dx+dy*dy>28*28)selected=Math.floorMod((int)Math.floor((Math.atan2(dy,dx)+Math.PI/2+Math.PI/5)/(Math.PI*2/5)),5);
        g.text(font,title,left+8,top+6,VanillaUi.LABEL,false);
        for(int i=0;i<5;i++){
            double angle=-Math.PI/2+i*Math.PI*2/5;int x=cx+(int)(Math.cos(angle)*RADIUS),y=cy+(int)(Math.sin(angle)*RADIUS);int id=data.slots[i];
            VanillaUi.slot(g,x-8,y-8);
            if(i==selected)VanillaUi.selectedSlot(g,x-8,y-8);
            g.text(font,""+(i+1),x-2,y-20,VanillaUi.LABEL,false);
            if(id>=0)g.item(SpellIcons.item(MagicSkill.values()[id]),x-8,y-8);
        }
        int id=data.slots[selected];Component name=id<0?Component.translatable("ui.thedarkdimensions.empty_slot"):Component.translatable("ui.thedarkdimensions.spell_level",MagicSkill.values()[id].label,data.level(id));
        var lines=font.split(name,PANEL_WIDTH-24);int yy=top+158;
        for(var line:lines){g.text(font,line,cx-font.width(line)/2,yy,VanillaUi.LABEL,false);yy+=10;}
        var hint=Component.translatable("ui.thedarkdimensions.wheel_hint");g.text(font,hint,cx-font.width(hint)/2,top+190,VanillaUi.LABEL,false);
    }
}
