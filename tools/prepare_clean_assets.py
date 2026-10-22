"""Archive imagegen originals and export native-size pixel sprites, without smoothing."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,shutil
ROOT=Path(__file__).resolve().parents[1]
SOURCES=ROOT/'tools/texture_sources'
GENERATED=SOURCES/'generated/clean_07'
for manifest in sorted(GENERATED.glob('*.json')):
    data=json.loads(manifest.read_text(encoding='utf-8-sig'))
    original=GENERATED/f"{manifest.stem}_original.png"
    if not original.exists():shutil.copyfile(data['path'],original)
    out=SOURCES/'clean_07'/data['kind']/f"{data['name']}.png"
    out.parent.mkdir(parents=True,exist_ok=True)
    image=Image.open(original).convert('RGBA').resize((16,16),Image.Resampling.NEAREST)
    image.save(out)
    print('Exported',data['kind'],data['name'],image.size)
