"""Supplied relic katanas and a pre-End Ancient Resonance chapter."""
import generate_finale
from generate_resources import RES,MOD,ROOT,write,recipe
from pathlib import Path
import json,copy,shutil

def mod(name):return f'{MOD}:{name}'
archive=ROOT/'tools/texture_sources/relic_katanas'
source=ROOT.parent/'TheDarkDimensions NutzeDieseResourcen!'
for folder in ['Basic Katana','Mindshifter Katana','Hells Blade']:
    if (source/folder).exists():shutil.copytree(source/folder,archive/folder,dirs_exist_ok=True)
manifest={}

def port(name,folder,model_path,texture_folder,off_model=None):
    base=archive/folder/'minecraft';textures=base/texture_folder;dest=RES/f'assets/{MOD}/textures/item/{name}';dest.mkdir(parents=True,exist_ok=True)
    for path in textures.glob('*.png*'):shutil.copyfile(path,dest/path.name)
    for suffix,path in [('',model_path),('_off',off_model)]:
        if not path:continue
        raw=json.loads((base/path).read_text(encoding='utf-8'));model=copy.deepcopy(raw)
        model['textures']={k:mod(f'item/{name}/'+v.split('/')[-1]) for k,v in raw['textures'].items()}
        extras=[]
        for key,value in list(model['textures'].items()):
            if key=='particle':continue
            stem=value.split('/')[-1]
            if not (textures/(stem+'_e.png')).exists():continue
            emissive='glow_'+key;model['textures'][emissive]=mod(f'item/{name}/{stem}_e')
            for original in raw['elements']:
                faces={face:copy.deepcopy(spec) for face,spec in original['faces'].items() if spec.get('texture')=='#'+key}
                if not faces:continue
                layer=copy.deepcopy(original);layer['faces']=faces;layer['light_emission']=15
                # Tiny outward offset avoids coplanar z-fighting. UVs and native mesh stay intact.
                layer['from']=[v-.001 for v in layer['from']];layer['to']=[v+.001 for v in layer['to']]
                for spec in layer['faces'].values():spec['texture']='#'+emissive
                extras.append(layer)
        model['elements']+=extras;write(f'assets/{MOD}/models/item/{name}{suffix}.json',model)
        manifest[name+suffix]={'source':str((base/path).relative_to(archive)).replace('\\','/'),'original_elements':len(raw['elements']),'emissive_layers':len(extras),'credit':raw.get('credit','')}
    native={'type':'minecraft:model','model':mod('item/'+name)}
    if off_model:
        # CIT's sheathed off-hand variant, with handedness respected for left-handed players.
        left={'type':'minecraft:model','model':mod('item/'+name+'_off')}
        right_handed={'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':['firstperson_lefthand','thirdperson_lefthand'],'model':left}],'fallback':native}
        left_handed={'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':['firstperson_righthand','thirdperson_righthand'],'model':left}],'fallback':native}
        native={'type':'minecraft:select','property':'minecraft:main_hand','cases':[{'when':'left','model':left_handed}],'fallback':right_handed}
    write(f'assets/{MOD}/items/{name}.json',{'model':native})

# Keep all seven supplied material designs archived; the common foundation uses the iron design.
port('basic_katana','Basic Katana','models/item/iron_sword.json','textures/item')
port('mindshifter','Mindshifter Katana','citresewn/cit/katana/mindshifter.json','citresewn/cit/textures','citresewn/cit/katana/mindshifter_off.json')
port('hellsblade','Hells Blade','citresewn/cit/murasama/murasama.json','citresewn/cit/textures','citresewn/cit/murasama/murasama_off.json')
write(f'assets/{MOD}/relic_katana_sources.json',manifest)

recipe('basic_katana',['  I',' I ','S  '],{'I':'minecraft:iron_ingot','S':mod('reinforced_stick')})
for name in ['frostgleam','bloodgrief','souleater']:
    path=f'data/{MOD}/recipe/{name}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data['key']['S']=mod('basic_katana');write(path,data)
recipe('stormveil',ingredients=[mod('lightning_core'),mod('solar_essence'),mod('basic_katana')])
recipe('mindshifter',ingredients=[mod('basic_katana'),mod('chronus_fragment'),mod('chronus_fragment'),mod('essentialium_infused_metalinium_ingot'),mod('essentialium_infused_metalinium_ingot'),'minecraft:ender_pearl'])
recipe('hellsblade',ingredients=[mod('basic_katana'),mod('fire_core'),mod('fire_core'),'minecraft:redstone_block','minecraft:netherite_scrap'])

