package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.*;

/** Per-world receipts and queued offline rewards are independent of loaded structures. */
public final class TrialLedger extends SavedData {
    public record Reward(String site,int kind,boolean ashfall){
        public static final Codec<Reward> CODEC=RecordCodecBuilder.create(i->i.group(Codec.STRING.fieldOf("site").forGetter(Reward::site),Codec.intRange(0,2).fieldOf("kind").forGetter(Reward::kind),Codec.BOOL.fieldOf("ashfall").forGetter(Reward::ashfall)).apply(i,Reward::new));
    }
    public final Map<String,List<String>> defeated;public final Map<String,List<Reward>> pending;public final Map<String,Integer> granted;public final Set<String> receipts;
    public TrialLedger(){this(Map.of(),Map.of(),Map.of(),List.of());}
    private TrialLedger(Map<String,List<String>> defeated,Map<String,List<Reward>> pending,Map<String,Integer> granted,List<String> receipts){this.defeated=new HashMap<>();defeated.forEach((k,v)->this.defeated.put(k,new ArrayList<>(v)));this.pending=new HashMap<>();pending.forEach((k,v)->this.pending.put(k,new ArrayList<>(v)));this.granted=new HashMap<>(granted);this.receipts=new HashSet<>(receipts);}
    public static final Codec<TrialLedger> CODEC=RecordCodecBuilder.create(i->i.group(Codec.unboundedMap(Codec.STRING,Codec.STRING.listOf()).fieldOf("defeated").forGetter(d->d.defeated),Codec.unboundedMap(Codec.STRING,Reward.CODEC.listOf()).fieldOf("pending").forGetter(d->d.pending),Codec.unboundedMap(Codec.STRING,Codec.intRange(0,7)).fieldOf("granted").forGetter(d->d.granted),Codec.STRING.listOf().fieldOf("receipts").forGetter(d->List.copyOf(d.receipts))).apply(i,TrialLedger::new));
    public static final SavedDataType<TrialLedger> TYPE=new SavedDataType<>(TheDarkDimensions.id("ancient_resonance"),TrialLedger::new,CODEC);
    public static TrialLedger data(ServerLevel level){return level.getDataStorage().computeIfAbsent(TYPE);}
    public void defeat(String site,UUID mob){var list=defeated.computeIfAbsent(site,k->new ArrayList<>());if(!list.contains(mob.toString())){list.add(mob.toString());setDirty();}}
    public void offer(UUID player,Reward reward){String id=player.toString();if(receipts.contains(id+"/"+reward.site))return;var rewards=pending.computeIfAbsent(id,k->new ArrayList<>());if(rewards.stream().noneMatch(r->r.site.equals(reward.site))){rewards.add(reward);setDirty();}}
    public void deliver(ServerPlayer p){
        String id=p.getUUID().toString();var progress=ResonanceProgress.data(p);progress.completed|=granted.getOrDefault(id,0);
        sync(p,progress.completed);var rewards=pending.remove(id);if(rewards==null)return;
        for(var reward:rewards){
            if(!receipts.add(id+"/"+reward.site))continue;
            int mask=1<<reward.kind;boolean first=(progress.completed&mask)==0;progress.completed|=mask;granted.put(id,progress.completed);
            if(first)give(p,new ItemStack(TrialKind.values()[reward.kind].sigil()));
            give(p,new ItemStack(SpellContent.METALINIUM_INGOT.get(),reward.ashfall?3:2));give(p,new ItemStack(MineralContent.ESSENTIALIUM.get(),reward.ashfall?3:2));give(p,new ItemStack(TheDarkDimensions.ASHES.get(),reward.ashfall?8:5));give(p,new ItemStack(MineralContent.CHRONUS_ORB.get()));
            if(Math.floorMod(reward.site.hashCode(),5)==0)give(p,new ItemStack(TrialKind.values()[reward.kind].core()));
            p.sendSystemMessage(Component.translatable("resonance.thedarkdimensions.reward",Component.translatable(TrialKind.values()[reward.kind].title())),false);
        }
        sync(p,progress.completed);setDirty();p.inventoryMenu.broadcastChanges();
    }
    private static void sync(ServerPlayer p,int mask){if(p.getData(ResonanceProgress.UNLOCKS)!=mask)p.setData(ResonanceProgress.UNLOCKS,mask);for(var kind:TrialKind.values())if((mask&kind.mask())!=0)award(p,"complete_"+kind.getSerializedName());if(mask==7)award(p,"complete_all");}
    private static void award(ServerPlayer p,String name){var a=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("resonance/"+name));if(a!=null)p.getAdvancements().award(a,"done");}
    private static void give(ServerPlayer p,ItemStack stack){if(!p.getInventory().add(stack))p.drop(stack,false,net.minecraft.util.Prediction.SERVER_ONLY);}
}
