"""Surface lighting and a clean 16x16 resource pass; apply after all older chapters."""
import generate_spells
from generate_resources import MOD,RES,write,item
from generate_minerals import block_model,self_drop,read,source
from pathlib import Path
import json,shutil,zipfile,io
from PIL import Image

SOURCE=Path(__file__).parent/'texture_sources/clean_07'
new_blocks=['glowstone_vein_stone','glowstone_crystal_block','pale_glowstone']
for name in new_blocks:
    block_model(name,{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/{name}'}})
    self_drop(name)
for tag in ['mineable/pickaxe','blocks_motion_no_leaves']:
    p=f'data/minecraft/tags/block/{tag}.json';data=read(RES/p)
    data['values']=sorted(set(data['values']+[f'{MOD}:{name}' for name in new_blocks]));write(p,data)
write(f'data/{MOD}/worldgen/feature/surface_crystal_rocks.json',{'type':f'{MOD}:surface_crystal_rocks'})
write(f'data/{MOD}/worldgen/placed_feature/surface_crystal_rocks.json',{'feature':f'{MOD}:surface_crystal_rocks','placement':[{'type':'minecraft:rarity_filter','chance':3},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
for name in ['ash_wastes','charred_forest','crystal_hollows','echo_fields']:
    p=f'data/{MOD}/worldgen/biome/{name}.json';data=read(RES/p)
    data['features'][2]=[f'{MOD}:surface_crystal_rocks'];write(p,data)

# Use the real Vanilla pixel grids for terrain instead of resizing generated atlas cells.
# Colour is applied by native model tint sources; the original PNG pixels stay intact.
palette={
    'black_sand':('black_concrete_powder',0xffffff),
    'burned_dirt':('coarse_dirt',0x899ba8),
    'burned_gravel':('gravel',0xa5a3ad),
    'burned_stone':('stone',0xaaa7b4),
    'burned_deepslate':('deepslate',0xbfc0ce),
    'burned_cobblestone':('cobblestone',0xaaa7b4),
    'burned_bedrock':('bedrock',0x9ca0aa),
    'burned_log':('spruce_log',0x91a1b8),
    'burned_planks':('dark_oak_planks',0xaebdd7),
    'burned_leaves':('oak_leaves',0x75806e),
    'burned_bricks':('stone_bricks',0xaaa7b4),
    'burned_coal_ore':('coal_ore',0xaaa7b4),
    'burned_iron_ore':('iron_ore',0xaaa7b4),
    'improved_amethyst_crystal_ore':('diamond_ore',0xa18ce4),
    'ember_stone':('magma',0xffffff),
    'echo_crystal':('diamond_block',0xbab1ff),
    'echo_relic':('chiseled_deepslate',0xaddce4),
    'improved_obsidian':('crying_obsidian',0xb9c9ff),
    'fire_cave_stone':('red_terracotta',0xffbe92),
    'water_cave_stone':('cyan_terracotta',0xa0ceff),
    'glowing_cave_stone':('end_stone',0xffefa0),
    'spell_rune_stone':('chiseled_polished_blackstone',0xc2b5ff),
    'glowing_honey_block':('honey_block_side',0xffffff)
}
write(f'assets/{MOD}/clean_block_palette.json',{name:colour for name,(_,colour) in palette.items()})
client=Path('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar')
native=SOURCE/'native';native.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(client) as z:
    def native_texture(name,texture):
        png=z.read(f'assets/minecraft/textures/block/{texture}.png')
        image=Image.open(io.BytesIO(png))
        assert image.width==16 and image.height%16==0,(texture,image.size)
        # An animated Vanilla strip contains several real 16x16 frames.
        # A single unchanged frame gives static custom blocks the exact UV size.
        if image.size==(16,16):(native/f'{name}.png').write_bytes(png)
        else:image.crop((0,0,16,16)).save(native/f'{name}.png')
    for name,(texture,colour) in palette.items():
        native_texture(name,texture)
        model=source('cube');model['textures']={face:f'{MOD}:block/{name}' for face in ['down','up','north','south','west','east','particle']}
        for element in model['elements']:
            for face in element['faces'].values():face['tintindex']=0
        # Preserve the end-grain and rotation of logs.
        if name=='burned_log':
            model['textures']['up']=model['textures']['down']=f'{MOD}:block/burned_log_top'
            native_texture('burned_log_top','spruce_log_top')
        write(f'assets/{MOD}/models/block/{name}.json',model)
        if name not in ['burned_bedrock','improved_obsidian']:
            write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':colour}]}})
    # Preserve honey's distinct top and bottom while cleaning all three faces.
    honey=read(RES/f'assets/{MOD}/models/block/glowing_honey_block.json')
    for face,native_name in [('up','honey_block_top'),('down','honey_block_bottom')]:
        name=f'glowing_honey_block_{face}';native_texture(name,native_name)
        honey['textures'][face]=f'{MOD}:block/{name}'
    write(f'assets/{MOD}/models/block/glowing_honey_block.json',honey)
    # Spelltable keeps its Vanilla dimensions and UV layout, with sharp source pixels.
    table=read(RES/f'assets/{MOD}/models/block/spelltable.json')
    for face in ['top','side','bottom']:
        name=f'spelltable_{face}';native_texture(name,f'enchanting_table_{face}')
        table['textures'][face]=f'{MOD}:block/{name}'
    table['textures']['particle']=f'{MOD}:block/spelltable_bottom';write(f'assets/{MOD}/models/block/spelltable.json',table)

for png in native.glob('*.png'):
    shutil.copyfile(png,RES/f'assets/{MOD}/textures/block/{png.name}')
for kind in ['item','block']:
    for png in (SOURCE/kind).glob('*.png'):
        shutil.copyfile(png,RES/f'assets/{MOD}/textures/{kind}/{png.name}')
        if kind=='item':item(png.stem)

for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    labels=['Glowstone-Aderstein','Glowstone-Kristallblock','Blasser Glowstone'] if lang=='de_de' else ['Glowstone Vein Stone','Glowstone Crystal Block','Pale Glowstone']
    for name,label in zip(new_blocks,labels):data[f'block.{MOD}.{name}']=label
    data[f'guide.{MOD}.surface_lights']='Leuchtende Kristallfelsen wachsen auf trockenem Gelände in den dunklen Dimensionen. Mit einer Spitzhacke kannst du die Lichtsteine abbauen.' if lang=='de_de' else 'Luminous crystal outcrops grow on dry terrain in the dark dimensions. Harvest their light-emitting stones with a pickaxe.'
    data[f'guide.{MOD}.spell_core']='Elemental Core + Buch → Spell Book.' if lang=='de_de' else 'Elemental Core + book → Spell Book.'
    write(p,data)
print('Generated surface crystal rocks, three light blocks and clean native 16x16 terrain textures.')