for tag in ['minecraft:swords','minecraft:enchantable/sword','minecraft:enchantable/sharp_weapon','minecraft:enchantable/weapon','minecraft:enchantable/durability',mod('katana_enchantable'),mod('improved_tools')]:
    namespace,path=tag.split(':');file=f'data/{namespace}/tags/item/{path}.json';data=json.loads((RES/file).read_text(encoding='utf-8')) if (RES/file).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+[mod(n) for n in (['mindshifter','hellsblade'] if path=='improved_tools' else ['basic_katana','mindshifter','hellsblade'])]));write(file,data)
for name in ['basic_katana','mindshifter','hellsblade']:
    data=json.loads((RES/f'data/{MOD}/weapon_attributes/frostgleam.json').read_text(encoding='utf-8'));write(f'data/{MOD}/weapon_attributes/{name}.json',data)
write(f'data/{MOD}/damage_type/hellsblade_pulse.json',{'message_id':'magic','exhaustion':0.0,'scaling':'never'})
for tag in ['bypasses_cooldown','bypasses_armor','is_magic']:
    path=f'data/minecraft/tags/damage_type/{tag}.json';data=json.loads((RES/path).read_text(encoding='utf-8')) if (RES/path).exists() else {'replace':False,'values':[]};data['values']=sorted(set(data['values']+[mod('hellsblade_pulse')]));write(path,data)

# Complete, identifiable weapons are rare side rewards, never replacements for trial Sigils.
def treasure(name,chance):return {'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':chance}],'entries':[{'type':'minecraft:item','name':mod(name)}]}
for chest,name,chance in [('chronus_sanctum','mindshifter',.04),('sunken_archive','mindshifter',.015),('elemental_shrine','hellsblade',.025),('hidden_cache','mindshifter',.005)]:
    path=f'data/{MOD}/loot_table/chests/{chest}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data['pools'].append(treasure(name,chance));write(path,data)
for chest,name,chance in [('end_city_treasure','mindshifter',.02),('nether_bridge','hellsblade',.02),('bastion_treasure','hellsblade',.03)]:
    table='relic_'+chest
    write(f'data/{MOD}/loot_modifiers/{table}.json',{'type':'neoforge:add_table','condition':{'type':'neoforge:loot_table_id','loot_table_id':'minecraft:chests/'+chest},'table':mod('chests/'+table)})
    write(f'data/{MOD}/loot_table/chests/{table}.json',{'type':'minecraft:chest','pools':[treasure(name,chance)]})

# Remove the End City/End Crystal cycle. All ingredients are obtainable before entering the realm.
recipe('astral_crystal_wand',['F','G','S'],{'F':mod('fire_core'),'G':'minecraft:ghast_tear','S':mod('reinforced_stick')})
write('data/minecraft/recipe/ender_eye.json',{'type':mod('resonant_ender_eye'),'category':'misc','ingredients':['minecraft:ender_pearl','minecraft:blaze_powder',mod('essentialium')],'result':{'id':'minecraft:ender_eye'}})
# Replace the vanilla recipe-book trigger; the milestone awards the native recipe to everyone.
write('data/minecraft/advancement/recipes/misc/ender_eye.json',{'parent':'minecraft:recipes/root','criteria':{'ready':{'trigger':'minecraft:impossible'}},'requirements':[['ready']],'rewards':{'recipes':['minecraft:ender_eye']}})
write(f'data/{MOD}/advancement/resonance/end_unsealed.json',{'parent':mod('exploration/enter_realm'),'display':{'icon':{'id':'minecraft:ender_eye'},'title':{'translate':f'end_progression.{MOD}.title'},'description':{'translate':f'end_progression.{MOD}.description'},'frame':'goal','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']],'rewards':{'recipes':['minecraft:ender_eye']}})

