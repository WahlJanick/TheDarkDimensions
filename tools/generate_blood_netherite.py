"""Blood Netherite equipment and save-compatible Chronus ingot presentation."""
import generate_armory
from generate_resources import RES,MOD,write,recipe,item
from generate_minerals import read
from pathlib import Path
from PIL import Image
import shutil

root=RES.parents[2];source=root.parent/'red-netherite/assets/minecraft/textures';archive=Path(__file__).parent/'texture_sources/blood_netherite'
archive.mkdir(parents=True,exist_ok=True)
def supplied(old,new,folder='item',outfolder=None):
    p=source/folder/f'{old}.png';stored=archive/f'{new}.png'
    if p.exists():shutil.copyfile(p,stored)
    target=RES/f'assets/{MOD}/textures/{outfolder or folder}/{new}.png';target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(stored,target)
def tag(name,values,namespace='minecraft',kind='item'):
    p=f'data/{namespace}/tags/{kind}/{name}.json';data=read(RES/p) if (RES/p).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+[f'{MOD}:{n}' for n in values]));write(p,data)
def smith(name,base):
    write(f'data/{MOD}/recipe/{name}.json',{'type':'minecraft:smithing_transform','template':f'{MOD}:red_netherite_upgrade_smithing_template','base':base,'addition':f'{MOD}:red_netherite_ingot','result':{'id':f'{MOD}:{name}'}})

recipe('red_netherite_ingot',ingredients=[f'{MOD}:creeperblood']*4+['minecraft:netherite_ingot']*2)
recipe('red_netherite_upgrade_smithing_template',['DTD','DND','DDD'],{'D':'minecraft:diamond','T':f'{MOD}:red_netherite_upgrade_smithing_template','N':'minecraft:nether_wart'},count=2)
recipe('red_netherite_block',['III','III','III'],{'I':f'{MOD}:red_netherite_ingot'})
recipe('red_netherite_ingots_from_block',ingredients=[f'{MOD}:red_netherite_block'],result='red_netherite_ingot',count=9)
supplied('netherite_ingot','red_netherite_ingot');item('red_netherite_ingot')
tools=[];improved=[]
for kind in ['sword','pickaxe','axe','shovel','hoe','warrior_axe']:
    name=f'red_netherite_{kind}';old=name if kind=='warrior_axe' else f'netherite_{kind}'
    supplied(old,name);item(name,True);tools.append(name)
    smith(name,f'{MOD}:netherite_warrior_axe' if kind=='warrior_axe' else f'minecraft:netherite_{kind}')
    if kind!='warrior_axe':
        enhanced=f'red_netherite_improved_{kind}';supplied(old,enhanced);item(enhanced,True);tools.append(enhanced);improved.append(enhanced)
        smith(enhanced,f'{MOD}:perfected_netherite_{kind}')
        write(f'data/{MOD}/recipe/{enhanced}_handle.json',{'type':'minecraft:smithing_transform','template':f'{MOD}:improving_template','base':f'{MOD}:{name}','addition':f'{MOD}:improved_stick','result':{'id':f'{MOD}:{enhanced}'}})
    for t in [kind+'s' if kind!='warrior_axe' else 'axes','enchantable/weapon','enchantable/sharp_weapon','enchantable/durability']:
        tag(t,[name]+([enhanced] if kind!='warrior_axe' else []))
    for t in (['enchantable/sword'] if kind=='sword' else ['enchantable/mining','enchantable/mining_loot']):tag(t,[name]+([enhanced] if kind!='warrior_axe' else []))
armor=[]
for kind in ['helmet','chestplate','leggings','boots']:
    name=f'red_netherite_{kind}';armor.append(name);supplied(f'netherite_{kind}',name);item(name);smith(name,f'minecraft:netherite_{kind}')
    for t in [kind+'s','enchantable/armor','enchantable/durability','trimmable_armor',f'enchantable/{"head" if kind=="helmet" else "chest" if kind=="chestplate" else "leg" if kind=="leggings" else "foot"}_armor']:tag(t,[name])
for layer in ['humanoid','humanoid_leggings']:
    supplied('netherite',f'red_netherite_{layer}',f'entity/equipment/{layer}',f'entity/equipment/{layer}')
