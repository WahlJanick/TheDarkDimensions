package at.janic.thedarkdimensions;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.level.Level;

/** A persistent forest hunter using the supplied original Enderman skin and native skeleton. */
public final class HollowWatcher extends Enderman {
    private net.minecraft.core.BlockPos home;
    public HollowWatcher(EntityType<? extends Enderman> type,Level level){super(type,level);setPersistenceRequired();}
    public void setForestHome(net.minecraft.core.BlockPos pos){home=pos.immutable();}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(2,new MeleeAttackGoal(this,1,false));
        goalSelector.addGoal(7,new WaterAvoidingRandomStrollGoal(this,.7));goalSelector.addGoal(8,new LookAtPlayerGoal(this,Player.class,16));goalSelector.addGoal(9,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected boolean teleport(){return false;}
    @Override public boolean teleport(double x,double y,double z){return false;}
    @Override protected void customServerAiStep(ServerLevel level){
        super.customServerAiStep(level);
        if(home==null||tickCount%20!=0)return;
        var target=getTarget();
        if(target!=null&&(!level.getBiome(target.blockPosition()).is(VeilwoodContent.BIOME)||!at.janic.thedarkdimensions.worldgen.VeilwoodBiomeSource.regionKey(target.getBlockX(),target.getBlockZ()).equals(at.janic.thedarkdimensions.worldgen.VeilwoodBiomeSource.regionKey(home.getX(),home.getZ()))))setTarget(null);
        if(!level.getBiome(blockPosition()).is(VeilwoodContent.BIOME)&&level.hasChunkAt(home)){
            setTarget(null);getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);
        }
    }
    @Override protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput out){super.addAdditionalSaveData(out);if(home!=null){out.putInt("veilwood_home_x",home.getX());out.putInt("veilwood_home_y",home.getY());out.putInt("veilwood_home_z",home.getZ());}}
    @Override protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput in){super.readAdditionalSaveData(in);int y=in.getIntOr("veilwood_home_y",Integer.MIN_VALUE);if(y!=Integer.MIN_VALUE)home=new net.minecraft.core.BlockPos(in.getIntOr("veilwood_home_x",0),y,in.getIntOr("veilwood_home_z",0));}
}
