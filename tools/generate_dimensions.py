"""Generate chapter two assets. Run after generate_resources.py; requires Pillow."""
from generate_resources import RES, MOD, write, save, item, recipe
from pathlib import Path
from PIL import Image, ImageDraw
import json, random, copy
rng=random.Random(26302)
blocks=['burned_dirt','burned_gravel','burned_stone','burned_deepslate','burned_cobblestone','burned_bedrock','burned_log','burned_planks','burned_leaves','burned_bricks','burned_coal_ore','burned_iron_ore','improved_amethyst_crystal_ore','ember_stone','echo_crystal','echo_relic','improved_obsidian']
noitems={'burned_bedrock','improved_obsidian'}
for name in blocks:
    im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
    for y in range(16):
        for x in range(16):
            base=rng.randrange(16,48)
            im.putpixel((x,y),(base+5,base,base+3,255))
    if name=='burned_log':
        for x in [2,5,10,13]: d.line((x,0,x,15),fill='#080a0b')
    if name in ['burned_bricks','burned_planks']:
        for y in [3,7,11,15]: d.line((0,y,15,y),fill='#090b10')
    colors={'burned_coal_ore':'#090809','burned_iron_ore':'#907667','improved_amethyst_crystal_ore':'#a986ef','ember_stone':'#ed6525','echo_relic':'#41bfca','echo_crystal':'#7659b1','improved_obsidian':'#5b4268'}
    if name in colors:
        for i in range(14):
            x,y=rng.randrange(15),rng.randrange(15); d.rectangle((x,y,x+1,y+1),fill=colors[name])
    save(im,'block/'+name)
    write(f'assets/{MOD}/models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'{MOD}:block/{name}'}})
    variants={'':{'model':f'{MOD}:block/{name}'}}
    if name=='burned_log': variants={'axis=y':{'model':f'{MOD}:block/{name}'},'axis=x':{'model':f'{MOD}:block/{name}','x':90,'y':90},'axis=z':{'model':f'{MOD}:block/{name}','x':90}}
    write(f'assets/{MOD}/blockstates/{name}.json',{'variants':variants})
    if name not in noitems:
        write(f'assets/{MOD}/models/item/{name}.json',{'parent':f'{MOD}:block/{name}'})
        write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
    drop={'burned_stone':f'{MOD}:burned_cobblestone','burned_coal_ore':'minecraft:coal','burned_iron_ore':'minecraft:raw_iron','improved_amethyst_crystal_ore':f'{MOD}:improved_amethyst','echo_relic':'minecraft:echo_shard'}.get(name,f'{MOD}:{name}')
    entry={'type':'minecraft:item','name':drop}
    if name in ['burned_coal_ore','improved_amethyst_crystal_ore']:
        entry['functions']=[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'}]
    if drop!=f'{MOD}:{name}':
        entry={'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':f'{MOD}:{name}','conditions':[{'condition':'minecraft:match_tool','predicate':{'predicates':{'minecraft:enchantments':[{'enchantments':'minecraft:silk_touch','levels':{'min':1}}]}}}]},entry]}
    write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[] if name in noitems else [{'rolls':1,'entries':[entry],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
for name,color in [('improved_amethyst','#ad83eb'),('improved_echo_shard','#4ed5d8'),('burned_hoglin_spawn_egg','#e07335'),('burned_pigman_spawn_egg','#b26665')]:
    im=Image.new('RGBA',(16,16)); d=ImageDraw.Draw(im)
    if 'egg' in name: d.ellipse((4,2,12,14),fill='#232024',outline=color)
    else: d.polygon([(5,14),(3,7),(9,1),(12,4),(11,10)],fill=color,outline='#342e49'); d.line((6,11,9,3),fill='#d3ffff')
    save(im,'item/'+name); item(name)
for name in ['burned_hoglin','burned_pigman']:
    im=Image.new('RGBA',(64,64))
    for y in range(64):
        for x in range(64):
            g=rng.randrange(18,48); im.putpixel((x,y),(g+6,g,g+3,255))
    d=ImageDraw.Draw(im)
    for i in range(65):
        x,y=rng.randrange(63),rng.randrange(63); d.line((x,y,x+1,y+2),fill='#c45e2c')
    d.rectangle((9,11,10,12),fill='#ffbe62'); d.rectangle((13,11,14,12),fill='#ffbe62')
    save(im,'entity/'+name)
for suffix in ['ns','ew']:
    data=json.loads((RES/f'assets/{MOD}/models/block/improved_nether_portal_{suffix}.json').read_text())
    data['textures']={k:v.replace('improved_nether_portal','echo_portal') for k,v in data['textures'].items()}
    write(f'assets/{MOD}/models/block/echo_portal_{suffix}.json',data)
write(f'assets/{MOD}/blockstates/echo_portal.json',{'variants':{'axis=x':{'model':f'{MOD}:block/echo_portal_ns'},'axis=z':{'model':f'{MOD}:block/echo_portal_ew'}}})
im=Image.open(RES/f'assets/{MOD}/textures/block/improved_nether_portal.png').convert('RGBA')
im=im.point(lambda v:v)
for y in range(im.height):
    for x in range(im.width):
        r,g,b,a=im.getpixel((x,y)); im.putpixel((x,y),(b//2,g//2,b,a))
save(im,'block/echo_portal'); write(f'assets/{MOD}/textures/block/echo_portal.png.mcmeta',{'animation':{'frametime':2,'interpolate':True}})
recipe('improved_amethyst',['PPP','PAP','PDP'],{'P':'minecraft:glass_pane','A':'minecraft:amethyst_shard','D':'minecraft:diamond'})
recipe('improved_echo_shard',['AAA','AEA','AAA'],{'A':f'{MOD}:improved_amethyst','E':'minecraft:echo_shard'})
recipe('burned_planks',ingredients=[f'{MOD}:burned_log']); p=RES/f'data/{MOD}/recipe/burned_planks.json'; data=json.loads(p.read_text()); data['result']['count']=4; write(str(p.relative_to(RES)),data)
recipe('burned_wood_sticks',['P','P'],{'P':f'{MOD}:burned_planks'},result='improved_stick'); p=RES/f'data/{MOD}/recipe/burned_wood_sticks.json'; data=json.loads(p.read_text()); data['result']['count']=4; write(str(p.relative_to(RES)),data)
recipe('burned_bricks',['SS','SS'],{'S':f'{MOD}:burned_stone'}); p=RES/f'data/{MOD}/recipe/burned_bricks.json'; data=json.loads(p.read_text()); data['result']['count']=4; write(str(p.relative_to(RES)),data)
write(f'data/minecraft/tags/item/planks.json',{'replace':False,'values':[f'{MOD}:burned_planks']})
for tag in ['stone_crafting_materials','stone_tool_materials']:
    write(f'data/minecraft/tags/item/{tag}.json',{'replace':False,'values':[f'{MOD}:burned_cobblestone']})
write(f'data/{MOD}/recipe/burned_stone_smelting.json',{'type':'minecraft:smelting','category':'blocks','ingredient':f'{MOD}:burned_cobblestone','result':{'id':f'{MOD}:burned_stone'},'experience':0.1,'cookingtime':200})
for tag,names in {'mineable/pickaxe':[n for n in blocks if n not in ['burned_dirt','burned_gravel','burned_log','burned_planks','burned_leaves']], 'mineable/shovel':['black_sand','burned_dirt','burned_gravel'],'mineable/axe':['burned_log','burned_planks'],'needs_diamond_tool':['improved_amethyst_crystal_ore','echo_relic'],'needs_stone_tool':['burned_iron_ore']}.items():
    write(f'data/minecraft/tags/block/{tag}.json',{'replace':False,'values':[f'{MOD}:{n}' for n in names]})
write(f'data/{MOD}/tags/block/burned_ore_replaceables.json',{'values':[f'{MOD}:burned_stone',f'{MOD}:burned_deepslate']})
def block(name):return {'type':'minecraft:block','result_state':f'{MOD}:{name}'}
def condition(test,result):return {'type':'minecraft:condition','if_true':test,'then_run':result}
surface={'type':'minecraft:sequence','sequence':[condition({'type':'minecraft:noise_threshold','noise':'minecraft:surface','min_threshold':0.0,'max_threshold':1.7976931348623157e308},block('burned_gravel')),block('burned_dirt')]}
material={'type':'minecraft:sequence','sequence':[condition({'type':'minecraft:vertical_gradient','random_name':f'{MOD}:bedrock','true_at_and_below':{'above_bottom':0},'false_at_and_above':{'above_bottom':5}},block('burned_bedrock')),condition({'type':'minecraft:above_preliminary_surface'},condition('minecraft:under_floor',surface)),condition({'type':'minecraft:vertical_gradient','random_name':f'{MOD}:deepslate','true_at_and_below':{'absolute':0},'false_at_and_above':{'absolute':8}},block('burned_deepslate')),block('burned_stone')]}
write(f'data/{MOD}/worldgen/material_rule/burned_surface.json',material)
noise=json.loads((Path(__file__).parent/'templates/overworld_noise.json').read_text()); noise['default_block']=f'{MOD}:burned_stone'; noise['material_rule']=f'{MOD}:burned_surface'
write(f'data/{MOD}/worldgen/noise_settings/burned_realm.json',noise)
dimension_type={'ambient_light':0.07,'attributes':{'minecraft:visual/sky_color':'#090811','minecraft:visual/fog_color':'#17121d','minecraft:visual/sky_light_factor':0.08,'minecraft:gameplay/sky_light_level':4,'minecraft:gameplay/piglins_zombify':False,'minecraft:gameplay/nether_portal_spawns_piglin':False,'minecraft:gameplay/respawn_anchor_works':True,'minecraft:gameplay/bed_rule':{'can_set_spawn':'always','can_sleep':'never'},'minecraft:audio/ambient_sounds':{'mood':{'block_search_extent':8,'offset':2.0,'sound':'minecraft:ambient.cave','tick_delay':4000}}},'coordinate_scale':1.0,'has_ceiling':False,'has_skylight':True,'has_fixed_time':True,'height':384,'logical_height':384,'min_y':-64,'infiniburn':'#minecraft:infiniburn_overworld','monster_spawn_block_light_limit':0,'monster_spawn_light_level':7,'skybox':'none'}
dimension_type['has_ender_dragon_fight']=False
dimension_type['ambient_light']=0.12
dimension_type['attributes']['minecraft:visual/sky_light_factor']=0.15
dimension_type['attributes']['minecraft:visual/fog_end_distance']=110.0
for dimension in ['burned_realm','echo_depths']:
    write(f'data/{MOD}/dimension_type/{dimension}.json',dimension_type)
    biomes=['ash_wastes','charred_forest','crystal_hollows'] if dimension=='burned_realm' else ['echo_fields']
    source={'type':'minecraft:fixed','biome':f'{MOD}:{biomes[0]}'} if len(biomes)==1 else {'type':'minecraft:multi_noise','biomes':[{'biome':f'{MOD}:{biome}','parameters':{'temperature':0,'humidity':humid,'continentalness':[-1,1],'erosion':[-1,1],'depth':[-1,1],'weirdness':[-1,1],'offset':0}} for biome,humid in zip(biomes,[-0.8,0,0.8])]}
    write(f'data/{MOD}/dimension/{dimension}.json',{'type':f'{MOD}:{dimension}','generator':{'type':'minecraft:noise','settings':f'{MOD}:burned_realm','biome_source':source}})
for name in ['burned_tree','echo_ruin','crystal_growth']:write(f'data/{MOD}/worldgen/feature/{name}.json',{'type':f'{MOD}:{name}'})
def placed(name,feature,placements):write(f'data/{MOD}/worldgen/placed_feature/{name}.json',{'feature':f'{MOD}:{feature}','placement':placements})
count=lambda n:{'type':'minecraft:count','count':n}
square={'type':'minecraft:in_square'}; biome_filter={'type':'minecraft:biome'}
heightmap={'type':'minecraft:heightmap','heightmap':'WORLD_SURFACE_WG'}
placed('burned_trees','burned_tree',[count(7),square,heightmap,biome_filter])
placed('echo_ruins','echo_ruin',[{'type':'minecraft:rarity_filter','chance':24},square,heightmap,biome_filter])
placed('surface_crystals','crystal_growth',[count(3),square,heightmap,biome_filter])
height=lambda low,high:{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':low},'max_inclusive':{'absolute':high}}}
placed('cave_crystals','crystal_growth',[count(18),square,height(-55,48),biome_filter])
for ore,size,n,low,high in [('improved_amethyst_crystal_ore',7,12,-56,40),('burned_coal_ore',14,12,0,160),('burned_iron_ore',9,10,-48,80),('ember_stone',12,7,-50,100)]:
    write(f'data/{MOD}/worldgen/feature/{ore}.json',{'type':'minecraft:ore','discard_chance_on_air_exposure':0.0,'size':size,'targets':[{'state':f'{MOD}:{ore}','target':{'predicate_type':'minecraft:tag_match','tag':f'{MOD}:burned_ore_replaceables'}}]})
    placed(ore,ore,[count(n),square,height(low,high),biome_filter])
base=json.loads((RES/f'data/{MOD}/worldgen/biome/black_desert.json').read_text())
for name in ['ash_wastes','charred_forest','crystal_hollows','echo_fields']:
    data=copy.deepcopy(base); data['carvers']=[]; data['climate']={'has_precipitation':False,'temperature':0.8,'downfall':0.0}; data['effects']['water_color']='#282139'
    attrs=data['attributes']; attrs['minecraft:visual/sky_color']='#090811'; attrs['minecraft:visual/fog_color']='#17121d'
    attrs['minecraft:visual/ambient_particles']=[{'particle':{'type':'minecraft:ash'},'probability':0.008}]
    attrs['minecraft:gameplay/natural_mob_spawns']['argument']={'spawn_costs':{},'spawns_by_category':{'monster':[{'type':f'{MOD}:burned_hoglin','weight':35,'count':{'type':'minecraft:uniform','min_inclusive':2,'max_inclusive':3}},{'type':f'{MOD}:burned_pigman','weight':65,'count':{'type':'minecraft:uniform','min_inclusive':2,'max_inclusive':4}}]}}
    data['features']=[[] for i in range(11)]; data['features'][4]=[f'{MOD}:echo_ruins'] if name!='echo_fields' else []
    data['features'][6]=[f'{MOD}:{ore}' for ore in ['burned_coal_ore','burned_iron_ore','improved_amethyst_crystal_ore','ember_stone']]
    data['features'][9]=[f'{MOD}:cave_crystals']
    if name=='charred_forest':data['features'][9].append(f'{MOD}:burned_trees')
    if name in ['crystal_hollows','echo_fields']:data['features'][9].append(f'{MOD}:surface_crystals')
    write(f'data/{MOD}/worldgen/biome/{name}.json',data)
for mob,extra in [('burned_hoglin','minecraft:cooked_porkchop'),('burned_pigman','minecraft:gold_nugget')]:
    write(f'data/{MOD}/loot_table/entities/{mob}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':drop,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3}}]}]} for drop in [f'{MOD}:ashes',extra]]})
