package at.janic.thedarkdimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class SpellContent {
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,TheDarkDimensions.MOD_ID);
    public static final DeferredHolder<MenuType<?>,MenuType<SpelltableMenu>> SPELLTABLE_MENU=MENUS.register("spelltable",() -> new MenuType<>(SpelltableMenu::new,FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredItem<Item> SPELL_BOOK=TheDarkDimensions.ITEMS.registerSimpleItem("spell_book",p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> METALINIUM_NUGGET=TheDarkDimensions.ITEMS.registerSimpleItem("metalinium_nugget");
    public static final DeferredItem<Item> METALINIUM_INGOT=TheDarkDimensions.ITEMS.registerSimpleItem("metalinium_ingot");
    // The displayed name is Chronus Infused Metalinium Ingot; retain this saved ID for existing worlds.
    public static final DeferredItem<Item> INFUSED_INGOT=TheDarkDimensions.ITEMS.registerSimpleItem("essentialium_infused_metalinium_ingot",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredBlock<Block> METALINIUM_ORE=TheDarkDimensions.BLOCKS.registerSimpleBlock("metalinium_ore",() -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));
    public static final DeferredBlock<Block> DEEPSLATE_METALINIUM_ORE=TheDarkDimensions.BLOCKS.registerSimpleBlock("deepslate_metalinium_ore",() -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));
    public static final DeferredItem<SpellBookItem> FIRE_BOOK=book("fire_spell_book",SpellBookItem.Spell.FIRE);
    public static final DeferredItem<SpellBookItem> LIGHTNING_BOOK=book("lightning_spell_book",SpellBookItem.Spell.LIGHTNING);
    public static final DeferredItem<SpellBookItem> ENDER_CHEST_BOOK=book("ender_chest_spell_book",SpellBookItem.Spell.ENDER_CHEST);
    public static final DeferredItem<SpellBookItem> WATER_BOOK=book("water_spell_book",SpellBookItem.Spell.WATER);
    static {
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(METALINIUM_ORE);
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(DEEPSLATE_METALINIUM_ORE);
    }
    private static DeferredItem<SpellBookItem> book(String id,SpellBookItem.Spell spell) {
        return TheDarkDimensions.ITEMS.registerItem(id,p -> new SpellBookItem(p,spell),p -> p.stacksTo(1).rarity(Rarity.RARE));
    }
    public static Item bookFor(int index) {
        return switch(index) {case 0 -> FIRE_BOOK.get();case 1 -> LIGHTNING_BOOK.get();case 2 -> ENDER_CHEST_BOOK.get();case 3 -> WATER_BOOK.get();default -> throw new IllegalArgumentException("Unknown spell");};
    }
    public static void init(IEventBus bus) { MENUS.register(bus); }
    private SpellContent() {}
}
