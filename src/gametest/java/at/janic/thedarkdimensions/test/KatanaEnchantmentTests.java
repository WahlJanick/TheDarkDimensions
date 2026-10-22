package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;

final class KatanaEnchantmentTests {
    private static List<Item> katanas(){return List.of(KatanaContent.FROSTGLEAM.get(),KatanaContent.BLOODGRIEF.get(),KatanaContent.SOULEATER.get(),SolarContent.STORMVEIL.get());}
    static void scope(GameTestHelper h){
        var r=h.getLevel().registryAccess();var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.experienceLevel=1000;
        for(var item:katanas()){
            var stack=new ItemStack(item);
            for(var key:List.of(Enchantments.SHARPNESS,Enchantments.SMITE,Enchantments.BANE_OF_ARTHROPODS,Enchantments.FIRE_ASPECT,Enchantments.KNOCKBACK,Enchantments.SWEEPING_EDGE)){
                var enchant=r.getOrThrow(key);h.assertTrue(!stack.supportsEnchantment(enchant)&&!stack.isPrimaryItemFor(enchant),"Katana rejects vanilla combat enchant in table and anvil: "+key);
                var menu=new AnvilMenu(1,player.getInventory());var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(enchant,1);
                menu.getSlot(0).set(stack.copy());menu.getSlot(1).set(book);menu.createResult();h.assertTrue(menu.getSlot(2).getItem().isEmpty(),"Native anvil cannot add blocked combat book");
            }
            for(var key:List.of(KatanaEnchantments.SLASHING,KatanaEnchantments.MASTER_PARRYING,Enchantments.UNBREAKING,Enchantments.MENDING,Enchantments.LOOTING))h.assertTrue(stack.supportsEnchantment(r.getOrThrow(key)),"Katana accepts its new combat enchants and normal utility enchants");
            var menu=new AnvilMenu(1,player.getInventory());var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(r.getOrThrow(KatanaEnchantments.SLASHING),3);menu.getSlot(0).set(stack);menu.getSlot(1).set(book);menu.createResult();
            h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(r.getOrThrow(KatanaEnchantments.SLASHING),menu.getSlot(2).getItem())==3,"Native anvil adds Slashing III");
        }
        h.assertTrue(new ItemStack(Items.DIAMOND_SWORD).supportsEnchantment(r.getOrThrow(Enchantments.SHARPNESS)),"Vanilla swords retain their enchantments");
        h.assertTrue(!new ItemStack(Items.DIAMOND_SWORD).supportsEnchantment(r.getOrThrow(KatanaEnchantments.SLASHING)),"Slashing is katana-only");h.succeed();
    }
    static void damage(GameTestHelper h){
        var r=h.getLevel().registryAccess();var target=h.spawn(EntityTypes.COW,new Vec3(4,2,4));target.setNoAi(true);var source=h.getLevel().damageSources().generic();
        for(var item:katanas())for(int level=1;level<=3;level++){
            var katana=new ItemStack(item);katana.enchant(r.getOrThrow(KatanaEnchantments.SLASHING),level);var sword=new ItemStack(Items.DIAMOND_SWORD);sword.enchant(r.getOrThrow(Enchantments.SHARPNESS),level+1);
            float actual=EnchantmentHelper.modifyDamage(h.getLevel(),katana,target,source,4),expected=EnchantmentHelper.modifyDamage(h.getLevel(),sword,target,source,4);
            h.assertTrue(actual==expected&&actual==5+level*.5f,"Native Slashing damage equals Sharpness one level above");
        }
        h.assertTrue(r.getOrThrow(KatanaEnchantments.SLASHING).value().getMaxLevel()==3&&r.getOrThrow(KatanaEnchantments.MASTER_PARRYING).value().getMaxLevel()==1,"New enchants have exact level caps");h.succeed();
    }
    @SuppressWarnings("unchecked") static void table(GameTestHelper h){
        try{
            var r=h.getLevel().registryAccess();var master=r.getOrThrow(KatanaEnchantments.MASTER_PARRYING);
            h.assertTrue(master.is(net.minecraft.tags.EnchantmentTags.IN_ENCHANTING_TABLE)&&!master.is(net.minecraft.tags.EnchantmentTags.TRADEABLE)&&!master.is(net.minecraft.tags.EnchantmentTags.ON_RANDOM_LOOT)&&master.value().getWeight()==1,"Master Parrying is rare and table-only");
            var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var menu=new EnchantmentMenu(1,player.getInventory());
            var method=EnchantmentMenu.class.getDeclaredMethod("getEnchantmentList",net.minecraft.core.RegistryAccess.class,ItemStack.class,int.class,int.class);method.setAccessible(true);
            var field=EnchantmentMenu.class.getDeclaredField("enchantmentSeed");field.setAccessible(true);var seed=(DataSlot)field.get(menu);int found=0;
            for(int i=0;i<1200;i++){
                seed.set(i*7919);for(var stack:List.of(new ItemStack(SolarContent.STORMVEIL.get()),new ItemStack(Items.BOOK))){
                    var lower=(List<EnchantmentInstance>)method.invoke(menu,r,stack,2,29);h.assertTrue(lower.stream().noneMatch(e->e.enchantment().is(KatanaEnchantments.MASTER_PARRYING)),"A sub-30 offer cannot roll Master Parrying, even with enchantability bonus");
                }
                var enchants=(List<EnchantmentInstance>)method.invoke(menu,r,new ItemStack(SolarContent.STORMVEIL.get()),2,30);
                h.assertTrue(enchants.stream().noneMatch(e->!new ItemStack(SolarContent.STORMVEIL.get()).supportsEnchantment(e.enchantment())),"Native table respects blocked vanilla enchants");
                if(enchants.stream().anyMatch(e->e.enchantment().is(KatanaEnchantments.MASTER_PARRYING)))found++;
            }
            h.assertTrue(found>5&&found<300,"Master Parrying actually appears rarely on native 30-level offers: "+found);h.succeed();
        }catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
    static void migration(GameTestHelper h){
        var r=h.getLevel().registryAccess();var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        for(var item:katanas()){
            var stack=new ItemStack(item);stack.enchant(r.getOrThrow(Enchantments.SHARPNESS),5);stack.enchant(r.getOrThrow(Enchantments.FIRE_ASPECT),2);stack.enchant(r.getOrThrow(Enchantments.MENDING),1);PerkContent.coat(stack);stack.setDamageValue(123);
            item.inventoryTick(stack,h.getLevel(),player,EquipmentSlot.MAINHAND);
            h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(r.getOrThrow(KatanaEnchantments.SLASHING),stack)==3&&!stack.supportsEnchantment(r.getOrThrow(Enchantments.SHARPNESS))&&EnchantmentHelper.getItemEnchantmentLevel(r.getOrThrow(Enchantments.SHARPNESS),stack)==0&&EnchantmentHelper.getItemEnchantmentLevel(r.getOrThrow(Enchantments.FIRE_ASPECT),stack)==0,"Existing combat enchants migrate to capped Slashing");
            h.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(r.getOrThrow(Enchantments.MENDING),stack)==1&&stack.getDamageValue()==123&&PerkContent.coated(stack),"Migration preserves utility, wear and coating");
        }h.succeed();
    }
    static void chance(GameTestHelper h){
        var weapon=new ItemStack(KatanaContent.FROSTGLEAM.get());weapon.enchant(h.getLevel().registryAccess().getOrThrow(KatanaEnchantments.MASTER_PARRYING),1);var random=RandomSource.create(263030);int granted=0;
        for(int i=0;i<10000;i++)if(KatanaEnchantments.secondCounter(weapon,h.getLevel(),random))granted++;
        h.assertTrue(granted>2800&&granted<3200,"Native random gives approximately thirty percent bonus counters: "+granted);
        h.assertTrue(!KatanaEnchantments.secondCounter(new ItemStack(KatanaContent.FROSTGLEAM.get()),h.getLevel(),random),"Unenchanted katana has no bonus chance");h.succeed();
    }
    private static long seed(net.minecraft.server.level.ServerPlayer p,boolean success){
        for(int i=0;i<30000;i++){p.getRandom().setSeed(i);if(KatanaEnchantments.secondCounter(p.getMainHandItem(),p.level(),p.getRandom())==success)return i;}throw new AssertionError("No seed");
    }
    private static net.minecraft.world.entity.animal.cow.Cow target(GameTestHelper h){var cow=h.spawn(EntityTypes.COW,new Vec3(5.5,2,7.5));cow.setNoAi(true);cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);cow.setHealth(100);return cow;}
    private static void parry(GameTestHelper h,net.minecraft.server.level.ServerPlayer p,LivingEntity target,boolean roll){p.getMainHandItem().enchant(h.getLevel().registryAccess().getOrThrow(KatanaEnchantments.MASTER_PARRYING),1);long seed=seed(p,roll);p.getRandom().setSeed(seed);KatanaDefense.guard(p);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(target),4);p.stopUsingItem();}
    static void counters(GameTestHelper h,boolean roll){
        try(var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get())){var p=f.player();var cow=target(h);parry(h,p,cow,roll);
            cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==93,"First counter gets three damage bonus");cow.damageCooldownTime=0;
            cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==(roll?86:89),"Second counter follows the one-time thirty-percent roll");cow.damageCooldownTime=0;
            cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==(roll?82:85),"A third counter is impossible");
        }h.succeed();
    }
    static void nativeCounter(GameTestHelper h){
        try(var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get())){var p=f.player();var cow=target(h);parry(h,p,cow,true);
            // Native attack HEAD/RETURN must defer the second opportunity until the
            // first attack (including any secondary damage events) has ended.
            p.attack(cow);cow.damageCooldownTime=0;float first=cow.getHealth();p.attack(cow);float second=cow.getHealth();cow.damageCooldownTime=0;p.attack(cow);
            h.assertTrue(first-second-(second-cow.getHealth())>2.9,"Native second attack gets a bonus; third attack does not");
        }h.succeed();
    }
    static void dodgeNoExtra(GameTestHelper h){
        var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get());var p=f.player();var cow=target(h);p.getMainHandItem().enchant(h.getLevel().registryAccess().getOrThrow(KatanaEnchantments.MASTER_PARRYING),1);p.getRandom().setSeed(seed(p,true));KatanaDefense.dodge(p,5);p.hurtServer(h.getLevel(),h.getLevel().damageSources().mobAttack(cow),4);
        h.runAfterDelay(9,()->{try{cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);cow.damageCooldownTime=0;cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==89,"Master Parrying never grants a second Perfect Dodge counter");h.succeed();}finally{f.close();}});
    }
    static void failed(GameTestHelper h){
        try(var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get())){var p=f.player();var cow=target(h);parry(h,p,cow,true);cow.setPermanentlyInvulnerable(true);p.attack(cow);cow.setPermanentlyInvulnerable(false);cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==96,"A failed first attack cannot unlock the second counter");}h.succeed();
    }
    static void secondMiss(GameTestHelper h){
        try(var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get())){var p=f.player();var cow=target(h);parry(h,p,cow,true);cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);KatanaDefense.miss(p);cow.damageCooldownTime=0;cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(cow.getHealth()==89,"A miss consumes the earned second opportunity");}h.succeed();
    }
    static void originalExpiry(GameTestHelper h){
        var f=DefenseTests.player(h,KatanaContent.FROSTGLEAM.get());var p=f.player();var cow=target(h);parry(h,p,cow,true);
        h.runAfterDelay(15,()->cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4));
        h.runAfterDelay(21,()->{try{float before=cow.getHealth();cow.damageCooldownTime=0;cow.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),4);h.assertTrue(before-cow.getHealth()==4,"First counter does not extend the original one-second window for the second");h.succeed();}finally{f.close();}});
    }
}
