package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.*;

public final class BloodNetheriteContent {
    public static final TagKey<Item> REPAIR = TagKey.create(Registries.ITEM,TheDarkDimensions.id("repairs_red_netherite"));
    public static final ToolMaterial MATERIAL = new ToolMaterial(ToolMaterial.NETHERITE.incorrectBlocksForDrops(),ToolMaterial.NETHERITE.durability(),ToolMaterial.NETHERITE.speed()+1,ToolMaterial.NETHERITE.attackDamageBonus()+1,ToolMaterial.NETHERITE.enchantmentValue(),REPAIR);
    public static final ArmorMaterial ARMOR = new ArmorMaterial(37,Map.of(ArmorType.HELMET,5,ArmorType.CHESTPLATE,10,ArmorType.LEGGINGS,8,ArmorType.BOOTS,5),15,ArmorMaterials.NETHERITE.equipSound(),3,0.15f,REPAIR,ResourceKey.create(EquipmentAssets.ROOT_ID,TheDarkDimensions.id("red_netherite")));
    public static final DeferredItem<Item> INGOT = TheDarkDimensions.ITEMS.registerSimpleItem("red_netherite_ingot",p -> p.fireResistant());
    public static final DeferredItem<SmithingTemplateItem> TEMPLATE = TheDarkDimensions.ITEMS.registerItem("red_netherite_upgrade_smithing_template",p -> new SmithingTemplateItem(
        Component.translatable("template.thedarkdimensions.blood.applies_to"),Component.translatable("template.thedarkdimensions.blood.ingredients"),
        Component.translatable("template.thedarkdimensions.blood.base_slot"),Component.translatable("template.thedarkdimensions.blood.addition_slot"),
        List.of(Identifier.withDefaultNamespace("container/slot/chestplate"),Identifier.withDefaultNamespace("container/slot/pickaxe")),List.of(Identifier.withDefaultNamespace("container/slot/ingot")),p));
    public static final Map<String,DeferredItem<Item>> TOOLS = new LinkedHashMap<>();
    public static final Map<String,DeferredItem<Item>> IMPROVED_TOOLS = new LinkedHashMap<>();
    public static final Map<ArmorType,DeferredItem<Item>> ARMOR_ITEMS = new LinkedHashMap<>();
    public static final DeferredBlock<Block> BLOCK = TheDarkDimensions.BLOCKS.registerSimpleBlock("red_netherite_block",() -> BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK));
    static {
        for(String kind:List.of("sword","pickaxe","axe","shovel","hoe")) {
            TOOLS.put(kind,tool(kind,false));IMPROVED_TOOLS.put(kind,tool(kind,true));
        }
        TOOLS.put("warrior_axe",tool("warrior_axe",false));
        for(var type:List.of(ArmorType.HELMET,ArmorType.CHESTPLATE,ArmorType.LEGGINGS,ArmorType.BOOTS))
            ARMOR_ITEMS.put(type,TheDarkDimensions.ITEMS.registerSimpleItem("red_netherite_"+type.getName(),p -> p.humanoidArmor(ARMOR,type).fireResistant()));
        TheDarkDimensions.ITEMS.registerSimpleBlockItem(BLOCK,p -> p.fireResistant());
    }
    private static DeferredItem<Item> tool(String kind,boolean improved) {
        return TheDarkDimensions.ITEMS.registerSimpleItem("red_netherite_"+(improved?"improved_":"")+kind,p -> {
            var m=MATERIAL;
            if(kind.equals("warrior_axe"))m=new ToolMaterial(m.incorrectBlocksForDrops(),m.durability(),m.speed()*1.15f,m.attackDamageBonus(),m.enchantmentValue(),m.repairItems());
            switch(kind) {
                case "sword" -> p.sword(m,3,-2.4f);
                case "pickaxe" -> p.pickaxe(m,1,-2.8f);
                case "axe" -> p.axe(m,5,-3);
                case "warrior_axe" -> p.axe(m,7,-3);
                case "shovel" -> p.shovel(m,1.5f,-3);
                case "hoe" -> p.hoe(m,-4,0);
                default -> throw new IllegalArgumentException(kind);
            }
            return p.fireResistant();
        });
    }
    public static void init() {}
    private BloodNetheriteContent() {}
}
