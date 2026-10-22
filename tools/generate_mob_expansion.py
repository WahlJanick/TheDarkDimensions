"""Expansion assets follow all previous resources; imported artist textures stay byte-exact."""
import generate_veilwood
from generate_resources import RES,MOD,write,item
from pathlib import Path
import shutil,json
from cem_model_data import geometry,visibility_rules
from PIL import Image,ImageDraw

sources=Path(__file__).resolve().parents[2]/'TheDarkDimensions NutzeDieseResourcen!'/ 'Other Mob textures/minecraft/textures/entity'
archive=Path(__file__).parent/'texture_sources/mob_expansion'
archive.mkdir(parents=True,exist_ok=True)
def import_texture(source,name):
    original=sources/source
    saved=archive/source
    saved.parent.mkdir(parents=True,exist_ok=True)
    if original.exists():shutil.copyfile(original,saved)
    target=RES/f'assets/{MOD}/textures/entity/{name}.png'
    target.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(saved,target)
import_texture('illager/evoker.png','fire_mage')

# Import the geometry as well: many pack skins have their own face UV layout.
models=sources.parents[1]/'optifine/cem'
definitions={
 'chronus_mage':('illager/illusioner.png',None,None,'Chronus Mage'),
 'ashen_wraith':('wraith.png','blaze/blaze2.jem','blaze','Ashen Wraith'),
 'bonewing':('blood_phantom.png','phantom/phantom6.jem','phantom','Bonewing'),
 'veilwood_spider':('spider/bogged_cave_spider.png','cave_spider/cave_spider2.jem','spider','Veilwood Spider'),
 'drowned_sentinel':('skeleton/sunken_skeleton.png','skeleton/skeleton4.jem','skeleton','Drowned Sentinel'),
 'cinder_broodmother':('spider/royal_spider.png','spider/spider6.jem','spider','Cinder Broodmother'),
 'lantern_wisp':('ghost.png','phantom/phantom5.jem','phantom','Lantern Wisp'),
 'briar_whisperer':('illager/whisperer.png','evoker/evoker10.jem','illager','Briar Whisperer'),
 'dune_stalker':('spider/relatively_improved_scorpion.png','spider/spider2.jem','spider','Dune Stalker'),
}
for name,(texture,model,base,title) in definitions.items():
    import_texture(texture,name)
    if model:
        saved=archive/'models'/model;saved.parent.mkdir(parents=True,exist_ok=True)
        if (models/model).exists():shutil.copyfile(models/model,saved)
        data=json.loads(saved.read_text(encoding='utf-8'))
        data['visibilityRules']=visibility_rules(data['models'])
        data['models']=[geometry(n) for n in data['models']];data['base']=base
        data['texture']=f'{MOD}:textures/entity/{name}.png'
        write(f'assets/{MOD}/models/entity/{name}.json',data)
    im=Image.new('RGBA',(16,16));draw=ImageDraw.Draw(im)
    colors={'chronus_mage':('#253965','#af8cdb'),'ashen_wraith':('#304d61','#aac7cf'),'bonewing':('#423832','#d4c7b0'),'veilwood_spider':('#354430','#8ca55c'),'drowned_sentinel':('#577255','#c9cbb1'),'cinder_broodmother':('#3c2118','#f1a536'),'lantern_wisp':('#5f6e70','#ecede1'),'briar_whisperer':('#2b6c25','#99b34c'),'dune_stalker':('#614630','#cda973')}
    main,spots=colors[name];draw.ellipse((3,1,12,14),fill=main);draw.rectangle((5,4,7,6),fill=spots);draw.rectangle((9,8,11,10),fill=spots);draw.point((6,12),fill=spots)
    im.save(RES/f'assets/{MOD}/textures/item/{name}_spawn_egg.png');item(name+'_spawn_egg')
    for lang in ['en_us','de_de']:
        path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data[f'entity.{MOD}.{name}']=title;data[f'item.{MOD}.{name}_spawn_egg']=title+' Spawn Egg';write(path,data)

def spawn(name,weight,maximum=1):return {'type':f'{MOD}:{name}','weight':weight,'count':{'type':'minecraft:uniform','min_inclusive':1,'max_inclusive':maximum}}
def add_spawn(biome,name,weight,maximum=1,category='monster'):
    path=f'data/{MOD}/worldgen/biome/{biome}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    entries=data['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category'].setdefault(category,[])
    entries[:]=[e for e in entries if e['type']!=f'{MOD}:{name}'];entries.append(spawn(name,weight,maximum));write(path,data)
add_spawn('veilwood','veilwood_spider',40,2)
add_spawn('water_cave','drowned_sentinel',35)
add_spawn('fire_cave','cinder_broodmother',3)
add_spawn('glowing_cave','lantern_wisp',35,2,'creature')
for biome in ['ash_wastes','charred_forest','cinder_highlands']:
    if (RES/f'data/{MOD}/worldgen/biome/{biome}.json').exists():add_spawn(biome,'ashen_wraith',16)
write(f'data/{MOD}/neoforge/biome_modifier/add_dune_stalkers.json',{'type':'neoforge:add_spawns','biomes':['minecraft:desert'],'spawners':[spawn('dune_stalker',25,2)]})
write(f'data/{MOD}/neoforge/biome_modifier/add_briar_whisperers.json',{'type':'neoforge:add_spawns','biomes':['minecraft:jungle','minecraft:sparse_jungle','minecraft:bamboo_jungle','minecraft:swamp','minecraft:mangrove_swamp'],'spawners':[spawn('briar_whisperer',12)]})
def loot(name,drops):
    pools=[]
    for item_id,lo,hi,chance in drops:
        pool={'rolls':1,'entries':[{'type':'minecraft:item','name':item_id,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]}]}
        if chance<1:pool['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
        pools.append(pool)
    write(f'data/{MOD}/loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
loot('chronus_mage',[(f'{MOD}:ashes',1,2,1),(f'{MOD}:chronus_orb',1,1,.4)])
loot('ashen_wraith',[(f'{MOD}:ashes',1,3,1)])
loot('bonewing',[('minecraft:bone',1,3,1),('minecraft:phantom_membrane',1,1,.15)])
loot('veilwood_spider',[('minecraft:string',1,2,1),('minecraft:spider_eye',1,1,.25)])
loot('drowned_sentinel',[('minecraft:bone',1,2,1),(f'{MOD}:water_core',1,1,.08)])
loot('cinder_broodmother',[('minecraft:string',2,4,1),(f'{MOD}:fire_core',1,1,1)])
loot('lantern_wisp',[])
loot('briar_whisperer',[('minecraft:vine',1,3,1),('minecraft:glow_berries',1,2,.5)])
loot('dune_stalker',[(f'{MOD}:ashes',1,2,1),('minecraft:spider_eye',1,1,.25)])
write(f'assets/{MOD}/mob_texture_sources.json',{'credit':'Barely Default / Mickey Joe; original CEM model credits are preserved in each imported model.','textures':{name:texture for name,(texture,*_) in definitions.items()}|{'fire_mage':'illager/evoker.png'}})
print('Imported ten original mob skins and seven custom geometries; generated biome spawns, eggs and themed loot.')
