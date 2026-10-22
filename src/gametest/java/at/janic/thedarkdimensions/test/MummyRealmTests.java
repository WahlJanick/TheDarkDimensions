package at.janic.thedarkdimensions.test;

import at.janic.thedarkdimensions.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;

final class MummyRealmTests {
    static void unlitPortal(GameTestHelper h) {
        for(int x=1;x<=4;x++)for(int y=1;y<=5;y++)h.setBlock(new BlockPos(x,y,3),(x==1||x==4||y==1||y==5)?net.minecraft.world.level.block.Blocks.OBSIDIAN:net.minecraft.world.level.block.Blocks.AIR);
        var player=h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(RevisionContent.ASTRAL_CRYSTAL_WAND.get()));
        var clicked=h.absolutePos(new BlockPos(2,1,3));
        var context=new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(clicked),net.minecraft.core.Direction.UP,clicked,false));
        h.setBlock(new BlockPos(3,5,3),net.minecraft.world.level.block.Blocks.AIR);
        h.assertTrue(RevisionContent.ASTRAL_CRYSTAL_WAND.get().useOn(context)==net.minecraft.world.InteractionResult.PASS,"Broken frame remains unlit");
        h.setBlock(new BlockPos(3,5,3),net.minecraft.world.level.block.Blocks.OBSIDIAN);
        h.assertTrue(RevisionContent.ASTRAL_CRYSTAL_WAND.get().useOn(context)==net.minecraft.world.InteractionResult.SUCCESS,"Wand ignites complete unlit frame directly");
        for(int x=2;x<=3;x++)for(int y=2;y<=4;y++)h.assertBlockPresent(TheDarkDimensions.IMPROVED_PORTAL.get(),new BlockPos(x,y,3));
        h.assertTrue(player.getMainHandItem().is(RevisionContent.ASTRAL_CRYSTAL_WAND)&&player.getMainHandItem().getMaxDamage()==64,"Reusable wand remains equipped");
        h.succeed();
    }
    static void hardOres(GameTestHelper h) {
        var level=h.getLevel();var pick=new ItemStack(Items.DIAMOND_PICKAXE);
        var silk=pick.copy();silk.enchant(level.registryAccess().getOrThrow(Enchantments.SILK_TOUCH),1);
        var fortune=pick.copy();fortune.enchant(level.registryAccess().getOrThrow(Enchantments.FORTUNE),3);
        Map<String,Item> drops=Map.of("coal",Items.COAL,"copper",Items.RAW_COPPER,"iron",Items.RAW_IRON,"gold",Items.RAW_GOLD,"redstone",Items.REDSTONE,"lapis",Items.LAPIS_LAZULI,"diamond",Items.DIAMOND,"emerald",Items.EMERALD);
        for(var entry:RevisionContent.ORES.entrySet()) {
            var state=entry.getValue().get().defaultBlockState();
            var plain=Block.getDrops(state,level,h.absolutePos(new BlockPos(4,2,4)),null,null,pick);
            h.assertTrue(!plain.isEmpty()&&plain.stream().allMatch(s -> s.is(drops.get(entry.getKey()))),"Hard Stone ore drops vanilla mineral: "+entry.getKey());
            var preserved=Block.getDrops(state,level,h.absolutePos(new BlockPos(4,2,4)),null,null,silk);
            h.assertTrue(preserved.size()==1&&preserved.getFirst().is(entry.getValue().get().asItem()),"Silk Touch preserves the custom Hard Stone ore");
            int base=0,boost=0;
            for(int i=0;i<50;i++) {
                base+=Block.getDrops(state,level,BlockPos.ZERO,null,null,pick).stream().mapToInt(ItemStack::getCount).sum();
                boost+=Block.getDrops(state,level,BlockPos.ZERO,null,null,fortune).stream().mapToInt(ItemStack::getCount).sum();
            }
            h.assertTrue(boost>base,"Fortune increases native mineral drops: "+entry.getKey());
        }
        var realm=level.getServer().getLevel(DarkContent.BURNED_REALM);var origin=new BlockPos(768,-24,768);
        for(var p:BlockPos.betweenClosed(origin.offset(-7,-7,-7),origin.offset(7,7,7)))realm.setBlock(p,RevisionContent.HARD_STONE.get().defaultBlockState(),18);
        for(int x=47;x<=49;x++)for(int z=47;z<=49;z++)Heightmap.primeHeightmaps(realm.getChunk(x,z),EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG));
        var feature=realm.registryAccess().getOrThrow(ResourceKey.create(Registries.FEATURE,TheDarkDimensions.id("realm_ore_diamond_small"))).value();
        for(int i=0;i<32;i++)feature.place(realm,realm.getChunkSource().getGenerator(),RandomSource.create(263+i),origin);
        int found=0;
        for(var p:BlockPos.betweenClosed(origin.offset(-7,-7,-7),origin.offset(7,7,7)))if(realm.getBlockState(p).is(RevisionContent.ORES.get("diamond").get()))found++;
        h.assertTrue(found>0,"Native ore feature generates Hard Stone Diamond Ore in the lower stratum");
        h.succeed();
    }
    static void mummyWaterLoot(GameTestHelper h) {
        var level=h.getLevel();var chunk=level.getChunkAt(h.absolutePos(new BlockPos(5,2,5)));
        var origin=new BlockPos(chunk.getPos().getMiddleBlockX(),64,chunk.getPos().getMiddleBlockZ());
        var original=level.getBiome(origin);
        var water=level.registryAccess().getOrThrow(ResourceKey.create(Registries.BIOME,TheDarkDimensions.id("water_cave")));
        chunk.fillBiomesFromNoise((x,y,z) -> water);
        var mummy=TheDarkDimensions.MUMMY.get().create(level,net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        var params=new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY,mummy).withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(origin)).withParameter(LootContextParams.DAMAGE_SOURCE,level.damageSources().generic()).create(LootContextParamSets.ENTITY);
        var loot=level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,TheDarkDimensions.id("entities/mummy")));
        int cores=0;var random=RandomSource.create(263);
        try {
            h.assertTrue(level.getBiome(origin).is(TheDarkDimensions.id("water_cave")),"Loot context is inside a Water Cave");
            for(int i=0;i<1000;i++) {
                var result=loot.getRandomItems(params,random);
                h.assertTrue(result.stream().anyMatch(s -> s.is(TheDarkDimensions.ASHES)),"Water Cave Mummy still drops Ashes");
                if(result.stream().anyMatch(s -> s.is(CaveContent.WATER_CORE)))cores++;
            }
            h.assertTrue(cores>100&&cores<200,"Water Cave Mummies retain the 15 percent Water Core chance");
        } finally { chunk.fillBiomesFromNoise((x,y,z) -> original); }
        h.succeed();
    }
}
