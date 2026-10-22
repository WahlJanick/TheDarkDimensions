package at.janic.thedarkdimensions.resonance;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.mixin.*;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class AncientRunes {
    public static int remaining(ItemCooldowns cooldowns,net.minecraft.resources.Identifier group){var access=(CooldownsAccess)cooldowns;var instance=access.darkDimensions$cooldowns().get(group);return instance==null?0:Math.max(0,((CooldownInstanceAccess)instance).darkDimensions$end()-access.darkDimensions$clock());}
    public static final String KEY="ancient_resonance_rune";
    public static int rune(ItemStack weapon){String id=weapon.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(KEY,"");for(int i=0;i<4;i++)if(id.equals(name(i)))return i;return -1;}
    public static String name(int rune){return switch(rune){case 0->"ember";case 1->"tide";case 2->"radiance";case 3->"chronus";default->"";};}
    public static int ingredient(ItemStack stack){for(int i=0;i<4;i++)if(stack.is(ResonanceContent.RUNES.get(i)))return i;return -1;}
    public static boolean hasRune(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().contains(KEY);}
    public static ItemStack bind(ItemStack weapon,int rune){if(!PerkContent.isWeapon(weapon)||hasRune(weapon)||rune<0||rune>3)return ItemStack.EMPTY;var copy=weapon.copyWithCount(1);CustomData.update(DataComponents.CUSTOM_DATA,copy,t->t.putString(KEY,name(rune)));return copy;}
    public static ItemStack unbind(ItemStack weapon){if(rune(weapon)<0)return ItemStack.EMPTY;var copy=weapon.copyWithCount(1);CustomData.update(DataComponents.CUSTOM_DATA,copy,t->t.remove(KEY));return copy;}
    private static final WeakHashMap<LivingEntity,Map<String,Long>> MOB_READY=new WeakHashMap<>();
    private static Map<String,Long> ready(LivingEntity attacker){return attacker instanceof ServerPlayer p?ResonanceProgress.data(p).runeReady:MOB_READY.computeIfAbsent(attacker,k->new HashMap<>());}
    private static boolean spend(LivingEntity attacker,int rune,int cooldown){long now=attacker.level().getServer().overworld().getGameTime();var ready=ready(attacker);String key=name(rune);if(now<ready.getOrDefault(key,Long.MIN_VALUE))return false;ready.put(key,now+cooldown);return true;}
    public static void hit(LivingDamageEvent.Post e){
        if(e.getHealthDamage()<=0||e.getEntity().level().isClientSide()||!(e.getSource().getDirectEntity() instanceof LivingEntity attacker)||attacker==e.getEntity())return;
        var source=e.getSource();if(!source.is(net.minecraft.tags.DamageTypeTags.IS_PLAYER_ATTACK)&&!source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK)&&!source.is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK_NO_AGGRO))return;
        ItemStack weapon=source.getWeaponItem();if(weapon==null)return;int rune=rune(weapon);var target=e.getEntity();
        switch(rune){
            case 0->{if(spend(attacker,0,120)){target.igniteForSeconds(2);at.janic.thedarkdimensions.finale.CombatFeedback.play(attacker,"rune_ember",target.position().add(0,1,0),0xe98d39);}}
            case 1->{if(attacker.isOnFire()&&spend(attacker,1,160)){attacker.clearFire();at.janic.thedarkdimensions.finale.CombatFeedback.play(attacker,"rune_tide",attacker.position().add(0,1,0),0x63bdd7);}}
            case 2->{if(target.hasEffect(MobEffects.GLOWING)&&spend(attacker,2,120)){target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,0),attacker);at.janic.thedarkdimensions.finale.CombatFeedback.play(attacker,"rune_radiance",target.position().add(0,1,0),0xeedb83);}}
            default->{}
        }
    }
    public static void perfectDodge(ServerPlayer p){
        if(rune(p.getMainHandItem())!=3||ResonanceProgress.data(p).lastSpell!=MagicElement.CHRONUS.ordinal())return;
        int source=MagicBooks.active(p);if(source<0)return;var book=MagicBooks.get(p,source);var d=MagicData.read(book);int selected=d.slots[d.selected];
        if(!d.knows(selected)||MagicSkill.values()[selected].element()!=MagicElement.CHRONUS)return;
        var group=p.getCooldowns().getCooldownGroup(book);if(remaining(p.getCooldowns(),group)<=0||!spend(p,3,200))return;
        Set<net.minecraft.resources.Identifier> groups=new HashSet<>();for(var element:MagicElement.values())groups.add(p.getCooldowns().getCooldownGroup(new ItemStack(MagicContent.book(element))));groups.add(p.getCooldowns().getCooldownGroup(new ItemStack(MagicContent.MASTER_BOOK.get())));
        for(var id:groups){int left=remaining(p.getCooldowns(),id);if(left>0){if(left<=20)p.getCooldowns().removeCooldown(id);else p.getCooldowns().addCooldown(id,left-20);}}
        at.janic.thedarkdimensions.finale.CombatFeedback.play(p,"rune_chronus",p.position().add(0,1,0),0x9a85c7);
    }
    public static void init(){
        var bus=net.neoforged.neoforge.common.NeoForge.EVENT_BUS;bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST,AncientRunes::hit);
        bus.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent e)->{
            var stack=e.getItemStack();int rune=rune(stack);if(rune>=0)e.getToolTip().add(Component.translatable("perk.thedarkdimensions.rune_"+name(rune)).withStyle(net.minecraft.ChatFormatting.GRAY));
            int ingredient=ingredient(stack);if(ingredient>=0)e.getToolTip().add(Component.translatable("resonance.thedarkdimensions.requires_"+name(ingredient)).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            for(var kind:TrialKind.values())if(stack.is(kind.core()))e.getToolTip().add(Component.translatable("resonance.thedarkdimensions.core_cost",Component.translatable(kind.title())).withStyle(net.minecraft.ChatFormatting.GRAY));
            if(stack.is(ResonanceContent.RUNESTONE.get().asItem()))e.getToolTip().add(Component.translatable("resonance.thedarkdimensions.runestone_help").withStyle(net.minecraft.ChatFormatting.GRAY));
        });
    }
}
