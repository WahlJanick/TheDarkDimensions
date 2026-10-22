package at.janic.thedarkdimensions.client;

import at.janic.thedarkdimensions.MagicSkill;
import net.minecraft.world.item.*;

final class SpellIcons {
    static ItemStack item(MagicSkill s){return new ItemStack(switch(s){
        case FIRE_BOLT->Items.FIRE_CHARGE;case FIRE_RING->Items.BLAZE_POWDER;case FIRE_GUARD->Items.MAGMA_CREAM;
        case WATER_HEAL->Items.WATER_BUCKET;case WATER_WAVE->Items.PRISMARINE_SHARD;case WATER_GUARD->Items.HEART_OF_THE_SEA;
        case LIGHTNING_STRIKE->Items.LIGHTNING_ROD.weathering().unaffected();case CHAIN_LIGHTNING->Items.IRON_CHAIN;case LIGHTNING_STEP->Items.GOLDEN_BOOTS;
        case CHRONUS_DASH->Items.FEATHER;case TIME_SLOW->Items.CLOCK;case REWIND->Items.RECOVERY_COMPASS;
        case BLOOD_CUT->Items.IRON_SWORD;case BLOOD_SIPHON->Items.REDSTONE;case BLOOD_GUARD->Items.NETHER_WART;
        case SOUL_BOLT->Items.SOUL_TORCH;case SOUL_FEAR->Items.SOUL_LANTERN;case SOUL_SIGHT->Items.SCULK_SENSOR;
        case ENDER_CHEST->Items.ENDER_CHEST;case ENDER_BLINK->Items.ENDER_PEARL;case ENDER_WARD->Items.ENDER_EYE;
        case EMBER_MARK->Items.BLAZE_ROD;case ASH_STEP->Items.FIRE_CHARGE;case INFERNO->Items.BLAZE_POWDER;
        case FROST_BIND->Items.BLUE_ICE;case WATER_VEIL->Items.GLASS_BOTTLE;case HEALING_SPRING->Items.SEA_LANTERN;
        case STATIC_CHARGE->Items.COPPER_INGOT;case THUNDER_LEAP->Items.IRON_BOOTS;case STORM_FIELD->Items.LIGHTNING_ROD.weathering().unaffected();
        case TIME_ANCHOR->Items.LODESTONE;case AFTERIMAGE->Items.PHANTOM_MEMBRANE;case INERTIA_FIELD->Items.COBWEB;case STOLEN_MOMENT->Items.CLOCK;
        case VOID_PULL->Items.CHORUS_FRUIT;case DIMENSION_FOLD->Items.END_CRYSTAL;
        case ELEMENTAL_BURST->Items.AMETHYST_SHARD;case ELEMENTAL_BARRIER->Items.SHIELD;case ELEMENTAL_SURGE->Items.NETHER_STAR;
    });}
    static String description(MagicSkill s){return switch(s){
        case FIRE_BOLT->"Schießt einen Feuerball.";case FIRE_RING->"Verbrennt nahe Gegner und lädt Glutzeichen auf. Stufe 5: Rückstoß.";case FIRE_GUARD->"Verleiht Feuerresistenz.";
        case WATER_HEAL->"Heilt dich und löscht Feuer. Stufe 5: entfernt Gift.";case WATER_WAVE->"Stößt nahe Gegner zurück.";case WATER_GUARD->"Verleiht Wasseratmung und Resistenz.";
        case LIGHTNING_STRIKE->"Ein Blitz trifft dein Ziel.";case CHAIN_LIGHTNING->"Blitze springen mit abnehmendem Schaden zwischen Gegnern. Stufe 5: ein zusätzlicher Sprung.";case LIGHTNING_STEP->"Dash mit Blitzschaden am Zielort.";
        case CHRONUS_DASH->"Kurzer Dash nach vorne.";case TIME_SLOW->"Verlangsamt nahe Gegner.";case REWIND->"Kehrt zur Position vor drei Sekunden zurück.";
        case BLOOD_CUT->"Verursacht Schaden und Blutung.";case BLOOD_SIPHON->"Entzieht einem Gegner Leben.";case BLOOD_GUARD->"Verleiht zusätzliche Absorptionsherzen.";
        case SOUL_BOLT->"Verletzt und desorientiert dein Ziel.";case SOUL_FEAR->"Desorientiert nahe Gegner.";case SOUL_SIGHT->"Nachtsicht; nahe Gegner leuchten.";
        case ENDER_CHEST->"Öffnet deine Enderchest.";case ENDER_BLINK->"Teleportiert ein Stück nach vorne.";case ENDER_WARD->"Verleiht sanften Fall und Resistenz.";
        case EMBER_MARK->"Markiert ein Ziel für 7–9 s. Drei Feuertreffer lösen das Zeichen aus; Stufe 5: zwei Treffer.";
        case ASH_STEP->"Dash mit glühender Spur. Stufe 5: Die Spur verlangsamt zusätzlich.";
        case INFERNO->"Löst eigene Glutzeichen im Umkreis von 12 Blöcken für 5–9 Magieschaden aus. Stufe 5: kleiner Splitterimpuls.";
        case FROST_BIND->"Hält ein Ziel kurz fest; Bosse werden leicht verlangsamt. Stufe 5: zwei Nebenziele und Frosted.";
        case WATER_VEIL->"Halbiert den nächsten Treffer innerhalb von 5–7 s. Stufe 5: 65 % Schutz und löscht Feuer.";
        case HEALING_SPRING->"Heilt dich und Teammitglieder in einem kleinen Bereich. Stufe 5: entfernt einmal Gift oder Schwäche.";
        case STATIC_CHARGE->"Kettenblitze bevorzugen das markierte Ziel; es ermöglicht Gewitterfelder. Stufe 5: markiert einen Nachbarn mit.";
        case THUNDER_LEAP->"Springt nach vorne; der Aufprall verletzt und stößt Gegner zurück. Stufe 5: größerer Radius.";
        case STORM_FIELD->"Benötigt statische Ziele. Nach 1 s treffen Impulse markierte Gegner. Stufe 5: zusätzlicher kurzer Blitz zu einem Nachbarn.";
        case TIME_ANCHOR->"Anker für 12–16 s; erneut wirken zum Zurückkehren. Setzen: 1 s Cooldown. Stufe 5: entfernt Slowness und Schwäche.";
        case AFTERIMAGE->"Dash mit einem kurzlebigen Nachbild, das angreifende Mobs ablenkt. Stufe 5: größere Ablenkungsreichweite.";
        case INERTIA_FIELD->"Ein festes Zeitfeld verlangsamt Gegner und feindliche Projektile. Stufe 5: stärkere Projektilbremsung.";
        case STOLEN_MOMENT->"8 s bereit: Perfect Dodge verkürzt den Cooldown des nächsten Zaubers um 35 %, auf Stufe 5 um 50 %.";
        case VOID_PULL->"Zieht Gegner zum Zielpunkt; Bosse widerstehen dem Zug. Stufe 5: stärkere kurze Verlangsamung.";
        case DIMENSION_FOLD->"Zwei Durchgänge für 6–8 s, bis 12 Blöcke. Im Ring schleichen. Stufe 5: kurzer Temposchub nach dem Durchgang.";
        case ELEMENTAL_BURST->"Magieschaden an nahen Gegnern.";case ELEMENTAL_BARRIER->"Verleiht Resistenz.";case ELEMENTAL_SURGE->"Blitzschaden, Verlangsamung und Heilung.";
    };}
}
