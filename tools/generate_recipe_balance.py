"""Nether-only Improving Template discovery and cheaper, reusable-tool recipes."""
import generate_warrior_stick
from generate_resources import RES,MOD,write,recipe
from generate_minerals import read

(RES/f'data/{MOD}/recipe/improving_template.json').unlink(missing_ok=True)
(RES/f'data/{MOD}/advancement/recipes/improving_template.json').unlink(missing_ok=True)
shrine=f'data/{MOD}/loot_table/chests/elemental_shrine.json'
data=read(RES/shrine)
for pool in data['pools']:
    pool['entries']=[entry for entry in pool['entries'] if entry.get('name')!=f'{MOD}:improving_template']
write(shrine,data)
recipe('burned_stick',ingredients=['minecraft:flint_and_steel','minecraft:stick'])
p=f'data/{MOD}/recipe/burned_stick.json'
data=read(RES/p);data['type']=f'{MOD}:burning_stick';write(p,data)

for chest,chance in [('nether_bridge',.2),('bastion_treasure',.35),('bastion_bridge',.2),('bastion_hoglin_stable',.2),('bastion_other',.2)]:
    name=f'improving_template_{chest}'
    write(f'data/{MOD}/loot_modifiers/{name}.json',{'type':'neoforge:add_table','condition':{'type':'neoforge:loot_table_id','loot_table_id':f'minecraft:chests/{chest}'},'table':f'{MOD}:chests/{name}'})
    write(f'data/{MOD}/loot_table/chests/{name}.json',{'type':'minecraft:chest','pools':[{'rolls':1,'condition':{'type':'minecraft:random_chance','chance':chance},'entries':[{'type':'minecraft:item','name':f'{MOD}:improving_template'}]}]})

for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p);de=lang=='de_de'
    data[f'guide.{MOD}.burned_stick']=('Feuerzeug + Stock → Burning Stick (formlos). Das Feuerzeug bleibt erhalten und verbraucht 3 Haltbarkeit; bei letzter Haltbarkeit zerbricht es. In einer Hand verursacht der Stock Feuerschaden. Ins Wasser werfen → Burned Stick.' if de else 'Flint and Steel + Stick → Burning Stick (shapeless). Returns the igniter with 3 durability consumed; it breaks when exhausted. Holding the stick deals fire damage. Throw it into water → Burned Stick.')
    data[f'guide.{MOD}.improving_template']+=(' Erstes Template in Netherfestungen (20 %) oder Bastionen (20 %, Schatzkisten 35 %) finden. Nur duplizierbar: 7 Diamonds + vorhandenes Template + Crying Obsidian → 2 Templates.' if de else 'Find the first template in Nether fortresses (20%) or Bastions (20%, treasure chests 35%). Duplication only: 7 diamonds + existing template + Crying Obsidian → 2 templates.')
    data[f'guide.{MOD}.blood_netherite']=data[f'guide.{MOD}.blood_netherite'].replace('3 Netherite Ingots','2 Netherite Ingots')
    data[f'guide.{MOD}.katanas']+=(' Blood Essence: 1 Creeperblood + Gold Ingot. Bloodgrief benötigt insgesamt 2 Creeperblood für seine beiden Blood Cores.' if de else 'Blood Essence: 1 Creeperblood + Gold Ingot. Bloodgrief requires 2 Creeperblood total for its two Blood Cores.')
    write(p,data)
print('Balanced recipes and added Improving Template discovery to native Nether chest loot.')
