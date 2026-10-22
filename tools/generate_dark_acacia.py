"""Import Dark Acacia artwork and native wood-family models after stick progression."""
import generate_sticks
from generate_resources import RES, MOD, write, recipe
from generate_minerals import read
from pathlib import Path
import shutil, zipfile

archive=Path(__file__).parent/'texture_sources/user_resources'
supplied=RES.parents[3]/'TheDarkDimensions NutzeDieseResourcen!'
if supplied.exists():
    for path in supplied.glob('*acacia*.png'):shutil.copyfile(path,archive/path.name)
textures={p.stem:p.stem.replace('acacia','dark_acacia') for p in archive.glob('*acacia*.png')}
for source,target in textures.items():
    shutil.copyfile(archive/f'{source}.png',RES/f'assets/{MOD}/textures/block/{target}.png')
names={'acacia_log':'burned_log','acacia_planks':'burned_planks'}
for name in ['wood','stairs','slab','door','trapdoor','sapling']:names[f'acacia_{name}']=f'dark_acacia_{name}'
for name in ['log','wood']:names[f'stripped_acacia_{name}']=f'stripped_dark_acacia_{name}'
model_names={}
def remap(value,model_refs=False):
    if isinstance(value,dict):return {k:remap(v,model_refs) for k,v in value.items()}
    if isinstance(value,list):return [remap(v,model_refs) for v in value]
    if isinstance(value,str):
        for source,target in names.items():
            if value==f'minecraft:{source}':return f'{MOD}:{target}'
        if value.startswith('minecraft:block/'):
            source=value.split('/')[-1]
            if model_refs and source in model_names:return f'{MOD}:block/{model_names[source]}'
            if source in textures:return f'{MOD}:block/{textures[source]}'
    return value
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as client:
    models=[n for n in client.namelist() if n.startswith('assets/minecraft/models/block/') and n.endswith('.json') and n.split('/')[-1].startswith(('acacia_','stripped_acacia_'))]
    model_names={Path(n).stem:names.get(Path(n).stem,Path(n).stem.replace('acacia','dark_acacia')) for n in models}
    import json
    for path in models:
        data=json.loads(client.read(path));target=model_names[Path(path).stem]
        # Distinguish a model reference from an identically named texture.
        parent=data.get('parent','')
        if parent.startswith('minecraft:block/') and parent.split('/')[-1] in model_names:
            data['parent']=f'{MOD}:block/{model_names[parent.split("/")[-1]]}'
        write(f'assets/{MOD}/models/block/{target}.json',remap(data))
    for source,target in names.items():
        state=remap(json.loads(client.read(f'assets/minecraft/blockstates/{source}.json')),True)
        if source=='acacia_slab':state['variants']['type=double']={'model':f'{MOD}:block/dark_acacia_slab_double'}
        write(f'assets/{MOD}/blockstates/{target}.json',state)
        model='dark_acacia_trapdoor_bottom' if source=='acacia_trapdoor' else target
        write(f'assets/{MOD}/models/item/{target}.json',{'parent':f'{MOD}:block/{model}'})
        write(f'assets/{MOD}/items/{target}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{target}'}})
        # Native loot preserves double-slab quantities and upper/lower door checks.
        write(f'data/{MOD}/loot_table/blocks/{target}.json',remap(json.loads(client.read(f'data/minecraft/loot_table/blocks/{source}.json'))))
    for name,texture in [('dark_acacia_door','dark_acacia_door_bottom'),('dark_acacia_sapling','dark_acacia_sapling')]:
        write(f'assets/{MOD}/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'{MOD}:block/{texture}'}})
    leaves=remap(json.loads(client.read('data/minecraft/loot_table/blocks/acacia_leaves.json')))
    def leaf_remap(v):
        if isinstance(v,dict):return {k:leaf_remap(c) for k,c in v.items()}
        if isinstance(v,list):return [leaf_remap(c) for c in v]
        return f'{MOD}:burned_leaves' if v=='minecraft:acacia_leaves' else v
    write(f'data/{MOD}/loot_table/blocks/burned_leaves.json',leaf_remap(leaves))

# Eight supplied plank designs form deterministic, position-dependent model variants.
variants=[]
for suffix in ['']+[f'_{i:02}' for i in range(1,8)]:
    name=f'burned_planks{suffix}'
    write(f'assets/{MOD}/models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/dark_acacia_planks{suffix}'}})
    variants.append({'model':f'{MOD}:block/{name}'})
