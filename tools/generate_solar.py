"""Solar progression and native animated Stormveil model import. Run from project root."""
import generate_mummy_realm
from generate_resources import RES,MOD,write,recipe,item
from pathlib import Path
from PIL import Image
import json,shutil,copy,zipfile
root=RES.parents[2]
archive=root/'tools/texture_sources/solar'
supplied_solar=root.parent/'TheDarkDimensions NutzeDieseResourcen!/Solar_Essence.png'
archived_solar=archive/'solar_essence_supplied.png'
if supplied_solar.exists():shutil.copyfile(supplied_solar,archived_solar)
for name in ['solar_essence','pocket_dimension_key']:
    target=RES/f'assets/{MOD}/textures/item/{name}.png'
    if name=='solar_essence' and archived_solar.exists():
        shutil.copyfile(archived_solar,target)
    else:
        Image.open(archive/(name+'_original.png')).resize((32,32),Image.Resampling.NEAREST).save(target)
    item(name)
loot=json.loads((RES/f'data/{MOD}/loot_table/entities/mummy.json').read_text())
loot['pools'].append({'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:solar_essence'}],'condition':{'type':'minecraft:random_chance','chance':0.05}})
write(f'data/{MOD}/loot_table/entities/mummy.json',loot)
recipe('pocket_dimension_key',[' N ',' N ','GEG'],{'N':'minecraft:netherite_ingot','G':'minecraft:gold_ingot','E':f'{MOD}:solar_essence'})
recipe('astral_crystal_wand',['D','E','S'],{'D':'minecraft:dragon_head','E':'minecraft:end_crystal','S':f'{MOD}:reinforced_stick'})
for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data[f'guide.{MOD}.astral_crystal_wand']='Senkrechtes Rezept: Dragon Head aus einem End-City-Schiff, End Crystal, Improved Stick. Rechtsklick auf einen vollständigen Obsidianrahmen oder ein aktives Netherportal öffnet das Burned Realm. 64 Aktivierungen Haltbarkeit.' if lang=='de_de' else 'Vertical recipe: Dragon Head from an End City ship, End Crystal, Improved Stick. Right-click a complete obsidian frame or active Nether Portal to open the Burned Realm. Durability: 64 activations.'
    write(path,data)
