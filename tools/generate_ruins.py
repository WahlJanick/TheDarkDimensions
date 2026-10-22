"""Mummy ruins, separate upgraded rooms and native key/anchor assets."""
import generate_ashfall
from generate_resources import RES,MOD,ROOT,write,recipe,item
from generate_caves import S,I,F,D,B,C,L,payload
from PIL import Image
import json,zipfile,gzip,io,colorsys,copy

# Keep the existing key sprite's geometry, alpha and original dimensions.
source=Image.open(RES/f'assets/{MOD}/textures/item/pocket_dimension_key.png').convert('RGBA')
for name,purple in [('foreign_key',True),('improved_pocket_dimension_key',False)]:
 image=source.copy();pixels=[]
 for r,g,b,a in image.get_flattened_data():
  h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
  if a>0 and 0.05<=h<=0.19 and s>0.35 and r>40:
   if purple:r,g,b=round(v*185),round(v*105),round(v*255)
   else:r,g,b=round(v*112),round(v*99),round(v*108)
  pixels.append((r,g,b,a))
 image.putdata(pixels);image.save(RES/f'assets/{MOD}/textures/item/{name}.png');item(name)
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as z:
 template=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/netherite_upgrade_smithing_template.png'))).convert('RGBA')
 # A violet dimensional rune on the familiar slate template backing.
 for y in range(4,11):
  for x in range(5,11):
   if x in (5,10) or y in (4,10):template.putpixel((x,y),(166,109,226,255))
 template.putpixel((8,7),(234,206,255,255));template.save(RES/f'assets/{MOD}/textures/item/pocket_key_upgrade_template.png');item('pocket_key_upgrade_template')
 papyrus=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/paper.png'))).convert('RGBA')
 for y in range(papyrus.height):
  for x in range(papyrus.width):
   r,g,b,a=papyrus.getpixel((x,y))
   if a:papyrus.putpixel((x,y),(min(255,round(r*1.0)),round(g*.86),round(b*.62),a))
 papyrus.save(RES/f'assets/{MOD}/textures/item/weathered_papyrus.png');item('weathered_papyrus')
recipe('foreign_key',[' I ',' I ','GEG'],{'I':'minecraft:iron_ingot','G':'minecraft:gold_ingot','E':'minecraft:ender_pearl'})
recipe('return_anchor',[' O ','OEO',' O '],{'O':'minecraft:obsidian','E':'minecraft:ender_pearl'})
write(f'data/{MOD}/recipe/improved_pocket_dimension_key.json',{'type':'minecraft:smithing_transform','template':f'{MOD}:pocket_key_upgrade_template','base':f'{MOD}:pocket_dimension_key','addition':'minecraft:netherite_block','result':{'id':f'{MOD}:improved_pocket_dimension_key'}})
write(f'data/{MOD}/advancement/recipes/improved_pocket_dimension_key.json',{'parent':'minecraft:recipes/root','criteria':{'has_template':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':f'{MOD}:pocket_key_upgrade_template'}]}}},'requirements':[['has_template']],'rewards':{'recipes':[f'{MOD}:improved_pocket_dimension_key']}})

# A compact violet crystal pedestal uses original vanilla materials.
def cuboid(low,high,texture):return {'from':low,'to':high,'faces':{face:{'texture':'#'+texture} for face in ['north','south','east','west','up','down']}}
write(f'assets/{MOD}/models/block/return_anchor.json',{'parent':'minecraft:block/block','textures':{'particle':'minecraft:block/crying_obsidian','base':'minecraft:block/obsidian','rune':'minecraft:block/crying_obsidian','crystal':'minecraft:block/amethyst_block'},'elements':[cuboid([0,0,0],[16,4,16],'base'),cuboid([5,4,5],[11,10,11],'rune'),cuboid([4,10,4],[12,16,12],'crystal')]})
write(f'assets/{MOD}/blockstates/return_anchor.json',{'variants':{'':{'model':f'{MOD}:block/return_anchor'}}})
write(f'assets/{MOD}/models/item/return_anchor.json',{'parent':f'{MOD}:block/return_anchor'})
write(f'assets/{MOD}/items/return_anchor.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/return_anchor'}})
write(f'data/{MOD}/loot_table/blocks/return_anchor.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:return_anchor'}],'condition':{'type':'minecraft:survives_explosion'}}]})
for tag in ['mineable/pickaxe','needs_diamond_tool']:
 path=f'data/minecraft/tags/block/{tag}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data['values'].append(f'{MOD}:return_anchor');write(path,data)
