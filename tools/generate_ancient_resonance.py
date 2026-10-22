"""Ancient Resonance: cave-floor ruins, native models and recipe progression."""
import generate_progression_rebalance
from generate_resources import RES,MOD,write,recipe
from generate_exploration import Template
from generate_caves import S,I,B
from pathlib import Path
import json,math,copy

def mod(name):return f'{MOD}:{name}'
def model_item(name,model):write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':mod(model)}})
def cube(lo,hi,texture):return {'from':lo,'to':hi,'faces':{face:{'texture':'#'+texture} for face in ['down','up','north','south','west','east']}}
hard=mod('block/hard_stone')
# Original 16x16 stone/core textures stay unchanged; the carvings are native geometry.
colors=[0xe98d39,0x63bdd7,0xeedb83,0x9a85c7]
for n,name in enumerate(['ember','tide','radiance','chronus']):
    base=json.loads((RES/f'assets/{MOD}/models/item/empty_runestone.json').read_text())
    base['textures']['inlay']=['minecraft:block/magma','minecraft:block/prismarine_bricks','minecraft:block/glowstone','minecraft:block/crying_obsidian'][n]
    base['elements']+=[cube([5,6,6.5],[11,10,7],'inlay'),cube([5,6,9],[11,10,9.5],'inlay')]
    if n==3:base['elements']+=[cube([7,4,6.5],[9,12,7],'inlay'),cube([7,4,9],[9,12,9.5],'inlay')]
    write(f'assets/{MOD}/models/item/{name}_rune.json',base);model_item(name+'_rune','item/'+name+'_rune')
    if n<3:
        sigil=copy.deepcopy(base);sigil['textures']['stone']='minecraft:block/gold_block';sigil['elements']+=[cube([6,1,7],[10,2,9],'inlay')]
        write(f'assets/{MOD}/models/item/{name}_trial_sigil.json',sigil);model_item(name+'_trial_sigil','item/'+name+'_trial_sigil')

write(f'assets/{MOD}/models/item/ashbound_field_journal.json',{'parent':'minecraft:item/generated','textures':{'layer0':mod('item/ashbound_bestiary')}})
write(f'assets/{MOD}/items/ashbound_field_journal.json',{'model':{'type':'minecraft:model','model':mod('item/ashbound_field_journal'),'tints':[{'type':'minecraft:constant','value':0xd0dbaa}]}})
for name in ['runic_binding_bench','dormant_runestone']:
    elements=[cube([0,0,0],[16,4,16],'stone'),cube([2,4,2],[14,12,14],'stone'),cube([0,12,0],[16,16,16],'top')]
    if name=='runic_binding_bench':
        elements=[cube([1,0,1],[5,12,5],'stone'),cube([11,0,1],[15,12,5],'stone'),cube([1,0,11],[5,12,15],'stone'),cube([11,0,11],[15,12,15],'stone'),cube([0,11,0],[16,15,16],'top')]
        for x,z in [(3,3),(10,3),(3,10),(10,10)]:elements.append(cube([x,15,z],[x+3,16,z+3],'inlay'))
    model={'parent':'minecraft:block/block','textures':{'particle':hard,'stone':hard,'top':'minecraft:block/chiseled_stone_bricks','inlay':'minecraft:block/amethyst_block'},'elements':elements}
    if name=='dormant_runestone':
        for n,kind in enumerate(['ember','tide','radiance']):
            model['textures']['top']=['minecraft:block/magma','minecraft:block/prismarine_bricks','minecraft:block/glowstone'][n]
            write(f'assets/{MOD}/models/block/{name}_{kind}.json',copy.deepcopy(model))
        write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{f'kind={kind}':{'model':mod('block/'+name+'_'+kind)} for kind in ['ember','tide','radiance']}})
        write(f'assets/{MOD}/models/item/{name}.json',{'parent':mod('block/'+name+'_ember')})
    else:
        write(f'assets/{MOD}/models/block/{name}.json',model);write(f'assets/{MOD}/blockstates/{name}.json',{'variants':{'':{'model':mod('block/'+name)}}});write(f'assets/{MOD}/models/item/{name}.json',{'parent':mod('block/'+name)})
    model_item(name,'item/'+name)

