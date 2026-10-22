"""Regenerate all chapters, then apply preserved artist textures and smithing upgrades."""
import generate_dimensions
from generate_resources import RES,MOD,write,recipe,item
from pathlib import Path
import json, shutil

SOURCE=Path(__file__).parent/'texture_sources'
kinds=['sword','pickaxe','axe','shovel','hoe','spear']
diamond={k:('improved_diamond_spear' if k=='spear' else 'enhanced_diamond_'+k) for k in kinds}
netherite={k:'perfected_netherite_'+k for k in kinds}
def smith(name,template,base,addition,result):
    write(f'data/{MOD}/recipe/{name}.json',{'type':'minecraft:smithing_transform','template':template,'base':base,'addition':addition,'result':{'id':f'{MOD}:{result}'}})
    write(f'data/{MOD}/advancement/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'has_template':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':template}]}},'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipes':[f'{MOD}:{name}']}}},'requirements':[['has_template','has_recipe']],'rewards':{'recipes':[f'{MOD}:{name}']}})
for kind in kinds:
    smith('improve_diamond_'+kind,f'{MOD}:improving_template','minecraft:diamond_'+kind,f'{MOD}:improved_stick',diamond[kind])
    smith('improve_netherite_'+kind,f'{MOD}:improving_template','minecraft:netherite_'+kind,f'{MOD}:improved_stick',netherite[kind])
    smith('upgrade_improved_'+kind,'minecraft:netherite_upgrade_smithing_template',f'{MOD}:{diamond[kind]}','minecraft:netherite_ingot',netherite[kind])
    if kind!='spear':item(netherite[kind],True)
    tag={'sword':'swords','pickaxe':'pickaxes','axe':'axes','shovel':'shovels','hoe':'hoes','spear':'spears'}[kind]
    names=[f'{MOD}:{diamond[kind]}',f'{MOD}:{netherite[kind]}']
    if kind=='spear':names.append(f'{MOD}:double_spear')
    write(f'data/minecraft/tags/item/{tag}.json',{'replace':False,'values':names})
write(f'data/minecraft/tags/item/cluster_max_harvestables.json',{'replace':False,'values':[f'{MOD}:{diamond["pickaxe"]}',f'{MOD}:{netherite["pickaxe"]}']})
write(f'data/{MOD}/tags/item/improved_tools.json',{'values':[f'{MOD}:{name}' for name in list(diamond.values())+list(netherite.values())+['double_spear']]})
recipe('improved_diamond_spear',['  D',' S ','S  '],{'D':'#minecraft:diamond_tool_materials','S':f'{MOD}:improved_stick'})
(RES/f'data/{MOD}/recipe/improving_template.json').unlink(missing_ok=True)
(RES/f'data/{MOD}/advancement/recipes/improving_template.json').unlink(missing_ok=True)
recipe('duplicate_improving_template',['DTD','DOD','DDD'],{'D':'minecraft:diamond','T':f'{MOD}:improving_template','O':'minecraft:crying_obsidian'},result='improving_template')
path=f'data/{MOD}/recipe/duplicate_improving_template.json';data=json.loads((RES/path).read_text());data['result']['count']=2;write(path,data)
smith('double_spear',f'{MOD}:improving_template',f'{MOD}:perfected_netherite_spear',f'{MOD}:improved_diamond_spear','double_spear')
item('improving_template')
for name in ['improved_diamond_spear','perfected_netherite_spear','double_spear']:
    item(name)
    write(f'assets/{MOD}/models/item/{name}_in_hand.json',{'parent':'minecraft:item/spear_in_hand','textures':{'layer0':f'{MOD}:item/{name}_in_hand'}})
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':['gui','ground','fixed','on_shelf'],'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}}],'fallback':{'type':'minecraft:model','model':f'{MOD}:item/{name}_in_hand'}},'swap_animation_scale':1.95})
for folder in ['items','blocks']:
    for source in (SOURCE/folder).glob('*.png'):
        target=RES/f'assets/{MOD}/textures/{"item" if folder=="items" else "block"}/{source.name}'
        target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,target)
slot=RES/f'assets/{MOD}/textures/gui/sprites/container/slot/improved_stick.png';slot.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(SOURCE/'items/improved_stick.png',slot)
# Different end grain makes burned trunks distinguishable from planks even in silhouette.
write(f'assets/{MOD}/models/block/burned_log.json',{'parent':'minecraft:block/cube_column','textures':{'side':f'{MOD}:block/burned_log','end':f'{MOD}:block/burned_log_top'}})
for lang in ['en_us','de_de']:
    data=json.loads((RES/f'assets/{MOD}/lang/{lang}.json').read_text(encoding='utf-8'))
    for kind in kinds:
        data[f'item.{MOD}.{diamond[kind]}']='Improved Diamond '+kind.title()
        data[f'item.{MOD}.{netherite[kind]}']='Perfected Netherite '+kind.title()
    data[f'item.{MOD}.double_spear']='Double Spear'
    data[f'item.{MOD}.improving_template']='Improving Template'
    de=lang=='de_de'
    for key,label in {'applies_to':'Diamant- und Netheritewerkzeuge, Speere' if de else 'Diamond and Netherite tools, spears','ingredients':'Improved Stick / Improved Diamond Spear','base_slot':'Werkzeug oder Perfected Netherite Spear einsetzen' if de else 'Insert a tool or Perfected Netherite Spear','addition_slot':'Improved Stick oder Improved Diamond Spear einsetzen' if de else 'Insert Improved Stick or Improved Diamond Spear'}.items():data[f'template.{MOD}.{key}']=label
    data[f'guide.{MOD}.improving_template']=('Schmiedetisch: Improving Template + Diamant-/Netheritewerkzeug + Improved Stick. Verzauberungen, Name und Abnutzung bleiben erhalten. Verbesserte Diamantwerkzeuge mit Netherite-Upgradeschablone und Netheritebarren weiter verbessern.' if de else 'Smithing: Improving Template + Diamond/Netherite tool + Improved Stick. Keeps enchantments, name and wear. Improved Diamond tools upgrade further with Netherite Upgrade Template and Netherite Ingot.')
    data[f'guide.{MOD}.double_spear']=('Improving Template + Perfected Netherite Spear + Improved Diamond Spear am Schmiedetisch. 8 Stichschaden, +2 Blöcke Reichweite, erhöhte kinetische Schadenswirkung und Verzauberbarkeit 25.' if de else 'Smithing: Improving Template + Perfected Netherite Spear + Improved Diamond Spear. 8 jab damage, +2 blocks reach, stronger kinetic attacks and enchantability 25.')
    write(f'assets/{MOD}/lang/{lang}.json',data)
print('Applied artist textures, 19 smithing recipes, tool tags and native spear models.')
