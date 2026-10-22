package at.janic.thedarkdimensions.finale;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.resonance.ResonanceContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class FinaleContent {
    public static final DeferredBlock<NexusAltarBlock> ALTAR=TheDarkDimensions.BLOCKS.registerBlock("nexus_altar",NexusAltarBlock::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS).strength(-1,3600000).noLootTable().lightLevel(s->s.getValue(NexusAltarBlock.MASK)==7?12:5));
    public static final DeferredBlock<NexusGateBlock> GATE=TheDarkDimensions.BLOCKS.registerBlock("nexus_gate",NexusGateBlock::new,()->BlockBehaviour.Properties.ofFullCopy(Blocks.TINTED_GLASS).strength(-1,3600000).noLootTable().lightLevel(s->5));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<NexusAltar>> ALTAR_ENTITY=ResonanceContent.BLOCK_ENTITIES.register("nexus_altar",()->new BlockEntityType<>(NexusAltar::new,ALTAR.get()));
    public static final DeferredHolder<EntityType<?>,EntityType<AshboundRegent>> REGENT=TheDarkDimensions.ENTITIES.register("ashbound_regent",()->EntityType.Builder.of(AshboundRegent::new,MobCategory.MONSTER).sized(.85f,2.65f).clientTrackingRange(14).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("ashbound_regent"))));
    public static final DeferredItem<SpawnEggItem> EGG=TheDarkDimensions.ITEMS.registerItem("ashbound_regent_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(REGENT.get()));
    public static final DeferredItem<Item> TROPHY=TheDarkDimensions.ITEMS.registerSimpleItem("regents_heart",p->p.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    static{TheDarkDimensions.ITEMS.registerSimpleBlockItem(ALTAR);}
    public static void init(net.neoforged.bus.api.IEventBus bus){
        CombatFeedback.init(bus);FinalProgress.init();
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(REGENT.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,260).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,.26).add(Attributes.FOLLOW_RANGE,36).add(Attributes.KNOCKBACK_RESISTANCE,.7).build()));
        var events=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        events.addListener(net.neoforged.bus.api.EventPriority.LOW,(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e)->{if(!e.isCanceled()&&e.getEntity() instanceof AshboundRegent boss)boss.ward(e);});
        events.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e)->{if(!e.isCanceled()&&e.getEntity() instanceof AshboundRegent boss&&!boss.isPractice()&&boss.hasArena()&&boss.level() instanceof net.minecraft.server.level.ServerLevel l)FinalLedger.data(l).defeated(boss.getUUID());});
    }
}
