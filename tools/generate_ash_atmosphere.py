"""Give the Burned Realm readable ash-grey lighting, including all of its biomes."""
import generate_dark_acacia
from generate_resources import RES, MOD, write
from generate_minerals import read

path=f'data/{MOD}/dimension_type/burned_realm.json'
dimension=read(RES/path)
dimension['ambient_light']=0.35
dimension['attributes'].update({
    'minecraft:visual/sky_color':'#b1aaa0',
    'minecraft:visual/fog_color':'#c0b8ad',
    'minecraft:visual/sky_light_factor':0.85,
    'minecraft:visual/fog_end_distance':192.0,
})
write(path,dimension)
# Biome colours override dimension defaults; update the surface and cave biomes too.
for name in ['ash_wastes','charred_forest','crystal_hollows','normal_caves','fire_cave','water_cave','glowing_cave']:
    path=f'data/{MOD}/worldgen/biome/{name}.json';biome=read(RES/path)
    biome['attributes'].update({
        'minecraft:visual/sky_color':'#b1aaa0',
        'minecraft:visual/fog_color':'#c0b8ad',
    })
    write(path,biome)
print('Applied a bright ash-grey Burned Realm atmosphere with stronger ambient light and longer fog distance.')
