package at.janic.thedarkdimensions.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** Small opaque red drops with gravity; uses Minecraft's droplet sprite, never potion spirals. */
public final class BloodDropParticle extends SingleQuadParticle {
    private BloodDropParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,TextureAtlasSprite sprite){
        super(level,x,y,z,sprite);xd=vx;yd=vy-.015;zd=vz;gravity=.9f;lifetime=16+random.nextInt(10);quadSize=.035f+random.nextFloat()*.025f;setSize(.015f,.015f);rCol=.68f+random.nextFloat()*.15f;gCol=.015f;bCol=.035f;
    }
    @Override public Layer getLayer(){return Layer.OPAQUE;}
    @Override public void tick(){super.tick();if(onGround)remove();}
    public static final class Provider implements ParticleProvider<SimpleParticleType>{
        private final SpriteSet sprites;public Provider(SpriteSet sprites){this.sprites=sprites;}
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel level,double x,double y,double z,double vx,double vy,double vz,RandomSource random){return new BloodDropParticle(level,x,y,z,vx,vy,vz,sprites.get(random));}
    }
}
