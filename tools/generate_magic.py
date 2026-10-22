"""Elemental skill books, master recipe and optional Curios spellbook slot."""
import generate_ruins
from generate_resources import RES,MOD,write,recipe,item
from PIL import Image
import json,colorsys,zipfile,io

books={'fire_spell_book':('Feuer',0xff8249),'water_spell_book':('Wasser',0x63bcff),'lightning_spell_book':('Blitz',0xffe36b),'chronus_spell_book':('Chronus',0xb09be2),'blood_spell_book':('Blut',0xd36078),'soul_spell_book':('Seelen',0x65d6ce),'ender_chest_spell_book':('Ender',0xcb88ef),'elemental_spell_book':('Elemental',0xd9e3ed),'elemental_master_spellbook':('Elemental Master',0xf5dc96)}
# Preserve the established clean book silhouette; recolor its cover rather than its pages.
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as z:
 source=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/enchanted_book.png'))).convert('RGBA')
assert source.size==(16,16)
for name,(label,color) in books.items():
 if name in ['fire_spell_book','water_spell_book','lightning_spell_book','ender_chest_spell_book']:continue
 im=source.copy();hue=colorsys.rgb_to_hsv(((color>>16)&255)/255,((color>>8)&255)/255,(color&255)/255)[0]
 for y in range(16):
  for x in range(16):
   r,g,b,a=source.getpixel((x,y));h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
   if a and s>.3:
    rr,gg,bb=colorsys.hsv_to_rgb(hue,.45 if name=='elemental_spell_book' else max(.4,s),v);im.putpixel((x,y),(round(rr*255),round(gg*255),round(bb*255),a))
 im.save(RES/f'assets/{MOD}/textures/item/{name}.png');item(name)
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as z:
 core=Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/heart_of_the_sea.png'))).convert('RGBA')
for name,color in [('chronus_core',0xb09be2),('ender_core',0xcb88ef)]:
 im=core.copy();hue=colorsys.rgb_to_hsv(((color>>16)&255)/255,((color>>8)&255)/255,(color&255)/255)[0]
 for y in range(im.height):
  for x in range(im.width):
   r,g,b,a=core.getpixel((x,y));h,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
   if a and s>.2:
    rr,gg,bb=colorsys.hsv_to_rgb(hue,s,v);im.putpixel((x,y),(round(rr*255),round(gg*255),round(bb*255),a))
 im.save(RES/f'assets/{MOD}/textures/item/{name}.png');item(name)
recipe('elemental_master_spellbook',['CCC','CBC','CCC'],{'C':f'{MOD}:spell_core','B':f'{MOD}:spell_book'})
recipe('chronus_core',ingredients=[f'{MOD}:spell_core',f'{MOD}:chronus_orb'])
recipe('ender_core',ingredients=[f'{MOD}:soul_core','minecraft:ender_pearl'])
write(f'data/{MOD}/curios/slots/spellbook.json',{'size':1,'order':210,'icon':'curios:slot/empty_curio_slot','validators':['curios:tag'],'entities':['minecraft:player']})
write(f'data/{MOD}/curios/entities/player_spellbook.json',{'entities':['minecraft:player'],'slots':['spellbook']})
write('data/curios/tags/item/spellbook.json',{'replace':False,'values':[f'{MOD}:{b}' for b in books]})
for lang in ['de_de','en_us']:
 p=RES/f'assets/{MOD}/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
 for name,(label,color) in books.items():data[f'item.{MOD}.{name}']=label+' Spellbook'
 data[f'item.{MOD}.chronus_core']='Chronus Core';data[f'item.{MOD}.ender_core']='Ender Core'
 data['key.category.thedarkdimensions.magic']='The Dark Dimensions – Magie'
 for key,label in [('open_spellbook','Spellbook öffnen'),('spellwheel','Spellwheel öffnen (halten)'),('cast_spell','Ausgewählten Zauber wirken')]:data[f'key.{MOD}.{key}']=label
 data[f'magic.{MOD}.learn_hint']='Öffne dein Spellbook über die einstellbare Taste und lerne einen Skill.'
 data[f'guide.{MOD}.spelltable']='Blankes Spellbook + Core am Spelltable bindet ein Element. Jeder Skill kostet 1 Chronus Infused Ingot. Benutze Zauber, um Stufe 5 zu erreichen. Master Spellbook: 8 Elemental Cores um ein blankes Buch; weitere Cores schalten zusätzliche Elemente frei.'
 data['curios.identifier.spellbook']='Spellbook'
 write(f'assets/{MOD}/lang/{lang}.json',data)
print('Generated elemental books, cores, master recipe and Curios spellbook slot.')
