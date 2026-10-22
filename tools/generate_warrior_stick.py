"""Preserve all previous content and install the red-wrapped Warrior Stick."""
import generate_katana_enchantments
from generate_resources import RES,MOD,write
from pathlib import Path
import shutil

source=Path(__file__).parent/'texture_sources/sticks/warrior_stick.png'
shutil.copyfile(source,RES/f'assets/{MOD}/textures/item/warrior_stick.png')
write(f'assets/{MOD}/models/item/warrior_stick.json',{'parent':'minecraft:item/handheld','textures':{'layer0':f'{MOD}:item/warrior_stick'}})
write(f'assets/{MOD}/items/warrior_stick.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/warrior_stick'}})
print('Installed original-size gray Warrior Stick with crimson axe-grip wrapping; removed uniform tint.')
