package at.janic.thedarkdimensions.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="net.minecraft.world.item.ItemCooldowns$CooldownInstance")
public interface CooldownInstanceAccess {
    @Accessor("endTime") int darkDimensions$end();
}
