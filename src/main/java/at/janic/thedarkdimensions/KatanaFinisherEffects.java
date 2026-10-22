package at.janic.thedarkdimensions;

import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Theme-specific impact visuals; no extra damage beyond the existing third-hit bonuses. */
public final class KatanaFinisherEffects {
    public static void play(ServerLevel level,ItemStack weapon,LivingEntity user,LivingEntity target){
        Vec3 at=target.position().add(0,target.getBbHeight()*.5,0);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK,at.x,at.y,at.z,1,0,0,0,0);
        ParticleOptions particle;SoundEvent sound;
        if(weapon.is(KatanaContent.FROSTGLEAM)){
            particle=ParticleTypes.SNOWFLAKE;sound=SoundEvents.GLASS_BREAK;
            SpellEffects.burst(level,new BlockParticleOption(ParticleTypes.BLOCK,net.minecraft.world.level.block.Blocks.BLUE_ICE.defaultBlockState()),at,18);
        }else if(weapon.is(KatanaContent.BLOODGRIEF)){
            particle=KatanaEffects.BLOOD_DROP.get();sound=SoundEvents.PLAYER_ATTACK_CRIT;
            for(int i=0;i<3;i++)SpellEffects.line(level,particle,at.add(-.65,-.5+i*.2,-.1),at.add(.65,.5+i*.2,.1));
        }else if(weapon.is(KatanaContent.SOULEATER)){
            particle=ParticleTypes.SOUL_FIRE_FLAME;sound=SoundEvents.SOUL_ESCAPE.value();
            SpellEffects.burst(level,ParticleTypes.SOUL,at,12);
        }else if(weapon.is(SolarContent.STORMVEIL)){
            particle=ParticleTypes.ELECTRIC_SPARK;sound=SoundEvents.LIGHTNING_BOLT_IMPACT;
            Vec3 start=user.getEyePosition();for(int i=0;i<4;i++){var end=start.lerp(at,(i+1)/4.0).add(i==3?0:(i%2==0?.15:-.15),0,0);SpellEffects.line(level,particle,start,end);start=end;}
            SpellEffects.burst(level,new DustParticleOptions(0xf8da69,1),at,14);
        }else if(weapon.is(RelicKatanas.MINDSHIFTER)){
            particle=ParticleTypes.REVERSE_PORTAL;sound=SoundEvents.ENDERMAN_TELEPORT;
            SpellEffects.burst(level,new DustParticleOptions(0xac72e4,1),at,14);
        }else if(weapon.is(RelicKatanas.HELLSBLADE)){
            particle=new DustParticleOptions(0xe92738,1.1f);sound=SoundEvents.REDSTONE_TORCH_BURNOUT;
            SpellEffects.burst(level,ParticleTypes.CRIT,at,8);
        }else if(weapon.is(RelicKatanas.BASIC)){
            particle=ParticleTypes.CRIT;sound=SoundEvents.PLAYER_ATTACK_STRONG;
        }else return;
        for(int i=0;i<28;i++){double a=i*Math.PI/14;double y=weapon.is(KatanaContent.SOULEATER)?(double)i/28-.5:Math.sin(a)*.35;
            level.sendParticles(particle,at.x+Math.cos(a)*.95,at.y+y,at.z+Math.sin(a)*.95,1,.03,.03,.03,.015);}
        SpellEffects.burst(level,particle,at,16);
        level.playSound(null,target.getX(),target.getY(),target.getZ(),sound,SoundSource.PLAYERS,.55f,weapon.is(KatanaContent.SOULEATER)?.75f:1.2f);
    }
}