for lang in ['en_us','de_de']:
    data=json.loads((RES/f'assets/{MOD}/lang/{lang}.json').read_text(encoding='utf-8'))
    for n in blocks:data[f'block.{MOD}.{n}']=n.replace('_',' ').title()
    for n in ['improved_amethyst','improved_echo_shard','burned_hoglin_spawn_egg','burned_pigman_spawn_egg']:data[f'item.{MOD}.{n}']=n.replace('_',' ').title()
    for n in ['burned_hoglin','burned_pigman']:data[f'entity.{MOD}.{n}']=n.replace('_',' ').title()
    for n in ['ash_wastes','charred_forest','crystal_hollows','echo_fields']:data[f'biome.{MOD}.{n}']=n.replace('_',' ').title()
    data[f'block.{MOD}.echo_portal']='Echo Portal'
    data[f'message.{MOD}.portal_improved']='Portal verbessert. Betritt das Burned Realm!' if lang=='de_de' else 'Portal improved. Enter the Burned Realm!'
    data[f'guide.{MOD}.fire_core']=('3 Improved Sticks + 5 weinender Obsidian + 1 Goldblock. Verbessere ein aktives Netherportal und betritt es, um das Burned Realm zu erreichen. Das Ankunftsportal führt zurück.' if lang=='de_de' else '3 Improved Sticks + 5 Crying Obsidian + 1 Gold Block. Upgrade an active Nether Portal; enter it to travel to the Burned Realm. Return through the arrival portal.')
    data[f'guide.{MOD}.improved_amethyst']=('Amethystscherbe in die Mitte, Diamant darunter, alle sieben übrigen Felder mit Glasscheiben füllen. Alternativ Improved Amethyst Crystal Ore mit einer Diamantspitzhacke abbauen.' if lang=='de_de' else 'Craft: glass panes in all slots except Amethyst Shard in the center and Diamond below it. Also mined from Improved Amethyst Crystal Ore with a diamond pickaxe.')
    data[f'guide.{MOD}.improved_echo_shard']=('Umgebe eine Echoscherbe mit 8 Improved Amethyst. Rechtsklick auf einen gefundenen Improved-Obsidian-Rahmen im Burned Realm öffnet die Echo Depths. Echo-Relikte in Ruinen liefern Echoscherben.' if lang=='de_de' else 'Surround an Echo Shard with 8 Improved Amethyst. Right-click a discovered Improved Obsidian frame in the Burned Realm to open the Echo Depths. Echo Relics in ruins drop Echo Shards.')
    write(f'assets/{MOD}/lang/{lang}.json',data)
print('Generated Burned Realm, Echo Depths, biomes, ores, ruins and burned assets.')
