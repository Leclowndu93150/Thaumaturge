package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistLeader;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchWarden;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityTaintacleGiant;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSpore;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTEntityEvents {
    private TTEntityEvents() {}

    @SubscribeEvent
    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(TTEntities.WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WispEntity::checkWispSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.BRAINY_HUSK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.BRAINY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityBrainyZombie::checkBrainyZombieSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.GIANT_BRAINY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.PECH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityPech::checkPechSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.ELDRITCH_CRAB.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.INHABITED_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityInhabitedZombie::checkInhabitedSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.ELDRITCH_GUARDIAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityEldritchGuardian::checkGuardianSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.CULTIST_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.CULTIST_CLERIC.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.FIRE_BAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityFireBat::checkFireBatSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.THAUMIC_SLIME.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ThaumicSlime::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TTEntities.TAINTACLE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractTaintacle::checkTaintacleSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(TTEntities.WISP.get(), WispEntity.createAttributes().build());
        event.put(TTEntities.CULTIST_LEADER.get(), EntityCultistLeader.createAttributes().build());
        event.put(TTEntities.CULTIST_PORTAL_GREATER.get(), EntityCultistPortalGreater.createAttributes().build());
        event.put(TTEntities.ELDRITCH_GOLEM.get(), EntityEldritchGolem.createAttributes().build());
        event.put(TTEntities.ELDRITCH_HIEROPHANT.get(), EntityEldritchHierophant.createAttributes().build());
        event.put(TTEntities.ELDRITCH_WARDEN.get(), EntityEldritchWarden.createAttributes().build());
        event.put(TTEntities.TAINTACLE_GIANT.get(), EntityTaintacleGiant.createAttributes().build());
        event.put(TTEntities.BRAINY_ZOMBIE.get(), EntityBrainyZombie.createAttributes().build());
        event.put(TTEntities.BRAINY_DROWNED.get(), EntityBrainyDrowned.createAttributes().build());
        event.put(TTEntities.BRAINY_HUSK.get(), EntityBrainyHusk.createAttributes().build());
        event.put(TTEntities.FIRE_BAT.get(), EntityFireBat.createAttributes().build());
        event.put(TTEntities.MIND_SPIDER.get(), EntityMindSpider.createAttributes().build());
        event.put(TTEntities.GIANT_BRAINY_ZOMBIE.get(), EntityGiantBrainyZombie.createAttributes().build());
        event.put(TTEntities.THAUMIC_SLIME.get(), ThaumicSlime.createAttributes().build());
        event.put(TTEntities.TAINT_CRAWLER.get(), EntityTaintCrawler.createAttributes().build());
        event.put(TTEntities.TAINT_SEED.get(), EntityTaintSeed.createAttributes().build());
        event.put(TTEntities.TAINT_SEED_PRIME.get(), EntityTaintSeedPrime.createAttributes().build());
        event.put(TTEntities.TAINT_SWARM.get(), EntityTaintSwarm.createAttributes().build());
        event.put(TTEntities.TAINT_SPORE.get(), EntityTaintSpore.createAttributes().build());
        event.put(TTEntities.TAINT_SPORE_SWARMER.get(), EntityTaintSporeSwarmer.createAttributes().build());
        event.put(TTEntities.TAINTACLE.get(), EntityTaintacle.createAttributes().build());
        event.put(TTEntities.TAINTACLE_SMALL.get(), EntityTaintacleSmall.createAttributes().build());
        event.put(TTEntities.SPELL_BAT.get(), EntitySpellBat.createAttributes().build());
        event.put(TTEntities.THAUMATURGE_GOLEM.get(), EntityThaumaturgeGolem.createAttributes().build());
        event.put(TTEntities.PECH.get(), EntityPech.createAttributes().build());
        event.put(TTEntities.ELDRITCH_CRAB.get(), EntityEldritchCrab.createAttributes().build());
        event.put(TTEntities.INHABITED_ZOMBIE.get(), EntityInhabitedZombie.createAttributes().build());
        event.put(TTEntities.ELDRITCH_GUARDIAN.get(), EntityEldritchGuardian.createAttributes().build());
        event.put(TTEntities.CULTIST_KNIGHT.get(), EntityCultistKnight.createAttributes().build());
        event.put(TTEntities.CULTIST_CLERIC.get(), EntityCultistCleric.createAttributes().build());
        event.put(TTEntities.CULTIST_PORTAL_LESSER.get(), EntityCultistPortalLesser.createAttributes().build());
        event.put(TTEntities.TURRET_CROSSBOW.get(), EntityTurretCrossbow.createAttributes().build());
        event.put(TTEntities.TURRET_CROSSBOW_ADVANCED.get(), EntityTurretCrossbowAdvanced.createAdvancedAttributes().build());
        event.put(TTEntities.ARCANE_BORE.get(), EntityArcaneBore.createAttributes().build());
    }
}
