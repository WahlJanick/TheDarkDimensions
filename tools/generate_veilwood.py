"""Bounded dense forests, byte-preserved Watcher art and a native-sized evil emerald icon."""
import generate_trinkets
from generate_resources import RES,MOD,write,item
from pathlib import Path
from PIL import Image,ImageDraw
import json,shutil

removed={'burned_hoglin','burned_pigman'}
def clean(value):
 if isinstance(value,dict):
  return {k:clean(v) for k,v in value.items() if not any(k.endswith('.'+n) or k.endswith('.'+n+'_spawn_egg') for n in removed)}
 if isinstance(value,list):
  return [clean(v) for v in value if not (isinstance(v,dict) and v.get('type') in {f'{MOD}:{n}' for n in removed}) and not (isinstance(v,str) and v in {f'{MOD}:{n}' for n in removed})]
 return value
for p in RES.rglob('*'):
 if not p.is_file():continue
 if any(p.stem==n or p.stem.startswith(n+'_') for n in removed):p.unlink();continue
 if p.suffix=='.json':
  old=json.loads(p.read_text(encoding='utf-8'));new=clean(old)
  if new!=old:write(p.relative_to(RES).as_posix(),new)

archive=Path(__file__).parent/'texture_sources/veilwood';archive.mkdir(parents=True,exist_ok=True)
source=Path(__file__).resolve().parents[2]/'TheDarkDimensions NutzeDieseResourcen!'
for original,name in [('enderman.png','hollow_watcher'),('enderman_eyes.png','hollow_watcher_eyes')]:
 p=source/'The new mob/textures/entity/enderman'/original;backup=archive/original
 if p.exists():shutil.copyfile(p,backup)
 target=RES/f'assets/{MOD}/textures/entity/{name}.png';target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(backup,target)

path=f'data/{MOD}/worldgen/biome/veilwood.json'
biome=json.loads((RES/f'data/{MOD}/worldgen/biome/charred_forest.json').read_text(encoding='utf-8'))
biome['attributes'].update({'minecraft:visual/sky_color':'#121a1d','minecraft:visual/fog_color':'#172326','minecraft:visual/fog_start_distance':3.0,'minecraft:visual/fog_end_distance':24.0,'minecraft:visual/sky_fog_end_distance':32.0,'minecraft:visual/sky_light_factor':.06,'minecraft:visual/ambient_light_color':'#303a3d','minecraft:visual/ambient_particles':[{'particle':{'type':'minecraft:white_ash'},'probability':.003}]})
biome['effects']['grass_color']='#354b3b';biome['effects']['foliage_color']='#243d2d'
biome['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']={'spawn_costs':{},'spawns_by_category':{}}
biome['features'][2]=[];biome['features'][4]=[]
biome['features'][9]=[f for f in biome['features'][9] if f!=f'{MOD}:burned_trees']+[f'{MOD}:veilwood_trees']
write(path,biome)
write(f'data/{MOD}/worldgen/feature/veilwood_tree.json',{'type':f'{MOD}:veilwood_tree'})
write(f'data/{MOD}/worldgen/placed_feature/veilwood_trees.json',{'feature':f'{MOD}:veilwood_tree','placement':[{'type':'minecraft:count','count':7},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
p=f'data/{MOD}/dimension/burned_realm.json';dimension=json.loads((RES/p).read_text(encoding='utf-8'))
dimension['generator']['biome_source']={'type':f'{MOD}:veilwood','delegate':dimension['generator']['biome_source'],'forest':f'{MOD}:veilwood'}
write(p,dimension)
write(f'data/{MOD}/loot_table/entities/hollow_watcher.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:ashes','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':2,'max':4}}]}]},{'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.25}],'entries':[{'type':'minecraft:item','name':f'{MOD}:chronus_orb'}]}]})

# Existing mod icons use explicit pixel shapes, crisp edges and the native effect raster.
icon=Image.new('RGBA',(18,18));d=ImageDraw.Draw(icon)
d.polygon([(5,2),(12,2),(16,7),(16,10),(9,16),(8,16),(1,10),(1,7)],fill='#0c1e15')
d.polygon([(5,3),(12,3),(15,7),(15,9),(9,15),(8,15),(2,9),(2,7)],fill='#16834a')
d.polygon([(5,4),(8,4),(6,7),(3,7)],fill='#79d78b');d.polygon([(9,4),(12,4),(14,7),(10,7)],fill='#38b763')
d.polygon([(3,10),(6,9),(8,14)],fill='#104e2c');d.polygon([(10,9),(14,10),(9,14)],fill='#20ad60')
d.line([(3,1),(4,4),(6,4)],fill='#172b1a',width=2);d.line([(14,1),(13,4),(11,4)],fill='#172b1a',width=2)
d.line([(4,7),(7,8)],fill='#092116',width=2);d.line([(10,8),(13,7)],fill='#092116',width=2)
d.point((6,8),fill='#e85763');d.point((11,8),fill='#e85763');d.line([(6,11),(8,12),(10,12),(12,10)],fill='#07170e');d.point((6,4),fill='#b8f6c8')
icon.save(RES/f'assets/{MOD}/textures/mob_effect/devils_charm.png')
egg=Image.new('RGBA',(16,16));d=ImageDraw.Draw(egg);d.ellipse((3,1,12,14),fill='#101315');d.rectangle((5,4,10,8),fill='#e4e4df');d.rectangle((6,8,9,10),fill='#e4e4df');d.point((7,7),fill='#111417');d.point((9,7),fill='#111417');egg.save(RES/f'assets/{MOD}/textures/item/hollow_watcher_spawn_egg.png');item('hollow_watcher_spawn_egg')
for lang in ['de_de','en_us']:
 p=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/p).read_text(encoding='utf-8'));data[f'biome.{MOD}.veilwood']='Veilwood';data[f'entity.{MOD}.hollow_watcher']='The Hollow Watcher';data[f'item.{MOD}.hollow_watcher_spawn_egg']='Hollow Watcher Spawn Egg';write(p,data)
print('Generated Veilwood forests and native fog, removed burned mobs, preserved original Watcher textures and drew evil emerald effect icon.')
