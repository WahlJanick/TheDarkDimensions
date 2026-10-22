"""Ashen Sovereign: original pack skin, discoverable dojo, boss loot and localized lore."""
import generate_core_recipes
from generate_exploration import Template, structure, advancement
from generate_resources import RES,MOD,write,item
from pathlib import Path
import shutil,json

root=RES.parents[2]
original=root.parent/'TheDarkDimensions NutzeDieseResourcen!/Other Mob textures/minecraft/textures/entity/illager/vindicator.png'
archive=root/'tools/texture_sources/katana_boss/vindicator.png';archive.parent.mkdir(parents=True,exist_ok=True)
if original.exists():shutil.copyfile(original,archive)
shutil.copyfile(archive,RES/f'assets/{MOD}/textures/entity/ashen_sovereign.png')
write(f'assets/{MOD}/katana_boss_texture_source.json',{'source':'illager/vindicator.png','credit':'Barely Default / Mickey Joe','geometry':'Vanilla Vindicator, 64x64; original texture is unchanged.'})
item('ashen_sovereign_spawn_egg')
write(f'assets/{MOD}/models/item/ashen_sovereign_spawn_egg.json',{'parent':'minecraft:item/vindicator_spawn_egg'})
write(f'assets/{MOD}/items/ashen_sovereign_spawn_egg.json',{'model':{'type':'minecraft:model','model':'minecraft:item/vindicator_spawn_egg'}})

structure('ashen_dojo',['ash_wastes','charred_forest','veilwood'],{'absolute':-1},True,42,263029)
t=Template([23,10,23]);t.box(0,0,0,22,0,22,'minecraft:stone_bricks');t.box(1,1,1,21,7,21,'minecraft:air')
# A low, weathered boundary leaves the courtyard open for directional dodges.
for x in range(23):
    for z in range(23):
        if x in [0,22] or z in [0,22]:t.put(x,1,z,'minecraft:cracked_stone_bricks')
        if (x*5+z*3)%13==0:t.put(x,0,z,'minecraft:mossy_stone_bricks')
t.box(9,1,0,13,2,0,'minecraft:air')
for x,z in [(2,2),(20,2),(2,20),(20,20)]:
    t.box(x,1,z,x,5,z,f'{MOD}:burned_log',{'axis':'y'});t.put(x,6,z,'minecraft:soul_lantern',{'hanging':'false','waterlogged':'false'})
# Surviving roof on the rear hall; broken edges and an unobstructed combat floor.
t.box(3,1,18,19,1,21,f'{MOD}:burned_planks');t.box(3,6,18,19,6,21,f'{MOD}:burned_planks')
for x in [3,19]:t.box(x,2,21,x,5,21,f'{MOD}:burned_log',{'axis':'y'})
for x in range(4,19):
    if x%4==0:t.put(x,6,18,'minecraft:air')
t.book(5,2,20,'The Last Duel','The Sovereign still holds his blade. Blue sparks warn of a cut; red sparks warn of a blow no blade can catch.\n\nMeet a blue cut at the last moment. Evade the red blow. When his balance breaks, answer with one strike.\n\nBeyond two wounds, his patience turns to fury.')
t.chest(17,2,20,'ashen_dojo');t.mob(11,1,12,'ashen_sovereign',120);t.save('ashen_dojo')
write(f'data/{MOD}/loot_table/chests/ashen_dojo.json',{'type':'minecraft:chest','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:japanese_essence','functions':[{'function':'minecraft:set_count','count':2}]}]},{'rolls':2,'entries':[{'type':'minecraft:item','name':'minecraft:gold_ingot','weight':2},{'type':'minecraft:item','name':f'{MOD}:ashes','weight':3},{'type':'minecraft:item','name':f'{MOD}:empty_runestone','weight':1}]}]})
write(f'data/{MOD}/loot_table/entities/ashen_sovereign.json',{'type':'minecraft:entity','pools':[
    {'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:souleater','functions':[{'function':'minecraft:set_damage','damage':.7}]}]},
    {'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:soul_core','functions':[{'function':'minecraft:set_count','count':2}]}]},
    {'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:ashes','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':8,'max':16}}]}]}
]})
write(f'data/{MOD}/damage_type/sovereign_heavy.json',{'message_id':'mob','exhaustion':.1,'scaling':'when_caused_by_living_non_player'})
write('data/minecraft/tags/damage_type/bypasses_shield.json',{'replace':False,'values':[f'{MOD}:sovereign_heavy']})
advancement('sovereign_defeated','enter_realm',f'{MOD}:souleater','The Last Duel','Defeat the Ashen Sovereign.',frame='challenge',reward=100)
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data[f'entity.{MOD}.ashen_sovereign']='The Ashen Sovereign'
    data[f'item.{MOD}.ashen_sovereign_spawn_egg']='Ashen Sovereign Spawn Egg'
    data[f'advancement.{MOD}.sovereign_defeated.title']='Das letzte Duell' if lang=='de_de' else 'The Last Duel'
    data[f'advancement.{MOD}.sovereign_defeated.description']='Besiege den Ashen Sovereign.' if lang=='de_de' else 'Defeat the Ashen Sovereign.'
    data[f'bestiary.{MOD}.ashen_sovereign.habitat']='Ashen Dojo in der Burned Dimension. Ein persistenter Boss je Dojo.' if lang=='de_de' else 'Ashen Dojo in the Burned Dimension. One persistent boss per dojo.'
    data[f'bestiary.{MOD}.ashen_sovereign.behavior']='Katana-Duellant mit drei Phasen. Blaue Hiebe lassen sich parieren, rote schwere Angriffe erfordern Ausweichen. Nach einem Parry ist er kurz offen für einen Konter.' if lang=='de_de' else 'Three-phase katana duellist. Blue cuts can be parried; red heavy attacks require a dodge. A successful parry briefly opens his guard for a counter.'
    data[f'advancement.{MOD}.bestiary_complete.description']='Beobachte alle fünfzehn Kreaturen im Bestiarium.' if lang=='de_de' else 'Observe all fifteen creatures in the Bestiary.'
    write(path,data)
print('Generated original Sovereign skin, Ashen Dojo, heavy damage type, boss loot and advancement.')
