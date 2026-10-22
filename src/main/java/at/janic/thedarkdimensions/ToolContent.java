package at.janic.thedarkdimensions;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ToolContent {
    public static final TagKey<Item> IMPROVED_TOOLS = TagKey.create(Registries.ITEM, TheDarkDimensions.id("improved_tools"));
    public static final DeferredItem<SmithingTemplateItem> IMPROVING_TEMPLATE = TheDarkDimensions.ITEMS.registerItem("improving_template", p -> new SmithingTemplateItem(
            Component.translatable("template.thedarkdimensions.applies_to"), Component.translatable("template.thedarkdimensions.ingredients"),
            Component.translatable("template.thedarkdimensions.base_slot"), Component.translatable("template.thedarkdimensions.addition_slot"),
            List.of(net.minecraft.resources.Identifier.withDefaultNamespace("container/slot/pickaxe"),net.minecraft.resources.Identifier.withDefaultNamespace("container/slot/spear")),
            List.of(TheDarkDimensions.id("container/slot/improved_stick")),p));
    public static final DeferredItem<Item> PERFECTED_NETHERITE_SWORD = netherite("sword",p -> p.sword(ToolMaterial.NETHERITE,3,-2.4f));
    public static final DeferredItem<Item> PERFECTED_NETHERITE_PICKAXE = netherite("pickaxe",p -> p.pickaxe(ToolMaterial.NETHERITE,1,-2.8f));
    public static final DeferredItem<Item> PERFECTED_NETHERITE_AXE = netherite("axe",p -> p.axe(ToolMaterial.NETHERITE,5,-3f));
    public static final DeferredItem<Item> PERFECTED_NETHERITE_SHOVEL = netherite("shovel",p -> p.shovel(ToolMaterial.NETHERITE,1.5f,-3f));
    public static final DeferredItem<Item> PERFECTED_NETHERITE_HOE = netherite("hoe",p -> p.hoe(ToolMaterial.NETHERITE,-4,0));
    public static final DeferredItem<Item> IMPROVED_DIAMOND_SPEAR = TheDarkDimensions.ITEMS.registerSimpleItem("improved_diamond_spear", p -> spear(p, false));
    public static final DeferredItem<Item> PERFECTED_NETHERITE_SPEAR = netherite("spear",p -> spear(p,true));
    public static final DeferredItem<Item> DOUBLE_SPEAR = TheDarkDimensions.ITEMS.registerSimpleItem("double_spear",p -> spear(p,true,1.5f)
            .enchantable(25).fireResistant()
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,7,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,1.0/1.15-4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build()));
    private static Item.Properties spear(Item.Properties p, boolean netherite) {
        return spear(p,netherite,1.2f);
    }
    private static Item.Properties spear(Item.Properties p, boolean netherite, float kineticDamage) {
        if(netherite) p.spear(ToolMaterial.NETHERITE,1.15f,kineticDamage,0.4f,2.5f,9,5.5f,5.1f,8.75f,4.6f);
        else p.spear(ToolMaterial.DIAMOND,1.05f,1.075f,0.5f,3,10,6.5f,5.1f,10,4.6f);
        // Spears use their own attack-range component, independent of the interaction attribute.
        return p.component(DataComponents.ATTACK_RANGE,new AttackRange(2,6.5f,2,8.5f,0.125f,0.5f));
    }
    private static DeferredItem<Item> netherite(String kind,java.util.function.UnaryOperator<Item.Properties> properties) {
        return TheDarkDimensions.ITEMS.registerSimpleItem("perfected_netherite_"+kind,p -> properties.apply(p).fireResistant());
    }
    public static void init() {}
    private ToolContent() {}
}
