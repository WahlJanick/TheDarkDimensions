package at.janic.thedarkdimensions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;

/** Allocations and visitors are saved separately: guests never overwrite the owner's return point. */
public final class PocketDimensions {
    public static final ResourceKey<Level> LEVEL=ResourceKey.create(Registries.DIMENSION,TheDarkDimensions.id("pocket_dimension"));
    public static final ResourceKey<Level> LARGE_LEVEL=ResourceKey.create(Registries.DIMENSION,TheDarkDimensions.id("large_pocket_dimension"));
    public static boolean isPocket(Level l){return l.dimension().equals(LEVEL)||l.dimension().equals(LARGE_LEVEL);}
    public record Room(String owner,int slot,boolean created,String dimension,double x,double y,double z,float yaw,float pitch,int size) {
        public Room(String owner,int slot,boolean created,String dimension,double x,double y,double z,float yaw,float pitch){this(owner,slot,created,dimension,x,y,z,yaw,pitch,10);}
        public static final Codec<Room> CODEC=RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("owner").forGetter(Room::owner),Codec.INT.fieldOf("slot").forGetter(Room::slot),
            Codec.BOOL.fieldOf("created").forGetter(Room::created),Codec.STRING.fieldOf("dimension").forGetter(Room::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(Room::x),Codec.DOUBLE.fieldOf("y").forGetter(Room::y),Codec.DOUBLE.fieldOf("z").forGetter(Room::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(Room::yaw),Codec.FLOAT.fieldOf("pitch").forGetter(Room::pitch),
            Codec.intRange(10,20).optionalFieldOf("size",10).forGetter(Room::size)).apply(i,Room::new));
        public int originX(){return (slot%256)*32;}
        public int originZ(){return (slot/256)*32;}
        public boolean large(){return size==20;}
        public boolean contains(Vec3 p){return p.x>=originX()&&p.x<originX()+size&&p.y>=64&&p.y<64+size&&p.z>=originZ()&&p.z<originZ()+size;}
    }
    public record Visit(String visitor,String owner,boolean large,String dimension,double x,double y,double z,float yaw,float pitch) {
        public static final Codec<Visit> CODEC=RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("visitor").forGetter(Visit::visitor),Codec.STRING.fieldOf("owner").forGetter(Visit::owner),Codec.BOOL.fieldOf("large").forGetter(Visit::large),
            Codec.STRING.fieldOf("dimension").forGetter(Visit::dimension),Codec.DOUBLE.fieldOf("x").forGetter(Visit::x),Codec.DOUBLE.fieldOf("y").forGetter(Visit::y),Codec.DOUBLE.fieldOf("z").forGetter(Visit::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(Visit::yaw),Codec.FLOAT.fieldOf("pitch").forGetter(Visit::pitch)).apply(i,Visit::new));
    }
    public static final class Data extends SavedData {
        public static final Codec<Data> CODEC=RecordCodecBuilder.create(i -> i.group(Room.CODEC.listOf().fieldOf("rooms").forGetter(d -> d.rooms),Visit.CODEC.listOf().optionalFieldOf("visits",List.of()).forGetter(d -> d.visits)).apply(i,Data::new));
        public static final SavedDataType<Data> TYPE=new SavedDataType<>(TheDarkDimensions.id("pocket_rooms"),Data::new,CODEC);
        private final List<Room> rooms;
        private final List<Visit> visits;
        public Data(){this(List.of(),List.of());}
        public Data(List<Room> rooms){this(rooms,List.of());}
        public Data(List<Room> rooms,List<Visit> visits){this.rooms=new ArrayList<>(rooms);this.visits=new ArrayList<>(visits);}
        public Room get(UUID id){return get(id,false);}
        public Room get(UUID id,boolean large){return rooms.stream().filter(r -> r.owner.equals(id.toString())&&r.large()==large).findFirst().orElse(null);}
        public void put(Room room){rooms.removeIf(r -> r.owner.equals(room.owner)&&r.large()==room.large());rooms.add(room);setDirty();}
        public Visit visit(UUID id){return visits.stream().filter(v -> v.visitor.equals(id.toString())).findFirst().orElse(null);}
        public void visit(Visit v){clearVisit(UUID.fromString(v.visitor));visits.add(v);setDirty();}
        public void clearVisit(UUID id){if(visits.removeIf(v -> v.visitor.equals(id.toString())))setDirty();}
        public int nextSlot(){return rooms.stream().mapToInt(Room::slot).max().orElse(-1)+1;}
        public Room at(Level level,BlockPos pos){if(!isPocket(level))return null;return rooms.stream().filter(r -> r.large()==level.dimension().equals(LARGE_LEVEL)&&r.contains(Vec3.atCenterOf(pos))).findFirst().orElse(null);}
    }
    public static Data data(ServerPlayer p){return data(p.level());}
    public static Data data(ServerLevel l){return l.getServer().overworld().getDataStorage().computeIfAbsent(Data.TYPE);}
    public static Room current(ServerPlayer p){
        if(!isPocket(p.level()))return null;Data d=data(p);Visit v=d.visit(p.getUUID());
        if(v!=null&&v.large==p.level().dimension().equals(LARGE_LEVEL))return d.get(UUID.fromString(v.owner),v.large);
        // Compatibility for players who logged out in their room before visitor sessions existed.
        return d.get(p.getUUID(),p.level().dimension().equals(LARGE_LEVEL));
    }
    public static void init(){
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post e) -> {
            if(!(e.getEntity() instanceof ServerPlayer p))return;
            if(!isPocket(p.level())){if(data(p).visit(p.getUUID())!=null)data(p).clearVisit(p.getUUID());return;}
            Room room=current(p);
            if(room==null){leave(p);return;}
            if(!room.contains(p.position())){
                Vec3 safe=entry(p.level(),room);
                if(safe==null)leave(p);else p.teleport(new TeleportTransition(p.level(),safe,Vec3.ZERO,0,0,TeleportTransition.DO_NOTHING));
            }
        });
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> e.getDispatcher().register(Commands.literal("pocket")
            .then(Commands.literal("exit").executes(c -> {ServerPlayer p=c.getSource().getPlayerOrException();return isPocket(p.level())&&leave(p)?1:0;}))));
    }
    public static boolean toggle(ServerPlayer p){return toggle(p,false);}
    public static boolean toggle(ServerPlayer p,boolean large){return enter(p,p.getUUID(),large,true);}
    public static boolean enter(ServerPlayer p,UUID owner,boolean large,boolean create){
        Data d=data(p);Visit oldVisit=d.visit(p.getUUID());Room current=current(p);
        if(current!=null&&current.owner.equals(owner.toString())&&current.large()==large)return leave(p);
        ServerLevel l=p.level().getServer().getLevel(large?LARGE_LEVEL:LEVEL);
        if(l==null){p.sendSystemMessage(Component.translatable("message.thedarkdimensions.pocket_missing"),true);return false;}
        Room old=d.get(owner,large);boolean own=owner.equals(p.getUUID());
        if(old==null&&(!create||!own)){p.sendSystemMessage(Component.translatable("message.thedarkdimensions.foreign_missing"),true);return false;}
        // Preserve the original outside-world return point even when switching between rooms.
        Visit visit=oldVisit!=null&&isPocket(p.level())?new Visit(p.getUUID().toString(),owner.toString(),large,oldVisit.dimension,oldVisit.x,oldVisit.y,oldVisit.z,oldVisit.yaw,oldVisit.pitch):
            new Visit(p.getUUID().toString(),owner.toString(),large,p.level().dimension().identifier().toString(),p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot());
        if(isPocket(p.level())&&oldVisit==null&&current!=null)visit=new Visit(p.getUUID().toString(),owner.toString(),large,current.dimension,current.x,current.y,current.z,current.yaw,current.pitch);
        Room room=old==null?new Room(owner.toString(),d.nextSlot(),true,visit.dimension,visit.x,visit.y,visit.z,visit.yaw,visit.pitch,large?20:10):old;
        if(old==null||!old.created){build(l,room);d.put(room);}
        Vec3 pos=entry(l,room);
        if(pos==null){p.sendSystemMessage(Component.translatable("message.thedarkdimensions.pocket_full"),true);return false;}
        if(p.teleport(new TeleportTransition(l,pos,Vec3.ZERO,0,0,TeleportTransition.DO_NOTHING))==null)return false;
        d.visit(visit);
        if(own)d.put(new Room(room.owner,room.slot,true,visit.dimension,visit.x,visit.y,visit.z,visit.yaw,visit.pitch,room.size));
        return true;
    }
    public static void build(ServerLevel l,Room r){
        for(int x=-1;x<=r.size;x++)for(int y=63;y<=64+r.size;y++)for(int z=-1;z<=r.size;z++){
            boolean wall=x==-1||x==r.size||y==63||y==64+r.size||z==-1||z==r.size;
            l.setBlock(new BlockPos(r.originX()+x,y,r.originZ()+z),wall?SolarContent.POCKET_BOUNDARY.get().defaultBlockState():net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
        }
    }
    public static Vec3 entry(ServerLevel l,Room r){
        for(int y=64;y<=62+r.size;y++)for(int a=0;a<r.size*r.size;a++){
            int x=(a%r.size+r.size/2)%r.size,z=(a/r.size+r.size/2)%r.size;
            BlockPos p=new BlockPos(r.originX()+x,y,r.originZ()+z);
            if(l.getBlockState(p).isAir()&&l.getBlockState(p.above()).isAir()&&l.getBlockState(p.below()).isFaceSturdy(l,p.below(),Direction.UP))return Vec3.atBottomCenterOf(p);
        }return null;
    }
    public static boolean leave(ServerPlayer p){
        Data d=data(p);Visit v=d.visit(p.getUUID());Room r=current(p);
        String dimension=v!=null?v.dimension:r!=null?r.dimension:"minecraft:overworld";
        ServerLevel l=p.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION,net.minecraft.resources.Identifier.parse(dimension)));
        TeleportTransition t=l==null||isPocket(l)||v==null&&r==null?TeleportTransition.createDefault(p,TeleportTransition.DO_NOTHING):new TeleportTransition(l,new Vec3(v!=null?v.x:r.x,v!=null?v.y:r.y,v!=null?v.z:r.z),Vec3.ZERO,v!=null?v.yaw:r.yaw,v!=null?v.pitch:r.pitch,TeleportTransition.DO_NOTHING);
        if(p.teleport(t)==null)return false;d.clearVisit(p.getUUID());return true;
    }
    private PocketDimensions(){}
}
