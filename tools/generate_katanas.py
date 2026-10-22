"""Port the supplied animated Blockbench katanas and add their progression recipes."""
import generate_lighting
from generate_resources import RES,MOD,write,recipe
from generate_minerals import read
from pathlib import Path
import shutil,json,zipfile

archive=Path(__file__).parent/'texture_sources/katanas'
supplied=RES.parents[3]/'Katanas/assets/minecraft/optifine/cit'
archive.mkdir(parents=True,exist_ok=True)
if supplied.exists():
    for path in supplied.iterdir():
        if path.is_file():shutil.copyfile(path,archive/path.name)
mapping={'frostgleam':'algidgleam','bloodgrief':'bloodygrief','souleater':'faintquasar'}
for name,old in mapping.items():
    model=read(archive/f'{old}.json')
    model['textures']={key:f'{MOD}:item/{name}' for key in model['textures']}
    write(f'assets/{MOD}/models/item/{name}.json',model)
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
    for suffix in ['.png','.png.mcmeta']:
        shutil.copyfile(archive/f'{old}{suffix}',RES/f'assets/{MOD}/textures/item/{name}{suffix}')

recipe('japanese_essence',ingredients=['minecraft:cherry_sapling',f'{MOD}:water_core'])
recipe('blood_essence',ingredients=[f'{MOD}:creeperblood','minecraft:gold_ingot'])
recipe('blood_core',ingredients=[f'{MOD}:blood_essence',f'{MOD}:water_core'])
recipe('soul_essence',ingredients=['minecraft:soul_sand','minecraft:blackstone'])
recipe('soul_core',ingredients=[f'{MOD}:soul_essence',f'{MOD}:fire_core'])
recipe('frostgleam',['  E',' I ','S  '],{'E':f'{MOD}:japanese_essence','I':'minecraft:blue_ice','S':f'{MOD}:improved_stick'})
for name,essence in [('bloodgrief','blood_core'),('souleater','soul_core')]:
    recipe(name,['  E',' E ','S  '],{'E':f'{MOD}:{essence}','S':f'{MOD}:improved_stick'})
for tag in ['minecraft:swords','minecraft:enchantable/sword','minecraft:enchantable/sharp_weapon','minecraft:enchantable/weapon','minecraft:enchantable/durability',f'{MOD}:improved_tools']:
    namespace,name=tag.split(':');p=f'data/{namespace}/tags/item/{name}.json'
    data=read(RES/p) if (RES/p).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+[f'{MOD}:{n}' for n in mapping]));write(p,data)

# Reuse native 16x16 item silhouettes and the clean core sprites, with model colours.
icons={'japanese_essence':('minecraft:item/pink_dye',0xffffff),'blood_essence':('minecraft:item/red_dye',0xe94545),'soul_essence':('minecraft:item/echo_shard',0xffffff),'blood_core':(f'{MOD}:item/fire_core',0xff5360),'soul_core':(f'{MOD}:item/spell_core',0x6bdfff)}
for name,(texture,tint) in icons.items():
    write(f'assets/{MOD}/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':texture}})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}','tints':[{'type':'minecraft:constant','value':tint}]}})
write(f'assets/{MOD}/models/item/creeperblood.json',{'parent':'minecraft:item/generated','textures':{'layer0':'minecraft:item/potion_overlay','layer1':'minecraft:item/potion'}})
write(f'assets/{MOD}/items/creeperblood.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/creeperblood','tints':[{'type':'minecraft:constant','value':0x69c77a}]}})
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    labels={'frostgleam':'Frostgleam','bloodgrief':'Bloodgrief','souleater':'Souleater','japanese_essence':'Japanese Essence','creeperblood':'Creeperblood','blood_essence':'Blood Essence','blood_core':'Blood Core','soul_essence':'Soul Essence','soul_core':'Soul Core'}
    for name,label in labels.items():data[f'item.{MOD}.{name}']=label
    data[f'guide.{MOD}.katanas']='Frostgleam: 6 Schaden, 1,5 Tempo, Frosted 4 Sekunden. Bloodgrief: 8 Schaden, 1,5 Tempo, Blood 5 Sekunden (tödlich, milchresistent). Souleater: 11,5 Schaden, Soulless 3 Sekunden. +2 Blöcke Reichweite. Creeperblood: ein Item mit 5 % Dropchance.' if lang=='de_de' else 'Frostgleam: 6 damage, 1.5 speed, Frosted for 4s. Bloodgrief: 8 damage, 1.5 speed, Blood for 5s (lethal, milk-resistant). Souleater: 11.5 damage, Soulless for 3s. +2 blocks reach. Creeperblood: one item, 5% drop chance.'
    for effect in ['frosted','blood','soulless']:data[f'effect.{MOD}.{effect}']=effect.title()
    write(p,data)
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as client:
    for effect,native in [('frosted','slowness'),('blood','instant_damage'),('soulless','nausea')]:
        icon=RES/f'assets/{MOD}/textures/mob_effect/{effect}.png';icon.parent.mkdir(parents=True,exist_ok=True)
        icon.write_bytes(client.read(f'assets/minecraft/textures/mob_effect/{native}.png'))
print('Ported three animated katanas; generated eight recipes, essences, cores and sword/enchantment tags.')
