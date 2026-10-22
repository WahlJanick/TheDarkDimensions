"""Back equipment, built-in slot icons and exact supplied wing textures; safe standalone."""
from pathlib import Path
import json,shutil,zipfile,hashlib

ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';MOD='thedarkdimensions'
def write(path,data):
    p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
manifest={}
for item,folder,broken in [('wool_glider','Wool Glider','broken_elytra.png'),('devils_wings','Devils Wings/red demon wings','elytra_broken.png')]:
    origin=ROOT.parent/'TheDarkDimensions NutzeDieseResourcen!'/folder/'assets/minecraft'
    archived=ROOT/'tools/texture_sources/back_equipment'/item
    for source,target in [('textures/item/elytra.png',f'textures/item/{item}.png'),(f'textures/item/{broken}',f'textures/item/{item}_broken.png'),('textures/entity/equipment/wings/elytra.png',f'textures/entity/equipment/wings/{item}.png')]:
        src=origin/source;archive=archived/source
        if src.exists():archive.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(src,archive)
        if not archive.exists():raise FileNotFoundError(src)
        dst=RES/f'assets/{MOD}/{target}';dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(archive,dst)
        manifest[target]={'source':f'{item}/{source}','sha256':hashlib.sha256(archive.read_bytes()).hexdigest()}
    for suffix in ['', '_broken']:write(f'assets/{MOD}/models/item/{item}{suffix}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'{MOD}:item/{item}{suffix}'}})
    write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:condition','property':'minecraft:broken','on_true':{'type':'minecraft:model','model':f'{MOD}:item/{item}_broken'},'on_false':{'type':'minecraft:model','model':f'{MOD}:item/{item}'}}})
    write(f'assets/{MOD}/equipment/{item}.json',{'layers':{'wings':[{'texture':f'{MOD}:{item}','use_player_texture':False}]}})
write(f'assets/{MOD}/back_equipment_sources.json',manifest)

# Native icon art; copy the predefined cape icon so the optional Trinkets jar is not
# required for the client atlas to load this sprite.
with zipfile.ZipFile(ROOT/'.dev/compat/trinkets-4.2.0+26.3.jar') as jar:
    icon=RES/f'assets/{MOD}/textures/gui/sprites/container/slots/glider.png';icon.parent.mkdir(parents=True,exist_ok=True);icon.write_bytes(jar.read('assets/trinkets/textures/gui/sprites/container/slots/cape.png'))
write('assets/minecraft/atlases/gui.json',{'sources':[{'type':'minecraft:single','resource':f'{MOD}:gui/sprites/container/slots/glider','sprite':f'{MOD}:container/slots/glider'}]})

slots=[('chest/dark_spellbook','minecraft:container/slot/banner_pattern',200),('chest/dark_focus','minecraft:container/slot/amethyst_shard',201),('chest/dark_talisman','minecraft:container/slot/emerald',202),('feet/dark_windstep','minecraft:container/slot/boots',0),('chest/dark_back_tool','minecraft:container/slot/sword',203),('chest/dark_glider',f'{MOD}:container/slots/glider',204)]
for path,icon,order in slots:
    predicate=f'{MOD}:back_tool' if path.endswith('dark_back_tool') else f'{MOD}:back_glider' if path.endswith('dark_glider') else 'trinkets:tag'
    data={'amount':1,'order':order,'max_stack_size':1,'icon':icon,'validator_predicates':[predicate],'quick_move_predicates':[predicate],'interact_equipable_predicates':[predicate],'cosmetic_slots':False}
    if path.endswith('dark_back_tool'):data['is_vanity']=True
    write(f'data/trinkets/slots/{path}.json',data)
write('data/trinkets/slots/feet/group.json',{'slot_id':8,'order':-600})
write('data/trinkets/slots/chest/group.json',{'slot_id':6,'order':-1300})
write('data/trinkets/entities/thedarkdimensions_player.json',{'replace':False,'entities':['minecraft:player'],'slots':[s[0] for s in slots]})
write('data/trinkets/tags/item/feet/dark_windstep.json',{'replace':False,'values':[f'{MOD}:windstep_sandals']})
write(f'assets/{MOD}/trinkets/back_gliders.json',{'priority':0,'target':[f'{MOD}:wool_glider',f'{MOD}:devils_wings'],'render':[{'type':'wings'}]})
write('data/c/tags/item/elytra.json',{'replace':False,'values':[f'{MOD}:wool_glider',f'{MOD}:devils_wings']})
write(f'data/{MOD}/recipe/wool_glider.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':[' S ','SWS','WLW'],'key':{'S':'minecraft:stick','W':'#minecraft:wool','L':'minecraft:leather'},'result':{'id':f'{MOD}:wool_glider','count':1}})
write(f'data/{MOD}/recipe/devils_wings.json',{'type':'minecraft:crafting_shaped','category':'equipment','pattern':['SDS','BEB',' T '],'key':{'S':f'{MOD}:soul_core','D':f'{MOD}:devils_eye','B':f'{MOD}:red_netherite_ingot','E':'minecraft:elytra','T':f'{MOD}:devils_tear'},'result':{'id':f'{MOD}:devils_wings','count':1}})
for item in ['wool_glider','devils_wings']:
    write(f'data/{MOD}/advancement/recipes/{item}.json',{'criteria':{'has_material':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:leather' if item=='wool_glider' else 'minecraft:elytra'}]}}},'rewards':{'recipes':[f'{MOD}:{item}']}})

mixins=RES/'thedarkdimensions.mixins.json';data=json.loads(mixins.read_text(encoding='utf-8'))
for name in ['DevilWingsFlightMixin','DevilWingsStartMixin','GliderRocketUseMixin','GliderRocketBoostMixin']:
    if name not in data['mixins']:data['mixins'].append(name)
if 'BackToolRenderMixin' not in data['client']:data['client'].append('BackToolRenderMixin')
write('thedarkdimensions.mixins.json',data)

for lang in ['de_de','en_us']:
    de=lang=='de_de';language_path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/language_path).read_text(encoding='utf-8'))
    translations={
      f'item.{MOD}.wool_glider':('Wool Glider','Wool Glider'),f'item.{MOD}.devils_wings':('Devil Wings','Devil Wings'),
      f'key.{MOD}.swap_back':('Hand und Rückenplatz tauschen','Swap main hand and back slot'),
      f'ui.{MOD}.creative_upgrade':('Creative: Klicken erhöht die Stufe ohne Training.','Creative: click to increase the level without training.'),
      f'ui.{MOD}.creative_learn':('Creative: Klicken lernt den Skill ohne Kosten.','Creative: click to learn without costs.'),
      f'finale.{MOD}.creative_spawn':('Creative-Test: Ashbound Regent beschworen.','Creative practice: summoned the Ashbound Regent.'),
      f'perk.{MOD}.wool_glider':('Leichter Gleiter · schneller Verschleiß · kein Raketenantrieb','Light glider · rapid wear · no firework boost'),
      f'perk.{MOD}.devils_wings':('Unzerstörbar · Lavaflug · kein eigener Feuerschutz','Unbreakable · lava flight · does not grant fire protection'),
      f'guide.{MOD}.back_equipment':('Rückenplatz: jedes Werkzeug oder jede Waffe. X tauscht mit der Haupthand; die Taste ist einstellbar. Gleiterplatz: Elytra, Wool Glider oder Devil Wings, zusätzlich zur Brustplatte. Windstep Sandals passen in den Fuß-Nebenslot. Wool Glider: drei Stöcke + drei Wolle beliebiger Farbe + ein Leder; 128 Haltbarkeit, vier Verschleißpunkte pro Flugsekunde, keine Raketen. Devil Wings: Elytra, zwei Soul Cores, zwei Blood Netherite Ingots, Devil’s Eye und Devil’s Tear; unzerstörbar und Lava zählt beim Gleiten als Luft. Feuerresistenz bleibt separat erforderlich.','Back slot: any tool or weapon. X swaps with the main hand; configurable in controls. Glider slot: Elytra, Wool Glider or Devil Wings alongside chest armor. Windstep Sandals fit the feet accessory slot. Wool Glider: three sticks + three wool of any color + one leather; 128 durability, four wear per flight second, no rockets. Devil Wings: Elytra, two Soul Cores, two Blood Netherite Ingots, Devil’s Eye and Devil’s Tear; unbreakable, with air-like gliding in lava. Fire resistance is still required separately.')
    }
    for key,values in translations.items():data[key]=values[0 if de else 1]
    for path,label in [('chest/dark_spellbook',('Spellbook','Spellbook')),('chest/dark_focus',('Magischer Fokus','Magic focus')),('chest/dark_talisman',('Talisman','Talisman')),('feet/dark_windstep',('Windstep Sandals','Windstep Sandals')),('chest/dark_back_tool',('Rücken-Werkzeug','Back tool')),('chest/dark_glider',('Gleiter','Glider'))]:
        data['trinkets.slot.'+path.replace('/','.')]=label[0 if de else 1]
    write(language_path,data)
print('Generated six native-icon accessory slots, two exact-texture gliders, recipes, wing equipment layers, optional Trinkets rendering and Creative guides.')

