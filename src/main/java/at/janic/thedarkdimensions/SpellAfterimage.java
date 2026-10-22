package at.janic.thedarkdimensions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.storage.ValueOutput;

/** Native client entity, never written into chunk/entity saves. */
final class SpellAfterimage extends ArmorStand {
    SpellAfterimage(ServerLevel level){super(EntityTypes.ARMOR_STAND,level);}
    @Override public boolean canBeSeenAsEnemy(){return isAlive();}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public boolean save(ValueOutput output){return false;}
    @Override public boolean saveAsPassenger(ValueOutput output){return false;}
}
