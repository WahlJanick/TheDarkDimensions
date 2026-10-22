package at.janic.thedarkdimensions;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;

public final class KatanaContent {
    public static final DeferredItem<Item> JAPANESE_ESSENCE=TheDarkDimensions.ITEMS.registerSimpleItem("japanese_essence");
    public static final DeferredItem<Item> CREEPERBLOOD=TheDarkDimensions.ITEMS.registerSimpleItem("creeperblood");
    public static final DeferredItem<Item> ZOMBIEBLOOD=TheDarkDimensions.ITEMS.registerSimpleItem("zombieblood");
    public static final DeferredItem<Item> BLOOD_ESSENCE=TheDarkDimensions.ITEMS.registerSimpleItem("blood_essence");
    public static final DeferredItem<Item> BLOOD_CORE=TheDarkDimensions.ITEMS.registerSimpleItem("blood_core",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> SOUL_ESSENCE=TheDarkDimensions.ITEMS.registerSimpleItem("soul_essence");
    public static final DeferredItem<Item> SOUL_CORE=TheDarkDimensions.ITEMS.registerSimpleItem("soul_core",p -> p.rarity(Rarity.UNCOMMON));
    public static final DeferredItem<KatanaItem> FROSTGLEAM=TheDarkDimensions.ITEMS.registerItem("frostgleam",p -> new KatanaItem(p,KatanaEffects.FROSTED,80),p -> p.sword(ToolMaterial.DIAMOND,2,-2.5f).durability(750).enchantable(15).repairable(JAPANESE_ESSENCE.get()).rarity(Rarity.UNCOMMON));
    public static final DeferredItem<KatanaItem> BLOODGRIEF=TheDarkDimensions.ITEMS.registerItem("bloodgrief",p -> new KatanaItem(p,KatanaEffects.BLOOD,100),p -> p.sword(ToolMaterial.DIAMOND,4,-2.5f).durability(1250).enchantable(18).repairable(BLOOD_CORE.get()).rarity(Rarity.RARE));
    public static final DeferredItem<KatanaItem> SOULEATER=TheDarkDimensions.ITEMS.registerItem("souleater",p -> new KatanaItem(p,KatanaEffects.SOULLESS,60),p -> p.sword(ToolMaterial.NETHERITE,6.5f,-2.4f).durability(2100).enchantable(22).repairable(SOUL_CORE.get()).fireResistant().rarity(Rarity.EPIC));
    public static void init() {}
    private KatanaContent() {}
}