variants={}
for n,kind in enumerate(['ember','tide','radiance']):
    for index in range(4):
        for lit in [False,True]:
            name=f'trial_seal_{kind}_{index}_{str(lit).lower()}'
            elements=[cube([0,0,0],[16,4,16],'stone')]
            for i in range(max(1,index)):
                x=8-max(1,index)*2+i*4;elements.append(cube([x,4,6],[x+2,13,10],'inlay'))
            model={'parent':'minecraft:block/block','textures':{'particle':hard,'stone':hard,'inlay':(['minecraft:block/magma','minecraft:block/prismarine_bricks','minecraft:block/glowstone'][n] if lit else 'minecraft:block/chiseled_stone_bricks')},'elements':elements}
            write(f'assets/{MOD}/models/block/{name}.json',model)
            variants[f'index={index},kind={kind},lit={str(lit).lower()}']={'model':mod('block/'+name)}
write(f'assets/{MOD}/blockstates/trial_seal.json',{'variants':variants});write(f'assets/{MOD}/models/item/trial_seal.json',{'parent':mod('block/trial_seal_ember_1_false')});model_item('trial_seal','item/trial_seal')

recipe('runic_binding_bench',['ERE','MSM','HHH'],{'E':mod('essentialium'),'R':mod('empty_runestone'),'M':mod('metalinium_ingot'),'S':mod('spelltable'),'H':mod('hard_stone')})
recipe('ashbound_field_journal',ingredients=['minecraft:book',mod('ashes'),mod('empty_runestone')])
for name,ingredients in {'ember':['empty_runestone','fire_core','soul_essence'],'tide':['empty_runestone','water_core','japanese_essence'],'radiance':['empty_runestone','glowing_honey','essentialium'],'chronus':['empty_runestone','chronus_orb','essentialium_infused_metalinium_ingot']}.items():recipe(name+'_rune',ingredients=list(map(mod,ingredients)))
for kind in ['ember','tide','radiance','chronus']:
    write(f'data/{MOD}/advancement/recipes/{kind}_rune.json',{'parent':'minecraft:recipes/root','criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']],'rewards':{'recipes':[mod(kind+'_rune')]}})
    name='complete_all' if kind=='chronus' else 'complete_'+kind
    write(f'data/{MOD}/advancement/resonance/{name}.json',{'parent':mod('exploration/enter_realm'),'display':{'icon':{'id':mod(kind+'_rune')},'title':{'translate':f'item.{MOD}.{kind}_rune'},'description':{'translate':f'resonance.{MOD}.requires_{kind}'},'frame':'challenge' if kind=='chronus' else 'goal','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']],'rewards':{'recipes':[mod(kind+'_rune')]}})
for name in ['runic_binding_bench']:
    write(f'data/{MOD}/loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':mod(name)}],'condition':{'type':'minecraft:survives_explosion'}}]})
for tag in ['minecraft:mineable/pickaxe','minecraft:needs_iron_tool']:
    ns,path=tag.split(':');p=f'data/{ns}/tags/block/{path}.json';data=json.loads((RES/p).read_text()) if (RES/p).exists() else {'replace':False,'values':[]};data['values']=sorted(set(data['values']+[mod('runic_binding_bench')]));write(p,data)

