package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemDartRenderer;
import com.leclowndu93150.thaumaturge.client.golem.GolemRenderer;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.CentrifugeModel;
import com.leclowndu93150.thaumaturge.client.model.entity.CrossbowModel;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchCrabModel;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.client.model.entity.GrapplerModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.client.model.entity.ManaPodModel;
import com.leclowndu93150.thaumaturge.client.model.entity.MatrixCubeModel;
import com.leclowndu93150.thaumaturge.client.model.entity.PechModel;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TCBannerModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSeedModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintacleModel;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCEntityRenderers {
    private static final float TAINTACLE_SHADOW = 0.6F;
    private static final float TAINTACLE_GIANT_SHADOW = 1.0F;
    private static final int TAINTACLE_GIANT_LENGTH = 14;
    private static final float TAINTACLE_SMALL_SHADOW = 0.2F;
    private static final float TAINT_SEED_SHADOW = 0.4F;
    private static final float TAINT_SEED_PRIME_SHADOW = 0.6F;

    private TCEntityRenderers() {}

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TCModelLayers.TAINTACLE, () -> TaintacleModel.createLayer(TaintacleModel.TAINTACLE_LENGTH));
        event.registerLayerDefinition(TCModelLayers.TAINTACLE_GIANT, () -> TaintacleModel.createLayer(TAINTACLE_GIANT_LENGTH));
        event.registerLayerDefinition(TCModelLayers.ELDRITCH_GOLEM, EldritchGolemModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.TAINTACLE_SMALL, () -> TaintacleModel.createLayer(TaintacleModel.TAINTACLE_SMALL_LENGTH));
        event.registerLayerDefinition(TCModelLayers.TAINT_SEED, TaintSeedModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.FIRE_BAT, BatModel::createBodyLayer);
        event.registerLayerDefinition(TCModelLayers.TC_BANNER, TCBannerModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.BRAIN, BrainModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.JAR_BRINE, JarBrineModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.CENTRIFUGE, CentrifugeModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.MATRIX_CUBE, MatrixCubeModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.PECH, PechModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.ELDRITCH_CRAB, EldritchCrabModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.ELDRITCH_GUARDIAN, EldritchGuardianModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.RESEARCH_TABLE, ResearchTableModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.DECONSTRUCTION_TABLE, DeconTableModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.MANA_POD, ManaPodModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.KNIGHT_ARMOR_HEAD, KnightArmorModel::createHead);
        event.registerLayerDefinition(TCModelLayers.KNIGHT_ARMOR_CHEST, KnightArmorModel::createChest);
        event.registerLayerDefinition(TCModelLayers.KNIGHT_ARMOR_LEGS, KnightArmorModel::createLegs);
        event.registerLayerDefinition(TCModelLayers.FORTRESS_ARMOR_HEAD, FortressArmorModel::createHead);
        event.registerLayerDefinition(TCModelLayers.FORTRESS_ARMOR_CHEST, FortressArmorModel::createChest);
        event.registerLayerDefinition(TCModelLayers.FORTRESS_ARMOR_LEGS, FortressArmorModel::createLegs);
        event.registerLayerDefinition(TCModelLayers.ROBE_ARMOR_HEAD, RobeArmorModel::createHead);
        event.registerLayerDefinition(TCModelLayers.ROBE_ARMOR_CHEST, RobeArmorModel::createChest);
        event.registerLayerDefinition(TCModelLayers.ROBE_ARMOR_LEGS, RobeArmorModel::createLegs);
        event.registerLayerDefinition(TCModelLayers.TURRET_CROSSBOW, CrossbowModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.ARCANE_BORE, ArcaneBoreModel::createLayer);
        event.registerLayerDefinition(TCModelLayers.GRAPPLER, GrapplerModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TCEntities.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(TCEntities.FLUX_RIFT.get(), FluxRiftRenderer::new);
        event.registerEntityRenderer(TCEntities.CAUSALITY_COLLAPSER.get(), NoModelRenderer::new);
        event.registerEntityRenderer(TCEntities.BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(TCEntities.GIANT_BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(TCEntities.BRAINY_DROWNED.get(), BrainyDrownedRenderer::new);
        event.registerEntityRenderer(TCEntities.BRAINY_HUSK.get(), BrainyHuskRenderer::new);
        event.registerEntityRenderer(TCEntities.FIRE_BAT.get(), FireBatRenderer::new);
        event.registerEntityRenderer(TCEntities.MIND_SPIDER.get(), MindSpiderRenderer::new);
        event.registerEntityRenderer(TCEntities.THAUMIC_SLIME.get(), ThaumicSlimeRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_CRAWLER.get(), TaintCrawlerRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINT_SEED.get(), context -> new TaintSeedRenderer(context, TAINT_SEED_SHADOW));
        event.registerEntityRenderer(TCEntities.TAINT_SEED_PRIME.get(), context -> new TaintSeedRenderer(context, TAINT_SEED_PRIME_SHADOW));
        event.registerEntityRenderer(TCEntities.TAINT_SWARM.get(), TaintSwarmRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINTACLE.get(), context -> new TaintacleRenderer(context, TCModelLayers.TAINTACLE, TaintacleModel.TAINTACLE_LENGTH, TAINTACLE_SHADOW));
        event.registerEntityRenderer(TCEntities.TAINTACLE_SMALL.get(),
                context -> new TaintacleRenderer(context, TCModelLayers.TAINTACLE_SMALL, TaintacleModel.TAINTACLE_SMALL_LENGTH, TAINTACLE_SMALL_SHADOW));
        event.registerEntityRenderer(TCEntities.FOCUS_PROJECTILE.get(), FocusProjectileRenderer::new);
        event.registerEntityRenderer(TCEntities.FOCUS_CLOUD.get(), NoModelRenderer::new);
        event.registerEntityRenderer(TCEntities.FOCUS_MINE.get(), FocusMineRenderer::new);
        event.registerEntityRenderer(TCEntities.SPELL_BAT.get(), SpellBatRenderer::new);
        event.registerEntityRenderer(TCEntities.FALLING_TAINT.get(), FallingTaintRenderer::new);
        event.registerEntityRenderer(TCEntities.BOTTLE_TAINT.get(), BottleTaintRenderer::new);
        event.registerEntityRenderer(TCEntities.SPECIAL_ITEM.get(), SpecialItemRenderer::new);
        event.registerEntityRenderer(TCEntities.FOLLOWING_ITEM.get(), ItemEntityRenderer::new);
        event.registerEntityRenderer(TCEntities.ALUMENTUM.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(TCEntities.THAUMATURGE_GOLEM.get(), GolemRenderer::new);
        event.registerEntityRenderer(TCEntities.GOLEM_DART.get(), GolemDartRenderer::new);
        event.registerEntityRenderer(TCEntities.PECH.get(), PechRenderer::new);
        event.registerEntityRenderer(TCEntities.ELDRITCH_CRAB.get(), EldritchCrabRenderer::new);
        event.registerEntityRenderer(TCEntities.INHABITED_ZOMBIE.get(), InhabitedZombieRenderer::new);
        event.registerEntityRenderer(TCEntities.ELDRITCH_GUARDIAN.get(), EldritchGuardianRenderer::new);
        event.registerEntityRenderer(TCEntities.CULTIST_LEADER.get(), CultistLeaderRenderer::new);
        event.registerEntityRenderer(TCEntities.CULTIST_PORTAL_GREATER.get(), CultistPortalGreaterRenderer::new);
        event.registerEntityRenderer(TCEntities.ELDRITCH_GOLEM.get(), EldritchGolemRenderer::new);
        event.registerEntityRenderer(TCEntities.ELDRITCH_WARDEN.get(), EldritchWardenRenderer::new);
        event.registerEntityRenderer(TCEntities.TAINTACLE_GIANT.get(), context -> new TaintacleRenderer(context, TCModelLayers.TAINTACLE_GIANT, TAINTACLE_GIANT_LENGTH, TAINTACLE_GIANT_SHADOW));
        event.registerEntityRenderer(TCEntities.CULTIST_KNIGHT.get(), CultistRenderer::new);
        event.registerEntityRenderer(TCEntities.CULTIST_CLERIC.get(), CultistClericRenderer::new);
        event.registerEntityRenderer(TCEntities.CULTIST_PORTAL_LESSER.get(), CultistPortalRenderer::new);
        event.registerEntityRenderer(TCEntities.ELDRITCH_ORB.get(), EldritchOrbRenderer::new);
        event.registerEntityRenderer(TCEntities.GOLEM_ORB.get(), GolemOrbRenderer::new);
        event.registerEntityRenderer(TCEntities.ASPECT_ORB.get(), AspectOrbRenderer::new);
        event.registerEntityRenderer(TCEntities.TURRET_CROSSBOW.get(), TurretCrossbowRenderer::new);
        event.registerEntityRenderer(TCEntities.TURRET_CROSSBOW_ADVANCED.get(), TurretCrossbowAdvancedRenderer::new);
        event.registerEntityRenderer(TCEntities.ARCANE_BORE.get(), ArcaneBoreRenderer::new);
        event.registerEntityRenderer(TCEntities.GRAPPLE.get(), GrappleRenderer::new);
    }
}
