"""Small pixel-art additions extending the mod's existing item and palette system."""
import generate_magic
from generate_resources import RES,MOD,write,recipe,item
from PIL import Image,ImageDraw
import zipfile,io,json

def sprite(name,draw):
 im=Image.new('RGBA',(16,16));draw(ImageDraw.Draw(im));im.save(RES/f'assets/{MOD}/textures/item/{name}.png');item(name)
def prism(d):
 d.polygon([(7,1),(12,6),(10,12),(7,14),(3,10),(3,5)],fill='#182844')
 d.polygon([(7,2),(11,6),(9,11),(7,13),(4,9),(4,5)],fill='#517bd3')
 d.polygon([(7,2),(7,12),(4,9),(4,5)],fill='#83bbea')
 d.line([(7,2),(10,6),(7,12)],fill='#bedfff');d.line([(4,7),(10,7)],fill='#385abe')
 d.rectangle((5,13,9,14),fill='#dfc999')
def talisman(d):
 d.line([(6,1),(4,3),(4,6),(7,8),(10,6),(10,3),(8,1)],fill='#b99158',width=1)
 d.rectangle((5,7,10,13),fill='#57384d');d.rectangle((6,8,9,12),fill='#efa8b9')
 for x,y in [(7,8),(6,9),(8,9),(7,10)]:d.point((x,y),fill='#fff0de')
 d.line((7,13,7,15),fill='#b84d6a');d.point((6,15),fill='#e28297');d.point((8,15),fill='#e28297')
def coating(d):
 d.rectangle((6,1,9,2),fill='#b68c53');d.rectangle((6,3,9,4),fill='#b8dbd6')
 d.polygon([(5,4),(10,4),(12,7),(12,12),(10,14),(5,14),(3,12),(3,7)],fill='#344d57')
 d.polygon([(5,5),(10,5),(11,7),(11,12),(9,13),(5,13),(4,11),(4,7)],fill='#8ecacc')
 d.rectangle((5,8,10,12),fill='#d5db67');d.rectangle((5,8,6,11),fill='#f3eda0');d.point((10,6),fill='#efffff')
def bandage(d):
 d.polygon([(2,6),(9,2),(13,6),(13,9),(6,14),(2,10)],fill='#7b6971')
 d.polygon([(3,6),(9,3),(12,6),(12,9),(6,13),(3,10)],fill='#e4dacf')
 d.line([(5,5),(9,9),(9,11)],fill='#aea6a3',width=1)
 d.line([(7,4),(11,8),(11,9)],fill='#aea6a3',width=1)
 d.rectangle((4,8,6,10),fill='#dc95a9');d.point((5,9),fill='#fff0dd')
def sandals(d):
 for x,y in [(1,5),(8,2)]:
  d.rectangle((x,y+1,x+5,y+7),fill='#4d3635');d.rectangle((x+1,y+1,x+4,y+6),fill='#b48c65')
  d.line([(x+1,y+2),(x+3,y+4),(x+4,y+2)],fill='#f0b2c0');d.line([(x+1,y+5),(x+4,y+5)],fill='#efe5d6')
sprite('astral_prism',prism);sprite('sakura_talisman',talisman);sprite('luminous_coating',coating);sprite('ronin_bandage',bandage);sprite('windstep_sandals',sandals)
recipe('luminous_coating',ingredients=[f'{MOD}:glowing_honey',f'{MOD}:essentialium','minecraft:amethyst_shard'])
write(f'data/{MOD}/recipe/apply_luminous_coating.json',{'type':f'{MOD}:apply_luminous_coating'})
recipe('astral_prism',[' E ','MAM',' E '],{'E':f'{MOD}:essentialium','M':f'{MOD}:metalinium_ingot','A':f'{MOD}:improved_amethyst'})
recipe('sakura_talisman',[' S ','EJE',' S '],{'S':'minecraft:string','E':f'{MOD}:japanese_essence','J':'minecraft:cherry_sapling'})
recipe('ronin_bandage',ingredients=[f'{MOD}:japanese_essence','minecraft:paper','minecraft:string'],count=4)
recipe('windstep_sandals',['J J','L L','S S'],{'J':f'{MOD}:japanese_essence','L':'minecraft:leather','S':f'{MOD}:reinforced_stick'})
for slot,name,order in [('focus','astral_prism',220),('talisman','sakura_talisman',230)]:
 write(f'data/{MOD}/curios/slots/{slot}.json',{'size':1,'order':order,'icon':'curios:slot/empty_charm_slot','validators':['curios:tag'],'entities':['minecraft:player']})
 write(f'data/{MOD}/curios/entities/player_{slot}.json',{'entities':['minecraft:player'],'slots':[slot]})
 write(f'data/curios/tags/item/{slot}.json',{'replace':False,'values':[f'{MOD}:{name}']})
# An extension point for modded weapons whose behavior lacks native weapon components.
write(f'data/{MOD}/tags/item/coatable_weapons.json',{'replace':False,'values':['minecraft:bow','minecraft:crossbow','minecraft:trident','minecraft:mace',f'{MOD}:throwable_axe']})
for lang in ['de_de','en_us']:
 p=RES/f'assets/{MOD}/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));de=lang=='de_de'
 for name,label in [('luminous_coating','Luminous Coating'),('astral_prism','Astral Prism'),('sakura_talisman','Sakura-Talisman' if de else 'Sakura Talisman'),('ronin_bandage','Ronin-Bandage' if de else 'Ronin Bandage'),('windstep_sandals','Windstep-Sandalen' if de else 'Windstep Sandals')]:data[f'item.{MOD}.{name}']=label
 data[f'perk.{MOD}.luminous_coating']='Luminous Coating'
 data[f'perk.{MOD}.coating_apply']='Mit einer Waffe im Craftingtable kombinieren' if de else 'Combine with a weapon in a crafting grid'
 data[f'perk.{MOD}.astral_prism']='Fokus / Nebenhand: −5 % Zauber-Cooldown' if de else 'Focus / offhand: −5% spell cooldown'
 data[f'perk.{MOD}.sakura_talisman']='Talisman / Nebenhand: 3 Sekunden Katana-Kombofenster' if de else 'Talisman / offhand: 3-second katana combo window'
 data[f'perk.{MOD}.ronin_bandage']='Regeneration II (5 Sekunden); entfernt Frosted' if de else 'Regeneration II (5 seconds); removes Frosted'
 data[f'perk.{MOD}.windstep_sandals']='Windstep-Taste: kurzer Bodendash; 5 Sekunden Cooldown' if de else 'Windstep key: short ground dash; 5-second cooldown'
 data[f'key.{MOD}.windstep']='Windstep-Dash' if de else 'Windstep dash'
 data['curios.identifier.focus']='Fokus' if de else 'Focus';data['curios.identifier.talisman']='Talisman'
 data[f'guide.{MOD}.luminous_coating']='Glowing Honey + Essentialium + Amethystsplitter → Luminous Coating. Im Craftingtable mit einer Waffe kombinieren. Treffer geben 8 Sekunden Glowing; der dauerhafte Perk erhält alle Verzauberungen und Itemdaten.' if de else 'Glowing Honey + Essentialium + amethyst shard → Luminous Coating. Combine with a weapon in a crafting grid. Hits apply Glowing for 8 seconds; the permanent perk preserves enchantments and item data.'
 write(f'assets/{MOD}/lang/{lang}.json',data)
print('Generated five 16x16 perk icons, coatings, Astral Prism and all three Japanese Essence recipes.')
