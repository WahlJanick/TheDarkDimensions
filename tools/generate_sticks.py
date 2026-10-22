"""Split stick progression while preserving the two existing saved item IDs."""
import generate_supplied_resources
from generate_resources import RES, MOD, write, recipe
from generate_minerals import read

old=f'{MOD}:improved_stick'
new=f'{MOD}:reinforced_stick'
def upgrade(value):
    if isinstance(value,dict): return {key:upgrade(child) for key,child in value.items()}
    if isinstance(value,list): return [upgrade(child) for child in value]
    return new if value==old else value

# Existing handle recipes now require the new Improved Stick. Burned wood still
# produces cooled Burned Sticks and must pass through the additional crafting step.
for path in (RES/f'data/{MOD}/recipe').glob('*.json'):
    if path.stem=='burned_wood_sticks':continue
    write(str(path.relative_to(RES)),upgrade(read(path)))
for path in (RES/f'data/{MOD}/advancement/recipes').glob('*.json'):
    if path.stem=='burned_wood_sticks':continue
    write(str(path.relative_to(RES)),upgrade(read(path)))
recipe('reinforced_stick',ingredients=[old,'minecraft:stick'])
write(f'assets/{MOD}/models/item/reinforced_stick.json',{'parent':'minecraft:item/handheld','textures':{'layer0':f'{MOD}:item/improved_stick'}})
write(f'assets/{MOD}/items/reinforced_stick.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/reinforced_stick'}})
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    data[f'item.{MOD}.burned_stick']='Burning Stick'
    data[f'item.{MOD}.improved_stick']='Burned Stick'
    data[f'item.{MOD}.reinforced_stick']='Improved Stick'
    data[f'guide.{MOD}.burned_stick']='Ashes + Feuerzeug + Stock → Burning Stick. In einer Hand verursacht er Feuerschaden. Ins Wasser werfen → Burned Stick.' if lang=='de_de' else 'Ashes + Flint and Steel + Stick → Burning Stick. Holding it deals fire damage. Throw it into water → Burned Stick.'
    data[f'guide.{MOD}.improved_stick']='Burning Stick ins Wasser werfen → Burned Stick. Burned Stick + normaler Stock → Improved Stick (formlos).' if lang=='de_de' else 'Throw a Burning Stick into water → Burned Stick. Burned Stick + normal Stick → Improved Stick (shapeless).'
    data[f'guide.{MOD}.reinforced_stick']='Burned Stick + normaler Stock → Improved Stick (formlos). Für verbesserte Werkzeuggriffe, Katanas, Fire Core und weitere Griff-Rezepte.' if lang=='de_de' else 'Burned Stick + normal Stick → Improved Stick (shapeless). Used for improved tool handles, katanas, Fire Core and other handle recipes.'
    write(p,data)
print('Added three-stage Burning / Burned / Improved Stick progression; preserved existing saved IDs.')
