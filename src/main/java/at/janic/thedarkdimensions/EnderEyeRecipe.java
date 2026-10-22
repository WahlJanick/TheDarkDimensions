package at.janic.thedarkdimensions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.*;

/** Native visible recipe whose world progression is also enforced for automated crafting. */
public final class EnderEyeRecipe extends ShapelessRecipe {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,TheDarkDimensions.MOD_ID);
    private final Recipe.CommonInfo info;private final CraftingRecipe.CraftingBookInfo book;private final ItemStackTemplate output;private final List<Ingredient> inputs;
    private static final MapCodec<EnderEyeRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Recipe.CommonInfo.MAP_CODEC.forGetter(r->r.info),CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(r->r.book),ItemStackTemplate.CODEC.fieldOf("result").forGetter(r->r.output),Ingredient.CODEC.listOf(1,9).fieldOf("ingredients").forGetter(r->r.inputs)).apply(i,EnderEyeRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,EnderEyeRecipe> STREAM=StreamCodec.composite(Recipe.CommonInfo.STREAM_CODEC,r->r.info,CraftingRecipe.CraftingBookInfo.STREAM_CODEC,r->r.book,ItemStackTemplate.STREAM_CODEC,r->r.output,Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),r->r.inputs,EnderEyeRecipe::new);
    private static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ShapelessRecipe>> TYPE=SERIALIZERS.register("resonant_ender_eye",()->new RecipeSerializer<>(CODEC.<ShapelessRecipe>xmap(r->r,r->(EnderEyeRecipe)r),STREAM.<ShapelessRecipe>map(r->r,r->(EnderEyeRecipe)r)));
    public EnderEyeRecipe(Recipe.CommonInfo info,CraftingRecipe.CraftingBookInfo book,ItemStackTemplate output,List<Ingredient> inputs){super(info,book,output,inputs);this.info=info;this.book=book;this.output=output;this.inputs=inputs;}
    @Override public boolean matches(CraftingInput input,Level level){return super.matches(input,level)&&(!(level instanceof ServerLevel server)||EndProgression.unlocked(server));}
    @Override public RecipeSerializer<ShapelessRecipe> getSerializer(){return TYPE.get();}
    public static void init(net.neoforged.bus.api.IEventBus bus){SERIALIZERS.register(bus);}
}
