"""Export ImageGen sheets to native Minecraft texture sizes without recolouring."""
from pathlib import Path
from PIL import Image
import shutil
import zipfile
ROOT=Path(__file__).parent/'texture_sources'
GEN=Path('C:/Users/janic/.codex/generated_images/01a0f15e-d4dc-7bc0-a9f1-134366037cf2')
def sheet(source, names, cols, rows, folder):
    path=GEN/source
    shutil.copyfile(path,ROOT/'generated'/source)
    image=Image.open(path).convert('RGBA')
    for i,name in enumerate(names):
        x,y=i%cols,i//cols
        tile=image.crop((round(x*image.width/cols),round(y*image.height/rows),round((x+1)*image.width/cols),round((y+1)*image.height/rows)))
        target=ROOT/folder/f'{name}.png';target.parent.mkdir(parents=True,exist_ok=True)
        tile.resize((16,16),Image.Resampling.NEAREST).save(target)
sheet('exec-74d6e3fe-f76c-4c46-9fc2-d3c94c6a129f.png',['fire_cave_stone','fire_crystal','water_cave_stone','water_crystal','glowing_cave_stone','glowing_honey_block','spell_rune_stone','glowing_crystal'],4,2,'blocks')
sheet('exec-68ac50a2-5374-4b99-91c6-11a3eb1270b1.png',['water_core','lightning_core','spell_core','glowing_honey'],2,2,'items')
for name,source,size in [('fire_mage','exec-ccbc2469-1963-4a18-80fe-73caea4f54b7.png',(64,128)),('water_husk','exec-108b34e7-084f-467c-b5ca-dc713da33fa2.png',(64,64)),('glowing_bee','exec-949e518f-032e-483b-832c-17b1de94ca3d.png',(64,64))]:
    shutil.copyfile(GEN/source,ROOT/'generated'/source)
    target=ROOT/'entities'/f'{name}.png';target.parent.mkdir(parents=True,exist_ok=True)
    Image.open(GEN/source).resize(size,Image.Resampling.NEAREST).save(target)
with zipfile.ZipFile(Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar') as jar:
    for name,native in [('fire_mage','witch'),('water_husk','husk'),('glowing_bee','bee')]:
        (ROOT/'items'/f'{name}_spawn_egg.png').write_bytes(jar.read(f'assets/minecraft/textures/item/{native}_spawn_egg.png'))
print('Exported elemental textures and vanilla spawn egg icons.')
