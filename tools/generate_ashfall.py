"""Weather-compatible katana attributes and an original three-strike finisher animation."""
import generate_elemental_stone
from generate_resources import RES,MOD,write
attacks=[]
for animation,damage,upswing in [('bettercombat:two_handed_slash_horizontal_right',1.0,0.5),('bettercombat:two_handed_slash_horizontal_left',1.0,0.5),(MOD+':katana_finisher',1.25,0.65)]:
 attacks.append({'hitbox':'HORIZONTAL_PLANE','damage_multiplier':damage,'angle':120,'upswing':upswing,'animation':animation,'swing_sound':{'id':'bettercombat:katana_slash','pitch':0.8 if len(attacks)==2 else 1.0}})
for name in ['frostgleam','bloodgrief','souleater','stormveil']:
 write(f'data/{MOD}/weapon_attributes/{name}.json',{'attributes':{'range_bonus':0,'pose':'bettercombat:pose_two_handed_katana','two_handed':True,'category':'katana','attacks':attacks}})
# Own fallback resource; with Better Combat the client adapts the installed two-handed
# slash on every resource reload, preserving hand translations and pack overrides.
moves=[]
for tick,yaw,pitch in [(0,0,-.7),(9,-.45,-.8),(11,.3,-1.2),(14,.7,-1.1),(20,.4,-.8)]:
 moves.append({'tick':tick,'easing':'EASEOUTQUAD' if tick==11 else 'EASEINOUTQUAD','torso':{'yaw':yaw},'rightArm':{'pitch':pitch,'roll':-.4},'leftArm':{'pitch':pitch-.2,'roll':.4}})
write(f'assets/{MOD}/player_animations/katana_finisher.json',{'version':3,'name':'katana_finisher','author':'The Dark Dimensions','emote':{'isLoop':'false','returnTick':2,'beginTick':9,'endTick':20,'stopTick':30,'degrees':False,'moves':moves}})
print('Generated Better Combat three-strike katana sequences and finisher animation.')

write(f'data/{MOD}/damage_type/katana_finisher.json',{'message_id':'player','exhaustion':0.1,'scaling':'never'})
import json
path='data/minecraft/tags/damage_type/bypasses_cooldown.json'
tag=json.loads((RES/path).read_text());tag['values'].append(MOD+':katana_finisher');write(path,tag)
