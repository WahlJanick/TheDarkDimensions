"""Generate deterministic pixel assets and 26.3 datapack resources. Run from the project root."""
from pathlib import Path
import json, math, random
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
MOD = 'thedarkdimensions'
def write(path, data):
    if '/loot_table/' in path:
        data = loot_format(data)
    p = RES / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')

def loot_format(value):
    """26.3 uses one condition/modifier holder and the generic type discriminator."""
    if isinstance(value, list): return [loot_format(v) for v in value]
    if not isinstance(value, dict): return value
    result = {k: loot_format(v) for k,v in value.items()}
    # A condition string on an already typed entry is a 26.3 predicate reference.
    if isinstance(result.get('condition'), str) and 'type' not in result: result['type'] = result.pop('condition')
    if 'function' in result: result['type'] = result.pop('function')
    if 'conditions' in result:
        terms = result.pop('conditions')
        if terms: result['condition'] = terms[0] if len(terms)==1 else {'type':'minecraft:all_of','terms':terms}
    if 'functions' in result: result['modifier'] = result.pop('functions')
    return result
def save(image, path):
    p = RES / f'assets/{MOD}/textures/{path}.png'
    p.parent.mkdir(parents=True, exist_ok=True)
    image.save(p)
def item(name, handheld=False):
    write(f'assets/{MOD}/items/{name}.json', {'model': {'type':'minecraft:model', 'model':f'{MOD}:item/{name}'}})
    write(f'assets/{MOD}/models/item/{name}.json', {'parent':'minecraft:item/handheld' if handheld else 'minecraft:item/generated', 'textures':{'layer0':f'{MOD}:item/{name}'}})
def recipe(name, pattern=None, key=None, ingredients=None, result=None, count=1):
    data={'type':'minecraft:crafting_shaped' if pattern else 'minecraft:crafting_shapeless', 'category':'equipment' if 'diamond' in name else 'misc'}
    if pattern: data.update(pattern=pattern,key=key)
    else: data['ingredients']=ingredients
    data['result']={'id':f'{MOD}:{result or name}'}
    if count != 1: data['result']['count']=count
    write(f'data/{MOD}/recipe/{name}.json',data)
    # Recipe book unlocks on acquiring any of the ingredients.
    criterion = next(iter(key.values())) if key else ingredients[0]
    write(f'data/{MOD}/advancement/recipes/{name}.json', {
        'parent':'minecraft:recipes/root',
        'criteria':{'has_ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':criterion}]}},
                    'has_recipe':{'trigger':'minecraft:recipe_unlocked','conditions':{'recipes':[f'{MOD}:{name}']}}},
        'requirements':[['has_ingredient','has_recipe']], 'rewards':{'recipes':[f'{MOD}:{name}']}})

rng=random.Random(3403)
im=Image.new('RGBA',(16,16))
for y in range(16):
    for x in range(16):
        g=rng.choice([17,22,25,29,34,41,48]); im.putpixel((x,y),(g,g,g+2,255))
save(im,'block/black_sand')
write(f'assets/{MOD}/blockstates/black_sand.json', {'variants':{'':{'model':f'{MOD}:block/black_sand'}}})
write(f'assets/{MOD}/models/block/black_sand.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/black_sand'}})
write(f'assets/{MOD}/models/item/black_sand.json',{'parent':f'{MOD}:block/black_sand'})
write(f'assets/{MOD}/items/black_sand.json', {'model':{'type':'minecraft:model','model':f'{MOD}:item/black_sand'}})

for name in ['improved_stick','burned_stick']:
    im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
    d.line((4,13,11,6),fill='#111219',width=3); d.line((4,12,11,5),fill='#3c3d46',width=1)
    if name=='burned_stick':
        d.polygon([(9,7),(7,4),(9,5),(9,1),(11,3),(12,0),(13,4),(12,7)],fill='#ed5a17')
        d.polygon([(10,6),(10,3),(12,5),(11,7)],fill='#ffc741')
    save(im,f'item/{name}'); item(name,True)
im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
d.polygon([(2,13),(4,9),(8,7),(11,10),(14,13),(10,15),(5,15)],fill='#85858a')
for x,y in [(4,12),(7,10),(9,12),(11,13),(6,14)]: d.point((x,y),fill='#d5d2d2')
save(im,'item/ashes'); item('ashes')
im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
d.polygon([(4,14),(1,10),(2,6),(5,4),(6,1),(9,4),(12,2),(14,7),(13,12),(10,15)],fill='#242032')
d.ellipse((4,5,12,13),fill='#df660d',outline='#ffd24c'); d.ellipse((6,7,10,11),fill='#fff19b')
d.line((7,5,7,2),fill='#ff9933',width=2)
save(im,'item/fire_core'); item('fire_core')

