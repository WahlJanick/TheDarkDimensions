package at.janic.thedarkdimensions;

import java.util.List;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Native particles and positional sounds, broadcast only after a successful spell. */
public final class SpellEffects {
    public static ParticleOptions particle(MagicSkill skill){return switch(skill.element()){
        case FIRE->ParticleTypes.FLAME;case WATER->ParticleTypes.SPLASH;case LIGHTNING->ParticleTypes.ELECTRIC_SPARK;
        case CHRONUS->new DustParticleOptions(0x9a7bd3,1);case BLOOD->KatanaEffects.BLOOD_DROP.get();
        case SOUL->ParticleTypes.SOUL_FIRE_FLAME;case ENDER->ParticleTypes.PORTAL;case ELEMENTAL->ParticleTypes.ENCHANT;
    };}
    public static void burst(ServerLevel l,ParticleOptions p,Vec3 at,int count){l.sendParticles(p,at.x,at.y,at.z,count,.35,.45,.35,.025);}
    public static void line(ServerLevel l,ParticleOptions p,Vec3 from,Vec3 to){
        int steps=Math.max(2,Math.min(48,(int)Math.ceil(from.distanceTo(to)*4)));
        for(int i=0;i<=steps;i++){var at=from.lerp(to,(double)i/steps);l.sendParticles(p,at.x,at.y,at.z,1,0,0,0,0);}
    }
    public static void ring(ServerLevel l,ParticleOptions p,Vec3 at,double radius){
        for(int i=0;i<32;i++){double a=i*Math.PI/16;l.sendParticles(p,at.x+Math.cos(a)*radius,at.y,at.z+Math.sin(a)*radius,1,.02,.03,.02,.015);}
    }
    private static Vec3 center(LivingEntity e){return e.position().add(0,e.getBbHeight()*.5,0);}
    public static void play(MagicSkill skill,ServerLevel l,Player player,Vec3 origin,Vec3 impact,List<LivingEntity> targets){
        play(skill,l,player,origin,impact,targets,1,0);
    }
    public static void play(MagicSkill skill,ServerLevel l,Player player,Vec3 origin,Vec3 impact,List<LivingEntity> targets,int rank,int mastery){
        var p=particle(skill);Vec3 at=player.position(),body=at.add(0,1,0),eye=player.getEyePosition();
        double reach=MagicMastery.radius(skill,mastery,5+(rank-1)*.5);
        switch(skill){
            case FIRE_BOLT->{line(l,p,eye,eye.add(player.getLookAngle().scale(1.8)));burst(l,ParticleTypes.SMALL_FLAME,eye.add(player.getLookAngle()),12);}
            case FIRE_RING->{ring(l,p,at.add(0,.25,0),reach);targets.stream().limit(16).forEach(e->burst(l,p,center(e),10));}
            case FIRE_GUARD->{ring(l,p,at.add(0,.3,0),.75);ring(l,ParticleTypes.SMALL_FLAME,body,.65);}
            case WATER_HEAL->{burst(l,ParticleTypes.HAPPY_VILLAGER,body,12);ring(l,p,at.add(0,.25,0),.8);}
            case WATER_WAVE->{ring(l,p,at.add(0,.4,0),2);ring(l,p,at.add(0,.4,0),reach);targets.stream().limit(16).forEach(e->line(l,ParticleTypes.FALLING_WATER,body,center(e)));}
            case WATER_GUARD->{ring(l,ParticleTypes.FALLING_WATER,body,.7);burst(l,ParticleTypes.NAUTILUS,body,14);}
            case LIGHTNING_STRIKE->{line(l,p,impact.add(0,5,0),impact);burst(l,p,impact.add(0,1,0),24);}
            case CHAIN_LIGHTNING->burst(l,p,eye,10); // Connections follow actual damaged targets in MagicCasting.
            case LIGHTNING_STEP->{line(l,p,origin.add(0,.8,0),body);ring(l,p,at.add(0,.2,0),2);}
            case CHRONUS_DASH->{line(l,p,origin.add(0,.8,0),body);burst(l,ParticleTypes.ENCHANT,body,10);}
            case TIME_SLOW->{ring(l,p,at.add(0,.2,0),reach);targets.stream().limit(16).forEach(e->{ring(l,p,e.position().add(0,1,0),.65);burst(l,ParticleTypes.ENCHANT,center(e),8);});}
            case REWIND->{line(l,ParticleTypes.REVERSE_PORTAL,origin.add(0,.8,0),body);ring(l,p,at.add(0,.2,0),1);burst(l,ParticleTypes.ENCHANT,body,18);}
            case BLOOD_CUT->{line(l,p,impact.add(-.4,.7,0),impact.add(.4,-.3,0));burst(l,p,impact,20);}
            case BLOOD_SIPHON->{line(l,p,impact,body);burst(l,ParticleTypes.HEART,body,4);}
            case BLOOD_GUARD->{ring(l,p,at.add(0,.3,0),.8);ring(l,p,body,.7);}
            case SOUL_BOLT->{line(l,p,eye,impact);burst(l,ParticleTypes.SOUL,impact,10);}
            case SOUL_FEAR->{ring(l,p,at.add(0,.4,0),reach);targets.stream().limit(16).forEach(e->burst(l,ParticleTypes.SOUL,center(e),8));}
            case SOUL_SIGHT->{ring(l,p,body,.8);targets.stream().limit(16).forEach(e->burst(l,ParticleTypes.GLOW,center(e),10));}
            case ENDER_CHEST->{ring(l,p,body,.65);burst(l,ParticleTypes.REVERSE_PORTAL,eye.add(player.getLookAngle().scale(.7)),16);}
            case ENDER_BLINK->{burst(l,p,origin.add(0,1,0),20);burst(l,ParticleTypes.REVERSE_PORTAL,body,20);}
            case ENDER_WARD->{ring(l,p,body,.8);burst(l,ParticleTypes.END_ROD,body,12);}
            case ELEMENTAL_BURST->{ring(l,ParticleTypes.FLAME,at.add(0,.2,0),reach*.6);ring(l,ParticleTypes.SPLASH,at.add(0,.3,0),reach);targets.stream().limit(16).forEach(e->burst(l,ParticleTypes.ELECTRIC_SPARK,center(e),8));}
            case ELEMENTAL_BARRIER->{ring(l,ParticleTypes.ENCHANT,body,.85);ring(l,ParticleTypes.END_ROD,at.add(0,.3,0),.8);}
            case ELEMENTAL_SURGE->{ring(l,ParticleTypes.ELECTRIC_SPARK,at.add(0,.3,0),reach);targets.stream().limit(16).forEach(e->{line(l,ParticleTypes.ELECTRIC_SPARK,eye,center(e));burst(l,ParticleTypes.SNOWFLAKE,center(e),8);});burst(l,ParticleTypes.HAPPY_VILLAGER,body,10);}
            case EMBER_MARK,STATIC_CHARGE->{line(l,p,eye,impact);ring(l,p,impact,.45);}
            case ASH_STEP,AFTERIMAGE->{line(l,p,origin.add(0,.8,0),body);burst(l,skill==MagicSkill.ASH_STEP?ParticleTypes.ASH:ParticleTypes.ENCHANT,origin.add(0,1,0),16);}
            case INFERNO->{ring(l,p,at.add(0,.2,0),3);burst(l,ParticleTypes.SMALL_FLAME,body,16);}
            case FROST_BIND->{line(l,ParticleTypes.SNOWFLAKE,eye,impact);ring(l,ParticleTypes.SNOWFLAKE,impact,.6);}
            case WATER_VEIL->{ring(l,ParticleTypes.FALLING_WATER,body,.7);burst(l,ParticleTypes.SPLASH,body,12);}
            case HEALING_SPRING->{ring(l,ParticleTypes.FALLING_WATER,at.add(0,.2,0),MagicMastery.radius(skill,mastery,2.5));burst(l,ParticleTypes.HAPPY_VILLAGER,body,8);}
            case THUNDER_LEAP->{line(l,p,origin.add(0,.2,0),at.add(0,.2,0));burst(l,p,body,16);}
            case STORM_FIELD->{ring(l,p,impact,MagicMastery.radius(skill,mastery,4));line(l,p,impact.add(0,4,0),impact.add(0,3,0));}
            case TIME_ANCHOR->{ring(l,p,at.add(0,.1,0),.7);line(l,ParticleTypes.REVERSE_PORTAL,origin.add(0,1,0),body);}
            case INERTIA_FIELD->{ring(l,p,at.add(0,.1,0),MagicMastery.radius(skill,mastery,4));ring(l,ParticleTypes.ENCHANT,body,1);}
            case STOLEN_MOMENT->{ring(l,p,body,.6);burst(l,ParticleTypes.ENCHANT,eye,10);}
            case VOID_PULL->{line(l,ParticleTypes.REVERSE_PORTAL,eye,impact);ring(l,p,impact,1);}
            case DIMENSION_FOLD->{burst(l,p,body,20);burst(l,ParticleTypes.REVERSE_PORTAL,impact,20);}
        }
        SoundEvent sound=switch(skill.element()){
            case FIRE->SoundEvents.FIRECHARGE_USE;case WATER->SoundEvents.PLAYER_SPLASH;case LIGHTNING->SoundEvents.LIGHTNING_BOLT_IMPACT;
            case CHRONUS->SoundEvents.AMETHYST_BLOCK_CHIME;case BLOOD->SoundEvents.WITCH_DRINK;case SOUL->SoundEvents.EVOKER_CAST_SPELL;
            case ENDER->SoundEvents.PLAYER_TELEPORT;case ELEMENTAL->SoundEvents.ENCHANTMENT_TABLE_USE;
        };
        l.playSound(null,player.getX(),player.getY(),player.getZ(),sound,SoundSource.PLAYERS,.4f,1.05f+skill.tier()*.1f);
    }
}
