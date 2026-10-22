package at.janic.thedarkdimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;

/** Fixed weapon identities; combat runs only from the confirmed sword-hit callback. */
public final class RelicKatanas {
    public static final DeferredItem<KatanaItem> BASIC=TheDarkDimensions.ITEMS.registerItem("basic_katana",p->new KatanaItem(p,null,0),p->p.sword(ToolMaterial.IRON,2,-2.4f).durability(480).enchantable(12).repairable(Items.IRON_INGOT));
    public static final DeferredItem<KatanaItem> MINDSHIFTER=TheDarkDimensions.ITEMS.registerItem("mindshifter",p->new KatanaItem(p,null,0),p->p.sword(ToolMaterial.DIAMOND,4.5f,-2.5f).durability(1450).enchantable(20).repairable(Items.SHULKER_SHELL).rarity(Rarity.RARE));
    public static final DeferredItem<KatanaItem> HELLSBLADE=TheDarkDimensions.ITEMS.registerItem("hellsblade",p->new KatanaItem(p,null,0),p->p.sword(ToolMaterial.NETHERITE,4.5f,-2.6f).durability(1800).enchantable(18).repairable(Items.REDSTONE_BLOCK).fireResistant().rarity(Rarity.EPIC));
    public static final ResourceKey<DamageType> PULSE=ResourceKey.create(Registries.DAMAGE_TYPE,TheDarkDimensions.id("hellsblade_pulse"));
    public static void init(){BASIC.getId();CrimsonRupture.init();net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e)->{
        if(e.getItemStack().is(MINDSHIFTER))e.getToolTip().add(1,net.minecraft.network.chat.Component.translatable("perk.thedarkdimensions.mindshift").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        if(e.getItemStack().is(HELLSBLADE))e.getToolTip().add(1,net.minecraft.network.chat.Component.translatable("perk.thedarkdimensions.crimson_rupture").withStyle(net.minecraft.ChatFormatting.RED));
    });}
    public static void hit(ItemStack weapon,LivingEntity target,LivingEntity attacker,boolean finisher){
        if(!(target.level() instanceof ServerLevel level)||!target.isAlive())return;
        if(weapon.is(HELLSBLADE))CrimsonRupture.hit(target,attacker,finisher);
    }
}