write(f'assets/{MOD}/equipment/red_netherite.json',{'layers':{layer:[{'texture':f'{MOD}:red_netherite_{layer}'}] for layer in ['humanoid','humanoid_leggings']}})
supplied('netherite_block','red_netherite_block','block')
write(f'assets/{MOD}/models/block/red_netherite_block.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/red_netherite_block'}})
write(f'assets/{MOD}/blockstates/red_netherite_block.json',{'variants':{'':{'model':f'{MOD}:block/red_netherite_block'}}})
write(f'assets/{MOD}/models/item/red_netherite_block.json',{'parent':f'{MOD}:block/red_netherite_block'})
write(f'assets/{MOD}/items/red_netherite_block.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/red_netherite_block'}})
write(f'data/{MOD}/loot_table/blocks/red_netherite_block.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:red_netherite_block'}],'condition':{'type':'minecraft:survives_explosion'}}]})
for t in ['mineable/pickaxe','needs_diamond_tool','blocks_motion_no_leaves']:tag(t,['red_netherite_block'],kind='block')
tag('repairs_red_netherite',['red_netherite_ingot'],MOD)
tag('improved_tools',improved+['red_netherite_warrior_axe'],MOD)

# Add subtable loot instead of replacing the vanilla Bastion tables.
for kind in ['treasure','bridge','hoglin_stable','other']:
    name=f'blood_template_bastion_{kind}'
    write(f'data/{MOD}/loot_modifiers/{name}.json',{'type':'neoforge:add_table','condition':{'type':'neoforge:loot_table_id','loot_table_id':f'minecraft:chests/bastion_{kind}'},'table':f'{MOD}:chests/{name}'})
    pool={'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:red_netherite_upgrade_smithing_template'}]}
    if kind!='treasure':pool['condition']={'type':'minecraft:random_chance','chance':0.1}
    write(f'data/{MOD}/loot_table/chests/{name}.json',{'type':'minecraft:chest','pools':[pool]})
# NeoForge 26.3 discovers modifier files directly; older index files are invalid here.
legacy_index=RES/'data/neoforge/loot_modifiers/global_loot_modifiers.json'
if legacy_index.exists():legacy_index.unlink()

# Imagegen originals are archived; only nearest-neighbor asset export is done here.
for name in ['chronus_infused_metalinium_ingot','red_netherite_upgrade_smithing_template']:
    original=archive/f'{name}_original.png'
    Image.open(original).convert('RGBA').resize((16,16),Image.Resampling.NEAREST).save(RES/f'assets/{MOD}/textures/item/{name}.png')
    item(name)
# Keep the established registry ID so old inventories, recipes and Spelltables retain their ingots.
legacy='essentialium_infused_metalinium_ingot'
write(f'assets/{MOD}/models/item/{legacy}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'{MOD}:item/chronus_infused_metalinium_ingot'}})
write(f'assets/{MOD}/items/{legacy}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{legacy}'}})
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    data[f'item.{MOD}.{legacy}']='Chronus Infused Metalinium Ingot'
    data[f'guide.{MOD}.spelltable']=data[f'guide.{MOD}.spelltable'].replace('Essentialium Infused','Chronus Infused')
    for name in tools+armor+['red_netherite_ingot']:
        data[f'item.{MOD}.{name}']=name.replace('red_netherite','blood_netherite').replace('_',' ').title()
    data[f'block.{MOD}.red_netherite_block']='Blood Netherite Block'
    data[f'item.{MOD}.red_netherite_upgrade_smithing_template']='Blood Netherite Upgrading Template'
    for key,de,en in [('applies_to','Netherite-Ausrüstung','Netherite equipment'),('ingredients','Blood Netherite Ingot','Blood Netherite Ingot'),('base_slot','Netherite-Rüstung oder Werkzeug einsetzen','Insert Netherite armor or tool'),('addition_slot','Blood Netherite Ingot einsetzen','Insert Blood Netherite Ingot')]:data[f'template.{MOD}.blood.{key}']=de if lang=='de_de' else en
    data[f'guide.{MOD}.blood_netherite']='4 Creeperblood + 3 Netherite Ingots → 1 Blood Netherite Ingot. Bastion-Template + Netherite-Ausrüstung + Blood Netherite Ingot am Schmiedetisch. Duplizieren: Template + 7 Diamonds + Nether Wart → 2 Templates.' if lang=='de_de' else '4 Creeperblood + 3 Netherite Ingots → 1 Blood Netherite Ingot. Smithing: Bastion template + Netherite equipment + Blood Netherite Ingot. Duplicate with template + 7 diamonds + Nether Wart → 2 templates.'
    write(p,data)
print('Generated Blood Netherite equipment, Bastion loot and Chronus ingot.')
