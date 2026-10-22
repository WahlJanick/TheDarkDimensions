"""Ancient Resonance finale: native sounds, arena and small exploration sites."""
import generate_ancient_resonance
from generate_resources import RES, MOD, write
from generate_exploration import Template, structure, chest_loot
from generate_ancient_resonance import cube, model_item
from generate_caves import S
import json, math, copy

def mod(name): return f'{MOD}:{name}'

# Seed the Regent's separate editable skin once. Later generation preserves artist edits.
regent_skin=RES/f'assets/{MOD}/textures/entity/ashbound_regent.png'
mage_skin=RES/f'assets/{MOD}/textures/entity/chronus_mage.png'
if not regent_skin.exists():regent_skin.write_bytes(mage_skin.read_bytes())

# Alias shipped Minecraft events: no audio dependency, and normal sound sliders/subtitles work.
cues={
    'perfect_parry':[('item.shield.block',.65,1.55),('block.amethyst_block.hit',.7,1.5)],
    'perfect_dodge':[('entity.player.attack.sweep',.5,1.3),('block.amethyst_block.chime',.3,1.7)],
    'rune_ember':[('item.firecharge.use',.45,1.25)],
    'rune_tide':[('entity.player.splash',.35,1.4)],
    'rune_radiance':[('block.amethyst_block.chime',.65,1.4)],
    'rune_chronus':[('block.enchantment_table.use',.4,1.65)],
    'trial_complete':[('ui.toast.challenge_complete',.3,1.15),('block.amethyst_block.resonate',.5,1.2)],
    'rare_loot':[('block.amethyst_block.chime',.65,1.65),('entity.experience_orb.pickup',.2,1.4)],
    'regent_awaken':[('entity.evoker.prepare_summon',.65,.7),('block.respawn_anchor.charge',.5,.8)],
    'regent_defeated':[('ui.toast.challenge_complete',.5,.85),('block.beacon.activate',.45,1.1)],
    'katana_finisher':[('entity.player.attack.sweep',.8,.8),('item.trident.throw',.25,1.1)],
}
sounds_path=RES/f'assets/{MOD}/sounds.json'
sounds=json.loads(sounds_path.read_text(encoding='utf-8')) if sounds_path.exists() else {}
for name,layers in cues.items():
    sounds[name]={'subtitle':f'subtitles.{MOD}.{name}','sounds':[{'name':'minecraft:'+event,'type':'event','volume':volume,'pitch':pitch} for event,volume,pitch in layers]}
write(f'assets/{MOD}/sounds.json',sounds)
for name in ['frostgleam','bloodgrief','souleater','stormveil']:
    path=f'data/{MOD}/weapon_attributes/{name}.json'
    data=json.loads((RES/path).read_text(encoding='utf-8'));data['attributes']['attacks'][2]['swing_sound']={'id':mod('katana_finisher'),'pitch':1.0,'volume':.65};write(path,data)

# Socket shapes remain readable without colour: one, two and three marks.
for mask in range(8):
    elements=[cube([0,0,0],[16,4,16],'stone'),cube([2,4,2],[14,12,14],'stone'),cube([0,12,0],[16,14,16],'top')]
    textures={'particle':mod('block/hard_stone'),'stone':mod('block/hard_stone'),'top':'minecraft:block/chiseled_polished_blackstone'}
    for n in range(3):
        textures['socket'+str(n)]=['minecraft:block/magma','minecraft:block/prismarine_bricks','minecraft:block/glowstone'][n] if mask&(1<<n) else 'minecraft:block/polished_blackstone'
        for i in range(n+1):elements.append(cube([2+n*5,14,3+i*4],[4+n*5,16,5+i*4],'socket'+str(n)))
    write(f'assets/{MOD}/models/block/nexus_altar_{mask}.json',{'parent':'minecraft:block/block','textures':textures,'elements':elements})