write(f'assets/{MOD}/blockstates/burned_planks.json',{'variants':{'':variants}})
write(f'assets/{MOD}/models/block/dark_acacia_slab_double.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/dark_acacia_double'}})
for prefix in ['carved','half_carved']:
    name=f'{prefix}_dark_acacia_log'
    write(f'assets/{MOD}/models/block/{name}.json',{'parent':'minecraft:block/cube_column','textures':{'side':f'{MOD}:block/{name}2','end':f'{MOD}:block/{name}'}})
    write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{'':{'model':f'{MOD}:block/{name}'}}})
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':f'{MOD}:block/{name}'})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
    write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    recipe(name,ingredients=[f'{MOD}:burned_log','minecraft:flint'] if prefix=='carved' else [f'{MOD}:carved_dark_acacia_log'])
planks=f'{MOD}:burned_planks'
recipe('dark_acacia_stairs',['P  ','PP ','PPP'],{'P':planks},count=4)
recipe('dark_acacia_slab',['PPP'],{'P':planks},count=6)
recipe('dark_acacia_door',['PP','PP','PP'],{'P':planks},count=3)
recipe('dark_acacia_trapdoor',['PPP','PPP'],{'P':planks},count=2)
recipe('dark_acacia_wood',['LL','LL'],{'L':f'{MOD}:burned_log'},count=3)
recipe('stripped_dark_acacia_wood',['LL','LL'],{'L':f'{MOD}:stripped_dark_acacia_log'},count=3)
for name in ['dark_acacia_wood','stripped_dark_acacia_log','stripped_dark_acacia_wood']:
    recipe(f'planks_from_{name}',ingredients=[f'{MOD}:{name}'],result='burned_planks',count=4)
def tag(kind,name,values):
    path=f'data/minecraft/tags/{kind}/{name}.json';data=read(RES/path) if (RES/path).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+values));write(path,data)
logs=[f'{MOD}:{n}' for n in ['burned_log','dark_acacia_wood','stripped_dark_acacia_log','stripped_dark_acacia_wood']]
for kind in ['block','item']:
    tag(kind,'logs',logs);tag(kind,'logs_that_burn',logs)
    for suffix in ['stairs','slabs','doors','trapdoors']:
        singular={'slabs':'slab','stairs':'stairs','doors':'door','trapdoors':'trapdoor'}[suffix]
        tag(kind,f'wooden_{suffix}',[f'{MOD}:dark_acacia_{singular}'])
    tag(kind,'saplings',[f'{MOD}:dark_acacia_sapling'])
tag('block','dirt',[f'{MOD}:burned_dirt'])
tag('block','mineable/axe',logs+[f'{MOD}:dark_acacia_{n}' for n in ['stairs','slab','door','trapdoor']]+[f'{MOD}:{p}_dark_acacia_log' for p in ['carved','half_carved']])
palette=read(RES/f'assets/{MOD}/clean_block_palette.json')
for name in ['burned_log','burned_planks']:palette.pop(name,None)
write(f'assets/{MOD}/clean_block_palette.json',palette)
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=read(RES/path)
    for name in list(names.values())+['carved_dark_acacia_log','half_carved_dark_acacia_log']:
        label={'burned_log':'Dark Acacia Log','burned_planks':'Dark Acacia Planks'}.get(name,name.replace('_',' ').title())
        data[f'block.{MOD}.{name}']=label
    write(path,data)
write(f'assets/{MOD}/dark_acacia_texture_sources.json',{target:f'{source}.png' for source,target in textures.items()})
print('Imported all 21 Dark Acacia textures; added wood family, plank variants, carved blocks and sapling loot.')
