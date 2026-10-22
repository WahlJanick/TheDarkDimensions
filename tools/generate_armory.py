"""New axes, warrior crafting, recoverable thrown axe and held gliding hammer."""
import generate_katanas
from generate_resources import RES,MOD,write,recipe,item
from generate_minerals import read
from pathlib import Path
from PIL import Image
import shutil,json

root=RES.parents[2];supplied=root.parent;archive=Path(__file__).parent/'texture_sources/armory'
archive.mkdir(parents=True,exist_ok=True)
materials={
    'wooden':('#minecraft:planks','#minecraft:logs'),
    'stone':('#minecraft:stone_tool_materials','minecraft:stone'),
    'copper':('minecraft:copper_ingot','minecraft:copper_block'),
    'iron':('minecraft:iron_ingot','minecraft:iron_block'),
    'golden':('minecraft:gold_ingot','minecraft:gold_block'),
    'diamond':('minecraft:diamond','minecraft:diamond_block'),
    'netherite':('minecraft:netherite_ingot','minecraft:netherite_block')
}
recipe('throwable_axe',['II','IS'],{'I':'minecraft:iron_ingot','S':'minecraft:stick'})
recipe('warrior_stick',ingredients=['minecraft:leather',f'{MOD}:improved_stick',f'{MOD}:creeperblood'])
recipe('thundergods_hammer',['BLB','LSL',' S '],{'B':'minecraft:iron_block','L':f'{MOD}:lightning_core','S':f'{MOD}:improved_stick'})
def smith(name,template,base,addition):
    write(f'data/{MOD}/recipe/{name}.json',{'type':'minecraft:smithing_transform','template':template,'base':base,'addition':addition,'result':{'id':f'{MOD}:{name}'}})
smith('awakened_thundergods_hammer',f'{MOD}:lightning_core',f'{MOD}:thundergods_hammer',f'{MOD}:lightning_spell_book')
write(f'data/{MOD}/tags/item/fish_ingredients.json',{'values':['#minecraft:fishes','minecraft:cooked_cod','minecraft:cooked_salmon']})
axes=[];warriors=[]
for tier,(material,block) in materials.items():
    fish=f'{tier}_fish_axe';warrior=f'{tier}_warrior_axe';axes.extend([fish,warrior]);warriors.append(warrior)
    fish_source=supplied/f'fish-axes/assets/minecraft/textures/item/{tier}_pickaxe.png'
    warrior_source=supplied/f'Axes/assets/minecraft/textures/item/{"iron" if tier=="copper" else tier}_axe.png'
    for name,path in [(fish,fish_source),(warrior,warrior_source)]:
        stored=archive/f'{name}.png'
        if path.exists():shutil.copyfile(path,stored)
        shutil.copyfile(stored,RES/f'assets/{MOD}/textures/item/{name}.png');item(name,True)
    if tier=='copper':
        write(f'assets/{MOD}/items/{warrior}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{warrior}','tints':[{'type':'minecraft:constant','value':0xdb946c}]}})
    bases=[f'minecraft:{tier}_axe',f'{MOD}:{warrior}']
    if tier=='diamond':bases.append(f'{MOD}:enhanced_diamond_axe')
    if tier=='netherite':bases.append(f'{MOD}:perfected_netherite_axe')
    if tier=='iron':bases.append(f'{MOD}:throwable_axe')
    write(f'data/{MOD}/tags/item/fishable_{tier}_axes.json',{'values':bases})
    smith(fish,f'{MOD}:improving_template',f'#{MOD}:fishable_{tier}_axes',f'#{MOD}:fish_ingredients')
    recipe(warrior,['MBM','MS ',' W '],{'M':material,'B':block,'S':f'{MOD}:improved_stick','W':f'{MOD}:warrior_stick'})
