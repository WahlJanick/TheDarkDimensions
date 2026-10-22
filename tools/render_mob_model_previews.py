"""Diagnostic projection of meshes emitted by native Minecraft ModelParts; source textures are read only."""
import json,math,argparse
from pathlib import Path
import numpy as np
from PIL import Image,ImageDraw
r=Path(__file__).resolve().parent.parent
parser=argparse.ArgumentParser();parser.add_argument('--yaw',type=float,default=30);parser.add_argument('--pitch',type=float,default=12);parser.add_argument('--label',default='front');args=parser.parse_args()
meshes=json.loads((r/f'.dev/mob-world-mesh-{int(args.yaw)}.json').read_text(encoding='utf-8'))
for state in ['idle','combat']:
 entries=sorted((x for x in meshes.items() if x[0].endswith('_'+state)));sheet=Image.new('RGB',(1120,310*math.ceil(len(entries)/4)),'#30343a');labels=ImageDraw.Draw(sheet)
 for index,(name,data) in enumerate(entries):
  kind=name.rsplit('_',1)[0];a=np.array(data,dtype=float);raw=np.array(Image.open(r/f'src/main/resources/assets/thedarkdimensions/textures/entity/{kind}.png').convert('RGBA'));h,w=raw.shape[:2]
  yaw=math.radians(args.yaw);pitch=math.radians(args.pitch)
  x=a[:,0]*math.cos(yaw)-a[:,2]*math.sin(yaw);z=a[:,0]*math.sin(yaw)+a[:,2]*math.cos(yaw)
  y=-a[:,1]*math.cos(pitch)+z*math.sin(pitch);depth=z*math.cos(pitch)+a[:,1]*math.sin(pitch)
  scale=min(245/max(x.max()-x.min(),.01),250/max(y.max()-y.min(),.01))
  px=(x-(x.min()+x.max())/2)*scale+140;py=(y-(y.min()+y.max())/2)*scale+135
  image=np.zeros((280,280,4),dtype=np.uint8);buffer=np.full((280,280),-np.inf)
  for start in range(0,len(a),4):
   for ids in [[start,start+1,start+2],[start,start+2,start+3]]:
    points=np.array([[px[i],py[i]] for i in ids]);lo=np.maximum(np.floor(points.min(axis=0)).astype(int),0);hi=np.minimum(np.ceil(points.max(axis=0)).astype(int),279)
    if (hi<lo).any():continue
    xx,yy=np.meshgrid(np.arange(lo[0],hi[0]+1),np.arange(lo[1],hi[1]+1));p0,p1,p2=points
    denominator=(p1[1]-p2[1])*(p0[0]-p2[0])+(p2[0]-p1[0])*(p0[1]-p2[1])
    if abs(denominator)<1e-8:continue
    b0=((p1[1]-p2[1])*(xx-p2[0])+(p2[0]-p1[0])*(yy-p2[1]))/denominator
    b1=((p2[1]-p0[1])*(xx-p2[0])+(p0[0]-p2[0])*(yy-p2[1]))/denominator;b2=1-b0-b1
    uv=b0[...,None]*a[ids[0],3:5]+b1[...,None]*a[ids[1],3:5]+b2[...,None]*a[ids[2],3:5]
    rgba=raw[np.clip((uv[:,:,1]*h).astype(int),0,h-1),np.clip((uv[:,:,0]*w).astype(int),0,w-1)].copy()
    d=b0*depth[ids[0]]+b1*depth[ids[1]]+b2*depth[ids[2]]
    # Watcher's legacy RGB skin is intentionally rendered with ENTITY_SOLID.
    if kind=='hollow_watcher':rgba[:,:,3]=255
    mask=(b0>=-1e-6)&(b1>=-1e-6)&(b2>=-1e-6)&(rgba[:,:,3]>127)&(d>buffer[yy,xx])
    shade=.72+.28*abs(a[ids[0],6]);rgba[:,:,:3]=(rgba[:,:,:3]*shade).astype(np.uint8)
    image[yy[mask],xx[mask]]=rgba[mask];buffer[yy[mask],xx[mask]]=d[mask]
  col=index%4;row=index//4;im=Image.fromarray(image);sheet.paste(im,(col*280,row*310),im);labels.text((col*280+10,row*310+283),kind+' / '+state,fill='white')
 sheet.save(r/f'.dev/mob-models-{args.label}-{state}.png')
