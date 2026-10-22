package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.resonance.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ResultSlot.class)
public abstract class RuneCraftingGateMixin extends Slot {
    protected RuneCraftingGateMixin(Container container,int id,int x,int y){super(container,id,x,y);}
    @Override public boolean mayPickup(Player player){
        int rune=AncientRunes.ingredient(getItem());if(rune<0)return super.mayPickup(player);
        int mask=player instanceof ServerPlayer p?ResonanceProgress.data(p).completed:player.getData(ResonanceProgress.UNLOCKS);
        return (rune==3?mask==7:(mask&(1<<rune))!=0)&&super.mayPickup(player);
    }
}
