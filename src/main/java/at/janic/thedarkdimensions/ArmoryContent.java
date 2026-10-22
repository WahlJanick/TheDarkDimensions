package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;

public final class ArmoryContent {
    public record Tier(String name,ToolMaterial material,float damage,float speed) {}
    public static final List<Tier> TIERS=List.of(new Tier("wooden",ToolMaterial.WOOD,6,-3.2f),new Tier("stone",ToolMaterial.STONE,7,-3.2f),new Tier("copper",ToolMaterial.COPPER,7,-3.2f),new Tier("iron",ToolMaterial.IRON,6,-3.1f),new Tier("golden",ToolMaterial.GOLD,6,-3),new Tier("diamond",ToolMaterial.DIAMOND,5,-3),new Tier("netherite",ToolMaterial.NETHERITE,5,-3));
    public static final Map<String,DeferredItem<Item>> FISH_AXES=new LinkedHashMap<>();
    public static final Map<String,DeferredItem<Item>> WARRIOR_AXES=new LinkedHashMap<>();
    public static final DeferredItem<Item> WARRIOR_STICK=TheDarkDimensions.ITEMS.registerSimpleItem("warrior_stick");
    public static final DeferredItem<SmithingTemplateItem> WARRIOR_TEMPLATE=TheDarkDimensions.ITEMS.registerItem("warrior_axe_upgrade_template",p -> new SmithingTemplateItem(
        net.minecraft.network.chat.Component.translatable("template.thedarkdimensions.warrior.applies_to"),
        net.minecraft.network.chat.Component.translatable("template.thedarkdimensions.warrior.ingredients"),
        net.minecraft.network.chat.Component.translatable("template.thedarkdimensions.warrior.base_slot"),
        net.minecraft.network.chat.Component.translatable("template.thedarkdimensions.warrior.addition_slot"),
        List.of(net.minecraft.resources.Identifier.withDefaultNamespace("container/slot/axe")),
        List.of(TheDarkDimensions.id("container/slot/improved_stick")),p));
    public static final DeferredItem<ThrowableAxeItem> THROWABLE_AXE=TheDarkDimensions.ITEMS.registerItem("throwable_axe",ThrowableAxeItem::new,p -> p.axe(ToolMaterial.IRON,6,-3.1f));
    public static final DeferredItem<ThunderHammerItem> HAMMER=hammer("thundergods_hammer",false);
    public static final DeferredItem<ThunderHammerItem> AWAKENED_HAMMER=hammer("awakened_thundergods_hammer",true);
    public static final DeferredHolder<EntityType<?>,EntityType<ThrownAxe>> THROWN_AXE=TheDarkDimensions.ENTITIES.register("thrown_axe",() -> EntityType.Builder.<ThrownAxe>of(ThrownAxe::new,MobCategory.MISC).sized(0.5f,0.5f).clientTrackingRange(4).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("thrown_axe"))));
    static {
        for(var tier:TIERS) {
            FISH_AXES.put(tier.name(),TheDarkDimensions.ITEMS.registerSimpleItem(tier.name()+"_fish_axe",p -> properties(p,tier,false)));
            WARRIOR_AXES.put(tier.name(),TheDarkDimensions.ITEMS.registerSimpleItem(tier.name()+"_warrior_axe",p -> properties(p,tier,true)));
        }
    }
    private static Item.Properties properties(Item.Properties p,Tier tier,boolean warrior) {
        var m=tier.material();
        if(warrior)m=new ToolMaterial(m.incorrectBlocksForDrops(),m.durability(),m.speed()*1.15f,m.attackDamageBonus(),m.enchantmentValue(),m.repairItems());
        p.axe(m,tier.damage()+(warrior?2:0),tier.speed());
        if(tier.name().equals("netherite"))p.fireResistant();
        return p;
    }
    private static DeferredItem<ThunderHammerItem> hammer(String name,boolean awakened) {
        return TheDarkDimensions.ITEMS.registerItem(name,p -> new ThunderHammerItem(p,awakened),p -> p.sword(ToolMaterial.IRON,7,-3).durability(1600).repairable(Items.IRON_INGOT).rarity(awakened?Rarity.EPIC:Rarity.RARE));
    }
    public static boolean isHammer(ItemStack stack) { return stack.is(HAMMER)||stack.is(AWAKENED_HAMMER); }
    public static boolean canGlide(ItemStack stack) { return stack.is(AWAKENED_HAMMER)&&!stack.nextDamageWillBreak(); }
    public static void init() {}
    private ArmoryContent() {}
}
