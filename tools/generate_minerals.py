"""Chapter five: ordinary caves, Essentialium geodes, Chronus Orbs and Spelltable."""
import generate_caves
from generate_resources import RES,MOD,write,item,recipe
from pathlib import Path
import json,copy,shutil
TEMPLATES=Path(__file__).parent/'templates/minerals'
SOURCE=Path(__file__).parent/'texture_sources'
BLUE=0x284cc4
def read(path):return json.loads(path.read_text(encoding='utf-8'))
def source(name):return read(TEMPLATES/f'{name}.json')
def block_model(name,model,variants=None):
    write(f'assets/{MOD}/models/block/{name}.json',model)
    write(f'assets/{MOD}/blockstates/{name}.json',{'variants':variants or {'':{'model':f'{MOD}:block/{name}'}}})
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':f'{MOD}:block/{name}'})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
def self_drop(name):
    write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
def tinted_item(name,texture):
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':texture}})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':BLUE}]}})

tinted_item('essentialium','minecraft:item/amethyst_shard')
item('chronus_orb')
rotations={'up':{},'down':{'x':180},'north':{'x':90},'south':{'x':90,'y':180},'east':{'x':90,'y':90},'west':{'x':90,'y':270}}
stages={'essentialium_cluster':'amethyst_cluster','small_essentialium_bud':'small_amethyst_bud','medium_essentialium_bud':'medium_amethyst_bud','large_essentialium_bud':'large_amethyst_bud'}
for name,native in stages.items():
    block_model(name,{'parent':'minecraft:block/tinted_cross','textures':{'cross':f'minecraft:block/{native}'}},{f'facing={d}':{'model':f'{MOD}:block/{name}',**r} for d,r in rotations.items()})
    tinted_item(name,f'minecraft:block/{native}')
    loot=source('cluster_loot' if name=='essentialium_cluster' else 'bud_loot')
    raw=json.dumps(loot).replace('minecraft:amethyst_shard',f'{MOD}:essentialium').replace('minecraft:amethyst_cluster',f'{MOD}:essentialium_cluster').replace('minecraft:small_amethyst_bud',f'{MOD}:{name}')
    write(f'data/{MOD}/loot_table/blocks/{name}.json',json.loads(raw))
for name,native in [('essentialium_block','amethyst_block'),('budding_essentialium','budding_amethyst')]:
    model=source('cube');model['textures']={k:f'minecraft:block/{native}' for k in ['down','up','north','south','west','east','particle']}
    for element in model['elements']:
        for face in element['faces'].values():face['tintindex']=0
    block_model(name,model)
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':BLUE}]}})
    if name=='essentialium_block':self_drop(name)
    else:write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[]})
