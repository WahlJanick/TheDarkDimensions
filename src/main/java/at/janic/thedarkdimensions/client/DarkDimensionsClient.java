package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.TheDarkDimensions;
import at.janic.thedarkdimensions.DarkContent;
import at.janic.thedarkdimensions.CaveContent;
import at.janic.thedarkdimensions.MineralContent;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = TheDarkDimensions.MOD_ID, dist = Dist.CLIENT)
public final class DarkDimensionsClient {
    public DarkDimensionsClient(IEventBus bus) {
        bus.addListener((EntityRenderersEvent.AddLayers e)->{for(var skin:e.getSkins()){var renderer=e.getPlayerRenderer(skin);if(renderer!=null)renderer.addLayer(new BackToolLayer(renderer));}});
        bus.addListener((EntityRenderersEvent.RegisterRenderers e)->e.registerEntityRenderer(at.janic.thedarkdimensions.Mindshift.BOLT.get(),net.minecraft.client.renderer.entity.ShulkerBulletRenderer::new));
        bus.addListener((EntityRenderersEvent.RegisterRenderers e)->e.registerEntityRenderer(at.janic.thedarkdimensions.finale.FinaleContent.REGENT.get(),AshboundRegentRenderer::new));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent e)->e.register(at.janic.thedarkdimensions.resonance.ResonanceContent.BENCH_MENU.get(),RunicBenchScreen::new));
        MagicKeys.init(bus);
        MagicFeedback.init(bus);
        DefenseClient.init();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post event) -> {
            DefenseClient.tick();
            var mc=net.minecraft.client.Minecraft.getInstance();
            if(mc.level==null||mc.player==null||mc.isPaused()||!mc.level.dimension().equals(at.janic.thedarkdimensions.DarkContent.BURNED_REALM)||!mc.player.getData(at.janic.thedarkdimensions.Ashfall.ACTIVE))return;
            var camera=mc.getCameraEntity();if(camera==null)return;
            var random=mc.level.getRandom();
            for(int i=0;i<6;i++){
                double x=camera.getX()+random.nextDouble()*32-16,z=camera.getZ()+random.nextDouble()*32-16,y=camera.getY()+4+random.nextDouble()*8;
                var pos=net.minecraft.core.BlockPos.containing(x,y,z);
                if(mc.level.canSeeSky(pos))mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.ASH,x,y,z,0.015,-0.035,0);
            }
        });
        bus.addListener((net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) -> {
            event.registerSpriteSet(at.janic.thedarkdimensions.KatanaEffects.BLOOD_DROP.get(),BloodDropParticle.Provider::new);
            event.registerSpriteSet(at.janic.thedarkdimensions.SolarContent.STORMVEIL_SWEEP.get(),net.minecraft.client.particle.AttackSweepParticle.Provider::new);
            event.registerSpriteSet(at.janic.thedarkdimensions.SolarContent.STORMVEIL_CRIT.get(),net.minecraft.client.particle.CritParticle.Provider::new);
        });
        bus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> event.registerLayerDefinition(DevilTridentModel.LAYER,DevilTridentModel::createLayer));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) -> event.register(TheDarkDimensions.id("devils_trident"),DevilTridentSpecialRenderer.Unbaked.CODEC));
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(net.minecraft.world.entity.EntityTypes.TRIDENT,DevilTridentRenderer::new));
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(at.janic.thedarkdimensions.ArmoryContent.THROWN_AXE.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) -> {
            try(var stream=DarkDimensionsClient.class.getResourceAsStream("/assets/thedarkdimensions/clean_block_palette.json")) {
                if(stream==null)throw new IllegalStateException("Missing clean block palette");
                var palette=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                for(var entry:palette.entrySet()) {
                    var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(TheDarkDimensions.id(entry.getKey()));
                    event.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xff000000|entry.getValue().getAsInt())),block);
                }
            } catch(java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
        });
        bus.addListener((net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) -> event.register(at.janic.thedarkdimensions.SpellContent.SPELLTABLE_MENU.get(),SpelltableScreen::new));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) -> event.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xff72cbd0)),at.janic.thedarkdimensions.SpellContent.METALINIUM_ORE.get(),at.janic.thedarkdimensions.SpellContent.DEEPSLATE_METALINIUM_ORE.get()));
        bus.addListener((net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.BlockTintSources event) -> {
            event.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(MineralContent.ESSENTIALIUM_TINT)),MineralContent.ESSENTIALIUM_CLUSTER.get(),MineralContent.SMALL_ESSENTIALIUM_BUD.get(),MineralContent.MEDIUM_ESSENTIALIUM_BUD.get(),MineralContent.LARGE_ESSENTIALIUM_BUD.get(),MineralContent.ESSENTIALIUM_BLOCK.get(),MineralContent.BUDDING_ESSENTIALIUM.get());
            event.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xffff9d12)),CaveContent.FIRE_CRYSTAL.get());
            event.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xff448fff)),CaveContent.WATER_CRYSTAL.get());
            event.register(java.util.List.of(net.minecraft.client.color.block.BlockTintSources.constant(0xffffff18)),CaveContent.GLOWING_CRYSTAL.get());
        });
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(CaveContent.FIRE_MAGE.get(),FireMageRenderer::new);
            event.registerEntityRenderer(CaveContent.GLOWING_BEE.get(),context -> new BeeRenderer(context) {
                @Override protected int getModelTint(BeeRenderState state) { return 0xffffee80; }
                @Override protected int getBlockLightLevel(net.minecraft.world.entity.animal.bee.Bee entity,net.minecraft.core.BlockPos pos) { return 15; }
            });
            event.registerEntityRenderer(at.janic.thedarkdimensions.VeilwoodContent.WATCHER.get(),HollowWatcherRenderer::new);
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.CHRONUS_MAGE.get(),ChronusMageRenderer::new);
            event.registerEntityRenderer(at.janic.thedarkdimensions.KatanaBossContent.SOVEREIGN.get(),AshenSovereignRenderer::new);
            event.registerEntityRenderer(at.janic.thedarkdimensions.ExplorationContent.KEEPER.get(),ChronusMageRenderer::new);
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.DROWNED_SENTINEL.get(),c->new PackMobRenderer<>(c,"drowned_sentinel",1));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.BRIAR_WHISPERER.get(),c->new PackMobRenderer<>(c,"briar_whisperer",1));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.VEILWOOD_SPIDER.get(),c->new PackMobRenderer<>(c,"veilwood_spider",.65f));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.DUNE_STALKER.get(),c->new PackMobRenderer<>(c,"dune_stalker",.85f));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.CINDER_BROODMOTHER.get(),c->new PackMobRenderer<>(c,"cinder_broodmother",1.3f));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.ASHEN_WRAITH.get(),c->new PackMobRenderer<>(c,"ashen_wraith",1));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.BONEWING.get(),c->new PackMobRenderer<>(c,"bonewing",1));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.LANTERN_WISP.get(),c->new PackMobRenderer<>(c,"lantern_wisp",.55f));
            event.registerEntityRenderer(at.janic.thedarkdimensions.MobExpansionContent.MAGIC_BOLT.get(),net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        });
        bus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(TheDarkDimensions.MUMMY.get(), context -> new net.minecraft.client.renderer.entity.ZombieRenderer(context) {
            @Override public Identifier getTextureLocation(ZombieRenderState state) { return TheDarkDimensions.id("textures/entity/mummy.png"); }
        }));
    }
}
