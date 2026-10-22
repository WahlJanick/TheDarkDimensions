package at.janic.thedarkdimensions.mixin;
import at.janic.thedarkdimensions.DevilContent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentItem.class)
public abstract class DevilLavaRiptideMixin {
    @ModifyExpressionValue(method="use",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean darkDimensions$begin(boolean wet,Level level,Player player,InteractionHand hand) {return wet||player.isInLava()&&player.getItemInHand(hand).is(DevilContent.TRIDENT);}
    @ModifyExpressionValue(method="releaseUsing",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean darkDimensions$launch(boolean wet,ItemStack stack,Level level,LivingEntity user,int remaining) {return wet||user.isInLava()&&stack.is(DevilContent.TRIDENT);}
}
