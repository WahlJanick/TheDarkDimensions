"""Katana defense: native notices, extensible heavy-enemy tag and restrained PAL poses."""
import generate_exploration
from generate_resources import RES,MOD,write
import json

write(f'data/{MOD}/tags/entity_type/katana_unparryable.json',{'replace':False,'values':['minecraft:ravager','minecraft:iron_golem','minecraft:warden',f'{MOD}:cinder_broodmother']})

def animation(name,pose):
    neutral={bone:{axis:0 for axis in values} for bone,values in pose.items()}
    moves=[{'tick':0,'easing':'EASEOUTQUAD',**neutral},{'tick':2,'easing':'EASEOUTQUAD',**pose},{'tick':5,'easing':'EASEINOUTQUAD',**pose},{'tick':9,'easing':'EASEINOUTQUAD',**neutral}]
    write(f'assets/{MOD}/player_animations/{name}.json',{'version':3,'name':name,'author':'The Dark Dimensions','emote':{'isLoop':'false','beginTick':0,'endTick':6,'stopTick':10,'degrees':False,'moves':moves}})

animation('katana_parry',{'torso':{'yaw':-.15,'pitch':-.08},'rightArm':{'pitch':-1.15,'roll':-.35},'leftArm':{'pitch':-.8,'roll':.3}})
for direction in ['forward','back','left','right']:
    side=1 if direction=='right' else -1 if direction=='left' else 0
    forward=1 if direction=='forward' else -1 if direction=='back' else 0
    animation('katana_dodge_'+direction,{'torso':{'pitch':forward*.18,'roll':side*.22},'rightLeg':{'pitch':forward*-.5,'roll':side*.25},'leftLeg':{'pitch':forward*.5,'roll':side*-.15},'rightArm':{'pitch':-.65,'roll':-.2},'leftArm':{'pitch':-.45,'roll':.2}})

for lang in ['de_de','en_us']:
    path=f'assets/{MOD}/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
    data['key.thedarkdimensions.katana_dodge']='Katana: Ausweichen' if lang=='de_de' else 'Katana: Dodge'
    data['key.category.thedarkdimensions.combat']='The Dark Dimensions – Kampf' if lang=='de_de' else 'The Dark Dimensions – Combat'
    notices={'parry':('Pariert!','Parry!'),'perfect_parry':('Perfekt pariert – Konter bereit!','Perfect Parry – Counter ready!'),'dodge':('Ausweichen','Dodge'),'perfect_dodge':('Perfekt ausgewichen!','Perfect Dodge!'),'counter':('Konter!','Counter!'),'guard':('Deckung','Guard'),'unparryable':('Nicht parierbar – ausweichen!','Unparryable – Dodge!')}
    for key,labels in notices.items():data[f'combat.{MOD}.{key}']=labels[lang=='en_us']
    write(path,data)
print('Generated heavy-enemy tag, five short katana animations and native defense notices.')
