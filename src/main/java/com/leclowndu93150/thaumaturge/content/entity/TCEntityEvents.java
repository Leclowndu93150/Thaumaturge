package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistLeader;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchGolem;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchWarden;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityTaintacleGiant;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbowAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCEntityEvents {
    private TCEntityEvents() {}

    @SubscribeEvent
    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(TCEntities.WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WispEntity::checkWispSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.BRAINY_HUSK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.BRAINY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.GIANT_BRAINY_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.PECH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityPech::checkPechSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.ELDRITCH_CRAB.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.INHABITED_ZOMBIE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityInhabitedZombie::checkInhabitedSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.ELDRITCH_GUARDIAN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityEldritchGuardian::checkGuardianSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.CULTIST_KNIGHT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.CULTIST_CLERIC.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.FIRE_BAT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityFireBat::checkFireBatSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TCEntities.THAUMIC_SLIME.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ThaumicSlime::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(TCEntities.WISP.get(), WispEntity.createAttributes().build());
        event.put(TCEntities.CULTIST_LEADER.get(), EntityCultistLeader.createAttributes().build());
        event.put(TCEntities.CULTIST_PORTAL_GREATER.get(), EntityCultistPortalGreater.createAttributes().build());
        event.put(TCEntities.ELDRITCH_GOLEM.get(), EntityEldritchGolem.createAttributes().build());
        event.put(TCEntities.ELDRITCH_WARDEN.get(), EntityEldritchWarden.createAttributes().build());
        event.put(TCEntities.TAINTACLE_GIANT.get(), EntityTaintacleGiant.createAttributes().build());
        event.put(TCEntities.BRAINY_ZOMBIE.get(), EntityBrainyZombie.createAttributes().build());
        event.put(TCEntities.BRAINY_DROWNED.get(), EntityBrainyDrowned.createAttributes().build());
        event.put(TCEntities.BRAINY_HUSK.get(), EntityBrainyHusk.createAttributes().build());
        event.put(TCEntities.FIRE_BAT.get(), EntityFireBat.createAttributes().build());
        event.put(TCEntities.MIND_SPIDER.get(), EntityMindSpider.createAttributes().build());
        event.put(TCEntities.GIANT_BRAINY_ZOMBIE.get(), EntityGiantBrainyZombie.createAttributes().build());
        event.put(TCEntities.THAUMIC_SLIME.get(), ThaumicSlime.createAttributes().build());
        event.put(TCEntities.TAINT_CRAWLER.get(), EntityTaintCrawler.createAttributes().build());
        event.put(TCEntities.TAINT_SEED.get(), EntityTaintSeed.createAttributes().build());
        event.put(TCEntities.TAINT_SEED_PRIME.get(), EntityTaintSeedPrime.createAttributes().build());
        event.put(TCEntities.TAINT_SWARM.get(), EntityTaintSwarm.createAttributes().build());
        event.put(TCEntities.TAINTACLE.get(), EntityTaintacle.createAttributes().build());
        event.put(TCEntities.TAINTACLE_SMALL.get(), EntityTaintacleSmall.createAttributes().build());
        event.put(TCEntities.SPELL_BAT.get(), EntitySpellBat.createAttributes().build());
        event.put(TCEntities.THAUMATURGE_GOLEM.get(), EntityThaumaturgeGolem.createAttributes().build());
        event.put(TCEntities.PECH.get(), EntityPech.createAttributes().build());
        event.put(TCEntities.ELDRITCH_CRAB.get(), EntityEldritchCrab.createAttributes().build());
        event.put(TCEntities.INHABITED_ZOMBIE.get(), EntityInhabitedZombie.createAttributes().build());
        event.put(TCEntities.ELDRITCH_GUARDIAN.get(), EntityEldritchGuardian.createAttributes().build());
        event.put(TCEntities.CULTIST_KNIGHT.get(), EntityCultistKnight.createAttributes().build());
        event.put(TCEntities.CULTIST_CLERIC.get(), EntityCultistCleric.createAttributes().build());
        event.put(TCEntities.CULTIST_PORTAL_LESSER.get(), EntityCultistPortalLesser.createAttributes().build());
        event.put(TCEntities.TURRET_CROSSBOW.get(), EntityTurretCrossbow.createAttributes().build());
        event.put(TCEntities.TURRET_CROSSBOW_ADVANCED.get(), EntityTurretCrossbowAdvanced.createAdvancedAttributes().build());
        event.put(TCEntities.ARCANE_BORE.get(), EntityArcaneBore.createAttributes().build());
    }
}
