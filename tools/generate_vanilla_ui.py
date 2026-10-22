"""Update only localized UI strings. Safe to run alone without rebuilding other resources."""
from pathlib import Path
import json

root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/thedarkdimensions/lang'
strings={
    'bind_element':('Element binden','Bind element'),
    'bind_hint':('Spellbook + Core: bindet ein Element. Beim Master-Buch werden weitere Elemente hinzugefügt.','Spellbook + Core: binds an element. Adds another element to a Master Spellbook.'),
    'spellbook':('Spellbook','Spellbook'),
    'spellwheel':('Spellwheel','Spellwheel'),
    'empty_slot':('Leerer Slot','Empty slot'),
    'spell_level':('%s · Stufe %s','%s · Level %s'),
    'wheel_hint':('Loslassen · 1–5 / Klick','Release · 1–5 / click'),
}
for language,index in [('de_de',0),('en_us',1)]:
    path=root/f'{language}.json';data=json.loads(path.read_text(encoding='utf-8'))
    for key,values in strings.items():data[f'ui.thedarkdimensions.{key}']=values[index]
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Updated vanilla UI labels and native tooltips in German and English; no textures changed.')
