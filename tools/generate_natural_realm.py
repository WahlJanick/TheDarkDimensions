"""Remove deprecated terrain blocks, use muted green grass and keep seeded natural crowns."""
import generate_solar
from generate_resources import RES,MOD,write
import json
removed={'burned_stone':'minecraft:stone','burned_deepslate':f'{MOD}:hard_stone','burned_dirt':'minecraft:dirt'}

def update(value):
    if isinstance(value,dict):
        return {key:update(child) for key,child in value.items() if not any(key==name or key.endswith('.'+name) for name in removed)}
    if isinstance(value,list):return [update(v) for v in value]
    if isinstance(value,str):return removed.get(value.removeprefix(MOD+':'),value) if value.startswith(MOD+':') else value
    return value
for p in RES.rglob('*'):
    if not p.is_file():continue
    assert p.resolve().is_relative_to(RES.resolve())
    if any(p.stem==name or p.stem.startswith(name+'_') for name in removed):
        p.unlink();continue
    if p.suffix=='.json':
        data=json.loads(p.read_text(encoding='utf-8'));changed=update(data)
        if changed!=data:write(p.relative_to(RES).as_posix(),changed)
# The legacy brick recipe used Burned Stone; do not make it compete with
# vanilla Stone Bricks after replacing its ingredient with ordinary Stone.
for path in ['recipe/burned_bricks.json','advancement/recipes/burned_bricks.json']:
    (RES/f'data/{MOD}'/path).unlink(missing_ok=True)
for p in (RES/f'data/{MOD}/worldgen/biome').glob('*.json'):
    data=json.loads(p.read_text(encoding='utf-8'))
    data['effects']['grass_color']='#687568'
    data['effects']['foliage_color']='#5f705b'
    write(p.relative_to(RES).as_posix(),data)
print('Removed Burned Stone/Deepslate/Dirt resources; replaced references and desaturated realm grass.')
