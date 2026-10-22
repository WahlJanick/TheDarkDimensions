package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Vanilla advancement-style nodes; learning and slot assignment happen on the tree. */
public final class SpellbookScreen extends AbstractContainerScreen<SpellbookMenu> {
    private static final Identifier STONE=Identifier.withDefaultNamespace("textures/block/stone.png"), FRAME=Identifier.withDefaultNamespace("advancements/task_frame_unobtained"), WINDOW=Identifier.withDefaultNamespace("textures/gui/advancements/window.png");
    private int hovered=-1,chosen=-1,panX,panY=-12;private boolean dragging;
    private MagicElement tab=MagicElement.FIRE;
    public SpellbookScreen(SpellbookMenu m,Inventory i,Component t){super(m,i,t,252,140);}
    private boolean single(){return menu.display.getItem(0).getItem() instanceof SpellBookItem b&&b.element()!=null;}
    private MagicElement element(){return single()?((SpellBookItem)menu.display.getItem(0).getItem()).element():tab;}
    private boolean visible(int id){return MagicSkill.values()[id].element()==element();}
    private int nx(int id){return 30+MagicSkill.values()[id].column()*100+panX;}
    private int ny(int id){return 34+MagicSkill.values()[id].row()*64+panY;}
    private int tabAt(double x,double y){if(!single())for(var e:MagicElement.values())if(AdvancementTabType.ABOVE.isMouseOver(leftPos,topPos,e.ordinal(),x,y))return e.ordinal();return -1;}
    private boolean creative(){return minecraft.player!=null&&minecraft.player.isCreative();}
    private boolean hasIngot(){if(minecraft.player==null)return false;var inv=minecraft.player.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(SpellContent.INFUSED_INGOT))return true;return false;}
    private boolean inside(double x,double y){return x>=leftPos+9&&x<leftPos+243&&y>=topPos+18&&y<topPos+131;}
    private int mx(){return nx(chosen)+44;}private int my(int choice){return ny(chosen)+(choice==1?-20:24);}
    private int masteryAt(double x,double y){if(chosen<0||!visible(chosen)||!inside(x,y))return 0;for(int choice=1;choice<=2;choice++)if(x>=leftPos+mx()&&x<leftPos+mx()+26&&y>=topPos+my(choice)&&y<topPos+my(choice)+26)return choice;return 0;}
    private int nodeAt(double x,double y){if(!inside(x,y))return -1;for(int id=0;id<MagicSkill.COUNT;id++)if(visible(id)&&x>=leftPos+nx(id)&&x<leftPos+nx(id)+26&&y>=topPos+ny(id)&&y<topPos+ny(id)+26)return id;return -1;}
    private void border(GuiGraphicsExtractor g,int x,int y,int color){g.fill(x+3,y+1,x+23,y+2,color);g.fill(x+3,y+24,x+23,y+25,color);g.fill(x+1,y+3,x+2,y+23,color);g.fill(x+24,y+3,x+25,y+23,color);}
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float a){
        super.extractBackground(g,x,y,a);
        g.enableScissor(leftPos+9,topPos+18,leftPos+243,topPos+131);
        for(int yy=18;yy<131;yy+=16)for(int xx=9;xx<243;xx+=16)g.blit(RenderPipelines.GUI_TEXTURED,STONE,leftPos+xx,topPos+yy,0,0,16,16,16,16);
        var d=menu.data();boolean cost=hasIngot();
        for(var skill:MagicSkill.values())if(visible(skill.ordinal()))for(var requirement:skill.requirements()){
            int xx=leftPos+nx(skill.ordinal()),yy=topPos+ny(skill.ordinal())+13;
            int px=leftPos+nx(requirement.skill().ordinal())+26,py=topPos+ny(requirement.skill().ordinal())+13,mid=(xx+px)/2;
            if(px>=xx){px-=13;py+=13;mid=xx-12;}
            int color=d.level(requirement.skill().ordinal())>=requirement.level()?0xff80ff20:0xff808080;
            g.fill(px,py-1,mid+1,py+1,color);g.fill(mid-1,Math.min(py,yy),mid+1,Math.max(py,yy)+1,color);g.fill(mid,yy-1,xx,yy+1,color);
        }
        for(int id=0;id<MagicSkill.COUNT;id++)if(visible(id)){
            int xx=leftPos+nx(id),yy=topPos+ny(id);
            g.blitSprite(RenderPipelines.GUI_TEXTURED,FRAME,xx,yy,26,26);
            border(g,xx,yy,creative()||d.knows(id)||d.canLearn(id)&&cost?0xff80ff20:0xff808080);
            g.item(SpellIcons.item(MagicSkill.values()[id]),xx+5,yy+5);
        }
        if(chosen>=0&&visible(chosen))for(int choice=1;choice<=2;choice++){
            int xx=leftPos+mx(),yy=topPos+my(choice),px=leftPos+nx(chosen)+26,py=topPos+ny(chosen)+13;
            int color=d.mastery(MagicSkill.values()[chosen])==choice?0xff80ff20:0xff808080;
            g.fill(px,py-1,xx-6,py+1,color);g.fill(xx-7,Math.min(py,yy+13),xx-5,Math.max(py,yy+13)+1,color);g.fill(xx-6,yy+12,xx,yy+14,color);
            g.blitSprite(RenderPipelines.GUI_TEXTURED,d.mastery(MagicSkill.values()[chosen])==choice?Identifier.withDefaultNamespace("advancements/task_frame_obtained"):FRAME,xx,yy,26,26);
            border(g,xx,yy,d.level(chosen)==5&&(d.mastery(MagicSkill.values()[chosen])==choice||d.mastery(MagicSkill.values()[chosen])==0||creative()||hasIngot())?0xff80ff20:0xff808080);
            g.item(choice==1?SpellIcons.item(MagicSkill.values()[chosen]):masteryIcon(),xx+5,yy+5);
        }
        g.disableScissor();
        g.blit(RenderPipelines.GUI_TEXTURED,WINDOW,leftPos,topPos,0,0,252,140,256,256);
        if(!single())for(var e:MagicElement.values()){
            var type=AdvancementTabType.ABOVE;int index=e.ordinal();
            type.extractRenderState(g,leftPos+type.getX(index),topPos+type.getY(index),e==tab,index);
            type.extractIcon(g,leftPos,topPos,index,new net.minecraft.world.item.ItemStack(MagicContent.book(e)));
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){g.text(font,element().label,8,6,VanillaUi.LABEL,false);}
    @Override protected void extractSlots(GuiGraphicsExtractor g,int x,int y){}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float a){
        super.extractRenderState(g,x,y,a);int mastery=masteryAt(x,y);if(mastery>0){masteryTooltip(g,x,y,mastery);hovered=-1;return;}hovered=nodeAt(x,y);int ti=tabAt(x,y);if(ti>=0){hovered=-1;g.setComponentTooltipForNextFrame(font,List.of(Component.literal(MagicElement.values()[ti].label)),x,y);return;}if(hovered<0)return;
        MagicSkill skill=MagicSkill.values()[hovered];var d=menu.data();var lines=new ArrayList<Component>();
        lines.add(Component.literal(skill.label).withStyle(ChatFormatting.WHITE));
        lines.add(Component.literal(SpellIcons.description(skill)).withStyle(ChatFormatting.GRAY));
        if(d.knows(hovered)){
            lines.add(Component.literal("Stufe "+d.level(hovered)+"/5").withStyle(ChatFormatting.GREEN));
            if(creative()&&d.level(hovered)<5)lines.add(Component.translatable("ui.thedarkdimensions.creative_upgrade").withStyle(ChatFormatting.GREEN));
            if(d.level(hovered)<5&&!creative()){int needed=new int[]{8,20,40,70}[d.level(hovered)-1];lines.add(Component.literal("Nächste Stufe: "+d.uses[hovered]+"/"+needed+" Einsätze").withStyle(ChatFormatting.GRAY));}
            lines.add(Component.literal("Taste 1–5: Slot zuweisen").withStyle(ChatFormatting.GRAY));
            if(d.level(hovered)==5){int choice=d.mastery(skill);lines.add(choice==0?Component.translatable("ui.thedarkdimensions.choose_mastery").withStyle(ChatFormatting.GREEN):Component.translatable(MagicMastery.key(skill,choice)).withStyle(ChatFormatting.GREEN));}

            for(int i=0;i<5;i++)if(d.slots[i]==hovered)lines.add(Component.literal("Slot "+(i+1)).withStyle(ChatFormatting.GREEN));
        }else if(creative()){
            lines.add(Component.translatable("ui.thedarkdimensions.creative_learn").withStyle(ChatFormatting.GREEN));
        }else{
            if((d.elements&(1<<skill.element().ordinal()))==0)lines.add(Component.literal("Fehlt: "+skill.element().label+"-Element am Spelltable").withStyle(ChatFormatting.GRAY));
            for(var requirement:skill.requirements())if(d.level(requirement.skill().ordinal())<requirement.level())lines.add(Component.translatable("ui.thedarkdimensions.requires_spell",requirement.skill().label,requirement.level(),d.level(requirement.skill().ordinal())).withStyle(ChatFormatting.GRAY));
            lines.add(Component.literal((hasIngot()?"Kosten: ":"Fehlt: ")+"1 Chronus Infused Ingot").withStyle(ChatFormatting.GRAY));
            if(d.canLearn(hovered)&&hasIngot())lines.add(Component.literal("Klicken: erlernen").withStyle(ChatFormatting.GREEN));
        }
        g.setTooltipForNextFrame(font,lines.stream().flatMap(line->font.split(line,Math.min(220,width-24)).stream()).toList(),x,y);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){
        int ti=tabAt(e.x(),e.y());if(e.button()==0&&ti>=0){tab=MagicElement.values()[ti];hovered=chosen=-1;panX=0;panY=-12;return true;}
        int mastery=masteryAt(e.x(),e.y());if(e.button()==0&&mastery>0){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,2000+chosen*2+mastery-1);return true;}
        if(e.button()==1&&inside(e.x(),e.y())){dragging=true;return true;}
        int id=nodeAt(e.x(),e.y());if(e.button()==0&&id>=0){chosen=id;pan(Math.min(0,237-(mx()+26)),my(1)<22?22-my(1):my(2)+26>127?127-(my(2)+26):0);if(creative()||!menu.data().knows(id)&&menu.data().canLearn(id)&&hasIngot())minecraft.gameMode.handleInventoryButtonClick(menu.containerId,100+id);return true;}
        if(e.button()==0&&inside(e.x(),e.y())){dragging=true;return true;}return super.mouseClicked(e,twice);
    }
    private net.minecraft.world.item.ItemStack masteryIcon(){return new net.minecraft.world.item.ItemStack(switch(element()){case FIRE->net.minecraft.world.item.Items.BLAZE_POWDER;case WATER->net.minecraft.world.item.Items.PRISMARINE_CRYSTALS;case LIGHTNING->net.minecraft.world.item.Items.LIGHTNING_ROD.weathering().unaffected();case CHRONUS->net.minecraft.world.item.Items.CLOCK;case BLOOD->net.minecraft.world.item.Items.REDSTONE;case SOUL->net.minecraft.world.item.Items.SOUL_LANTERN;case ENDER->net.minecraft.world.item.Items.ENDER_PEARL;case ELEMENTAL->net.minecraft.world.item.Items.NETHER_STAR;});}
    private void masteryTooltip(GuiGraphicsExtractor g,int x,int y,int choice){var skill=MagicSkill.values()[chosen];var d=menu.data();var lines=new ArrayList<Component>();lines.add(Component.translatable(MagicMastery.key(skill,choice)).withStyle(ChatFormatting.WHITE));lines.add(Component.translatable(MagicMastery.key(skill,choice)+".desc").withStyle(ChatFormatting.GRAY));
        if(d.level(chosen)<5)lines.add(Component.translatable("ui.thedarkdimensions.mastery_level").withStyle(ChatFormatting.GRAY));
        else if(d.mastery(skill)==choice)lines.add(Component.translatable("ui.thedarkdimensions.mastery_selected").withStyle(ChatFormatting.GREEN));
        else lines.add(Component.translatable(d.mastery(skill)==0||creative()?"ui.thedarkdimensions.mastery_free":"ui.thedarkdimensions.mastery_change").withStyle(d.mastery(skill)==0||creative()||hasIngot()?ChatFormatting.GREEN:ChatFormatting.GRAY));
        g.setTooltipForNextFrame(font,lines.stream().flatMap(line->font.split(line,Math.min(220,width-24)).stream()).toList(),x,y);
    }
    private void pan(double dx,double dy){int max=java.util.Arrays.stream(MagicSkill.values()).filter(s->s.element()==element()).mapToInt(MagicSkill::column).max().orElse(0);panX=Math.clamp(panX+(int)dx,-Math.max(0,max*100-100),12);panY=Math.clamp(panY+(int)dy,-112,24);}
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging){pan(dx,dy);return true;}return super.mouseDragged(e,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){dragging=false;return super.mouseReleased(e);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(inside(x,y)){pan(sx*16,sy*16);return true;}return super.mouseScrolled(x,y,sx,sy);}
    @Override public boolean keyPressed(KeyEvent e){int id=hovered>=0?hovered:chosen;if(e.key()>=49&&e.key()<=53&&menu.data().knows(id)){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1000+id*5+e.key()-49);return true;}return super.keyPressed(e);}
}
