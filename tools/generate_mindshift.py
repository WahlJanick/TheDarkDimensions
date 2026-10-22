"""End-focused Mindshifter and redstone fissures for Hellsblade; preserve supplied art."""
import generate_relic_katanas
from generate_resources import RES,MOD,write,recipe
import json

recipe('mindshifter',ingredients=['minecraft:end_crystal',f'{MOD}:basic_katana','minecraft:shulker_shell'])
for chest in ['chronus_sanctum','sunken_archive','hidden_cache']:
    path=f'data/{MOD}/loot_table/chests/{chest}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data['pools']=[pool for pool in data['pools'] if not any(entry.get('name')==f'{MOD}:mindshifter' for entry in pool['entries'])];write(path,data)
path=f'data/{MOD}/loot_table/chests/relic_end_city_treasure.json';data=json.loads((RES/path).read_text(encoding='utf-8'));data['pools'][0]['condition']['chance']=.04;write(path,data)
write(f'data/{MOD}/tags/entity_type/mindshift_swap_immune.json',{'replace':False,'values':['minecraft:ender_dragon','minecraft:wither']})

def animation(name,poses):
    write(f'assets/{MOD}/player_animations/{name}.json',{'version':3,'name':name,'author':'The Dark Dimensions','emote':{'isLoop':'false','beginTick':0,'endTick':12,'stopTick':16,'degrees':False,'moves':[{'tick':tick,'easing':ease,'torso':{'yaw':yaw,'pitch':tilt},'rightArm':{'pitch':right,'roll':roll},'leftArm':{'pitch':left,'roll':-roll*.5}} for tick,ease,yaw,tilt,right,roll,left in poses]}})
# Short preparation, forward blade flick and recovery; no full-body spinning.
animation('mindshifter_cast',[(0,'EASEOUTQUAD',0,0,-.4,-.1,-.3),(3,'EASEINOUTQUAD',-.28,-.06,-1.7,-.35,-1.15),(6,'EASEOUTQUAD',.2,.03,-.75,-.12,-.7),(12,'EASEINOUTQUAD',0,0,0,0,0)])
animation('mindshifter_shift',[(0,'EASEOUTQUAD',-.18,-.08,-.85,-.2,-.7),(4,'EASEOUTQUAD',.1,-.02,-1.1,-.15,-.8),(12,'EASEINOUTQUAD',0,0,0,0,0)])

for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'));de=lang=='de_de'
    data[f'guide.{MOD}.mindshifter']=('End Crystal + Basic Katana + Shulker Shell, formlos. 8,5 Schaden, 1,5 Tempo, +2 Reichweite. Rechtsklick: zielsuchendes Shulker-Projektil mit 5 Schaden und 5 Sekunden Cooldown, 3 Haltbarkeit je Schuss. Bei einem sicheren Treffer werden die Positionen getauscht, ohne Levitation. Schleichen + Rechtsklick: Parieren. 24 Blöcke Zielerfassung, höchstens 32 Blöcke Tauschreichweite. Blockierte/ungeladene Ziele, Flüssigkeiten, fehlender Boden, Reittiere und Vanilla-Endbosse verhindern den Tausch; ein Treffer verursacht weiterhin Schaden. Reparatur mit Shulker Shells.' if de else 'End Crystal + Basic Katana + Shulker Shell, shapeless. 8.5 damage, 1.5 speed, +2 reach. Right-click fires a homing Shulker projectile: five damage, five-second cooldown, three durability. Safe impacts exchange positions without levitation. Sneak + right-click to parry. Aim range 24 blocks; swap range up to 32 blocks. Blocked/unloaded destinations, liquids, unsupported ground, mounts and vanilla end bosses prevent swapping; confirmed hits still deal damage. Repair with Shulker Shells.')
    data[f'guide.{MOD}.hellsblade']=('9,5 Schaden, 1,4 Tempo, +2 Reichweite. Crimson Rupture: Treffer hinterlassen bis zu zwei rote Risse am Ziel. Sie gehören dem Angreifer und verfallen nach 4 Sekunden. Der dritte Kombotreffer bricht sie für 2 Magieschaden je Riss auf (2–4), ohne Feuer oder Blockzerstörung. Mehrfachtreffer derselben Schwungphase lösen keinen zweiten Impuls aus. Rezept und seltene Fundorte bleiben erhalten.' if de else '9.5 damage, 1.4 speed, +2 reach. Crimson Rupture: hits leave up to two red fissures on the target, owned by the attacker and expiring after four seconds. The third combo hit ruptures them for two magic damage per fissure (2–4), without fire or terrain damage. Multiple callbacks in one swing cannot duplicate the pulse. Existing recipe and rare treasure locations are retained.')
    data[f'perk.{MOD}.mindshift']='Mindshift · Rechtsklick: Positionstausch (5 s)' if de else 'Mindshift · right-click: swap positions (5s)'
    data[f'perk.{MOD}.crimson_rupture']='Crimson Rupture · Risse beim dritten Treffer aufbrechen' if de else 'Crimson Rupture · third hit ruptures fissures'
    data[f'combat.{MOD}.mindshift_cooldown']='Mindshift: noch %s s' if de else 'Mindshift: %ss remaining'
    data[f'guide.{MOD}.mindshifter']+=(' Als Fundwaffe nur in End Cities (4 %). Die früheren Fundorte vor dem End entfallen.' if de else 'Found as rare treasure only in End Cities (4%). Earlier pre-End treasure locations are removed.')
    write(path,data)
print('Updated End-focused Mindshifter recipe, two native player animations, swap immunity tag and Crimson Rupture guides; supplied textures and models unchanged.')
