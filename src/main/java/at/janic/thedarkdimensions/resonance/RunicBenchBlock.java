package at.janic.thedarkdimensions.resonance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class RunicBenchBlock extends Block {
    public RunicBenchBlock(Properties p){super(p);}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player player,BlockHitResult hit){if(!l.isClientSide())player.openMenu(new SimpleMenuProvider((id,inv,p)->new RunicBenchMenu(id,inv,net.minecraft.world.inventory.ContainerLevelAccess.create(l,pos)),Component.translatable("block.thedarkdimensions.runic_binding_bench")));return InteractionResult.SUCCESS;}
}