# Every owner gets an independent allocation in a second void level. Never overwrite the 10-cube dimension.
dimension=copy.deepcopy(json.loads((RES/f'data/{MOD}/dimension/pocket_dimension.json').read_text(encoding='utf-8')))
dimension['type']=f'{MOD}:large_pocket_dimension'
write(f'data/{MOD}/dimension/large_pocket_dimension.json',dimension)
dtype=json.loads((RES/f'data/{MOD}/dimension_type/pocket_dimension.json').read_text(encoding='utf-8'))
dtype['attributes']['minecraft:visual/sky_color']='#b9aacd';dtype['attributes']['minecraft:visual/fog_color']='#b9aacd'
write(f'data/{MOD}/dimension_type/large_pocket_dimension.json',dtype)
path=ROOT/'src/gametest/resources/data/minecraft/worldgen/world_preset/flat_all_dimensions.json'
preset=json.loads(path.read_text(encoding='utf-8'));preset['dimensions'][f'{MOD}:large_pocket_dimension']=dimension;path.write_text(json.dumps(preset,indent=2)+'\n',encoding='utf-8')

write(f'data/{MOD}/tags/worldgen/biome/has_mummy_ruins.json',{'replace':False,'values':['minecraft:desert']})
write(f'data/{MOD}/worldgen/structure/mummy_ruins.json',{'type':'minecraft:jigsaw','biomes':f'#{MOD}:has_mummy_ruins','start_pool':f'{MOD}:mummy_ruins','size':1,'start_height':{'absolute':-5},'project_start_to_heightmap':'WORLD_SURFACE_WG','max_distance_from_center':32,'step':'surface_structures','terrain_adaptation':'none','use_expansion_hack':False,'spawn_overrides':{}})
write(f'data/{MOD}/worldgen/template_pool/mummy_ruins.json',{'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':f'{MOD}:mummy_ruins','processors':{'processors':[]},'projection':'rigid'}}]})
write(f'data/{MOD}/worldgen/structure_set/mummy_ruins.json',{'structures':[{'structure':f'{MOD}:mummy_ruins','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':28,'separation':10,'salt':263019}})
loot={'type':'minecraft:chest','pools':[
 {'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:ashes','modifier':[{'type':'minecraft:set_count','count':{'type':'minecraft:uniform','min':4,'max':12}}]}]},
 {'rolls':{'type':'minecraft:uniform','min':2,'max':4},'entries':[{'type':'minecraft:item','name':name,'weight':weight,'modifier':[{'type':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':count}}]} for name,weight,count in [('minecraft:bone',8,6),('minecraft:rotten_flesh',6,5),('minecraft:gold_nugget',5,8),('minecraft:iron_ingot',3,2)]]},
 {'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:weathered_papyrus'}]},
 {'rolls':1,'condition':{'type':'minecraft:random_chance','chance':0.05},'entries':[{'type':'minecraft:item','name':f'{MOD}:solar_essence'}]},
 {'rolls':1,'condition':{'type':'minecraft:random_chance','chance':0.03},'entries':[{'type':'minecraft:item','name':f'{MOD}:pocket_key_upgrade_template'}]}]}
write(f'data/{MOD}/loot_table/chests/mummy_ruins.json',loot)

# An eleven-block sandstone burial chamber; staircase/arch reveal it above the sand.
palette=[];indices={};blocks={}
def put(x,y,z,name,properties=None,nbt=None):
 key=(name,tuple(sorted((properties or {}).items())))
 if key not in indices:
  indices[key]=len(palette);state={'id':S(name)}
  if properties:state['properties']=C({k:S(v) for k,v in properties.items()})
  palette.append(state)
 block={'pos':L(3,[x,y,z]),'state':I(indices[key])}
 if nbt:block['nbt']=C(nbt)
 blocks[(x,y,z)]=block
for x in range(11):
 for z in range(11):
  for y in range(6):
   wall=y in [0,5] or x in [0,10] or z in [0,10]
   name='minecraft:cut_sandstone' if wall else 'minecraft:air'
   if wall and (x+z+y)%7==0:name='minecraft:chiseled_sandstone'
   if wall and y==0 and (x+z)%5==0:name='minecraft:orange_terracotta'
   put(x,y,z,name)
# A safe obvious route exists around both pressure plates.
for z in range(5):
 step=5-z
 for x in [4,5,6]:
  put(x,step,z,'minecraft:sandstone_stairs',{'facing':'north','half':'bottom','shape':'straight','waterlogged':'false'})
  for y in range(step+1,9):put(x,y,z,'minecraft:air')
for x in [3,7]:
 for y in range(5,8):put(x,y,0,'minecraft:chiseled_sandstone')
for x in range(3,8):put(x,8,0,'minecraft:cut_sandstone')
# Fallen sand partially covers the old burial niches.
for x,z in [(1,2),(1,3),(9,2),(9,3),(2,9),(8,9)]:put(x,1,z,'minecraft:sand')
for x in [2,8]:
 put(x,1,8,'minecraft:chest',{'facing':'north','type':'single','waterlogged':'false'},{'id':S('minecraft:chest'),'LootTable':S(f'{MOD}:chests/mummy_ruins')})
# One explosive trap and an armed arrow trap, both visibly avoidable.
put(7,0,6,'minecraft:tnt');put(7,1,6,'minecraft:stone_pressure_plate',{'powered':'false'})
put(1,1,6,'minecraft:dispenser',{'facing':'east','triggered':'false'},{'id':S('minecraft:dispenser'),'Items':L(10,[{'Slot':B(0),'id':S('minecraft:arrow'),'count':I(8)}])})
put(2,1,6,'minecraft:redstone_wire',{'north':'none','east':'side','south':'none','west':'side','power':'0'})
put(3,1,6,'minecraft:stone_pressure_plate',{'powered':'false'})
for x in [2,8]:put(x,3,9,'minecraft:wall_torch',{'facing':'north'})
entities=[]
for x in [4,6]:entities.append({'pos':L(6,[x+0.5,1.0,8.5]),'blockPos':L(3,[x,1,8]),'nbt':C({'id':S(f'{MOD}:mummy'),'PersistenceRequired':B(1),'Health':F(20),'Pos':L(6,[x+0.5,1.0,8.5]),'Rotation':L(5,[180,0])})})
template=C({'DataVersion':I(5023),'size':L(3,[11,9,11]),'palette':L(10,palette),'blocks':L(10,list(blocks.values())),'entities':L(10,entities)})
path=RES/f'data/{MOD}/structure/mummy_ruins.nbt';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(gzip.compress(b'\x0a\x00\x00'+payload(template),mtime=0))

for lang in ['en_us','de_de']:
 path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
 names={'pocket_key_upgrade_template':('Pocket Key Upgrade Template','Pocket-Key-Upgrade-Vorlage'),'improved_pocket_dimension_key':('Improved Pocket Dimension Key','Verbesserter Pocket Dimension Key'),'foreign_key':('Foreign Key','Foreign Key'),'weathered_papyrus':('Weathered Papyrus','Verwitterter Papyrus')}
 for name,labels in names.items():data[f'item.{MOD}.{name}']=labels[lang=='de_de']
 data[f'block.{MOD}.return_anchor']='Rückkehr-Anker' if lang=='de_de' else 'Return Anchor'
 messages={
  'foreign_unlinked':('Link this key to a Return Anchor inside a pocket room.','Verknüpfe den Key mit einem Rückkehr-Anker in einem Pocket-Raum.'),
  'foreign_missing':('The linked pocket room no longer exists.','Der verknüpfte Pocket-Raum existiert nicht mehr.'),
  'anchor_outside':('This anchor only works inside a pocket room.','Dieser Anker funktioniert nur in einem Pocket-Raum.'),
  'foreign_bound':('Foreign Key linked to this %s×%s×%s room.','Foreign Key mit diesem %s×%s×%s-Raum verknüpft.')}
 # One size argument is repeated using indexed format placeholders.
 messages['foreign_bound']=('Foreign Key linked to this %1$s×%1$s×%1$s room.','Foreign Key mit diesem %1$s×%1$s×%1$s-Raum verknüpft.')
 for name,labels in messages.items():data[f'message.{MOD}.{name}']=labels[lang=='de_de']
 data[f'tooltip.{MOD}.foreign_unlinked']='Unverknüpft · am Anker binden' if lang=='de_de' else 'Unlinked · bind at an anchor'
 data[f'tooltip.{MOD}.foreign_linked']='Verknüpft: %1$s×%1$s×%1$s' if lang=='de_de' else 'Linked: %1$s×%1$s×%1$s'
 hints=[('Ash reveals the path beyond obsidian.','Asche verrät den Weg hinter dem Obsidian.'),('Water tames a burning handle.','Wasser bändigt einen brennenden Griff.'),('Flint and steel, an end crystal and an Improved Stick...','Feuerzeug, Endkristall und Improved Stick...'),('...form the wand that awakens the portal.','...formen den Stab, der das Portal erweckt.')]
 for n,labels in enumerate(hints,1):data[f'hint.{MOD}.papyrus_{n}']=labels[lang=='de_de']
 write(path,data)
print('Generated buried Mummy ruins, traps and loot; anchor, foreign and upgraded keys; separate 20-cube dimension.')
