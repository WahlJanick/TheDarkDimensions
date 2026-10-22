"""Optional Trinkets Updated 4.2 slot definitions and native slot tags."""
import generate_perks
from generate_resources import RES, MOD, write
import json

books=json.loads((RES/'data/curios/tags/item/spellbook.json').read_text(encoding='utf-8'))['values']
slots=[('dark_spellbook',books,'Spellbook','Spellbook'),('dark_focus',[f'{MOD}:astral_prism'],'Fokus','Focus'),('dark_talisman',[f'{MOD}:sakura_talisman'],'Talisman','Talisman')]
for order,(name,values,_,_) in enumerate(slots):
 write(f'data/trinkets/slots/chest/{name}.json',{'amount':1,'order':200+order,'max_stack_size':1,'icon':'trinkets:container/slots/necklace','validator_predicates':['trinkets:tag'],'quick_move_predicates':['trinkets:tag'],'interact_equipable_predicates':['trinkets:tag'],'cosmetic_slots':False})
 write(f'data/trinkets/tags/item/chest/{name}.json',{'replace':False,'values':values})
write('data/trinkets/entities/thedarkdimensions_player.json',{'replace':False,'entities':['minecraft:player'],'slots':[f'chest/{name}' for name,_,_,_ in slots]})
for lang in ['de_de','en_us']:
 data=json.loads((RES/f'assets/{MOD}/lang/{lang}.json').read_text(encoding='utf-8'))
 for name,_,de,en in slots:data[f'trinkets.slot.chest.{name}']=de if lang=='de_de' else en
 write(f'assets/{MOD}/lang/{lang}.json',data)
print('Generated Trinkets Updated spellbook, focus and talisman slots.')
