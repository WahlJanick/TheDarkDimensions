"""Chapter four: elemental caves, inhabitants, cores and a discoverable jigsaw shrine."""
import generate_upgrades
from generate_resources import RES,MOD,write,item,recipe
from pathlib import Path
import json,copy,shutil,struct,gzip,zipfile
SOURCE=Path(__file__).parent/'texture_sources'
blocks=['fire_cave_stone','fire_crystal','water_cave_stone','water_crystal','glowing_cave_stone','glowing_honey_block','spell_rune_stone','glowing_crystal']
for name in blocks:
    write(f'assets/{MOD}/models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/{name}'}})
    write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{'':{'model':f'{MOD}:block/{name}'}}})
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':f'{MOD}:block/{name}'})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
    write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for tag in ['mineable/pickaxe']:
    p=f'data/minecraft/tags/block/{tag}.json';data=json.loads((RES/p).read_text());data['values'] += [f'{MOD}:{name}' for name in blocks if name!='glowing_honey_block'];write(p,data)
for name in ['water_core','lightning_core','spell_core','glowing_honey','fire_mage_spawn_egg','water_husk_spawn_egg','glowing_bee_spawn_egg']:item(name)
# Exact vanilla cluster pixels/geometry with runtime colour filters.
for name,color in [('fire_crystal',0xff9d12),('water_crystal',0x448fff),('glowing_crystal',0xffff18)]:
    write(f'assets/{MOD}/models/block/{name}.json',{'parent':'minecraft:block/tinted_cross','textures':{'cross':'minecraft:block/amethyst_cluster'}})
    rotations={'up':{},'down':{'x':180},'north':{'x':90},'south':{'x':90,'y':180},'east':{'x':90,'y':90},'west':{'x':90,'y':270}}
    write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{f'facing={facing}':{'model':f'{MOD}:block/{name}',**rotation} for facing,rotation in rotations.items()}})
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:block/amethyst_cluster'}})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':color}]}})
recipe('lightning_core',['L','E'],{'L':'minecraft:lightning_rod','E':f'{MOD}:improved_echo_shard'})
recipe('spell_core',ingredients=[f'{MOD}:lightning_core']*2+[f'{MOD}:fire_core']*2+[f'{MOD}:water_core']*2)
for mob,core,chance in [('fire_mage','fire_core',0.1),('water_husk','water_core',0.15)]:
    write(f'data/{MOD}/loot_table/entities/{mob}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:{core}'}],'conditions':[{'condition':'minecraft:random_chance','chance':chance}]},{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:ashes' if mob=='fire_mage' else 'minecraft:rotten_flesh'}]}]})
write(f'data/{MOD}/loot_table/entities/glowing_bee.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:glowing_honey'}]}]})

dimension_path=f'data/{MOD}/dimension/burned_realm.json';dimension=json.loads((RES/dimension_path).read_text())
entries=dimension['generator']['biome_source']['biomes']
for surface in ['ash_wastes','charred_forest','crystal_hollows']:
    p=f'data/{MOD}/worldgen/biome/{surface}.json';b=json.loads((RES/p).read_text());b['carvers']=['minecraft:cave','minecraft:cave_extra_underground','minecraft:canyon'];write(p,b)