names=['cinder_crucible','sunken_reliquary','luminous_hollow']
for n,(name,kind,biome) in enumerate(zip(names,['ember','tide','radiance'],['fire_cave','water_cave','glowing_cave'])):
    write(f'data/{MOD}/worldgen/structure/{name}.json',{'type':mod('cave_trial'),'kind':kind,'biomes':[mod(biome)],'start_pool':mod(name),'step':'underground_structures','terrain_adaptation':'none','spawn_overrides':{}})
    write(f'data/{MOD}/worldgen/structure_set/{name}.json',{'structures':[{'structure':mod(name),'weight':1}],'placement':{'type':'minecraft:random_spread','spacing':24,'separation':8,'salt':263031+n}})
    write(f'data/{MOD}/worldgen/template_pool/{name}.json',{'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':mod(name+'_'+str(v)),'processors':{'processors':[]},'projection':'rigid'}} for v in range(3)]})
    for v in range(3):
        t=Template([31,11,31]);floor=set();rooms=[]
        if n==0:rooms=[(15,15,8+v%2,7),(5+v,6,4,4),(25-v,7+v,4,4),(15+v,25,4,4)]
        elif n==1:rooms=[(15,15,4,4),(6,6+v,5,5),(24,7,5,4+v%2),(15-v,25-v,6,4)]
        else:rooms=[(15,15,7,6+v%2),(5+v,11,4,5),(25-v,13+v,4,5),(14+v,25,5,4)]
        for cx,cz,rx,rz in rooms:
            for x in range(1,30):
                for z in range(1,30):
                    if ((x-cx)/rx)**2+((z-cz)/rz)**2<=1:floor.add((x,z))
        paths=set()
        for cx,cz,rx,rz in rooms[1:]:
            for d in range(max(abs(cx-15),abs(cz-15))+1):
                length=max(abs(cx-15),abs(cz-15));x=round(15+(cx-15)*d/length);z=round(15+(cz-15)*d/length)
                for dx in [-1,0,1]:
                    for dz in [-1,0,1]:paths.add((x+dx,z+dz))
        floor|=paths
        walls={(x+dx,z+dz) for x,z in floor for dx,dz in [(1,0),(-1,0),(0,1),(0,-1)] if (x+dx,z+dz) not in floor}
        for x,z in sorted(floor|walls):
            t.put(x,0,z,mod('hard_stone'))
            if (x,z) in walls:
                for y in range(1,8):t.put(x,y,z,'minecraft:stone_bricks' if (x+z+y+v)%7 else 'minecraft:cracked_stone_bricks')
            else:
                for y in range(1,8):t.put(x,y,z,'minecraft:water' if n==1 and y<=2 and (x,z) not in paths and abs(x-15)+abs(z-15)>5 else 'minecraft:air')
            t.put(x,8,z,mod(['fire_cave_stone','water_cave_stone','glowing_cave_stone'][n]))
        # An open, lit cave-facing entry, at different sides in each layout.
        side=[(15,1),(1,15),(29,15)][v]
        for d in range(15):
            x=round(side[0]+(15-side[0])*d/14);z=round(side[1]+(15-side[1])*d/14)
            for dx in [-1,0,1]:
                t.put(x+dx,0,z,mod('hard_stone'))
                for y in range(1,4):t.put(x+dx,y,z,'minecraft:air')
        t.put(15,1,15,mod('dormant_runestone'),{'kind':kind},{'id':S(mod('dormant_runestone'))})
        t.put(side[0],1,side[1],mod('trial_seal'),{'kind':kind,'index':'0','lit':'true'})
        for index,(cx,cz,rx,rz) in enumerate(rooms[1:],1):
            # Dry pedestals with one, two or three raised strokes, independent of colour.
            t.put(cx,1,cz,mod('hard_stone'));t.put(cx,2,cz,mod('trial_seal'),{'kind':kind,'index':str(index),'lit':'false'})
            t.put(cx,7,cz,['minecraft:shroomlight','minecraft:sea_lantern','minecraft:glowstone'][n])
            for dx in [-1,1]:t.put(cx+dx,2,cz,'minecraft:air')
        for x,z in [(12,14),(18,14)]:t.put(x,7,z,['minecraft:shroomlight','minecraft:sea_lantern','minecraft:glowstone'][n])
        if n==0:
            for x,z in [(11,16),(19,16)]:t.put(x,0,z,'minecraft:magma_block')
            t.put(15,1,19,'minecraft:anvil',{'facing':'north'});t.put(14,1,19,'minecraft:blast_furnace',{'facing':'north','lit':'false'})
        elif n==1:
            for cx,cz,_,_ in rooms[1:]:
                for dx in [-2,-1,0,1,2]:t.put(cx+dx,2,cz+2,'minecraft:prismarine_bricks');t.put(cx+dx,3,cz+2,'minecraft:air')
        else:
            for x,z in [(11,13),(19,13),(15,20)]:
                t.put(x,1,z,mod('essentialium_block'));t.put(x,2,z,mod('glowing_crystal'),{'facing':'up','waterlogged':'false','ashfall':'false'})
        t.save(name+'_'+str(v))

