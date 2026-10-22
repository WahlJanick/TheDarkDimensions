package at.janic.thedarkdimensions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class Bestiary {
    public static final List<String> SPECIES=List.of("mummy","fire_mage","glowing_bee","hollow_watcher","chronus_mage","ashen_wraith","bonewing","veilwood_spider","drowned_sentinel","cinder_broodmother","lantern_wisp","briar_whisperer","dune_stalker","sanctum_keeper","ashen_sovereign","ashbound_regent");
    public record Entry(int kills,List<String> drops){
        public static final Codec<Entry> CODEC=RecordCodecBuilder.create(i->i.group(Codec.intRange(0,1000000).fieldOf("kills").forGetter(Entry::kills),Codec.STRING.listOf().fieldOf("drops").forGetter(Entry::drops)).apply(i,Entry::new));
    }
    public static final class Data {
        public final Map<String,Entry> entries;
        public Data(){this(Map.of());}public Data(Map<String,Entry> entries){this.entries=new HashMap<>(entries);}
        public static final Codec<Data> CODEC=Codec.unboundedMap(Codec.STRING,Entry.CODEC).xmap(Data::new,d->d.entries);
    }
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Data>> RECORDS=DevilContent.ATTACHMENTS.register("bestiary",()->AttachmentType.builder(()->new Data()).serialize(Data.CODEC.fieldOf("records")).copyOnDeath().build());
    public static void discover(ServerPlayer player,String species){
        if(!SPECIES.contains(species))return;
        var data=player.getData(RECORDS);data.entries.putIfAbsent(species,new Entry(0,List.of()));
        if(data.entries.keySet().containsAll(SPECIES))ExplorationContent.award(player,"bestiary_complete");
    }
    public static void observeDrops(ServerPlayer player,String species,List<String> drops,boolean killed){
        discover(player,species);if(!SPECIES.contains(species))return;
        var data=player.getData(RECORDS);var old=data.entries.get(species);Set<String> all=new TreeSet<>(old.drops());all.addAll(drops);
        data.entries.put(species,new Entry(Math.min(1000000,old.kills()+(killed?1:0)),List.copyOf(all)));
        if(killed&&species.equals("sanctum_keeper"))ExplorationContent.award(player,"keeper_defeated");
        if(killed&&species.equals("cinder_broodmother"))ExplorationContent.award(player,"broodmother_defeated");
        if(killed&&species.equals("ashen_sovereign"))ExplorationContent.award(player,"sovereign_defeated");
    }
    public static void refresh(ServerPlayer player,ItemStack book){
        var data=player.getData(RECORDS);List<Filterable<Component>> pages=new ArrayList<>();
        pages.add(Filterable.passThrough(Component.translatable("bestiary.thedarkdimensions.intro",data.entries.size(),SPECIES.size())));
        for(String species:SPECIES){
            var entry=data.entries.get(species);
            if(entry==null){pages.add(Filterable.passThrough(Component.translatable("bestiary.thedarkdimensions.unknown")));continue;}
            var page=Component.empty().append(Component.translatable("entity.thedarkdimensions."+species).withStyle(ChatFormatting.BOLD)).append("\n\n")
                .append(Component.translatable("bestiary.thedarkdimensions."+species+".habitat")).append("\n\n")
                .append(Component.translatable("bestiary.thedarkdimensions.kills",entry.kills()));
            pages.add(Filterable.passThrough(page));
            pages.add(Filterable.passThrough(Component.empty().append(Component.translatable("entity.thedarkdimensions."+species).withStyle(ChatFormatting.BOLD)).append("\n\n").append(Component.translatable("bestiary.thedarkdimensions."+species+".behavior"))));
            if(!entry.drops().isEmpty()){
                var loot=Component.empty().append(Component.translatable("entity.thedarkdimensions."+species).withStyle(ChatFormatting.BOLD)).append("\n\n").append(Component.translatable("bestiary.thedarkdimensions.drops")).append("\n");
                for(String id:entry.drops()){
                    var item=BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(id));
                    if(item!=null)loot.append("• ").append(Component.translatable(item.getDescriptionId())).append("\n");
                }
                pages.add(Filterable.passThrough(loot));
            }
        }
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,new WrittenBookContent(Filterable.passThrough("Ashbound Bestiary"),"The Dark Dimensions",0,pages,true));
    }
    public static final class Item extends net.minecraft.world.item.Item {
        public Item(Properties p){super(p);}
        @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
            if(player instanceof ServerPlayer server){
                var book=server.getItemInHand(hand);refresh(server,book);server.inventoryMenu.broadcastChanges();server.containerMenu.broadcastChanges();server.openItemGui(book,hand);
            }
            return InteractionResult.SUCCESS;
        }
    }
    public static void init(){
        // Force attachment registration before the common attachment registry is registered.
        RECORDS.getId();
        var bus=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        bus.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e)->{
            if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator()||p.tickCount%40!=0)return;
            for(var mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(24),m->m.isAlive()&&p.hasLineOfSight(m))){
                var id=BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());if(id.getNamespace().equals(TheDarkDimensions.MOD_ID))discover(p,id.getPath());
            }
            if(p.level().dimension().equals(DarkContent.BURNED_REALM)){
                ExplorationContent.award(p,"enter_realm");
                if(p.tickCount%200==0){
                    if(MobExpansionContent.nearGeode(p.level(),p.blockPosition()))ExplorationContent.award(p,"find_geode");
                    for(var structure:p.level().structureManager().getAllStructuresAt(p.blockPosition()).keySet()){
                        var key=p.level().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).getKey(structure);
                        if(key==null)continue;String path=key.getPath();
                        if(key.getNamespace().equals(TheDarkDimensions.MOD_ID)&&(path.equals("chronus_sanctum")||path.equals("sunken_archive"))&&p.level().structureManager().getStructureAt(p.blockPosition(),structure).isValid()){
                            ExplorationContent.award(p,path.equals("chronus_sanctum")?"find_sanctum":"find_archive");
                            if(path.equals("chronus_sanctum")&&p.getRandom().nextFloat()<.3f){
                                p.level().playSound(null,p.blockPosition(),net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,net.minecraft.sounds.SoundSource.AMBIENT,.25f,.65f);
                                p.level().sendParticles(p,net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,false,false,p.getX(),p.getY()+1,p.getZ(),8,2,1,2,.01);
                            }
                        }
                    }
                }
            }
        });
        bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(net.neoforged.neoforge.event.entity.living.LivingDropsEvent e)->{
            if(e.isCanceled()||!(e.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level))return;
            var id=BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType());if(!id.getNamespace().equals(TheDarkDimensions.MOD_ID)||!SPECIES.contains(id.getPath()))return;
            ServerPlayer killer=e.getSource().getEntity() instanceof ServerPlayer p?p:e.getEntity().getKillCredit() instanceof ServerPlayer p?p:null;
            List<String> drops=e.getDrops().stream().map(d->BuiltInRegistries.ITEM.getKey(d.getItem().getItem()).toString()).distinct().toList();
            if(killer!=null)observeDrops(killer,id.getPath(),drops,true);
            for(var player:level.players())if(player!=killer&&player.distanceToSqr(e.getEntity())<=576&&player.hasLineOfSight(e.getEntity()))observeDrops(player,id.getPath(),drops,false);
        });
    }
}
