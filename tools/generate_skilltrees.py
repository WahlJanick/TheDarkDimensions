"""Current full resource chain, with expanded spellbook guides; no texture edits."""
import generate_mindshift
from generate_resources import RES, MOD, write
import json

write(f'data/{MOD}/damage_type/arcane_pulse.json',{'message_id':'arcane_pulse','scaling':'never','exhaustion':0.0})
for tag in ['bypasses_cooldown','bypasses_armor','is_magic']:
    path=f'data/minecraft/tags/damage_type/{tag}.json'
    data=json.loads((RES/path).read_text(encoding='utf-8'))
    if f'{MOD}:arcane_pulse' not in data['values']:data['values'].append(f'{MOD}:arcane_pulse')
    write(path,data)
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));de=lang=='de_de'
    data[f'guide.{MOD}.spelltable']=(
        'Blankes Spellbook + Core am Spelltable bindet ein Element. 39 Zauber in acht Element-Bäumen; jeder neue Skill kostet 1 Chronus Infused Ingot und die angezeigte Stufe des Vorgängers. 8 / 20 / 40 / 70 erfolgreiche Einsätze ergeben Stufen 2 / 3 / 4 / 5. Grün umrandete Knoten sind gelernt oder mit deinen Materialien freischaltbar; graue zeigen im Hover fehlende Bedingungen. Knoten anklicken zum Erlernen, darüber Taste 1–5 zum Belegen. Master-Buch: Element-Reiter, fünf gemeinsame gemischte Slots. Öffnen, Spellwheel und Wirken sind separat belegbare Tasten. Blankes Buch: Book + Runestone + Metalinium. Master-Buch: vier Elemental Cores, zwei Essentialium, zwei Chronus Infused Ingots und ein blankes Buch. Weitere Cores am Spelltable schalten zusätzliche Elemente frei.' if de else
        'Blank Spellbook + Core at the Spelltable binds an element. 39 spells across eight element trees. Each new skill costs one Chronus Infused Ingot and the displayed predecessor level. 8 / 20 / 40 / 70 successful uses grant levels 2 / 3 / 4 / 5. Green nodes are learned or affordable; hover gray nodes for missing requirements. Click to learn, hover and press 1–5 to assign. Master books have element tabs and five shared mixed slots. Open book, spellwheel and cast use separately configurable keys. Blank book: Book + Runestone + Metalinium. Master book: four Elemental Cores, two Essentialium, two Chronus Infused Ingots and a blank book. Bind additional elements using cores at the Spelltable.')
    data[f'guide.{MOD}.spell_branches']=(
        'Feuer: Feuerstoß → Glutzeichen → Inferno; Feuerstoß → Aschenschritt → Flammenring. Wasser: Wasserheilung → Gezeitenstoß → Frostfessel oder Wasserschleier → Heilquelle / Wasserschutz. Blitz: Blitzschlag → Statische Ladung → Gewitterfeld, Kettenblitz oder Blitzschritt → Donnersprung. Chronus: Dash → Rücksprung → Zeitanker, Zeitfeld → Trägheitsfeld oder Nachbild → Gestohlener Moment. Ender: Enderchest → Rissschritt → Leerenzug oder Enderschutz → Dimensionsfalte. Die bisherigen Blut-, Seelen- und Elemental-Zauber bleiben erhalten. Vorhandene Bücher behalten alle gelernten Zauber, ihre Stufen und Slots.' if de else
        'Fire: Fire Bolt → Ember Mark → Inferno, or Ash Step → Flame Ring. Water: Water Healing → Tidal Wave → Frost Bind, or Water Veil → Healing Spring / Water Guard. Lightning: Lightning Strike → Static Charge → Storm Field, Chain Lightning, or Lightning Step → Thunder Leap. Chronus: Dash → Rewind → Time Anchor, Time Slow → Inertia Field, or Afterimage → Stolen Moment. Ender: Enderchest → Rift Step → Void Pull, or Ender Ward → Dimension Fold. Existing Blood, Soul and Elemental spells remain available. Existing books retain all learned spells, levels and slots.')
    data[f'guide.{MOD}.spell_tactics']=(
        'Glutzeichen laden sich durch drei eigene Feuertreffer auf; auf Stufe 5 reichen zwei. Inferno löst eigene Zeichen aus. Statische Ladung lässt Kettenblitze Ziele bevorzugen und ermöglicht angekündigte Gewitterfelder. Heilquelle wirkt auf dich und Teammitglieder. Zeitanker: erneut wirken zum Rücksprung; Setzen hat 1 s Cooldown, Rückkehr bis zu 9 s. Gestohlener Moment: nach Vorbereitung innerhalb von 8 s einen Perfect Dodge schaffen; der nächste erfolgreiche Zauber hat 35 % kürzeren Cooldown, auf Stufe 5 50 %. Vorbereitung 1 s, interne Erholung 12 s. Dimensionsfalte: auf die Oberseite eines sicheren Bodens zielen; im Durchgang schleichen. Zwei kurze Durchgänge in derselben Dimension, bis 12 Blöcke. Keine Blöcke werden verändert und keine Chunks nachgeladen.' if de else
        'Ember Marks charge through three of your fire hits; level five needs two. Inferno consumes your marks. Static Charge prioritizes chain-lightning targets and enables telegraphed Storm Fields. Healing Spring supports you and teammates. Cast Time Anchor again to return: placement cooldown one second, return up to nine seconds. Stolen Moment: prepare, then Perfect Dodge within eight seconds; the next successful spell has a 35% shorter cooldown, or 50% at level five. Preparation cooldown one second, internal recovery twelve seconds. Dimension Fold: aim at the upper face of safe ground; crouch inside a passage. Two temporary passages in the same dimension, up to twelve blocks apart. Spells never change terrain or load chunks.')
    data['death.attack.arcane_pulse']='%1$s wurde von arkaner Magie getroffen' if de else '%1$s was struck by arcane magic'
    data['death.attack.arcane_pulse.player']='%1$s wurde von %2$s arkaner Magie getroffen' if de else '%1$s was struck by %2$s\'s arcane magic'
    write(path,data)
print('Generated 39-spell skilltree guides, tactics and native magic damage tags; existing assets preserved.')
import generate_vanilla_ui

import generate_back_equipment
import generate_spell_mastery
