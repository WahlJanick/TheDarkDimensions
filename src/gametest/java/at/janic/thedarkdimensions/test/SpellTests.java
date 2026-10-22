package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;

final class SpellTests {
    static void enderChest(GameTestHelper h) {
        var player=h.makeMockServerPlayerInLevel();
        player.getEnderChestInventory().setItem(0,new ItemStack(Items.DIAMOND,3));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpellContent.ENDER_CHEST_BOOK.get()));
        SpellContent.ENDER_CHEST_BOOK.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.containerMenu instanceof net.minecraft.world.inventory.ChestMenu,"Opens portable chest menu");
        h.assertTrue(player.containerMenu.getSlot(0).getItem().is(Items.DIAMOND),"Uses player's existing personal Enderchest");
        player.containerMenu.getSlot(0).set(new ItemStack(Items.EMERALD,2));
        h.assertTrue(player.getEnderChestInventory().getItem(0).is(Items.EMERALD),"Changes persist in personal storage");
        player.closeContainer();h.succeed();
    }
    static void lightning(GameTestHelper h) {
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(4.5,2,3.5)));player.setYRot(0);player.setXRot(0);
        h.setBlock(4,3,8,net.minecraft.world.level.block.Blocks.STONE);
        var victim=h.spawn(net.minecraft.world.entity.EntityTypes.ZOMBIE,new net.minecraft.world.phys.Vec3(4.5,2,7.5));victim.setNoAi(true);
        float health=victim.getHealth();
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpellContent.LIGHTNING_BOOK.get()));
        SpellContent.LIGHTNING_BOOK.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(victim.getHealth()<health&&victim.getHealth()>=health-6,"Lightning damages enemies at aimed target, respecting armor");
        h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,victim.getBoundingBox().inflate(3)).size()>0,"Spawns lightning visual");h.succeed();
    }
    static void selection(GameTestHelper h) {
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.experienceLevel=0;
        var cores=List.of(TheDarkDimensions.FIRE_CORE.get(),CaveContent.LIGHTNING_CORE.get(),MagicContent.ENDER_CORE.get(),CaveContent.WATER_CORE.get());
        for(int i=0;i<4;i++) {
            var menu=new SpelltableMenu(i,player.getInventory());
            h.assertTrue(!menu.clickMenuButton(player,0),"Missing ingredients rejected");
            menu.ingredients.setItem(0,new ItemStack(SpellContent.SPELL_BOOK.get()));
            menu.ingredients.setItem(1,new ItemStack(cores.get(i),2));
            h.assertTrue(!menu.clickMenuButton(player,4)&&!menu.clickMenuButton(player,-1),"Unknown actions rejected");
            h.assertTrue(menu.clickMenuButton(player,0),"Core binds element without XP");
            h.assertTrue(menu.result.getItem(0).is(SpellContent.bookFor(i)),"Core determines elemental book");
            var data=MagicData.read(menu.result.getItem(0));
            h.assertTrue(data.learned==0&&java.util.Arrays.stream(data.slots).allMatch(n->n==-1),"New book needs skill learning");
            h.assertTrue(menu.ingredients.getItem(0).isEmpty()&&menu.ingredients.getItem(1).getCount()==1,"Consumes one book and core");
            h.assertTrue(!menu.clickMenuButton(player,0)&&player.experienceLevel==0,"No duplicate crafting or XP cost");
            h.assertTrue(!menu.quickMoveStack(player,2).isEmpty()&&menu.result.isEmpty(),"Shift-click retrieves book");
            menu.removed(player);h.assertTrue(menu.ingredients.isEmpty(),"Closing returns ingredients");
        }
        h.succeed();
    }
    private static void recipe(GameTestHelper h,List<ItemStack> stacks,int width,int height,Item result) {
        var input=CraftingInput.of(width,height,stacks);
        var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(result),"Correct recipe for "+result);
    }
    static void recipes(GameTestHelper h) {
        recipe(h,List.of(new ItemStack(TheDarkDimensions.EMPTY_RUNESTONE.get()),new ItemStack(Items.BOOK),new ItemStack(SpellContent.METALINIUM_INGOT.get())),3,1,SpellContent.SPELL_BOOK.get());
        recipe(h,Collections.nCopies(9,new ItemStack(SpellContent.METALINIUM_NUGGET.get())),3,3,SpellContent.METALINIUM_INGOT.get());
        recipe(h,List.of(new ItemStack(SpellContent.METALINIUM_INGOT.get()),new ItemStack(MineralContent.CHRONUS_ORB.get())),2,1,SpellContent.INFUSED_INGOT.get());
        var registries=h.getLevel().registryAccess();
        var ore=registries.lookupOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE).getValue(TheDarkDimensions.id("metalinium_ore"));
        var biomes=registries.lookupOrThrow(net.minecraft.core.registries.Registries.BIOME);
        h.assertTrue(biomes.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("plains")).getGenerationSettings().hasFeature(ore),"Metalinium is injected into Overworld terrain");
        h.assertTrue(!biomes.getValue(TheDarkDimensions.id("normal_caves")).getGenerationSettings().hasFeature(ore),"Metalinium does not enter Burned Realm caves");
        h.succeed();
    }
    static void loot(GameTestHelper h) {
        var level=h.getLevel();var pick=new ItemStack(Items.DIAMOND_PICKAXE);
        var fortune=pick.copy();fortune.enchant(level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE),3);
        var silk=pick.copy();silk.enchant(level.registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH),1);
        for(var block:List.of(SpellContent.METALINIUM_ORE.get(),SpellContent.DEEPSLATE_METALINIUM_ORE.get())) {
            boolean sawNine=false;
            for(int i=0;i<250;i++) {
                int plain=Block.getDrops(block.defaultBlockState(),level,h.absolutePos(new BlockPos(3,3,3)),null,null,pick).stream().filter(s -> s.is(SpellContent.METALINIUM_NUGGET)).mapToInt(ItemStack::getCount).sum();
                int enhanced=Block.getDrops(block.defaultBlockState(),level,h.absolutePos(new BlockPos(3,3,3)),null,null,fortune).stream().filter(s -> s.is(SpellContent.METALINIUM_NUGGET)).mapToInt(ItemStack::getCount).sum();
                h.assertTrue(plain>=3&&plain<=6&&enhanced>=3&&enhanced<=9,"Normal 3–6; Fortune bounded at nine");sawNine|=enhanced==9;
            }
            h.assertTrue(sawNine,"Fortune III can reach nine nuggets");
            h.assertTrue(Block.getDrops(block.defaultBlockState(),level,h.absolutePos(new BlockPos(3,3,3)),null,null,silk).stream().anyMatch(s -> s.is(block.asItem())),"Silk Touch preserves ore");
        }
        h.succeed();
    }
    static void water(GameTestHelper h) {
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setHealth(10);player.igniteForSeconds(10);
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpellContent.WATER_BOOK.get()));
        SpellContent.WATER_BOOK.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getHealth()==16&&!player.isOnFire(),"Water heals three hearts and extinguishes fire");
        h.assertTrue(player.getItemInHand(InteractionHand.MAIN_HAND).getCount()==1,"Book is reusable");
        for(int i=0;i<4;i++)h.assertTrue(player.getCooldowns().isOnCooldown(new ItemStack(SpellContent.bookFor(i))),"Shared cooldown prevents switching exploits");
        SpellContent.WATER_BOOK.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getHealth()==16,"Cooldown blocks second heal");h.succeed();
    }
    static void fire(GameTestHelper h) {
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(4,3,4)));
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpellContent.FIRE_BOOK.get()));
        SpellContent.FIRE_BOOK.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(h.getLevel().getEntitiesOfClass(SmallFireball.class,player.getBoundingBox().inflate(2)).stream().anyMatch(e -> e.getOwner()==player),"Fire burst spawns owned projectile");h.succeed();
    }
}


