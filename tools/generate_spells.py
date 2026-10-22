"""Spell progression and Overworld Metalinium. Reuses vanilla silhouettes with model tints."""
import generate_minerals
from generate_minerals import read,source,block_model
from generate_resources import RES,MOD,write,recipe
import copy

recipe('spell_book',ingredients=[f'{MOD}:spell_core','minecraft:book'])
recipe('metalinium_ingot',['NNN','NNN','NNN'],{'N':f'{MOD}:metalinium_nugget'})
recipe('essentialium_infused_metalinium_ingot',ingredients=[f'{MOD}:metalinium_ingot',f'{MOD}:chronus_orb'])
icons={'spell_book':('book',0xbcb0d5),'metalinium_nugget':('iron_nugget',0x72cbd0),'metalinium_ingot':('iron_ingot',0x72cbd0),'essentialium_infused_metalinium_ingot':('iron_ingot',0x536cdd),'fire_spell_book':('enchanted_book',0xff903b),'lightning_spell_book':('enchanted_book',0xffdf50),'ender_chest_spell_book':('enchanted_book',0xa178e0),'water_spell_book':('enchanted_book',0x58b5ff)}
for name,(native,tint) in icons.items():
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'minecraft:item/{native}'}})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':tint}]}})
ores=['metalinium_ore','deepslate_metalinium_ore']
for name,native in zip(ores,['iron_ore','deepslate_iron_ore']):
    model=source('cube');model['textures']={k:f'minecraft:block/{native}' for k in ['down','up','north','south','west','east','particle']}
    for element in model['elements']:
        for face in element['faces'].values():face['tintindex']=0
    block_model(name,model)
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':0x72cbd0}]}})
    loot=source('ore_loot');loot.pop('random_sequence',None)
    children=loot['pools'][0]['entries'][0]['children'];children[0]['name']=f'{MOD}:{name}'
    children[1]['name']=f'{MOD}:metalinium_nugget'
    children[1]['modifier']=[{'type':'minecraft:set_count','count':{'type':'minecraft:uniform','min':3,'max':6}},{'type':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:uniform_bonus_count','parameters':{'bonusMultiplier':1}},{'type':'minecraft:limit_count','limit':{'max':9}},{'type':'minecraft:explosion_decay'}]
    write(f'data/{MOD}/loot_table/blocks/{name}.json',loot)
for tag in ['mineable/pickaxe','needs_stone_tool','blocks_motion_no_leaves']:
    p=f'data/minecraft/tags/block/{tag}.json';data=read(RES/p)
    data['values']=sorted(set(data['values']+[f'{MOD}:{n}' for n in ores]));write(p,data)
write(f'data/{MOD}/worldgen/feature/metalinium_ore.json',{'type':'minecraft:ore','size':8,'discard_chance_on_air_exposure':0.0,'targets':[{'state':f'{MOD}:{name}','target':{'predicate_type':'minecraft:tag_match','tag':f'minecraft:{tag}_ore_replaceables'}} for name,tag in zip(ores,['stone','deepslate'])]})
write(f'data/{MOD}/worldgen/placed_feature/metalinium_ore.json',{'feature':f'{MOD}:metalinium_ore','placement':[{'type':'minecraft:count','count':8},{'type':'minecraft:in_square'},generate_minerals.height(-56,48),{'type':'minecraft:biome'}]})
write(f'data/{MOD}/neoforge/biome_modifier/add_metalinium.json',{'type':'neoforge:add_features','biomes':'#minecraft:is_overworld','features':f'{MOD}:metalinium_ore','step':'underground_ores'})
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    for name in icons:data[f'item.{MOD}.{name}']=name.replace('_',' ').title()
    for name in ores:data[f'block.{MOD}.{name}']=name.replace('_',' ').title()
    spells=['Feuerstoß','Blitzschlag','Enderchest','Wasserheilung'] if lang=='de_de' else ['Fire Burst','Lightning','Ender Chest','Water Healing']
    for i,name in enumerate(spells):
        data[f'spell.{MOD}.{i}']=name
        data[f'item.{MOD}.{list(icons)[4+i]}']=name+' – Spell Book'
    data[f'gui.{MOD}.ingredients']='Buch + Lapis + Infused Ingot' if lang=='de_de' else 'Book + Lapis + Infused Ingot'
    data[f'gui.{MOD}.free']='Wähle einen Zauber • Keine Levelkosten' if lang=='de_de' else 'Choose a spell • No level cost'
    data[f'item.{MOD}.essentialium_infused_metalinium_ingot']='Chronus Infused Metalinium Ingot'
    data[f'guide.{MOD}.spelltable']='Spell Book + 1 Lapis + 1 Chronus Infused Metalinium Ingot → wähle einen Zauber ohne Levelkosten. Rechtsklick mit dem fertigen Buch wirkt den Zauber.' if lang=='de_de' else 'Spell Book + 1 lapis + 1 Chronus Infused Metalinium Ingot → choose a spell without levels. Right-click with the resulting book to cast.'
    write(p,data)
print('Generated spells, Metalinium ores, capped Fortune drops and crafting recipes.')
