package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.resonance.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;

final class RelicKatanaTests {
    private static void phase(net.minecraft.world.entity.player.Player p,int index){if(net.neoforged.fml.ModList.get().isLoaded("bettercombat"))try{Class.forName("net.bettercombat.logic.PlayerAttackProperties").getMethod("setComboCount",int.class).invoke(p,index);}catch(Exception ex){throw new IllegalStateException(ex);}}
    private static void recipe(GameTestHelper h,List<ItemStack> stacks,Item result){var input=CraftingInput.of(3,3,stacks);var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(result),"Actual recipe crafts "+result);}
    static void foundation(GameTestHelper h){
        var iron=new ItemStack(Items.IRON_INGOT);recipe(h,List.of(ItemStack.EMPTY,ItemStack.EMPTY,iron,ItemStack.EMPTY,iron,ItemStack.EMPTY,new ItemStack(TheDarkDimensions.IMPROVED_STICK.get()),ItemStack.EMPTY,ItemStack.EMPTY),RelicKatanas.BASIC.get());
        var base=new ItemStack(RelicKatanas.BASIC.get());var fire=new ItemStack(TheDarkDimensions.FIRE_CORE.get());var chronus=new ItemStack(ExplorationContent.CHRONUS_FRAGMENT.get());var ingot=new ItemStack(SpellContent.INFUSED_INGOT.get());
        recipe(h,List.of(base,new ItemStack(Items.END_CRYSTAL),new ItemStack(Items.SHULKER_SHELL),ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY),RelicKatanas.MINDSHIFTER.get());
        recipe(h,List.of(base,fire,fire,new ItemStack(Items.REDSTONE_BLOCK),new ItemStack(Items.NETHERITE_SCRAP),ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY),RelicKatanas.HELLSBLADE.get());
        for(var output:List.of(KatanaContent.FROSTGLEAM.get(),KatanaContent.BLOODGRIEF.get(),KatanaContent.SOULEATER.get(),SolarContent.STORMVEIL.get())){
            var id=ResourceKey.create(Registries.RECIPE,net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(output));var found=h.getLevel().getServer().getRecipeManager().byKey(id).orElseThrow();h.assertTrue(found.value().placementInfo().ingredients().stream().anyMatch(i->i.test(base)),"Every existing special katana requires foundation: "+output);
        }
        var old=CraftingInput.of(3,1,List.of(new ItemStack(CaveContent.LIGHTNING_CORE.get()),new ItemStack(SolarContent.SOLAR_ESSENCE.get()),new ItemStack(TheDarkDimensions.IMPROVED_STICK.get())));h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,old,h.getLevel()).isEmpty(),"Old Stormveil stick recipe cannot bypass foundation");h.succeed();
    }
    static void stats(GameTestHelper h){try(var f=DefenseTests.player(h,Items.IRON_SWORD)){
        var p=f.player();var blades=List.of(RelicKatanas.BASIC.get(),RelicKatanas.MINDSHIFTER.get(),RelicKatanas.HELLSBLADE.get());double[] damage={5,8.5,9.5},speed={1.6,1.5,1.4};int[] durability={480,1450,1800};double reach=p.getAttributeBaseValue(Attributes.ENTITY_INTERACTION_RANGE);
        for(int i=0;i<3;i++){var stack=new ItemStack(blades.get(i));h.assertTrue(stack.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==damage[i],"Fixed damage for tier "+i);h.assertTrue(Math.abs(stack.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_SPEED,4,EquipmentSlot.MAINHAND)-speed[i])<.001&&stack.getMaxDamage()==durability[i],"Balanced speed and durability");h.assertTrue(KatanaDefense.katana(stack),"All three support katana defenses");h.assertTrue(stack.supportsEnchantment(h.getLevel().registryAccess().getOrThrow(KatanaEnchantments.SLASHING))&&!stack.supportsEnchantment(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS)),"Katana-only enchantment rules apply");p.setItemInHand(InteractionHand.MAIN_HAND,stack);new GameplayEvents().playerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));h.assertTrue(p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)==reach+(i==0?0:2),"Only advanced katanas receive enhanced-handle reach");}
    }h.succeed();}
    static void hits(GameTestHelper h,boolean hell){var item=hell?RelicKatanas.HELLSBLADE.get():RelicKatanas.MINDSHIFTER.get();var f=DefenseTests.player(h,item);var p=f.player();var mob=h.spawn(EntityTypes.COW,new Vec3(6,2,8));mob.setNoAi(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.setHealth(100);var weapon=p.getMainHandItem();phase(p,0);item.hurtEnemy(weapon,mob,p);h.assertTrue(!mob.isOnFire()&&!mob.hasEffect(MobEffects.SLOWNESS),"Neither relic uses the old ignition or slow-on-hit perk");h.assertTrue(mob.getHealth()==100&&(!hell||CrimsonRupture.stacks(mob,p)==1),"First hit leaves one fissure without bonus damage");
        h.runAfterDelay(1,()->{phase(p,1);item.hurtEnemy(weapon,mob,p);h.assertTrue(mob.getHealth()==100&&(!hell||CrimsonRupture.stacks(mob,p)==2),"Second hit prepares two fissures without detonation");});
        h.runAfterDelay(2,()->{try(f){phase(p,2);item.hurtEnemy(weapon,mob,p);if(hell){h.assertTrue(mob.getHealth()<=96&&!mob.isOnFire()&&CrimsonRupture.stacks(mob,p)==0,"Third hit consumes two fissures for four magic damage, never fire");float health=mob.getHealth();item.hurtEnemy(weapon,mob,p);h.assertTrue(mob.getHealth()==health&&CrimsonRupture.stacks(mob,p)==0,"Repeated callback cannot detonate or charge the same swing twice");}else h.assertTrue(!mob.hasEffect(MobEffects.WEAKNESS)&&!mob.hasEffect(MobEffects.LEVITATION),"Mindshifter's special action belongs to its ranged ability");h.succeed();}});
    }
    static void defense(GameTestHelper h){for(var item:List.of(RelicKatanas.BASIC.get(),RelicKatanas.MINDSHIFTER.get(),RelicKatanas.HELLSBLADE.get()))try(var f=DefenseTests.player(h,item)){var p=f.player();var mob=h.spawn(EntityTypes.COW,new Vec3(5.5,2,7.5));mob.setNoAi(true);h.assertTrue(KatanaDefense.guard(p),"New katana starts native guard");h.assertTrue(!p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(mob),8)&&p.getHealth()==20&&KatanaDefense.staggered(mob),"Perfect parry works for each new katana");}h.succeed();}
    static void loot(GameTestHelper h){var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);for(var entry:Map.of("minecraft:chests/end_city_treasure",RelicKatanas.MINDSHIFTER.get(),"minecraft:chests/nether_bridge",RelicKatanas.HELLSBLADE.get(),"minecraft:chests/bastion_treasure",RelicKatanas.HELLSBLADE.get(),"thedarkdimensions:chests/elemental_shrine",RelicKatanas.HELLSBLADE.get()).entrySet()){
        var table=h.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,Identifier.parse(entry.getKey())));var random=RandomSource.create(330);int relics=0;boolean other=false;
        for(int i=0;i<1500;i++)for(var stack:table.getRandomItems(params,random))if(stack.is(entry.getValue())){relics++;h.assertTrue(stack.getCount()==1&&stack.getDamageValue()==0&&at.janic.thedarkdimensions.finale.CombatFeedback.rare(stack),"Rare weapon arrives complete with its fixed identity");}else other=true;
        h.assertTrue(relics>=5&&relics<110&&other,"Native table adds rare weapon and retains ordinary loot: "+entry.getKey()+" "+relics+"/1500");
    }h.succeed();}
    static void endGate(GameTestHelper h){var l=h.getLevel();var data=EndProgression.data(l);int previous=data.completed;try{data.completed=0;var manager=l.getServer().getRecipeManager();var input=CraftingInput.of(3,1,List.of(new ItemStack(Items.ENDER_PEARL),new ItemStack(Items.BLAZE_POWDER),new ItemStack(MineralContent.ESSENTIALIUM.get())));h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,input,l).isEmpty()&&CrafterBlock.getPotentialResults(l,input).isEmpty(),"Table and Crafter both reject Eyes before the trials");data.complete(0);data.complete(0);data.complete(1);h.assertTrue(data.completed==3&&manager.getRecipeFor(RecipeType.CRAFTING,input,l).isEmpty(),"Repeating one trial cannot replace the missing third kind");data.complete(2);var found=manager.getRecipeFor(RecipeType.CRAFTING,input,l);h.assertTrue(found.isPresent()&&found.get().value() instanceof EnderEyeRecipe&&found.get().value().assemble(input).is(Items.ENDER_EYE),"All three trials unlock a native Eye recipe");h.assertTrue(CrafterBlock.getPotentialResults(l,input).isPresent(),"Crafter sees unlock immediately after its earlier negative lookup");var old=CraftingInput.of(2,1,List.of(new ItemStack(Items.ENDER_PEARL),new ItemStack(Items.BLAZE_POWDER)));h.assertTrue(manager.getRecipeFor(RecipeType.CRAFTING,old,l).isEmpty(),"Two-ingredient vanilla recipe cannot bypass Essentialium");}finally{data.completed=previous;data.setDirty();}h.succeed();}
    static void endSave(GameTestHelper h){var data=new EndProgression();for(int n=0;n<3;n++)data.complete(n);var saved=EndProgression.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,data).getOrThrow();var restored=EndProgression.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,saved).getOrThrow();h.assertTrue(restored.completed==7,"World trial unlock serializes permanently");var realm=h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM);h.assertTrue(EndProgression.data(realm)==EndProgression.data(h.getLevel()),"Unlock is shared across dimensions rather than duplicated per realm");h.succeed();}
    static void migration(GameTestHelper h){try(var f=DefenseTests.player(h,Items.IRON_SWORD)){var p=f.player();var data=EndProgression.data(h.getLevel());int previous=data.completed;var ledger=TrialLedger.data(h.getLevel().getServer().getLevel(DarkContent.BURNED_REALM));String offline=UUID.randomUUID().toString();try{data.completed=0;ledger.granted.put(offline,7);p.tickCount=40;net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));h.assertTrue(data.completed==7,"Existing offline receipts migrate into world End unlock without repeating trials");var adv=p.level().getServer().getAdvancements().get(TheDarkDimensions.id("resonance/end_unsealed"));h.assertTrue(adv!=null&&p.getAdvancements().getOrStartProgress(adv).isDone(),"Milestone grants recipe-book advancement to online player");}finally{ledger.granted.remove(offline);ledger.setDirty();data.completed=previous;data.setDirty();}}h.succeed();}
}


