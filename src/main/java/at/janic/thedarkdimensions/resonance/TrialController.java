package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** No forced chunks. Timers and encounters pause with their structure. */
public final class TrialController extends BlockEntity {
    private static final WeakHashMap<ServerLevel,Map<BlockPos,TrialController>> LOADED=new WeakHashMap<>();
    public static final String MOB_SITE="ancient_trial_site";
    private String identity=UUID.randomUUID().toString();
    private int phase,seals,step,wave,waitTicks=60,absentTicks,missingTicks,clock,warning;
    private boolean paid,encounter;private BlockPos hazard;
    private final Set<UUID> participants=new LinkedHashSet<>(),enemies=new LinkedHashSet<>();
    private final Map<UUID,Long> confirmation=new HashMap<>();private final Map<Integer,BlockPos> panels=new TreeMap<>();
    private final Set<BlockPos> entrances=new HashSet<>();
    public TrialController(BlockPos pos,BlockState state){super(ResonanceContent.CONTROLLER.get(),pos,state);}
    public String identity(){return identity;}public int phase(){return phase;}public int wave(){return wave;}public int step(){return step;}public int seals(){return seals;}
    public TrialKind kind(){return getBlockState().getValue(DormantRunestoneBlock.KIND);}
    public AABB bounds(){return new AABB(Vec3.atLowerCornerOf(worldPosition.offset(-19,-2,-19)),Vec3.atLowerCornerOf(worldPosition.offset(20,10,20)));}
    private ServerLevel world(){return (ServerLevel)level;}
    private void register(){if(level instanceof ServerLevel l)LOADED.computeIfAbsent(l,k->new HashMap<>()).put(worldPosition,this);}
    public static TrialController nearest(ServerLevel l,BlockPos pos,double radius){return LOADED.getOrDefault(l,Map.of()).values().stream().filter(t->!t.isRemoved()&&t.worldPosition.distSqr(pos)<=radius*radius).min(Comparator.comparingDouble(t->t.worldPosition.distSqr(pos))).orElse(null);}
    @Override public void setRemoved(){if(level instanceof ServerLevel l){var loaded=LOADED.get(l);if(loaded!=null)loaded.remove(worldPosition,this);}super.setRemoved();}
    public void interact(ServerPlayer p,InteractionHand hand){
        register();if(!p.isAlive()||p.isSpectator()||p.level()!=level||p.distanceToSqr(Vec3.atCenterOf(worldPosition))>64)return;
        ResonanceProgress.discover(p,this);
        if(phase==3){p.sendSystemMessage(FieldJournal.text("already_complete"),true);return;}
        if(!p.level().dimension().equals(DarkContent.BURNED_REALM)||p.level().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){p.sendSystemMessage(FieldJournal.text("realm_required"),true);return;}
        if(!paid){
            if(!p.getItemInHand(hand).is(kind().core())){p.sendSystemMessage(FieldJournal.text("activation_cost",kind().core().getDefaultInstance().getHoverName()),true);return;}
            long now=p.level().getGameTime();Long until=confirmation.remove(p.getUUID());
            if(until==null||until<now){confirmation.put(p.getUUID(),now+100);p.sendSystemMessage(FieldJournal.text("activation_confirm",kind().core().getDefaultInstance().getHoverName()),true);return;}
            if(!p.isCreative())p.getItemInHand(hand).shrink(1);paid=true;
        }
        participants.add(p.getUUID());
        if(phase==0){phase=1;waitTicks=60;for(var nearby:world().players())if(eligible(nearby)){participants.add(nearby.getUUID());ResonanceProgress.discover(nearby,this);}message("trial_started");}
        else p.sendSystemMessage(FieldJournal.text("joined"),true);
        if(kind()!=TrialKind.RADIANCE&&seals==7&&phase==1){phase=2;waitTicks=60;message("wave_warning",wave+1);}
        findPanels();setChanged();
    }
    private boolean eligible(ServerPlayer p){return p.level()==level&&p.isAlive()&&!p.isSpectator()&&bounds().contains(p.position());}
    private List<ServerPlayer> present(){return participants.stream().map(id->world().getServer().getPlayerList().getPlayer(id)).filter(Objects::nonNull).filter(this::eligible).toList();}
    private void message(String key,Object...args){for(UUID id:participants){var p=world().getServer().getPlayerList().getPlayer(id);if(p!=null)p.sendSystemMessage(FieldJournal.text(key,args),true);}}
    private void findPanels(){
        if(panels.size()==3)return;
        for(int x=(worldPosition.getX()-19)>>4;x<=(worldPosition.getX()+19)>>4;x++)for(int z=(worldPosition.getZ()-19)>>4;z<=(worldPosition.getZ()+19)>>4;z++){
            var chunk=world().getChunkSource().getChunkNow(x,z);if(chunk==null)continue;
            for(var pos:BlockPos.betweenClosed(new BlockPos(x*16,worldPosition.getY()-1,z*16),new BlockPos(x*16+15,worldPosition.getY()+7,z*16+15))){
                if(!bounds().contains(Vec3.atCenterOf(pos)))continue;var s=chunk.getBlockState(pos);
                if(s.is(ResonanceContent.SEAL)&&s.getValue(DormantRunestoneBlock.KIND)==kind()){int index=s.getValue(TrialSealBlock.INDEX);if(index==0)entrances.add(pos.immutable());else panels.putIfAbsent(index,pos.immutable());}
            }
        }
    }
    public int expectedPanel(){return 1+Math.floorMod(identity.hashCode()+step*2+(step/3),3);}
    public void seal(ServerPlayer p,BlockPos pos){
        if(phase!=1||!participants.contains(p.getUUID())||!eligible(p)||p.distanceToSqr(Vec3.atCenterOf(pos))>36||!bounds().contains(Vec3.atCenterOf(pos)))return;
        var s=world().getBlockState(pos);if(!s.is(ResonanceContent.SEAL)||s.getValue(DormantRunestoneBlock.KIND)!=kind())return;
        findPanels();int index=s.getValue(TrialSealBlock.INDEX);if(index==0||!pos.equals(panels.get(index)))return;
        if(kind()==TrialKind.RADIANCE){
            if(step>=5)return;
            if(index==expectedPanel()){step++;sound(pos,SoundEvents.AMETHYST_BLOCK_CHIME,1+step*.13f);message("puzzle_progress",step,5);if(step==5&&enemies.isEmpty())complete();}
            else{message("puzzle_mistake");if(enemies.size()<3)spawn(MobExpansionContent.ASHEN_WRAITH.get(),present());}
        }else{
            if((seals&(1<<(index-1)))!=0)return;seals|=1<<(index-1);world().setBlock(pos,s.setValue(TrialSealBlock.LIT,true),2);sound(pos,SoundEvents.AMETHYST_BLOCK_RESONATE,1);message("seals_open",Integer.bitCount(seals),3);
            if(seals==7){phase=2;waitTicks=60;message("wave_warning",wave+1);}
        }
        setChanged();
    }
    private boolean areaLoaded(){for(int x=(worldPosition.getX()-19)>>4;x<=(worldPosition.getX()+19)>>4;x++)for(int z=(worldPosition.getZ()-19)>>4;z<=(worldPosition.getZ()+19)>>4;z++)if(world().getChunkSource().getChunkNow(x,z)==null||!world().areEntitiesLoaded(ChunkPos.pack(x,z)))return false;return true;}
    public void serverTick(){
        if(!(level instanceof ServerLevel))return;register();clock++;
        if(clock%40==0){boolean nearby=false;for(var p:world().players())if(eligible(p)){ResonanceProgress.discover(p,this);nearby=true;}if(nearby&&Ashfall.active(world())){findPanels();particles(worldPosition.above(),6);for(var entrance:entrances)particles(entrance.above(),4);}}
        if(phase==0||phase==3||!areaLoaded())return;
        var active=present();if(active.isEmpty()||world().getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL){if(++absentTicks>=200)retry();setChanged();return;}absentTicks=0;
        if(clock%10==0)reconcileEnemies(active);
        if(phase==1&&kind()==TrialKind.RADIANCE){
            if(step==5&&enemies.isEmpty()){complete();return;}
            if(clock%80==40)for(var pos:panels.values()){var s=world().getBlockState(pos);if(s.is(ResonanceContent.SEAL))world().setBlock(pos,s.setValue(TrialSealBlock.LIT,false),2);}
            if(step<5&&clock%80==0){findPanels();var pos=panels.get(expectedPanel());if(pos!=null){for(var panel:panels.values()){var s=world().getBlockState(panel);if(s.is(ResonanceContent.SEAL))world().setBlock(panel,s.setValue(TrialSealBlock.LIT,panel.equals(pos)),2);}particles(pos.above(),Ashfall.active(world())?16:8);sound(pos,SoundEvents.NOTE_BLOCK_CHIME.value(),.7f+expectedPanel()*.25f);message("puzzle_signal",expectedPanel());}}
        }
        if(phase==2&&enemies.isEmpty()){
            if(waitTicks>0)waitTicks--;
            else if(wave>=(kind()==TrialKind.EMBER?3:2)){complete();return;}
            else startWave(active);
        }
        if((phase==2&&!enemies.isEmpty())||phase==1&&kind()==TrialKind.RADIANCE)hazards(active);
        if(clock%20==0)setChanged();
    }
    private void reconcileEnemies(List<ServerPlayer> active){
        var ledger=TrialLedger.data(world());var dead=ledger.defeated.getOrDefault(identity,List.of());boolean missing=false;
        for(var it=enemies.iterator();it.hasNext();){UUID id=it.next();if(dead.contains(id.toString())){it.remove();continue;}var entity=world().getEntity(id);
            if(entity instanceof Mob mob){if(!bounds().contains(mob.position())){var at=safeSpawn(mob);if(at!=null)mob.snapTo(at.x,at.y,at.z,0,0);}if(!active.isEmpty())mob.setTarget(active.get(Math.floorMod(mob.getId(),active.size())));}
            else missing=true;
        }
        if(missing){missingTicks+=10;if(missingTicks>=200){retry();return;}}else missingTicks=0;
        if(enemies.isEmpty()&&!dead.isEmpty()){ledger.defeated.remove(identity);ledger.setDirty();encounter=false;waitTicks=80;message("wave_cleared");setChanged();}
    }
    private void startWave(List<ServerPlayer> active){
        int extra=Math.min(2,Math.max(0,active.size()-1));int before=enemies.size();
        if(kind()==TrialKind.EMBER){spawn(CaveContent.FIRE_MAGE.get(),active);spawn(wave==2?MobExpansionContent.CINDER_BROODMOTHER.get():MobExpansionContent.ASHEN_WRAITH.get(),active);for(int i=0;i<extra;i++)spawn(MobExpansionContent.ASHEN_WRAITH.get(),active);}
        else for(int i=0;i<2+extra;i++)spawn(MobExpansionContent.DROWNED_SENTINEL.get(),active);
        if(enemies.size()-before<2+extra){for(UUID id:enemies){var mob=world().getEntity(id);if(mob!=null)mob.discard();}enemies.clear();waitTicks=80;message("spawn_blocked");return;}encounter=true;wave++;waitTicks=80;message("wave_started",wave);setChanged();
    }
    private Vec3 safeSpawn(Mob mob){
        int start=world().getRandom().nextInt(289);
        for(int n=0;n<289;n++){int index=(n+start)%289,dx=index%17-8,dz=index/17-8;if(dx*dx+dz*dz<9)continue;
            for(int y=-1;y<=4;y++){var pos=worldPosition.offset(dx,y,dz);if(!world().getBlockState(pos.below()).isFaceSturdy(world(),pos.below(),Direction.UP))continue;if(!world().getBlockState(pos).isAir()&&!world().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.WATER))continue;
                var at=Vec3.atBottomCenterOf(pos);var box=mob.getBoundingBox().move(at.subtract(mob.position()));if(world().noCollision(mob,box))return at;
            }
        }
        return null;
    }
    private void spawn(EntityType<? extends Mob> type,List<ServerPlayer> active){
        if(enemies.size()>=6)return;Mob mob=type.create(world(),EntitySpawnReason.EVENT);if(mob==null)return;var at=safeSpawn(mob);if(at==null){mob.discard();return;}
        mob.snapTo(at.x,at.y,at.z,0,0);mob.finalizeSpawn(world(),world().getCurrentDifficultyAt(mob.blockPosition()),EntitySpawnReason.EVENT,null);mob.setPersistenceRequired();mob.getPersistentData().putString(MOB_SITE,identity);
        if(!active.isEmpty())mob.setTarget(active.getFirst());if(world().addFreshEntity(mob))enemies.add(mob.getUUID());setChanged();
    }
    private void hazards(List<ServerPlayer> active){
        if(kind()==TrialKind.RADIANCE)return;
        if(warning==0&&clock%(Ashfall.active(world())?160:220)==0){hazard=active.getFirst().blockPosition();warning=40;message("hazard_"+kind().getSerializedName());}
        if(warning>0&&hazard!=null){
            if(clock%5==0){for(int n=0;n<16;n++){double a=n*Math.PI/8;world().sendParticles(kind()==TrialKind.EMBER?ParticleTypes.FLAME:ParticleTypes.BUBBLE, hazard.getX()+.5+Math.cos(a)*2,hazard.getY()+.2,hazard.getZ()+.5+Math.sin(a)*2,1,0,0,0,0);}}
            if(--warning==0){for(var p:active)if(bounds().contains(p.position())&&p.position().distanceToSqr(Vec3.atBottomCenterOf(hazard))<=6.25){if(kind()==TrialKind.EMBER)p.hurtServer(world(),world().damageSources().inFire(),4);else p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,0));}hazard=null;}
        }
    }
    public void retry(){
        for(UUID id:enemies){var entity=world().getEntity(id);if(entity!=null)entity.discard();}enemies.clear();
        var ledger=TrialLedger.data(world());ledger.defeated.remove(identity);ledger.setDirty();
        // A wave only counts after every member has been defeated.
        if(encounter&&wave>0)wave--;encounter=false;phase=0;participants.clear();confirmation.clear();absentTicks=0;missingTicks=0;waitTicks=60;warning=0;hazard=null;setChanged();
    }
    private void complete(){
        if(phase==3||!enemies.isEmpty())return;phase=3;warning=0;hazard=null;var ledger=TrialLedger.data(world());EndProgression.data(world()).complete(kind().ordinal());
        for(UUID id:participants){ledger.offer(id,new TrialLedger.Reward(identity,kind().ordinal(),Ashfall.active(world())));var p=world().getServer().getPlayerList().getPlayer(id);if(p!=null){ResonanceProgress.discover(p,this);ledger.deliver(p);}}
        for(UUID id:participants){var p=world().getServer().getPlayerList().getPlayer(id);if(p!=null)at.janic.thedarkdimensions.finale.CombatFeedback.play(p,"trial_complete",p.position().add(0,1,0),kind().color);}message("trial_complete");setChanged();
    }
    public Set<UUID> enemies(){return Set.copyOf(enemies);}
    public String diagnostic(){return "phase="+phase+", wave="+wave+", step="+step+", enemies="+enemies+", deaths="+TrialLedger.data(world()).defeated.get(identity)+", active="+present().size()+", loaded="+areaLoaded();}
    private void sound(BlockPos p,SoundEvent sound,float pitch){world().playSound(null,p,sound,SoundSource.BLOCKS,.6f,pitch);}
    private void particles(BlockPos p,int count){world().sendParticles(new DustParticleOptions(kind().color,.9f),p.getX()+.5,p.getY()+.5,p.getZ()+.5,count,.3,.5,.3,.01);}
    @Override protected void saveAdditional(ValueOutput out){super.saveAdditional(out);out.putString("site",identity);out.putInt("phase",phase);out.putInt("seals",seals);out.putInt("step",step);out.putInt("wave",wave);out.putInt("wait",waitTicks);out.putInt("absent",absentTicks);out.putInt("clock",clock);out.putBoolean("paid",paid);out.putBoolean("encounter",encounter);out.store("participants",Codec.STRING.listOf(),participants.stream().map(UUID::toString).toList());out.store("enemies",Codec.STRING.listOf(),enemies.stream().map(UUID::toString).toList());}
    @Override protected void loadAdditional(ValueInput in){super.loadAdditional(in);identity=in.getStringOr("site",identity);phase=Math.clamp(in.getIntOr("phase",0),0,3);seals=in.getIntOr("seals",0)&7;step=Math.clamp(in.getIntOr("step",0),0,5);wave=Math.clamp(in.getIntOr("wave",0),0,3);waitTicks=Math.clamp(in.getIntOr("wait",60),0,200);absentTicks=Math.clamp(in.getIntOr("absent",0),0,200);clock=in.getIntOr("clock",0);paid=in.getBooleanOr("paid",false);encounter=in.getBooleanOr("encounter",false);participants.clear();enemies.clear();readIds(in,"participants",participants,32);readIds(in,"enemies",enemies,6);panels.clear();confirmation.clear();warning=0;hazard=null;}
    private static void readIds(ValueInput in,String key,Set<UUID> ids,int max){for(String id:in.read(key,Codec.STRING.listOf()).orElse(List.of()).stream().limit(max).toList())try{ids.add(UUID.fromString(id));}catch(IllegalArgumentException ignored){}}
    public static void init(){
        var bus=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        // Retries must never become a repeatable source of mob loot or experience.
        bus.addListener(net.neoforged.bus.api.EventPriority.HIGHEST,(net.neoforged.neoforge.event.entity.living.LivingDropsEvent e)->{if(e.getEntity().getPersistentData().contains(MOB_SITE))e.setCanceled(true);});
        bus.addListener((net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent e)->{if(e.getEntity().getPersistentData().contains(MOB_SITE))e.setCanceled(true);});
        bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e)->{if(!e.isCanceled()&&e.getEntity().level() instanceof ServerLevel l){String site=e.getEntity().getPersistentData().getStringOr(MOB_SITE,"");if(!site.isEmpty())TrialLedger.data(l).defeat(site,e.getEntity().getUUID());}});
        bus.addListener((net.neoforged.neoforge.event.entity.ProjectileImpactEvent e)->{var owner=e.getProjectile().getOwner();if(owner==null||!owner.getPersistentData().contains(MOB_SITE))return;if(e.getRayTraceResult().getType()==HitResult.Type.BLOCK){e.setCanceled(true);e.getProjectile().discard();}});
        bus.addListener((net.neoforged.neoforge.event.tick.EntityTickEvent.Pre e)->{if(e.getEntity() instanceof Projectile projectile&&!projectile.level().isClientSide()){var owner=projectile.getOwner();if(owner!=null&&owner.getPersistentData().contains(MOB_SITE)&&projectile.position().distanceToSqr(owner.position())>1600)projectile.discard();}});
    }
}
