package at.janic.thedarkdimensions.test;
import at.janic.thedarkdimensions.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

final class DevilTests {
    static void upgrade(GameTestHelper h) {
        var base=new ItemStack(Items.TRIDENT);base.setDamageValue(83);base.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Old trident"));
        base.enchant(h.getLevel().registryAccess().getOrThrow(Enchantments.LOYALTY),3);
        var input=new SmithingRecipeInput(new ItemStack(DevilContent.TEAR.get()),base,new ItemStack(DevilContent.EYE.get()));
        var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,input,h.getLevel());h.assertTrue(recipe.isPresent(),"Devil's Tear and Eye upgrade a Trident");
        var obsolete=new SmithingRecipeInput(new ItemStack(BloodNetheriteContent.TEMPLATE.get()),base,new ItemStack(BloodNetheriteContent.INGOT.get()));
        h.assertTrue(h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.SMITHING,obsolete,h.getLevel()).isEmpty(),"Old Blood Netherite trident recipe removed");
        var menu=new net.minecraft.world.inventory.SmithingMenu(0,h.makeMockPlayer(GameType.SURVIVAL).getInventory());
        h.assertTrue(menu.getSlot(0).mayPlace(new ItemStack(DevilContent.TEAR.get())),"Template field accepts Devil's Tear");
        var output=recipe.get().value().assemble(input);h.assertTrue(output.is(DevilContent.TRIDENT)&&output.getDamageValue()==83&&output.isEnchanted()&&output.get(DataComponents.CUSTOM_NAME).getString().equals("Old trident"),"Smithing preserves existing trident components");
        double damage=output.get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND);
        h.assertTrue(damage==new ItemStack(Items.NETHERITE_SWORD).get(DataComponents.ATTRIBUTE_MODIFIERS).compute(Attributes.ATTACK_DAMAGE,1,EquipmentSlot.MAINHAND),"Devil melee damage equals a Netherite sword");
        for(var key:java.util.List.of(Enchantments.LOYALTY,Enchantments.CHANNELING,Enchantments.RIPTIDE,Enchantments.IMPALING))h.assertTrue(h.getLevel().registryAccess().getOrThrow(key).value().isSupportedItem(output),"Native trident enchantment supported: "+key);
        h.succeed();
    }
    static void melee(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);var enemy=h.spawn(EntityTypes.ZOMBIE,new Vec3(4,2,4));enemy.setNoAi(true);
        var stack=new ItemStack(DevilContent.TRIDENT.get());DevilContent.TRIDENT.get().hurtEnemy(stack,enemy,player);
        h.assertTrue(enemy.hasEffect(KatanaEffects.BLOOD)&&enemy.getEffect(KatanaEffects.BLOOD).getDuration()==100&&enemy.isOnFire(),"Melee applies five seconds of Blood and fire");
        h.assertTrue(player.hasEffect(DevilContent.DEVILS_CHARM)&&player.getEffect(DevilContent.DEVILS_CHARM).getDuration()==100,"Melee grants five seconds of charm");
        player.removeEffect(DevilContent.DEVILS_CHARM);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        h.assertTrue(DevilContent.TRIDENT.get().use(h.getLevel(),player,InteractionHand.MAIN_HAND).consumesAction()&&player.hasEffect(DevilContent.DEVILS_CHARM),"Beginning right-click use also grants charm");player.stopUsingItem();h.succeed();
    }
    static void throwing(GameTestHelper h) {
        var player=(net.minecraft.server.level.ServerPlayer)h.makeMockServerPlayer(GameType.SURVIVAL);player.setPos(h.absoluteVec(new Vec3(5.5,2,2.5)));player.setYRot(0);player.setXRot(0);
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        h.getLevel().getServer().getPlayerList().placeNewPlayer(connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        player.setPos(h.absoluteVec(new Vec3(5.5,2,2.5)));player.setYRot(0);player.setXRot(0);player.setNoGravity(true);
        var enemy=h.spawn(EntityTypes.VILLAGER,new Vec3(5.5,2,8.5));enemy.setNoAi(true);enemy.setNoGravity(true);
        var stack=new ItemStack(DevilContent.TRIDENT.get());stack.setDamageValue(42);stack.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Returning devil"));stack.enchant(h.getLevel().registryAccess().getOrThrow(Enchantments.LOYALTY),3);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        h.assertTrue(DevilContent.TRIDENT.get().releaseUsing(stack,h.getLevel(),player,71990),"Native charged throw succeeds");h.assertTrue(player.getMainHandItem().isEmpty(),"Throw transfers the held item without duplication");
        var arrows=h.getLevel().getEntitiesOfClass(ThrownTrident.class,player.getBoundingBox().inflate(3));h.assertTrue(arrows.size()==1,"Native Trident projectile spawned");var projectile=arrows.getFirst();
        h.assertTrue(((DevilProjectile)projectile).darkDimensions$isDevil(),"Devil render identity is synced");
        h.runAfterDelay(6,() -> {
            h.assertTrue(enemy.getHealth()<=12&&enemy.hasEffect(KatanaEffects.BLOOD)&&enemy.isOnFire(),"Thrown hit deals eight damage, blood and fire");
            h.assertTrue(projectile.isNoPhysics(),"Native Loyalty return begins after hit");double distance=projectile.distanceToSqr(player);
            h.runAfterDelay(5,() -> {
                h.assertTrue(projectile.distanceToSqr(player)<distance,"Loyalty moves projectile back to owner");projectile.playerTouch(player);
                var returned=player.getInventory().getNonEquipmentItems().stream().filter(s -> s.is(DevilContent.TRIDENT)).findFirst();
                h.assertTrue(projectile.isRemoved()&&returned.isPresent()&&returned.get().getDamageValue()==43&&returned.get().isEnchanted()&&returned.get().get(DataComponents.CUSTOM_NAME).getString().equals("Returning devil"),"Pickup retains damage, name and Loyalty");h.succeed();
            });
        });
    }
    static void charm(GameTestHelper h) {
        for(int x=0;x<10;x++)for(int z=0;z<10;z++)h.setBlock(x,1,z,Blocks.STONE);
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setPos(h.absoluteVec(new Vec3(2.5,2,3.5)));
        var adult=h.spawn(EntityTypes.VILLAGER,new Vec3(5.5,2,3.5));var baby=h.spawn(EntityTypes.VILLAGER,new Vec3(5.5,2,5.5));baby.setAge(-24000);
        var far=h.spawn(EntityTypes.VILLAGER,new Vec3(16.5,2,3.5));far.setNoAi(true);
        var effect=DevilContent.DEVILS_CHARM.get();effect.applyEffectTick(h.getLevel(),player,0);
        for(var villager:java.util.List.of(adult,baby))h.assertTrue(villager.getBrain().getMemory(MemoryModuleType.NEAREST_HOSTILE).orElse(null)==player,"Charm marks wearer as frightening for adults and babies");
        h.assertTrue(!far.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE),"Villagers outside eight-block range are unaffected");
        double adultBefore=adult.distanceToSqr(player),babyBefore=baby.distanceToSqr(player);
        boolean[] stopped={false};
        h.onEachTick(() -> {if(!stopped[0]&&h.getTick()%5==0)effect.applyEffectTick(h.getLevel(),player,0);});
        h.startSequence().thenWaitUntil(() ->
            h.assertTrue(adult.distanceToSqr(player)>adultBefore&&baby.distanceToSqr(player)>babyBefore,"Adult and baby AI actually move away from charm wearer")
        ).thenExecute(() -> stopped[0]=true).thenIdle(35).thenExecute(() ->
            h.assertTrue(!adult.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE)&&!baby.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE),"Fear expires after charm stops")
        ).thenSucceed();
    }
}
