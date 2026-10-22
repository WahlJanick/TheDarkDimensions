"""Exploration chapter: native book, two discoverable structures, rewards and advancements."""
import generate_mob_expansion
from generate_resources import RES,MOD,write,recipe,item
from generate_caves import S,I,F,B,C,L,payload
from PIL import Image,ImageDraw
from pathlib import Path
import json,gzip,math

def sprite(name,kind):
    im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
    if kind=='fragment':
        d.polygon([(8,1),(12,5),(10,10),(5,14),(3,9),(5,4)],fill='#262337');d.polygon([(8,2),(10,5),(8,9),(5,12),(5,7)],fill='#8970ba');d.line([(8,3),(7,6),(8,8)],fill='#d9c8ed');d.point((11,3),fill='#c1acd9')
    elif kind=='hourglass':
        d.rectangle((3,1,12,3),fill='#665024');d.rectangle((3,12,12,14),fill='#665024');d.line([(4,4),(5,6),(8,8),(10,10),(11,11)],fill='#a8c0c9');d.line([(11,4),(10,6),(7,8),(5,10),(4,11)],fill='#a8c0c9');d.line([(4,2),(11,2)],fill='#d7b56b');d.line([(4,13),(11,13)],fill='#d7b56b');d.polygon([(5,5),(10,5),(8,7)],fill='#a58ad1');d.polygon([(8,9),(10,11),(5,11)],fill='#a58ad1');d.point((8,8),fill='#e2d4f4')
    elif kind=='book':
        d.polygon([(3,2),(12,1),(14,3),(14,13),(5,15),(2,12),(2,4)],fill='#251d22');d.polygon([(3,3),(12,2),(12,11),(4,13)],fill='#6d4035');d.polygon([(5,13),(13,11),(13,13),(5,14)],fill='#ddd0ab');d.line([(4,4),(4,11)],fill='#a87554');d.polygon([(8,5),(10,7),(8,10),(6,7)],fill='#9979c2');d.point((8,7),fill='#e8daf8')
    else:
        d.ellipse((3,1,12,14),fill='#253965');d.rectangle((5,4,7,6),fill='#d7bd74');d.rectangle((9,8,11,10),fill='#af8cdb');d.point((6,12),fill='#d7bd74')
    im.save(RES/f'assets/{MOD}/textures/item/{name}.png');item(name)
for name,kind in [('chronus_fragment','fragment'),('timeworn_hourglass','hourglass'),('ashbound_bestiary','book'),('sanctum_keeper_spawn_egg','egg')]:sprite(name,kind)
recipe('ashbound_bestiary',None,None,ingredients=['minecraft:book',f'{MOD}:ashes','minecraft:feather'])
recipe('timeworn_hourglass',[' G ','FCF',' S '],{'G':'minecraft:glass','F':f'{MOD}:chronus_fragment','C':f'{MOD}:essentialium_infused_metalinium_ingot','S':f'{MOD}:reinforced_stick'})
for path in [f'data/curios/tags/item/focus.json',f'data/trinkets/tags/item/chest/dark_focus.json']:
    data=json.loads((RES/path).read_text(encoding='utf-8'));data['values'].append(f'{MOD}:timeworn_hourglass');write(path,data)

def chest_loot(name,entries,special):
    pools=[{'rolls':{'type':'minecraft:uniform','min':2,'max':4},'entries':[{'type':'minecraft:item','name':n,'weight':w,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]} for n,w,lo,hi in entries]}]
    for n,lo,hi,chance in special:
        p={'rolls':1,'entries':[{'type':'minecraft:item','name':n,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]}]}
        if chance<1:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
        pools.append(p)
    write(f'data/{MOD}/loot_table/chests/{name}.json',{'type':'minecraft:chest','pools':pools})