write(f'assets/{MOD}/blockstates/nexus_altar.json',{'variants':{f'attuned={i}':{'model':mod(f'block/nexus_altar_{i}')} for i in range(8)}})
write(f'assets/{MOD}/models/item/nexus_altar.json',{'parent':mod('block/nexus_altar_0')});model_item('nexus_altar','item/nexus_altar')
write(f'assets/{MOD}/blockstates/nexus_gate.json',{'variants':{'':{'model':mod('block/nexus_gate')}}})
write(f'assets/{MOD}/models/block/nexus_gate.json',{'parent':'minecraft:block/cube_all','textures':{'all':'minecraft:block/crying_obsidian'}})
heart=copy.deepcopy(json.loads((RES/f'assets/{MOD}/models/item/chronus_rune.json').read_text(encoding='utf-8')))
heart['textures']['stone']='minecraft:block/gold_block';heart['textures']['inlay']='minecraft:block/amethyst_block'
heart['elements']+=[cube([6,5,5],[10,11,11],'inlay')]
write(f'assets/{MOD}/models/item/regents_heart.json',heart);model_item('regents_heart','item/regents_heart')
write(f'assets/{MOD}/items/ashbound_regent_spawn_egg.json',{'model':{'type':'minecraft:model','model':'minecraft:item/evoker_spawn_egg'}})
write(f'data/{MOD}/damage_type/regent_wave.json',{'message_id':'mob','exhaustion':.1,'scaling':'when_caused_by_living_non_player'})
path='data/minecraft/tags/damage_type/bypasses_shield.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data['values']=sorted(set(data['values']+[mod('regent_wave')]));write(path,data)
write(f'data/{MOD}/loot_table/entities/ashbound_regent.json',{'type':'minecraft:entity','pools':[]})

biomes=['ash_wastes','charred_forest','echo_fields','crystal_hollows']
structure('resonance_nexus',biomes,{'absolute':0},True,48,263032)
path=f'data/{MOD}/worldgen/structure/resonance_nexus.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data['terrain_adaptation']='beard_thin';data['max_distance_from_center']=48;write(path,data)
t=Template([49,12,49])
for x in range(49):
    for z in range(49):
        distance=math.hypot(x-24,z-24)
        if distance>22:continue
        t.put(x,0,z,'minecraft:polished_blackstone_bricks' if (x+z)%9 else 'minecraft:cracked_polished_blackstone_bricks')
        # Clear the combat floor, including plants and existing trees.
        for y in range(1,10):t.put(x,y,z,'minecraft:air')
        if distance>21:
            entry=abs(x-24)<=2 and z<7
            for y in range(1,6):t.put(x,y,z,mod('nexus_gate') if entry else 'minecraft:stone_bricks' if (x*3+z+y)%7 else 'minecraft:cracked_stone_bricks')
        elif 19<distance<20 and (x+z)%4==0:t.put(x,1,z,'minecraft:glowstone')
for x,z in [(9,9),(39,9),(9,39),(39,39),(4,24),(44,24),(24,44)]:
    t.box(x,1,z,x,6+(x+z)%3,z,'minecraft:chiseled_polished_blackstone');t.put(x,7+(x+z)%3,z,'minecraft:shroomlight')
t.put(24,1,24,mod('nexus_altar'),{'attuned':'0'},{'id':S(mod('nexus_altar'))})
t.book(20,1,13,'The Last Resonance','Three trials. Three sigils. Tune each sigil at the central altar. The gate stays open.\n\nBlue blade: parry. Red circle: dodge. Golden ward: break with magic.\n\nTouch the altar with an empty hand to summon the Regent. Allies may join at the altar. Your field journal can save this place.')
t.put(21,0,1,'minecraft:stone_bricks');t.book(21,1,1,'The Sealed Nexus','Use each Trial Sigil on the sealed entrance. The Sigils remain yours; the gate stays open forever.\n\nInside, touch the central altar with an empty hand. Blue strike: parry. Red circle: dodge. Golden ward: magic.')
t.save('resonance_nexus')

