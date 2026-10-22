package at.janic.thedarkdimensions.compat;

import at.janic.thedarkdimensions.TheDarkDimensions;
import at.janic.thedarkdimensions.DarkContent;
import at.janic.thedarkdimensions.ToolContent;
import at.janic.thedarkdimensions.CaveContent;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@JeiPlugin
public final class DarkDimensionsJeiPlugin implements IModPlugin {
    @Override public Identifier getPluginUid() { return TheDarkDimensions.id("jei"); }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        for(var item:java.util.List.of(at.janic.thedarkdimensions.BackEquipment.GLIDER.get(),at.janic.thedarkdimensions.BackEquipment.WINGS.get(),at.janic.thedarkdimensions.PerkContent.WINDSTEP_SANDALS.get()))registration.addItemStackInfo(item.getDefaultInstance(),Component.translatable("guide.thedarkdimensions.back_equipment"));
        registration.addItemStackInfo(net.minecraft.world.item.Items.ENDER_EYE.getDefaultInstance(),Component.translatable("guide.thedarkdimensions.end_progression"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.RelicKatanas.BASIC.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.basic_katana"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.RelicKatanas.MINDSHIFTER.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.mindshifter"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.RelicKatanas.HELLSBLADE.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.hellsblade"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.finale.FinaleContent.ALTAR.get().asItem().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.resonance_finale"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.finale.FinaleContent.TROPHY.get().getDefaultInstance(),Component.translatable("finale.thedarkdimensions.ability_unlocked"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.resonance.ResonanceContent.BENCH.get().asItem().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.runic_binding"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.resonance.ResonanceContent.RUNESTONE.get().asItem().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.ancient_trials"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.resonance.ResonanceContent.JOURNAL.get().getDefaultInstance(),Component.translatable("resonance.thedarkdimensions.journal_waypoints"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.ArmoryContent.WARRIOR_TEMPLATE.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.warrior_axe_upgrade_template"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.KatanaContent.ZOMBIEBLOOD.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.zombieblood"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.SpellContent.SPELL_BOOK.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.spell_book"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.MagicContent.MASTER_BOOK.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.elemental_master_spellbook"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.SolarContent.STORMVEIL.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.stormveil"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.PerkContent.LUMINOUS_COATING.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.luminous_coating"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.BloodNetheriteContent.ARMOR_ITEMS.get(net.minecraft.world.item.equipment.ArmorType.CHESTPLATE).get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.devils_armor"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.DevilContent.TRIDENT.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.devils_trident"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.BloodNetheriteContent.TEMPLATE.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.blood_netherite"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.ArmoryContent.FISH_AXES.get("iron").get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.fish_axes"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.ArmoryContent.HAMMER.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.hammer"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.KatanaContent.FROSTGLEAM.get().getDefaultInstance(),Component.translatable("guide.thedarkdimensions.katanas"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.MineralContent.SPELLTABLE.get().asItem().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.spelltable"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.MagicContent.MASTER_BOOK.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.spell_branches"), Component.translatable("guide.thedarkdimensions.spell_tactics"),Component.translatable("guide.thedarkdimensions.spell_mastery"));
        registration.addItemStackInfo(CaveContent.GLOWING_HONEY.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.glowing_honey"));
        registration.addItemStackInfo(CaveContent.SPELL_CORE.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.spell_core"));
        registration.addItemStackInfo(ToolContent.IMPROVING_TEMPLATE.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.improving_template"));
        registration.addItemStackInfo(ToolContent.DOUBLE_SPEAR.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.double_spear"));
        registration.addItemStackInfo(DarkContent.IMPROVED_AMETHYST.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.improved_amethyst"));
        registration.addItemStackInfo(DarkContent.IMPROVED_ECHO_SHARD.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.improved_echo_shard"));
        registration.addItemStackInfo(TheDarkDimensions.ASHES.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.ashes"));
        registration.addItemStackInfo(TheDarkDimensions.BURNING_STICK.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.burned_stick"));
        registration.addItemStackInfo(TheDarkDimensions.BURNED_STICK.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.improved_stick"));
        registration.addItemStackInfo(TheDarkDimensions.IMPROVED_STICK.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.reinforced_stick"));
        registration.addItemStackInfo(TheDarkDimensions.EMPTY_RUNESTONE.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.empty_runestone"));
        registration.addItemStackInfo(TheDarkDimensions.FIRE_CORE.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.fire_core"));
        registration.addItemStackInfo(CaveContent.WATER_CORE.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.water_core"));
        registration.addItemStackInfo(CaveContent.LIGHTNING_CORE.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.lightning_core"));
        registration.addItemStackInfo(at.janic.thedarkdimensions.RevisionContent.ASTRAL_CRYSTAL_WAND.get().getDefaultInstance(), Component.translatable("guide.thedarkdimensions.astral_crystal_wand"));
    }
}
