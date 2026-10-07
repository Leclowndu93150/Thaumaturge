package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.hierophant.HierophantGeometry;
import com.leclowndu93150.thaumaturge.client.entity.hierophant.HierophantHammerRenderer;
import com.leclowndu93150.thaumaturge.client.entity.hierophant.HierophantRenderer;
import com.leclowndu93150.thaumaturge.client.entity.hierophant.HierophantSpellRenderer;
import com.leclowndu93150.thaumaturge.client.entity.taint.TaintSporeRenderer;
import com.leclowndu93150.thaumaturge.client.entity.taint.TaintSporeSwarmerRenderer;
import com.leclowndu93150.thaumaturge.client.golem.GolemDartRenderer;
import com.leclowndu93150.thaumaturge.client.golem.GolemRenderer;
import com.leclowndu93150.thaumaturge.client.model.entity.ArcaneBoreModel;
import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.CrossbowModel;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchCrabModel;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGolemModel;
import com.leclowndu93150.thaumaturge.client.model.entity.EldritchGuardianModel;
import com.leclowndu93150.thaumaturge.client.model.entity.FocusMineModel;
import com.leclowndu93150.thaumaturge.client.model.entity.GrapplerModel;
import com.leclowndu93150.thaumaturge.client.model.entity.JarBrineModel;
import com.leclowndu93150.thaumaturge.client.model.entity.ManaPodModel;
import com.leclowndu93150.thaumaturge.client.model.entity.MatrixCubeModel;
import com.leclowndu93150.thaumaturge.client.model.entity.PechModel;
import com.leclowndu93150.thaumaturge.client.model.entity.ResearchTableModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TTBannerModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSeedModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeSwarmerModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintacleModel;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.PraetorArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.ThaumostaticHarnessModel;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTEntityRenderers {
    private static final float TAINTACLE_SHADOW = 0.6F;
    private static final float TAINTACLE_GIANT_SHADOW = 1.0F;
    private static final float TAINTACLE_SMALL_SHADOW = 0.2F;
    private static final float TAINT_SEED_SHADOW = 0.4F;
    private static final float TAINT_SEED_PRIME_SHADOW = 0.6F;

    private TTEntityRenderers() {}

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TTModelLayers.CULTIST, () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 32));
        event.registerLayerDefinition(TTModelLayers.ELDRITCH_HIEROPHANT, HierophantGeometry::createBodyLayer);
        event.registerLayerDefinition(TTModelLayers.TAINTACLE, TaintacleModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.ELDRITCH_GOLEM, EldritchGolemModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.TAINT_SEED, TaintSeedModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.TAINT_SPORE, TaintSporeModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.TAINT_SPORE_SWARMER, TaintSporeSwarmerModel::createShellLayer);
        event.registerLayerDefinition(TTModelLayers.TAINT_SPORE_SWARMER_CORE, TaintSporeSwarmerModel::createCoreLayer);
        event.registerLayerDefinition(TTModelLayers.FIRE_BAT, BatModel::createBodyLayer);
        event.registerLayerDefinition(TTModelLayers.TC_BANNER, TTBannerModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.BRAIN, BrainModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.JAR_BRINE, JarBrineModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.MATRIX_CUBE, MatrixCubeModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.PECH, PechModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.ELDRITCH_CRAB, EldritchCrabModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.ELDRITCH_GUARDIAN, EldritchGuardianModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.RESEARCH_TABLE, ResearchTableModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.DECONSTRUCTION_TABLE, DeconTableModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.MANA_POD, ManaPodModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.PRAETOR_ARMOR_HEAD, PraetorArmorModel::createHead);
        event.registerLayerDefinition(TTModelLayers.PRAETOR_ARMOR_CHEST, PraetorArmorModel::createChest);
        event.registerLayerDefinition(TTModelLayers.PRAETOR_ARMOR_LEGS, PraetorArmorModel::createLegs);
        event.registerLayerDefinition(TTModelLayers.KNIGHT_ARMOR_HEAD, KnightArmorModel::createHead);
        event.registerLayerDefinition(TTModelLayers.KNIGHT_ARMOR_CHEST, KnightArmorModel::createChest);
        event.registerLayerDefinition(TTModelLayers.KNIGHT_ARMOR_LEGS, KnightArmorModel::createLegs);
        event.registerLayerDefinition(TTModelLayers.FORTRESS_ARMOR_HEAD, FortressArmorModel::createHead);
        event.registerLayerDefinition(TTModelLayers.FORTRESS_ARMOR_CHEST, FortressArmorModel::createChest);
        event.registerLayerDefinition(TTModelLayers.FORTRESS_ARMOR_LEGS, FortressArmorModel::createLegs);
        event.registerLayerDefinition(TTModelLayers.ROBE_ARMOR_HEAD, RobeArmorModel::createHead);
        event.registerLayerDefinition(TTModelLayers.ROBE_ARMOR_CHEST, RobeArmorModel::createChest);
        event.registerLayerDefinition(TTModelLayers.ROBE_ARMOR_LEGS, RobeArmorModel::createLegs);
        event.registerLayerDefinition(TTModelLayers.THAUMOSTATIC_HARNESS, ThaumostaticHarnessModel::createChest);
        event.registerLayerDefinition(TTModelLayers.TURRET_CROSSBOW, CrossbowModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.ARCANE_BORE, ArcaneBoreModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.GRAPPLER, GrapplerModel::createLayer);
        event.registerLayerDefinition(TTModelLayers.FOCUS_MINE, FocusMineModel::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(TTEntities.HIEROPHANT_HAMMER.get(), HierophantHammerRenderer::new);
        event.registerEntityRenderer(TTEntities.ELDRITCH_HIEROPHANT.get(), HierophantRenderer::new);
        event.registerEntityRenderer(TTEntities.HIEROPHANT_CRESCENT.get(), context -> new HierophantSpellRenderer<>(context, HierophantSpellRenderer.Shape.CRESCENT));
        event.registerEntityRenderer(TTEntities.HIEROPHANT_SIGIL.get(), context -> new HierophantSpellRenderer<>(context, HierophantSpellRenderer.Shape.SIGIL));
        event.registerEntityRenderer(TTEntities.HIEROPHANT_NOVA.get(), context -> new HierophantSpellRenderer<>(context, HierophantSpellRenderer.Shape.NOVA));
        event.registerEntityRenderer(TTEntities.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(TTEntities.FLUX_RIFT.get(), FluxRiftRenderer::new);
        event.registerEntityRenderer(TTEntities.CAUSALITY_COLLAPSER.get(), NoModelRenderer::new);
        event.registerEntityRenderer(TTEntities.BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(TTEntities.GIANT_BRAINY_ZOMBIE.get(), BrainyZombieRenderer::new);
        event.registerEntityRenderer(TTEntities.BRAINY_DROWNED.get(), BrainyDrownedRenderer::new);
        event.registerEntityRenderer(TTEntities.BRAINY_HUSK.get(), BrainyHuskRenderer::new);
        event.registerEntityRenderer(TTEntities.FIRE_BAT.get(), FireBatRenderer::new);
        event.registerEntityRenderer(TTEntities.MIND_SPIDER.get(), MindSpiderRenderer::new);
        event.registerEntityRenderer(TTEntities.THAUMIC_SLIME.get(), ThaumicSlimeRenderer::new);
        event.registerEntityRenderer(TTEntities.TAINT_CRAWLER.get(), TaintCrawlerRenderer::new);
        event.registerEntityRenderer(TTEntities.TAINT_SPORE.get(), TaintSporeRenderer::new);
        event.registerEntityRenderer(TTEntities.TAINT_SPORE_SWARMER.get(), TaintSporeSwarmerRenderer::new);
        event.registerEntityRenderer(TTEntities.TAINT_SEED.get(), context -> new TaintSeedRenderer(context, TAINT_SEED_SHADOW));
        event.registerEntityRenderer(TTEntities.TAINT_SEED_PRIME.get(), context -> new TaintSeedRenderer(context, TAINT_SEED_PRIME_SHADOW));
        event.registerEntityRenderer(TTEntities.TAINT_SWARM.get(), TaintSwarmRenderer::new);
        event.registerEntityRenderer(TTEntities.TAINTACLE.get(), context -> new TaintacleRenderer(context, TAINTACLE_SHADOW));
        event.registerEntityRenderer(TTEntities.TAINTACLE_SMALL.get(), context -> new TaintacleRenderer(context, TAINTACLE_SMALL_SHADOW));
        event.registerEntityRenderer(TTEntities.FOCUS_PROJECTILE.get(), FocusProjectileRenderer::new);
        event.registerEntityRenderer(TTEntities.FOCUS_CLOUD.get(), NoModelRenderer::new);
        event.registerEntityRenderer(TTEntities.FOCUS_MINE.get(), FocusMineRenderer::new);
        event.registerEntityRenderer(TTEntities.SPELL_BAT.get(), SpellBatRenderer::new);
        event.registerEntityRenderer(TTEntities.FALLING_TAINT.get(), FallingTaintRenderer::new);
        event.registerEntityRenderer(TTEntities.BOTTLE_TAINT.get(), BottleTaintRenderer::new);
        event.registerEntityRenderer(TTEntities.SPECIAL_ITEM.get(), SpecialItemRenderer::new);
        event.registerEntityRenderer(TTEntities.FOLLOWING_ITEM.get(), ItemEntityRenderer::new);
        event.registerEntityRenderer(TTEntities.ALUMENTUM.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(TTEntities.THAUMATURGE_GOLEM.get(), GolemRenderer::new);
        event.registerEntityRenderer(TTEntities.GOLEM_DART.get(), GolemDartRenderer::new);
        event.registerEntityRenderer(TTEntities.PECH.get(), PechRenderer::new);
        event.registerEntityRenderer(TTEntities.ELDRITCH_CRAB.get(), EldritchCrabRenderer::new);
        event.registerEntityRenderer(TTEntities.INHABITED_ZOMBIE.get(), InhabitedZombieRenderer::new);
        event.registerEntityRenderer(TTEntities.ELDRITCH_GUARDIAN.get(), EldritchGuardianRenderer::new);
        event.registerEntityRenderer(TTEntities.CULTIST_LEADER.get(), CultistLeaderRenderer::new);
        event.registerEntityRenderer(TTEntities.CULTIST_PORTAL_GREATER.get(), CultistPortalGreaterRenderer::new);
        event.registerEntityRenderer(TTEntities.ELDRITCH_GOLEM.get(), EldritchGolemRenderer::new);
        event.registerEntityRenderer(TTEntities.ELDRITCH_WARDEN.get(), EldritchWardenRenderer::new);
        event.registerEntityRenderer(TTEntities.TAINTACLE_GIANT.get(), context -> new TaintacleRenderer(context, TAINTACLE_GIANT_SHADOW));
        event.registerEntityRenderer(TTEntities.CULTIST_KNIGHT.get(), CultistRenderer::new);
        event.registerEntityRenderer(TTEntities.CULTIST_CLERIC.get(), CultistClericRenderer::new);
        event.registerEntityRenderer(TTEntities.CULTIST_PORTAL_LESSER.get(), CultistPortalRenderer::new);
        event.registerEntityRenderer(TTEntities.ELDRITCH_ORB.get(), EldritchOrbRenderer::new);
        event.registerEntityRenderer(TTEntities.GOLEM_ORB.get(), GolemOrbRenderer::new);
        event.registerEntityRenderer(TTEntities.ASPECT_ORB.get(), AspectOrbRenderer::new);
        event.registerEntityRenderer(TTEntities.TURRET_CROSSBOW.get(), TurretCrossbowRenderer::new);
        event.registerEntityRenderer(TTEntities.TURRET_CROSSBOW_ADVANCED.get(), TurretCrossbowAdvancedRenderer::new);
        event.registerEntityRenderer(TTEntities.ARCANE_BORE.get(), ArcaneBoreRenderer::new);
        event.registerEntityRenderer(TTEntities.GRAPPLE.get(), GrappleRenderer::new);
    }
}
