package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;

final class PerkTests {
    private static ServerPlayer player(GameTestHelper h){var p=(ServerPlayer)h.makeMockServerPlayer(net.minecraft.world.level.GameType.SURVIVAL);var c=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(c);h.getLevel().getServer().getPlayerList().placeNewPlayer(c,p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));var v=h.absoluteVec(new Vec3(4.5,2,4.5));p.teleportTo(v.x,v.y,v.z);return p;}
    private static ItemStack craft(GameTestHelper h,int w,int ht,List<ItemStack> stacks){var in=CraftingInput.of(w,ht,stacks);var recipe=h.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,in,h.getLevel());h.assertTrue(recipe.isPresent(),"Recipe exists");return recipe.get().value().assemble(in);}
    static void recipes(GameTestHelper h){
        h.assertTrue(craft(h,3,1,List.of(new ItemStack(CaveContent.GLOWING_HONEY.get()),new ItemStack(MineralContent.ESSENTIALIUM.get()),new ItemStack(Items.AMETHYST_SHARD))).is(PerkContent.LUMINOUS_COATING),"Honey and essentialium craft coating");
        var e=new ItemStack(MineralContent.ESSENTIALIUM.get());var m=new ItemStack(SpellContent.METALINIUM_INGOT.get());var a=new ItemStack(DarkContent.IMPROVED_AMETHYST.get());var empty=ItemStack.EMPTY;
        h.assertTrue(craft(h,3,3,List.of(empty,e,empty,m,a,m,empty,e,empty)).is(PerkContent.ASTRAL_PRISM),"Prism uses all three underused materials");
        var j=new ItemStack(KatanaContent.JAPANESE_ESSENCE.get());var string=new ItemStack(Items.STRING);
        h.assertTrue(craft(h,3,3,List.of(empty,string,empty,j,new ItemStack(Items.CHERRY_SAPLING),j,empty,string,empty)).is(PerkContent.SAKURA_TALISMAN),"Japanese essence crafts talisman");
        var bandage=craft(h,3,1,List.of(j,new ItemStack(Items.PAPER),string));h.assertTrue(bandage.is(PerkContent.RONIN_BANDAGE)&&bandage.getCount()==4,"Essence crafts four bandages");
        var leather=new ItemStack(Items.LEATHER);var stick=new ItemStack(TheDarkDimensions.IMPROVED_STICK.get());
        h.assertTrue(craft(h,3,3,List.of(j,empty,j,leather,empty,leather,stick,empty,stick)).is(PerkContent.WINDSTEP_SANDALS),"Essence crafts windstep sandals");h.succeed();
    }
    static void weapons(GameTestHelper h){
        int checked=0;for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){var original=new ItemStack(item);if(!PerkContent.isWeapon(original))continue;checked++;
            original.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("My weapon"));if(original.isDamageableItem())original.setDamageValue(7);
            original.enchant(h.getLevel().registryAccess().getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),2);
            net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,original,t->t.putInt("other_mod_data",42));
            var coated=craft(h,2,1,List.of(original,new ItemStack(PerkContent.LUMINOUS_COATING.get())));
            h.assertTrue(coated.is(item)&&PerkContent.coated(coated)&&!PerkContent.coated(original),"Coating copies weapon rather than mutating ingredient");
            h.assertTrue(coated.get(DataComponents.CUSTOM_NAME).equals(original.get(DataComponents.CUSTOM_NAME))&&coated.getDamageValue()==original.getDamageValue()&&coated.get(DataComponents.ENCHANTMENTS).equals(original.get(DataComponents.ENCHANTMENTS)),"Name, damage and enchantments preserved");
            h.assertTrue(coated.get(DataComponents.CUSTOM_DATA).copyTag().getIntOr("other_mod_data",0)==42,"Foreign custom data preserved");
            var tooltip=coated.getTooltipLines(Item.TooltipContext.of(h.getLevel()),null,TooltipFlag.NORMAL);
            int perk=-1,enchant=-1;for(int line=0;line<tooltip.size();line++)if(tooltip.get(line).getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t){if(t.getKey().equals("perk.thedarkdimensions.luminous_coating"))perk=line;if(t.getKey().equals("enchantment.minecraft.unbreaking"))enchant=line;}
            h.assertTrue(perk>0&&enchant>=0&&perk>enchant,"Perk appears below enchantments for every weapon");
            var bad=CraftingInput.of(2,1,List.of(coated,new ItemStack(PerkContent.LUMINOUS_COATING.get())));h.assertTrue(!LuminousCoatingRecipe.INSTANCE.matches(bad,h.getLevel()),"Duplicate coating rejected");
        }h.assertTrue(checked>40,"Covers vanilla and all registered mod weapons");
        for(var item:List.of(Items.BOW,Items.CROSSBOW,Items.TRIDENT,Items.MACE,Items.DIAMOND_SPEAR,ArmoryContent.THROWABLE_AXE.get()))h.assertTrue(PerkContent.isWeapon(new ItemStack(item)),"Ranged and unusual weapons supported");
        var in=CraftingInput.of(2,1,List.of(new ItemStack(Items.DIRT),new ItemStack(PerkContent.LUMINOUS_COATING.get())));h.assertTrue(!LuminousCoatingRecipe.INSTANCE.matches(in,h.getLevel()),"Non-weapons rejected");h.succeed();
    }
    static void hits(GameTestHelper h){
        var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var sword=new ItemStack(Items.DIAMOND_SWORD);PerkContent.coat(sword);p.setItemInHand(InteractionHand.MAIN_HAND,sword);
        var zombie=h.spawn(EntityTypes.ZOMBIE,new BlockPos(4,2,4));zombie.setNoAi(true);
        zombie.hurtServer(h.getLevel(),h.getLevel().damageSources().playerAttack(p),2);h.assertTrue(zombie.hasEffect(MobEffects.GLOWING)&&zombie.getEffect(MobEffects.GLOWING).getDuration()==160,"Melee applies eight-second glow");
        zombie.removeEffect(MobEffects.GLOWING);zombie.damageCooldownTime=0;zombie.hurtServer(h.getLevel(),h.getLevel().damageSources().indirectMagic(p,p),2);h.assertTrue(!zombie.hasEffect(MobEffects.GLOWING),"Holding coated sword does not coat spell damage");
        var bow=new ItemStack(Items.BOW);PerkContent.coat(bow);var arrow=new Arrow(h.getLevel(),p,new ItemStack(Items.ARROW),bow);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        zombie.damageCooldownTime=0;zombie.hurtServer(h.getLevel(),h.getLevel().damageSources().arrow(arrow,p),2);h.assertTrue(zombie.hasEffect(MobEffects.GLOWING),"Projectile uses original bow even after weapon switch");
        zombie.removeEffect(MobEffects.GLOWING);var trident=new ItemStack(Items.TRIDENT);PerkContent.coat(trident);var thrown=new ThrownTrident(h.getLevel(),p,trident);zombie.damageCooldownTime=0;zombie.hurtServer(h.getLevel(),h.getLevel().damageSources().trident(thrown,p),2);h.assertTrue(zombie.hasEffect(MobEffects.GLOWING),"Thrown trident coating works");
        zombie.removeEffect(MobEffects.GLOWING);var axe=new ItemStack(ArmoryContent.THROWABLE_AXE.get());PerkContent.coat(axe);var thrownAxe=new ThrownAxe(h.getLevel(),p,axe);zombie.damageCooldownTime=0;zombie.hurtServer(h.getLevel(),h.getLevel().damageSources().trident(thrownAxe,p),2);h.assertTrue(zombie.hasEffect(MobEffects.GLOWING),"Recoverable thrown axe coating works");h.succeed();
    }
    static void bandage(GameTestHelper h){var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);p.setNoGravity(true);p.setHealth(10);p.getFoodData().setFoodLevel(20);p.addEffect(new MobEffectInstance(KatanaEffects.FROSTED,100,0));p.addEffect(new MobEffectInstance(KatanaEffects.BLOOD,100,0));
        var bandage=new ItemStack(PerkContent.RONIN_BANDAGE.get(),2);bandage.finishUsingItem(h.getLevel(),p);
        h.assertTrue(bandage.getCount()==1&&!p.hasEffect(KatanaEffects.FROSTED)&&p.hasEffect(KatanaEffects.BLOOD),"Consumes one bandage and only clears Frosted");var regen=p.getEffect(MobEffects.REGENERATION);h.assertTrue(regen!=null&&regen.getDuration()==100&&regen.getAmplifier()==1,"Five seconds of Regeneration II, usable at full hunger");
        p.removeEffect(KatanaEffects.BLOOD);for(int i=0;i<100;i++)regen.tickServer(h.getLevel(),p,()->{});h.assertTrue(p.getHealth()==14,"Regeneration heals exactly two hearts");h.succeed();}
    static void accessories(GameTestHelper h){
        var p=player(h);try{h.assertTrue(PerkContent.cooldown(p,100)==100,"No focus means normal cooldown");p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PerkContent.ASTRAL_PRISM.get()));h.assertTrue(PerkContent.cooldown(p,100)==95,"Offhand focus gives five percent");
            if(net.neoforged.fml.ModList.get().isLoaded("curios")){var inv=top.theillusivec4.curios.api.CuriosApi.getCuriosInventoryOrNull(p);h.assertTrue(inv!=null&&inv.getStacksHandler("focus").isPresent()&&inv.getStacksHandler("talisman").isPresent(),"Both Curios slots registered");inv.getStacksHandler("focus").get().getStacks().setStackInSlot(0,new ItemStack(PerkContent.ASTRAL_PRISM.get()));h.assertTrue(PerkContent.cooldown(p,100)==95,"Equipped and offhand focus cannot stack");p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);h.assertTrue(PerkContent.cooldown(p,100)==95,"Curios focus works with empty hands");inv.getStacksHandler("talisman").get().getStacks().setStackInSlot(0,new ItemStack(PerkContent.SAKURA_TALISMAN.get()));h.assertTrue(PerkContent.comboWindow(p)==60,"Curios talisman extends combo window");}
        }finally{h.getLevel().getServer().getPlayerList().remove(p);}h.succeed();
    }
    static void combo(GameTestHelper h){var p=h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);var b=new ItemStack(KatanaContent.FROSTGLEAM.get());p.setItemInHand(InteractionHand.MAIN_HAND,b);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(PerkContent.SAKURA_TALISMAN.get()));var target=h.spawn(EntityTypes.ZOMBIE,new BlockPos(4,2,4));target.setNoAi(true);h.assertTrue(!KatanaCombos.hit(b,p,target),"First strike");h.assertTrue(PerkContent.comboWindow(p)==60,"Talisman active initially");h.runAfterDelay(50,()->{h.assertTrue(PerkContent.comboWindow(p)==60,"Talisman still equipped at second strike");h.assertTrue(!KatanaCombos.hit(b,p,target),"Second strike within extended window");});h.runAfterDelay(52,()->{h.assertTrue(KatanaCombos.hit(b,p,target),"Third strike completes combo beyond normal two-second window");h.succeed();});}
    static void sandals(GameTestHelper h){var p=player(h);try{
        h.assertTrue(!PerkContent.dash(p),"No sandals means no dash");p.setItemSlot(EquipmentSlot.FEET,new ItemStack(PerkContent.WINDSTEP_SANDALS.get()));p.setOnGround(true);p.setYRot(0);p.setXRot(0);var start=p.position();for(int y=2;y<5;y++)h.setBlock(4,y,6,net.minecraft.world.level.block.Blocks.STONE);
        h.assertTrue(PerkContent.dash(p)&&p.position().distanceTo(start)>.4&&p.getZ()<h.absolutePos(new BlockPos(4,2,6)).getZ(),"Sandals dash stops before wall");h.assertTrue(!PerkContent.dash(p)&&p.getItemBySlot(EquipmentSlot.FEET).getDamageValue()==1,"Five-second server cooldown and durability cost");
    }finally{h.getLevel().getServer().getPlayerList().remove(p);}h.succeed();}
}
