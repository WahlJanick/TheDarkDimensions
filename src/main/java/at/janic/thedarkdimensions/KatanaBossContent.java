package at.janic.thedarkdimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class KatanaBossContent {
    public static final ResourceKey<Structure> DOJO=ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id("ashen_dojo"));
    public static final DeferredHolder<EntityType<?>,EntityType<AshenSovereign>> SOVEREIGN=TheDarkDimensions.ENTITIES.register("ashen_sovereign",()->EntityType.Builder.of(AshenSovereign::new,MobCategory.MONSTER).sized(.65f,1.95f).clientTrackingRange(12).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("ashen_sovereign"))));
    public static final DeferredItem<SpawnEggItem> EGG=TheDarkDimensions.ITEMS.registerItem("ashen_sovereign_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(SOVEREIGN.get()));
    public static void init(IEventBus bus){
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(SOVEREIGN.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,120).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,.29).add(Attributes.FOLLOW_RANGE,24).add(Attributes.KNOCKBACK_RESISTANCE,.45).build()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOW,(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e)->{
            if(!e.isCanceled()&&e.getEntity() instanceof AshenSovereign boss)boss.guard(e);
        });
    }
}
