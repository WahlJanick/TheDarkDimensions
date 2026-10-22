package at.janic.thedarkdimensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Native blasting inventory, hopper access, recipes and XP with twice the speed. */
public final class SolarForgeBlockEntity extends AbstractFurnaceBlockEntity {
    public SolarForgeBlockEntity(BlockPos pos,BlockState state) { super(SolarContent.SOLAR_FORGE_ENTITY.get(),pos,state,RecipeType.BLASTING); }
    // Vanilla fuel providers match the Blast Furnace block explicitly. Evaluate fuel
    // in that context, then double speed and halve burn duration once more.
    @Override protected net.minecraft.world.level.storage.loot.LootContext getLootContext(ServerLevel level,ItemStack fuel) {
        return new net.minecraft.world.level.storage.loot.LootContext.Builder(
            new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE,net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY,this)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.CONTAINER,this)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,net.minecraft.world.phys.Vec3.atCenterOf(getBlockPos()))
                .withOptionalParameter(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.QUERIED_STACK,fuel.isEmpty()?null:fuel)
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.CONTAINER_PROCESS))
            .create(java.util.Optional.empty());
    }
    @Override protected float getSpeedMultiplier(ServerLevel level,ItemStack fuel) { return super.getSpeedMultiplier(level,fuel)*2; }
    @Override protected int getBurnDuration(ServerLevel level,ItemStack fuel) { return super.getBurnDuration(level,fuel)/2; }
    @Override protected Component getDefaultName() { return Component.translatable("container.thedarkdimensions.solar_forge"); }
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory) { return new BlastFurnaceMenu(id,inventory,this,this.dataAccess); }
}
