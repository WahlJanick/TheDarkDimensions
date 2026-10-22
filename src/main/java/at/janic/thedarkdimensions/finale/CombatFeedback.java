package at.janic.thedarkdimensions.finale;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.*;

/** Server-confirmed cues; no additional hit processing or required combat dependency. */
public final class CombatFeedback {
    public static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(Registries.SOUND_EVENT,TheDarkDimensions.MOD_ID);
    public static final List<String> NAMES=List.of("perfect_parry","perfect_dodge","rune_ember","rune_tide","rune_radiance","rune_chronus","trial_complete","rare_loot","regent_awaken","regent_defeated","katana_finisher");
    public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> EVENTS=new LinkedHashMap<>();
    static{for(String name:NAMES)EVENTS.put(name,SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(TheDarkDimensions.id(name))));}
    private static final WeakHashMap<LivingEntity,Map<String,Long>> READY=new WeakHashMap<>();
    public static boolean play(LivingEntity user,String name,Vec3 at,int color){
        if(!(user.level() instanceof ServerLevel level)||!EVENTS.containsKey(name))return false;
        long now=level.getServer().overworld().getGameTime();var ready=READY.computeIfAbsent(user,k->new HashMap<>());if(now<ready.getOrDefault(name,Long.MIN_VALUE))return false;ready.put(name,now+(name.equals("rare_loot")?30:6));
        if(Set.of("rare_loot","trial_complete","regent_defeated").contains(name)&&user instanceof ServerPlayer p)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(EVENTS.get(name),SoundSource.PLAYERS,at.x,at.y,at.z,name.equals("rare_loot")?.28f:.6f,1,level.getRandom().nextLong()));
        else level.playSound(null,at.x,at.y,at.z,EVENTS.get(name).get(),SoundSource.PLAYERS,name.contains("defeated")?.65f:.4f,1);
        if(color!=0)level.sendParticles(new DustParticleOptions(color,.85f),at.x,at.y,at.z,name.equals("trial_complete")?16:7,.28,.3,.28,.015);
        return true;
    }
    public static boolean rare(ItemStack stack){return !stack.isEmpty()&&(stack.getRarity()==Rarity.RARE||stack.getRarity()==Rarity.EPIC||stack.is(SolarContent.SOLAR_ESSENCE)||at.janic.thedarkdimensions.resonance.ResonanceContent.SIGILS.stream().anyMatch(stack::is));}
    public static void init(net.neoforged.bus.api.IEventBus bus){
        SOUNDS.register(bus);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post e)->{if(e.getPlayer() instanceof ServerPlayer p&&rare(e.getOriginalStack())&&e.getOriginalStack().getCount()>e.getCurrentStack().getCount())play(p,"rare_loot",p.position().add(0,1,0),0);});
    }
}

