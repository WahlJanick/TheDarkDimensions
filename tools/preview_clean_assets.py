"""Contact sheet of actual 16x16 exports, enlarged without smoothing for QA."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources/assets/thedarkdimensions'
items=['fire_core','water_core','lightning_core','spell_core','spell_book','fire_spell_book','water_spell_book','lightning_spell_book','ender_chest_spell_book']
blocks=['glowstone_vein_stone','glowstone_crystal_block','pale_glowstone','burned_dirt','burned_gravel','burned_stone','burned_deepslate','burned_cobblestone','burned_log','burned_log_top','burned_planks','burned_leaves','burned_bricks','fire_cave_stone','water_cave_stone','glowing_cave_stone','ember_stone','echo_crystal','echo_relic','improved_obsidian','spell_rune_stone']
entries=[('item',n) for n in items]+[('block',n) for n in blocks]
cols=6;width=180;height=168
sheet=Image.new('RGBA',(cols*width,math.ceil(len(entries)/cols)*height),'#242936');draw=ImageDraw.Draw(sheet)
for index,(kind,name) in enumerate(entries):
    x=(index%cols)*width;y=(index//cols)*height
    draw.rectangle((x+25,y+8,x+153,y+136),fill='#485264')
    sprite=Image.open(RES/f'textures/{kind}/{name}.png').convert('RGBA')
    assert sprite.size==(16,16),(name,sprite.size)
    sheet.alpha_composite(sprite.resize((128,128),Image.Resampling.NEAREST),(x+26,y+9))
    words=name.replace('_',' ').split();line='';lines=[]
    for word in words:
        if len(line)+len(word)>23:lines.append(line);line=''
        line+=(' ' if line else '')+word
    lines.append(line)
    for j,line in enumerate(lines):draw.text((x+8,y+140+j*11),line,fill='white')
out=ROOT/'tools/texture_sources/clean_07/preview.png';sheet.save(out)
print(out)
