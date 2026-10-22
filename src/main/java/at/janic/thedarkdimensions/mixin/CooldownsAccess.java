package at.janic.thedarkdimensions.mixin;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCooldowns.class)
public interface CooldownsAccess {
    @Accessor("cooldowns") Map<Identifier,?> darkDimensions$cooldowns();
    @Accessor("tickCount") int darkDimensions$clock();
}
