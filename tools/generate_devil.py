"""Artist Spelltable textures, emissive layers and native Devil trident CEM port."""
import generate_blood_netherite
from generate_resources import RES,MOD,write,item
from generate_minerals import read
from pathlib import Path
import shutil,json,copy,zipfile

root=RES.parents[2];archive=Path(__file__).parent/'texture_sources/devils_trident';table_archive=Path(__file__).parent/'texture_sources/spelltable'
archive.mkdir(parents=True,exist_ok=True);table_archive.mkdir(parents=True,exist_ok=True)
pack=root.parent/'TheDevilsTrident/assets/minecraft';table_pack=root.parent/'Spell_table/assets/minecraft/textures/block'
def archive_copy(original,stored,target):
    if original.exists():shutil.copyfile(original,stored)
    target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(stored,target)
for face in ['top','side']:
    for suffix in ['', '_e']:
        name=f'spelltable_{face}{suffix}'
        archive_copy(table_pack/f'enchanting_table_{face}{suffix}.png',table_archive/f'{name}.png',RES/f'assets/{MOD}/textures/block/{name}.png')
model=read(RES/f'assets/{MOD}/models/block/spelltable.json');model['render_type']='minecraft:cutout'
for element in model['elements']:
    for face in element['faces'].values():face.pop('tintindex',None)
glow=copy.deepcopy(model['elements'][0]);glow['light_emission']=15;glow['shade']=False
glow['from']=[-0.001,0,-0.001];glow['to']=[16.001,12.001,16.001];glow['faces'].pop('down',None)
for face in glow['faces'].values():face['texture']=face['texture']+'_e';face.pop('cullface',None)
model['elements'].append(glow);model['textures'].update(top_e=f'{MOD}:block/spelltable_top_e',side_e=f'{MOD}:block/spelltable_side_e')
write(f'assets/{MOD}/models/block/spelltable.json',model)
p=f'assets/{MOD}/clean_block_palette.json';palette=read(RES/p);palette.pop('spelltable',None);write(p,palette)
for kind in ['item','entity']:
    archive_copy(pack/f'textures/{kind}/trident.png',archive/f'trident_{kind}.png',RES/f'assets/{MOD}/textures/{kind}/devils_trident.png')
jem_file=archive/'trident.jem'
if (pack/'optifine/cem/trident.jem').exists():shutil.copyfile(pack/'optifine/cem/trident.jem',jem_file)
jem=read(jem_file)
# Convert OptiFine invertAxis xy coordinates to the native model coordinate system.
boxes=[]
for part in jem['models']:
    assert part['invertAxis']=='xy' and part['translate']==[0,-27,0]
    for box in part['boxes']:
        x,y,z,w,h,d=box['coordinates'];u,v=box['textureOffset']
        coords=[-x-w,27-y-h,z,w,h,d]
        boxes.append(f'.texOffs({u},{v}).addBox('+','.join(f'{float(n)}F' for n in coords)+')')
java='''package at.janic.thedarkdimensions.client;
import at.janic.thedarkdimensions.TheDarkDimensions;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
/** Generated from the artist CEM: exact box sizes and texture offsets, adapted axes. */
public final class DevilTridentModel extends Model<Unit> {
    public static final Identifier TEXTURE=TheDarkDimensions.id("textures/entity/devils_trident.png");
    public static final ModelLayerLocation LAYER=new ModelLayerLocation(TheDarkDimensions.id("devils_trident"),"main");
    public DevilTridentModel(ModelPart root) {super(root,RenderTypes::entitySolid);}
    public static LayerDefinition createLayer() {
        var mesh=new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("trident",CubeListBuilder.create()BOXES,PartPose.ZERO);
        return LayerDefinition.create(mesh,WIDTH,HEIGHT);
    }
}
'''.replace('BOXES','\n            '+ '\n            '.join(boxes)).replace('WIDTH',str(jem['textureSize'][0])).replace('HEIGHT',str(jem['textureSize'][1]))
(root/'src/main/java/at/janic/thedarkdimensions/client/DevilTridentModel.java').write_text(java,encoding='utf-8')
native_archive=archive/'native_trident_item.json'
if not native_archive.exists():
    client=Path('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar')
    with zipfile.ZipFile(client) as z:native_archive.write_bytes(z.read('assets/minecraft/items/trident.json'))
def rewrite(value):
    if isinstance(value,dict):return {k:rewrite(v) for k,v in value.items()}
    if isinstance(value,list):return [rewrite(v) for v in value]
    if value=='minecraft:item/trident':return f'{MOD}:item/devils_trident'
    if value=='minecraft:trident':return f'{MOD}:devils_trident'
    return value
item('devils_trident');write(f'assets/{MOD}/items/devils_trident.json',rewrite(read(native_archive)))
write(f'data/{MOD}/recipe/devils_trident.json',{'type':'minecraft:smithing_transform','template':f'{MOD}:red_netherite_upgrade_smithing_template','base':'minecraft:trident','addition':f'{MOD}:red_netherite_ingot','result':{'id':f'{MOD}:devils_trident'}})
for tag in ['enchantable/trident','enchantable/trident_exclusive','enchantable/weapon','enchantable/durability','enchantable/sharp_weapon']:
    p=f'data/minecraft/tags/item/{tag}.json';data=read(RES/p) if (RES/p).exists() else {'replace':False,'values':[]}
    data['values']=sorted(set(data['values']+[f'{MOD}:devils_trident']));write(p,data)
# A native icon uses the same pixel raster as the existing status effects.
native=Path('C:/Users/janic/.gradle/caches/neoformruntime/artifacts/minecraft_26.3_client.jar')
icon=archive/'devils_charm.png'
if not icon.exists():
    with zipfile.ZipFile(native) as z:icon.write_bytes(z.read('assets/minecraft/textures/mob_effect/hero_of_the_village.png'))
shutil.copyfile(icon,RES/f'assets/{MOD}/textures/mob_effect/devils_charm.png')
for lang in ['de_de','en_us']:
    p=f'assets/{MOD}/lang/{lang}.json';data=read(RES/p)
    data[f'item.{MOD}.devils_trident']="Devil's Trident";data[f'effect.{MOD}.devils_charm']="Devil's Charm"
    data[f'guide.{MOD}.devils_trident']='Schmiedetisch: Blood Netherite Upgrading Template + Trident + Blood Netherite Ingot. 8 Grundschaden, Treffer verursachen Blood (5 s) und Feuer (4 s). Benutzen gibt Devil’s Charm für 5 s: Villager und ihre Kinder fliehen im Umkreis von 8 Blöcken.' if lang=='de_de' else 'Smithing: Blood Netherite Upgrading Template + Trident + Blood Netherite Ingot. 8 base damage; hits apply Blood (5s) and fire (4s). Use grants Devil’s Charm for 5s: adult and baby villagers flee within 8 blocks.'
    write(p,data)
print('Imported Spelltable art/emission and Devil trident texture, CEM geometry, smithing and guide.')