chest_loot('chronus_sanctum',[('minecraft:gold_ingot',4,1,3),(f'{MOD}:chronus_orb',3,1,2),(f'{MOD}:essentialium',4,1,3),('minecraft:lapis_lazuli',8,3,8)],[(f'{MOD}:chronus_fragment',1,2,1),(f'{MOD}:spell_book',1,1,.12)])
chest_loot('sunken_archive',[('minecraft:prismarine_shard',8,2,5),('minecraft:iron_ingot',5,1,3),('minecraft:lapis_lazuli',8,3,8),(f'{MOD}:essentialium',2,1,2)],[(f'{MOD}:water_core',1,1,.2),(f'{MOD}:spell_book',1,1,.08)])
write(f'data/{MOD}/loot_table/entities/sanctum_keeper.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:chronus_fragment','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':2,'max':3}}]}]},{'rolls':1,'entries':[{'type':'minecraft:item','name':f'{MOD}:chronus_orb'}]}]})

def structure(name,biomes,height,surface,spacing,salt):
    write(f'data/{MOD}/tags/worldgen/biome/has_{name}.json',{'replace':False,'values':[f'{MOD}:{b}' for b in biomes]})
    data={'type':'minecraft:jigsaw','biomes':f'#{MOD}:has_{name}','start_pool':f'{MOD}:{name}','size':1,'start_height':height,'max_distance_from_center':40,'step':'surface_structures' if surface else 'underground_structures','terrain_adaptation':'none','use_expansion_hack':False,'spawn_overrides':{}}
    if surface:data['project_start_to_heightmap']='WORLD_SURFACE_WG'
    write(f'data/{MOD}/worldgen/structure/{name}.json',data)
    write(f'data/{MOD}/worldgen/template_pool/{name}.json',{'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':f'{MOD}:{name}','processors':{'processors':[]},'projection':'rigid'}}]})
    write(f'data/{MOD}/worldgen/structure_set/{name}.json',{'structures':[{'structure':f'{MOD}:{name}','weight':1}],'placement':{'type':'minecraft:random_spread','spacing':spacing,'separation':spacing//3,'salt':salt}})
structure('chronus_sanctum',['ash_wastes','charred_forest','echo_fields','crystal_hollows','veilwood'],{'absolute':-8},True,34,263025)
structure('sunken_archive',['water_cave'],{'type':'minecraft:uniform','min_inclusive':{'absolute':-34},'max_inclusive':{'absolute':-12}},False,18,263026)

class Template:
    def __init__(self,size):self.size=size;self.palette=[];self.indices={};self.blocks={};self.entities=[]
    def put(self,x,y,z,name,props=None,nbt=None):
        key=(name,tuple(sorted((props or {}).items())))
        if key not in self.indices:
            self.indices[key]=len(self.palette);state={'id':S(name)}
            if props:state['properties']=C({k:S(v) for k,v in props.items()})
            self.palette.append(state)
        block={'pos':L(3,[x,y,z]),'state':I(self.indices[key])}
        if nbt:block['nbt']=C(nbt)
        self.blocks[(x,y,z)]=block
    def box(self,x0,y0,z0,x1,y1,z1,name,props=None):
        for x in range(x0,x1+1):
            for y in range(y0,y1+1):
                for z in range(z0,z1+1):self.put(x,y,z,name,props)
    def chest(self,x,y,z,loot,wet=False):self.put(x,y,z,'minecraft:chest',{'facing':'north','type':'single','waterlogged':str(wet).lower()},{'id':S('minecraft:chest'),'LootTable':S(f'{MOD}:chests/{loot}')})
    def mob(self,x,y,z,name,health):self.entities.append({'pos':L(6,[x+.5,float(y),z+.5]),'blockPos':L(3,[x,y,z]),'nbt':C({'id':S(f'{MOD}:{name}'),'PersistenceRequired':B(1),'Health':F(health),'Pos':L(6,[x+.5,float(y),z+.5]),'Rotation':L(5,[180,0])})})
    def book(self,x,y,z,title,text):
        stack={'Slot':B(0),'id':S('minecraft:written_book'),'count':I(1),'components':C({'minecraft:written_book_content':C({'title':S(title),'author':S('The Lost Archivist'),'generation':I(0),'resolved':B(1),'pages':L(8,text.split('\n\n'))})})}
        self.put(x,y,z,'minecraft:lectern',{'facing':'north','has_book':'true','powered':'false'},{'id':S('minecraft:lectern'),'Book':C({k:v for k,v in stack.items() if k!='Slot'}),'Page':I(0)})
    def save(self,name):
        data=C({'DataVersion':I(5023),'size':L(3,self.size),'palette':L(10,self.palette),'blocks':L(10,list(self.blocks.values())),'entities':L(10,self.entities)})
        path=RES/f'data/{MOD}/structure/{name}.nbt';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(gzip.compress(b'\x0a\x00\x00'+payload(data),mtime=0))

t=Template([27,16,23])
# Stone-built underground chambers, with a ruined arch and descending entrance visible above ground.
t.box(0,0,0,18,8,20,'minecraft:stone_bricks');t.box(1,1,1,17,7,19,'minecraft:air')
for x in range(19):
    for z in range(21):
        if (x*3+z)%11==0:t.put(x,0,z,f'{MOD}:hard_stone')
        if (x+z)%9==0:t.put(x,8,z,'minecraft:mossy_stone_bricks')
for z in range(9):
    y=9-z
    for x in [8,9,10]:
        t.put(x,y,z,'minecraft:stone_brick_stairs',{'facing':'north','half':'bottom','shape':'straight','waterlogged':'false'})
        t.box(x,y+1,z,x,min(15,y+4),z,'minecraft:air')
for x in [6,12]:t.box(x,8,0,x,13,0,'minecraft:chiseled_stone_bricks')
t.box(6,14,0,12,14,0,'minecraft:stone_bricks');t.put(7,14,0,'minecraft:air');t.put(11,14,0,'minecraft:air')
# A lower side crypt can be opened using its clearly visible lever.
t.box(12,1,7,12,6,18,'minecraft:stone_bricks')
t.put(12,1,10,'minecraft:iron_door',{'facing':'east','half':'lower','hinge':'left','open':'false','powered':'false'})
t.put(12,2,10,'minecraft:iron_door',{'facing':'east','half':'upper','hinge':'left','open':'false','powered':'false'})
t.put(11,2,9,'minecraft:lever',{'face':'wall','facing':'west','powered':'false'})
t.chest(15,1,15,'chronus_sanctum');t.chest(3,1,16,'chronus_sanctum')
# Both arrow traps have an obvious safe route around the plates.
for z in [11,15]:
    t.put(1,2,z,'minecraft:dispenser',{'facing':'east','triggered':'false'},{'id':S('minecraft:dispenser'),'Items':L(10,[{'Slot':B(0),'id':S('minecraft:arrow'),'count':I(6)}])})
    t.put(2,2,z,'minecraft:redstone_wire',{'north':'none','east':'side','south':'none','west':'side','power':'0'})
    t.put(3,2,z,'minecraft:stone_pressure_plate',{'powered':'false'});t.put(2,1,z,'minecraft:stone_bricks');t.put(3,1,z,'minecraft:stone_bricks')
for x,z in [(3,5),(15,5),(3,18),(15,18)]:
    t.box(x,1,z,x,5,z,'minecraft:chiseled_stone_bricks');t.put(x,6,z,'minecraft:lantern',{'hanging':'false','waterlogged':'false'})
# A true geode sits beside the crypt; its doorway leads into the crystal-lined hollow.
for x in range(19,27):
    for y in range(0,9):
        for z in range(8,17):
            radius=math.sqrt((x-22.5)**2+(y-4)**2+(z-12)**2)
            if radius>4:continue
            name='minecraft:smooth_basalt' if radius>3.5 else 'minecraft:calcite' if radius>3 else f'{MOD}:budding_essentialium' if (x+y+z)%4==0 else f'{MOD}:essentialium_block' if radius>2.4 else 'minecraft:air'
            if radius<=2.4:name='minecraft:air'
            t.put(x,y,z,name)
t.box(17,1,11,21,3,13,'minecraft:air')
t.put(22,2,12,f'{MOD}:essentialium_cluster',{'facing':'up','waterlogged':'false'});t.put(22,1,12,f'{MOD}:budding_essentialium')
# A floating, supported crystal ornament hangs above the central spelltable.
t.put(8,1,14,f'{MOD}:spelltable');t.put(8,5,14,f'{MOD}:essentialium_block')
for dx,dy,dz,face in [(0,1,0,'up'),(0,-1,0,'down'),(1,0,0,'east'),(-1,0,0,'west'),(0,0,1,'south'),(0,0,-1,'north')]:t.put(8+dx,5+dy,14+dz,f'{MOD}:essentialium_cluster',{'facing':face,'waterlogged':'false'})
t.book(6,1,14,'Timeworn Notes','The crystals remember. Three breaths forward, one step back.\n\nThe Keeper guards fragments of a broken hourglass. Their focus steadies Chronus magic.\n\nBeyond the echo gate, another sky waits. Its seal is not yet understood.')
t.mob(8,1,17,'sanctum_keeper',44);t.save('chronus_sanctum')

v=Template([15,9,15]);v.box(0,0,0,14,8,14,'minecraft:prismarine_bricks');v.box(1,1,1,13,7,13,'minecraft:air');v.box(1,1,1,13,2,13,'minecraft:water',{'level':'0'})
v.box(6,1,0,8,4,0,'minecraft:air');v.box(6,0,0,8,0,14,'minecraft:stone_bricks')
for x,z in [(2,2),(12,2),(2,12),(12,12)]:
    v.box(x,1,z,x,5,z,'minecraft:dark_prismarine');v.put(x,6,z,'minecraft:sea_lantern')
for x in [3,11]:v.put(x,3,11,'minecraft:prismarine_bricks');v.chest(x,4,11,'sunken_archive')
v.put(7,3,12,'minecraft:prismarine_bricks');v.book(7,4,12,'Sunken Record','Water swallowed these shelves, but not our knowledge.\n\nThe Sentinels still watch the sealed chests. Their slow water bolts can be dodged.\n\nA core may remain among lapis and prismarine.')
v.mob(4,3,8,'drowned_sentinel',24);v.mob(10,3,8,'drowned_sentinel',24);v.save('sunken_archive')

def advancement(name,parent,icon,title,description,trigger='minecraft:impossible',conditions=None,frame='task',reward=0):
    criterion={'trigger':trigger}
    if conditions:criterion['conditions']=conditions
    data={'display':{'icon':{'id':icon},'title':{'translate':f'advancement.{MOD}.{name}.title'},'description':{'translate':f'advancement.{MOD}.{name}.description'},'frame':frame,'show_toast':True,'announce_to_chat':frame=='challenge','hidden':False},'criteria':{'done':criterion},'requirements':[['done']]}
    if parent:data['parent']=f'{MOD}:exploration/{parent}'
    else:data['display']['background']='minecraft:block/stone'
    if reward:data['rewards']={'experience':reward}
    write(f'data/{MOD}/advancement/exploration/{name}.json',data)
    return title,description
adv={
 'root':advancement('root',None,f'{MOD}:ashes','Ashbound','Collect Ashes and begin your journey.','minecraft:inventory_changed',{'items':[{'items':f'{MOD}:ashes'}]}),
 'awaken_portal':advancement('awaken_portal','root',f'{MOD}:astral_crystal_wand','Awaken the Gate','Use an Astral Crystal Wand to improve a Nether portal.'),
 'enter_realm':advancement('enter_realm','awaken_portal','minecraft:obsidian','Under an Ashen Sky','Enter the Burned Dimension.'),
 'find_geode':advancement('find_geode','enter_realm',f'{MOD}:essentialium','Crystal Memory','Discover an Essentialium geode.'),
 'find_sanctum':advancement('find_sanctum','find_geode',f'{MOD}:chronus_orb','Timeworn Halls','Explore a Chronus Sanctum.'),
 'keeper_defeated':advancement('keeper_defeated','find_sanctum',f'{MOD}:chronus_fragment','The Keeper Falls','Defeat a Sanctum Keeper.',frame='challenge',reward=50),
 'hourglass':advancement('hourglass','keeper_defeated',f'{MOD}:timeworn_hourglass','Time in Your Hands','Obtain a Timeworn Hourglass.','minecraft:inventory_changed',{'items':[{'items':f'{MOD}:timeworn_hourglass'}]}),
 'find_archive':advancement('find_archive','enter_realm',f'{MOD}:water_core','Beneath the Water','Explore a Sunken Archive.'),
 'broodmother_defeated':advancement('broodmother_defeated','enter_realm',f'{MOD}:fire_core','Cinderbreaker','Defeat a Cinder Broodmother.',frame='challenge',reward=50),
 'first_spell':advancement('first_spell','find_geode',f'{MOD}:spell_book','First Incantation','Successfully cast an elemental spell.'),
 'bestiary_complete':advancement('bestiary_complete','enter_realm',f'{MOD}:ashbound_bestiary','Living Chronicle','Observe all fourteen creatures in the Bestiary.',frame='challenge',reward=75),
}
info={
 'mummy':('Overworld deserts.','Wrapped undead. Its strikes cause Hunger and Poison.'),
 'fire_mage':('Fire Caves in the Burned Dimension.','Fireproof mage. Fires small fireballs.'),
 'glowing_bee':('Glowing Caves.','Glowing cave bee. Leave it alone unless you need honey.'),
 'hollow_watcher':('Veilwood. One natural spawn per forest region.','Persistent watcher. Does not teleport or steal blocks.'),
 'chronus_mage':('Near Essentialium geodes in the Burned Dimension.','Warns before firing time bolts. Dashes away when approached.'),
 'ashen_wraith':('Open areas of the Burned Dimension. More common during Ashfall.','Floating close-range attacker.'),
 'bonewing':('Rarely above open areas of the Burned Dimension.','Circles before swooping. Listen for its attack sound.'),
 'veilwood_spider':('Veilwood.','Small spider. Its bite causes brief Poison.'),
 'drowned_sentinel':('Water Caves and Sunken Archives.','Water guardian. Dodge its slow, announced water bolts.'),
 'cinder_broodmother':('Rarely in Fire Caves.','Fireproof mini-boss. Leave the flame circle before it erupts.'),
 'lantern_wisp':('Glowing Caves.','Peaceful light spirit. Avoids players and has no kill loot.'),
 'briar_whisperer':('Overworld jungles and swamps.','Plant caster. Move away from the green root circle.'),
 'dune_stalker':('Overworld deserts in the dark.','Scorpion. Its sting causes brief Poison.'),
 'sanctum_keeper':('Chronus Sanctums.','Stronger time mage. Escape its purple time-field warning.'),
}
de_info={
 'mummy':('Overworld-Wüsten.','Mumie. Ihre Treffer verursachen Hunger und Gift.'),
 'fire_mage':('Fire Caves der Burned Dimension.','Feuerfester Magier. Verschießt kleine Feuerbälle.'),
 'glowing_bee':('Glowing Caves.','Leuchtende Höhlenbiene. Bleibt friedlich, bis sie provoziert wird.'),
 'hollow_watcher':('Veilwood. Ein natürlicher Spawn je Waldgebiet.','Dauerhafter Wächter. Teleportiert nicht und stiehlt keine Blöcke.'),
 'chronus_mage':('Bei Essentialium-Geoden der Burned Dimension.','Kündigt Zeitprojektile an. Weicht bei Annäherung zurück.'),
 'ashen_wraith':('Offene Gebiete der Burned Dimension. Häufiger bei Ashfall.','Schwebender Nahkämpfer.'),
 'bonewing':('Selten über offenen Flächen der Burned Dimension.','Kreist vor dem Sturzflug. Achte auf seinen Angriffssound.'),
 'veilwood_spider':('Veilwood.','Kleine Spinne mit kurzem Gift-Effekt.'),
 'drowned_sentinel':('Water Caves und Sunken Archives.','Wasserwächter. Weiche den angekündigten Wasserprojektilen aus.'),
 'cinder_broodmother':('Selten in Fire Caves.','Feuerfester Mini-Boss. Verlasse den Flammenkreis rechtzeitig.'),
 'lantern_wisp':('Glowing Caves.','Friedlicher Lichtgeist. Weicht Spielern aus; kein Kill-Loot.'),
 'briar_whisperer':('Overworld-Dschungel und Sümpfe.','Pflanzenmagier. Verlasse den grünen Wurzelkreis.'),
 'dune_stalker':('Overworld-Wüsten im Dunkeln.','Skorpion mit kurzem Gift-Effekt.'),
 'sanctum_keeper':('Chronus Sanctums.','Stärkerer Zeitmagier. Verlasse sein violettes Zeitfeld.'),
}
de_adv={'root':('Aschenpfad','Sammle Ashes und beginne deine Reise.'),'awaken_portal':('Erwecke das Tor','Verbessere ein Netherportal mit dem Astral Crystal Wand.'),'enter_realm':('Unter aschigem Himmel','Betritt die Burned Dimension.'),'find_geode':('Kristallgedächtnis','Entdecke eine Essentialium-Geode.'),'find_sanctum':('Hallen der Zeit','Erkunde einen Chronus Sanctum.'),'keeper_defeated':('Der Wächter fällt','Besiege einen Sanctum Keeper.'),'hourglass':('Zeit in deinen Händen','Erhalte eine Timeworn Hourglass.'),'find_archive':('Unter Wasser','Erkunde ein Sunken Archive.'),'broodmother_defeated':('Glutbrecher','Besiege eine Cinder Broodmother.'),'first_spell':('Erste Beschwörung','Wirke erfolgreich einen Elementarzauber.'),'bestiary_complete':('Lebendige Chronik','Beobachte alle vierzehn Bestiarium-Kreaturen.')}
for lang in ['en_us','de_de']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data.update({f'item.{MOD}.chronus_fragment':'Chronus Fragment',f'item.{MOD}.timeworn_hourglass':'Timeworn Hourglass',f'item.{MOD}.ashbound_bestiary':'Ashbound Bestiary',f'entity.{MOD}.sanctum_keeper':'Sanctum Keeper',f'item.{MOD}.sanctum_keeper_spawn_egg':'Sanctum Keeper Spawn Egg'})
    data[f'perk.{MOD}.timeworn_hourglass']='15 % kürzere Chronus-Cooldowns; Focus-Slot oder Nebenhand. Stapelt nicht mit Astral Prism.' if lang=='de_de' else '15% shorter Chronus cooldowns; Focus slot or offhand. Does not stack with Astral Prism.'
    data[f'bestiary.{MOD}.intro']='Ashbound Bestiary\n\nEntdeckt: %s / %s\n\nBeobachte Mobs und ihre Drops. Dein Wissen bleibt nach dem Tod erhalten.' if lang=='de_de' else 'Ashbound Bestiary\n\nDiscovered: %s / %s\n\nObserve mobs and their drops. Your knowledge survives death.'
    data[f'bestiary.{MOD}.unknown']='Noch nicht entdeckt.\n\nErkunde die Dimension und die Overworld.' if lang=='de_de' else 'Not yet discovered.\n\nExplore the dimension and the Overworld.'
    data[f'bestiary.{MOD}.kills']='Besiegt: %s' if lang=='de_de' else 'Defeated: %s';data[f'bestiary.{MOD}.drops']='Beobachtete Drops:' if lang=='de_de' else 'Observed drops:'
    for species,(habitat,behavior) in (de_info if lang=='de_de' else info).items():data[f'bestiary.{MOD}.{species}.habitat']=habitat;data[f'bestiary.{MOD}.{species}.behavior']=behavior
    for key,(title,description) in (de_adv if lang=='de_de' else adv).items():data[f'advancement.{MOD}.{key}.title']=title;data[f'advancement.{MOD}.{key}.description']=description
    write(path,data)
print('Generated Chronus Sanctum and Sunken Archive, 11 advancements, native Bestiary, Chronus Fragment and Timeworn Hourglass.')