# Structure and material-recipe hints are translated; ordinary recipes stay native.
de={
'cinder_crucible':'Cinder Crucible','sunken_reliquary':'Sunken Reliquary','luminous_hollow':'Luminous Hollow',
'activation_cost':'Benötigt 1 × %s. Der Core wird beim Start verbraucht.',
'activation_confirm':'Verbraucht 1 × %s. Innerhalb von 5 Sekunden erneut klicken, um zu starten.',
'core_cost':'Am Dormant Runestone von %s: 1 Core zum Starten.',
'runestone_help':'Core halten: Kosten anzeigen; erneut klicken: starten. Field Journal: Wegpunkt speichern.',
'realm_required':'Prüfungen benötigen die Burned Dimension und einen Schwierigkeitsgrad über Friedlich.',
'already_complete':'Diese Prüfung ist bereits abgeschlossen.',
'trial_started':'Prüfung aktiviert. Öffne die drei Siegel; bei Licht folge dem angekündigten Zeichen.',
'joined':'Du nimmst an dieser Prüfung teil.',
'wave_warning':'Welle %s wird vorbereitet.','wave_started':'Welle %s beginnt!','wave_cleared':'Welle beendet. Kurze Verschnaufpause.',
'spawn_blocked':'Die Arena ist blockiert. Räume Platz für die nächste Welle frei.',
'seals_open':'Siegel geöffnet: %s / %s','puzzle_signal':'Leuchtendes Zeichen: %s Strich(e).','puzzle_progress':'Lichtfolge: %s / %s',
'puzzle_mistake':'Falsches Zeichen. Der bisherige Fortschritt bleibt erhalten.',
'hazard_ember':'Flammenfeld! Verlasse den angekündigten Kreis.','hazard_tide':'Wasserimpuls! Verlasse den Blasenkreis.',
'trial_complete':'Prüfung abgeschlossen!','reward':'%s abgeschlossen. Fortschritt und Beute erhalten.',
'waypoint_saved':'Entdeckten Prüfungsort als Wegpunkt gespeichert.',
'journal_intro':'Ashbound Field Journal\n\nPrüfungen: %s / %s\n\nPersönliches Wissen und Wegpunkte bleiben nach dem Tod erhalten.',
'journal_next_explore':'Nächster Schritt\n\n%s\n\nErkunde die entsprechenden Höhlen. Suche nach beleuchteten Ruineneingängen.',
'journal_next_trial':'Nächster Schritt\n\nBereite %s vor. Benötigter Core: %s. Öffne Siegel oder folge den Lichtzeichen. Binde danach eine freigeschaltete Rune an der Bench.',
'journal_next_complete':'Nächster Schritt\n\nAlle Elemente gemeistert. Chronus Rune freigeschaltet. Binde deine bevorzugte Rune und kombiniere sie mit vorhandenen Waffen-Perks.',
'journal_biomes':'Entdeckte Gebiete','journal_runes':'Runen-Freischaltungen','journal_waypoints':'Wegpunkte\n\nBenutze dieses Journal am Runenstein, um genau diesen entdeckten Ort zu speichern. Alternativ: in der Nähe schleichen und das Journal öffnen. Keine Orte werden automatisch markiert.',
'hint_ember':'Unter den glühenden Felsen liegt eine vergessene Schmiede.',
'hint_tide':'Hinter dem Wasser warten drei verschlossene Kammern.',
'hint_radiance':'In leuchtenden Tiefen antworten Kristalle auf eine alte Melodie.',
'trial_details_ember':'Fire Core benötigt. Aktiviere drei Runensteine und besiege drei angekündigte Wellen. Flammenkreise lassen Zeit zum Ausweichen.',
'trial_details_tide':'Water Core benötigt. Öffne drei Siegel in verbundenen Kammern; danach zwei Wächterwellen. Trockene Wege und Lufttaschen helfen.',
'trial_details_radiance':'Lightning Core benötigt. Folge fünf angekündigten Zeichen: ein, zwei oder drei Striche. Fehler rufen begrenzte Gegner; richtige Schritte bleiben erhalten.',
'unknown':'Noch nicht entdeckt','completed':'Abgeschlossen','not_completed':'Noch offen','unlocked':'Freigeschaltet','locked':'Gesperrt',
'bench_hint':'Waffe + Rune / Chronus Orb',
'requires_ember':'Binden und Craften: Cinder Crucible abschließen.',
'requires_tide':'Binden und Craften: Sunken Reliquary abschließen.',
'requires_radiance':'Binden und Craften: Luminous Hollow abschließen.',
'requires_chronus':'Binden und Craften: alle drei Prüfungen abschließen.',
}
en={
'activation_cost':'Requires 1 × %s. Starting consumes the core.','activation_confirm':'Consumes 1 × %s. Click again within 5 seconds to start.','core_cost':'At the Dormant Runestone of %s: one core to start.',
'runestone_help':'Hold core: view cost; click again: start. Field Journal: save waypoint.','realm_required':'Trials require the Burned Dimension and a difficulty above Peaceful.','already_complete':'This trial has already been completed.',
'trial_started':'Trial activated. Open three seals, or follow the announced light symbols.','joined':'You have joined this trial.','wave_warning':'Preparing wave %s.','wave_started':'Wave %s begins!','wave_cleared':'Wave cleared. A short pause.',
'spawn_blocked':'Arena obstructed. Make room for the next wave.','seals_open':'Seals opened: %s / %s','puzzle_signal':'Lit symbol: %s stroke(s).','puzzle_progress':'Light sequence: %s / %s','puzzle_mistake':'Wrong symbol. Correct steps are preserved.',
'hazard_ember':'Flame field! Leave the marked circle.','hazard_tide':'Water pulse! Leave the bubble circle.','trial_complete':'Trial complete!','reward':'%s completed. Progress and loot received.','waypoint_saved':'Discovered trial location saved as a waypoint.',
'journal_intro':'Ashbound Field Journal\n\nTrials: %s / %s\n\nPersonal discoveries and waypoints survive death.',
'journal_next_explore':'Next step\n\n%s\n\nExplore the matching caves. Look for lit ruin entrances.',
'journal_next_trial':'Next step\n\nPrepare for %s. Required core: %s. Open seals or follow the light symbols. Then bind an unlocked rune at the Bench.',
'journal_next_complete':'Next step\n\nAll elements mastered. Chronus Rune unlocked. Bind your chosen rune and combine it with existing weapon perks.',
'journal_biomes':'Discovered areas','journal_runes':'Rune unlocks','journal_waypoints':'Waypoints\n\nUse this journal on a runestone to save that discovered location. Alternatively, crouch and open it nearby. No locations are marked automatically.',
'hint_ember':'A forgotten forge lies beneath the glowing rocks.','hint_tide':'Three sealed chambers wait behind the water.','hint_radiance':'In luminous depths, crystals answer an ancient melody.',
'trial_details_ember':'Requires a Fire Core. Activate three runestones and defeat three announced waves. Flame circles give time to dodge.',
'trial_details_tide':'Requires a Water Core. Open three seals in connected chambers, then defeat two guardian waves. Dry paths and air pockets help.',
'trial_details_radiance':'Requires a Lightning Core. Follow five announced symbols: one, two or three strokes. Mistakes summon limited enemies; correct steps remain.',
'unknown':'Not yet discovered','completed':'Completed','not_completed':'Still open','unlocked':'Unlocked','locked':'Locked','bench_hint':'Weapon + Rune / Chronus Orb',
'requires_ember':'Binding and crafting: complete Cinder Crucible.','requires_tide':'Binding and crafting: complete Sunken Reliquary.','requires_radiance':'Binding and crafting: complete Luminous Hollow.','requires_chronus':'Binding and crafting: complete all three trials.',
}
for lang,strings in [('de_de',de),('en_us',en)]:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    for name in names:strings.setdefault(name,{'cinder_crucible':'Cinder Crucible','sunken_reliquary':'Sunken Reliquary','luminous_hollow':'Luminous Hollow'}[name])
    for key,value in strings.items():data[f'resonance.{MOD}.{key}']=value
    for name,label in [('runic_binding_bench','Runic Binding Bench'),('dormant_runestone','Dormant Runestone'),('trial_seal','Trial Seal')]:data[f'block.{MOD}.{name}']=label
    data[f'item.{MOD}.ashbound_field_journal']='Ashbound Field Journal'
    for n,kind in enumerate(['ember','tide','radiance','chronus']):
        data[f'item.{MOD}.{kind}_rune']=kind.title()+' Rune'
        if n<3:data[f'item.{MOD}.{kind}_trial_sigil']=kind.title()+' Trial Sigil'
    perks_de=['Ember Rune – entzündet bei Nahkampftreffern kurz (6 s Cooldown).','Tide Rune – Nahkampftreffer löscht eigenes Feuer (8 s Cooldown).','Radiance Rune – leuchtende Ziele 3 s um 15 % verlangsamt (6 s Cooldown).','Chronus Rune – Perfect Dodge verkürzt aktiven Chronus-Cooldown um 1 s (10 s Cooldown).']
    perks_en=['Ember Rune – melee hits briefly ignite targets (6s cooldown).','Tide Rune – melee hits extinguish the wielder (8s cooldown).','Radiance Rune – glowing targets slowed by 15% for 3s (6s cooldown).','Chronus Rune – Perfect Dodge reduces active Chronus cooldown by 1s (10s cooldown).']
    for kind,text in zip(['ember','tide','radiance','chronus'],perks_de if lang=='de_de' else perks_en):data[f'perk.{MOD}.rune_{kind}']=text
    data[f'guide.{MOD}.runic_binding']=('Waffe + freigeschaltete Rune an der Bench. Eine Rune pro Waffe; andere Perks und Komponenten bleiben erhalten. Waffe + Chronus Orb entfernt die Rune und gibt sie zurück. Freischaltung durch die passende Prüfung, Chronus durch alle drei.' if lang=='de_de' else 'Weapon + unlocked rune at the Bench. One rune per weapon; other perks and components are preserved. Weapon + Chronus Orb removes and returns the rune. Unlock through the matching trial; all three for Chronus.')
    data[f'guide.{MOD}.ancient_trials']=('Cinder Crucible: Fire Core; Sunken Reliquary: Water Core; Luminous Hollow: Lightning Core. Erster Klick zeigt Kosten, zweiter startet. Ein Core je Ort; Wiederholungsversuche kosten keinen weiteren. Jeder Ort ist nur einmal abschließbar. Sigil je Element und Spieler nur einmal. Offline-Beteiligte erhalten die Beute beim nächsten Login. Ashfall erhöht nur gewöhnliche Materialbeute.' if lang=='de_de' else 'Cinder Crucible: Fire Core; Sunken Reliquary: Water Core; Luminous Hollow: Lightning Core. First click previews cost; second starts. One core per site; retries do not cost another. Each site can be completed once. One sigil per element per player. Offline participants receive loot on their next login. Ashfall only increases ordinary material rewards.')
    write(path,data)
print('Generated Ancient Resonance: nine varied cave-floor ruin layouts, native 16x16-texture models, four runes, sigils, binding bench, journal and localized guidance.')
