package at.janic.thedarkdimensions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

/** Common entry points never load Trinkets classes when the optional mod is absent. */
public final class BackEquipment {
    public static final ResourceKey<EquipmentAsset> GLIDER_ASSET=ResourceKey.create(EquipmentAssets.ROOT_ID,TheDarkDimensions.id("wool_glider"));
    public static final ResourceKey<EquipmentAsset> WINGS_ASSET=ResourceKey.create(EquipmentAssets.ROOT_ID,TheDarkDimensions.id("devils_wings"));
    private static Item.Properties flight(Item.Properties p,ResourceKey<EquipmentAsset> asset){return p.component(DataComponents.GLIDER,Unit.INSTANCE).component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.CHEST).setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(asset).setDamageOnHurt(false).build());}
    public static final DeferredItem<Item> GLIDER=TheDarkDimensions.ITEMS.registerSimpleItem("wool_glider",p->flight(p.durability(128).repairable(Items.LEATHER),GLIDER_ASSET));
    public static final DeferredItem<Item> WINGS=TheDarkDimensions.ITEMS.registerSimpleItem("devils_wings",p->flight(p.stacksTo(1).fireResistant().rarity(Rarity.EPIC).component(DataComponents.UNBREAKABLE,Unit.INSTANCE),WINGS_ASSET));
    public static boolean trinkets(){return net.neoforged.fml.ModList.get().isLoaded("trinkets_updated");}
    public static boolean tool(ItemStack stack){return !stack.isEmpty()&&!stack.has(DataComponents.GLIDER)&&(stack.has(DataComponents.TOOL)||PerkContent.isWeapon(stack)||stack.isDamageableItem()&&!stack.has(DataComponents.EQUIPPABLE)||stack.is(Items.SPYGLASS)||stack.is(net.neoforged.neoforge.common.Tags.Items.TOOLS));}
    public static ItemStack stored(Player p){return trinkets()?TrinketsMagic.getSlot(p,TrinketsMagic.BACK_SLOT,false):ItemStack.EMPTY;}
    public static ItemStack activeGlider(LivingEntity e){
        var chest=e.getItemBySlot(EquipmentSlot.CHEST);if(LivingEntity.canGlideUsing(chest,EquipmentSlot.CHEST))return chest;
        if(e instanceof Player p&&trinkets()){var back=TrinketsMagic.getSlot(p,TrinketsMagic.GLIDER_SLOT,true);if(back.has(DataComponents.GLIDER)&&!back.nextDamageWillBreak())return back;}
        return ItemStack.EMPTY;
    }
    public static boolean devilWings(LivingEntity e){return activeGlider(e).is(WINGS);}
    public static boolean lavaFlight(LivingEntity e){return devilWings(e)&&e.isInLava()&&!e.isInWater();}
    public static boolean rocketBlocked(LivingEntity e){return e.isFallFlying()&&activeGlider(e).is(GLIDER);}
    public static boolean swap(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&p.containerMenu==p.inventoryMenu&&trinkets()&&TrinketsMagic.swapBack(p);}
    public static boolean sandals(Player p){return p.getItemBySlot(EquipmentSlot.FEET).is(PerkContent.WINDSTEP_SANDALS)||trinkets()&&TrinketsMagic.getSlot(p,TrinketsMagic.FEET_SLOT,true).is(PerkContent.WINDSTEP_SANDALS);}
    public static void hurtSandals(ServerPlayer p){
        if(p.getItemBySlot(EquipmentSlot.FEET).is(PerkContent.WINDSTEP_SANDALS))p.getItemBySlot(EquipmentSlot.FEET).hurtAndBreak(1,p,EquipmentSlot.FEET);
        else if(trinkets())TrinketsMagic.hurtSlot(p,TrinketsMagic.FEET_SLOT,1);
    }
    public static void wear(ServerPlayer p){
        if(!p.isAlive()||p.isCreative()||!p.isFallFlying()||p.getFallFlyingTicks()==0||p.getFallFlyingTicks()%20!=0)return;
        var active=activeGlider(p);if(!active.is(GLIDER))return;
        int extra=Math.min(3,Math.max(0,active.getMaxDamage()-1-active.getDamageValue()));if(extra==0)return;
        if(active==p.getItemBySlot(EquipmentSlot.CHEST))active.hurtAndBreak(extra,p,EquipmentSlot.CHEST);
        else if(trinkets())TrinketsMagic.hurtSlot(p,TrinketsMagic.GLIDER_SLOT,extra);
    }
    public static void init(IEventBus bus){
        GLIDER.getId();WINGS.getId();
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e)->e.enqueueWork(()->{if(trinkets())TrinketsMagic.registerPredicates();}));
        var events=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        events.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e)->{if(e.getEntity() instanceof ServerPlayer p)wear(p);});
        events.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e)->{if(e.getItemStack().is(GLIDER))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("perk.thedarkdimensions.wool_glider").withStyle(net.minecraft.ChatFormatting.GRAY));if(e.getItemStack().is(WINGS))e.getToolTip().add(net.minecraft.network.chat.Component.translatable("perk.thedarkdimensions.devils_wings").withStyle(net.minecraft.ChatFormatting.GRAY));});
    }
}
