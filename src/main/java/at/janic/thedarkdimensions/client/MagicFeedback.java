package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;

/** A second small native attack-indicator bar, below the melee bar; no custom HUD panels. */
public final class MagicFeedback {
    private static final Identifier BACK=Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_background"),PROGRESS=Identifier.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private static final SpellCooldownFeedback STATE=new SpellCooldownFeedback();
    private static Object player,level;private static int flash;
    private static boolean usable(){var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||!mc.player.isAlive()||mc.player.isSpectator())return false;int source=MagicBooks.active(mc.player);if(source<0)return false;var d=MagicData.read(MagicBooks.get(mc.player,source));return d.knows(d.slots[d.selected]);}
    private static void tick(){var mc=Minecraft.getInstance();if(player!=mc.player||level!=mc.level){STATE.reset();flash=0;player=mc.player;level=mc.level;}if(mc.isPaused())return;if(flash>0)flash--;
        boolean active=usable();int remaining=active?at.janic.thedarkdimensions.resonance.AncientRunes.remaining(mc.player.getCooldowns(),mc.player.getCooldowns().getCooldownGroup(new ItemStack(SpellContent.FIRE_BOOK.get()))):0;
        if(STATE.tick(active,remaining)){flash=10;if(mc.gui.screen()==null)mc.player.playSound(net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,.25f,1.5f);}
    }
    private static void render(GuiGraphicsExtractor g,net.minecraft.client.DeltaTracker delta){var mc=Minecraft.getInstance();if(mc.gui.hud.isHidden()||mc.gui.screen()!=null||!mc.options.getCameraType().isFirstPerson()||!usable())return;
        float fraction=mc.player.getCooldowns().getCooldownPercent(new ItemStack(SpellContent.FIRE_BOOK.get()),0);if(fraction<=0&&flash==0)return;
        int x=g.guiWidth()/2-8,y=g.guiHeight()/2+18,fill=Math.clamp(Math.round((1-fraction)*16),0,16);
        g.blitSprite(RenderPipelines.GUI_TEXTURED,BACK,x,y,16,4);
        if(fill>0)g.blitSprite(RenderPipelines.GUI_TEXTURED,PROGRESS,16,4,0,0,x,y,fill,4,flash>0?0xff80ff20:-1);
    }
    public static void init(IEventBus bus){bus.addListener((net.neoforged.neoforge.client.event.RegisterGuiLayersEvent e)->e.registerAbove(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR,TheDarkDimensions.id("spell_cooldown"),MagicFeedback::render));net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientTickEvent.Post e)->tick());}
}
