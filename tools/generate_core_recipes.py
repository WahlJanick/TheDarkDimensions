"""Runestone-based elemental core recipes; preserve all earlier generated resources."""
import generate_katana_defense
from generate_resources import RES, MOD, write, recipe
import json

recipe('empty_runestone',[' S ','SDS',' S '],{'S':'minecraft:stone','D':'minecraft:diamond'})
recipe('fire_core',['BBB','BRB','BBB'],{'B':'minecraft:blaze_powder','R':f'{MOD}:empty_runestone'})
recipe('water_core',[' W ','WRW',' W '],{'W':'minecraft:water_bucket','R':f'{MOD}:empty_runestone'})
recipe('lightning_core',[' L ','LRL',' L '],{'L':f'#{MOD}:core_lightning_rods','R':f'{MOD}:empty_runestone'})
write(f'data/{MOD}/tags/item/core_lightning_rods.json',{'replace':False,'values':[
    f'minecraft:{wax}{stage}lightning_rod' for wax in ['', 'waxed_'] for stage in ['', 'exposed_', 'weathered_', 'oxidized_']
]})

# Native stone tablet model uses Minecraft's original 16x16 block textures.
# Three non-overlapping sections give the empty rune tablet bevelled corners.
elements=[]
for start,end in [([3,1,7],[13,3,9]),([2,3,7],[14,13,9]),([3,13,7],[13,15,9])]:
    elements.append({'from':start,'to':end,'faces':{face:{'texture':'#stone'} for face in ['down','up','north','south','west','east']}})
write(f'assets/{MOD}/models/item/empty_runestone.json',{'parent':'minecraft:block/block','textures':{'particle':'minecraft:block/stone','stone':'minecraft:block/stone'},'elements':elements,'display':{'gui':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[1,1,1]}}})
write(f'assets/{MOD}/items/empty_runestone.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/empty_runestone'}})
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data[f'item.{MOD}.empty_runestone']='Leerer Runestone' if lang=='de_de' else 'Empty Runestone'
    guides={
      'empty_runestone':('Vier Stone kreuzförmig um einen Diamond ergeben einen leeren Runestone. Grundlage für Fire, Water und Lightning Cores.','Four Stone in a cross around one Diamond craft an Empty Runestone. Base for Fire, Water and Lightning Cores.'),
      'fire_core':('Acht Blaze Powder um einen leeren Runestone craften einen Fire Core.','Eight Blaze Powder surrounding one Empty Runestone craft a Fire Core.'),
      'water_core':('Vier Water Buckets kreuzförmig um einen leeren Runestone craften einen Water Core. Die vier leeren Eimer bleiben erhalten.','Four Water Buckets in a cross around one Empty Runestone craft a Water Core. All four empty buckets are returned.'),
      'lightning_core':('Vier Lightning Rods kreuzförmig um einen leeren Runestone craften einen Lightning Core. Alle Oxidationsstufen, auch gewachst und gemischt, sind gültig.','Four Lightning Rods in a cross around one Empty Runestone craft a Lightning Core. Any oxidation stage works, including waxed and mixed variants.')
    }
    for name,text in guides.items():data[f'guide.{MOD}.{name}']=text[lang=='en_us']
    write(path,data)
print('Generated Empty Runestone, three core recipes and an eight-variant Lightning Rod ingredient tag.')