for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));de=lang=='de_de'
    for name,label in [('basic_katana','Basic Katana'),('mindshifter','Mindshifter'),('hellsblade','Hellsblade')]:data[f'item.{MOD}.{name}']=label
    data[f'perk.{MOD}.mind_fracture']='Mind Fracture · dritter Treffer schwächt' if de else 'Mind Fracture · third hit weakens'
    data[f'perk.{MOD}.crimson_discharge']='Crimson Discharge · dritter Treffer entlädt' if de else 'Crimson Discharge · third hit discharges'
    guides={
      'basic_katana':('2 Iron Ingots diagonal über einem Improved Stick. 5 Schaden, 1,6 Tempo, normales Schwert-Reichweitenprofil. Grundlage aller sechs besonderen Katana-Rezepte; unterstützt Parry, Dodge und Slashing.','Two diagonal iron ingots above an Improved Stick. Five damage, 1.6 speed, normal sword reach. Foundation for all six special katana recipes; supports parry, dodge and Slashing.'),
      'mindshifter':('8,5 Schaden, 1,5 Tempo, +2 Reichweite. Treffer: Slowness I für 1,5 Sekunden. Dritter Treffer: Weakness I für 4 Sekunden. Rezept: Basic Katana + 2 Chronus Fragments + 2 Chronus Infused Ingots + Ender Pearl. Selten in Chronus Sanctums (4 %), Sunken Archives (1,5 %), versteckten Kammern (0,5 %) und End Cities (2 %).','8.5 damage, 1.5 speed, +2 reach. Hits: Slowness I for 1.5 seconds. Third hit: Weakness I for four seconds. Recipe: Basic Katana + two Chronus Fragments + two Chronus Infused Ingots + Ender Pearl. Rare in Chronus Sanctums (4%), Sunken Archives (1.5%), hidden caches (0.5%) and End Cities (2%).'),
      'hellsblade':('9,5 Schaden, 1,4 Tempo, +2 Reichweite. Entzündet das Ziel kurz; dritter Treffer verursacht einen roten Impuls mit 2 Magieschaden. Rezept: Basic Katana + 2 Fire Cores + Redstone Block + Netherite Scrap. Selten in Netherfestungen (2 %), Bastionsschatzkisten (3 %) und Elemental Shrines (2,5 %).','9.5 damage, 1.4 speed, +2 reach. Briefly ignites targets; third hit releases a red pulse for two magic damage. Recipe: Basic Katana + two Fire Cores + Redstone Block + Netherite Scrap. Rare in Nether fortresses (2%), Bastion treasure chests (3%) and Elemental Shrines (2.5%).'),
      'end_progression':('Nether → Burned Dimension → drei Höhlenprüfungen → End. Portalstab: Fire Core / Ghast Tear / Improved Stick, senkrecht. Die drei unterschiedlichen abgeschlossenen Prüfungen schalten Enderaugen dauerhaft für die Welt frei. Danach: Ender Pearl + Blaze Powder + Essentialium → Enderauge. Funktioniert auch im Crafter; vorherige Prüfungen werden aus alten Spielständen übernommen.','Nether → Burned Realm → three cave trials → End. Portal wand: Fire Core / Ghast Tear / Improved Stick vertically. Completing the three distinct trials permanently unlocks Eyes of Ender for the world. Then: Ender Pearl + Blaze Powder + Essentialium → Eye of Ender. Also works in a Crafter; previous trial receipts migrate from older saves.'),
    }
    for key,values in guides.items():data[f'guide.{MOD}.{key}']=values[0 if de else 1]
    data[f'guide.{MOD}.astral_crystal_wand']=('Fire Core oben, Ghast Tear mittig, Improved Stick darunter. Bereits vor dem End herstellbar; verbessert einen vollständigen Obsidian-Portalrahmen.' if de else 'Fire Core above a Ghast Tear above an Improved Stick. Available before the End; upgrades a complete obsidian portal frame.')
    data[f'guide.{MOD}.stormveil']=data[f'guide.{MOD}.stormveil'].replace('Improved Stick','Basic Katana')
    data[f'guide.{MOD}.katanas']+=(' Alle Katana-Rezepte verwenden jetzt ein Basic Katana statt des Improved Sticks.' if de else 'All special katana recipes now use a Basic Katana instead of an Improved Stick.')
    data[f'end_progression.{MOD}.title']='Das End erwacht' if de else 'The End Awakens'
    data[f'end_progression.{MOD}.description']='Alle drei Prüfungen sind abgeschlossen. Enderaugen können hergestellt werden.' if de else 'All three trials are complete. Eyes of Ender can now be crafted.'
    data[f'end_progression.{MOD}.journal_locked']='Weg ins End\nAbgeschlossene Prüfungen der Welt: %s/3.\n\nNether → Burned Dimension → Cinder Crucible, Sunken Reliquary, Luminous Hollow → Enderaugen.\n\nEnderaugen benötigen zusätzlich Essentialium.' if de else 'Path to the End\nWorld trials complete: %s/3.\n\nNether → Burned Realm → Cinder Crucible, Sunken Reliquary, Luminous Hollow → Eyes of Ender.\n\nEyes also require Essentialium.'
    data[f'end_progression.{MOD}.journal_ready']='Das End ist freigeschaltet\nEnder Pearl + Blaze Powder + Essentialium → Enderauge.\n\nDie drei Prüfungen bleiben für diese Welt abgeschlossen; die Sigils öffnen weiterhin den Resonance Nexus.' if de else 'The End is unsealed\nEnder Pearl + Blaze Powder + Essentialium → Eye of Ender.\n\nThe three trials stay completed for this world; your Sigils still open the Resonance Nexus.'
    write(path,data)
print('Installed three original katana designs, native hand variants/emission/animation, common foundation recipes, rare treasures and pre-End trial progression.')

