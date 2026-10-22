package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.SpelltableMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SpelltableScreen extends AbstractContainerScreen<SpelltableMenu> {
    private Button bind;
    public SpelltableScreen(SpelltableMenu menu,Inventory inventory,Component title) { super(menu,inventory,title,SpelltableMenu.WIDTH,SpelltableMenu.HEIGHT); }
    @Override protected void init() {
        super.init();
        bind=addRenderableWidget(Button.builder(Component.translatable("ui.thedarkdimensions.bind_element"),button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0))
            .bounds(leftPos+18,topPos+64,140,20).build());
        bind.setTooltip(Tooltip.create(Component.translatable("ui.thedarkdimensions.bind_hint")));
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float a) {
        super.extractBackground(g,mouseX,mouseY,a);
        VanillaUi.panel(g,leftPos,topPos,imageWidth,imageHeight);
        for(var slot:menu.slots)VanillaUi.slot(g,leftPos+slot.x,topPos+slot.y);
        VanillaUi.arrow(g,leftPos+102,topPos+34);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y) {
        super.extractLabels(g,x,y);
        g.text(font,"+",56,39,VanillaUi.LABEL,false);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float a) {
        if(bind!=null)bind.active=menu.canLearn();
        super.extractRenderState(g,x,y,a);
    }
}
