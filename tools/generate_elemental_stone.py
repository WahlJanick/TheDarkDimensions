"""Palette-only elemental accents on original Hard Stone sprites; no resizing or whole-model tint."""
import generate_natural_realm
from generate_resources import RES,MOD,write
from PIL import Image
import json
archive=RES.parents[2]/'tools/texture_sources/mummy_hard_stone'
accents={'fire':(1.08,0.99,0.92),'water':(0.92,1.01,1.08),'glowing':(1.07,1.05,0.92)}
ores=['coal','copper','iron','gold','redstone','lapis','diamond','emerald']
manifest={}
palette_path=f'assets/{MOD}/clean_block_palette.json'
palette=json.loads((RES/palette_path).read_text())
for theme,gains in accents.items():
    base=theme+'_cave_stone';palette.pop(base,None)
    for mineral in ['']+ores:
        name=base+('_'+mineral+'_ore' if mineral else '')
        variants=[]
        for suffix in ['', '2','3','4']:
            source=archive/(f'{mineral}_ore{suffix}.png' if mineral else f'stone{suffix}.png')
            image=Image.open(source).convert('RGBA')
            rock=Image.open(archive/f'stone{suffix}.png').convert('RGBA')
            greys=set(rock.get_flattened_data())
            original=list(image.get_flattened_data());pixels=[];mask=[]
            for index,pixel in enumerate(original):
                if pixel in greys:
                    pixels.append(tuple(min(255,round(pixel[c]*gains[c])) for c in range(3))+(pixel[3],));mask.append(index)
                else:pixels.append(pixel)
            image.putdata(pixels)
            target=name+suffix
            image.save(RES/f'assets/{MOD}/textures/block/{target}.png')
            manifest[target]={'source':source.name,'stone_pixels':mask,'size':list(image.size),'gains':gains}
            write(f'assets/{MOD}/models/block/{target}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/{target}'}})
            variants.append({'model':f'{MOD}:block/{target}'})
        write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{'':variants}})
        write(f'assets/{MOD}/models/item/{name}.json',{'parent':f'{MOD}:block/{name}'})
        write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
        if mineral:
            data=json.loads((RES/f'data/{MOD}/loot_table/blocks/hard_stone_{mineral}_ore.json').read_text())
            def remap(value):
                if isinstance(value,dict):return {k:remap(v) for k,v in value.items()}
                if isinstance(value,list):return [remap(v) for v in value]
                return f'{MOD}:{name}' if value==f'{MOD}:hard_stone_{mineral}_ore' else value
            write(f'data/{MOD}/loot_table/blocks/{name}.json',remap(data))
        for tag in ['mineable/pickaxe','blocks_motion_no_leaves']+(['needs_stone_tool'] if mineral in ['copper','iron','lapis'] else ['needs_iron_tool'] if mineral in ['gold','redstone','diamond','emerald'] else []):
            path=f'data/minecraft/tags/block/{tag}.json';data=json.loads((RES/path).read_text())
            if f'{MOD}:{name}' not in data['values']:data['values'].append(f'{MOD}:{name}')
            write(path,data)
write(palette_path,palette)
write(f'assets/{MOD}/elemental_stone_texture_sources.json',manifest)
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    for theme in accents:
        data[f'block.{MOD}.{theme}_cave_stone']=theme.title()+' Stone'
        for mineral in ores:data[f'block.{MOD}.{theme}_cave_stone_{mineral}_ore']=theme.title()+' Stone '+mineral.title()+' Ore'
    write(path,data)
print('108 elemental Hard Stone sprites: original dimensions, masked stone accent, unmodified ore pixels; four variants per block.')