for entry in entries:entry['parameters']['depth']=[-1,0.3499]
for kind,humidity in [('fire',-0.8),('water',0),('glowing',0.8)]:
    entry=copy.deepcopy(entries[0]);entry['biome']=f'{MOD}:{kind}_cave';entry['parameters']['humidity']=humidity;entry['parameters']['depth']=[0.35,2.0];entries.append(entry)
    biome=copy.deepcopy(json.loads((RES/f'data/{MOD}/worldgen/biome/ash_wastes.json').read_text()))
    biome['carvers']=['minecraft:cave','minecraft:cave_extra_underground','minecraft:canyon']
    biome['features'][4]=[]
    biome['features'][9]=[f'{MOD}:{kind}_cave_chambers']
    attrs=biome['attributes'];attrs['minecraft:visual/fog_color']={'fire':'#39261e','water':'#1c3444','glowing':'#46412c'}[kind]
    mob={'fire':'fire_mage','water':'water_husk','glowing':'glowing_bee'}[kind]
    attrs['minecraft:gameplay/natural_mob_spawns']['argument']={'spawn_costs':{},'spawns_by_category':{('creature' if kind=='glowing' else 'monster'):[{'type':f'{MOD}:{mob}','weight':100,'count':{'type':'minecraft:uniform','min_inclusive':1,'max_inclusive':2}}]}}
    write(f'data/{MOD}/worldgen/biome/{kind}_cave.json',biome)
    write(f'data/{MOD}/worldgen/feature/{kind}_cave.json',{'type':f'{MOD}:elemental_cave','kind':kind})
    write(f'data/{MOD}/worldgen/placed_feature/{kind}_cave_chambers.json',{'feature':f'{MOD}:{kind}_cave','placement':[{'type':'minecraft:count','count':3},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':-48},'max_inclusive':{'absolute':28}}},{'type':'minecraft:biome'}]})
write(dimension_path,dimension)
# GameTestServer ignores standalone dimensions, so keep its test-only preset in step.
test=Path('src/gametest/resources/data/minecraft/worldgen/world_preset/flat_all_dimensions.json')
data=json.loads(test.read_text());data['dimensions'][f'{MOD}:burned_realm']=dimension;test.write_text(json.dumps(data,indent=2)+'\n')

write(f'data/{MOD}/tags/worldgen/biome/has_elemental_shrine.json',{'values':[f'{MOD}:{n}' for n in ['ash_wastes','charred_forest','crystal_hollows']]})
write(f'data/{MOD}/worldgen/structure/elemental_shrine.json',{'type':'minecraft:jigsaw','biomes':f'#{MOD}:has_elemental_shrine','start_pool':f'{MOD}:elemental_shrine','size':1,'start_height':{'absolute':0},'project_start_to_heightmap':'WORLD_SURFACE_WG','max_distance_from_center':32,'step':'surface_structures','terrain_adaptation':'beard_thin','use_expansion_hack':False,'spawn_overrides':{}})
write(f'data/{MOD}/worldgen/template_pool/elemental_shrine.json',{'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':f'{MOD}:elemental_shrine','processors':{'processors':[]},'projection':'rigid'}}]})
write(f'data/{MOD}/worldgen/structure_set/elemental_shrines.json',{'structures':[{'structure':f'{MOD}:elemental_shrine','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':24,'separation':10,'salt':263041}})
write(f'data/{MOD}/loot_table/chests/elemental_shrine.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3,'max':5},'entries':[{'type':'minecraft:item','name':f'{MOD}:{name}','weight':weight} for name,weight in [('glowing_honey',6),('improved_amethyst',5),('water_core',1),('fire_core',1),('improving_template',2)]]}]})

# Minimal safe NBT writer; no object deserialization or external NBT dependency.
S=lambda s:(8,s);I=lambda n:(3,n);F=lambda n:(5,n);D=lambda n:(6,n);B=lambda n:(1,n);C=lambda d:(10,d);L=lambda t,v:(9,(t,v))
def utf(s):raw=s.encode();return struct.pack('>H',len(raw))+raw
def payload(tag):
    t,v=tag
    if t in [1,3,5,6]:return struct.pack({1:'>b',3:'>i',5:'>f',6:'>d'}[t],v)
    if t==8:return utf(v)
    if t==9:
        sub,values=v;return struct.pack('>bi',sub,len(values))+b''.join(payload((sub,val)) for val in values)
    if t==10:return b''.join(bytes([val[0]])+utf(key)+payload(val) for key,val in v.items())+b'\x00'
    raise ValueError(t)
palette=[];palette_index={};placed=[]
def put(x,y,z,name,nbt=None):
    if name not in palette_index:palette_index[name]=len(palette);palette.append({'id':S(name)})
    block={'pos':L(3,[x,y,z]),'state':I(palette_index[name])}
    if nbt:block['nbt']=C(nbt)
    placed.append(block)
for x in range(9):
    for z in range(9):
        for y in range(6):
            name='minecraft:stone_bricks' if y==0 else 'minecraft:air'
            if y>0 and x in [0,8] and z in [0,8]:name=f'{MOD}:spell_rune_stone'
            if y==4 and (x in [0,8] or z in [0,8]):name='minecraft:stone_bricks'
            if (x,y,z) in [(2,1,4),(4,1,4),(6,1,4)]:name=f'{MOD}:spell_rune_stone'
            if (x,y,z)==(2,2,4):name=f'{MOD}:fire_crystal'
            if (x,y,z)==(4,2,4):name=f'{MOD}:water_crystal'
            if (x,y,z)==(6,2,4):name=f'{MOD}:glowing_crystal'
            if (x,y,z)==(4,1,7):
                put(x,y,z,'minecraft:chest',{'id':S('minecraft:chest'),'LootTable':S(f'{MOD}:chests/elemental_shrine')})
            else:put(x,y,z,name)
version=5023 # Minecraft 26.3 structure data version
entity={'pos':L(6,[7.5,1.0,4.5]),'blockPos':L(3,[7,1,4]),'nbt':C({'id':S(f'{MOD}:fire_mage'),'PersistenceRequired':B(1),'Health':F(26),'Pos':L(6,[7.5,1.0,4.5]),'Rotation':L(5,[0,0])})}
template=C({'DataVersion':I(version),'size':L(3,[9,6,9]),'palette':L(10,palette),'blocks':L(10,placed),'entities':L(10,[entity])})
p=RES/f'data/{MOD}/structure/elemental_shrine.nbt';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(gzip.compress(b'\x0a\x00\x00'+payload(template),mtime=0))
for folder,path in [('blocks','block'),('items','item'),('entities','entity')]:
    for source in (SOURCE/folder).glob('*.png'):
        dest=RES/f'assets/{MOD}/textures/{path}/{source.name}';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,dest)
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/p).read_text(encoding='utf-8'))
    for name in blocks:data[f'block.{MOD}.{name}']=name.replace('_',' ').title()
    for name in ['water_core','lightning_core','spell_core','glowing_honey','fire_mage_spawn_egg','water_husk_spawn_egg','glowing_bee_spawn_egg']:data[f'item.{MOD}.{name}']=name.replace('_',' ').title()
    data[f'item.{MOD}.spell_core']='Elemental Core'
    for name in ['fire_mage','water_husk','glowing_bee']:data[f'entity.{MOD}.{name}']=name.replace('_',' ').title()
    for name in ['fire_cave','water_cave','glowing_cave']:data[f'biome.{MOD}.{name}']=name.replace('_',' ').title()
    data[f'guide.{MOD}.glowing_honey']=('Von Glowing Bees: mit einer Glasflasche ernten (30 Sekunden Pause) oder als Drop. Trinken gibt 60 Sekunden Glowing; die Glasflasche bleibt erhalten.' if lang=='de_de' else 'Harvest Glowing Bees with a glass bottle (30 second cooldown), or obtain as a drop. Drink for 60 seconds of Glowing; the glass bottle is returned.')
    data[f'guide.{MOD}.spell_core']='2 Lightning Cores + 2 Fire Cores + 2 Water Cores. Shapeless crafting; saved for future spells.'
    write(p,data)
print('Generated elemental cave biomes, chambers, shrine, loot and core recipes.')
