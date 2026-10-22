package at.janic.thedarkdimensions.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(ShulkerBullet.class)
public interface ShulkerBulletTargetAccess {
    @Accessor("finalTarget") void darkdimensions$setTarget(EntityReference<Entity> target);
    @Invoker("selectNextMoveDirection") void darkdimensions$selectDirection(Direction.Axis avoid,Entity target);
}
