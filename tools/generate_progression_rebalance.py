"""Current recipe progression, Warrior smithing and interchangeable essence blood."""
import generate_recipe_balance
from generate_resources import RES,MOD,write,recipe,item
from generate_minerals import read
from pathlib import Path
import shutil

def mod(name): return f'{MOD}:{name}'
def smith(name,template,base,addition,result):
    write(f'data/{MOD}/recipe/{name}.json',{'type':'minecraft:smithing_transform','template':template,'base':base,'addition':addition,'result':{'id':mod(result)}})
    write(f'data/{MOD}/advancement/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_base':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':base}]}},'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipes':[mod(name)]}}},'requirements':[['has_base','has_recipe']],'rewards':{'recipes':[mod(name)]}})

recipe('stormveil',ingredients=[mod('lightning_core'),mod('solar_essence'),mod('reinforced_stick')])
recipe('spell_book',ingredients=['minecraft:book',mod('empty_runestone'),mod('metalinium_ingot')])
recipe('elemental_master_spellbook',['ECE','CBC','MCM'],{'E':mod('essentialium'),'C':mod('spell_core'),'B':mod('spell_book'),'M':mod('essentialium_infused_metalinium_ingot')})
recipe('thundergods_hammer',['DBD','LSL',' S '],{'D':'minecraft:diamond','B':'minecraft:diamond_block','L':mod('lightning_core'),'S':mod('reinforced_stick')})
write(f'data/{MOD}/tags/item/essence_blood.json',{'replace':False,'values':[mod('creeperblood'),mod('zombieblood')]})
write(f'data/{MOD}/tags/entity_type/zombie_blood_sources.json',{'replace':False,'values':['minecraft:'+x for x in ['zombie','husk','drowned','zombie_villager','zombified_piglin','zombie_horse','zombie_nautilus','zoglin']]})
recipe('blood_essence',ingredients=['#'+mod('essence_blood'),'minecraft:gold_ingot'])
recipe('blood_essence_bulk',ingredients=['#'+mod('essence_blood'),'#'+mod('essence_blood'),'minecraft:gold_ingot','minecraft:gold_ingot'],result='blood_essence',count=2)
recipe('warrior_axe_upgrade_template',['IBI','LSL','IBI'],{'I':'minecraft:iron_ingot','B':'#'+mod('essence_blood'),'L':'minecraft:leather','S':mod('reinforced_stick')})
for tier in ['wooden','stone','copper','iron','golden','diamond','netherite']:
    name=f'{tier}_warrior_axe' if tier=='netherite' else f'convert_{tier}_warrior_axe'
    smith(name,mod('warrior_axe_upgrade_template'),f'minecraft:{tier}_axe',mod('warrior_stick'),f'{tier}_warrior_axe')
smith('upgrade_netherite_warrior_axe','minecraft:netherite_upgrade_smithing_template',mod('diamond_warrior_axe'),'minecraft:netherite_ingot','netherite_warrior_axe')

archive=Path(__file__).parent/'texture_sources/progression_rebalance'
shutil.copyfile(archive/'warrior_axe_upgrade_template.png',RES/f'assets/{MOD}/textures/item/warrior_axe_upgrade_template.png')
item('warrior_axe_upgrade_template')
# Preserve the existing native blood-bottle model, tinting only its liquid layer.
write(f'assets/{MOD}/models/item/zombieblood.json',read(RES/f'assets/{MOD}/models/item/creeperblood.json'))
write(f'assets/{MOD}/items/zombieblood.json',{'model':{'type':'minecraft:model','model':mod('item/zombieblood'),'tints':[{'type':'minecraft:constant','value':0x8B303A}]}})

for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p);de=lang=='de_de'
    data[f'item.{MOD}.zombieblood']='Zombieblood' if de else 'Zombie Blood'
    data[f'item.{MOD}.warrior_axe_upgrade_template']='Warrior Axe Upgrade Template'
    prefix=f'template.{MOD}.warrior.'
    for key,value in {'applies_to':('Äxte','Axes'),'ingredients':('Warrior Stick','Warrior Stick'),'base_slot':('Lege eine normale Axt hinein','Add a normal axe'),'addition_slot':('Lege einen Warrior Stick hinein','Add a Warrior Stick')}.items():data[prefix+key]=value[0 if de else 1]
    data[f'guide.{MOD}.warrior_axe_upgrade_template']=('Template: 4 Iron Ingots, 2 Blood (Creeper oder Zombie), 2 Leather und Improved Stick. Smithing: Template + normale Axt + Warrior Stick → Warrior Axe. Diamond Warrior Axe mit Netherite Upgrade Template + Netherite Ingot upgraden. Name, Verzauberungen und Verschleiß bleiben erhalten.' if de else 'Template: 4 iron ingots, 2 blood (creeper or zombie), 2 leather and Improved Stick. Smithing: template + normal axe + Warrior Stick → Warrior Axe. Upgrade Diamond Warrior Axe with Netherite Upgrade Template + Netherite Ingot. Preserves name, enchantments and wear.')
    data[f'guide.{MOD}.zombieblood']=('5 % Chance auf 1 Zombieblood bei Zombies, ihren Varianten und zombifizierten Tieren. Blood Essence: 1 Creeperblood oder Zombieblood + Gold Ingot. Gemischt: 2 beliebige Blood + 2 Gold Ingots → 2 Blood Essences. Andere Creeperblood-Rezepte bleiben Creeperblood-Rezepte.' if de else '5% chance of one Zombie Blood from zombies, their variants and zombified animals. Blood Essence: one Creeper Blood or Zombie Blood + Gold Ingot. Mixed: any two blood + two gold ingots → two Blood Essences. Other Creeper Blood recipes still require Creeper Blood.')
    data[f'guide.{MOD}.spell_book']=('Blank Spellbook: Book + Empty Runestone + Metalinium Ingot. Erst Buch + Core am Spelltable bestimmt das Element; ohne Levelkosten. Skills benötigen Chronus Infused Ingots und Übung.' if de else 'Blank Spellbook: Book + Empty Runestone + Metalinium Ingot. Bind its element with book + core at the Spelltable, without experience levels. Skills require Chronus Infused Ingots and practice.')
    data[f'guide.{MOD}.elemental_master_spellbook']=('4 Elemental Cores, 2 Essentialium, 2 Chronus Infused Ingots um ein Blank Spellbook. Das Buch startet ohne Element. Am Spelltable mit Cores mehrere Elemente freischalten; Fortschritt bleibt erhalten.' if de else 'Four Elemental Cores, two Essentialium, two Chronus Infused Ingots around a Blank Spellbook. Starts without an element. Unlock multiple elements using cores at the Spelltable; existing progress is retained.')
    data[f'guide.{MOD}.hammer']=('10 Schaden. Crafting: Diamond / Diamond Block / Diamond; Lightning Core / Improved Stick / Lightning Core; leer / Improved Stick / leer. Nur der Awakened Hammer ermöglicht Elytra-Gleiten in einer Hand. Awakening: Lightning Core als Template, Hammer als Basis und Lightning Spellbook als Zusatz. Rechtsklick: gezielter Blitzschlag mit 5 Sekunden Cooldown.' if de else '10 damage. Crafting: diamond / diamond block / diamond; lightning core / Improved Stick / lightning core; empty / Improved Stick / empty. Only the Awakened Hammer grants held Elytra gliding. Awaken with Lightning Core as template, hammer as base and Lightning Spellbook as addition. Right-click: aimed lightning strike with a five-second cooldown.')
    key=f'guide.{MOD}.katanas'
    data[key]=data[key].replace('1 Creeperblood + Gold Ingot','1 Creeperblood oder Zombieblood + Gold Ingot' if de else '1 Creeper Blood or Zombie Blood + Gold Ingot').replace('2 Creeperblood','2 Blood')
    data[f'guide.{MOD}.stormveil']=('Solar Essence + Lightning Core + Improved Stick, formlos. 9 Schaden, 1,5 Angriffstempo und kurze elektrische Treffer; dritter Treffer erzeugt Kettenblitz.' if de else 'Solar Essence + Lightning Core + Improved Stick, shapeless. Nine damage, 1.5 attack speed and brief electrical hits; the third hit creates chain lightning.')
    write(p,data)
print('Updated Warrior upgrades, spellbook progression, Stormveil, awakened-only flight recipes and interchangeable blood essences.')