hammer_candidates=list((supplied/'Great Hammer').rglob('textures/item/trident.png'))
hammer_stored=archive/'thundergods_hammer.png'
if hammer_candidates:shutil.copyfile(hammer_candidates[0],hammer_stored)
for name in ['thundergods_hammer','awakened_thundergods_hammer']:
    shutil.copyfile(hammer_stored,RES/f'assets/{MOD}/textures/item/{name}.png');item(name,True)
manifest=archive/'throwable_axe_prompt.json'
if manifest.exists():
    data=read(manifest);original=archive/'throwable_axe_original.png'
    if not original.exists():shutil.copyfile(data['path'],original)
    Image.open(original).convert('RGBA').resize((16,16),Image.Resampling.NEAREST).save(archive/'throwable_axe.png')
if (archive/'throwable_axe.png').exists():shutil.copyfile(archive/'throwable_axe.png',RES/f'assets/{MOD}/textures/item/throwable_axe.png')
else:shutil.copyfile(archive/'iron_axe_reference.png',RES/f'assets/{MOD}/textures/item/throwable_axe.png')
item('throwable_axe',True)
write(f'assets/{MOD}/models/item/warrior_stick.json',{'parent':'minecraft:item/handheld','textures':{'layer0':f'{MOD}:item/improved_stick'}})
write(f'assets/{MOD}/items/warrior_stick.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/warrior_stick','tints':[{'type':'minecraft:constant','value':0xc0a385}]}})
for tag in ['axes','enchantable/mining','enchantable/mining_loot','enchantable/weapon','enchantable/sharp_weapon','enchantable/durability']:
    p=f'data/minecraft/tags/item/{tag}.json';data=read(RES/p) if (RES/p).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+[f'{MOD}:{name}' for name in axes+['throwable_axe']]));write(p,data)
for tag in ['swords','enchantable/sword','enchantable/sharp_weapon','enchantable/weapon','enchantable/durability']:
    p=f'data/minecraft/tags/item/{tag}.json';data=read(RES/p) if (RES/p).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+[f'{MOD}:{name}' for name in ['thundergods_hammer','awakened_thundergods_hammer']]));write(p,data)
p=f'data/{MOD}/tags/item/improved_tools.json';data=read(RES/p)
data['values']=sorted(set(data['values']+[f'{MOD}:{name}' for name in warriors+['thundergods_hammer','awakened_thundergods_hammer']]));write(p,data)
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    for name in axes:data[f'item.{MOD}.{name}']=name.replace('_',' ').title()
    data.update({f'item.{MOD}.throwable_axe':'Throwable Axe',f'item.{MOD}.warrior_stick':'Warrior Stick',f'item.{MOD}.thundergods_hammer':"Thundergod's Hammer",f'item.{MOD}.awakened_thundergods_hammer':"Thundergod's Hammer — Awakened"})
    data[f'guide.{MOD}.fish_axes']='Improving Template + Axt + beliebiger roher oder gekochter Fisch am Schmiedetisch. Werte und Werkzeugfunktion entsprechen der normalen Axt der Materialstufe; Verzauberungen und Abnutzung bleiben erhalten.' if lang=='de_de' else 'Smithing: Improving Template + axe + any raw or cooked fish. Normal axe stats and tool use; preserves enchantments and wear.'
    data[f'guide.{MOD}.hammer']='10 Schaden. In Haupt- oder Nebenhand halten und in der Luft erneut springen, um wie mit einer Elytra zu gleiten. Schmiedetisch: Lightning Core als Template + Hammer + Lightning Spell Book → Awakened. Rechtsklick: gezielter Blitzangriff bis 24 Blöcke, 5 Sekunden Cooldown.' if lang=='de_de' else '10 damage. Hold in either hand and jump again in midair to glide like Elytra. Smithing: Lightning Core template + hammer + Lightning Spell Book → Awakened. Right-click: aimed lightning blast up to 24 blocks, 5s cooldown.'
    write(p,data)
print('Generated throwable axe, seven fish and warrior tiers, warrior stick, hammer and lightning upgrade.')
