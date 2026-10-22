package at.janic.thedarkdimensions;
import java.util.*;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.neoforged.neoforge.registries.*;

public final class ElementalStoneContent {
    public static final Map<String,Map<String,DeferredBlock<DropExperienceBlock>>> ORES=new LinkedHashMap<>();
    static {
        for(String theme:new String[]{"fire","water","glowing"}){
            Map<String,DeferredBlock<DropExperienceBlock>> ores=new LinkedHashMap<>();
            for(String kind:new String[]{"coal","copper","iron","gold","redstone","lapis","diamond","emerald"}){
                int min=switch(kind){case "redstone","lapis" -> 1;case "diamond","emerald" -> 3;default -> 0;};
                int max=switch(kind){case "coal" -> 2;case "redstone" -> 5;case "lapis","diamond","emerald" -> 7;default -> 0;};
                int light=switch(theme){case "fire" -> 2;case "glowing" -> 5;default -> 0;};
                var ore=TheDarkDimensions.BLOCKS.registerBlock(theme+"_cave_stone_"+kind+"_ore",p -> new DropExperienceBlock(UniformInt.of(min,max),p),() -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(5.5f,9).requiresCorrectToolForDrops().lightLevel(s -> light));
                TheDarkDimensions.ITEMS.registerSimpleBlockItem(ore);ores.put(kind,ore);
            }
            ORES.put(theme,ores);
        }
    }
    public static void init(){}
    public static Block themedOre(String theme,BlockState state){
        for(var e:RevisionContent.ORES.entrySet()){
            String kind=e.getKey();
            var normal=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace(kind+"_ore"));
            var deep=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("deepslate_"+kind+"_ore"));
            if(state.is(e.getValue().get())||state.is(normal)||state.is(deep))return ORES.get(theme).get(kind).get();
        }
        return null;
    }
}
