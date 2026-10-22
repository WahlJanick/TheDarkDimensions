"""Katana-only combat enchants and distinct cooled/improved stick artwork."""
import generate_katana_boss
from generate_resources import RES,MOD,write
import json,shutil
from pathlib import Path

write(f'data/{MOD}/tags/item/katana_enchantable.json',{'replace':False,'values':[f'{MOD}:{n}' for n in ['frostgleam','bloodgrief','souleater','stormveil']]})
for name,weight,max_level,min_cost,max_cost,effects in [
 ('slashing',10,3,{'base':12,'per_level_above_first':11},{'base':32,'per_level_above_first':11},{'minecraft:damage':[{'effect':{'type':'minecraft:add','value':{'type':'minecraft:linear','base':1.5,'per_level_above_first':.5}}}]}),
 ('master_parrying',1,1,{'base':30,'per_level_above_first':0},{'base':80,'per_level_above_first':0},{})]:
    write(f'data/{MOD}/enchantment/{name}.json',{'description':{'translate':f'enchantment.{MOD}.{name}'},'supported_items':f'#{MOD}:katana_enchantable','weight':weight,'max_level':max_level,'min_cost':min_cost,'max_cost':max_cost,'anvil_cost':8 if name=='master_parrying' else 2,'slots':['mainhand'],'effects':effects})
for tag,names in [('in_enchanting_table',['slashing','master_parrying']),('tradeable',['slashing']),('on_random_loot',['slashing']),('non_treasure',['slashing'])]:
    path=f'data/minecraft/tags/enchantment/{tag}.json';file=RES/path
    data=json.loads(file.read_text(encoding='utf-8')) if file.exists() else {'replace':False,'values':[]}
    for name in names:
        id=f'{MOD}:{name}'
        if id not in data['values']:data['values'].append(id)
    write(path,data)
# Stable saved IDs: improved_stick is the cooled Burned Stick; reinforced_stick
# is the actual Improved Stick. Only the latter switches to its new gray sprite.
gray=Path(__file__).parent/'texture_sources/sticks/improved_stick_gray.png'
if gray.exists():
    shutil.copyfile(gray,RES/f'assets/{MOD}/textures/item/reinforced_stick.png')
    shutil.copyfile(gray,RES/f'assets/{MOD}/textures/gui/sprites/container/slot/improved_stick.png')
write(f'assets/{MOD}/models/item/reinforced_stick.json',{'parent':'minecraft:item/handheld','textures':{'layer0':f'{MOD}:item/reinforced_stick'}})
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data[f'enchantment.{MOD}.slashing']='Slashing';data[f'enchantment.{MOD}.master_parrying']='Master Parrying'
    data[f'combat.{MOD}.second_counter']='Zweiter Konter bereit!' if lang=='de_de' else 'Second counter ready!'
    data[f'guide.{MOD}.reinforced_stick']='Burned Stick + normaler Stock → Improved Stick (formlos). Für verbesserte Werkzeuggriffe, Katanas und weitere Griff-Rezepte.' if lang=='de_de' else 'Burned Stick + normal Stick → Improved Stick (shapeless). Used for improved tool handles, katanas and other handle recipes.'
    write(path,data)
print('Generated Slashing I–III, rare Master Parrying, katana tag and distinct Improved Stick model.')
