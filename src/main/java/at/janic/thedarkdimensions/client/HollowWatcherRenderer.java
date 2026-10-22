package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.TheDarkDimensions;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;

public final class HollowWatcherRenderer extends EndermanRenderer {
    private static final Identifier SKIN=Identifier.fromNamespaceAndPath(TheDarkDimensions.MOD_ID,"textures/entity/hollow_watcher.png");
    public HollowWatcherRenderer(EntityRendererProvider.Context context){
        super(context);
        // The legacy pack has an all-black, fully opaque eyes image. In 26.3's
        // translucent eyes pass it covers the skin instead of adding light.
        layers.removeIf(layer->layer instanceof EnderEyesLayer);
        model=createModel();
    }
    public static EndermanModel<EndermanRenderState> createModel(){
        return new EndermanModel<>(EndermanModel.createBodyLayer().bakeRoot()){
            @Override public void setupAnim(EndermanRenderState state){super.setupAnim(state);hat.visible=false;}
        };
    }
    public static RenderType skinRenderType(){return RenderTypes.entitySolid(SKIN);}
    @Override protected RenderType getRenderType(EndermanRenderState state,boolean visible,boolean transparent,boolean glowing){
        // Its faceless white head uses RGB white with zero alpha, as in the old
        // opaque Enderman renderer. Keep the supplied PNG intact and render it
        // with the opaque pipeline, which does not discard these head pixels.
        return visible?skinRenderType():super.getRenderType(state,visible,transparent,glowing);
    }
    @Override public Identifier getTextureLocation(EndermanRenderState state){return SKIN;}
}