# Original diamond tool sprites with charcoal handles.
for kind in ['sword','pickaxe','axe','shovel','hoe']:
    name=f'enhanced_diamond_{kind}'; im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
    d.line((3,13,10,6),fill='#16171b',width=3); d.line((3,12,10,5),fill='#444550',width=1)
    if kind=='sword':
        d.polygon([(7,8),(12,1),(14,1),(14,3),(9,10)],fill='#27b8b4',outline='#0c6267'); d.line((9,7,13,2),fill='#a6ffff'); d.line((6,7,10,11),fill='#2cd7d1',width=2)
    elif kind=='pickaxe':
        d.line((4,3,10,3,13,6,13,9),fill='#0c6267',width=3); d.line((4,2,10,2,13,5),fill='#54eee4',width=2)
    elif kind=='axe':
        d.polygon([(7,2),(12,2),(14,4),(12,8),(8,8),(8,5),(6,4)],fill='#28b8b6',outline='#0c6267'); d.line((8,2,12,2,13,4),fill='#a6ffff')
    elif kind=='shovel':
        d.polygon([(8,4),(12,1),(15,4),(12,8),(9,8)],fill='#28b8b6',outline='#0c6267'); d.line((10,4,12,2,14,4),fill='#a6ffff')
    else:
        d.line((7,2,12,2,14,4),fill='#0c6267',width=3); d.line((7,1,12,1,14,3),fill='#54eee4',width=2)
    save(im,f'item/{name}'); item(name,True)

# Original 64x64 zombie-layout skins, including an independent baby texture.
for name in ['the_burned','the_burned_baby']:
    im=Image.new('RGBA',(64,64),(0,0,0,0))
    rectangles=[(0,0,32,16),(16,16,40,32),(40,16,56,32),(0,16,16,32),(16,48,32,64),(32,48,48,64)]
    for x0,y0,x1,y1 in rectangles:
        for y in range(y0,y1):
            for x in range(x0,x1):
                g=rng.randint(13,38); im.putpixel((x,y),(g,g,g+1,255))
    d=ImageDraw.Draw(im); d.rectangle((9,11,10,11),fill='#d26121'); d.rectangle((13,11,14,11),fill='#d26121')
    d.line((11,14,13,14),fill='#060608'); d.line((20,22,24,28),fill='#633c2c'); d.line((44,21,45,27),fill='#4c3128')
    save(im,f'entity/{name}')
im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
d.ellipse((4,2,12,14),fill='#131418',outline='#706452'); d.rectangle((6,5,8,8),fill='#d26121'); d.rectangle((9,10,11,12),fill='#34353b')
save(im,'item/the_burned_spawn_egg'); item('the_burned_spawn_egg')

im=Image.new('RGBA',(16,16*16))
for frame in range(16):
    for y in range(16):
        for x in range(16):
            phase=math.sin(math.hypot(x-7.5,y-7.5)*1.2-frame*math.pi/8)+math.cos((x-y)*0.6+frame*math.pi/8)
            g=int(80+55*phase); im.putpixel((x,frame*16+y),(max(10,g//5),max(25,g),max(50,g+40),210))
save(im,'block/improved_nether_portal')
write(f'assets/{MOD}/textures/block/improved_nether_portal.png.mcmeta',{'animation':{'frametime':2,'interpolate':True}})
for axis,faces,start,end in [('ns',['north','south'],[0,0,6],[16,16,10]),('ew',['east','west'],[6,0,0],[10,16,16])]:
    write(f'assets/{MOD}/models/block/improved_nether_portal_{axis}.json', {
        'render_type':'minecraft:translucent','textures':{'particle':f'{MOD}:block/improved_nether_portal','portal':f'{MOD}:block/improved_nether_portal'},
        'elements':[{'from':start,'to':end,'faces':{face:{'uv':[0,0,16,16],'texture':'#portal'} for face in faces}}]})
write(f'assets/{MOD}/blockstates/improved_nether_portal.json',{'variants':{'axis=x':{'model':f'{MOD}:block/improved_nether_portal_ns'},'axis=z':{'model':f'{MOD}:block/improved_nether_portal_ew'}}})

recipe('burned_stick',ingredients=[f'{MOD}:ashes','minecraft:flint_and_steel','minecraft:stick'])
patterns={'sword':['D','S','S'],'pickaxe':['DDD',' S ',' S '],'axe':['DD','DS',' S'],'shovel':['D','S','S'],'hoe':['DD',' S',' S']}
for kind,pattern in patterns.items():
    # Sword uses two diamonds and one handle, unlike the shovel.
    if kind=='sword': pattern=['D','D','S']
    recipe(f'enhanced_diamond_{kind}',pattern,{'D':'#minecraft:diamond_tool_materials','S':f'{MOD}:improved_stick'})
recipe('fire_core',['OSO','SGS','OOO'],{'O':'minecraft:crying_obsidian','S':f'{MOD}:improved_stick','G':'minecraft:gold_block'})

write(f'data/{MOD}/loot_table/blocks/black_sand.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:black_sand'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
write(f'data/{MOD}/loot_table/entities/the_burned.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:ashes','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}},{'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]}]})

