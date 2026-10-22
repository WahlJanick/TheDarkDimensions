package at.janic.thedarkdimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class ExplorationContent {
    public static final ResourceKey<Structure> SANCTUM=ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id("chronus_sanctum"));
    public static final ResourceKey<Structure> WATER_VAULT=ResourceKey.create(Registries.STRUCTURE,TheDarkDimensions.id("sunken_archive"));
    public static final DeferredItem<Item> CHRONUS_FRAGMENT=TheDarkDimensions.ITEMS.registerSimpleItem("chronus_fragment",p->p.rarity(Rarity.RARE));
    public static final DeferredItem<Item> HOURGLASS=TheDarkDimensions.ITEMS.registerSimpleItem("timeworn_hourglass",p->p.stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<Bestiary.Item> BESTIARY=TheDarkDimensions.ITEMS.registerItem("ashbound_bestiary",Bestiary.Item::new,p->p.stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final DeferredHolder<EntityType<?>,EntityType<SanctumKeeper>> KEEPER=TheDarkDimensions.ENTITIES.register("sanctum_keeper",()->EntityType.Builder.of(SanctumKeeper::new,MobCategory.MONSTER).sized(.6f,1.95f).clientTrackingRange(10).build(ResourceKey.create(Registries.ENTITY_TYPE,TheDarkDimensions.id("sanctum_keeper"))));
    public static final DeferredItem<SpawnEggItem> KEEPER_EGG=TheDarkDimensions.ITEMS.registerItem("sanctum_keeper_spawn_egg",SpawnEggItem::new,p->p.spawnEgg(KEEPER.get()));
    public static void init(IEventBus bus){
        Bestiary.init();
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(KEEPER.get(),net.minecraft.world.entity.monster.Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,44).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.MOVEMENT_SPEED,.24).add(Attributes.FOLLOW_RANGE,24).build()));
    }
    public static final class SanctumKeeper extends RealmMobs.ChronusMage {
        private int fieldCooldown=120,windup;
        private net.minecraft.world.phys.Vec3 field;
        public SanctumKeeper(EntityType<? extends SanctumKeeper> type,Level level){super(type,level);xpReward=25;}
        @Override protected void customServerAiStep(ServerLevel level){
            super.customServerAiStep(level);if(fieldCooldown>0)fieldCooldown--;
            var target=getTarget();
            if(fieldCooldown==0&&target!=null&&distanceToSqr(target)<144&&getSensing().hasLineOfSight(target)){
                field=target.position();windup=25;fieldCooldown=160;
                playSound(net.minecraft.sounds.SoundEvents.EVOKER_PREPARE_SUMMON,.6f,.7f);
            }
            if(windup>0&&field!=null){
                if(windup%5==0)for(int i=0;i<12;i++){
                    double a=i*Math.PI/6;level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,field.x+Math.cos(a)*2,field.y+.3,field.z+Math.sin(a)*2,1,0,0,0,0);
                }
                if(--windup==0){
                    for(var victim:level.getEntitiesOfClass(LivingEntity.class,new net.minecraft.world.phys.AABB(field,field).inflate(2,2,2))){
                        if(victim==this||victim instanceof RealmMobs.ChronusMage||victim instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator()))continue;
                        if(victim.position().subtract(field).multiply(1,0,1).lengthSqr()>4)continue;
                        if(getSensing().hasLineOfSight(victim))victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,60,1),this);
                    }
                    field=null;
                }
            }
        }
    }
    public static void award(net.minecraft.server.level.ServerPlayer player,String path){
        var advancement=player.level().getServer().getAdvancements().get(TheDarkDimensions.id("exploration/"+path));
        if(advancement!=null)player.getAdvancements().award(advancement,"done");
    }
}
