package at.janic.thedarkdimensions.finale;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.resonance.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

public final class NexusAltar extends BlockEntity {
    private int mask,phase,wait,absent,missing,clock,practiceReturnPhase;private boolean practice;private UUID boss;private final Set<UUID> members=new LinkedHashSet<>();
    public NexusAltar(BlockPos p,BlockState s){super(FinaleContent.ALTAR_ENTITY.get(),p,s);mask=s.getValue(NexusAltarBlock.MASK);}
    public int mask(){return mask;}public int phase(){return phase;}public UUID boss(){return boss;}
    private ServerLevel world(){return (ServerLevel)level;}
    public AABB bounds(){return new AABB(worldPosition).inflate(22,10,22);}
    private boolean eligible(ServerPlayer p){return p.level()==level&&p.isAlive()&&!p.isSpectator()&&bounds().contains(p.position());}
    private void message(ServerPlayer p,String key,Object...args){p.sendSystemMessage(Component.translatable("finale.thedarkdimensions."+key,args),true);}
    public void interact(ServerPlayer p,InteractionHand hand){interact(p,hand,false);}
    public void interactFromGate(ServerPlayer p,InteractionHand hand,BlockPos gate){
        if(p.level()!=level||p.distanceToSqr(Vec3.atCenterOf(gate))>64||gate.distSqr(worldPosition)>24*24||!level.getBlockState(gate).is(FinaleContent.GATE))return;
        var held=p.getItemInHand(hand);if(!p.isCreative()&&!held.is(ResonanceContent.JOURNAL)&&ResonanceContent.SIGILS.stream().noneMatch(held::is)){message(p,"need_sigils");return;}interact(p,hand,true);
    }
    private void interact(ServerPlayer p,InteractionHand hand,boolean fromGate){
        if(p.level()!=level||!p.isAlive()||p.isSpectator()||!fromGate&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))>64||!p.isCreative()&&!p.level().dimension().equals(DarkContent.BURNED_REALM))return;
        if(p.isCreative()){startPractice(p);return;}FinalProgress.data(p).found=true;
        if(p.getItemInHand(hand).is(ResonanceContent.JOURNAL)){FinalProgress.data(p).waypoint=level.dimension().identifier()+"\n"+worldPosition.getX()+", "+worldPosition.getY()+", "+worldPosition.getZ();message(p,"waypoint");return;}
        for(int n=0;n<3;n++)if(p.getItemInHand(hand).is(ResonanceContent.SIGILS.get(n))){if(!ResonanceProgress.unlocked(p,n)){message(p,"sigil_locked");return;}mask|=1<<n;world().setBlock(worldPosition,getBlockState().setValue(NexusAltarBlock.MASK,mask),2);setChanged();message(p,"attuned",Integer.bitCount(mask));if(mask==7)openGate();return;}
        if(mask!=7){message(p,"need_sigils");return;}if(phase==3){message(p,"completed");return;}if(world().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){message(p,"peaceful");return;}
        if(!p.getItemInHand(hand).isEmpty()){message(p,"empty_hand");return;}members.add(p.getUUID());
        if(phase==0){phase=1;wait=80;for(var nearby:world().players())if(eligible(nearby)){members.add(nearby.getUUID());FinalProgress.data(nearby).found=true;}CombatFeedback.play(p,"regent_awaken",Vec3.atCenterOf(worldPosition),0xd5bfe9);}
        message(p,"battle_warning");setChanged();
    }
    private void startPractice(ServerPlayer p){
        if(phase==1||phase==2){message(p,"battle_warning");return;}
        var mob=FinaleContent.REGENT.get().create(world(),EntitySpawnReason.EVENT);if(mob==null)return;
        var at=Vec3.atBottomCenterOf(worldPosition.offset(0,0,6));mob.snapTo(at.x,at.y,at.z,0,0);
        if(!world().noCollision(mob)){message(p,"blocked");return;}
        mob.setArena(worldPosition,1);mob.setPractice();if(!world().addFreshEntity(mob))return;
        practiceReturnPhase=phase==3?3:0;practice=true;members.clear();members.add(p.getUUID());boss=mob.getUUID();phase=2;absent=missing=0;
        message(p,"creative_spawn");setChanged();
    }
    private void openGate(){for(var pos:BlockPos.betweenClosed(worldPosition.offset(-24,-1,-24),worldPosition.offset(24,8,24)))if(world().getBlockState(pos).is(FinaleContent.GATE))world().setBlock(pos,Blocks.AIR.defaultBlockState(),3);}
    private List<ServerPlayer> present(){return members.stream().map(id->world().getServer().getPlayerList().getPlayer(id)).filter(Objects::nonNull).filter(this::eligible).toList();}
    private boolean loaded(){for(int x=(worldPosition.getX()-22)>>4;x<=(worldPosition.getX()+22)>>4;x++)for(int z=(worldPosition.getZ()-22)>>4;z<=(worldPosition.getZ()+22)>>4;z++)if(world().getChunkSource().getChunkNow(x,z)==null||!world().areEntitiesLoaded(ChunkPos.pack(x,z)))return false;return true;}
    private boolean finishIfDefeated(){
        if(phase!=2||boss==null)return false;var ledger=FinalLedger.data(world());if(!ledger.deaths.remove(boss.toString()))return false;
        phase=3;ledger.setDirty();for(UUID id:members){ledger.offer(id);var p=world().getServer().getPlayerList().getPlayer(id);if(p!=null&&p.isAlive())ledger.deliver(p);}setChanged();return true;
    }
    public void tickServer(){
        if(!(level instanceof ServerLevel))return;clock++;if(clock%40==0)for(var p:world().players())if(eligible(p))FinalProgress.data(p).found=true;
        // A recorded kill is authoritative even if the last participant died or left.
        if(practice&&phase==2&&boss!=null){var mob=world().getEntity(boss);if(mob!=null&&!mob.isAlive()){reset();return;}}
        if(phase==0||phase==3||!practice&&finishIfDefeated()||!loaded())return;var players=present();
        if(players.isEmpty()||!practice&&world().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){if(++absent>=300)reset();setChanged();return;}absent=0;
        if(phase==1&&--wait<=0){var mob=FinaleContent.REGENT.get().create(world(),EntitySpawnReason.EVENT);if(mob==null){reset();return;}var at=Vec3.atBottomCenterOf(worldPosition.offset(0,0,6));mob.snapTo(at.x,at.y,at.z,0,0);if(!world().noCollision(mob)){reset();for(var p:players)message(p,"blocked");return;}mob.setArena(worldPosition,players.size());mob.setTarget(players.getFirst());if(!world().addFreshEntity(mob)){reset();return;}boss=mob.getUUID();phase=2;setChanged();}
        if(phase==2&&boss!=null){
            var entity=world().getEntity(boss);if(entity==null){if(++missing>=200)reset();}else{missing=0;if(entity instanceof AshboundRegent regent&&(!regent.isAlive()||regent.getTarget()==null||!(regent.getTarget() instanceof ServerPlayer target)||!eligible(target)))regent.setTarget(players.getFirst());}
        }if(clock%20==0)setChanged();
    }
    public void reset(){if(boss!=null){var entity=world().getEntity(boss);if(entity!=null)entity.discard();}phase=practice?practiceReturnPhase:0;practice=false;boss=null;members.clear();absent=missing=0;setChanged();}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putInt("Attuned",mask);out.putInt("Phase",phase);out.putInt("Wait",wait);out.putBoolean("CreativePractice",practice);out.putInt("PracticeReturnPhase",practiceReturnPhase);if(boss!=null)out.putString("Regent",boss.toString());out.store("Members",Codec.STRING.listOf(),members.stream().map(UUID::toString).toList());}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);mask=in.getIntOr("Attuned",getBlockState().getValue(NexusAltarBlock.MASK))&7;phase=Math.clamp(in.getIntOr("Phase",0),0,3);wait=Math.clamp(in.getIntOr("Wait",80),0,80);practice=in.getBooleanOr("CreativePractice",false);practiceReturnPhase=in.getIntOr("PracticeReturnPhase",0)==3?3:0;boss=null;try{boss=UUID.fromString(in.getStringOr("Regent",""));}catch(IllegalArgumentException ignored){}members.clear();for(String id:in.read("Members",Codec.STRING.listOf()).orElse(List.of()))try{members.add(UUID.fromString(id));}catch(IllegalArgumentException ignored){}if(phase==2&&boss==null){phase=practice?practiceReturnPhase:0;practice=false;}}
}
