package at.janic.thedarkdimensions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Independent, persistent weather; never changes Overworld or Nether rain. */
public final class Ashfall {
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> ACTIVE=DevilContent.ATTACHMENTS.register("ashfall",() -> AttachmentType.builder(() -> false).sync(ByteBufCodecs.BOOL).build());
    public static final class Data extends SavedData {
        public long next,end;
        public Data(){this(0,0);}
        public Data(long next,long end){this.next=next;this.end=end;}
        public static final Codec<Data> CODEC=RecordCodecBuilder.create(i -> i.group(Codec.LONG.fieldOf("next").forGetter(d -> d.next),Codec.LONG.fieldOf("end").forGetter(d -> d.end)).apply(i,Data::new));
        public static final SavedDataType<Data> TYPE=new SavedDataType<>(TheDarkDimensions.id("ashfall_weather"),Data::new,CODEC);
    }
    public static Data data(ServerLevel l){return l.getDataStorage().computeIfAbsent(Data.TYPE);}
    public static boolean active(Level l){return l instanceof ServerLevel s && l.dimension().equals(DarkContent.BURNED_REALM)&&data(s).end>l.getGameTime();}
    public static void start(ServerLevel l){Data d=data(l);d.end=l.getGameTime()+2400+l.getRandom().nextInt(2401);d.next=d.end+9600+l.getRandom().nextInt(8401);d.setDirty();}
    public static void stop(ServerLevel l){Data d=data(l);d.end=0;d.next=l.getGameTime()+9600+l.getRandom().nextInt(8401);d.setDirty();}
    private static final java.util.WeakHashMap<ServerLevel,java.util.Set<net.minecraft.world.level.ChunkPos>> LOADED=new java.util.WeakHashMap<>();
    private static final java.util.WeakHashMap<ServerLevel,java.util.Set<net.minecraft.world.level.ChunkPos>> PENDING=new java.util.WeakHashMap<>();
    private static final java.util.WeakHashMap<ServerLevel,Boolean> LAST=new java.util.WeakHashMap<>();
    public static void init(){
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.ChunkEvent.Load e) -> {
            if(e.getLevel() instanceof ServerLevel l&&l.dimension().equals(DarkContent.BURNED_REALM)){
                var cp=e.getChunk().getPos();
                LOADED.computeIfAbsent(l,k -> new java.util.HashSet<>()).add(cp);
                PENDING.computeIfAbsent(l,k -> new java.util.LinkedHashSet<>()).add(cp);
            }
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.ChunkEvent.Unload e) -> {
            if(e.getLevel() instanceof ServerLevel l){var set=LOADED.get(l);if(set!=null)set.remove(e.getChunk().getPos());var pending=PENDING.get(l);if(pending!=null)pending.remove(e.getChunk().getPos());}
        });
        NeoForge.EVENT_BUS.addListener(Ashfall::tick);
        NeoForge.EVENT_BUS.addListener(Ashfall::drops);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> e.getDispatcher().register(Commands.literal("ashfall")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("start").executes(c -> command(c.getSource(),true)))
            .then(Commands.literal("stop").executes(c -> command(c.getSource(),false)))
            .then(Commands.literal("status").executes(c -> {var l=c.getSource().getLevel();c.getSource().sendSuccess(() -> Component.literal("Ashfall: "+(active(l)?"active":"inactive")),false);return 1;}))));
    }
    private static int command(net.minecraft.commands.CommandSourceStack source,boolean start){
        var l=source.getServer().getLevel(DarkContent.BURNED_REALM);
        if(l==null)return 0;if(start)start(l);else stop(l);
        source.sendSuccess(() -> Component.literal(start?"Ashfall started in the Burned Realm.":"Ashfall stopped."),true);return 1;
    }
    private static void tick(LevelTickEvent.Post e){
        if(!(e.getLevel() instanceof ServerLevel l)||!l.dimension().equals(DarkContent.BURNED_REALM))return;
        Data d=data(l);long now=l.getGameTime();
        if(d.next==0){stop(l);}else if(now>=d.next){start(l);}
        boolean active=active(l);
        Boolean previous=LAST.put(l,active);
        if(previous==null||previous!=active)PENDING.computeIfAbsent(l,k -> new java.util.LinkedHashSet<>()).addAll(LOADED.getOrDefault(l,java.util.Set.of()));
        var pending=PENDING.get(l);
        if(pending!=null&&!pending.isEmpty()){var it=pending.iterator();var cp=it.next();it.remove();updateChunk(l,cp.x(),cp.z(),active);}
        for(var player:l.players()){
            if(player.connection!=null && player.getData(ACTIVE)!=active)player.setData(ACTIVE,active);
            // One of 25 nearby chunks every four ticks: no chunk generation or full-world scans.
            if(now%4==0){int slot=(int)((now/4)%25);updateChunk(l,player.chunkPosition().x()+slot%5-2,player.chunkPosition().z()+slot/5-2,active);}
            if(active&&now%600==0&&!player.isSpectator()&&l.getRandom().nextFloat()<0.35f)spawn(l,player);
        }
    }
    public static void updateChunk(ServerLevel l,int x,int z,boolean active){
        var chunk=l.getChunkSource().getChunkNow(x,z);if(chunk==null)return;
        var sections=chunk.getSections();var pos=new BlockPos.MutableBlockPos();
        for(int n=0;n<sections.length;n++){
            var section=sections[n];if(!section.maybeHas(s -> s.hasProperty(AshfallCrystalBlock.ASHFALL)&&s.getValue(AshfallCrystalBlock.ASHFALL)!=active))continue;
            int y0=l.getSectionYFromSectionIndex(n)*16;
            for(int a=0;a<16;a++)for(int b=0;b<16;b++)for(int c=0;c<16;c++){
                var s=section.getBlockState(a,b,c);
                if(s.hasProperty(AshfallCrystalBlock.ASHFALL)&&s.getValue(AshfallCrystalBlock.ASHFALL)!=active){pos.set(x*16+a,y0+b,z*16+c);l.setBlock(pos,s.setValue(AshfallCrystalBlock.ASHFALL,active),2);}
            }
        }
    }
    private static void spawn(ServerLevel l,net.minecraft.server.level.ServerPlayer player){
        if(l.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL||!l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MOBS)||!l.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.SPAWN_MONSTERS))return;
        if(l.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,player.getBoundingBox().inflate(48)).size()>=12)return;
        double angle=l.getRandom().nextDouble()*Math.PI*2;int x=(int)Math.floor(player.getX()+Math.cos(angle)*28),z=(int)Math.floor(player.getZ()+Math.sin(angle)*28);
        if(l.getChunkSource().getChunkNow(x>>4,z>>4)==null)return;
        int y=l.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x,z);var pos=new BlockPos(x,y,z);
        if(!l.getBlockState(pos.below()).isFaceSturdy(l,pos.below(),Direction.UP)||!l.getBlockState(pos).isAir()||!l.getBlockState(pos.above()).isAir())return;
        var mob=TheDarkDimensions.MUMMY.get().create(l,EntitySpawnReason.EVENT);if(mob==null)return;
        mob.snapTo(x+0.5,y,z+0.5,0,0);
        if(!l.noCollision(mob)){mob.discard();return;}
        mob.finalizeSpawn(l,l.getCurrentDifficultyAt(pos),EntitySpawnReason.EVENT,null);l.addFreshEntity(mob);
    }
    private static void drops(LivingDropsEvent e){
        var mob=e.getEntity();if(!active(mob.level())||!((ServerLevel)mob.level()).getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MOB_DROPS))return;
        if(mob instanceof FireMage)bonus(e,TheDarkDimensions.FIRE_CORE.get(),0.10f);
        if(mob instanceof Mummy){bonus(e,SolarContent.SOLAR_ESSENCE.get(),0.05f);if(mob.level().getBiome(mob.blockPosition()).is(TheDarkDimensions.id("water_cave")))bonus(e,CaveContent.WATER_CORE.get(),0.15f);}
        if(mob instanceof GlowingBee)bonus(e,CaveContent.GLOWING_HONEY.get(),1);
    }
    private static void bonus(LivingDropsEvent e,Item item,float chance){
        if(chance<1 && e.getDrops().stream().anyMatch(d -> d.getItem().is(item)))return;
        var m=e.getEntity();if(m.getRandom().nextFloat()<chance)e.getDrops().add(new ItemEntity(m.level(),m.getX(),m.getY(),m.getZ(),new ItemStack(item)));
    }
    private Ashfall(){}
}
