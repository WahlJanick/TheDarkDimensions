package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TheDarkDimensions.MOD_ID)
public final class ProgressionTests {
    private static final Map<String, Consumer<GameTestHelper>> TESTS = new LinkedHashMap<>();
    static {
        TESTS.put("mastery_menu",MasteryTests::menu);
        TESTS.put("mastery_migration",MasteryTests::migration);
        TESTS.put("mastery_graph",MasteryTests::graph);
        TESTS.put("mastery_ray",MasteryTests::ray);
        TESTS.put("mastery_fan",MasteryTests::fan);
        TESTS.put("mastery_marks",MasteryTests::marks);
        TESTS.put("mastery_veil",MasteryTests::veil);
        TESTS.put("mastery_reactive",MasteryTests::reactive);
        TESTS.put("mastery_spring",MasteryTests::spring);
        TESTS.put("mastery_inertia",MasteryTests::inertia);
        TESTS.put("mastery_anchor",MasteryTests::anchor);
        TESTS.put("mastery_afterimage",MasteryTests::afterimage);
        TESTS.put("mastery_fold",MasteryTests::fold);
        TESTS.put("mastery_cooldown",MasteryTests::cooldown);
        TESTS.put("mastery_blood",MasteryTests::blood);
        TESTS.put("mastery_catalog",MasteryTests::catalog);
        TESTS.put("mastery_sources",MasteryTests::sources);
        TESTS.put("mastery_chain",MasteryTests::chain);
        TESTS.put("back_creativeskills",BackEquipmentTests::creativeSkills);
        TESTS.put("back_slots",BackEquipmentTests::slots);
        TESTS.put("back_swap",BackEquipmentTests::swap);
        TESTS.put("back_sandals",BackEquipmentTests::sandals);
        TESTS.put("back_vanillachest",BackEquipmentTests::vanillaChest);
        TESTS.put("back_backflight",BackEquipmentTests::backFlight);
        TESTS.put("back_woolwear",BackEquipmentTests::woolWear);
        TESTS.put("back_rockets",BackEquipmentTests::rockets);
        TESTS.put("back_lava",BackEquipmentTests::lava);
        TESTS.put("back_creativeboss",BackEquipmentTests::creativeBoss);
        TESTS.put("back_recipes",BackEquipmentTests::recipes);
        TESTS.put("skilltree_teleport_hooks",SkillTreeTests::teleportHooks);
        TESTS.put("skilltree_graph",SkillTreeTests::graph);
        TESTS.put("skilltree_migration",SkillTreeTests::migration);
        TESTS.put("skilltree_learning",SkillTreeTests::learning);
        TESTS.put("skilltree_embers",SkillTreeTests::embers);
        TESTS.put("skilltree_frost",SkillTreeTests::frost);
        TESTS.put("skilltree_veil",SkillTreeTests::veil);
        TESTS.put("skilltree_spring",SkillTreeTests::spring);
        TESTS.put("skilltree_chain",SkillTreeTests::chain);
        TESTS.put("skilltree_storm",SkillTreeTests::storm);
        TESTS.put("skilltree_anchor",SkillTreeTests::anchor);
        TESTS.put("skilltree_afterimage",SkillTreeTests::afterimage);
        TESTS.put("skilltree_inertia",SkillTreeTests::inertia);
        TESTS.put("skilltree_moment",SkillTreeTests::moment);
        TESTS.put("skilltree_fold",SkillTreeTests::fold);
        TESTS.put("skilltree_leappull",SkillTreeTests::leapPull);
        TESTS.put("skilltree_ashtrail",SkillTreeTests::ashTrail);
        TESTS.put("mindshift_right_click_cooldown_and_sneak_parry",MindshiftTests::cast);
        TESTS.put("mindshift_actual_native_homing_impact_swap",MindshiftTests::flight);
        TESTS.put("mindshift_blocked_destination_atomicity",MindshiftTests::blocked);
        TESTS.put("mindshift_cancellable_teleport_atomicity",MindshiftTests::canceled);
        TESTS.put("mindshift_fatal_hit_no_swap",MindshiftTests::fatal);
        TESTS.put("mindshift_native_projectile_save_reload",MindshiftTests::persistence);
        TESTS.put("mindshift_liquid_void_and_range_checks",MindshiftTests::unsafe);
        TESTS.put("mindshift_hellsblade_fissure_ownership_and_expiry",MindshiftTests::scars);
        TESTS.put("mindshift_native_player_animation_loading",MindshiftTests::animations);
        TESTS.put("relic_foundation_and_all_six_upgrade_recipes",RelicKatanaTests::foundation);
        TESTS.put("relic_fixed_stats_enchants_and_reach",RelicKatanaTests::stats);
        TESTS.put("relic_mindshifter_melee_no_legacy_debuffs",h->RelicKatanaTests.hits(h,false));
        TESTS.put("relic_hellsblade_confirmed_three_hit_pulse",h->RelicKatanaTests.hits(h,true));
        TESTS.put("relic_actual_parry_all_three_katanas",RelicKatanaTests::defense);
        TESTS.put("relic_native_rare_chest_loot",RelicKatanaTests::loot);
        TESTS.put("relic_end_eye_table_and_crafter_gate",RelicKatanaTests::endGate);
        TESTS.put("relic_end_world_unlock_persistence",RelicKatanaTests::endSave);
        TESTS.put("relic_end_old_offline_trial_receipt_migration",RelicKatanaTests::migration);
        TESTS.put("finale_feedback_personal_throttled_sounds",FinaleTests::feedback);
        TESTS.put("finale_offline_reward_receipts",FinaleTests::receipts);
        TESTS.put("finale_resonant_step_persistence",FinaleTests::ability);
        TESTS.put("finale_permanent_sigil_gate_and_waypoint",FinaleTests::sigils);
        TESTS.put("finale_native_arena_cooperative_battle",FinaleTests::arenaBattle);
        TESTS.put("finale_arena_departure_cleanup_and_retry",FinaleTests::retry);
        TESTS.put("finale_departed_participant_victory_receipt",FinaleTests::departedVictory);
        TESTS.put("finale_boss_three_phases",FinaleTests::phases);
        TESTS.put("finale_boss_actual_perfect_parry",FinaleTests::parry);
        TESTS.put("finale_boss_rift_bypasses_parry",h->FinaleTests.rift(h,false));
        TESTS.put("finale_boss_rift_perfect_dodge",h->FinaleTests.rift(h,true));
        TESTS.put("finale_boss_magic_ward",FinaleTests::ward);
        for(var kind:at.janic.thedarkdimensions.worldgen.WanderingSitesFeature.Kind.values())TESTS.put("finale_native_discovery_"+kind.getSerializedName(),h->FinaleTests.sites(h,kind));
        TESTS.put("finale_native_nexus_generation",FinaleTests::structure);
        TESTS.put("resonance_native_recipes_and_crafting_gate",ResonanceTests::recipes);
        TESTS.put("resonance_bench_component_preservation",ResonanceTests::bench);
        TESTS.put("resonance_bench_full_inventory_atomicity",ResonanceTests::fullInventory);
        TESTS.put("resonance_melee_rune_cooldowns",ResonanceTests::melee);
        TESTS.put("resonance_chronus_perfect_dodge",ResonanceTests::chronus);
        TESTS.put("resonance_journal_respawn_and_dimension",ResonanceTests::persistence);
        TESTS.put("resonance_offline_reward_receipts",ResonanceTests::rewards);
        TESTS.put("resonance_trial_ember",h->ResonanceTests.trial(h,at.janic.thedarkdimensions.resonance.TrialKind.EMBER));
        TESTS.put("resonance_trial_tide",h->ResonanceTests.trial(h,at.janic.thedarkdimensions.resonance.TrialKind.TIDE));
        TESTS.put("resonance_trial_radiance",h->ResonanceTests.trial(h,at.janic.thedarkdimensions.resonance.TrialKind.RADIANCE));
        TESTS.put("resonance_trial_retry_without_farming",ResonanceTests::retry);
        TESTS.put("resonance_cooperative_reload_and_offline_rewards",ResonanceTests::multiplayer);
        TESTS.put("resonance_native_cave_floor_worldgen",ResonanceTests::structures);
        TESTS.put("recipe_balance_burning_stick_igniter_wear",RecipeBalanceTests::burningStick);
        TESTS.put("progression_warrior_smithing",ProgressionRebalanceTests::warriorUpgrades);
        TESTS.put("progression_mixed_blood_and_book_costs",ProgressionRebalanceTests::mixedBlood);
        TESTS.put("progression_all_zombie_blood_drops",ProgressionRebalanceTests::zombieDrops);
        TESTS.put("recipe_balance_templates_only_duplicate",RecipeBalanceTests::templates);
        TESTS.put("recipe_balance_templates_actual_nether_loot",RecipeBalanceTests::netherLoot);
        TESTS.put("katana_enchants_native_scope_and_anvil",KatanaEnchantmentTests::scope);
        TESTS.put("katana_enchants_native_sharpness_damage_equivalence",KatanaEnchantmentTests::damage);
        TESTS.put("katana_enchants_native_table_level_thirty_and_rarity",KatanaEnchantmentTests::table);
        TESTS.put("katana_enchants_saved_item_migration",KatanaEnchantmentTests::migration);
        TESTS.put("katana_enchants_master_thirty_percent_chance",KatanaEnchantmentTests::chance);
        TESTS.put("katana_enchants_master_two_counters_maximum",h->KatanaEnchantmentTests.counters(h,true));
        TESTS.put("katana_enchants_master_failed_roll_one_counter",h->KatanaEnchantmentTests.counters(h,false));
        TESTS.put("katana_enchants_master_native_attack_defers_second",KatanaEnchantmentTests::nativeCounter);
        TESTS.put("katana_enchants_master_no_perfect_dodge_bonus",KatanaEnchantmentTests::dodgeNoExtra);
        TESTS.put("katana_enchants_master_failed_attack_spends_window",KatanaEnchantmentTests::failed);
        TESTS.put("katana_enchants_master_second_miss_spends_window",KatanaEnchantmentTests::secondMiss);
        TESTS.put("katana_enchants_master_original_one_second_expiry",KatanaEnchantmentTests::originalExpiry);
        TESTS.put("katana_boss_native_attributes_and_three_phases",KatanaBossTests::phases);
        TESTS.put("katana_boss_announced_sword_damage",KatanaBossTests::windup);
        TESTS.put("katana_boss_actual_perfect_parry_and_counter",KatanaBossTests::parry);
        TESTS.put("katana_boss_heavy_cannot_be_parried",h->KatanaBossTests.heavy(h,false));
        TESTS.put("katana_boss_heavy_can_be_dodged",h->KatanaBossTests.heavy(h,true));
        TESTS.put("katana_boss_native_loot_bestiary_advancement",KatanaBossTests::loot);
        TESTS.put("katana_boss_native_dojo_generation",KatanaBossTests::structure);
        TESTS.put("runestone_fire_core_recipes",CoreRecipeTests::fire);
        TESTS.put("runestone_water_core_returns_buckets",CoreRecipeTests::water);
        TESTS.put("runestone_lightning_all_oxidation_and_waxed_variants",CoreRecipeTests::lightning);
        TESTS.put("katana_defense_perfect_parry_and_counter",DefenseTests::perfectParry);
        TESTS.put("katana_defense_normal_parry",DefenseTests::normalParry);
        TESTS.put("katana_defense_normal_parry_master_no_counter",DefenseTests::normalMasterParry);
        TESTS.put("katana_defense_normal_parry_clears_old_perfect",DefenseTests::normalClearsPerfect);
        TESTS.put("katana_defense_guard_and_rear_attacks",DefenseTests::guardAndRear);
        TESTS.put("katana_defense_recovery_and_stormveil",DefenseTests::recovery);
        TESTS.put("katana_defense_heavy_attacks_and_projectiles",DefenseTests::heavyAndProjectile);
        TESTS.put("katana_defense_perfect_dodge_and_counter",DefenseTests::perfectDodge);
        TESTS.put("katana_defense_normal_dodge_and_expiry",DefenseTests::normalDodge);
        TESTS.put("katana_defense_collision_food_and_weapon_validation",DefenseTests::dodgeValidation);
        TESTS.put("katana_defense_native_animation_loading",DefenseTests::animations);
        TESTS.put("katana_defense_counter_miss_spends_attempt",DefenseTests::counterMiss);
        TESTS.put("katana_defense_counter_failed_attack_spends_attempt",DefenseTests::counterFailedAttack);
        TESTS.put("katana_defense_counter_short_expiry",DefenseTests::counterExpiry);
        TESTS.put("katana_defense_counter_real_attack_once",DefenseTests::counterRealAttack);
        TESTS.put("exploration_recipes_and_hourglass_focus",ExplorationTests::recipes);
        TESTS.put("exploration_bestiary_persistence_and_advancement",ExplorationTests::bestiary);
        TESTS.put("exploration_native_structures_traps_lore_and_generation",ExplorationTests::structures);
        TESTS.put("exploration_native_chest_loot",ExplorationTests::loot);
        TESTS.put("exploration_keeper_time_field",ExplorationTests::keeper);
        TESTS.put("mobs_expansion_attributes_and_peaceful_wisp",MobExpansionTests::attributes);
        TESTS.put("mobs_expansion_short_venom",MobExpansionTests::venom);
        TESTS.put("mobs_expansion_projectile_impacts",MobExpansionTests::projectiles);
        TESTS.put("mobs_expansion_root_windup",MobExpansionTests::roots);
        TESTS.put("mobs_expansion_flame_windup",MobExpansionTests::flame);
        TESTS.put("mobs_expansion_biome_spawn_scope",MobExpansionTests::biomes);
        TESTS.put("mobs_expansion_geode_affinity",MobExpansionTests::geodes);
        TESTS.put("mobs_expansion_chronus_dash_collision_and_cooldown",MobExpansionTests::dash);
        TESTS.put("veilwood_seeded_source_codec",VeilwoodTests::source);
        TESTS.put("veilwood_singleton_saved_spawn",VeilwoodTests::singleton);
        TESTS.put("veilwood_native_darkness_fog",VeilwoodTests::atmosphere);
        TESTS.put("spell_effects_success_broadcast_and_failed_cast",SpellEffectsTests::broadcast);
        TESTS.put("trinkets_spellbook_casting_progress_and_menu",TrinketsTests::books);
        TESTS.put("trinkets_focus_talisman_slots_and_effects",TrinketsTests::accessories);
        TESTS.put("perk_material_recipes",PerkTests::recipes);
        TESTS.put("perk_coating_all_weapons_and_preservation",PerkTests::weapons);
        TESTS.put("perk_coating_melee_ranged_and_thrown_hits",PerkTests::hits);
        TESTS.put("perk_ronin_bandage_healing_and_frost",PerkTests::bandage);
        TESTS.put("perk_astral_prism_and_curios_accessories",PerkTests::accessories);
        TESTS.put("perk_sakura_extended_combo_window",PerkTests::combo);
        TESTS.put("perk_windstep_equipment_dash_collision_cooldown",PerkTests::sandals);
        TESTS.put("magic_skill_costs_and_prerequisites",MagicTests::skills);
        TESTS.put("magic_master_elements_and_recipes",MagicTests::master);
        TESTS.put("magic_book_save_and_legacy_migration",MagicTests::persistence);
        TESTS.put("magic_casting_levels_and_shared_cooldown",MagicTests::casting);
        TESTS.put("chronus_dash_collision",MagicTests::chronus);
        TESTS.put("chronus_rewind_history_and_death",MagicTests::rewind);
        TESTS.put("curios_equipped_book_casting",MagicTests::curios);
        TESTS.put("mummy_ruins_crafting_and_key_upgrade",RuinsTests::recipes);
        TESTS.put("separate_twenty_cube_and_old_save_migration",RuinsTests::large);
        TESTS.put("foreign_keys_visitors_and_return_anchor",RuinsTests::visitors);
        TESTS.put("mummy_ruins_template_traps_and_locate_registration",RuinsTests::ruins);
        TESTS.put("mummy_ruins_actual_chest_loot",RuinsTests::loot);
        TESTS.put("ashfall_bonus_resources",AshfallComboTests::resources);
        TESTS.put("ashfall_persistence_light_and_dimension",AshfallComboTests::weather);
        TESTS.put("katana_three_strikes_reset_and_cleave",AshfallComboTests::combos);
        TESTS.put("stormveil_chain_targets_and_damage",AshfallComboTests::chain);
        TESTS.put("bettercombat_katana_registration",AshfallComboTests::compat);
        TESTS.put("elemental_ore_wall_preservation",ElementalStoneTests::walls);
        TESTS.put("elemental_ore_loot_and_silk_touch",ElementalStoneTests::loot);
        TESTS.put("natural_tree_crown_variation_and_seed",NaturalRealmTests::trees);
        TESTS.put("removed_terrain_id_migration",NaturalRealmTests::aliases);
        TESTS.put("solar_mummy_effects_and_drop_chance",SolarTests::mummy);
        TESTS.put("solar_crafting_and_smithing",SolarTests::recipes);
        TESTS.put("solar_forge_actual_double_speed",SolarTests::forge);
        TESTS.put("stormveil_melee_arc_stats_and_reach",SolarTests::blade);
        TESTS.put("pocket_private_room_return_and_persistence",SolarTests::pocket);
        TESTS.put("hard_stone_ore_generation_and_loot",MummyRealmTests::hardOres);
        TESTS.put("water_cave_mummy_loot",MummyRealmTests::mummyWaterLoot);
        TESTS.put("astral_wand_ignites_unlit_frame",MummyRealmTests::unlitPortal);
        TESTS.put("devil_tear_eye_recipes_and_armor_enchantment", InfernalTests::recipes);
        TESTS.put("devil_armor_complete_set_fire_immunity", InfernalTests::protection);
        TESTS.put("devil_armor_actual_lava_swimming", InfernalTests::swimming);
        TESTS.put("devil_trident_actual_lava_riptide", InfernalTests::riptide);
        TESTS.put("devil_trident_upgrade_and_enchantments", DevilTests::upgrade);
        TESTS.put("devil_trident_melee_and_charm", DevilTests::melee);
        TESTS.put("devil_trident_throw_loyalty_and_pickup", DevilTests::throwing);
        TESTS.put("devils_charm_adult_and_baby_villagers", DevilTests::charm);
        TESTS.put("blood_netherite_recipe_counts", BloodNetheriteTests::recipes);
        TESTS.put("blood_netherite_upgrade_preservation", BloodNetheriteTests::upgrades);
        TESTS.put("blood_netherite_equipment_stats", BloodNetheriteTests::stats);
        TESTS.put("blood_netherite_actual_bastion_loot", BloodNetheriteTests::bastionLoot);
        TESTS.put("armory_crafting_recipes", ArmoryTests::recipes);
        TESTS.put("fish_and_hammer_smithing", ArmoryTests::smithing);
        TESTS.put("fish_and_warrior_axe_properties", ArmoryTests::axeStats);
        TESTS.put("thrown_axe_recovery", ArmoryTests::thrownPickup);
        TESTS.put("held_hammer_gliding", ArmoryTests::gliding);
        TESTS.put("hammer_aimed_lightning_blast", ArmoryTests::blast);
        TESTS.put("katana_impact_effects", KatanaEffectTests::impacts);
        TESTS.put("blood_effect_can_kill", KatanaEffectTests::bloodKills);
        TESTS.put("blood_resists_milk_and_expires", KatanaEffectTests::milk);
        TESTS.put("katana_essence_recipe_chains", KatanaTests::essences);
        TESTS.put("three_diagonal_katana_recipes", KatanaTests::katanas);
        TESTS.put("katana_tiers_enchantments_and_reach", KatanaTests::stats);
        TESTS.put("creeperblood_drops", KatanaTests::bloodDrops);
        TESTS.put("surface_light_rock_generation", SurfaceLightsTests::generation);
        TESTS.put("surface_light_terrain_safety", SurfaceLightsTests::terrain);
        TESTS.put("surface_light_biomes_and_drops", SurfaceLightsTests::registration);
        TESTS.put("spell_selection_and_costs", SpellTests::selection);
        TESTS.put("spell_recipes", SpellTests::recipes);
        TESTS.put("metalinium_loot_and_fortune", SpellTests::loot);
        TESTS.put("water_spell_and_cooldown", SpellTests::water);
        TESTS.put("fire_spell_projectile", SpellTests::fire);
        TESTS.put("portable_personal_ender_chest", SpellTests::enderChest);
        TESTS.put("lightning_spell_target", SpellTests::lightning);
        TESTS.put("water_cools_entire_stack", ProgressionTests::water);
        TESTS.put("dry_stick_remains_burned", ProgressionTests::dry);
        TESTS.put("held_stick_damage_and_reach", ProgressionTests::player);
        TESTS.put("portal_upgrade_x", h -> portal(h, Direction.Axis.X));
        TESTS.put("portal_upgrade_z", h -> portal(h, Direction.Axis.Z));
        TESTS.put("recipes_and_biome_registration", ProgressionTests::recipes);
        TESTS.put("vanilla_desert_surface", ProgressionTests::surface);
        TESTS.put("mummy_drops_ashes", ProgressionTests::loot);
        TESTS.put("dimension_recipes", ProgressionTests::dimensionRecipes);
        TESTS.put("burned_terrain", ProgressionTests::burnedTerrain);
        TESTS.put("dimension_portal_round_trip", ProgressionTests::dimensionTravel);
        TESTS.put("crystal_and_relic_drops", ProgressionTests::crystalDrops);
        TESTS.put("trees_crystals_and_portal_ruins", ProgressionTests::features);
        TESTS.put("smithing_upgrade_routes", ProgressionTests::smithing);
        TESTS.put("double_spear_stats_and_reach", ProgressionTests::doubleSpear);
        TESTS.put("mummy_desert_registration", ProgressionTests::locateDesert);
        TESTS.put("elemental_core_recipes", ProgressionTests::coreRecipes);
        TESTS.put("glowing_honey_effect_and_harvest", ProgressionTests::glowingHoney);
        TESTS.put("three_elemental_caves", ProgressionTests::elementalCaves);
        TESTS.put("elemental_mob_loot", ProgressionTests::elementalLoot);
        TESTS.put("elemental_shrine_template", ProgressionTests::shrine);
        TESTS.put("vanilla_crystal_attachments", ProgressionTests::crystalAttachments);
        TESTS.put("spelltable_recipe_and_mineral_drops", ProgressionTests::minerals);
        TESTS.put("essentialium_geode_and_chronus_generation", ProgressionTests::mineralGeneration);
        TESTS.put("essentialium_growth_stages", ProgressionTests::essentialiumGrowth);
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void mockNetwork(net.neoforged.neoforge.event.OnDatapackSyncEvent e){
        var p=e.getPlayer();if(p!=null&&p.getGameProfile().name().equals("test-mock-player"))net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(p.connection.getConnection());
    }
    @SubscribeEvent public static void setup(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, register -> TESTS.forEach((name, body) ->
                register.register(TheDarkDimensions.id(name), body)));
    }
    @SubscribeEvent public static void register(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(TheDarkDimensions.id("integration"), new TestEnvironmentDefinition.AllOf(List.of()));
        TESTS.forEach((name, body) -> event.registerTest(TheDarkDimensions.id(name), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, TheDarkDimensions.id(name)),
                new TestData<>(environment, TheDarkDimensions.id("integration_empty"), 300, 0, true))));
    }
    private static ItemEntity drop(GameTestHelper h, Item item, int count, BlockPos pos) {
        Vec3 p = h.absoluteVec(Vec3.atCenterOf(pos));
        var entity = new ItemEntity(h.getLevel(), p.x, p.y, p.z, new ItemStack(item, count));
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        h.getLevel().addFreshEntity(entity);
        return entity;
    }
    private static void minerals(GameTestHelper h) {
        var level=h.getLevel();var e=new ItemStack(MineralContent.ESSENTIALIUM.get());var b=new ItemStack(Items.BOOK);var o=new ItemStack(Items.OBSIDIAN);
        var input=CraftingInput.of(3,3,List.of(e,e,e,b,o,b,o,o,o));
        var recipe=level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,level);
        h.assertTrue(recipe.isPresent()&&recipe.get().value().assemble(input).is(MineralContent.SPELLTABLE.get().asItem()),"Exact EEE/BOB/OOO recipe crafts Spelltable");
        var wrong=CraftingInput.of(3,3,List.of(e,e,e,b,o,b,o,o,ItemStack.EMPTY));
        h.assertTrue(level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,wrong,level).isEmpty(),"Spelltable requires all four obsidian");
        var pick=new ItemStack(Items.DIAMOND_PICKAXE);var pos=h.absolutePos(new BlockPos(4,3,4));
        var cluster=Block.getDrops(MineralContent.ESSENTIALIUM_CLUSTER.get().defaultBlockState(),level,pos,null,null,pick);
        h.assertTrue(cluster.stream().filter(s -> s.is(MineralContent.ESSENTIALIUM)).mapToInt(ItemStack::getCount).sum()==4,"Full crystal yields four Essentialium with pickaxe");
        var ore=Block.getDrops(MineralContent.CHRONUS_ORB_ORE.get().defaultBlockState(),level,pos,null,null,pick);
        h.assertTrue(ore.size()==1&&ore.getFirst().is(MineralContent.CHRONUS_ORB),"Chronus ore drops one orb");
        pick.enchant(level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);
        h.assertTrue(Block.getDrops(MineralContent.CHRONUS_ORB_ORE.get().defaultBlockState(),level,pos,null,null,pick).stream().anyMatch(s -> s.is(MineralContent.CHRONUS_ORB_ORE.get().asItem())),"Silk Touch preserves Chronus ore");
        h.assertTrue(Block.getDrops(MineralContent.ESSENTIALIUM_CLUSTER.get().defaultBlockState(),level,pos,null,null,pick).stream().anyMatch(s -> s.is(MineralContent.ESSENTIALIUM_CLUSTER.get().asItem())),"Silk Touch preserves Essentialium cluster");
        level.setBlock(pos,MineralContent.SPELLTABLE.get().defaultBlockState(),3);
        h.assertTrue(!level.getBlockState(pos).isCollisionShapeFullBlock(level,pos),"Spelltable uses table-height shape");
        h.succeed();
    }
    private static void mineralGeneration(GameTestHelper h) {
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);var generator=realm.getChunkSource().getGenerator();
        var origin=new BlockPos(352,-20,192);
        for(var p:BlockPos.betweenClosed(origin.offset(-20,-20,-20),origin.offset(20,20,20)))realm.setBlock(p,Blocks.STONE.defaultBlockState(),18);
        var geode=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("essentialium_geode"))).value();
        h.assertTrue(geode.place(realm,generator,net.minecraft.util.RandomSource.create(263),origin),"Vanilla geode algorithm places Essentialium geodes");
        int crystals=0,budding=0;
        for(var p:BlockPos.betweenClosed(origin.offset(-20,-20,-20),origin.offset(20,20,20))) {
            var state=realm.getBlockState(p);
            if(state.is(MineralContent.BUDDING_ESSENTIALIUM.get()))budding++;
            if(state.getBlock() instanceof AmethystClusterBlock&&state.getBlock()!=Blocks.AMETHYST_CLUSTER)crystals++;
        }
        h.assertTrue(crystals>0&&budding>0,"Geode contains blue crystals and regenerative budding blocks");
        for(int cx=31;cx<=33;cx++)for(int cz=11;cz<=13;cz++)realm.setChunkForced(cx,cz,true);
        h.runAfterDelay(30,() -> {
        var ore=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("chronus_orb_ore"))).value();
        var oreOrigin=new BlockPos(512,-20,192);
        for(var p:BlockPos.betweenClosed(oreOrigin.offset(-6,-6,-6),oreOrigin.offset(6,6,6)))realm.setBlock(p,Blocks.STONE.defaultBlockState(),18);
        // Finished ServerLevel chunks discard worldgen heightmaps; restore the
        // map OreFeature normally receives during real chunk decoration.
        for(int cx=31;cx<=33;cx++)for(int cz=11;cz<=13;cz++)
            net.minecraft.world.level.levelgen.Heightmap.primeHeightmaps(realm.getChunk(cx,cz),EnumSet.of(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG));
        boolean placed=false;
        h.assertTrue(realm.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,512,192)>=oreOrigin.getY(),"26.3 heightmap must recognise burned terrain as solid");
        for(int attempt=0;attempt<32;attempt++)placed|=ore.place(realm,generator,net.minecraft.util.RandomSource.create(263+attempt),oreOrigin);
        int orbs=0;
        for(var p:BlockPos.betweenClosed(oreOrigin.offset(-6,-6,-6),oreOrigin.offset(6,6,6)))if(realm.getBlockState(p).is(MineralContent.CHRONUS_ORB_ORE.get()))orbs++;
        System.out.println("Chronus ore generation: "+orbs+" blocks across 32 fixed-seed attempts");
        h.assertTrue(placed&&orbs>0,"Vanilla small-vein attempts produce actual Chronus ore blocks");
        h.assertTrue(realm.registryAccess().get(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("normal_caves"))).isPresent(),"Ordinary underground biome exists");
        for(String old:List.of("burned_coal_ore","burned_iron_ore","improved_amethyst_crystal_ore","ember_stone"))
            h.assertTrue(realm.registryAccess().get(ResourceKey.create(Registries.PLACED_FEATURE,TheDarkDimensions.id(old))).isEmpty(),"Obsolete ore deposit removed: "+old);
        h.succeed();
        });
    }
    private static void essentialiumGrowth(GameTestHelper h) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(4,3,4));
        level.setBlock(pos,MineralContent.BUDDING_ESSENTIALIUM.get().defaultBlockState(),3);
        var random=net.minecraft.util.RandomSource.create(263);
        for(int i=0;i<1500;i++)level.getBlockState(pos).randomTick(level,pos,random);
        for(var d:Direction.values()) {
            var state=level.getBlockState(pos.relative(d));
            h.assertTrue(state.is(MineralContent.ESSENTIALIUM_CLUSTER.get())&&state.getValue(AmethystClusterBlock.FACING)==d,"All Essentialium growth stages end in custom cluster, never purple vanilla amethyst");
        }
        h.succeed();
    }
    private static void coreRecipes(GameTestHelper h) {
        var level=h.getLevel();var manager=level.getServer().getRecipeManager();
        var rod=new ItemStack(Items.LIGHTNING_ROD.weathering().unaffected());var empty=ItemStack.EMPTY;
        var lightning=CraftingInput.of(3,3,List.of(empty,rod,empty,rod,new ItemStack(TheDarkDimensions.EMPTY_RUNESTONE.get()),rod,empty,rod,empty));
        var result=manager.getRecipeFor(RecipeType.CRAFTING,lightning,level);
        h.assertTrue(result.isPresent()&&result.get().value().assemble(lightning).is(CaveContent.LIGHTNING_CORE),"Four rods around Empty Runestone craft Lightning Core");
        var l=new ItemStack(CaveContent.LIGHTNING_CORE.get());var f=new ItemStack(TheDarkDimensions.FIRE_CORE.get());var w=new ItemStack(CaveContent.WATER_CORE.get());
        var spell=CraftingInput.of(3,2,List.of(l,w,f,w,l,f));
        var output=manager.getRecipeFor(RecipeType.CRAFTING,spell,level);
        h.assertTrue(output.isPresent()&&output.get().value().assemble(spell).is(CaveContent.SPELL_CORE),"Six cores craft Spell Core regardless of arrangement");
        var shortRecipe=CraftingInput.of(3,2,List.of(l,w,f,w,l,ItemStack.EMPTY));
        h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,shortRecipe,level).isEmpty(),"Missing core must not craft");h.succeed();
    }
    private static void glowingHoney(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        var bottle=new ItemStack(CaveContent.GLOWING_HONEY.get()).finishUsingItem(h.getLevel(),player);
        h.assertTrue(bottle.is(Items.GLASS_BOTTLE),"Drinking returns glass bottle");
        var effect=player.getEffect(net.minecraft.world.effect.MobEffects.GLOWING);
        h.assertTrue(effect!=null&&effect.getDuration()==1200,"Honey gives Glowing for 60 seconds");
        var bee=h.spawn(CaveContent.GLOWING_BEE.get(),new BlockPos(4,2,4));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));
        bee.mobInteract(player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().is(CaveContent.GLOWING_HONEY),"Bottle harvests glowing honey without killing bee");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.GLASS_BOTTLE));
        bee.mobInteract(player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE)&&bee.isAlive()&&bee.isCurrentlyGlowing(),"Harvest cooldown prevents duplication; bee stays alive and glowing");h.succeed();
    }
    private static void elementalCaves(GameTestHelper h) {
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);
        String[] kinds={"fire","water","glowing"};Block[] linings={CaveContent.FIRE_CAVE_STONE.get(),CaveContent.WATER_CAVE_STONE.get(),CaveContent.GLOWING_CAVE_STONE.get()};
        for(int i=0;i<3;i++) {
            var origin=new BlockPos(160+i*32,-20,160);
            realm.setChunkForced(origin.getX()>>4,origin.getZ()>>4,true);
            for(int cx=-1;cx<=1;cx++)for(int cz=-1;cz<=1;cz++)realm.setChunkForced((origin.getX()>>4)+cx,(origin.getZ()>>4)+cz,true);
            var feature=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id(kinds[i]+"_cave"))).value();
            // An irregular pre-existing cave: the decorator must preserve its outline.
            var rock=new ArrayList<BlockPos>();
            for(var cursor:BlockPos.betweenClosed(origin.offset(-8,-13,-8),origin.offset(8,13,8))) {
                var p=cursor.immutable();int dx=p.getX()-origin.getX(),dy=p.getY()-origin.getY(),dz=p.getZ()-origin.getZ();
                boolean air=Math.abs(dx)<6&&Math.abs(dz)<5&&dy>-3&&dy<2+(dx*dx+dz*dz)%4;
                realm.setBlock(p,(air?Blocks.AIR:Blocks.STONE).defaultBlockState(),18);
                if(!air)rock.add(p);
            }
            h.assertTrue(feature.place(realm,realm.getChunkSource().getGenerator(),net.minecraft.util.RandomSource.create(263+i),origin),"Cave feature places");
            h.assertTrue(rock.stream().noneMatch(p -> realm.getBlockState(p).isAir()),"Decoration must never carve identical rooms or corridors");
            int themed=0,glow=0,clusters=0;
            for(var p:BlockPos.betweenClosed(origin.offset(-7,-12,-7),origin.offset(7,12,7))) {
                var state=realm.getBlockState(p);
                if(state.is(linings[i]))themed++;
                if(state.is(Blocks.GLOWSTONE))glow++;
                if(state.getBlock() instanceof AmethystClusterBlock) {
                    clusters++;h.assertTrue(state.canSurvive(realm,p),"Crystal must attach to an actual cave wall");
                }
            }
            h.assertTrue(themed>30&&clusters>0,"Natural cave surfaces receive lining and attached amethyst clusters");
            final int kind=i;
            h.runAfterDelay(30,() -> {
                var mobs=realm.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new AABB(origin).inflate(12));
                System.out.println("Cave "+kind+" nearby inhabitants "+mobs);
                h.assertTrue(mobs.stream().anyMatch(m -> m.getType()==(kind==0?CaveContent.FIRE_MAGE.get():kind==1?TheDarkDimensions.MUMMY.get():CaveContent.GLOWING_BEE.get())),"Cave includes correct inhabitant "+kind);
            });
            if(i==2)h.assertTrue(glow>40,"Glowing cave contains abundant Glowstone");
        }
        h.runAfterDelay(31,h::succeed);
    }
    private static void crystalAttachments(GameTestHelper h) {
        var centre=h.absolutePos(new BlockPos(4,3,4));var level=h.getLevel();
        level.setBlock(centre,Blocks.STONE.defaultBlockState(),3);
        for(var d:Direction.values()) {
            var p=centre.relative(d);
            var state=CaveContent.FIRE_CRYSTAL.get().defaultBlockState().setValue(AmethystClusterBlock.FACING,d);
            level.setBlock(p,state,3);
            h.assertTrue(state.canSurvive(level,p)&&!state.isCollisionShapeFullBlock(level,p),"Vanilla cluster shape and all six attachment directions");
        }
        level.setBlock(centre,Blocks.AIR.defaultBlockState(),3);
        for(var d:Direction.values())h.assertTrue(level.getBlockState(centre.relative(d)).isAir(),"Removing support removes the cluster");
        h.succeed();
    }
    private static void elementalLoot(GameTestHelper h) {
        String[] names={"fire_mage","mummy","glowing_bee"};Item[] items={TheDarkDimensions.FIRE_CORE.get(),CaveContent.WATER_CORE.get(),CaveContent.GLOWING_HONEY.get()};
        var types=List.of(CaveContent.FIRE_MAGE.get(),TheDarkDimensions.MUMMY.get(),CaveContent.GLOWING_BEE.get());
        for(int i=0;i<3;i++) {
            var entity=types.get(i).create(h.getLevel(),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(h.getLevel())
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,entity)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,Vec3.ZERO)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,h.getLevel().damageSources().generic())
                .create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
            var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("entities/"+names[i])));
            int drops=0;final Item wanted=items[i];var random=net.minecraft.util.RandomSource.create(263);
            for(int n=0;n<1000;n++)if(table.getRandomItems(params,random).stream().anyMatch(s->s.is(wanted)))drops++;
            h.assertTrue(i==2?drops==1000:(i==0?drops>60&&drops<140:drops==0),"Core drop probability and guaranteed honey must work: "+names[i]+" "+drops);
            System.out.println("Elemental loot "+names[i]+": "+drops+"/1000");
        }
        h.succeed();
    }
    private static void shrine(GameTestHelper h) {
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);
        var structure=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id("elemental_shrine"))).value();
        h.assertTrue(structure.biomes().size()==3,"Shrine generates in all three surface biomes");
        var template=realm.getStructureTemplateManager().getOrCreate(TheDarkDimensions.id("elemental_shrine"));
        var origin=new BlockPos(256,240,160);
        realm.setChunkForced(16,10,true);
        h.assertTrue(template.getSize().getX()==9&&template.getSize().getZ()==9,"Shrine template is registered");
        h.assertTrue(template.placeInWorld(realm,origin,origin,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),net.minecraft.util.RandomSource.create(263),18),"Shrine template places");
        System.out.println("Shrine chest state "+realm.getBlockState(origin.offset(4,1,7))+" floor "+realm.getBlockState(origin.offset(4,0,7)));
        h.assertTrue(realm.getBlockState(origin.offset(4,1,7)).is(Blocks.CHEST),"Shrine has loot chest");
        var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)realm.getBlockEntity(origin.offset(4,1,7));
        chest.unpackLootTable(null);boolean reward=false;for(int slot=0;slot<chest.getContainerSize();slot++)if(!chest.getItem(slot).isEmpty())reward=true;
        h.assertTrue(reward,"Shrine loot chest supplies rewards");
        h.runAfterDelay(30,() -> {h.assertTrue(!realm.getEntitiesOfClass(FireMage.class,new AABB(origin).inflate(10)).isEmpty(),"Shrine guardian spawns");h.succeed();});
    }
    private static void water(GameTestHelper h) {
        h.setBlock(3, 2, 3, Blocks.WATER);
        var entity = drop(h, TheDarkDimensions.BURNING_STICK.get(), 17, new BlockPos(3, 2, 3));
        h.runAfterDelay(10, () -> {
            h.assertTrue(entity.getItem().is(TheDarkDimensions.BURNED_STICK), "Water must cool the stick");
            h.assertTrue(entity.getItem().getCount() == 17, "Cooling must preserve the stack count");
            h.succeed();
        });
    }
    private static void dry(GameTestHelper h) {
        var entity = drop(h, TheDarkDimensions.BURNING_STICK.get(), 3, new BlockPos(3, 2, 3));
        h.runAfterDelay(10, () -> {
            h.assertTrue(entity.getItem().is(TheDarkDimensions.BURNING_STICK), "Dry sticks must remain burning");
            h.succeed();
        });
    }
    private static void player(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.getAbilities().invulnerable = false;
        var events = new GameplayEvents();
        player.tickCount = 20;
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(TheDarkDimensions.BURNING_STICK.get()));
        float health = player.getHealth();
        events.playerTick(new PlayerTickEvent.Post(player));
        h.assertTrue(player.getHealth() < health, "Offhand burned stick must deal fire damage");
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        double blockRange = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        double entityRange = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TheDarkDimensions.ENHANCED_DIAMOND_PICKAXE.get()));
        events.playerTick(new PlayerTickEvent.Post(player));
        events.playerTick(new PlayerTickEvent.Post(player));
        h.assertTrue(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) == blockRange + 2, "Exactly +2 block reach, without stacking");
        h.assertTrue(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) == entityRange + 2, "Exactly +2 attack reach");
        player.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        events.playerTick(new PlayerTickEvent.Post(player));
        h.assertTrue(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) == blockRange, "Reach must reset after unequipping");
        h.assertTrue(player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE) == entityRange, "Attack reach must reset");
        h.succeed();
    }
    private static BlockPos pos(Direction.Axis axis, int horizontal, int y) {
        return axis == Direction.Axis.X ? new BlockPos(horizontal + 2, y + 1, 3) : new BlockPos(3, y + 1, horizontal + 2);
    }
    private static void portal(GameTestHelper h, Direction.Axis axis) {
        var level = h.getLevel();
        for (int a = 0; a < 4; a++) for (int y = 0; y < 5; y++) {
            if (a == 0 || a == 3 || y == 0 || y == 4) h.setBlock(pos(axis, a, y), Blocks.OBSIDIAN);
        }
        for (int a = 1; a <= 2; a++) for (int y = 1; y <= 3; y++) {
            level.setBlock(h.absolutePos(pos(axis, a, y)), Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis), 18);
        }
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.getAbilities().mayBuild = true;
        player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(RevisionContent.ASTRAL_CRYSTAL_WAND.get()));
        BlockPos clicked = h.absolutePos(pos(axis, 1, 1));
        var context = new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(clicked), Direction.NORTH, clicked, false));
        h.assertTrue(TheDarkDimensions.FIRE_CORE.get().useOn(context)==InteractionResult.PASS,"Fire Core no longer activates portals");
        h.assertTrue(RevisionContent.ASTRAL_CRYSTAL_WAND.get().useOn(context) == InteractionResult.SUCCESS, "Upgrade must succeed");
        h.assertTrue(player.getMainHandItem().getCount() == 1, "Astral Crystal Wand is retained after activation");
        for (int a = 1; a <= 2; a++) for (int y = 1; y <= 3; y++) {
            h.assertBlockPresent(TheDarkDimensions.IMPROVED_PORTAL.get(), pos(axis, a, y));
            h.assertTrue(h.getBlockState(pos(axis, a, y)).getValue(NetherPortalBlock.AXIS) == axis, "Portal axis must be preserved");
        }
        RevisionContent.ASTRAL_CRYSTAL_WAND.get().useOn(context);
        h.assertTrue(player.getMainHandItem().getCount() == 1, "Repeated activation does not consume the wand");
        h.setBlock(pos(axis, 0, 2), Blocks.AIR);
        h.runAfterDelay(2, () -> {
            for (int a = 1; a <= 2; a++) for (int y = 1; y <= 3; y++) h.assertBlockNotPresent(TheDarkDimensions.IMPROVED_PORTAL.get(), pos(axis, a, y));
            h.succeed();
        });
    }
    private static void recipes(GameTestHelper h) {
        var level = h.getLevel();
        var manager = level.getServer().getRecipeManager();
        var stick = new ItemStack(TheDarkDimensions.IMPROVED_STICK.get());
        var powder = new ItemStack(Items.BLAZE_POWDER);
        var input = CraftingInput.of(3, 3, List.of(powder,powder,powder,powder,new ItemStack(TheDarkDimensions.EMPTY_RUNESTONE.get()),powder,powder,powder,powder));
        var recipe = manager.getRecipeFor(RecipeType.CRAFTING, input, level);
        h.assertTrue(recipe.isPresent() && recipe.get().value().assemble(input).is(TheDarkDimensions.FIRE_CORE), "Eight Blaze Powder around Empty Runestone craft Fire Core");
        var burnedInput = CraftingInput.of(2, 1, List.of(new ItemStack(Items.STICK), new ItemStack(Items.FLINT_AND_STEEL)));
        var burnedRecipe = manager.getRecipeFor(RecipeType.CRAFTING, burnedInput, level);
        h.assertTrue(burnedRecipe.isPresent() && burnedRecipe.get().value().assemble(burnedInput).is(TheDarkDimensions.BURNING_STICK), "Burning Stick recipe must craft");
        for (var ingredients : List.of(List.of(new ItemStack(TheDarkDimensions.BURNED_STICK.get()), new ItemStack(Items.STICK)), List.of(new ItemStack(Items.STICK), new ItemStack(TheDarkDimensions.BURNED_STICK.get())))) {
            var improvedInput = CraftingInput.of(2, 1, ingredients);
            var improvedRecipe = manager.getRecipeFor(RecipeType.CRAFTING, improvedInput, level);
            h.assertTrue(improvedRecipe.isPresent() && improvedRecipe.get().value().assemble(improvedInput).is(TheDarkDimensions.IMPROVED_STICK), "Burned Stick plus vanilla Stick crafts new Improved Stick in either order");
        }
        var invalidInput = CraftingInput.of(2, 1, List.of(new ItemStack(TheDarkDimensions.BURNING_STICK.get()), new ItemStack(Items.STICK)));
        h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING, invalidInput, level).isEmpty(), "Burning Stick must first be cooled");
        var planks=new ItemStack(DarkContent.BURNED_PLANKS.get());
        var doorInput=CraftingInput.of(2,3,List.of(planks,planks,planks,planks,planks,planks));
        var doorRecipe=manager.getRecipeFor(RecipeType.CRAFTING,doorInput,level);
        h.assertTrue(doorRecipe.isPresent()&&doorRecipe.get().value().assemble(doorInput).is(DarkAcaciaContent.DOOR.get().asItem())&&doorRecipe.get().value().assemble(doorInput).getCount()==3,"Dark Acacia planks craft three doors");
        var slabInput=CraftingInput.of(3,1,List.of(planks,planks,planks));
        var slabRecipe=manager.getRecipeFor(RecipeType.CRAFTING,slabInput,level);
        h.assertTrue(slabRecipe.isPresent()&&slabRecipe.get().value().assemble(slabInput).is(DarkAcaciaContent.SLAB.get().asItem())&&slabRecipe.get().value().assemble(slabInput).getCount()==6,"Dark Acacia planks craft six slabs");
        h.assertTrue(Blocks.DIRT.defaultBlockState().is(net.minecraft.tags.BlockTags.DIRT),"Dark Acacia sapling accepts burned soil");
        var saplingPos=new BlockPos(2048,240,2048);
        for(var p:BlockPos.betweenClosed(saplingPos.offset(-5,0,-5),saplingPos.offset(5,13,5))) level.setBlock(p,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
        level.setBlock(saplingPos.below(),Blocks.DIRT.defaultBlockState(),3);
        level.setBlock(saplingPos,DarkAcaciaContent.SAPLING.get().defaultBlockState(),3);
        for(int i=0;i<2;i++) DarkAcaciaContent.SAPLING.get().advanceTree(level,saplingPos,level.getBlockState(saplingPos),level.getRandom());
        h.assertTrue(level.getBlockState(saplingPos).is(DarkContent.BURNED_LOG.get()),"Dark Acacia sapling grows dimension wood on burned soil");
        var logPos=h.absolutePos(new BlockPos(1,2,1));
        level.setBlock(logPos,DarkContent.BURNED_LOG.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X),3);
        var woodcutter=h.makeMockPlayer(GameType.CREATIVE);
        woodcutter.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_AXE));
        var strip=new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(woodcutter,InteractionHand.MAIN_HAND,logPos,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(logPos),Direction.UP,logPos,false));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(strip);
        h.assertTrue(strip.isCanceled()&&level.getBlockState(logPos).is(DarkAcaciaContent.STRIPPED_LOG.get())&&level.getBlockState(logPos).getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)==Direction.Axis.X,"Axe strips Dark Acacia and preserves axis");
        var wandInput=CraftingInput.of(1,3,List.of(new ItemStack(TheDarkDimensions.FIRE_CORE.get()),new ItemStack(Items.GHAST_TEAR),stick));
        var wandRecipe=manager.getRecipeFor(RecipeType.CRAFTING,wandInput,level);
        h.assertTrue(wandRecipe.isPresent()&&wandRecipe.get().value().assemble(wandInput).is(RevisionContent.ASTRAL_CRYSTAL_WAND),"Astral Crystal Wand requires crystal over Improved Stick");
        h.assertTrue(RevisionContent.HARD_STONE.get().defaultBlockState().getDestroySpeed(level,BlockPos.ZERO)>Blocks.DEEPSLATE.defaultBlockState().getDestroySpeed(level,BlockPos.ZERO),"Hard Stone mines slower than deepslate");
        var swordInput = CraftingInput.of(1, 3, List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND), stick));
        var swordRecipe = manager.getRecipeFor(RecipeType.CRAFTING, swordInput, level);
        h.assertTrue(swordRecipe.isPresent() && swordRecipe.get().value().assemble(swordInput).is(TheDarkDimensions.ENHANCED_DIAMOND_SWORD), "Enhanced sword recipe must craft");
        h.assertTrue(level.registryAccess().get(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("black_desert"))).isEmpty(), "Black Desert has been removed");
        h.assertTrue(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.usedBiomes().anyMatch(net.minecraft.world.level.biome.Biomes.DESERT::equals), "Vanilla Desert remains in natural Overworld generation");
        h.succeed();
    }
    private static void loot(GameTestHelper h) {
        var burned = h.spawn(TheDarkDimensions.MUMMY.get(), new BlockPos(4, 2, 4));
        burned.hurtServer(h.getLevel(), burned.damageSources().genericKill(), 1000);
        h.runAfterDelay(2, () -> {
            var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, burned.getBoundingBox().inflate(2));
            int ashes = drops.stream().filter(e -> e.getItem().is(TheDarkDimensions.ASHES)).mapToInt(e -> e.getItem().getCount()).sum();
            h.assertTrue(ashes >= 1 && ashes <= 3, "Killing a Mummy must drop 1Ã¢â‚¬â€œ3 Ashes");
            h.succeed();
        });
    }
    private static void locateDesert(GameTestHelper h) {
        var level=h.getLevel();var registries=level.registryAccess();
        var desert=registries.getOrThrow(net.minecraft.world.level.biome.Biomes.DESERT).value();
        var monsters=desert.getModifiedEnvironmentAttributes().applyModifier(net.minecraft.world.attribute.EnvironmentAttributes.NATURAL_MOB_SPAWNS,net.minecraft.world.level.biome.MobSpawnSettings.EMPTY).getMobsToSpawn(net.minecraft.world.entity.MobCategory.MONSTER).unwrap();
        h.assertTrue(monsters.stream().anyMatch(e -> e.value().type()==TheDarkDimensions.MUMMY.get()),"Actual Desert spawn list contains Mummies");
        h.assertTrue(monsters.stream().noneMatch(e -> e.value().type()==net.minecraft.world.entity.EntityTypes.HUSK),"Actual Desert spawn list no longer contains Husks");
        var source=net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(registries.getOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.OVERWORLD));
        var settings=registries.getOrThrow(net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD);
        for(long seed:new long[]{0,42,2026}) {
            var random=net.minecraft.world.level.levelgen.RandomState.create(registries.lookupOrThrow(Registries.NOISE),seed,settings.value());
            var found=source.findClosestBiome3d(new BlockPos(0,64,0),6400,32,64,b -> b.is(net.minecraft.world.level.biome.Biomes.DESERT),random,level);
            h.assertTrue(found!=null,"Vanilla locate search must find Desert within 6400 blocks for seed "+seed);
            System.out.println("Desert locate seed="+seed+" result="+found.getFirst());
        }
        h.succeed();
    }
    private static void smithing(GameTestHelper h) {
        var level=h.getLevel();var manager=level.getServer().getRecipeManager();
        var unbreaking=level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING);
        var name=net.minecraft.network.chat.Component.literal("My old tool");
        for(String kind:List.of("sword","pickaxe","axe","shovel","hoe","spear")) {
            String diamondId=kind.equals("spear")?"improved_diamond_spear":"enhanced_diamond_"+kind;
            String netheriteId="perfected_netherite_"+kind;
            for(String material:List.of("diamond","netherite")) {
                var base=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(material+"_"+kind)));
                base.setDamageValue(137);base.enchant(unbreaking,3);base.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,name);
                var input=new SmithingRecipeInput(new ItemStack(ToolContent.IMPROVING_TEMPLATE.get()),base,new ItemStack(TheDarkDimensions.IMPROVED_STICK.get()));
                var recipe=manager.getRecipeFor(RecipeType.SMITHING,input,level);
                h.assertTrue(recipe.isPresent(),"Smithing upgrade must exist for "+material+" "+kind);
                var out=recipe.get().value().assemble(input);
                h.assertTrue(out.is(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(TheDarkDimensions.id(material.equals("diamond")?diamondId:netheriteId))),"Smithing must create the correct improved tool");
                h.assertTrue(out.getDamageValue()==137,"Smithing must preserve wear");
                h.assertTrue(name.equals(out.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME)),"Smithing must preserve names");
                h.assertTrue(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(unbreaking,out)==3,"Smithing must preserve enchantments");
            }
            var base=new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(TheDarkDimensions.id(diamondId)));
            base.setDamageValue(137);base.enchant(unbreaking,3);
            var input=new SmithingRecipeInput(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),base,new ItemStack(Items.NETHERITE_INGOT));
            var recipe=manager.getRecipeFor(RecipeType.SMITHING,input,level);
            h.assertTrue(recipe.isPresent(),"Improved Diamond "+kind+" must upgrade further to Netherite");
            var out=recipe.get().value().assemble(input);
            h.assertTrue(out.is(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(TheDarkDimensions.id(netheriteId))),"Netherite route must preserve the improved handle");
            h.assertTrue(out.getDamageValue()==137&&net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(unbreaking,out)==3,"Netherite route must preserve wear and enchantments");
            h.assertTrue(TheDarkDimensions.enhanced(out),"Perfected Netherite tools must retain their reach bonus");
        }
        h.succeed();
    }
    private static void doubleSpear(GameTestHelper h) {
        var level=h.getLevel();var base=new ItemStack(ToolContent.PERFECTED_NETHERITE_SPEAR.get());
        base.setDamageValue(97);base.enchant(level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),2);
        var input=new SmithingRecipeInput(new ItemStack(ToolContent.IMPROVING_TEMPLATE.get()),base,new ItemStack(ToolContent.IMPROVED_DIAMOND_SPEAR.get()));
        var recipe=level.getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,level);
        h.assertTrue(recipe.isPresent(),"Both improved spears and template must combine");
        var out=recipe.get().value().assemble(input);
        h.assertTrue(out.is(ToolContent.DOUBLE_SPEAR)&&out.getDamageValue()==97,"Double Spear must preserve the Netherite spear's wear");
        var components=net.minecraft.core.component.DataComponents.ATTACK_RANGE;
        h.assertTrue(out.get(components).maxReach()==Items.NETHERITE_SPEAR.getDefaultInstance().get(components).maxReach()+2,"Native spear attacks must gain exactly two blocks");
        h.assertTrue(out.get(net.minecraft.core.component.DataComponents.ENCHANTABLE).value()>base.get(net.minecraft.core.component.DataComponents.ENCHANTABLE).value(),"Double Spear must be more enchantable");
        h.assertTrue(out.get(net.minecraft.core.component.DataComponents.KINETIC_WEAPON).damageMultiplier()>base.get(net.minecraft.core.component.DataComponents.KINETIC_WEAPON).damageMultiplier(),"Double Spear must deal stronger kinetic damage");
        var player=h.makeMockPlayer(GameType.SURVIVAL);var range=player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        player.setItemInHand(InteractionHand.MAIN_HAND,out);new GameplayEvents().playerTick(new PlayerTickEvent.Post(player));
        h.assertTrue(player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)==range+2,"Double Spear must also increase block reach");
        h.succeed();
    }
    private static void features(GameTestHelper h) {
        var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);
        h.assertTrue(realm!=null,"Burned Realm must exist");
        var generator=realm.getChunkSource().getGenerator();
        var random=net.minecraft.util.RandomSource.create(263);
        BlockPos tree=new BlockPos(64,240,64);
        for(BlockPos p:BlockPos.betweenClosed(tree.offset(-4,0,-4),tree.offset(4,12,4)))realm.setBlock(p,Blocks.AIR.defaultBlockState(),18);
        realm.setBlock(tree.below(),Blocks.DIRT.defaultBlockState(),18);
        var treeFeature=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("burned_tree"))).value();
        h.assertTrue(treeFeature.place(realm,generator,random,tree),"Burned trees must generate on burned dirt");
        h.assertTrue(realm.getBlockState(tree).is(DarkContent.BURNED_LOG.get()),"Trees must use burned logs");
        BlockPos ruin=new BlockPos(80,240,64);
        for(BlockPos p:BlockPos.betweenClosed(ruin.offset(-4,-3,-4),ruin.offset(5,6,4)))realm.setBlock(p,p.getY()<ruin.getY()?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        var ruinFeature=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("echo_ruin"))).value();
        h.assertTrue(ruinFeature.place(realm,generator,random,ruin),"Discovered portal ruins must generate");
        h.assertTrue(ImprovedEchoShardItem.isFrame(realm,ruin.offset(-1,0,0),Direction.Axis.X),"Generated ruins must have an activatable Improved Obsidian frame");
        h.assertTrue(realm.getBlockState(ruin.offset(-2,0,2)).is(DarkContent.ECHO_RELIC.get()),"Generated ruins must include an Echo Relic");
        BlockPos crystal=new BlockPos(100,240,64);
        for(BlockPos p:BlockPos.betweenClosed(crystal.offset(-5,-5,-5),crystal.offset(5,6,5)))realm.setBlock(p,p.getY()<crystal.getY()?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),18);
        var crystalFeature=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("crystal_growth"))).value();
        h.assertTrue(crystalFeature.place(realm,generator,random,crystal),"Crystal growth must generate on burned terrain");
        h.succeed();
    }
    private static void crystalDrops(GameTestHelper h) {
        var level=h.getLevel();
        var pos=h.absolutePos(new BlockPos(4,2,4));
        var ore=DarkContent.IMPROVED_AMETHYST_ORE.get().defaultBlockState();
        var pick=new ItemStack(Items.DIAMOND_PICKAXE);
        var drops=Block.getDrops(ore,level,pos,null,null,pick);
        h.assertTrue(drops.stream().anyMatch(s->s.is(DarkContent.IMPROVED_AMETHYST)),"Mining crystal ore must drop improved amethyst instead of the ore");
        pick.enchant(level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);
        var silk=Block.getDrops(ore,level,pos,null,null,pick);
        h.assertTrue(silk.stream().anyMatch(s->s.is(DarkContent.IMPROVED_AMETHYST_ORE.get().asItem())),"Silk Touch must preserve crystal ore");
        var relic=Block.getDrops(DarkContent.ECHO_RELIC.get().defaultBlockState(),level,pos,null,null,new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(relic.stream().anyMatch(s->s.is(Items.ECHO_SHARD)),"Ruins must supply a normal Echo Shard");
        h.succeed();
    }
    private static void dimensionRecipes(GameTestHelper h) {
        var level=h.getLevel();
        var panes=new ItemStack(Items.GLASS_PANE);
        var amethyst=CraftingInput.of(3,3,List.of(panes,panes,panes,panes,new ItemStack(Items.AMETHYST_SHARD),panes,panes,new ItemStack(Items.DIAMOND),panes));
        var result=level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,amethyst,level);
        h.assertTrue(result.isPresent()&&result.get().value().assemble(amethyst).is(DarkContent.IMPROVED_AMETHYST),"Improved Amethyst recipe must craft");
        var a=new ItemStack(DarkContent.IMPROVED_AMETHYST.get());
        var echo=CraftingInput.of(3,3,List.of(a,a,a,a,new ItemStack(Items.ECHO_SHARD),a,a,a,a));
        var echoResult=level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,echo,level);
        h.assertTrue(echoResult.isPresent()&&echoResult.get().value().assemble(echo).is(DarkContent.IMPROVED_ECHO_SHARD),"Echo Shard recipe must craft");
        h.assertTrue(DarkContent.IMPROVED_OBSIDIAN.get().asItem()==Items.AIR,"Improved Obsidian must have no obtainable item");
        h.assertTrue(!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.containsKey(TheDarkDimensions.id("burned_hoglin"))&&!net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.containsKey(TheDarkDimensions.id("burned_pigman")),"Removed burned mobs are not registered");
        h.succeed();
    }
    private static void dimensionTravel(GameTestHelper h) {
        var level=h.getLevel();
        BlockPos entry=h.absolutePos(new BlockPos(3,2,3));
        var entity=h.spawn(net.minecraft.world.entity.EntityTypes.PIG,new BlockPos(3,2,4));
        var transition=PortalTravel.destination(level,entity,entry,false);
        h.assertTrue(transition!=null,"Burned Realm must provide a portal destination");
        var traveled=entity.teleport(transition);
        h.assertTrue(traveled!=null&&traveled.level().dimension().equals(DarkContent.BURNED_REALM),"Entity must travel into Burned Realm");
        var realm=(net.minecraft.server.level.ServerLevel)traveled.level();
        var back=PortalTravel.destination(realm,traveled,traveled.blockPosition(),false);
        h.assertTrue(back!=null&&back.newLevel()==level,"Realm portal must return to source dimension");
        var echo=PortalTravel.destination(realm,traveled,traveled.blockPosition().offset(10,0,0),true);
        h.assertTrue(echo!=null,"Echo Depths must provide a portal destination");
        var inEcho=traveled.teleport(echo);
        h.assertTrue(inEcho!=null&&inEcho.level().dimension().equals(DarkContent.ECHO_DEPTHS),"Echo portal must travel to Echo Depths");
        var echoBack=PortalTravel.destination((net.minecraft.server.level.ServerLevel)inEcho.level(),inEcho,inEcho.blockPosition(),true);
        h.assertTrue(echoBack!=null&&echoBack.newLevel()==realm,"Echo portal must return to Burned Realm");
        inEcho.discard();
        // Test activation against a complete discovered frame, including both orientations.
        var fake=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(realm);
        for(var axis:new Direction.Axis[]{Direction.Axis.X,Direction.Axis.Z}) {
            BlockPos corner=new BlockPos(24,220,axis==Direction.Axis.X?24:40);
            for(int x=0;x<4;x++)for(int y=0;y<5;y++)realm.setBlock(ImprovedEchoShardItem.relative(corner,axis,x,y),x==0||x==3||y==0||y==4?DarkContent.IMPROVED_OBSIDIAN.get().defaultBlockState():Blocks.AIR.defaultBlockState(),18);
            fake.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DarkContent.IMPROVED_ECHO_SHARD.get(),2));
            var context=new UseOnContext(fake,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(corner),Direction.UP,corner,false));
            h.assertTrue(DarkContent.IMPROVED_ECHO_SHARD.get().useOn(context)==InteractionResult.SUCCESS,"Echo frame must activate on axis "+axis);
            h.assertTrue(fake.getMainHandItem().getCount()==1,"Activation consumes one shard");
            h.assertTrue(PortalLogic.find(realm,ImprovedEchoShardItem.relative(corner,axis,1,1),DarkContent.ECHO_PORTAL.get(),DarkContent.IMPROVED_OBSIDIAN.get()).size()==6,"Echo portal must contain six connected blocks");
            DarkContent.IMPROVED_ECHO_SHARD.get().useOn(context);
            h.assertTrue(fake.getMainHandItem().getCount()==1,"Repeated activation must not consume another shard");
        }
        h.succeed();
    }
    private static void burnedTerrain(GameTestHelper h) { terrain(h,true); }
    private static void surface(GameTestHelper h) { terrain(h,false); }
    private static void terrain(GameTestHelper h,boolean burned) {
        var level = h.getLevel();
        var registries = level.registryAccess();
        var biome = registries.getOrThrow(burned?ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("ash_wastes")):net.minecraft.world.level.biome.Biomes.DESERT);
        var settings = registries.getOrThrow(burned?ResourceKey.create(Registries.NOISE_SETTINGS,TheDarkDimensions.id("burned_realm")):net.minecraft.world.level.levelgen.NoiseGeneratorSettings.OVERWORLD);
        var state = net.minecraft.world.level.levelgen.RandomState.create(registries.lookupOrThrow(Registries.NOISE), 0L, settings.value());
        var generator = new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(new net.minecraft.world.level.biome.FixedBiomeSource(biome), settings);
        var height = net.minecraft.world.level.LevelHeightAccessor.create(-64, 384);
        var chunk = new net.minecraft.world.level.chunk.ProtoChunk(new net.minecraft.world.level.ChunkPos(0, 0),
                net.minecraft.world.level.chunk.UpgradeData.EMPTY, height, net.minecraft.world.level.chunk.PalettedContainerFactory.create(registries), null);
        chunk.fillBiomesFromNoise((x, y, z) -> biome);
        // Isolate terrain/material generation from structure loading in the flat test world.
        var structures = new net.minecraft.world.level.StructureManager(level, new net.minecraft.world.level.levelgen.WorldOptions(0L, false, false), null) {
            @Override public List<net.minecraft.world.level.levelgen.structure.StructureStart> startsForStructure(int x, int z,
                    java.util.function.Predicate<net.minecraft.world.level.levelgen.structure.Structure> filter) { return List.of(); }
        };
        generator.buildTerrain(chunk, net.minecraft.world.level.levelgen.blending.Blender.empty(), state, structures,
                new net.minecraft.world.level.biome.BiomeManager((x, y, z) -> biome, 0L), null, Set.of(biome)).join();
        int surfaceBlocks=0,deepHardStone=0,upperStone=0;
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=-64;y<150;y++) {
            var block=chunk.getBlockState(new BlockPos(x,y,z));
            if(burned) {
                if(block.is(Blocks.GRASS_BLOCK)||block.is(Blocks.DIRT))surfaceBlocks++;
                if(y<0&&block.is(RevisionContent.HARD_STONE.get()))deepHardStone++;
                if(y>=8&&block.is(Blocks.STONE))upperStone++;
                h.assertTrue(!block.is(Blocks.DEEPSLATE),"Hard Stone replaces all deepslate in realm terrain");
            } else if(block.is(Blocks.SAND))surfaceBlocks++;
        }
        h.assertTrue(surfaceBlocks>100,burned?"Realm generates vanilla grass/dirt surfaces":"Vanilla Desert generates normal sand");
        if(burned)h.assertTrue(deepHardStone>100&&upperStone>100,"Real chunk generation contains upper Stone and lower Hard Stone");
        h.succeed();
    }
}