biome=json.loads((RES/f'data/{MOD}/worldgen/biome/black_desert.json').read_text(encoding='utf-8'))
attrs=biome['attributes']; attrs['minecraft:visual/sky_color']='#484757'; attrs['minecraft:visual/fog_color']='#77717b'
spawns=attrs['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category']
spawns['monster']=[{'type':f'{MOD}:the_burned','count':{'type':'minecraft:uniform','min_inclusive':2,'max_inclusive':4},'weight':100},{'type':'minecraft:enderman','count':1,'weight':5}]
biome['effects']['water_color']='#48516a'
# Features that rely on vanilla sand are omitted, and no vanilla structure tag is overridden.
biome['features'][4]=[]
biome['features'][9]=['minecraft:glow_lichen','minecraft:brown_mushroom_normal','minecraft:red_mushroom_normal']
write(f'data/{MOD}/worldgen/biome/black_desert.json',biome)
write(f'data/minecraft/tags/worldgen/biome/is_overworld.json',{'replace':False,'values':[f'{MOD}:black_desert']})
write(f'data/minecraft/tags/block/sand.json',{'replace':False,'values':[f'{MOD}:black_sand']})
write(f'data/minecraft/tags/item/sand.json',{'replace':False,'values':[f'{MOD}:black_sand']})
write(f'data/minecraft/tags/block/mineable/shovel.json',{'replace':False,'values':[f'{MOD}:black_sand']})
write(f'data/minecraft/tags/block/dragon_immune.json',{'replace':False,'values':[f'{MOD}:improved_nether_portal']})

en={'itemGroup.thedarkdimensions':'The Dark Dimensions','biome.thedarkdimensions.black_desert':'Black Desert',
    'block.thedarkdimensions.black_sand':'Black Sand','block.thedarkdimensions.improved_nether_portal':'Improved Nether Portal',
    'entity.thedarkdimensions.the_burned':'The Burned','message.thedarkdimensions.portal_improved':'Portal improved. The next chapter awaits.',
    'guide.thedarkdimensions.ashes':'The Burned drop 1–3 Ashes. Find them in the Black Desert.',
    'guide.thedarkdimensions.burned_stick':'Craft Ashes + Flint and Steel + Stick. Holding it in either hand deals fire damage. Throw it into water to cool it into an Improved Stick.',
    'guide.thedarkdimensions.improved_stick':'Obtained by throwing a Burned Stick into water. Use instead of normal sticks to craft diamond tools with an enhanced handle: +2 blocks of block and attack reach while held in the main hand.',
    'guide.thedarkdimensions.fire_core':'Craft 3 Improved Sticks, 5 Crying Obsidian and 1 Gold Block. Right-click an active Nether Portal or its frame to upgrade the whole portal. The improved portal has no destination in chapter one.'}
names={'ashes':'Ashes','burned_stick':'Burned Stick','improved_stick':'Improved Stick','fire_core':'Fire Core','the_burned_spawn_egg':'The Burned Spawn Egg'}
for name,label in names.items(): en[f'item.{MOD}.{name}']=label
for kind in patterns: en[f'item.{MOD}.enhanced_diamond_{kind}']=f'Diamond {kind.title()} with Enhanced Handle'
de=dict(en)
de.update({'biome.thedarkdimensions.black_desert':'Schwarze Wüste','block.thedarkdimensions.black_sand':'Schwarzer Sand',
    'block.thedarkdimensions.improved_nether_portal':'Verbessertes Netherportal',
    'message.thedarkdimensions.portal_improved':'Portal verbessert. Das nächste Kapitel folgt.',
    'guide.thedarkdimensions.ashes':'The Burned droppen 1–3 Ashes. Du findest sie in der schwarzen Wüste.',
    'guide.thedarkdimensions.burned_stick':'Ashes + Feuerzeug + Stock craften. Verursacht Feuerschaden, wenn du ihn in einer Hand hältst. Ins Wasser werfen, um einen Improved Stick zu erhalten.',
    'guide.thedarkdimensions.improved_stick':'Wirf einen Burned Stick ins Wasser. Ersetzt Stöcke in Diamantwerkzeug-Rezepten. Mit verstärktem Griff: +2 Blöcke Abbau- und Angriffsreichweite in der Haupthand.',
    'guide.thedarkdimensions.fire_core':'3 Improved Sticks + 5 weinender Obsidian + 1 Goldblock. Rechtsklick auf ein aktives Netherportal oder seinen Rahmen verbessert das gesamte Portal. Im ersten Kapitel hat es noch kein Ziel.'})
write(f'assets/{MOD}/lang/en_us.json',en); write(f'assets/{MOD}/lang/de_de.json',de)
print('Generated recipes, worldgen, loot, models, translations and original pixel textures.')
