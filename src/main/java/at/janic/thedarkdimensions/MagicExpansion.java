package at.janic.thedarkdimensions;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Short-lived, server-owned spells. Fields never load chunks or edit terrain. */
public final class MagicExpansion {
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> PULSE=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,TheDarkDimensions.id("arcane_pulse"));
    private static final class Mark {
        final ServerPlayer owner;final long until;final int level,mastery;int charges;
        Mark(ServerPlayer owner,int ticks,int level){this(owner,ticks,level,0);}Mark(ServerPlayer owner,int ticks,int level,int mastery){this.owner=owner;this.until=owner.level().getGameTime()+ticks;this.level=level;this.mastery=mastery;}
    }
    private record Anchor(ServerLevel level,Vec3 at,long until,int mastery){}
    private static final class Moment {final ServerLevel level;final long until;final int rank,mastery;boolean credit;Moment(ServerPlayer p,int rank,int mastery){level=p.level();until=level.getGameTime()+(mastery==1?240:160);this.rank=rank;this.mastery=mastery;}}
    private static final class Field {
        final ServerPlayer owner;final ServerLevel level;final MagicSkill skill;final Vec3 at,end;final int rank,mastery;final long start,until;
        final Map<UUID,Long> ready=new HashMap<>();ArmorStand decoy;
        Field(ServerPlayer p,MagicSkill skill,Vec3 at,Vec3 end,int rank,int duration,int mastery){owner=p;level=p.level();this.mastery=mastery;this.skill=skill;this.at=at;this.end=end;this.rank=rank;start=level.getGameTime();until=start+duration;}
    }
    private static final Map<LivingEntity,Mark> EMBERS=new WeakHashMap<>(),STATIC=new WeakHashMap<>();
    private static final Map<ServerPlayer,Mark> VEILS=new WeakHashMap<>();
    private static final Map<ServerPlayer,Anchor> ANCHORS=new WeakHashMap<>();
    private static final Map<ServerPlayer,Moment> MOMENTS=new WeakHashMap<>();
    private static final Map<ServerPlayer,Long> MOMENT_READY=new WeakHashMap<>();
    private static final List<Field> FIELDS=new ArrayList<>();
    public static boolean hostile(LivingEntity e,Player p){return e!=p&&e.isAlive()&&!e.isAlliedTo(p)&&(e instanceof Monster||e instanceof Mob m&&m.getTarget()==p);}
    private static boolean live(Mark m,LivingEntity e){return e.isAlive()&&m.owner.isAlive()&&!m.owner.hasDisconnected()&&m.owner.level()==e.level()&&e.level().getGameTime()<m.until;}
    public static boolean marked(ServerPlayer p,LivingEntity e,boolean ember){var m=(ember?EMBERS:STATIC).get(e);return m!=null&&m.owner==p&&live(m,e);}
    private static List<LivingEntity> enemies(ServerPlayer p,Vec3 at,double radius){return p.level().getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(radius),e->hostile(e,p)&&e.position().distanceToSqr(at)<=radius*radius&&p.hasLineOfSight(e));}
    private static boolean boss(LivingEntity e){return e.typeHolder().is(Mindshift.IMMUNE)||e instanceof at.janic.thedarkdimensions.finale.AshboundRegent||e instanceof AshenSovereign;}
    private static void damage(ServerPlayer p,LivingEntity target,float amount){target.hurtServer(p.level(),new net.minecraft.world.damagesource.DamageSource(p.level().registryAccess().getOrThrow(PULSE),p,p),amount);}
    private static void frost(ServerPlayer p,LivingEntity e,int rank,int duration){e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,duration,boss(e)?0:4),p);if(rank==5&&!boss(e))e.addEffect(new MobEffectInstance(KatanaEffects.FROSTED,duration,0),p);SpellEffects.burst(p.level(),ParticleTypes.SNOWFLAKE,e.position().add(0,.7,0),12);}
    private static void add(ServerPlayer p,MagicSkill s,Vec3 at,Vec3 end,int rank,int duration,int mastery){
        // One field of each kind per caster, including after equipment changes.
        for(var it=FIELDS.iterator();it.hasNext();){var old=it.next();if(old.owner==p&&old.skill==s){dispose(old);it.remove();}}
        FIELDS.add(new Field(p,s,at,end,rank,MagicMastery.duration(s,mastery,duration),mastery));
    }
    public static boolean cast(ServerPlayer p,MagicSkill skill,int rank,LivingEntity target){return cast(p,skill,rank,target,0);}
    public static boolean cast(ServerPlayer p,MagicSkill skill,int rank,LivingEntity target,int mastery){
        int bonus=rank-1;Vec3 origin=p.position();
        switch(skill){
            case EMBER_MARK->{if(target==null)return false;EMBERS.put(target,new Mark(p,140+bonus*10,rank,mastery));if(mastery==2)enemies(p,target.position(),3).stream().filter(e->e!=target).limit(2).forEach(e->EMBERS.put(e,new Mark(p,140,rank,mastery)));}
            case ASH_STEP->{if(!MagicCasting.move(p,MagicMastery.distance(skill,mastery,3+bonus*.25)))return false;add(p,skill,origin,p.position(),rank,60+bonus*10,mastery);}
            case INFERNO->{var list=enemies(p,p.position(),mastery==1?18:12);boolean found=false;for(var e:list)if(marked(p,e,true)){detonate(p,e,4+rank+(mastery==2?3:0));if(rank==5)for(var nearby:enemies(p,e.position(),1.5))if(nearby!=e)damage(p,nearby,2);found=true;}if(!found)return false;}
            case FROST_BIND->{if(target==null)return false;frost(p,target,rank,30+bonus*5+(mastery==1?30:0));if(rank==5)enemies(p,target.position(),2).stream().filter(e->e!=target).limit(mastery==2?5:2).forEach(e->frost(p,e,1,20));}
            case WATER_VEIL->VEILS.put(p,new Mark(p,100+bonus*10,rank,mastery));
            case HEALING_SPRING->add(p,skill,origin,null,rank,100+bonus*10,mastery);
            case STATIC_CHARGE->{if(target==null)return false;STATIC.put(target,new Mark(p,MagicMastery.duration(skill,mastery,180+bonus*15),rank,mastery));target.addEffect(new MobEffectInstance(MobEffects.GLOWING,MagicMastery.duration(skill,mastery,180+bonus*15),0),p);if(rank==5)enemies(p,target.position(),3).stream().filter(e->e!=target).limit(mastery==2?3:1).forEach(e->{int ticks=mastery==1?300:mastery==2?120:180;STATIC.put(e,new Mark(p,ticks,rank,mastery));e.addEffect(new MobEffectInstance(MobEffects.GLOWING,ticks,0),p);});}
            case THUNDER_LEAP->{if(!p.onGround()||p.isPassenger()||!MagicCasting.move(p,MagicMastery.distance(skill,mastery,3+bonus*.25)))return false;p.setOnGround(false);p.setDeltaMovement(p.getDeltaMovement().add(0,.65,0));p.syncVelocity=true;add(p,skill,origin,null,rank,45,mastery);}
            case STORM_FIELD->{Vec3 at=target!=null?target.position():p.pick(12,1,false).getLocation();if(at.distanceToSqr(origin)>14*14||!p.level().hasChunkAt(BlockPos.containing(at))||enemies(p,at,MagicMastery.radius(skill,mastery,4)).stream().noneMatch(e->marked(p,e,false)))return false;add(p,skill,at,null,rank,100+bonus*10,mastery);}
            case TIME_ANCHOR->{var a=ANCHORS.get(p);if(a!=null&&a.level==p.level()&&a.until>p.level().getGameTime()){if(!teleport(p,a.at))return false;ANCHORS.remove(p);if(a.mastery==2)p.heal(4);if(rank==5){p.removeEffect(MobEffects.SLOWNESS);p.removeEffect(MobEffects.WEAKNESS);}}else{if(!safe(p,origin))return false;ANCHORS.put(p,new Anchor(p.level(),origin,p.level().getGameTime()+MagicMastery.duration(skill,mastery,240+bonus*20),mastery));}}
            case AFTERIMAGE->{if(!MagicCasting.move(p,MagicMastery.distance(skill,mastery,3+bonus*.25)))return false;add(p,skill,origin,null,rank,40+bonus*5,mastery);var field=FIELDS.getLast();var stand=new SpellAfterimage(p.level());stand.setPos(origin);stand.setInvisible(true);stand.setNoGravity(true);stand.setPermanentlyInvulnerable(true);stand.setSilent(true);stand.setYRot(p.getYRot());if(p.level().addFreshEntity(stand))field.decoy=stand;}
            case INERTIA_FIELD->add(p,skill,origin,null,rank,100+bonus*10,mastery);
            case STOLEN_MOMENT->{if(p.level().getGameTime()<MOMENT_READY.getOrDefault(p,Long.MIN_VALUE))return false;MOMENTS.put(p,new Moment(p,rank,mastery));MOMENT_READY.put(p,p.level().getGameTime()+240);}
            case VOID_PULL->{if(target==null)return false;Vec3 at=target.position();var list=enemies(p,at,MagicMastery.radius(skill,mastery,4+bonus*.25));if(list.isEmpty())return false;for(var e:list){if(boss(e))continue;Vec3 v=at.subtract(e.position()).multiply(1,0,1);if(v.lengthSqr()>.1){e.setDeltaMovement(e.getDeltaMovement().add(v.normalize().scale(.35+bonus*.025)));e.syncVelocity=true;}e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,30,rank==5&&!boss(e)?3:0),p);}if(mastery==2)for(var e:list)damage(p,e,4);add(p,skill,at,null,rank,30,mastery);}
            case DIMENSION_FOLD->{var hit=p.pick(12,1,false);if(!(hit instanceof BlockHitResult block)||block.getDirection()!=net.minecraft.core.Direction.UP)return false;Vec3 at=Vec3.atBottomCenterOf(block.getBlockPos().above());if(origin.distanceToSqr(at)<9||!safe(p,origin)||!safe(p,at))return false;add(p,skill,origin,at,rank,120+bonus*10,mastery);}
            default->{return false;}
        }
        return true;
    }
    public static int cooldown(ServerPlayer p,MagicSkill skill,int base){
        if(skill==MagicSkill.STOLEN_MOMENT)return 20;
        if(skill==MagicSkill.TIME_ANCHOR&&ANCHORS.containsKey(p))base=20;
        var m=MOMENTS.get(p);if(skill!=MagicSkill.STOLEN_MOMENT&&m!=null&&m.credit&&m.level==p.level()&&m.until>p.level().getGameTime()){MOMENTS.remove(p);return Math.max(10,Math.round(base*(m.mastery==2?.35f:m.rank==5?.5f:.65f)));}return base;
    }
    public static boolean chain(ServerPlayer p,LivingEntity aimed,int rank){return chain(p,aimed,rank,0);}
    public static boolean chain(ServerPlayer p,LivingEntity aimed,int rank,int mastery){
        LivingEntity current=aimed;
        if(current==null)current=enemies(p,p.position(),16).stream().filter(e->marked(p,e,false)).min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);
        if(current==null)return false;
        Set<LivingEntity> visited=new HashSet<>();Vec3 previous=p.getEyePosition();float amount=5+(rank-1)*.5f;
        int jumps=2+(rank-1)/2+(rank==5?1:0)+(mastery==1?2:0);
        for(int n=0;n<=jumps&&current!=null;n++){
            visited.add(current);Vec3 at=current.position().add(0,current.getBbHeight()*.5,0);
            current.hurtServer(p.level(),p.level().damageSources().lightningBolt(),amount+(marked(p,current,false)?1:0));SpellEffects.line(p.level(),ParticleTypes.ELECTRIC_SPARK,previous,at);
            LivingEntity from=current;
            current=enemies(p,from.position(),4).stream().filter(e->!visited.contains(e)&&from.hasLineOfSight(e)).min(Comparator.<LivingEntity>comparingInt(e->marked(p,e,false)?0:1).thenComparingDouble(from::distanceToSqr)).orElse(null);
            previous=at;amount*=mastery==2?.9f:.8f;
        }
        return true;
    }
    public static void perfectDodge(ServerPlayer p){
        var m=MOMENTS.get(p);if(m==null||m.credit||m.level!=p.level()||m.until<=p.level().getGameTime()||!p.isAlive())return;
        m.credit=true;MOMENT_READY.put(p,p.level().getGameTime()+240);SpellEffects.burst(p.level(),ParticleTypes.ENCHANT,p.getEyePosition(),18);
    }
    public static void fireHit(ServerPlayer p,LivingEntity e){var m=EMBERS.get(e);if(m==null||m.owner!=p||!live(m,e))return;if(++m.charges>=(m.mastery==1?1:m.level==5?2:3))detonate(p,e,4+m.level*.5f);}
    private static void detonate(ServerPlayer p,LivingEntity e,float amount){EMBERS.remove(e);damage(p,e,amount);SpellEffects.burst(p.level(),ParticleTypes.FLAME,e.position().add(0,.7,0),20);p.level().playSound(null,e.blockPosition(),net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,net.minecraft.sounds.SoundSource.PLAYERS,.5f,.8f);}
    private static void afterDamage(LivingDamageEvent.Post event){if(event.getHealthDamage()>0&&event.getSource().is(DamageTypeTags.IS_FIRE)&&event.getSource().getEntity() instanceof ServerPlayer p)fireHit(p,event.getEntity());}
    private static void veil(LivingDamageEvent.Pre event){if(!(event.getEntity() instanceof ServerPlayer p)||event.getNewDamage()<=0||event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;var m=VEILS.get(p);if(m==null||!live(m,p))return;VEILS.remove(p);event.setNewDamage(event.getNewDamage()*(m.mastery==1?.2f:m.level==5?.35f:.5f));SpellEffects.burst(p.level(),ParticleTypes.SPLASH,p.getEyePosition(),20);if(m.level==5)p.clearFire();if(m.mastery==2&&event.getSource().getEntity() instanceof LivingEntity attacker&&hostile(attacker,p))attacker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,80,boss(attacker)?0:2),p);}
    public static boolean safe(ServerPlayer p,Vec3 at){
        var l=p.level();var box=p.getBoundingBox().move(at.subtract(p.position()));if(box.minY<l.getMinY()||box.maxY>=l.getMaxY()||!l.getWorldBorder().isWithinBounds(box))return false;
        for(var pos:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY-.08,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){if(!l.hasChunkAt(pos))return false;var s=l.getBlockState(pos);if(!s.getFluidState().isEmpty()||s.is(BlockTags.FIRE)||s.is(BlockTags.PORTALS)||s.is(Blocks.MAGMA_BLOCK)||s.is(Blocks.CACTUS)||s.is(Blocks.POWDER_SNOW)||s.is(Blocks.WITHER_ROSE)||s.is(Blocks.SWEET_BERRY_BUSH)||s.is(BlockTags.CAMPFIRES)&&s.getValue(net.minecraft.world.level.block.CampfireBlock.LIT))return false;}
        return l.noCollision(p,box)&&l.getBlockCollisions(p,box.move(0,-.08,0)).iterator().hasNext();
    }
    public static boolean teleport(ServerPlayer p,Vec3 at){if(!p.isAlive()||p.isSpectator()||p.isPassenger()||p.isVehicle()||!safe(p,at))return false;var origin=p.position();var level=p.level();var e=new net.neoforged.neoforge.event.entity.EntityTeleportEvent(p,level,at.x,at.y,at.z);NeoForge.EVENT_BUS.post(e);if(e.isCanceled()||e.getTarget().distanceToSqr(at)>1e-8||p.level()!=level||!origin.equals(p.position())||!safe(p,at))return false;p.teleportTo(at.x,at.y,at.z);p.setDeltaMovement(Vec3.ZERO);p.resetFallDistance();return true;}
    private static void redirect(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event){
        if(event.isCanceled()||!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer p)||!p.isAlive()||boss(event.getEntity())||!hostile(event.getEntity(),p))return;
        for(var f:FIELDS)if(f.skill==MagicSkill.AFTERIMAGE&&f.owner==p&&f.level==event.getEntity().level()&&f.until>f.level.getGameTime()&&f.decoy!=null&&f.decoy.isAlive()&&event.getEntity().distanceToSqr(f.decoy)<(f.rank==5?64:36)&&event.getEntity().hasLineOfSight(f.decoy)){event.setNewAboutToBeSetTarget(f.decoy);return;}
    }
    private static void dispose(Field f){if(f.decoy!=null){for(var mob:f.level.getEntitiesOfClass(Mob.class,f.decoy.getBoundingBox().inflate(16),m->m.getTarget()==f.decoy))mob.setTarget(f.owner.isAlive()&&f.owner.level()==f.level?f.owner:null);f.decoy.discard();}}
    private static boolean friend(Player other,ServerPlayer p){return other==p||other.isAlliedTo(p);}
    public static void tick(ServerLevel level){
        long now=level.getGameTime();
        EMBERS.entrySet().removeIf(e->e.getKey().level()==level&&!live(e.getValue(),e.getKey()));STATIC.entrySet().removeIf(e->e.getKey().level()==level&&!live(e.getValue(),e.getKey()));VEILS.entrySet().removeIf(e->e.getKey().level()==level&&!live(e.getValue(),e.getKey()));
        ANCHORS.entrySet().removeIf(e->e.getKey().level()!=e.getValue().level||!e.getKey().isAlive()||e.getKey().hasDisconnected()||e.getValue().level==level&&now>=e.getValue().until);
        MOMENTS.entrySet().removeIf(e->e.getKey().level()!=e.getValue().level||!e.getKey().isAlive()||e.getKey().hasDisconnected()||e.getValue().level==level&&now>=e.getValue().until);
        if(now%10==0){for(var entry:EMBERS.entrySet())if(entry.getKey().level()==level)SpellEffects.burst(level,ParticleTypes.SMALL_FLAME,entry.getKey().position().add(0,1,0),3);for(var entry:STATIC.entrySet())if(entry.getKey().level()==level)SpellEffects.burst(level,ParticleTypes.ELECTRIC_SPARK,entry.getKey().position().add(0,1,0),3);for(var a:ANCHORS.values())if(a.level==level)SpellEffects.ring(level,ParticleTypes.ENCHANT,a.at.add(0,.1,0),.6);}
        for(var it=FIELDS.iterator();it.hasNext();){var f=it.next();if(f.level!=level)continue;if(now>=f.until||!f.owner.isAlive()||f.owner.hasDisconnected()||f.owner.level()!=level||!level.hasChunkAt(BlockPos.containing(f.at))){if(now>=f.until&&f.skill==MagicSkill.AFTERIMAGE&&f.mastery==2&&f.owner.isAlive()&&!f.owner.hasDisconnected()&&f.owner.level()==level)for(var e:enemies(f.owner,f.at,4))e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,80,boss(e)?0:2),f.owner);dispose(f);it.remove();continue;}
            long age=now-f.start;var p=f.owner;int rank=f.rank,mastery=f.mastery;double radius=MagicMastery.radius(f.skill,mastery,f.skill==MagicSkill.HEALING_SPRING?2.5:4);
            switch(f.skill){
                case ASH_STEP->{if(age%10==0){SpellEffects.line(level,ParticleTypes.SMALL_FLAME,f.at.add(0,.1,0),f.end.add(0,.1,0));for(var e:enemies(p,f.at,6)){Vec3 a=e.position().subtract(f.at),b=f.end.subtract(f.at);double t=Math.clamp(a.dot(b)/Math.max(.001,b.lengthSqr()),0,1);if(e.position().distanceToSqr(f.at.add(b.scale(t)))<(mastery==2?2.25:1)){e.igniteForSeconds(2);damage(p,e,1);fireHit(p,e);if(rank==5)e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,20,0),p);}}}}
                case HEALING_SPRING->{if(age%10==0)SpellEffects.ring(level,ParticleTypes.FALLING_WATER,f.at.add(0,.15,0),radius);if(age%20==0)for(var other:level.getEntitiesOfClass(Player.class,new AABB(f.at,f.at).inflate(radius),e->e.isAlive()&&!e.isSpectator()&&friend(e,p)&&e.position().distanceToSqr(f.at)<=radius*radius&&p.hasLineOfSight(e))){other.heal(.75f+(rank-1)*.125f+(mastery==2?.5f:0));other.clearFire();if(rank==5&&age==20){if(other.hasEffect(MobEffects.POISON))other.removeEffect(MobEffects.POISON);else other.removeEffect(MobEffects.WEAKNESS);}SpellEffects.burst(level,ParticleTypes.HAPPY_VILLAGER,other.position().add(0,.8,0),3);}}
                case STORM_FIELD->{if(age%10==0)SpellEffects.ring(level,ParticleTypes.ELECTRIC_SPARK,f.at.add(0,.1,0),radius);if(age>=20&&age%20==0)for(var e:enemies(p,f.at,radius))if(marked(p,e,false)){SpellEffects.line(level,ParticleTypes.ELECTRIC_SPARK,e.position().add(0,4,0),e.position());damage(p,e,1.5f+(rank-1)*.25f+(mastery==2?1:0));if(rank==5)enemies(p,e.position(),2).stream().filter(n->n!=e&&!marked(p,n,false)).limit(1).forEach(n->{damage(p,n,1);SpellEffects.line(level,ParticleTypes.ELECTRIC_SPARK,e.getEyePosition(),n.getEyePosition());});}}
                case THUNDER_LEAP->{if(age>3&&p.onGround()){for(var e:enemies(p,p.position(),rank==5?3.5:2.5)){damage(p,e,3+(rank-1)*.5f+(mastery==2?2:0));Vec3 v=e.position().subtract(p.position());e.knockback(mastery==2?.9:.5,-v.x,-v.z,level.damageSources().indirectMagic(p,p),0);}p.resetFallDistance();SpellEffects.ring(level,ParticleTypes.ELECTRIC_SPARK,p.position().add(0,.2,0),rank==5?3.5:2.5);it.remove();}}
                case AFTERIMAGE->{if(age%5==0){for(int h=0;h<4;h++)SpellEffects.ring(level,new DustParticleOptions(0xb09be2,.7f),f.at.add(0,.3+h*.4,0),h==3?.18:.28);if(f.decoy!=null)for(var e:enemies(p,f.at,rank==5?8:6))if(e instanceof Mob mob&&!boss(e)&&mob.getTarget()==p&&mob.hasLineOfSight(f.decoy))mob.setTarget(f.decoy);}}
                case INERTIA_FIELD->{if(age%10==0){SpellEffects.ring(level,new DustParticleOptions(0x9a7bd3,1),f.at.add(0,.1,0),radius);for(var e:enemies(p,f.at,radius))e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,15,boss(e)?0:mastery==2?3:1),p);}for(var shot:level.getEntitiesOfClass(Projectile.class,new AABB(f.at,f.at).inflate(radius),e->e.position().distanceToSqr(f.at)<=radius*radius&&e.getOwner() instanceof LivingEntity owner&&hostile(owner,p)))if(shot.getDeltaMovement().lengthSqr()>.0025)shot.setDeltaMovement(shot.getDeltaMovement().scale(mastery==2?.55:rank==5?.7:.85));}
                case VOID_PULL->{if(age%5==0)SpellEffects.ring(level,ParticleTypes.REVERSE_PORTAL,f.at.add(0,.3,0),1.2*(1-age/30.0));}
                case DIMENSION_FOLD->{if(age%5==0)for(var at:List.of(f.at,f.end)){for(int n=0;n<20;n++){double a=n*Math.PI/10;level.sendParticles(ParticleTypes.PORTAL,at.x+Math.cos(a)*.55,at.y+1+Math.sin(a)*.9,at.z,1,0,0,0,0);}}for(var other:level.players()){if(!other.isAlive()||other.isSpectator()||!other.isShiftKeyDown()||now<f.ready.getOrDefault(other.getUUID(),Long.MIN_VALUE))continue;Vec3 dest=other.position().distanceToSqr(f.at)<1?f.end:other.position().distanceToSqr(f.end)<1?f.at:null;if(dest!=null&&teleport(other,dest)){f.ready.put(other.getUUID(),now+30);if(rank==5)other.addEffect(new MobEffectInstance(MobEffects.SPEED,40,0),p);if(mastery==2){other.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,0,false,false,true),p);other.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,60,0,false,false,true),p);}SpellEffects.burst(level,ParticleTypes.REVERSE_PORTAL,other.getEyePosition(),12);}}}
                default->{}
            }
        }
    }
    public static void init(){var bus=NeoForge.EVENT_BUS;bus.addListener(MagicExpansion::veil);bus.addListener(MagicExpansion::afterDamage);bus.addListener(MagicExpansion::redirect);bus.addListener((net.neoforged.neoforge.event.tick.LevelTickEvent.Post e)->{if(e.getLevel() instanceof ServerLevel l)tick(l);});bus.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->{for(var f:FIELDS)dispose(f);FIELDS.clear();EMBERS.clear();STATIC.clear();VEILS.clear();ANCHORS.clear();MOMENTS.clear();MOMENT_READY.clear();});}
}
