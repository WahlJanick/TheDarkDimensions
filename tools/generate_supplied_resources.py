"""Import the user's replacement core, essence and spell-book artwork last."""
import generate_infernal
from generate_resources import RES, MOD, write, item
from pathlib import Path
import shutil, json

archive=Path(__file__).parent/'texture_sources/user_resources'
supplied=RES.parents[3]/'TheDarkDimensions NutzeDieseResourcen!'
archive.mkdir(parents=True,exist_ok=True)
if supplied.exists():
    for path in supplied.glob('*.png'):
        shutil.copyfile(path,archive/path.name)

mapping={
    'blood_core':'blood_core.png',
    'blood_essence':'blood_essence.png',
    'spell_core':'elemental_core_matching_style.png',
    'fire_core':'fire_core.png',
    'fire_spell_book':'fire_spellbook_32x32_editable (1).png',
    'japanese_essence':'japanese_essence.png',
    'lightning_core':'lightning_core.png',
    'lightning_spell_book':'lightning_spellbook_32x32_editable.png',
    'soul_core':'soul_core.png',
    'soul_essence':'soul_essence.png',
    'water_spell_book':'Water_Book_32x32_editable.png',
    'water_core':'water_core.png',
}
for name,filename in mapping.items():
    shutil.copyfile(archive/filename,RES/f'assets/{MOD}/textures/item/{name}.png')
    item(name)
    # Earlier essence models used vanilla sprites with tints. Supplied art is full-colour.
    write(f'assets/{MOD}/items/{name}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{name}'}})
(archive/'mapping.json').write_text(json.dumps(mapping,indent=2)+'\n',encoding='utf-8')
print(f'Imported {len(mapping)} supplied 32x32 textures without resizing or tinting; archived the alternative Spell Core.')