block_model('chronus_orb_ore',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/chronus_orb_ore'}})
loot=json.dumps(source('ore_loot')).replace('minecraft:diamond_ore',f'{MOD}:chronus_orb_ore').replace('minecraft:diamond',f'{MOD}:chronus_orb')
write(f'data/{MOD}/loot_table/blocks/chronus_orb_ore.json',json.loads(loot))
block_model('spelltable',source('spelltable'));self_drop('spelltable')
recipe('spelltable',['EEE','BOB','OOO'],{'E':f'{MOD}:essentialium','B':'minecraft:book','O':'minecraft:obsidian'})
names=list(stages)+['essentialium_block','budding_essentialium','chronus_orb_ore','spelltable']
for tag,values in [('mineable/pickaxe',names),('needs_iron_tool',['chronus_orb_ore'])]:
    p=f'data/minecraft/tags/block/{tag}.json';file=RES/p
    data=read(file) if file.exists() else {'replace':False,'values':[]}
    data['values'] += [f'{MOD}:{n}' for n in values];write(p,data)

geode=source('geode')
for key,name in [('inner_layer_provider','essentialium_block'),('alternate_inner_layer_provider','budding_essentialium')]:
    geode['blocks'][key]={'id':f'{MOD}:{name}'}
geode['blocks']['inner_placements']=[f'{MOD}:{name}' for name in stages]
write(f'data/{MOD}/worldgen/feature/essentialium_geode.json',geode)
height=lambda low,high:{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':low},'max_inclusive':{'absolute':high}}}
write(f'data/{MOD}/worldgen/placed_feature/essentialium_geodes.json',{'feature':f'{MOD}:essentialium_geode','placement':[{'type':'minecraft:rarity_filter','chance':16},{'type':'minecraft:in_square'},height(-52,24),{'type':'minecraft:biome'}]})
write(f'data/{MOD}/worldgen/feature/chronus_orb_ore.json',{'type':'minecraft:ore','discard_chance_on_air_exposure':0.0,'size':5,'targets':[{'state':f'{MOD}:chronus_orb_ore','target':{'predicate_type':'minecraft:tag_match','tag':f'{MOD}:burned_ore_replaceables'}}]})
write(f'data/{MOD}/worldgen/placed_feature/chronus_orb_ore.json',{'feature':f'{MOD}:chronus_orb_ore','placement':[{'type':'minecraft:count','count':7},{'type':'minecraft:in_square'},height(-56,16),{'type':'minecraft:biome'}]})
for name in ['ash_wastes','charred_forest','crystal_hollows','echo_fields','fire_cave','water_cave','glowing_cave']:
    p=f'data/{MOD}/worldgen/biome/{name}.json';data=read(RES/p)
    data['features'][6]=[f'{MOD}:chronus_orb_ore']
    data['features'][7]=[f'{MOD}:essentialium_geodes']
    write(p,data)
normal=copy.deepcopy(read(RES/f'data/{MOD}/worldgen/biome/ash_wastes.json'))
normal['features'][4]=[];normal['features'][9]=[]
write(f'data/{MOD}/worldgen/biome/normal_caves.json',normal)
dimension=read(RES/f'data/{MOD}/dimension/burned_realm.json');entries=dimension['generator']['biome_source']['biomes']
for entry in list(entries):
    if entry['biome'].endswith('_cave'):
        entry['parameters']['weirdness']=[0.35,1.0]
        other=copy.deepcopy(entry);other['parameters']['weirdness']=[-1.0,-0.35];entries.append(other)
entry=copy.deepcopy(entries[0]);entry['biome']=f'{MOD}:normal_caves';entry['parameters']['depth']=[0.35,2.0];entry['parameters']['humidity']=[-1.0,1.0];entry['parameters']['weirdness']=[-0.3499,0.3499];entries.append(entry)
write(f'data/{MOD}/dimension/burned_realm.json',dimension)
test=Path('src/gametest/resources/data/minecraft/worldgen/world_preset/flat_all_dimensions.json')
preset=read(test);preset['dimensions'][f'{MOD}:burned_realm']=dimension;test.write_text(json.dumps(preset,indent=2)+'\n')
# Legacy registry IDs remain for old saves, but obsolete deposits cannot generate.
for old in ['burned_coal_ore','burned_iron_ore','improved_amethyst_crystal_ore','ember_stone']:
    for folder in ['feature','placed_feature']:
        (RES/f'data/{MOD}/worldgen/{folder}/{old}.json').unlink(missing_ok=True)
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    for name in names:data[f'block.{MOD}.{name}']=name.replace('_',' ').title()
    data[f'block.{MOD}.essentialium_cluster']='Essentialium Gem Crystals'
    data[f'item.{MOD}.essentialium']='Essentialium'
    data[f'item.{MOD}.chronus_orb']='Chronus Orb'
    data[f'biome.{MOD}.normal_caves']='Normal Caves'
    data[f'guide.{MOD}.spelltable']=('3 Essentialium + 2 Bücher + 4 Obsidian. Platzierbarer Tisch; Zauberfunktion folgt später.' if lang=='de_de' else '3 Essentialium + 2 books + 4 obsidian. Placeable table; spell mechanics will be added later.')
    data[f'guide.{MOD}.improved_amethyst']=('Weiterhin craftbar aus einer Amethystscherbe, einem Diamanten und sieben Glasscheiben. Das frühere Erz generiert nicht mehr; Essentialium ist ein eigener Geoden-Rohstoff.' if lang=='de_de' else 'Still craftable from one amethyst shard, one diamond and seven glass panes. The old ore no longer generates; Essentialium is a separate geode resource.')
    write(p,data)
for folder,path in [('items','item'),('blocks','block')]:
    for name in ['chronus_orb','chronus_orb_ore']:
        source_file=SOURCE/folder/f'{name}.png'
        if source_file.exists():shutil.copyfile(source_file,RES/f'assets/{MOD}/textures/{path}/{name}.png')
print('Generated normal caves, Essentialium geodes, Chronus Ore and Spelltable; removed old ore deposits.')
# 26.3 derives motion-blocking worldgen heightmaps from tags, not material flags.
# Vanilla's lists do not automatically include modded stones or other solids.
motion=[f'{MOD}:{p.stem}' for p in (RES/f'assets/{MOD}/blockstates').glob('*.json') if 'portal' not in p.stem and p.stem!='burned_leaves']
write('data/minecraft/tags/block/blocks_motion_no_leaves.json',{'replace':False,'values':sorted(motion)})
