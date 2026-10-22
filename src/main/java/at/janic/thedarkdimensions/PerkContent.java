package at.janic.thedarkdimensions;

import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.consume_effects.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class PerkContent {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES=DeferredRegister.create(Registries.RECIPE_SERIALIZER,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<LuminousCoatingRecipe>> COATING_RECIPE=RECIPES.register("apply_luminous_coating",()->LuminousCoatingRecipe.SERIALIZER);
    public static final TagKey<Item> COATABLE=TagKey.create(Registries.ITEM,TheDarkDimensions.id("coatable_weapons"));
    public static final DeferredItem<Item> LUMINOUS_COATING=TheDarkDimensions.ITEMS.registerSimpleItem("luminous_coating",p->p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> ASTRAL_PRISM=TheDarkDimensions.ITEMS.registerSimpleItem("astral_prism",p->p.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Item> SAKURA_TALISMAN=TheDarkDimensions.ITEMS.registerSimpleItem("sakura_talisman",p->p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> RONIN_BANDAGE=TheDarkDimensions.ITEMS.registerSimpleItem("ronin_bandage",p->p.stacksTo(16).component(DataComponents.CONSUMABLE,Consumables.defaultFood().consumeSeconds(1.6f).hasConsumeParticles(false).onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION,100,1))).onConsume(new RemoveStatusEffectsConsumeEffect(KatanaEffects.FROSTED)).build()));
    public static final DeferredItem<Item> WINDSTEP_SANDALS=TheDarkDimensions.ITEMS.registerSimpleItem("windstep_sandals",p->p.humanoidArmor(ArmorMaterials.LEATHER,ArmorType.BOOTS).durability(240).rarity(Rarity.UNCOMMON));
    private static final WeakHashMap<ServerPlayer,Long> DASH_READY=new WeakHashMap<>();
    public static boolean isWeapon(ItemStack s){return s.has(DataComponents.WEAPON)||s.getItem() instanceof ProjectileWeaponItem||s.is(COATABLE)||s.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.EMPTY).modifiers().stream().anyMatch(e->e.attribute().equals(Attributes.ATTACK_DAMAGE)&&e.modifier().amount()>0);}
    public static boolean coated(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBooleanOr("luminous_coating",false);}
    public static void coat(ItemStack s){CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putBoolean("luminous_coating",true));}
    public static boolean accessory(Item i){return i==ASTRAL_PRISM.get()||i==SAKURA_TALISMAN.get()||i==ExplorationContent.HOURGLASS.get();}
    public static boolean equipped(LivingEntity user,Item item){
        // Better Combat hides the effective offhand for two-handed weapons; read the stored accessory.
        ItemStack offhand=user instanceof Player p?p.getInventory().getItem(net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND):user.getOffhandItem();
        if(offhand.is(item))return true;
        return user instanceof Player p&&((net.neoforged.fml.ModList.get().isLoaded("curios")&&CuriosMagic.equipped(p,item))||(net.neoforged.fml.ModList.get().isLoaded("trinkets_updated")&&TrinketsMagic.equipped(p,item)));
    }
    public static int comboWindow(LivingEntity p){return equipped(p,SAKURA_TALISMAN.get())?60:40;}
    public static int cooldown(Player p,int base){return equipped(p,ASTRAL_PRISM.get())?Math.max(1,Math.round(base*.95f)):base;}
    public static int cooldown(Player p,int base,MagicSkill skill){
        if(skill.element()==MagicElement.CHRONUS&&equipped(p,ExplorationContent.HOURGLASS.get()))return Math.max(1,Math.round(base*.85f));
        return cooldown(p,base);
    }
    public static boolean dash(ServerPlayer p){
        if(!p.isAlive()||p.isSpectator()||p.containerMenu!=p.inventoryMenu||!p.onGround()||KatanaDefense.dodging(p)||!BackEquipment.sandals(p))return false;
        long now=p.level().getGameTime();if(now<DASH_READY.getOrDefault(p,Long.MIN_VALUE))return false;
        if(!MagicCasting.move(p,3))return false;
        DASH_READY.put(p,now+100);BackEquipment.hurtSandals(p);
        p.level().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,p.getX(),p.getY()+.2,p.getZ(),12,.4,.1,.4,.03);return true;
    }
    public static void hit(LivingDamageEvent.Post event){
        var victim=event.getEntity();var source=event.getSource();if(victim.level().isClientSide()||event.getHealthDamage()<=0||victim==source.getEntity())return;
        if(!(source.getDirectEntity() instanceof LivingEntity)&&!(source.getDirectEntity() instanceof Projectile))return;
        if(source.is(DamageTypeTags.IS_FIRE)||source.is(net.minecraft.world.damagesource.DamageTypes.MAGIC)||source.is(net.minecraft.world.damagesource.DamageTypes.INDIRECT_MAGIC)||source.is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT))return;
        ItemStack weapon=source.getWeaponItem();
        if(weapon!=null&&coated(weapon))victim.addEffect(new MobEffectInstance(MobEffects.GLOWING,160,0));
    }
    public static void init(IEventBus bus){
        RECIPES.register(bus);
        var events=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;events.addListener(PerkContent::hit);
        events.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e)->{
            if(e.getItemStack().is(LUMINOUS_COATING))e.getToolTip().add(Component.translatable("perk.thedarkdimensions.coating_apply").withStyle(ChatFormatting.AQUA));
            if(coated(e.getItemStack())){var lines=e.getToolTip();int index=1;var enchantments=e.getItemStack().get(DataComponents.ENCHANTMENTS);if(enchantments!=null&&e.getDisplay().shows(DataComponents.ENCHANTMENTS))index+=enchantments.size();lines.add(Math.min(index,lines.size()),Component.translatable("perk.thedarkdimensions.luminous_coating").withStyle(ChatFormatting.GRAY));}
            if(e.getItemStack().is(ASTRAL_PRISM))e.getToolTip().add(Component.translatable("perk.thedarkdimensions.astral_prism").withStyle(ChatFormatting.AQUA));
            if(e.getItemStack().is(ExplorationContent.HOURGLASS))e.getToolTip().add(Component.translatable("perk.thedarkdimensions.timeworn_hourglass").withStyle(ChatFormatting.LIGHT_PURPLE));
            if(e.getItemStack().is(SAKURA_TALISMAN))e.getToolTip().add(Component.translatable("perk.thedarkdimensions.sakura_talisman").withStyle(ChatFormatting.LIGHT_PURPLE));
            if(e.getItemStack().is(RONIN_BANDAGE))e.getToolTip().add(Component.translatable("perk.thedarkdimensions.ronin_bandage").withStyle(ChatFormatting.GREEN));
            if(e.getItemStack().is(WINDSTEP_SANDALS))e.getToolTip().add(Component.translatable("perk.thedarkdimensions.windstep_sandals").withStyle(ChatFormatting.AQUA));
        });
    }
}
