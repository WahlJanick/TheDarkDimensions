"""Check packaged assets, JSON and preservation of supplied tool art."""
import json,zipfile,io
from pathlib import Path
from PIL import Image
root=Path(__file__).resolve().parent.parent
def artist(original,archive):
    """Artist folders may be removed after import; verify against archived originals then."""
    return original if original.exists() else root/'tools/texture_sources'/archive
version=next(line.split('=',1)[1].strip() for line in (root/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='))
jar=root/f'build/libs/thedarkdimensions-{version}.jar'
with zipfile.ZipFile(jar) as z:
    files={n for n in z.namelist() if not n.endswith('/')}
    for name in ['solar_essence','pocket_dimension_key']:
        icon=Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{name}.png')))
        if name=='solar_essence':
            original=artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!/Solar_Essence.png','solar/solar_essence_supplied.png')
            assert z.read(f'assets/thedarkdimensions/textures/item/{name}.png')==original.read_bytes()
        else:
            assert icon.size==(32,32) and icon.mode=='RGBA' and icon.getextrema()[3][0]==0
    solar=json.loads(z.read('data/thedarkdimensions/loot_table/entities/mummy.json'))['pools'][-1]
    assert solar['condition']=={'type':'minecraft:random_chance','chance':0.05}
    assert solar['entries']==[{'type':'minecraft:item','name':'thedarkdimensions:solar_essence'}]
    key=json.loads(z.read('data/thedarkdimensions/recipe/pocket_dimension_key.json'))
    assert key['pattern']==[' N ',' N ','GEG'] and key['key']=={'N':'minecraft:netherite_ingot','G':'minecraft:gold_ingot','E':'thedarkdimensions:solar_essence'}
    forge=json.loads(z.read('data/thedarkdimensions/recipe/solar_forge.json'))
    assert forge['template']=='thedarkdimensions:solar_essence' and forge['base']=='minecraft:blast_furnace' and forge['addition']=='minecraft:coal_block' and forge['result']['id']=='thedarkdimensions:solar_forge'
    dimension=json.loads(z.read('data/thedarkdimensions/dimension/pocket_dimension.json'))
    assert dimension['generator']['type']=='minecraft:flat' and dimension['generator']['settings']['layers']==[{'height':1,'block':'minecraft:air'}]
    for name in ['SolarForgeBlockEntity','PocketDimensions','StormveilItem']:
        assert f'at/janic/thedarkdimensions/{name}.class' in files
    blade_archive=root/'tools/texture_sources/stormveil'
    for original in (blade_archive/'optifine/cit/kaminari_pack').glob('*.png*'):
        assert z.read(f'assets/thedarkdimensions/textures/item/stormveil/{original.name}')==original.read_bytes()
        if original.name.endswith('.mcmeta'):json.loads(z.read(f'assets/thedarkdimensions/textures/item/stormveil/{original.name}'))
    for original in (blade_archive/'textures/particle').glob('*.png'):
        assert z.read(f'assets/thedarkdimensions/textures/particle/stormveil/{original.name}')==original.read_bytes()
    original_model=json.loads((blade_archive/'optifine/cit/kaminari_pack/kaminari.json').read_text())
    for element in original_model['elements']:
        element['faces']={side:face for side,face in element['faces'].items() if face.get('texture')!='#missing'}
    blade_model=json.loads(z.read('assets/thedarkdimensions/models/item/stormveil.json'))
    for element in blade_model['elements']:
        for field in ['from','to']:
            assert len(element[field])==3 and all(-16<=v<=32 for v in element[field])
        for face in element['faces'].values():
            assert face['texture'].startswith('#') and face['texture'][1:] in blade_model['textures']
            assert face.get('rotation',0) in [0,90,180,270]
    assert blade_model['elements'][:len(original_model['elements'])]==original_model['elements']
    assert blade_model['display']==original_model['display']
    assert any(e.get('light_emission')==15 for e in blade_model['elements'])
    for name in ['bypasses_cooldown','is_lightning']:
        assert 'thedarkdimensions:stormveil_arc' in json.loads(z.read(f'data/minecraft/tags/damage_type/{name}.json'))['values']
    elemental=json.loads(z.read('assets/thedarkdimensions/elemental_stone_texture_sources.json'))
    assert len(elemental)==108
    for target,info in elemental.items():
        source=Image.open(root/'tools/texture_sources/mummy_hard_stone'/info['source']).convert('RGBA')
        image=Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/block/{target}.png'))).convert('RGBA')
        assert image.size==source.size==tuple(info['size'])
        mask=set(info['stone_pixels']);original=list(source.get_flattened_data());actual=list(image.get_flattened_data())
        for index,(before,after) in enumerate(zip(original,actual)):
            if index not in mask:assert before==after,(target,index,'ore pixel changed')
            else:assert after==tuple(min(255,round(before[c]*info['gains'][c])) for c in range(3))+(before[3],)
    for theme in ['fire','water','glowing']:
        for mineral in ['','coal','copper','iron','gold','redstone','lapis','diamond','emerald']:
            name=theme+'_cave_stone'+('_'+mineral+'_ore' if mineral else '')
            assert len(json.loads(z.read(f'assets/thedarkdimensions/blockstates/{name}.json'))['variants'][''])==4
            assert 'tints' not in json.loads(z.read(f'assets/thedarkdimensions/items/{name}.json'))['model']
    clean_palette=json.loads(z.read('assets/thedarkdimensions/clean_block_palette.json'))
    assert all(theme+'_cave_stone' not in clean_palette for theme in ['fire','water','glowing'])
    supplied_mapping=json.loads((root/'tools/texture_sources/user_resources/mapping.json').read_text(encoding='utf-8'))
    supplied_art=artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!','user_resources')
    for name,filename in supplied_mapping.items():
        source=supplied_art/filename
        if not source.exists():source=root/'tools/texture_sources/user_resources'/filename
        assert z.read(f'assets/thedarkdimensions/textures/item/{name}.png')==source.read_bytes()
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{name}.png'))).size==(32,32)
        model=json.loads(z.read(f'assets/thedarkdimensions/models/item/{name}.json'))
        assert model['textures']['layer0']==f'thedarkdimensions:item/{name}'
        assert 'tints' not in json.loads(z.read(f'assets/thedarkdimensions/items/{name}.json'))['model']
    assert not any('/test/' in n or 'flat_all_dimensions' in n or 'integration_empty' in n for n in files)
    for n in files:
        if n.endswith('.json'):json.loads(z.read(n))
        if n.endswith('.png'):Image.open(io.BytesIO(z.read(n))).verify()
    wood_sources=json.loads(z.read('assets/thedarkdimensions/dark_acacia_texture_sources.json'))
    realm=json.loads(z.read('data/thedarkdimensions/dimension_type/burned_realm.json'))
    assert realm['ambient_light']==0.35 and realm['attributes']['minecraft:visual/sky_light_factor']==0.85
    assert realm['attributes']['minecraft:visual/fog_end_distance']==192.0
    for name in ['ash_wastes','charred_forest','crystal_hollows','normal_caves','fire_cave','water_cave','glowing_cave']:
        atmosphere=json.loads(z.read(f'data/thedarkdimensions/worldgen/biome/{name}.json'))['attributes']
        assert atmosphere['minecraft:visual/sky_color']=='#b1aaa0' and atmosphere['minecraft:visual/fog_color']=='#c0b8ad'
    for target,source in wood_sources.items():
        original=supplied_art/source
        if not original.exists():original=root/'tools/texture_sources/user_resources'/source
        assert z.read(f'assets/thedarkdimensions/textures/block/{target}.png')==original.read_bytes()
    hard_sources=json.loads(z.read('assets/thedarkdimensions/hard_stone_texture_sources.json'))
    for target,filename in hard_sources.items():
        original=artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!'/filename,'mummy_hard_stone/'+filename)
        assert z.read(f'assets/thedarkdimensions/textures/block/{target}.png')==original.read_bytes()
    assert z.read('assets/thedarkdimensions/textures/entity/mummy.png')==artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!/mummy.png','mummy_hard_stone/mummy.png').read_bytes()
    assert 'data/thedarkdimensions/worldgen/biome/black_desert.json' not in files
    for old in ['the_burned','water_husk']:
        assert f'data/thedarkdimensions/loot_table/entities/{old}.json' not in files
    assert not any(n.endswith('/'+old+'.class') for n in files for old in ['TheBurned','WaterHusk','FireCoreItem','OverworldBiomeBuilderMixin','SurfaceMaterialMixin'])
    for removed in ['burned_stone','burned_deepslate','burned_dirt']:
        assert f'assets/thedarkdimensions/blockstates/{removed}.json' not in files
        assert f'assets/thedarkdimensions/items/{removed}.json' not in files
        assert f'data/thedarkdimensions/loot_table/blocks/{removed}.json' not in files
    for biome_name in ['ash_wastes','charred_forest','crystal_hollows','normal_caves','fire_cave','water_cave','glowing_cave']:
        effects=json.loads(z.read(f'data/thedarkdimensions/worldgen/biome/{biome_name}.json'))['effects']
        assert effects['grass_color']=='#687568' and effects['foliage_color']=='#5f705b'
    noise=json.loads(z.read('data/thedarkdimensions/worldgen/noise_settings/burned_realm.json'))
    assert noise['default_block']=='minecraft:stone'
    wand=json.loads(z.read('data/thedarkdimensions/recipe/astral_crystal_wand.json'))
    assert wand['pattern']==['F','G','S'] and wand['key']=={'F':'thedarkdimensions:fire_core','G':'minecraft:ghast_tear','S':'thedarkdimensions:reinforced_stick'}
    assert len(json.loads(z.read('assets/thedarkdimensions/blockstates/burned_planks.json'))['variants'][''])==8
    for name in ['burned_log','burned_planks']:
        assert 'tints' not in json.loads(z.read(f'assets/thedarkdimensions/items/{name}.json'))['model']
    stick=json.loads(z.read('data/thedarkdimensions/recipe/reinforced_stick.json'))
    assert stick['ingredients']==['thedarkdimensions:improved_stick','minecraft:stick'] and stick['result']['id']=='thedarkdimensions:reinforced_stick'
    for lang in ['de_de','en_us']:
        labels=json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
        assert [labels[f'item.thedarkdimensions.{n}'] for n in ['burned_stick','improved_stick','reinforced_stick']]==['Burning Stick','Burned Stick','Improved Stick']
    def models(value):
        if isinstance(value,dict):
            for key,child in value.items():
                if key in ['model','parent'] and isinstance(child,str) and child.startswith('thedarkdimensions:'):
                    assert f'assets/thedarkdimensions/models/{child.split(":",1)[1]}.json' in files, child
                models(child)
        elif isinstance(value,list):
            for child in value:models(child)
    for n in files:
        if n.endswith('.json') and ('/blockstates/' in n or '/models/' in n):models(json.loads(z.read(n)))
    for kind in ['fire','water','glowing']:
        assert f'data/thedarkdimensions/worldgen/biome/{kind}_cave.json' in files
        assert f'data/thedarkdimensions/worldgen/feature/{kind}_cave.json' in files
    for mob in ['fire_mage','mummy','glowing_bee']:
        assert f'assets/thedarkdimensions/textures/entity/{mob}.png' in files
        assert f'data/thedarkdimensions/loot_table/entities/{mob}.json' in files
    assert 'data/thedarkdimensions/structure/elemental_shrine.nbt' in files
    assert 'data/thedarkdimensions/worldgen/biome/normal_caves.json' in files
    assert 'data/thedarkdimensions/worldgen/feature/essentialium_geode.json' in files
    assert 'data/thedarkdimensions/recipe/spelltable.json' in files
    for recipe in ['spell_book','metalinium_ingot','essentialium_infused_metalinium_ingot']:
        assert f'data/thedarkdimensions/recipe/{recipe}.json' in files
    infused=json.loads(z.read('data/thedarkdimensions/recipe/essentialium_infused_metalinium_ingot.json'))
    assert infused['ingredients']==['thedarkdimensions:metalinium_ingot','thedarkdimensions:chronus_orb']
    modifier=json.loads(z.read('data/thedarkdimensions/neoforge/biome_modifier/add_metalinium.json'))
    assert modifier['biomes']=='#minecraft:is_overworld' and modifier['step']=='underground_ores'
    for name in ['spell_book','fire_spell_book','lightning_spell_book','ender_chest_spell_book','water_spell_book','metalinium_nugget','metalinium_ingot','essentialium_infused_metalinium_ingot']:
        assert f'assets/thedarkdimensions/items/{name}.json' in files
    assert 'at/janic/thedarkdimensions/client/SpelltableScreen.class' in files
    assert 'at/janic/thedarkdimensions/KatanaEffects.class' in files
    assert 'at/janic/thedarkdimensions/mixin/BloodHeartMixin.class' in files
    assert 'BloodHeartMixin' in json.loads(z.read('thedarkdimensions.mixins.json'))['client']
    assert 'HammerGlidingMixin' in json.loads(z.read('thedarkdimensions.mixins.json'))['mixins']
    for cls in ['ArmoryContent','ThrowableAxeItem','ThrownAxe','ThunderHammerItem']:
        assert f'at/janic/thedarkdimensions/{cls}.class' in files
    tiers=['wooden','stone','copper','iron','golden','diamond','netherite']
    for tier in tiers:
        fish=f'{tier}_fish_axe';warrior=f'{tier}_warrior_axe'
        for name in [fish,warrior]:
            assert f'assets/thedarkdimensions/items/{name}.json' in files
            assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{name}.png'))).size==(16,16)
            assert f'data/thedarkdimensions/recipe/{name}.json' in files
        assert z.read(f'assets/thedarkdimensions/textures/item/{fish}.png')==artist(root.parent/f'fish-axes/assets/minecraft/textures/item/{tier}_pickaxe.png',f'armory/{fish}.png').read_bytes()
        source='iron' if tier=='copper' else tier
        assert z.read(f'assets/thedarkdimensions/textures/item/{warrior}.png')==artist(root.parent/f'Axes/assets/minecraft/textures/item/{source}_axe.png',f'armory/{warrior}.png').read_bytes()
        smith=json.loads(z.read(f'data/thedarkdimensions/recipe/{fish}.json'))
        assert smith['type']=='minecraft:smithing_transform' and smith['template']=='thedarkdimensions:improving_template'
        assert smith['addition']=='#thedarkdimensions:fish_ingredients'
        warrior_recipe=json.loads(z.read(f'data/thedarkdimensions/recipe/{warrior}.json'))
        if tier=='netherite':assert warrior_recipe['type']=='minecraft:smithing_transform' and warrior_recipe['base']=='minecraft:netherite_axe'
        else:assert warrior_recipe['pattern']==['MBM','MS ',' W ']
    hammer_source=next((root.parent/'Great Hammer').rglob('textures/item/trident.png'),root/'tools/texture_sources/armory/thundergods_hammer.png')
    for name in ['thundergods_hammer','awakened_thundergods_hammer']:
        assert z.read(f'assets/thedarkdimensions/textures/item/{name}.png')==hammer_source.read_bytes()
    assert Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/item/throwable_axe.png'))).size==(16,16)
    assert json.loads(z.read('data/thedarkdimensions/recipe/throwable_axe.json'))['pattern']==['II','IS']
    awakening=json.loads(z.read('data/thedarkdimensions/recipe/awakened_thundergods_hammer.json'))
    assert awakening['template']=='thedarkdimensions:lightning_core' and awakening['addition']=='thedarkdimensions:lightning_spell_book'
    assert 'at/janic/thedarkdimensions/BloodNetheriteContent.class' in files
    red_source=root.parent/'red-netherite/assets/minecraft/textures'
    for kind in ['sword','pickaxe','axe','shovel','hoe','warrior_axe','helmet','chestplate','leggings','boots','ingot']:
        name=f'red_netherite_{kind}'
        original=name if kind=='warrior_axe' else f'netherite_{kind}'
        assert z.read(f'assets/thedarkdimensions/textures/item/{name}.png')==artist(red_source/f'item/{original}.png',f'blood_netherite/{name}.png').read_bytes()
        assert f'assets/thedarkdimensions/items/{name}.json' in files
    equipment=json.loads(z.read('assets/thedarkdimensions/equipment/red_netherite.json'))
    for layer in ['humanoid','humanoid_leggings']:
        assert equipment['layers'][layer][0]['texture']==f'thedarkdimensions:red_netherite_{layer}'
        assert z.read(f'assets/thedarkdimensions/textures/entity/equipment/{layer}/red_netherite_{layer}.png')==artist(red_source/f'entity/equipment/{layer}/netherite.png',f'blood_netherite/red_netherite_{layer}.png').read_bytes()
    for name in ['chronus_infused_metalinium_ingot','red_netherite_upgrade_smithing_template']:
        tex=z.read(f'assets/thedarkdimensions/textures/item/{name}.png');im=Image.open(io.BytesIO(tex))
        assert im.size==(16,16) and im.mode=='RGBA' and im.getchannel('A').getextrema()[0]==0
    old='essentialium_infused_metalinium_ingot'
    assert json.loads(z.read('assets/thedarkdimensions/lang/en_us.json'))[f'item.thedarkdimensions.{old}']=='Chronus Infused Metalinium Ingot'
    assert json.loads(z.read(f'assets/thedarkdimensions/models/item/{old}.json'))['textures']['layer0']=='thedarkdimensions:item/chronus_infused_metalinium_ingot'
    assert 'tints' not in json.loads(z.read(f'assets/thedarkdimensions/items/{old}.json'))['model']
    duplication=json.loads(z.read('data/thedarkdimensions/recipe/red_netherite_upgrade_smithing_template.json'))
    assert duplication['result']['count']==2 and duplication['key']['N']=='minecraft:nether_wart'
    blood_ingot=json.loads(z.read('data/thedarkdimensions/recipe/red_netherite_ingot.json'))
    assert blood_ingot['ingredients'].count('thedarkdimensions:creeperblood')==4 and blood_ingot['ingredients'].count('minecraft:netherite_ingot')==2
    assert 'data/neoforge/loot_modifiers/global_loot_modifiers.json' not in files
    for kind in ['treasure','bridge','hoglin_stable','other']:
        modifier=json.loads(z.read(f'data/thedarkdimensions/loot_modifiers/blood_template_bastion_{kind}.json'))
        assert modifier['type']=='neoforge:add_table' and modifier['condition']['loot_table_id']==f'minecraft:chests/bastion_{kind}'
    for cls in ['DevilContent','DevilTridentItem','client/DevilTridentRenderer','client/DevilTridentModel','client/DevilTridentSpecialRenderer']:
        assert f'at/janic/thedarkdimensions/{cls}.class' in files
    mixins=json.loads(z.read('thedarkdimensions.mixins.json'))['mixins']
    assert 'DevilTridentMixin' in mixins and 'DevilTridentImpactMixin' in mixins
    for face in ['top','side']:
        for suffix in ['', '_e']:
            name=f'spelltable_{face}{suffix}'
            original=root.parent/f'Spell_table/assets/minecraft/textures/block/enchanting_table_{face}{suffix}.png'
            assert z.read(f'assets/thedarkdimensions/textures/block/{name}.png')==artist(original,f'spelltable/{name}.png').read_bytes()
    table_model=json.loads(z.read('assets/thedarkdimensions/models/block/spelltable.json'))
    assert table_model['render_type']=='minecraft:cutout' and table_model['elements'][1]['light_emission']==15
    assert not any('tintindex' in face for element in table_model['elements'] for face in element['faces'].values())
    for kind in ['item','entity']:
        original=root.parent/f'TheDevilsTrident/assets/minecraft/textures/{kind}/trident.png'
        assert z.read(f'assets/thedarkdimensions/textures/{kind}/devils_trident.png')==artist(original,f'devils_trident/trident_{kind}.png').read_bytes()
    devil=json.loads(z.read('assets/thedarkdimensions/items/devils_trident.json'))
    assert devil['model']['fallback']['on_true']['model']['type']=='thedarkdimensions:devils_trident'
    assert devil['model']['cases'][0]['model']['model']=='thedarkdimensions:item/devils_trident'
    devil_smith=json.loads(z.read('data/thedarkdimensions/recipe/devils_trident.json'))
    assert devil_smith['base']=='minecraft:trident' and devil_smith['template']=='thedarkdimensions:devils_tear' and devil_smith['addition']=='thedarkdimensions:devils_eye'
    assert 'assets/thedarkdimensions/textures/mob_effect/devils_charm.png' in files
    assert 'at/janic/thedarkdimensions/DevilArmor.class' in files
    for name,center in [('devils_tear','ghast_tear'),('devils_eye','ender_eye')]:
        material=json.loads(z.read(f'data/thedarkdimensions/recipe/{name}.json'))
        assert material['pattern']==['BBB','BCB','BBB'] and material['key']['B']=='thedarkdimensions:creeperblood' and material['key']['C']==f'minecraft:{center}'
        im=Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{name}.png')))
        assert im.size==(16,16) and im.mode=='RGBA' and im.getchannel('A').getextrema()[0]==0
    enchantment=json.loads(z.read('data/thedarkdimensions/enchantment/devils_armor.json'))
    assert enchantment['max_level']==1 and enchantment['supported_items']=='#thedarkdimensions:devils_armor_enchantable'
    assert 'DevilArmorEntityMixin' in mixins and 'DevilArmorMovementMixin' in mixins and 'DevilLavaRiptideMixin' in mixins
    assert 'DevilArmorClientMixin' in json.loads(z.read('thedarkdimensions.mixins.json'))['client']
    for effect in ['frosted','blood','soulless']:
        assert f'assets/thedarkdimensions/textures/mob_effect/{effect}.png' in files
    supplied=root.parent/'Katanas/assets/minecraft/optifine/cit'
    if not supplied.exists():supplied=root/'tools/texture_sources/katanas'
    for name,old in [('frostgleam','algidgleam'),('bloodgrief','bloodygrief'),('souleater','faintquasar')]:
        assert z.read(f'assets/thedarkdimensions/textures/item/{name}.png')==(supplied/f'{old}.png').read_bytes()
        assert z.read(f'assets/thedarkdimensions/textures/item/{name}.png.mcmeta')==(supplied/f'{old}.png.mcmeta').read_bytes()
        model=json.loads(z.read(f'assets/thedarkdimensions/models/item/{name}.json'))
        original=json.loads((supplied/f'{old}.json').read_text())
        assert model['elements']==original['elements'] and model['display']==original['display']
        assert all(value==f'thedarkdimensions:item/{name}' for value in model['textures'].values())
        recipe=json.loads(z.read(f'data/thedarkdimensions/recipe/{name}.json'))
        assert recipe['pattern']==(['  E',' I ','S  '] if name=='frostgleam' else ['  E',' E ','S  ']) and recipe['key']['S']=='thedarkdimensions:basic_katana'
        if name=='frostgleam':
            assert recipe['key']['E']=='thedarkdimensions:japanese_essence' and recipe['key']['I']=='minecraft:blue_ice'
    for lang in ['de_de','en_us']:
        labels=json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
        assert labels['item.thedarkdimensions.spell_core']=='Elemental Core'
        assert 'Elemental Core' in labels['guide.thedarkdimensions.spell_core']
    blood=json.loads(z.read('data/thedarkdimensions/recipe/blood_essence.json'))
    assert blood['ingredients']==['#thedarkdimensions:essence_blood','minecraft:gold_ingot']
    assert 'data/thedarkdimensions/recipe/improving_template.json' not in files
    assert 'data/thedarkdimensions/advancement/recipes/improving_template.json' not in files
    assert 'thedarkdimensions:improving_template' not in z.read('data/thedarkdimensions/loot_table/chests/elemental_shrine.json').decode()
    duplicate=json.loads(z.read('data/thedarkdimensions/recipe/duplicate_improving_template.json'))
    assert duplicate['result']['count']==2 and duplicate['key']['T']=='thedarkdimensions:improving_template'
    burning=json.loads(z.read('data/thedarkdimensions/recipe/burned_stick.json'))
    assert burning['type']=='thedarkdimensions:burning_stick' and burning['ingredients']==['minecraft:flint_and_steel','minecraft:stick']
    red=json.loads(z.read('data/thedarkdimensions/recipe/red_netherite_ingot.json'))
    assert red['ingredients'].count('minecraft:netherite_ingot')==2 and red['ingredients'].count('thedarkdimensions:creeperblood')==4
    for chest,chance in [('nether_bridge',.2),('bastion_treasure',.35),('bastion_bridge',.2),('bastion_hoglin_stable',.2),('bastion_other',.2)]:
        name=f'improving_template_{chest}'
        modifier=json.loads(z.read(f'data/thedarkdimensions/loot_modifiers/{name}.json'))
        assert modifier['condition']['loot_table_id']==f'minecraft:chests/{chest}'
        loot=json.loads(z.read(f'data/thedarkdimensions/loot_table/chests/{name}.json'))['pools'][0]
        assert loot['condition']['chance']==chance and loot['entries'][0]['name']=='thedarkdimensions:improving_template'
    new_lights=['glowstone_vein_stone','glowstone_crystal_block','pale_glowstone']
    for name in new_lights:
        assert f'data/thedarkdimensions/loot_table/blocks/{name}.json' in files
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/block/{name}.png'))).size==(16,16)
    assert 'data/thedarkdimensions/worldgen/placed_feature/surface_crystal_rocks.json' in files
    for name in ['ash_wastes','charred_forest','crystal_hollows','echo_fields']:
        biome=json.loads(z.read(f'data/thedarkdimensions/worldgen/biome/{name}.json'))
        assert biome['features'][2]==['thedarkdimensions:surface_crystal_rocks']
    for name in ['fire_core','water_core','lightning_core','spell_core','spell_book','fire_spell_book','water_spell_book','lightning_spell_book','ender_chest_spell_book']:
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{name}.png'))).size==((32,32) if name in supplied_mapping else (16,16))
        model=json.loads(z.read(f'assets/thedarkdimensions/models/item/{name}.json'))
        assert model['textures']['layer0']==f'thedarkdimensions:item/{name}'
        item_model=json.loads(z.read(f'assets/thedarkdimensions/items/{name}.json'))
        assert 'tints' not in item_model['model']
    palette=json.loads(z.read('assets/thedarkdimensions/clean_block_palette.json'))
    for name in palette:
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/block/{name}.png'))).size==(16,16)
    motion=json.loads(z.read('data/minecraft/tags/block/blocks_motion_no_leaves.json'))['values']
    assert 'thedarkdimensions:hard_stone' in motion and 'thedarkdimensions:burned_stone' not in motion and 'thedarkdimensions:burned_deepslate' not in motion
    for old in ['burned_coal_ore','burned_iron_ore','improved_amethyst_crystal_ore','ember_stone']:
        assert f'data/thedarkdimensions/worldgen/placed_feature/{old}.json' not in files
    for n in files:
        if n.startswith('data/thedarkdimensions/worldgen/biome/') and not n.endswith('black_desert.json'):
            biome=json.loads(z.read(n))
            assert 'thedarkdimensions:chronus_orb_ore' in biome['features'][6] and any(n.startswith('thedarkdimensions:realm_ore_') for n in biome['features'][6])
            assert biome['features'][7]==['thedarkdimensions:essentialium_geodes']
    for texture in ['assets/thedarkdimensions/textures/item/chronus_orb.png','assets/thedarkdimensions/textures/block/chronus_orb_ore.png']:
        assert Image.open(io.BytesIO(z.read(texture))).size==(16,16)
    for kind in ['fire','water','glowing']:
        biome=json.loads(z.read(f'data/thedarkdimensions/worldgen/biome/{kind}_cave.json'))
        assert biome['carvers']==['minecraft:cave','minecraft:cave_extra_underground','minecraft:canyon']
        model=json.loads(z.read(f'assets/thedarkdimensions/models/block/{kind}_crystal.json'))
        assert model['parent']=='minecraft:block/tinted_cross'
        assert model['textures']['cross']=='minecraft:block/amethyst_cluster'
        variants=json.loads(z.read(f'assets/thedarkdimensions/blockstates/{kind}_crystal.json'))['variants']
        assert len(variants)==6
    for kind in ['pickaxe','shovel']:
        source=artist(root.parent/'TheDarkDimensionsResouces'/f'improved_diamond_{kind}.png',f'items/enhanced_diamond_{kind}.png')
        assert z.read(f'assets/thedarkdimensions/textures/item/enhanced_diamond_{kind}.png')==source.read_bytes()
    # Check every namespaced model texture reference resolves in the release.
    for n in files:
        if '/models/' not in n or not n.endswith('.json'):continue
        for value in json.loads(z.read(n)).get('textures',{}).values():
            if value.startswith('thedarkdimensions:'):
                path='assets/thedarkdimensions/textures/'+value.split(':',1)[1]+'.png'
                assert path in files,(n,path)
print(f'Validated {jar.name}: JSON/PNG, model textures, Solar progression and animated Stormveil, Blood armor and loot, no test fixtures; supplied tool and armor textures are byte-identical to originals or their archives.')

# Pocket expansion resources are included, without touching original key art.
with zipfile.ZipFile(jar) as z:
    prefix='assets/thedarkdimensions/textures/item/'
    original=Image.open(io.BytesIO(z.read(prefix+'pocket_dimension_key.png'))).convert('RGBA')
    for name in ['foreign_key','improved_pocket_dimension_key']:
        image=Image.open(io.BytesIO(z.read(prefix+name+'.png'))).convert('RGBA')
        assert image.size==original.size
        assert image.getchannel('A').tobytes()==original.getchannel('A').tobytes()
        assert image.tobytes()!=original.tobytes()
    for name in ['pocket_key_upgrade_template','weathered_papyrus']:
        assert Image.open(io.BytesIO(z.read(prefix+name+'.png'))).size==(16,16)
    recipe=json.loads(z.read('data/thedarkdimensions/recipe/improved_pocket_dimension_key.json'))
    assert recipe['addition']=='minecraft:netherite_block' and recipe['base']=='thedarkdimensions:pocket_dimension_key'
    normal=json.loads(z.read('data/thedarkdimensions/dimension/pocket_dimension.json'))
    large=json.loads(z.read('data/thedarkdimensions/dimension/large_pocket_dimension.json'))
    assert normal['type']!=large['type'] and large['generator']['settings']['layers']==normal['generator']['settings']['layers']
    ruin=json.loads(z.read('data/thedarkdimensions/worldgen/structure/mummy_ruins.json'))
    assert ruin['size']==1 and ruin['start_height']['absolute']==-5
    assert 'data/thedarkdimensions/structure/mummy_ruins.nbt' in z.namelist()
    assert json.loads(z.read('data/thedarkdimensions/tags/worldgen/biome/has_mummy_ruins.json'))['values']==['minecraft:desert']
    loot=json.loads(z.read('data/thedarkdimensions/loot_table/chests/mummy_ruins.json'))
    assert loot['pools'][-1]['condition']['chance']==0.03 and loot['pools'][-2]['condition']['chance']==0.05
print('Validated Mummy ruin placement/loot, separate upgraded dimension, key silhouettes/alpha and native recipes.')

# Elemental magic is self-contained; Curios is optional, never bundled.
with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    assert not any('/test/' in n or n.startswith('top/theillusivec4/') for n in names)
    for name in ['chronus_spell_book','blood_spell_book','soul_spell_book','elemental_spell_book','elemental_master_spellbook','chronus_core','ender_core']:
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{name}.png'))).size==(16,16)
        assert f'assets/thedarkdimensions/items/{name}.json' in names
    master=json.loads(z.read('data/thedarkdimensions/recipe/elemental_master_spellbook.json'))
    assert master['pattern']==['ECE','CBC','MCM']
    slot=json.loads(z.read('data/thedarkdimensions/curios/slots/spellbook.json'))
    assert slot['size']==1 and slot['entities']==['minecraft:player']
    tag=json.loads(z.read('data/curios/tags/item/spellbook.json'))
    assert len(tag['values'])==9 and 'thedarkdimensions:spell_book' not in tag['values']
    assert 'curios' in z.read('META-INF/neoforge.mods.toml').decode('utf-8')
print('Validated elemental magic recipes, seven new 16x16 sprites, nine Curios book entries and absence of bundled optional mods/tests.')

with zipfile.ZipFile(jar) as z:
    for item in ['luminous_coating','astral_prism','sakura_talisman','ronin_bandage','windstep_sandals']:
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{item}.png'))).size==(16,16)
        assert f'assets/thedarkdimensions/items/{item}.json' in z.namelist()
    assert json.loads(z.read('data/thedarkdimensions/recipe/apply_luminous_coating.json'))['type']=='thedarkdimensions:apply_luminous_coating'
    assert json.loads(z.read('data/thedarkdimensions/recipe/ronin_bandage.json'))['result']['count']==4
    assert json.loads(z.read('data/thedarkdimensions/recipe/windstep_sandals.json'))['key']['S']=='thedarkdimensions:reinforced_stick'
    for slot in ['focus','talisman']:
        assert json.loads(z.read(f'data/thedarkdimensions/curios/slots/{slot}.json'))['size']==1
print('Validated permanent coating recipe, five 16x16 icons, Japanese Essence recipes and two accessory slots.')

with zipfile.ZipFile(jar) as z:
    assert not any(name.startswith('eu/pb4/trinkets/') for name in z.namelist())
    assert 'trinkets_updated' in z.read('META-INF/neoforge.mods.toml').decode('utf-8')
    entity=json.loads(z.read('data/trinkets/entities/thedarkdimensions_player.json'))
    assert entity['entities']==['minecraft:player'] and len(entity['slots'])==6
    for slot,count in [('dark_spellbook',9),('dark_focus',2),('dark_talisman',1)]:
        definition=json.loads(z.read(f'data/trinkets/slots/chest/{slot}.json'))
        assert definition['amount']==1 and definition['validator_predicates']==['trinkets:tag'] and not definition['cosmetic_slots']
        assert len(json.loads(z.read(f'data/trinkets/tags/item/chest/{slot}.json'))['values'])==count
print('Validated optional Trinkets Updated integration, six dedicated slots and absence of embedded Trinkets classes.')

with zipfile.ZipFile(jar) as z:
    for name in ['SpellEffects','KatanaFinisherAnimation','client/SpellIcons','client/SpellbookScreen','mixin/KatanaAnimationResourcesMixin']:
        assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    mixins=json.loads(z.read('thedarkdimensions.mixins.json'))
    assert 'KatanaAnimationResourcesMixin' in mixins['client']
    for item in ['frostgleam','bloodgrief','souleater','stormveil']:
        attacks=json.loads(z.read(f'data/thedarkdimensions/weapon_attributes/{item}.json'))['attributes']['attacks']
        assert len(attacks)==3 and attacks[2]['hitbox']=='HORIZONTAL_PLANE' and attacks[2]['upswing']>attacks[0]['upswing']
        assert attacks[2]['animation']=='thedarkdimensions:katana_finisher'
print('Validated native spell tree, server spell effects and horizontal katana finisher integration.')

with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    for removed in ['burned_hoglin','burned_pigman']:
        assert not any(removed in name for name in names)
        for name in names:
            if name.endswith('.json'):
                assert f'thedarkdimensions:{removed}' not in z.read(name).decode('utf-8'),name
    for original,target in [('enderman.png','hollow_watcher.png'),('enderman_eyes.png','hollow_watcher_eyes.png')]:
        art=artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!'/'The new mob/textures/entity/enderman'/original,'veilwood/'+original)
        assert z.read('assets/thedarkdimensions/textures/entity/'+target)==art.read_bytes()
    assert Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/mob_effect/devils_charm.png'))).size==(18,18)
    source=json.loads(z.read('data/thedarkdimensions/dimension/burned_realm.json'))['generator']['biome_source']
    assert source['type']=='thedarkdimensions:veilwood' and source['delegate']['type']=='minecraft:multi_noise'
    biome=json.loads(z.read('data/thedarkdimensions/worldgen/biome/veilwood.json'))
    assert biome['attributes']['minecraft:visual/fog_end_distance']==24
    assert biome['attributes']['minecraft:visual/sky_light_factor']==.06
    assert 'thedarkdimensions:veilwood_trees' in biome['features'][9]
    for cls in ['HollowWatcher','VeilwoodContent','KatanaFinisherEffects','worldgen/VeilwoodBiomeSource','client/HollowWatcherRenderer']:
        assert f'at/janic/thedarkdimensions/{cls}.class' in names
    assert 'at/janic/thedarkdimensions/BurnedHoglin.class' not in names
    assert 'at/janic/thedarkdimensions/BurnedPigman.class' not in names
print('Validated Veilwood fog and biome source, original Watcher textures, native emerald icon and full removal of burned mobs.')

with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    assert not any('/modeltest/' in name or '/test/' in name for name in names)
    sources=json.loads(z.read('assets/thedarkdimensions/mob_texture_sources.json'))['textures']
    assert len(sources)==10
    for mob,source in sources.items():
        supplied=artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!/Other Mob textures/minecraft/textures/entity'/source,'mob_expansion/'+source)
        actual=z.read(f'assets/thedarkdimensions/textures/entity/{mob}.png')
        assert actual==supplied.read_bytes(),mob
        if mob!='fire_mage':
            assert f'assets/thedarkdimensions/items/{mob}_spawn_egg.json' in names
            assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{mob}_spawn_egg.png'))).size==(16,16)
            assert f'data/thedarkdimensions/loot_table/entities/{mob}.json' in names
    for mob in ['ashen_wraith','bonewing','veilwood_spider','drowned_sentinel','cinder_broodmother','lantern_wisp','briar_whisperer','dune_stalker']:
        model=json.loads(z.read(f'assets/thedarkdimensions/models/entity/{mob}.json'))
        assert model['texture']==f'thedarkdimensions:textures/entity/{mob}.png'
        assert tuple(model['textureSize'])==Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/entity/{mob}.png'))).size
        assert model.get('credit') and model['models']
    for name,biomes in [('add_dune_stalkers',['minecraft:desert']),('add_briar_whisperers',['minecraft:jungle','minecraft:sparse_jungle','minecraft:bamboo_jungle','minecraft:swamp','minecraft:mangrove_swamp'])]:
        modifier=json.loads(z.read(f'data/thedarkdimensions/neoforge/biome_modifier/{name}.json'))
        assert modifier['biomes']==biomes
    assert 'at/janic/thedarkdimensions/client/PackMobModel.class' in names
    assert 'at/janic/thedarkdimensions/client/FireMageRenderer.class' in names
print('Validated ten byte-exact pack skins, eight credited native model imports, nine spawn eggs/loot tables and Overworld biome scopes; no model-test fixtures bundled.')

with zipfile.ZipFile(jar) as z:
    for item in ['chronus_fragment','timeworn_hourglass','ashbound_bestiary','sanctum_keeper_spawn_egg']:
        assert Image.open(io.BytesIO(z.read(f'assets/thedarkdimensions/textures/item/{item}.png'))).size==(16,16)
        assert f'assets/thedarkdimensions/items/{item}.json' in z.namelist()
    for structure in ['chronus_sanctum','sunken_archive']:
        assert len(z.read(f'data/thedarkdimensions/structure/{structure}.nbt'))>100
        config=json.loads(z.read(f'data/thedarkdimensions/worldgen/structure/{structure}.json'))
        assert config['type']=='minecraft:jigsaw' and config['biomes']==f'#thedarkdimensions:has_{structure}'
        assert f'data/thedarkdimensions/worldgen/structure_set/{structure}.json' in z.namelist()
        assert f'data/thedarkdimensions/worldgen/template_pool/{structure}.json' in z.namelist()
        assert json.loads(z.read(f'data/thedarkdimensions/loot_table/chests/{structure}.json'))['pools']
    advances=[n for n in z.namelist() if n.startswith('data/thedarkdimensions/advancement/exploration/') and n.endswith('.json')]
    assert len(advances)==12
    for lang in ['en_us','de_de']:
        strings=json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
        for path in advances:
            display=json.loads(z.read(path))['display']
            assert display['title']['translate'] in strings and display['description']['translate'] in strings
    focus=json.loads(z.read('data/curios/tags/item/focus.json'))['values']
    assert set(focus)=={'thedarkdimensions:astral_prism','thedarkdimensions:timeworn_hourglass'}
    assert 'at/janic/thedarkdimensions/Bestiary.class' in z.namelist()
print('Validated exploration structures, four 16x16 icons, twelve localized advancements and both optional focus-slot integrations.')

with zipfile.ZipFile(jar) as z:
    for name in ['katana_parry','katana_dodge_forward','katana_dodge_back','katana_dodge_left','katana_dodge_right']:
        animation=json.loads(z.read(f'assets/thedarkdimensions/player_animations/{name}.json'))
        assert animation['name']==name and animation['emote']['stopTick']==10
        assert animation['emote']['isLoop']=='false' and len(animation['emote']['moves'])==4
    heavy=json.loads(z.read('data/thedarkdimensions/tags/entity_type/katana_unparryable.json'))
    assert set(heavy['values'])=={'minecraft:ravager','minecraft:iron_golem','minecraft:warden','thedarkdimensions:cinder_broodmother'}
    for lang in ['en_us','de_de']:
        data=json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
        for notice in ['parry','perfect_parry','dodge','perfect_dodge','counter','guard','unparryable']:assert f'combat.thedarkdimensions.{notice}' in data
        assert 'key.thedarkdimensions.katana_dodge' in data and 'key.category.thedarkdimensions.combat' in data
    for name in ['KatanaDefense','DefenseNetwork','client/DefenseClient','client/DefensePlayerAnimations']:assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    assert not any(name.startswith('com/zigythebird/') for name in z.namelist())
print('Validated server katana defense, five short optional animations, heavy-enemy tag and localized controls/feedback; PAL is not bundled.')

with zipfile.ZipFile(jar) as z:
    assert 'KatanaCounterAttemptMixin' in json.loads(z.read('thedarkdimensions.mixins.json'))['mixins']
    assert 'at/janic/thedarkdimensions/mixin/KatanaCounterAttemptMixin.class' in z.namelist()
    unlock=json.loads(z.read('data/thedarkdimensions/advancement/recipes/astral_crystal_wand.json'))
    assert unlock['criteria']['has_ingredient']['conditions']['items'][0]['items']=='thedarkdimensions:fire_core'
    for mob,source in [('bonewing','phantom/phantom6.jem'),('lantern_wisp','phantom/phantom5.jem'),('veilwood_spider','cave_spider/cave_spider2.jem'),('dune_stalker','spider/spider2.jem'),('cinder_broodmother','spider/spider6.jem'),('ashen_wraith','blaze/blaze2.jem'),('drowned_sentinel','skeleton/skeleton4.jem'),('briar_whisperer','evoker/evoker10.jem')]:
        original=json.loads((root/'tools/texture_sources/mob_expansion/models'/source).read_text(encoding='utf-8'))
        actual=json.loads(z.read(f'assets/thedarkdimensions/models/entity/{mob}.json'))
        def geometry(node):
            return {k:([geometry(c) for c in v] if k=='submodels' else geometry(v) if k=='submodel' else v) for k,v in node.items() if k!='animations'}
        assert actual['models']==[geometry(n) for n in original['models']],mob
        visibility_targets=set()
        def collect_visibility(node):
            for animation in node.get('animations',[]):
                visibility_targets.update(k.removesuffix('.visible') for k in animation if k.endswith('.visible') and not k.startswith('health_bar.'))
            for child in node.get('submodels',[]):collect_visibility(child)
            if 'submodel' in node:collect_visibility(node['submodel'])
        for node in original['models']:collect_visibility(node)
        assert set(actual['visibilityRules'])==visibility_targets,mob
        if mob=='dune_stalker':assert actual['visibilityRules']=={'head_aggro':'aggressive','head_chill':'calm'}
        if mob=='veilwood_spider':assert 'head_aggro_rotation' not in actual['visibilityRules'] and len(actual['visibilityRules'])==18
print('Validated one-attempt counter hook, pre-End portal-wand progression and all eight original CEM geometries, pivots and UV rectangles.')

with zipfile.ZipFile(jar) as z:
    expected={
      'empty_runestone':([' S ','SDS',' S '],{'S':'minecraft:stone','D':'minecraft:diamond'}),
      'fire_core':(['BBB','BRB','BBB'],{'B':'minecraft:blaze_powder','R':'thedarkdimensions:empty_runestone'}),
      'water_core':([' W ','WRW',' W '],{'W':'minecraft:water_bucket','R':'thedarkdimensions:empty_runestone'}),
      'lightning_core':([' L ','LRL',' L '],{'L':'#thedarkdimensions:core_lightning_rods','R':'thedarkdimensions:empty_runestone'})
    }
    for name,(pattern,key) in expected.items():
        data=json.loads(z.read(f'data/thedarkdimensions/recipe/{name}.json'))
        assert data['pattern']==pattern and data['key']==key and data['result']=={'id':f'thedarkdimensions:{name}'}
    rods=json.loads(z.read('data/thedarkdimensions/tags/item/core_lightning_rods.json'))['values']
    assert set(rods)=={f'minecraft:{wax}{stage}lightning_rod' for wax in ['', 'waxed_'] for stage in ['', 'exposed_', 'weathered_', 'oxidized_']}
    assert 'assets/thedarkdimensions/items/empty_runestone.json' in z.namelist()
    model=json.loads(z.read('assets/thedarkdimensions/models/item/empty_runestone.json'))
    assert model['textures']['stone']=='minecraft:block/stone' and len(model['elements'])==3
    for lang in ['de_de','en_us']:
        data=json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
        assert 'item.thedarkdimensions.empty_runestone' in data
        for name in expected:assert f'guide.thedarkdimensions.{name}' in data
print('Validated Runestone tablet, all three core recipes and all eight Lightning Rod stages including waxed variants.')

with zipfile.ZipFile(jar) as z:
    original=artist(root.parent/'TheDarkDimensions NutzeDieseResourcen!/Other Mob textures/minecraft/textures/entity/illager/vindicator.png','katana_boss/vindicator.png')
    skin=z.read('assets/thedarkdimensions/textures/entity/ashen_sovereign.png')
    assert skin==original.read_bytes() and Image.open(io.BytesIO(skin)).size==(64,64)
    for name in ['AshenSovereign','KatanaBossContent','client/AshenSovereignRenderer']:
        assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    egg=json.loads(z.read('assets/thedarkdimensions/items/ashen_sovereign_spawn_egg.json'))
    assert egg['model']['model']=='minecraft:item/vindicator_spawn_egg'
    dojo=json.loads(z.read('data/thedarkdimensions/worldgen/structure/ashen_dojo.json'))
    assert dojo['type']=='minecraft:jigsaw' and dojo['biomes']=='#thedarkdimensions:has_ashen_dojo'
    biomes=json.loads(z.read('data/thedarkdimensions/tags/worldgen/biome/has_ashen_dojo.json'))['values']
    assert set(biomes)=={'thedarkdimensions:ash_wastes','thedarkdimensions:charred_forest','thedarkdimensions:veilwood'}
    for kind in ['structure_set','template_pool']:
        assert f'data/thedarkdimensions/worldgen/{kind}/ashen_dojo.json' in z.namelist()
    assert len(z.read('data/thedarkdimensions/structure/ashen_dojo.nbt'))>100
    loot=json.loads(z.read('data/thedarkdimensions/loot_table/entities/ashen_sovereign.json'))
    assert [pool['entries'][0]['name'] for pool in loot['pools']]==['thedarkdimensions:souleater','thedarkdimensions:soul_core','thedarkdimensions:ashes']
    assert json.loads(z.read('data/minecraft/tags/damage_type/bypasses_shield.json'))=={'replace':False,'values':['thedarkdimensions:regent_wave','thedarkdimensions:sovereign_heavy']}
    assert 'data/thedarkdimensions/damage_type/sovereign_heavy.json' in z.namelist()
    for lang in ['de_de','en_us']:
        strings=json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
        for key in ['entity','bestiary']:
            assert f'{key}.thedarkdimensions.ashen_sovereign'+('.behavior' if key=='bestiary' else '') in strings
print('Validated original 64x64 Sovereign skin, native renderer, locate-compatible Dojo, biome scope, heavy damage type and boss loot.')

with zipfile.ZipFile(jar) as z:
    ids={'thedarkdimensions:'+n for n in ['frostgleam','bloodgrief','souleater','stormveil','basic_katana','mindshifter','hellsblade']}
    assert set(json.loads(z.read('data/thedarkdimensions/tags/item/katana_enchantable.json'))['values'])==ids
    slash=json.loads(z.read('data/thedarkdimensions/enchantment/slashing.json'))
    master=json.loads(z.read('data/thedarkdimensions/enchantment/master_parrying.json'))
    assert slash['max_level']==3 and slash['effects']['minecraft:damage'][0]['effect']=={'type':'minecraft:add','value':{'type':'minecraft:linear','base':1.5,'per_level_above_first':.5}}
    assert master['max_level']==1 and master['weight']==1 and master['min_cost']['base']==30
    for data in [slash,master]:assert data['supported_items']=='#thedarkdimensions:katana_enchantable' and data['slots']==['mainhand']
    assert 'thedarkdimensions:master_parrying' in json.loads(z.read('data/minecraft/tags/enchantment/in_enchanting_table.json'))['values']
    for tag in ['tradeable','on_random_loot','non_treasure']:assert 'thedarkdimensions:master_parrying' not in json.loads(z.read(f'data/minecraft/tags/enchantment/{tag}.json'))['values']
    assert 'KatanaEnchantingTableMixin' in json.loads(z.read('thedarkdimensions.mixins.json'))['mixins']
    assert 'at/janic/thedarkdimensions/KatanaEnchantments.class' in z.namelist()
    gray=Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/item/reinforced_stick.png'))).convert('RGBA')
    old=Image.open(root/'tools/texture_sources/items/improved_stick.png').convert('RGBA')
    assert gray.size==old.size==(16,16) and gray.getchannel('A').tobytes()==old.getchannel('A').tobytes()
    assert all(r==g==b for r,g,b,a in gray.getdata())
    assert z.read('assets/thedarkdimensions/textures/gui/sprites/container/slot/improved_stick.png')==z.read('assets/thedarkdimensions/textures/item/reinforced_stick.png')
    assert z.read('assets/thedarkdimensions/textures/item/improved_stick.png')==(root/'tools/texture_sources/items/improved_stick.png').read_bytes()
    assert json.loads(z.read('assets/thedarkdimensions/models/item/reinforced_stick.json'))['textures']['layer0']=='thedarkdimensions:item/reinforced_stick'
print('Validated katana-only Slashing I–III, rare table-only Master Parrying, native level-30 gate and 16x16 zero-saturation Improved Stick with preserved Burned Stick art.')

with zipfile.ZipFile(jar) as z:
    warrior=Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/item/warrior_stick.png'))).convert('RGBA')
    assert warrior.size==(16,16) and warrior.getextrema()[3]==(0,255)
    reds={(54,22,21),(86,33,33),(104,40,42)}
    axe=Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/item/iron_warrior_axe.png'))).convert('RGBA')
    assert reds.issubset({(r,g,b) for r,g,b,a in axe.getdata() if a})
    assert any((r,g,b) in reds and a for r,g,b,a in warrior.getdata())
    assert any(r==g==b and a for r,g,b,a in warrior.getdata())
    assert z.read('assets/thedarkdimensions/textures/item/warrior_stick.png')==(root/'tools/texture_sources/sticks/warrior_stick.png').read_bytes()
    assert json.loads(z.read('assets/thedarkdimensions/models/item/warrior_stick.json'))['textures']['layer0']=='thedarkdimensions:item/warrior_stick'
    assert 'tints' not in json.loads(z.read('assets/thedarkdimensions/items/warrior_stick.json'))['model']
print('Validated 16x16 Warrior Stick with neutral gray shaft, red grip from original axe palette and no whole-item tint.')

with zipfile.ZipFile(jar) as z:
    def recipe_data(name):return json.loads(z.read(f'data/thedarkdimensions/recipe/{name}.json'))
    assert set(recipe_data('stormveil')['ingredients'])=={'thedarkdimensions:lightning_core','thedarkdimensions:solar_essence','thedarkdimensions:basic_katana'}
    assert set(recipe_data('spell_book')['ingredients'])=={'minecraft:book','thedarkdimensions:empty_runestone','thedarkdimensions:metalinium_ingot'}
    hammer=recipe_data('thundergods_hammer')
    assert hammer['pattern']==['DBD','LSL',' S '] and hammer['key']['B']=='minecraft:diamond_block' and hammer['key']['D']=='minecraft:diamond'
    master=recipe_data('elemental_master_spellbook')
    assert master['key']['E']=='thedarkdimensions:essentialium' and master['key']['M']=='thedarkdimensions:essentialium_infused_metalinium_ingot' and master['key']['C']=='thedarkdimensions:spell_core'
    for tier in ['wooden','stone','copper','iron','golden','diamond','netherite']:
        conversion=recipe_data(f'convert_{tier}_warrior_axe' if tier!='netherite' else 'netherite_warrior_axe')
        assert conversion['type']=='minecraft:smithing_transform' and conversion['template']=='thedarkdimensions:warrior_axe_upgrade_template' and conversion['base']==f'minecraft:{tier}_axe' and conversion['addition']=='thedarkdimensions:warrior_stick' and conversion['result']['id']==f'thedarkdimensions:{tier}_warrior_axe'
    upgrade=recipe_data('upgrade_netherite_warrior_axe')
    assert upgrade['base']=='thedarkdimensions:diamond_warrior_axe' and upgrade['template']=='minecraft:netherite_upgrade_smithing_template' and upgrade['addition']=='minecraft:netherite_ingot'
    assert recipe_data('warrior_axe_upgrade_template')['pattern']==['IBI','LSL','IBI']
    assert set(json.loads(z.read('data/thedarkdimensions/tags/item/essence_blood.json'))['values'])=={'thedarkdimensions:creeperblood','thedarkdimensions:zombieblood'}
    bulk=recipe_data('blood_essence_bulk')
    assert bulk['ingredients'].count('#thedarkdimensions:essence_blood')==2 and bulk['ingredients'].count('minecraft:gold_ingot')==2 and bulk['result']=={'id':'thedarkdimensions:blood_essence','count':2}
    for name in ['warrior_axe_upgrade_template','zombieblood']:
        for lang in ['de_de','en_us']:assert f'item.thedarkdimensions.{name}' in json.loads(z.read(f'assets/thedarkdimensions/lang/{lang}.json'))
    icon=Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/item/warrior_axe_upgrade_template.png'))).convert('RGBA')
    assert icon.size==(16,16) and icon.getextrema()[3][0]==0 and icon.getextrema()[3][1]>=250
    assert z.read('assets/thedarkdimensions/textures/item/warrior_axe_upgrade_template.png')==(root/'tools/texture_sources/progression_rebalance/warrior_axe_upgrade_template.png').read_bytes()
    assert json.loads(z.read('assets/thedarkdimensions/items/zombieblood.json'))['model']['tints'][0]['type']=='minecraft:constant'
print('Validated Warrior conversion and Netherite progression, blank/master spellbook recipes, diamond hammer costs, three-ingredient Stormveil, mixed-blood essence recipes and new 16x16 template art.')

with zipfile.ZipFile(jar) as z:
    def data(path):return json.loads(z.read(path))
    def recipe_data(name):return data(f'data/thedarkdimensions/recipe/{name}.json')
    bench=recipe_data('runic_binding_bench')
    assert bench['pattern']==['ERE','MSM','HHH'] and bench['key']=={'E':'thedarkdimensions:essentialium','R':'thedarkdimensions:empty_runestone','M':'thedarkdimensions:metalinium_ingot','S':'thedarkdimensions:spelltable','H':'thedarkdimensions:hard_stone'}
    ingredients={'ember':['empty_runestone','fire_core','soul_essence'],'tide':['empty_runestone','water_core','japanese_essence'],'radiance':['empty_runestone','glowing_honey','essentialium'],'chronus':['empty_runestone','chronus_orb','essentialium_infused_metalinium_ingot']}
    for kind,names in ingredients.items():
        assert recipe_data(kind+'_rune')['ingredients']==['thedarkdimensions:'+name for name in names]
        advancement='complete_all' if kind=='chronus' else 'complete_'+kind
        assert data(f'data/thedarkdimensions/advancement/resonance/{advancement}.json')['rewards']['recipes']==['thedarkdimensions:'+kind+'_rune']
        assert '/models/item/' not in data(f'assets/thedarkdimensions/items/{kind}_rune.json')['model']['model']
    template_bytes=[]
    for name,kind,biome in [('cinder_crucible','ember','fire_cave'),('sunken_reliquary','tide','water_cave'),('luminous_hollow','radiance','glowing_cave')]:
        structure=data(f'data/thedarkdimensions/worldgen/structure/{name}.json')
        assert structure['type']=='thedarkdimensions:cave_trial' and structure['kind']==kind and structure['biomes']==['thedarkdimensions:'+biome]
        assert len(data(f'data/thedarkdimensions/worldgen/template_pool/{name}.json')['elements'])==3
        assert data(f'data/thedarkdimensions/worldgen/structure_set/{name}.json')['placement']['spacing']==24
        for variant in range(3):template_bytes.append(z.read(f'data/thedarkdimensions/structure/{name}_{variant}.nbt'))
    assert len(set(template_bytes))==9 and all(len(b)>1000 for b in template_bytes)
    assert len(data('assets/thedarkdimensions/blockstates/trial_seal.json')['variants'])==24
    for model in ['runic_binding_bench','dormant_runestone_ember','dormant_runestone_tide','dormant_runestone_radiance']:
        block=data(f'assets/thedarkdimensions/models/block/{model}.json');assert block['elements']
    for cls in ['TrialController','TrialLedger','ResonanceProgress','AncientRunes','RunicBenchMenu','FieldJournal','CaveTrialStructure']:
        assert f'at/janic/thedarkdimensions/resonance/{cls}.class' in z.namelist()
    mixins=data('thedarkdimensions.mixins.json')['mixins']
    for name in ['CooldownsAccess','CooldownInstanceAccess','RuneCraftingGateMixin']:assert name in mixins
    for lang in ['de_de','en_us']:
        strings=data(f'assets/thedarkdimensions/lang/{lang}.json')
        for kind in ingredients:assert f'perk.thedarkdimensions.rune_{kind}' in strings
        for name in ['cinder_crucible','sunken_reliquary','luminous_hollow','activation_confirm','journal_waypoints']:assert f'resonance.thedarkdimensions.{name}' in strings
        assert '%s' in strings['resonance.thedarkdimensions.journal_next_explore']
print('Validated Ancient Resonance: nine distinct cave-floor ruin templates, matching cave scopes, native binding and rune recipes, all unlock advancements, vanilla model assets, server state classes, accessors and DE/EN journal guidance.')

with zipfile.ZipFile(jar) as z:
    def data(path):return json.loads(z.read(path))
    sounds=data('assets/thedarkdimensions/sounds.json')
    cues=['perfect_parry','perfect_dodge','rune_ember','rune_tide','rune_radiance','rune_chronus','trial_complete','rare_loot','regent_awaken','regent_defeated','katana_finisher']
    for name in cues:
        assert sounds[name]['subtitle']==f'subtitles.thedarkdimensions.{name}'
        assert all(sound['type']=='event' and sound['name'].startswith('minecraft:') for sound in sounds[name]['sounds'])
    for name in ['frostgleam','bloodgrief','souleater','stormveil']:
        attack=data(f'data/thedarkdimensions/weapon_attributes/{name}.json')['attributes']['attacks'][2]
        assert attack['swing_sound']['id']=='thedarkdimensions:katana_finisher'
        assert attack['animation']=='thedarkdimensions:katana_finisher' and attack['upswing']==.65
    arena=data('data/thedarkdimensions/worldgen/structure/resonance_nexus.json')
    assert arena['type']=='minecraft:jigsaw' and arena['size']==1 and arena['project_start_to_heightmap']=='WORLD_SURFACE_WG'
    assert data('data/thedarkdimensions/worldgen/structure_set/resonance_nexus.json')['placement']['spacing']==48
    assert len(z.read('data/thedarkdimensions/structure/resonance_nexus.nbt'))>3000
    assert len(data('assets/thedarkdimensions/blockstates/nexus_altar.json')['variants'])==8
    assert data('data/thedarkdimensions/loot_table/entities/ashbound_regent.json')['pools']==[]
    assert 'thedarkdimensions:regent_wave' in data('data/minecraft/tags/damage_type/bypasses_shield.json')['values']
    assert 'thedarkdimensions:ashbound_regent' not in data('data/thedarkdimensions/tags/entity_type/katana_unparryable.json')['values']
    for kind in ['camp','cache','bridge','crystal']:
        name='wandering_'+kind
        assert data(f'data/thedarkdimensions/worldgen/feature/{name}.json')=={'type':'thedarkdimensions:wandering_site','kind':kind}
        assert data(f'data/thedarkdimensions/worldgen/placed_feature/{name}.json')['placement'][-1]['type']=='minecraft:biome'
        for biome in ['ash_wastes','charred_forest','echo_fields','crystal_hollows']:
            assert f'thedarkdimensions:{name}' in data(f'data/thedarkdimensions/worldgen/biome/{biome}.json')['features'][4]
    for name in ['abandoned_camp','hidden_cache']:
        loot=z.read(f'data/thedarkdimensions/loot_table/chests/{name}.json').decode()
        assert 'trial_sigil' not in loot and 'regents_heart' not in loot
    for name in ['AshboundRegent','NexusAltar','NexusAltarBlock','NexusGateBlock','FinalProgress','FinalLedger','CombatFeedback','FinaleContent']:
        assert f'at/janic/thedarkdimensions/finale/{name}.class' in z.namelist()
    for lang in ['de_de','en_us']:
        strings=data(f'assets/thedarkdimensions/lang/{lang}.json')
        for name in cues:assert f'subtitles.thedarkdimensions.{name}' in strings
        assert 'Resonant Step' in strings['finale.thedarkdimensions.ability_unlocked']
        assert f'bestiary.thedarkdimensions.ashbound_regent.behavior' in strings
print('Validated Last Resonance: permanent Nexus assets, parryable Regent and unblockable Rift, four bounded discovery features, progression-safe loot, eleven native sound events, Better Combat finisher cues and DE/EN finale guides.')

with zipfile.ZipFile(jar) as z:
    def data(path):return json.loads(z.read(path))
    archive=root/'tools/texture_sources/relic_katanas'
    manifest=data('assets/thedarkdimensions/relic_katana_sources.json')
    assert set(manifest)=={'basic_katana','mindshifter','mindshifter_off','hellsblade','hellsblade_off'}
    for name,entry in manifest.items():
        original=json.loads((archive/entry['source']).read_text(encoding='utf-8'));model=data(f'assets/thedarkdimensions/models/item/{name}.json')
        assert model['elements'][:entry['original_elements']]==original['elements']
        assert model['display']==original['display'] and model.get('credit')==original.get('credit')
        assert len(model['elements'])==entry['original_elements']+entry['emissive_layers']
        if name!='basic_katana':assert entry['emissive_layers']>0
    for name,folder,sub in [('basic_katana','Basic Katana','textures/item'),('mindshifter','Mindshifter Katana','citresewn/cit/textures'),('hellsblade','Hells Blade','citresewn/cit/textures')]:
        for original in (archive/folder/'minecraft'/sub).glob('*.png*'):
            assert z.read(f'assets/thedarkdimensions/textures/item/{name}/{original.name}')==original.read_bytes()
        attacks=data(f'data/thedarkdimensions/weapon_attributes/{name}.json')['attributes']['attacks']
        assert len(attacks)==3 and attacks[2]['animation']=='thedarkdimensions:katana_finisher'
    for name in ['mindshifter','hellsblade']:
        model=data(f'assets/thedarkdimensions/items/{name}.json')['model'];assert model['property']=='minecraft:main_hand'
        assert model['cases'][0]['when']=='left' and model['fallback']['property']=='minecraft:display_context'
    assert Image.open(io.BytesIO(z.read('assets/thedarkdimensions/textures/item/hellsblade/animation.png'))).size==(32,256)
    assert data('assets/thedarkdimensions/textures/item/hellsblade/animation.png.mcmeta')['animation']['frametime']==2
    assert data('data/thedarkdimensions/recipe/basic_katana.json')['key']=={'I':'minecraft:iron_ingot','S':'thedarkdimensions:reinforced_stick'}
    for name in ['mindshifter','hellsblade','stormveil']:
        assert 'thedarkdimensions:basic_katana' in data(f'data/thedarkdimensions/recipe/{name}.json')['ingredients']
    katanas=data('data/thedarkdimensions/tags/item/katana_enchantable.json')['values']
    for name in ['basic_katana','mindshifter','hellsblade']:assert f'thedarkdimensions:{name}' in katanas
    reach=data('data/thedarkdimensions/tags/item/improved_tools.json')['values']
    assert 'thedarkdimensions:basic_katana' not in reach and 'thedarkdimensions:mindshifter' in reach and 'thedarkdimensions:hellsblade' in reach
    eye=data('data/minecraft/recipe/ender_eye.json')
    assert eye['type']=='thedarkdimensions:resonant_ender_eye' and set(eye['ingredients'])=={'minecraft:ender_pearl','minecraft:blaze_powder','thedarkdimensions:essentialium'}
    assert 'EnderEyeCrafterMixin' in data('thedarkdimensions.mixins.json')['mixins']
    assert data('data/thedarkdimensions/advancement/resonance/end_unsealed.json')['rewards']['recipes']==['minecraft:ender_eye']
    for name in ['RelicKatanas','EndProgression','EnderEyeRecipe']:assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    for chest,weapon,chance in [('relic_end_city_treasure','mindshifter',.04),('elemental_shrine','hellsblade',.025)]:
        pool=data(f'data/thedarkdimensions/loot_table/chests/{chest}.json')['pools'][-1]
        assert pool['condition']['chance']==chance and pool['entries'][0]['name']==f'thedarkdimensions:{weapon}'
    for chest in ['end_city_treasure','nether_bridge','bastion_treasure']:assert data(f'data/thedarkdimensions/loot_modifiers/relic_{chest}.json')['condition']['loot_table_id']==f'minecraft:chests/{chest}'
    for lang in ['de_de','en_us']:
        strings=data(f'assets/thedarkdimensions/lang/{lang}.json')
        for name in ['basic_katana','mindshifter','hellsblade','end_progression']:assert f'guide.thedarkdimensions.{name}' in strings
        for name in ['mind_fracture','crimson_discharge']:assert f'perk.thedarkdimensions.{name}' in strings
print('Validated original relic art, five exact base meshes and hand transforms, native emissive layers, original eight-frame timing, all Basic Katana foundations, rare chest additions and the pre-End world-trial gate.')

with zipfile.ZipFile(jar) as z:
    def data(path):return json.loads(z.read(path))
    assert data('data/thedarkdimensions/recipe/mindshifter.json')['ingredients']==['minecraft:end_crystal','thedarkdimensions:basic_katana','minecraft:shulker_shell']
    assert data('data/thedarkdimensions/tags/entity_type/mindshift_swap_immune.json')['values']==['minecraft:ender_dragon','minecraft:wither']
    for chest in ['chronus_sanctum','sunken_archive','hidden_cache']:assert 'thedarkdimensions:mindshifter' not in z.read(f'data/thedarkdimensions/loot_table/chests/{chest}.json').decode()
    for name in ['Mindshift','MindshiftBolt','CrimsonRupture']:assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    assert 'ShulkerBulletTargetAccess' in data('thedarkdimensions.mixins.json')['mixins']
    for name in ['mindshifter_cast','mindshifter_shift']:
        anim=data(f'assets/thedarkdimensions/player_animations/{name}.json')
        assert anim['name']==name and anim['version']==3 and anim['emote']['stopTick']==16 and len(anim['emote']['moves'])>=3
    for lang in ['de_de','en_us']:
        strings=data(f'assets/thedarkdimensions/lang/{lang}.json')
        for name in ['mindshift','crimson_rupture']:assert f'perk.thedarkdimensions.{name}' in strings
        assert f'combat.thedarkdimensions.mindshift_cooldown' in strings
print('Validated End-only Mindshifter recipe/treasure, saved native Shulker projectile/accessor, two optional player animations, swap immunity and localized Crimson Rupture/cooldown feedback.')


with zipfile.ZipFile(jar) as z:
    def data(path):return json.loads(z.read(path))
    for name in ['MagicExpansion','SpellAfterimage','MagicSkill','MagicData']:
        assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    assert b'magic_learned_v2' in z.read('at/janic/thedarkdimensions/MagicData.class')
    pulse='thedarkdimensions:arcane_pulse'
    assert data('data/thedarkdimensions/damage_type/arcane_pulse.json')['scaling']=='never'
    for tag in ['bypasses_cooldown','bypasses_armor','is_magic']:
        assert pulse in data(f'data/minecraft/tags/damage_type/{tag}.json')['values']
    for lang in ['de_de','en_us']:
        strings=data(f'assets/thedarkdimensions/lang/{lang}.json')
        for name in ['spelltable','spell_branches','spell_tactics']:
            assert f'guide.thedarkdimensions.{name}' in strings
        assert '39' in strings['guide.thedarkdimensions.spelltable']
        assert 'death.attack.arcane_pulse' in strings
    assert not any('/test/' in name or name.endswith('SkillTreeTests.class') for name in z.namelist())
print('Validated expanded skilltree runtime, 64-bit book migration, transient native afterimages, magic pulse tags and localized 39-spell guides; integration fixtures excluded.')

with zipfile.ZipFile(jar) as z:
    def data(path):return json.loads(z.read(path))
    import hashlib
    archive=root/'tools/texture_sources/back_equipment'
    manifest=data('assets/thedarkdimensions/back_equipment_sources.json')
    assert len(manifest)==6
    for target,entry in manifest.items():
        raw=z.read(f'assets/thedarkdimensions/{target}')
        assert raw==(archive/entry['source']).read_bytes()
        assert hashlib.sha256(raw).hexdigest()==entry['sha256']
        assert Image.open(io.BytesIO(raw)).size==((64,32) if '/wings/' in target else (16,16))
    for name in ['wool_glider','devils_wings']:
        model=data(f'assets/thedarkdimensions/items/{name}.json')['model']
        assert model['type']=='minecraft:condition' and model['property']=='minecraft:broken'
        assert data(f'assets/thedarkdimensions/equipment/{name}.json')['layers']['wings']==[{'texture':f'thedarkdimensions:{name}','use_player_texture':False}]
        assert data(f'data/thedarkdimensions/recipe/{name}.json')['result']['id']==f'thedarkdimensions:{name}'
    slots=data('data/trinkets/entities/thedarkdimensions_player.json')['slots']
    assert len(slots)==6 and 'feet/dark_windstep' in slots
    for name in slots:
        slot=data(f'data/trinkets/slots/{name}.json')
        assert slot['amount']==1 and slot['max_stack_size']==1
        assert slot['icon'].startswith('minecraft:container/slot/') or slot['icon']=='thedarkdimensions:container/slots/glider'
    assert data('data/trinkets/slots/chest/dark_back_tool.json')['is_vanity']
    assert data('data/trinkets/slots/feet/group.json')['slot_id']==8
    assert data('data/trinkets/slots/chest/group.json')['slot_id']==6
    assert data('data/trinkets/tags/item/feet/dark_windstep.json')['values']==['thedarkdimensions:windstep_sandals']
    assert data('assets/thedarkdimensions/trinkets/back_gliders.json')['render']==[{'type':'wings'}]
    mixins=data('thedarkdimensions.mixins.json')
    for name in ['DevilWingsFlightMixin','DevilWingsStartMixin','GliderRocketUseMixin','GliderRocketBoostMixin']:assert name in mixins['mixins']
    assert 'BackToolRenderMixin' in mixins['client']
    for name in ['BackEquipment','client/BackToolLayer']:assert f'at/janic/thedarkdimensions/{name}.class' in z.namelist()
    for lang in ['de_de','en_us']:
        strings=data(f'assets/thedarkdimensions/lang/{lang}.json')
        for key in ['key.thedarkdimensions.swap_back','ui.thedarkdimensions.creative_upgrade','guide.thedarkdimensions.back_equipment']:assert key in strings
    assert not any('/test/' in n or '/modeltest/' in n for n in z.namelist())
print('Validated six accessory slots/icons, vanity tool storage, exact supplied glider art, native wing layers, recipes, Creative localization and all flight/swap mixins; no test classes packaged.')
