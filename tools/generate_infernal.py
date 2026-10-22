"""Devil's Armor enchantment and Tear/Eye Trident progression."""
import generate_devil
from generate_resources import RES,MOD,write,item,recipe
from generate_minerals import read
from pathlib import Path
from PIL import Image

archive=Path(__file__).parent/'texture_sources/infernal'
for name,center in [('devils_tear','ghast_tear'),('devils_eye','ender_eye')]:
    recipe(name,['BBB','BCB','BBB'],{'B':f'{MOD}:creeperblood','C':f'minecraft:{center}'})
    Image.open(archive/f'{name}_original.png').convert('RGBA').resize((16,16),Image.Resampling.NEAREST).save(RES/f'assets/{MOD}/textures/item/{name}.png')
    item(name)
write(f'data/{MOD}/recipe/devils_trident.json',{'type':'minecraft:smithing_transform','template':f'{MOD}:devils_tear','base':'minecraft:trident','addition':f'{MOD}:devils_eye','result':{'id':f'{MOD}:devils_trident'}})
write(f'data/{MOD}/tags/item/devils_armor_enchantable.json',{'values':[f'{MOD}:red_netherite_{kind}' for kind in ['helmet','chestplate','leggings','boots']]})
write(f'data/{MOD}/enchantment/devils_armor.json',{'description':{'translate':f'enchantment.{MOD}.devils_armor'},'supported_items':f'#{MOD}:devils_armor_enchantable','weight':2,'max_level':1,'min_cost':{'base':25,'per_level_above_first':0},'max_cost':{'base':75,'per_level_above_first':0},'anvil_cost':8,'slots':['armor'],'effects':{}})
for tag in ['non_treasure','in_enchanting_table','tradeable','on_random_loot']:
    write(f'data/minecraft/tags/enchantment/{tag}.json',{'replace':False,'values':[f'{MOD}:devils_armor']})
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    data[f'item.{MOD}.devils_tear']="Devil's Tear";data[f'item.{MOD}.devils_eye']="Devil's Eye";data[f'enchantment.{MOD}.devils_armor']="Devil's Armor"
    data[f'guide.{MOD}.devils_trident']='Schmiedetisch: Devil’s Tear im Template-Feld + Trident + Devil’s Eye. Tear: 8 Creeperblood um Ghast Tear. Eye: 8 Creeperblood um Eye of Ender. 8 Schaden, Blood (5 s), Feuer (4 s), Devil’s Charm (5 s). Riptide funktioniert mit diesem Trident auch in Lava.' if lang=='de_de' else 'Smithing: Devil’s Tear template + Trident + Devil’s Eye. Craft each with eight Creeperblood around Ghast Tear / Eye of Ender. 8 damage, Blood (5s), fire (4s), Devil’s Charm (5s). This Trident also enables Riptide in lava.'
    data[f'guide.{MOD}.devils_armor']='Devil’s Armor I auf allen vier getragenen Blood-Netherite-Rüstungsteilen: immun gegen Feuer- und Lavaschaden und Schwimmen in Lava wie in Wasser. Ein fehlendes oder unverzaubertes Teil deaktiviert den Set-Bonus. Erhältlich am Verzauberungstisch, als Buch und im Villager-Handel.' if lang=='de_de' else 'Devil’s Armor I on all four worn Blood Netherite armor pieces: fire/lava immunity and water-like swimming in lava. Missing or unenchanted pieces disable the bonus. Available from enchanting, books and villager trades.'
    write(p,data)
print('Generated Devil armor enchantment, Tear/Eye recipes and revised Trident upgrade.')