for kind,chance in [('camp',24),('cache',16),('bridge',14),('crystal',40)]:
    name='wandering_'+kind
    write(f'data/{MOD}/worldgen/feature/{name}.json',{'type':mod('wandering_site'),'kind':kind})
    placement=[{'type':'minecraft:rarity_filter','chance':chance},{'type':'minecraft:in_square'}]
    if kind=='cache':placement.append({'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':-42},'max_inclusive':{'absolute':12}}})
    else:placement.append({'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'})
    placement.append({'type':'minecraft:biome'})
    write(f'data/{MOD}/worldgen/placed_feature/{name}.json',{'feature':mod(name),'placement':placement})
for biome in biomes+['veilwood','fire_cave','water_cave','glowing_cave','essentialium_cave']:
    path=RES/f'data/{MOD}/worldgen/biome/{biome}.json'
    if not path.exists():continue
    data=json.loads(path.read_text(encoding='utf-8'));features=data['features']
    # All surface variants occur before vegetation; cave caches have their own height filter.
    additions=['cache']+(['camp','bridge','crystal'] if biome in biomes else [])
    features[4]=list(dict.fromkeys(features[4]+[mod('wandering_'+kind) for kind in additions]));write(str(path.relative_to(RES)),data)
chest_loot('abandoned_camp',[(mod('ashes'),9,2,6),('minecraft:bread',6,1,3),('minecraft:coal',5,2,5),(mod('metalinium_nugget'),4,2,5)],[(mod('glowing_honey'),1,1,.12)])
chest_loot('hidden_cache',[(mod('ashes'),6,3,8),('minecraft:iron_ingot',6,1,3),(mod('metalinium_nugget'),5,3,7),(mod('chronus_orb'),1,1,1)],[(mod('chronus_fragment'),1,1,.08)])
write(f'data/{MOD}/advancement/resonance/regent_defeated.json',{'parent':mod('resonance/complete_all'),'display':{'icon':{'id':mod('regents_heart')},'title':{'translate':f'finale.{MOD}.adv_title'},'description':{'translate':f'finale.{MOD}.adv_description'},'frame':'challenge','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'done':{'trigger':'minecraft:impossible'}},'requirements':[['done']]})

en={
 'need_sigils':'Attune the Ember, Tide and Radiance Trial Sigils at this altar.',
 'sigil_locked':'Complete this trial yourself before attuning its Sigil.',
 'attuned':'Sigils attuned: %s/3. Sigils are retained; the gate stays open.',
 'completed':'The Ashbound Regent has fallen. This Nexus is at peace.',
 'peaceful':'The Regent cannot awaken in Peaceful difficulty.',
 'empty_hand':'Use an empty hand to awaken the Regent or join the fight.',
 'battle_warning':'The Regent awakens! Blue: parry. Red circle: dodge. Gold: magic.',
 'blocked':'The summoning space is blocked. Clear the area behind the altar.',
 'waypoint':'Resonance Nexus marked in your Field Journal.',
 'ability_unlocked':'Resonant Step unlocked: Perfect Dodge grants Speed I for 3 seconds (8 second cooldown).',
 'journal_hint':'Ancient Resonance\nThree cave trials hold the Sigils. Together they open the final Resonance Nexus on the surface of the Burned Realm.',
 'journal_seek':'The Final Resonance\nTake your three Trial Sigils to a Resonance Nexus on the surface. Use each Sigil on the sealed gate or altar. The gate remains open; Sigils are not consumed.',
 'journal_found':'Resonance Nexus\nAttune all three Sigils, then use an empty hand on the altar. Blue blade: parry. Red circle: dodge. Golden ward: magic. Use this journal on the altar to save its position.',
 'journal_won':'Ancient Resonance Complete\nResonant Step: after a Perfect Dodge, Speed I for 3 seconds. Cooldown: 8 seconds. This ability survives death and dimensional travel.',
 'adv_title':'The Last Resonance','adv_description':'Defeat the Ashbound Regent and learn Resonant Step.',
}
de={
 'need_sigils':'Stimme Ember-, Tide- und Radiance-Trial-Sigil an diesem Altar ein.',
 'sigil_locked':'Schließe zuerst selbst die zugehörige Prüfung ab.',
 'attuned':'Sigils eingestimmt: %s/3. Sigils bleiben erhalten; das Tor bleibt offen.',
 'completed':'Der Ashbound Regent ist besiegt. Dieser Nexus ruht.',
 'peaceful':'Auf Friedlich kann der Regent nicht erwachen.',
 'empty_hand':'Mit leerer Hand den Regenten erwecken oder dem Kampf beitreten.',
 'battle_warning':'Der Regent erwacht! Blau: parieren. Roter Kreis: ausweichen. Gold: Magie.',
 'blocked':'Der Beschwörungsplatz ist versperrt. Räume hinter dem Altar frei.',
 'waypoint':'Resonance Nexus im Field Journal markiert.',
 'ability_unlocked':'Resonant Step freigeschaltet: Perfect Dodge gibt Tempo I für 3 Sekunden (8 Sekunden Abklingzeit).',
 'journal_hint':'Ancient Resonance\nDrei Höhlenprüfungen bergen die Sigils. Gemeinsam öffnen sie den finalen Resonance Nexus an der Oberfläche der Burned Dimension.',
 'journal_seek':'Die letzte Resonanz\nBringe die drei Trial Sigils zum Resonance Nexus an der Oberfläche. Benutze sie am versiegelten Tor oder Altar. Das Tor bleibt offen, die Sigils bleiben erhalten.',
 'journal_found':'Resonance Nexus\nDrei Sigils einstimmen, dann den Altar mit leerer Hand benutzen. Blau: parieren. Roter Kreis: ausweichen. Gold: Magie. Journal am Altar benutzen, um den Ort zu markieren.',
 'journal_won':'Ancient Resonance beendet\nResonant Step: Perfect Dodge gibt Tempo I für 3 Sekunden. Abklingzeit: 8 Sekunden. Bleibt nach Tod und Dimensionswechsel erhalten.',
 'adv_title':'Die letzte Resonanz','adv_description':'Besiege den Ashbound Regent und erlerne Resonant Step.',
}
for lang,strings in [('en_us',en),('de_de',de)]:
    path=RES/f'assets/{MOD}/lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'));data.update({f'finale.{MOD}.{key}':value for key,value in strings.items()})
    data.update({f'entity.{MOD}.ashbound_regent':'The Ashbound Regent',f'item.{MOD}.ashbound_regent_spawn_egg':'Ashbound Regent Spawn Egg' if lang=='en_us' else 'Ashbound-Regent-Spawn-Ei',f'item.{MOD}.regents_heart':"Regent’s Heart",f'block.{MOD}.nexus_altar':'Resonance Altar',f'block.{MOD}.nexus_gate':'Resonance Seal'})
    subs=['Perfect parry','Perfect dodge','Ember rune ignites','Tide rune extinguishes','Radiance rune resonates','Chronus rune rewinds','Trial completed','Rare treasure collected','Regent awakens','Regent defeated','Katana finishing swing'] if lang=='en_us' else ['Perfekt pariert','Perfekt ausgewichen','Ember-Rune entzündet','Tide-Rune löscht','Radiance-Rune erklingt','Chronus-Rune wirkt','Prüfung abgeschlossen','Seltener Schatz aufgehoben','Regent erwacht','Regent besiegt','Kräftiger Katana-Hieb']
    data.update({f'subtitles.{MOD}.{name}':label for name,label in zip(cues,subs)})
    data[f'guide.{MOD}.resonance_finale']=' '.join(strings[key] for key in ['journal_seek','battle_warning','ability_unlocked'])
    data[f'bestiary.{MOD}.ashbound_regent.habitat']='Resonance Nexus, Burned Realm surface.' if lang=='en_us' else 'Resonance Nexus an der Oberfläche der Burned Dimension.'
    data[f'bestiary.{MOD}.ashbound_regent.behavior']=strings['battle_warning']+' '+strings['ability_unlocked']
    write(str(path.relative_to(RES)),data)
print('Generated Resonance Nexus, final boss assets, eleven sound cues and four small discovery types.')

