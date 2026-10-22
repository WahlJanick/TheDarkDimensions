"""Mummies, vanilla surface terrain, Hard Stone strata and astral portal activation."""
from pathlib import Path
import shutil,json,zipfile

# Earlier generators use the removed biome as a construction template only.
root=Path(__file__).resolve().parent.parent
legacy=root/'src/main/resources/data/thedarkdimensions/worldgen/biome/black_desert.json'
if not legacy.exists():
    legacy.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(root/'tools/templates/legacy_black_desert.json',legacy)
import generate_ash_atmosphere
from generate_resources import RES,MOD,write,recipe,item
from generate_minerals import read

archive=Path(__file__).parent/'texture_sources/mummy_hard_stone'
archive.mkdir(parents=True,exist_ok=True)
supplied=root.parent/'TheDarkDimensions NutzeDieseResourcen!'
for path in supplied.glob('*.png'):
    if path.stem=='mummy' or path.stem.startswith(('stone','coal_ore','copper_ore','iron_ore','gold_ore','redstone_ore','lapis_ore','diamond_ore','emerald_ore')):
        shutil.copyfile(path,archive/path.name)
shutil.copyfile(archive/'mummy.png',RES/f'assets/{MOD}/textures/entity/mummy.png')
ore_kinds=['coal','copper','iron','gold','redstone','lapis','diamond','emerald']
mapping={}
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as client:
    def native(folder,name):return json.loads(client.read(f'data/minecraft/{folder}/{name}.json'))
    def ore_remap(value):
        if isinstance(value,dict):return {k:ore_remap(v) for k,v in value.items()}
        if isinstance(value,list):return [ore_remap(v) for v in value]
        if value=='minecraft:deepslate_ore_replaceables':return f'{MOD}:hard_stone_replaceables'
        if isinstance(value,str) and value.startswith('minecraft:deepslate_'):
            return f'{MOD}:hard_stone_{value.split(":deepslate_",1)[1]}'
        return value
    for name in ['hard_stone']+[f'hard_stone_{kind}_ore' for kind in ore_kinds]:
        source='stone' if name=='hard_stone' else name.removeprefix('hard_stone_')
        variants=[]
        for suffix in ['', '2','3','4']:
            target=f'{name}{suffix}';filename=f'{source}{suffix}.png';mapping[target]=filename
            shutil.copyfile(archive/filename,RES/f'assets/{MOD}/textures/block/{target}.png')
            write(f'assets/{MOD}/models/block/{target}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/{target}'}})
            variants.append({'model':f'{MOD}:block/{target}'})
        write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{'':variants}})
        write(f'assets/{MOD}/models/item/{name}.json',{'parent':f'{MOD}:block/{name}'})
        write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
        if source=='stone':
            loot={'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:hard_stone'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]}
        else:
            loot=native('loot_table/blocks',source)
            def drops(value):
                if isinstance(value,dict):return {k:drops(v) for k,v in value.items()}
                if isinstance(value,list):return [drops(v) for v in value]
                return f'{MOD}:{name}' if value==f'minecraft:{source}' else value
            loot=drops(loot)
        write(f'data/{MOD}/loot_table/blocks/{name}.json',loot)
    selected=['ore_coal_lower','ore_coal_upper','ore_copper','ore_iron_middle','ore_iron_small','ore_iron_upper','ore_gold','ore_gold_lower','ore_redstone','ore_redstone_lower','ore_lapis','ore_lapis_buried','ore_diamond','ore_diamond_buried','ore_diamond_large','ore_diamond_medium','ore_emerald']
    for name in selected:
        placed=native('worldgen/placed_feature',name)
        feature=placed['feature'].split(':')[1]
        write(f'data/{MOD}/worldgen/feature/realm_{feature}.json',ore_remap(native('worldgen/feature',feature)))
        placed['feature']=f'{MOD}:realm_{feature}'
        write(f'data/{MOD}/worldgen/placed_feature/realm_{name}.json',placed)

write(f'assets/{MOD}/hard_stone_texture_sources.json',mapping)
def tag(kind,name,values):
    path=f'data/minecraft/tags/{kind}/{name}.json';data=read(RES/path) if (RES/path).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+values));write(path,data)
blocks=[f'{MOD}:hard_stone']+[f'{MOD}:hard_stone_{k}_ore' for k in ore_kinds]
tag('block','mineable/pickaxe',blocks)
tag('block','needs_stone_tool',[f'{MOD}:hard_stone_{k}_ore' for k in ['copper','iron','lapis']])
tag('block','needs_iron_tool',[f'{MOD}:hard_stone_{k}_ore' for k in ['gold','redstone','diamond','emerald']])
tag('block','blocks_motion_no_leaves',blocks)
write(f'data/{MOD}/tags/block/hard_stone_replaceables.json',{'values':[f'{MOD}:hard_stone']})
write(f'data/{MOD}/tags/block/burned_ore_replaceables.json',{'values':['minecraft:stone',f'{MOD}:hard_stone']})

# Native Overworld surface rules give grass, dirt, gravel, sand and bedrock.
# The underground gradient replaces deepslate with our slower-mining Hard Stone.
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as client:
    underground=json.loads(client.read('data/minecraft/worldgen/material_rule/overworld/underground.json'))
    def hard(value):
        if isinstance(value,dict):return {k:hard(v) for k,v in value.items()}
        if isinstance(value,list):return [hard(v) for v in value]
        return f'{MOD}:hard_stone' if value=='minecraft:deepslate' else value
    write(f'data/{MOD}/worldgen/material_rule/realm_underground.json',hard(underground))
    rule={'type':'minecraft:sequence','sequence':['minecraft:bedrock_floor',{'type':'minecraft:condition','if_true':{'type':'minecraft:above_preliminary_surface'},'then_run':'minecraft:overworld/surface'},f'{MOD}:realm_underground']}
    write(f'data/{MOD}/worldgen/material_rule/burned_surface.json',rule)