recipe('stormveil',ingredients=[f'{MOD}:solar_essence',f'{MOD}:lightning_core','minecraft:netherite_ingot',f'{MOD}:reinforced_stick'])
write(f'data/{MOD}/recipe/solar_forge.json',{'type':'minecraft:smithing_transform','template':f'{MOD}:solar_essence','base':'minecraft:blast_furnace','addition':'minecraft:coal_block','result':{'id':f'{MOD}:solar_forge'}})
write(f'data/{MOD}/advancement/recipes/solar_forge.json',{'parent':'minecraft:recipes/root','criteria':{'has_essence':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':f'{MOD}:solar_essence'}]}}},'requirements':[['has_essence']],'rewards':{'recipes':[f'{MOD}:solar_forge']}})
with zipfile.ZipFile('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as z:
    state=json.loads(z.read('assets/minecraft/blockstates/blast_furnace.json'))
    for key,variant in state['variants'].items():variant['model']=variant['model'].replace('minecraft:block/blast_furnace',f'{MOD}:block/solar_forge')
    write(f'assets/{MOD}/blockstates/solar_forge.json',state)
    for suffix in ['', '_on']:
        model=json.loads(z.read(f'assets/minecraft/models/block/blast_furnace{suffix}.json'))
        model['textures']['top']='minecraft:block/gold_block'
        model['textures']['side']='minecraft:block/chiseled_stone_bricks'
        write(f'assets/{MOD}/models/block/solar_forge{suffix}.json',model)
write(f'assets/{MOD}/models/item/solar_forge.json',{'parent':f'{MOD}:block/solar_forge'})
write(f'assets/{MOD}/items/solar_forge.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/solar_forge'}})
write(f'data/{MOD}/loot_table/blocks/solar_forge.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:solar_forge'}],'condition':{'type':'minecraft:survives_explosion'}}]})
write(f'assets/{MOD}/models/block/pocket_boundary.json',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/quartz_bricks'}})
write(f'assets/{MOD}/blockstates/pocket_boundary.json',{'variants':{'':{'model':f'{MOD}:block/pocket_boundary'}}})
for tag,names in [('mineable/pickaxe',['solar_forge']),('needs_stone_tool',['solar_forge']),('blocks_motion_no_leaves',['solar_forge','pocket_boundary'])]:
    p=RES/f'data/minecraft/tags/block/{tag}.json';data=json.loads(p.read_text()) if p.exists() else {'values':[]}
    data['values']+= [f'{MOD}:{n}' for n in names if f'{MOD}:{n}' not in data['values']];write(f'data/minecraft/tags/block/{tag}.json',data)
# Private rooms use a void level and a saved per-player room allocation.
dtype=json.loads((RES/f'data/{MOD}/dimension_type/burned_realm.json').read_text())
dtype.update(ambient_light=1,has_skylight=False)
dtype['attributes']={'minecraft:visual/sky_color':'#e5dac4','minecraft:visual/fog_color':'#e5dac4','minecraft:gameplay/bed_rule':{'can_set_spawn':'never','can_sleep':'never'},'minecraft:gameplay/piglins_zombify':False,'minecraft:gameplay/respawn_anchor_works':False}
write(f'data/{MOD}/dimension_type/pocket_dimension.json',dtype)
dimension={'type':f'{MOD}:pocket_dimension','generator':{'type':'minecraft:flat','settings':{'biome':'minecraft:plains','features':False,'lakes':False,'layers':[{'height':1,'block':'minecraft:air'}]}}}
write(f'data/{MOD}/dimension/pocket_dimension.json',dimension)
p=root/'src/gametest/resources/data/minecraft/worldgen/world_preset/flat_all_dimensions.json'
data=json.loads(p.read_text());data['dimensions'][f'{MOD}:pocket_dimension']=dimension;p.write_text(json.dumps(data,indent=2)+'\n')
# Preserve the artist's model geometry, display transforms, textures and animation timing.
source=root.parent/'TheDarkDimensions NutzeDieseResourcen!/Lightning Blade'
blade=root/'tools/texture_sources/stormveil'
if source.exists():shutil.copytree(source,blade,dirs_exist_ok=True)
cit=blade/'optifine/cit/kaminari_pack'
textures=RES/f'assets/{MOD}/textures/item/stormveil';textures.mkdir(parents=True,exist_ok=True)
for p in cit.glob('*.png*'):shutil.copyfile(p,textures/p.name)
model=json.loads((cit/'kaminari.json').read_text())
# Blockbench exports unassigned faces as #missing, including zero-area sheet edges.
# Omit these faces natively rather than displaying Minecraft's error checkerboard.
for element in model['elements']:
    element['faces']={side:face for side,face in element['faces'].items() if face.get('texture')!='#missing'}
model['textures']={k:f'{MOD}:item/stormveil/{v.removeprefix("./")}' for k,v in model['textures'].items()}
# Native models support per-element full-bright emissive sheets.
for k,sheet in [('e3','kaminari_sheet_1_e'),('e4','kaminari_sheet_2_e')]:model['textures'][k]=f'{MOD}:item/stormveil/{sheet}'
extra=[]
for original in model['elements']:
    faces={k:copy.deepcopy(v) for k,v in original['faces'].items() if v.get('texture') in ['#3','#4']}
    if not faces:continue
    e=copy.deepcopy(original);e['faces']=faces;e['light_emission']=15
    e['from']=[v-0.001 for v in e['from']];e['to']=[v+0.001 for v in e['to']]
    for f in faces.values():f['texture']='#e'+f['texture'][1:]
    extra.append(e)
model['elements']+=extra
write(f'assets/{MOD}/models/item/stormveil.json',model)
write(f'assets/{MOD}/items/stormveil.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/stormveil'}})
particle=RES/f'assets/{MOD}/textures/particle/stormveil';particle.mkdir(parents=True,exist_ok=True)
for p in (blade/'textures/particle').glob('*.png'):shutil.copyfile(p,particle/p.name)
write(f'assets/{MOD}/particles/stormveil_sweep.json',{'textures':[f'{MOD}:stormveil/sweep_{i}' for i in range(8)]})
write(f'assets/{MOD}/particles/stormveil_crit.json',{'textures':[f'{MOD}:stormveil/critical_hit']})
write(f'data/{MOD}/damage_type/stormveil_arc.json',{'message_id':'lightningBolt','exhaustion':0.0,'scaling':'never'})
for tag in ['bypasses_cooldown','is_lightning']:
    write(f'data/minecraft/tags/damage_type/{tag}.json',{'replace':False,'values':[f'{MOD}:stormveil_arc']})
for path in ['data/thedarkdimensions/tags/item/improved_tools.json','data/minecraft/tags/item/swords.json','data/minecraft/tags/item/enchantable/sword.json','data/minecraft/tags/item/enchantable/weapon.json','data/minecraft/tags/item/enchantable/sharp_weapon.json','data/minecraft/tags/item/enchantable/durability.json']:
    p=RES/path;data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
    if f'{MOD}:stormveil' not in data['values']:data['values'].append(f'{MOD}:stormveil')
    write(path,data)
for lang in ['en_us','de_de']:
    path=f'assets/{MOD}/lang/{lang}.json';labels=json.loads((RES/path).read_text(encoding='utf-8'))
    labels.update({'item.thedarkdimensions.solar_essence':'Solar Essence','item.thedarkdimensions.pocket_dimension_key':'Pocket Dimension Key','item.thedarkdimensions.stormveil':'Stormveil','block.thedarkdimensions.solar_forge':'Solar Forge','container.thedarkdimensions.solar_forge':'Solar Forge','block.thedarkdimensions.pocket_boundary':'Pocket Boundary',
    'message.thedarkdimensions.pocket_missing':'Pocket dimension unavailable.' if lang=='en_us' else 'Pocket Dimension nicht verfügbar.',
    'message.thedarkdimensions.pocket_full':'Your pocket has no safe entry. Clear space inside first.' if lang=='en_us' else 'Deine Pocket hat keinen freien Eingang. Räume zuerst innen Platz frei.'})
    write(path,labels)
print('Solar progression, private pocket level, Solar Forge and supplied animated Stormveil imported.')
