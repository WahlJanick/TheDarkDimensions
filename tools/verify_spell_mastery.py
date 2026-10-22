"""Source/resource checks for unreleased mastery work; deliberately does not build or modify a JAR."""
from pathlib import Path
import json,re,zipfile
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';JAVA=ROOT/'src/main/java/at/janic/thedarkdimensions'
source=(JAVA/'MagicSkill.java').read_text(encoding='utf-8')
skills=re.findall(r'\b([A-Z][A-Z_]+)\("',source)
assert len(skills)==39 and len(set(skills))==39
for language in ['de_de','en_us']:
    data=json.loads((RES/f'assets/thedarkdimensions/lang/{language}.json').read_text(encoding='utf-8'))
    for skill in skills:
        keys=[f'mastery.thedarkdimensions.{skill.lower()}.{choice}' for choice in [1,2]]
        assert data[keys[0]]!=data[keys[1]]
        for key in keys:assert data[key] and data[key+'.desc']
    assert 'guide.thedarkdimensions.spell_mastery' in data
recipe=json.loads((RES/'data/thedarkdimensions/recipe/wool_glider.json').read_text())
assert recipe['pattern']==[' S ','SWS','WLW'] and recipe['key']=={'S':'minecraft:stick','W':'#minecraft:wool','L':'minecraft:leather'}
assert json.loads((RES/'assets/thedarkdimensions/particles/blood_drop.json').read_text())=={'textures':['minecraft:drip_fall']}
assert Image.open(RES/'assets/thedarkdimensions/textures/entity/ashbound_regent.png').size==(64,64)
assert 'textures/entity/ashbound_regent.png' in (JAVA/'client/AshboundRegentRenderer.java').read_text()
with zipfile.ZipFile(Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as vanilla:
    names=set(vanilla.namelist())
    for name in ['advancements/task_frame_obtained','advancements/task_frame_unobtained','hud/crosshair_attack_indicator_background','hud/crosshair_attack_indicator_progress']:
        assert f'assets/minecraft/textures/gui/sprites/{name}.png' in names
    assert 'assets/minecraft/textures/particle/drip_fall.png' in names
assert len((ROOT/'docs/spell_masteries.md').read_text(encoding='utf-8').splitlines())>=45
print('Validated 78 DE/EN mastery choices, native UI/particle sprites, editable 64x64 Regent skin, mastery documentation and pending mixed-color Wool Glider recipe. No JAR built.')