noise=read(RES/f'data/{MOD}/worldgen/noise_settings/burned_realm.json')
noise['default_block']='minecraft:stone';write(f'data/{MOD}/worldgen/noise_settings/burned_realm.json',noise)
for name in ['ash_wastes','charred_forest','crystal_hollows','echo_fields','normal_caves','fire_cave','water_cave','glowing_cave']:
    path=f'data/{MOD}/worldgen/biome/{name}.json';biome=read(RES/path)
    biome['effects']['grass_color']='#4d6338'
    biome['effects']['foliage_color']='#56643c'
    biome['effects'].pop('grass_color_modifier',None)
    biome['features'][6]=[f'{MOD}:realm_{n}' for n in selected]+[f'{MOD}:chronus_orb_ore']
    def mummies(value):
        if isinstance(value,dict):return {k:mummies(v) for k,v in value.items()}
        if isinstance(value,list):return [mummies(v) for v in value]
        return f'{MOD}:mummy' if value==f'{MOD}:water_husk' else value
    write(path,mummies(biome))

ashes=read(RES/f'data/{MOD}/loot_table/entities/the_burned.json')
water=read(RES/f'data/{MOD}/loot_table/entities/water_husk.json')['pools'][0]
water['condition']={'type':'minecraft:all_of','terms':[water['condition'],{'type':'minecraft:location_check','predicate':{'biomes':f'{MOD}:water_cave'}}]}
ashes['pools'].append(water)
write(f'data/{MOD}/loot_table/entities/mummy.json',ashes)
write(f'data/{MOD}/neoforge/biome_modifier/add_mummies.json',{'type':'neoforge:add_spawns','biomes':['minecraft:desert'],'spawners':[{'type':f'{MOD}:mummy','weight':100,'count':{'type':'minecraft:uniform','min_inclusive':2,'max_inclusive':4}}]})
write(f'data/{MOD}/neoforge/biome_modifier/remove_desert_husks.json',{'type':'neoforge:remove_spawns','biomes':['minecraft:desert'],'entity_types':['minecraft:husk']})
item('mummy_spawn_egg')
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as client:
    (RES/f'assets/{MOD}/textures/item/mummy_spawn_egg.png').write_bytes(client.read('assets/minecraft/textures/item/husk_spawn_egg.png'))
recipe('astral_crystal_wand',['FE',' S'],{'F':'minecraft:flint_and_steel','E':'minecraft:end_crystal','S':f'{MOD}:reinforced_stick'})
write(f'assets/{MOD}/models/item/astral_crystal_wand.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'minecraft:item/stick','layer1':'minecraft:item/end_crystal'}})
write(f'assets/{MOD}/items/astral_crystal_wand.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/astral_crystal_wand'}})
recipe('dark_acacia_sapling',ingredients=['minecraft:acacia_sapling',f'{MOD}:ashes'])

# Remove the biome and all old Husk-variant resources from the shipped pack.
legacy.unlink(missing_ok=True)
for name in ['the_burned','water_husk']:
    (RES/f'data/{MOD}/loot_table/entities/{name}.json').unlink(missing_ok=True)
    for folder in ['items','models/item','textures/item','textures/entity']:
        suffix='.png' if folder.startswith('textures') else '.json'
        for stem in [name,name+'_baby',name+'_spawn_egg']:(RES/f'assets/{MOD}/{folder}/{stem}{suffix}').unlink(missing_ok=True)
(RES/'data/minecraft/tags/worldgen/biome/is_overworld.json').unlink(missing_ok=True)
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=read(RES/path)
    data={k:v for k,v in data.items() if not any(n in k for n in ['the_burned','water_husk','black_desert'])}
    for name in ['hard_stone']+[f'hard_stone_{k}_ore' for k in ore_kinds]:data[f'block.{MOD}.{name}']=name.replace('_',' ').title()
    data[f'entity.{MOD}.mummy']='Mummy';data[f'item.{MOD}.mummy_spawn_egg']='Mummy Spawn Egg'
    data[f'item.{MOD}.astral_crystal_wand']='Astral Crystal Wand'
    data[f'guide.{MOD}.ashes']='Mummies in Wüsten droppen 1–3 Ashes.' if lang=='de_de' else 'Desert Mummies drop 1–3 Ashes.'
    data[f'guide.{MOD}.fire_core']='Fire Core: Zutat für Elemental Core, Waffen und Magie.' if lang=='de_de' else 'Fire Core: ingredient for Elemental Core, weapons and magic.'
    data[f'guide.{MOD}.astral_crystal_wand']='Flint and Steel neben End Crystal, Improved Stick direkt unter dem End Crystal. Rechtsklick auf einen vollständigen Obsidianrahmen oder ein aktives Netherportal öffnet den Zugang zum Burned Realm. 64 Aktivierungen Haltbarkeit.' if lang=='de_de' else 'Flint and Steel beside End Crystal, Improved Stick directly below the crystal. Right-click a complete obsidian frame or active Nether Portal to open the Burned Realm. Durability: 64 activations.'
    write(path,data)
test=Path('src/gametest/resources/data/minecraft/worldgen/world_preset/flat_all_dimensions.json');preset=read(test)
for name in ['burned_realm','echo_depths']:preset['dimensions'][f'{MOD}:{name}']=read(RES/f'data/{MOD}/dimension/{name}.json')
test.write_text(json.dumps(preset,indent=2)+'\n',encoding='utf-8')
print('Replaced Husk variants and Black Desert; generated Mummies, vanilla terrain, Hard Stone ore variants and Astral Crystal Wand.')
