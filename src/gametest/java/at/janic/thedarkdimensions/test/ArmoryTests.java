package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

final class ArmoryTests {
    private static Item nativeItem(String name) { return BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(name)); }
    private static void recipe(GameTestHelper h,List<ItemStack> input,int w,int height,Item output) {
        var grid=CraftingInput.of(w,height,input);var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel());
        h.assertTrue(found.isPresent()&&found.get().value().assemble(grid).is(output),"Crafting recipe for "+output);
    }
    static void recipes(GameTestHelper h) {
        var iron=new ItemStack(Items.IRON_INGOT);var improved=new ItemStack(TheDarkDimensions.IMPROVED_STICK.get());
        recipe(h,List.of(iron,iron,iron,new ItemStack(Items.STICK)),2,2,ArmoryContent.THROWABLE_AXE.get());
        recipe(h,List.of(new ItemStack(Items.LEATHER),improved,new ItemStack(KatanaContent.CREEPERBLOOD.get())),3,1,ArmoryContent.WARRIOR_STICK.get());
        var block=new ItemStack(Items.DIAMOND_BLOCK);var core=new ItemStack(CaveContent.LIGHTNING_CORE.get());var diamond=new ItemStack(Items.DIAMOND);
        recipe(h,List.of(diamond,block,diamond,core,improved,core,ItemStack.EMPTY,improved,ItemStack.EMPTY),3,3,ArmoryContent.HAMMER.get());
        for(var tier:ArmoryContent.TIERS) {
            if(tier.name().equals("netherite"))continue;
            var material=new ItemStack(nativeItem(switch(tier.name()) {case "wooden" -> "oak_planks";case "stone" -> "cobblestone";case "diamond" -> "diamond";case "golden" -> "gold_ingot";default -> tier.name()+"_ingot";}));
            var full=new ItemStack(nativeItem(switch(tier.name()) {case "wooden" -> "oak_log";case "stone" -> "stone";case "golden" -> "gold_block";default -> tier.name()+"_block";}));
            recipe(h,List.of(material,full,material,material,improved,ItemStack.EMPTY,ItemStack.EMPTY,new ItemStack(ArmoryContent.WARRIOR_STICK.get()),ItemStack.EMPTY),3,3,ArmoryContent.WARRIOR_AXES.get(tier.name()).get());
        }
        h.succeed();
    }
    static void smithing(GameTestHelper h) {
        for(var tier:ArmoryContent.TIERS)for(var fish:List.of(Items.COD,Items.SALMON,Items.TROPICAL_FISH,Items.PUFFERFISH,Items.COOKED_COD,Items.COOKED_SALMON)) {
            var axe=new ItemStack(nativeItem(tier.name()+"_axe"));axe.setDamageValue(7);axe.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keepsake"));
            axe.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),2);
            var input=new SmithingRecipeInput(new ItemStack(ToolContent.IMPROVING_TEMPLATE.get()),axe,new ItemStack(fish));
            var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel());
            h.assertTrue(found.isPresent(),"All six fish work at all seven tiers");var result=found.get().value().assemble(input);
            h.assertTrue(result.is(ArmoryContent.FISH_AXES.get(tier.name()))&&result.getDamageValue()==7&&result.get(DataComponents.CUSTOM_NAME).getString().equals("Keepsake")&&result.isEnchanted(),"Fish conversion preserves wear, name and enchantments");
        }
        var hammer=new ItemStack(ArmoryContent.HAMMER.get());hammer.setDamageValue(37);
        var input=new SmithingRecipeInput(new ItemStack(CaveContent.LIGHTNING_CORE.get()),hammer,new ItemStack(SpellContent.LIGHTNING_BOOK.get()));
        var found=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel());
        h.assertTrue(found.isPresent()&&found.get().value().assemble(input).is(ArmoryContent.AWAKENED_HAMMER)&&found.get().value().assemble(input).getDamageValue()==37,"Hammer awakening uses Lightning Core as template and preserves damage");
        var menu=new net.minecraft.world.inventory.SmithingMenu(0,h.makeMockPlayer(GameType.SURVIVAL).getInventory());
        h.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(CaveContent.LIGHTNING_CORE.get())),"Smithing UI accepts the core in its template slot");h.succeed();
    }
    static void axeStats(GameTestHelper h) {
        for(var tier:ArmoryContent.TIERS) {
            var normal=new ItemStack(nativeItem(tier.name()+"_axe"));var fish=new ItemStack(ArmoryContent.FISH_AXES.get(tier.name()).get());var warrior=new ItemStack(ArmoryContent.WARRIOR_AXES.get(tier.name()).get());
            double base=normal.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND);
            h.assertTrue(fish.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==base,"Fish axe keeps native damage");
            h.assertTrue(warrior.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==base+2,"Warrior axe adds exactly two damage");
            float speed=normal.get(DataComponents.TOOL).getMiningSpeed(Blocks.OAK_LOG.defaultBlockState());
            h.assertTrue(fish.get(DataComponents.TOOL).getMiningSpeed(Blocks.OAK_LOG.defaultBlockState())==speed&&Math.abs(warrior.get(DataComponents.TOOL).getMiningSpeed(Blocks.OAK_LOG.defaultBlockState())-speed*1.15f)<0.001,"Fish mining is native; Warrior mining 15 percent faster");
            h.assertTrue(fish.get(DataComponents.BLOCK_TRANSFORMER).equals(normal.get(DataComponents.BLOCK_TRANSFORMER)),"Fish axe retains stripping and copper scraping");
        }
        h.succeed();
    }
    static void thrownPickup(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(h.absoluteVec(new Vec3(5.5,3,2.5)));player.setYRot(0);player.setXRot(0);
        var stack=new ItemStack(ArmoryContent.THROWABLE_AXE.get());stack.setDamageValue(42);stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Returning axe"));
        stack.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),1);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        for(int x=3;x<=7;x++)for(int y=0;y<8;y++)h.setBlock(x,y,8,Blocks.STONE);
        ArmoryContent.THROWABLE_AXE.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(player.getMainHandItem().isEmpty(),"Survival throw consumes exactly the held axe");
        var axes=h.getLevel().getEntitiesOfClass(ThrownAxe.class,player.getBoundingBox().inflate(3));h.assertTrue(axes.size()==1,"One projectile spawned");var axe=axes.getFirst();
        h.runAfterDelay(18,() -> {
            h.assertTrue(axe.isInGround(),"Axe sticks in block like an arrow");axe.playerTouch(player);
            h.assertTrue(axe.isRemoved(),"Pickup removes projectile");
            var returned=player.getInventory().getNonEquipmentItems().stream().filter(s -> s.is(ArmoryContent.THROWABLE_AXE)).findFirst();
            h.assertTrue(returned.isPresent()&&returned.get().getDamageValue()==43&&returned.get().isEnchanted()&&returned.get().get(DataComponents.CUSTOM_NAME).getString().equals("Returning axe"),"Recovered axe preserves enchantments, name and wear");h.succeed();
        });
    }
    static void gliding(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setOnGround(false);
        h.assertTrue(!player.tryToStartFallFlying(),"No hammer or Elytra means no glide");
        for(var hammer:List.of(ArmoryContent.HAMMER.get(),ArmoryContent.AWAKENED_HAMMER.get())) {
            var stack=new ItemStack(hammer);
            h.assertTrue(stack.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND)==10,"Hammer deals ten melee damage");
            boolean awakened=stack.is(ArmoryContent.AWAKENED_HAMMER);
            h.assertTrue(LivingEntity.canGlideUsing(stack,EquipmentSlot.MAINHAND)==awakened&&LivingEntity.canGlideUsing(stack,EquipmentSlot.OFFHAND)==awakened&&!LivingEntity.canGlideUsing(stack,EquipmentSlot.CHEST),"Only awakened hammer glides, in either hand");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.tick();player.setOnGround(false);
            h.assertTrue(player.tryToStartFallFlying()==awakened&&player.isFallFlying()==awakened,"Only awakened hammer starts native Elytra flight");player.stopFallFlying();
            stack.setDamageValue(stack.getMaxDamage()-1);h.assertTrue(!LivingEntity.canGlideUsing(stack,EquipmentSlot.MAINHAND),"Broken hammer cannot glide");
            player.tick();player.setOnGround(false);h.assertTrue(!player.tryToStartFallFlying(),"Broken hammer removes NeoForge flight ability");
        }
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ArmoryContent.AWAKENED_HAMMER.get()));player.tick();player.setOnGround(false);
        h.assertTrue(player.tryToStartFallFlying(),"Offhand hammer also enables flight");player.stopFallFlying();
        player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ArmoryContent.HAMMER.get()));player.tick();player.setOnGround(false);h.assertTrue(!player.tryToStartFallFlying(),"Switching to basic hammer removes flight ability");
        player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);player.tick();player.setOnGround(false);h.assertTrue(!player.tryToStartFallFlying(),"Putting hammer away removes flight");
        h.succeed();
    }
    static void blast(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(h.absoluteVec(new Vec3(4.5,2,3.5)));player.setYRot(0);player.setXRot(0);
        var target=h.spawn(EntityTypes.ZOMBIE,new Vec3(4.5,2,8.5));target.setNoAi(true);float health=target.getHealth();
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ArmoryContent.AWAKENED_HAMMER.get()));
        h.setBlock(4,3,6,Blocks.STONE);ArmoryContent.AWAKENED_HAMMER.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);
        h.assertTrue(target.getHealth()==health,"Lightning targeting cannot pass through walls");h.setBlock(4,3,6,Blocks.AIR);
        h.assertTrue(ArmoryContent.AWAKENED_HAMMER.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND)==InteractionResult.SUCCESS&&target.getHealth()<health,"Awakened hammer strikes the aimed enemy");
        h.assertTrue(!h.getLevel().getEntitiesOfClass(LightningBolt.class,target.getBoundingBox().inflate(2)).isEmpty(),"Lightning strike entity appears on target");float after=target.getHealth();
        ArmoryContent.AWAKENED_HAMMER.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND);h.assertTrue(target.getHealth()==after,"Cooldown stops repeated blast");h.succeed();
    }
}
