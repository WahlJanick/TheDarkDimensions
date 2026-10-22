package at.janic.thedarkdimensions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** A normal, visible shapeless recipe whose igniter is returned with three points of wear. */
public final class BurningStickRecipe extends ShapelessRecipe {
    public static final int IGNITER_WEAR=3;
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,TheDarkDimensions.MOD_ID);
    private final Recipe.CommonInfo info;
    private final CraftingRecipe.CraftingBookInfo book;
    private final ItemStackTemplate output;
    private final List<Ingredient> inputs;
    private static final MapCodec<BurningStickRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Recipe.CommonInfo.MAP_CODEC.forGetter(r->r.info),
        CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r->r.book),
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(r->r.output),
        Ingredient.CODEC.listOf(1,9).fieldOf("ingredients").forGetter(r->r.inputs)
    ).apply(i,BurningStickRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,BurningStickRecipe> STREAM=StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC,r->r.info,CraftingRecipe.CraftingBookInfo.STREAM_CODEC,r->r.book,
        ItemStackTemplate.STREAM_CODEC,r->r.output,Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),r->r.inputs,BurningStickRecipe::new);
    private static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ShapelessRecipe>> TYPE=SERIALIZERS.register("burning_stick",()->new RecipeSerializer<>(
        CODEC.<ShapelessRecipe>xmap(r->r,r->(BurningStickRecipe)r),STREAM.<ShapelessRecipe>map(r->r,r->(BurningStickRecipe)r)));

    public BurningStickRecipe(Recipe.CommonInfo info,CraftingRecipe.CraftingBookInfo book,ItemStackTemplate output,List<Ingredient> inputs){
        super(info,book,output,inputs);this.info=info;this.book=book;this.output=output;this.inputs=inputs;
    }
    @Override public RecipeSerializer<ShapelessRecipe> getSerializer(){return TYPE.get();}
    @Override public NonNullList<ItemStack> getRemainingItems(CraftingInput input){
        var result=super.getRemainingItems(input);
        for(int i=0;i<input.size();i++){
            var original=input.getItem(i);
            if(!original.is(Items.FLINT_AND_STEEL))continue;
            var igniter=original.copyWithCount(1);
            if(igniter.isDamageableItem()){
                int damage=igniter.getDamageValue()+IGNITER_WEAR;
                if(damage>=igniter.getMaxDamage()){result.set(i,ItemStack.EMPTY);continue;}
                igniter.setDamageValue(damage);
            }
            result.set(i,igniter);
        }
        return result;
    }
    public static void init(IEventBus bus){SERIALIZERS.register(bus);}
}
