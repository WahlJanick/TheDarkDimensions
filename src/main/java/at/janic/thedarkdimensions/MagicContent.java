package at.janic.thedarkdimensions;

import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;

public final class MagicContent {
    public static final DeferredItem<Item> CHRONUS_CORE=TheDarkDimensions.ITEMS.registerSimpleItem("chronus_core",p->p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> ENDER_CORE=TheDarkDimensions.ITEMS.registerSimpleItem("ender_core",p->p.rarity(Rarity.RARE));
    public static final DeferredItem<SpellBookItem> CHRONUS_BOOK=book("chronus_spell_book",MagicElement.CHRONUS);
    public static final DeferredItem<SpellBookItem> BLOOD_BOOK=book("blood_spell_book",MagicElement.BLOOD);
    public static final DeferredItem<SpellBookItem> SOUL_BOOK=book("soul_spell_book",MagicElement.SOUL);
    public static final DeferredItem<SpellBookItem> ELEMENTAL_BOOK=book("elemental_spell_book",MagicElement.ELEMENTAL);
    public static final DeferredItem<SpellBookItem> MASTER_BOOK=book("elemental_master_spellbook",null);
    public static final DeferredHolder<MenuType<?>,MenuType<SpellbookMenu>> BOOK_MENU=SpellContent.MENUS.register("spellbook",()->new MenuType<>(SpellbookMenu::new,FeatureFlags.DEFAULT_FLAGS));
    private static DeferredItem<SpellBookItem> book(String id,MagicElement e){return TheDarkDimensions.ITEMS.registerItem(id,p->new SpellBookItem(p,e),p->p.stacksTo(1).rarity(Rarity.EPIC));}
    public static Item book(MagicElement e){return switch(e){case FIRE->SpellContent.FIRE_BOOK.get();case WATER->SpellContent.WATER_BOOK.get();case LIGHTNING->SpellContent.LIGHTNING_BOOK.get();case ENDER->SpellContent.ENDER_CHEST_BOOK.get();case CHRONUS->CHRONUS_BOOK.get();case BLOOD->BLOOD_BOOK.get();case SOUL->SOUL_BOOK.get();case ELEMENTAL->ELEMENTAL_BOOK.get();};}
    public static void init(IEventBus bus){
        MagicExpansion.init();
        MagicMastery.init();
        bus.addListener(MagicNetwork::register);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event)->event.enqueueWork(()->{if(net.neoforged.fml.ModList.get().isLoaded("curios"))CuriosMagic.register();}));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event)->{if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)MagicCasting.record(p);});
    }
}
