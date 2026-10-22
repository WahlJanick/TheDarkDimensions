package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import at.janic.thedarkdimensions.finale.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import eu.pb4.trinkets.api.TrinketsApi;

final class BackEquipmentTests {
    static void creativeSkills(GameTestHelper h){try(var f=DefenseTests.player(h,MagicContent.MASTER_BOOK.get())){var p=f.player();var menu=new SpellbookMenu(1,p.getInventory(),0);int id=MagicSkill.DIMENSION_FOLD.ordinal();
        h.assertTrue(!menu.clickMenuButton(p,100+id),"Survival cannot skip element, predecessor or ingot requirements");p.setGameMode(GameType.CREATIVE);
        h.assertTrue(menu.clickMenuButton(p,100+id),"Creative learns a skill on an unbound Master Book without ingredients");
        for(int rank=2;rank<=5;rank++){h.assertTrue(menu.clickMenuButton(p,100+id),"Creative click upgrades one rank");h.assertTrue(MagicData.read(p.getMainHandItem()).level(id)==rank,"Creative rank matches saved book progress");}
        h.assertTrue(!menu.clickMenuButton(p,100+id)&&MagicData.read(p.getMainHandItem()).uses[id]==70,"Creative mastery stops at level five");
        h.assertTrue(java.util.Arrays.stream(MagicData.read(p.getMainHandItem()).slots).filter(slot->slot==id).count()==1,"Upgrades do not overwrite extra wheel slots");
        h.assertTrue(menu.clickMenuButton(p,1000+id*5+4),"Upgraded skill still binds to the fifth wheel slot");p.setGameMode(GameType.SURVIVAL);var d=MagicData.read(p.getMainHandItem());
        h.assertTrue(d.level(id)==5&&d.slots[4]==id&&!menu.clickMenuButton(p,100+MagicSkill.INFERNO.ordinal()),"Saved Creative progress persists, but Survival cannot buy another shortcut");
    }h.succeed();}
    static void slots(GameTestHelper h){if(!BackEquipment.trinkets()){h.succeed();return;}var p=h.makeMockPlayer(GameType.SURVIVAL);var attachment=TrinketsApi.getAttachment(p);
        h.assertTrue(attachment.getGroups().get("feet").slotId()==8&&attachment.getGroups().get("chest").slotId()==6,"Slots attach beside native boots and chestplate");
        for(String id:List.of(TrinketsMagic.BOOK_SLOT,TrinketsMagic.FOCUS_SLOT,TrinketsMagic.TALISMAN_SLOT,TrinketsMagic.FEET_SLOT,TrinketsMagic.BACK_SLOT,TrinketsMagic.GLIDER_SLOT))h.assertTrue(attachment.getInventory(id)!=null&&attachment.getInventory(id).getContainerSize()==1,"Dedicated accessory slot exists: "+id);
        var inv=attachment.getInventory(TrinketsMagic.BACK_SLOT);var ref=inv.getOrCreateSlotAccess(0);h.assertTrue(inv.slotType().isVanityOnly(),"Stored tools do not contribute combat attributes or other equipment effects");
        for(var item:List.of(Items.DIAMOND_SWORD,Items.IRON_AXE,Items.NETHERITE_PICKAXE,Items.SHEARS,Items.FISHING_ROD,Items.BOW,RelicKatanas.MINDSHIFTER.get(),SolarContent.STORMVEIL.get()))h.assertTrue(inv.slotType().validatorCheck(new ItemStack(item),ref,p),"Back slot accepts actual tool components: "+item);
        for(var item:List.of(Items.DIRT,Items.DIAMOND_CHESTPLATE,Items.ELYTRA,BackEquipment.GLIDER.get()))h.assertTrue(!inv.slotType().validatorCheck(new ItemStack(item),ref,p),"Back tool storage excludes blocks, armor and gliders");
        var wings=attachment.getInventory(TrinketsMagic.GLIDER_SLOT);for(var item:List.of(Items.ELYTRA,BackEquipment.GLIDER.get(),BackEquipment.WINGS.get()))h.assertTrue(wings.slotType().validatorCheck(new ItemStack(item),wings.getOrCreateSlotAccess(0),p),"Wing slot accepts GLIDER equipment");
        h.assertTrue(!wings.slotType().validatorCheck(new ItemStack(Items.DIAMOND_SWORD),wings.getOrCreateSlotAccess(0),p),"Wing slot rejects weapons");
        h.assertTrue(attachment.getInventory(TrinketsMagic.FEET_SLOT).slotType().icon().toString().equals("minecraft:container/slot/boots"),"Feet uses predefined boots icon");h.succeed();
    }
    static void swap(GameTestHelper h){if(!BackEquipment.trinkets()){h.succeed();return;}try(var f=DefenseTests.player(h,RelicKatanas.MINDSHIFTER.get())){var p=f.player();var sword=p.getMainHandItem();sword.setDamageValue(43);sword.enchant(h.getLevel().registryAccess().getOrThrow(Enchantments.UNBREAKING),3);PerkContent.coat(sword);var original=sword.copy();var pick=new ItemStack(Items.NETHERITE_PICKAXE);pick.setDamageValue(17);var pickCopy=pick.copy();
        h.assertTrue(TrinketsMagic.setSlot(p,TrinketsMagic.BACK_SLOT,pick),"Initial storage slot access is created lazily");h.assertTrue(BackEquipment.swap(p)&&ItemStack.isSameItemSameComponents(p.getMainHandItem(),pickCopy)&&ItemStack.isSameItemSameComponents(BackEquipment.stored(p),original),"Atomic swap preserves damage, enchants and coating");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIRT,64));h.assertTrue(!BackEquipment.swap(p)&&BackEquipment.stored(p).is(RelicKatanas.MINDSHIFTER)&&p.getMainHandItem().getCount()==64,"Invalid input cannot lose or duplicate stored equipment");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(BackEquipment.swap(p)&&BackEquipment.stored(p).isEmpty()&&ItemStack.isSameItemSameComponents(p.getMainHandItem(),original),"Empty main hand retrieves the exact stored stack");h.assertTrue(BackEquipment.swap(p)&&p.getMainHandItem().isEmpty(),"Empty back slot stores the held tool");
        p.containerMenu=ChestMenu.threeRows(4,p.getInventory(),new net.minecraft.world.SimpleContainer(27));h.assertTrue(!BackEquipment.swap(p),"Network swap cannot edit equipment during another container transaction");p.containerMenu=p.inventoryMenu;
    }h.succeed();}
    static void sandals(GameTestHelper h){if(!BackEquipment.trinkets()){h.succeed();return;}try(var f=DefenseTests.player(h,Items.IRON_SWORD)){var p=f.player();p.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));TrinketsMagic.setSlot(p,TrinketsMagic.FEET_SLOT,new ItemStack(PerkContent.WINDSTEP_SANDALS.get()));var start=p.position();
        h.assertTrue(PerkContent.dash(p)&&p.position().distanceToSqr(start)>1,"Feet-side sandals activate dash alongside normal boots");h.assertTrue(p.getItemBySlot(EquipmentSlot.FEET).getDamageValue()==0&&TrinketsMagic.getSlot(p,TrinketsMagic.FEET_SLOT,true).getDamageValue()==1,"Dash damages sandals, not equipped armor boots");h.assertTrue(!PerkContent.dash(p),"The existing dash cooldown also covers accessory sandals");
    }h.succeed();}
    private static void fly(GameTestHelper h,ServerPlayer p){var at=h.absoluteVec(new Vec3(5.5,11,5.5));p.teleportTo(at.x,at.y,at.z);p.stopFallFlying();p.setNoGravity(true);p.setOnGround(false);h.onEachTick(()->h.getLevel().getChunkSource().addTicketWithRadius(TicketType.PORTAL,p.chunkPosition(),2));}
    static void vanillaChest(GameTestHelper h){var f=DefenseTests.player(h,Items.AIR);var p=f.player();p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(BackEquipment.GLIDER.get()));fly(h,p);h.runAfterDelay(2,()->{try(f){h.assertTrue(p.tryToStartFallFlying()&&p.isFallFlying(),"Wool Glider works in native chest slot, even without Trinkets");p.stopFallFlying();p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(BackEquipment.WINGS.get()));h.assertTrue(p.tryToStartFallFlying()&&BackEquipment.devilWings(p),"Unbreakable wings use native GLIDER/Equippable components");p.getItemBySlot(EquipmentSlot.CHEST).hurtAndBreak(1000,p,EquipmentSlot.CHEST);h.assertTrue(p.getItemBySlot(EquipmentSlot.CHEST).is(BackEquipment.WINGS)&&p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==0,"Devil Wings cannot lose durability");h.succeed();}});}
    static void backFlight(GameTestHelper h){if(!BackEquipment.trinkets()){h.succeed();return;}var f=DefenseTests.player(h,Items.AIR);var p=f.player();p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));TrinketsMagic.setSlot(p,TrinketsMagic.GLIDER_SLOT,new ItemStack(Items.ELYTRA));fly(h,p);
        h.assertTrue(p.tryToStartFallFlying()&&p.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE),"Native Trinkets flight preserves chest armor");
        h.runAfterDelay(24,()->{try(f){h.assertTrue(TrinketsMagic.getSlot(p,TrinketsMagic.GLIDER_SLOT,true).getDamageValue()>=1,"Native flight applies durability to actual back Elytra");h.succeed();}});
    }
    static void woolWear(GameTestHelper h){if(!BackEquipment.trinkets()){h.succeed();return;}var f=DefenseTests.player(h,Items.AIR);var p=f.player();TrinketsMagic.setSlot(p,TrinketsMagic.GLIDER_SLOT,new ItemStack(BackEquipment.GLIDER.get()));fly(h,p);h.assertTrue(p.tryToStartFallFlying(),"Wool glider starts from the dedicated back slot");
        h.runAfterDelay(44,()->{var glider=TrinketsMagic.getSlot(p,TrinketsMagic.GLIDER_SLOT,true);h.assertTrue(glider.getDamageValue()>=6&&glider.getCount()==1,"Cloth glider accumulates much faster wear than a normal Elytra");glider.setDamageValue(glider.getMaxDamage()-1);});
        h.runAfterDelay(47,()->{try(f){h.assertTrue(!p.isFallFlying()&&TrinketsMagic.getSlot(p,TrinketsMagic.GLIDER_SLOT,true).getCount()==1,"Worn glider stops flight and remains as repairable broken equipment");h.succeed();}});
    }
    static void rockets(GameTestHelper h){var f=DefenseTests.player(h,Items.FIREWORK_ROCKET);var p=f.player();fly(h,p);p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(BackEquipment.GLIDER.get()));p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.FIREWORK_ROCKET,3));h.runAfterDelay(2,()->{try(f){h.assertTrue(p.tryToStartFallFlying(),"Glider starts for rocket restriction test");
        h.assertTrue(!p.getMainHandItem().use(h.getLevel(),p,InteractionHand.MAIN_HAND).consumesAction()&&p.getMainHandItem().getCount()==3,"Wool glider cannot consume or launch a booster");
        p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.ELYTRA));var rocket=new FireworkRocketEntity(h.getLevel(),p.getMainHandItem(),p);p.setDeltaMovement(Vec3.ZERO);rocket.tick();h.assertTrue(p.getDeltaMovement().length()>.5,"Normal Elytra still receives native firework boost");
        p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(BackEquipment.GLIDER.get()));p.setDeltaMovement(Vec3.ZERO);rocket.tick();h.assertTrue(p.getDeltaMovement().lengthSqr()<.001,"Changing to cloth during a boost cannot bypass the restriction");rocket.discard();h.succeed();}});
    }
    static void lava(GameTestHelper h){var f=DefenseTests.player(h,Items.AIR);var p=f.player();fly(h,p);p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(BackEquipment.WINGS.get()));for(int x=4;x<=7;x++)for(int y=9;y<=13;y++)for(int z=4;z<=7;z++)h.setBlock(x,y,z,Blocks.LAVA);
        h.runAfterDelay(5,()->{try(f){p.setOnGround(false);h.assertTrue(p.isInLava()&&!p.fireImmune(),"Wings do not suppress real lava detection or grant fire resistance");p.stopFallFlying();h.assertTrue(p.tryToStartFallFlying()&&p.isFallFlying(),"Devil Wings can start gliding while submerged in lava");
            try{var method=LivingEntity.class.getDeclaredMethod("shouldTravelInFluid",net.minecraft.world.level.material.FluidState.class);method.setAccessible(true);h.assertTrue(!(boolean)method.invoke(p,h.getLevel().getFluidState(p.blockPosition())),"Native movement dispatch selects air/gliding physics instead of lava drag");}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
            p.damageCooldownTime=0;p.setHealth(20);p.hurtServer(h.getLevel(),h.getLevel().damageSources().lava(),4);h.assertTrue(p.getHealth()<20,"Unprotected wearer still takes lava damage");h.succeed();}});
    }
    static void creativeBoss(GameTestHelper h){var f=DefenseTests.player(h,Items.END_CRYSTAL);var p=f.player();p.setGameMode(GameType.CREATIVE);h.setBlock(6,2,6,FinaleContent.ALTAR.get());var altar=h.getBlockEntity(new BlockPos(6,2,6),NexusAltar.class);
        h.assertTrue(p.isCreative()&&p.level()==h.getLevel()&&p.distanceToSqr(Vec3.atCenterOf(altar.getBlockPos()))<=64,"Creative fixture remains next to its altar: creative="+p.isCreative()+", distance="+p.distanceToSqr(Vec3.atCenterOf(altar.getBlockPos())));
        for(int x=5;x<=7;x++)for(int y=2;y<=5;y++)for(int z=11;z<=13;z++)h.setBlock(x,y,z,Blocks.AIR);
        altar.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(altar.phase()==2&&altar.mask()==0,"Creative summons instantly in Overworld without Sigils, trials or empty hand");var id=altar.boss();var mob=h.getLevel().getEntity(id);h.assertTrue(mob instanceof AshboundRegent b&&b.isPractice()&&b.getMainHandItem().isEmpty()&&b.getOffhandItem().isEmpty(),"Test Regent spawns with no held crystal or staff");altar.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(altar.boss().equals(id),"Repeated clicks cannot duplicate an active boss");
        ((LivingEntity)mob).hurtServer(h.getLevel(),h.getLevel().damageSources().genericKill(),10000);altar.tickServer();h.assertTrue(!FinalProgress.data(p).won&&altar.phase()==0&&altar.mask()==0,"Creative practice does not grant progression or permanently unlock Sigils");
        altar.interact(p,InteractionHand.MAIN_HAND);h.assertTrue(altar.phase()==2&&!altar.boss().equals(id),"Creative can start another practice fight");
        var regent=(AshboundRegent)h.getLevel().getEntity(altar.boss());regent.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.END_CRYSTAL));regent.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.STICK));
        var out=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess());h.assertTrue(regent.save(out),"Regent saves normally");
        var loaded=EntityType.create(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),out.buildResult()),h.getLevel(),new EntitySpawnRequest(EntitySpawnReason.LOAD,false)).orElseThrow();
        h.assertTrue(loaded instanceof AshboundRegent r&&r.isPractice()&&r.getMainHandItem().isEmpty()&&r.getOffhandItem().isEmpty(),"Reload also removes legacy held equipment and retains Creative practice");loaded.discard();altar.reset();f.close();h.succeed();
    }
    static void recipes(GameTestHelper h){var wool=new ItemStack(Items.WOOL.white());var leather=new ItemStack(Items.LEATHER);var stick=new ItemStack(Items.STICK);var grid=CraftingInput.of(3,3,List.of(ItemStack.EMPTY,stick,ItemStack.EMPTY,stick,wool,stick,new ItemStack(Items.WOOL.red()),leather,new ItemStack(Items.WOOL.blue())));var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel());h.assertTrue(recipe.isPresent()&&recipe.get().value().assemble(grid).is(BackEquipment.GLIDER),"Three sticks, three mixed-color wool and one leather craft the cloth glider");
        var soul=new ItemStack(KatanaContent.SOUL_CORE.get());var blood=new ItemStack(BloodNetheriteContent.INGOT.get());grid=CraftingInput.of(3,3,List.of(soul,new ItemStack(DevilContent.EYE.get()),soul,blood,new ItemStack(Items.ELYTRA),blood,ItemStack.EMPTY,new ItemStack(DevilContent.TEAR.get()),ItemStack.EMPTY));recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,grid,h.getLevel());h.assertTrue(recipe.isPresent()&&recipe.get().value().assemble(grid).is(BackEquipment.WINGS),"End-game wings retain an Elytra and Blood Netherite cost");h.succeed();}
}


