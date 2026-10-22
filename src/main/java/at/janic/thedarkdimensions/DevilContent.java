package at.janic.thedarkdimensions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.*;

public final class DevilContent {
    public static final DeferredItem<Item> TEAR=TheDarkDimensions.ITEMS.registerSimpleItem("devils_tear",p -> p.fireResistant().rarity(Rarity.RARE));
    public static final DeferredItem<Item> EYE=TheDarkDimensions.ITEMS.registerSimpleItem("devils_eye",p -> p.fireResistant().rarity(Rarity.RARE));
    public static final DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENTS=DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<net.neoforged.neoforge.attachment.AttachmentType<?>,net.neoforged.neoforge.attachment.AttachmentType<Boolean>> DEVIL_PROJECTILE=ATTACHMENTS.register("devil_projectile",() -> net.neoforged.neoforge.attachment.AttachmentType.builder(() -> false).serialize(com.mojang.serialization.Codec.BOOL.fieldOf("devil")).sync(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());
    public static final DeferredHolder<MobEffect,MobEffect> DEVILS_CHARM=KatanaEffects.EFFECTS.register("devils_charm",Charm::new);
    public static final DeferredItem<DevilTridentItem> TRIDENT=TheDarkDimensions.ITEMS.registerItem("devils_trident",DevilTridentItem::new,p -> p.durability(2031).enchantable(15).fireResistant().repairable(BloodNetheriteContent.INGOT.get()).rarity(Rarity.EPIC)
        .component(DataComponents.TOOL,TridentItem.createToolProperties()).attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,7,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,-2.9,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build()));
    public static void charm(LivingEntity user) { if(!user.level().isClientSide())user.addEffect(new MobEffectInstance(DEVILS_CHARM,100)); }
    public static void impact(LivingEntity target,Entity attacker) {
        if(!target.level().isClientSide()&&target.isAlive()) {
            target.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,100),attacker);
            target.igniteForSeconds(4);
        }
    }
    private static final class Charm extends MobEffect {
        Charm() {super(MobEffectCategory.BENEFICIAL,0xb52a30);}
        @Override public boolean shouldApplyEffectTickThisTick(int ticks,int amplifier) {return ticks%5==0;}
        @Override public boolean applyEffectTick(ServerLevel level,LivingEntity user,int amplifier) {
            for(var villager:level.getEntitiesOfClass(Villager.class,user.getBoundingBox().inflate(8,4,8),v -> v.isAlive()&&v.distanceToSqr(user)<=64)) {
                var brain=villager.getBrain();
                brain.setMemoryWithExpiry(MemoryModuleType.NEAREST_HOSTILE,user,30);
                brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);brain.eraseMemory(MemoryModuleType.BREED_TARGET);
                brain.setActiveActivityIfPossible(Activity.PANIC);
                var flee=LandRandomPos.getPosAway(villager,12,4,user.position());
                if(flee==null)flee=LandRandomPos.getPosAway(villager,6,2,user.position());
                if(flee==null)flee=LandRandomPos.getPosAway(villager,3,1,user.position());
                if(flee!=null)brain.setMemory(MemoryModuleType.WALK_TARGET,new WalkTarget(flee,1.2f,0));
            }
            return true;
        }
    }
    public static void init(net.neoforged.bus.api.IEventBus bus) {ATTACHMENTS.register(bus);}
    private DevilContent() {}
}
