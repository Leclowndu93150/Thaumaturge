package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTBlockEntityRenderers {
    private static final float RECHARGE_PEDESTAL_ITEM_SCALE = 1.5F;

    private TTBlockEntityRenderers() {}

    @SubscribeEvent
    public static void onRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TTBlockEntities.INFUSION_MATRIX.get(), InfusionMatrixRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.VIS_RELAY.get(), VisRelayRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.FOCAL_MANIPULATOR.get(), FocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.RECHARGE_PEDESTAL.get(), context -> new RechargePedestalRenderer(context, RECHARGE_PEDESTAL_ITEM_SCALE));
        event.registerBlockEntityRenderer(TTBlockEntities.JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.JAR_VOID.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.JAR_BRAIN.get(), JarBrainRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.DIOPTRA.get(), DioptraRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.CENTRIFUGE.get(), CentrifugeRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ESSENTIA_CRYSTALIZER.get(), EssentiaCrystalizerRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ESSENTIA_RESERVOIR.get(), EssentiaReservoirRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.FLUX_SCRUBBER.get(), FluxScrubberRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.GOLEM_BUILDER.get(), GolemBuilderRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(), AdvancedAlchemicalFurnaceRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.HUNGRY_CHEST.get(), HungryChestRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.BANNER.get(), BannerRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.BELLOWS.get(), BellowsRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.TUBE_VALVE.get(), TubeValveRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ELDRITCH_OBELISK.get(), EldritchObeliskRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.RESEARCH_TABLE.get(), ResearchTableRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.DECONSTRUCTION_TABLE.get(), DeconstructionTableRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.MANA_POD.get(), ManaPodRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ELDRITCH_CAP.get(),
                context -> new EldritchCapRenderer<>(context, EldritchObeliskRenderer.CAP_MODEL, EldritchCapRenderer.CAP_TEXTURE, EldritchCapRenderer.CAP_TEXTURE_OUTER, cap -> 0));
        event.registerBlockEntityRenderer(TTBlockEntities.ELDRITCH_ALTAR.get(), context -> new EldritchCapRenderer<>(context, EldritchCapRenderer.ALTAR_MODEL, EldritchCapRenderer.ALTAR_TEXTURE,
                EldritchCapRenderer.ALTAR_TEXTURE, BlockEntityEldritchAltar::getEyes));
        event.registerBlockEntityRenderer(TTBlockEntities.ELDRITCH_PORTAL.get(), EldritchPortalRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ELDRITCH_NOTHING.get(), EldritchNothingRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.HOLE.get(), HoleRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ELDRITCH_LOCK.get(), EldritchLockRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.PATTERN_CRAFTER.get(), PatternCrafterRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.NODE.get(), NodeRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.JAR_NODE.get(), NodeRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.NODE_TRANSDUCER.get(), NodeTransducerRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.ARCANE_BORE.get(), ArcaneBoreBlockRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.MIRROR.get(), MirrorRenderer::new);
        event.registerBlockEntityRenderer(TTBlockEntities.MIRROR_ESSENTIA.get(), MirrorRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterStandalone event) {
        for (int i = 0; i < BellowsRenderer.parts.length; i++) {
            String part = BellowsRenderer.parts[i];
            Identifier modelId = TTIds.rl("block/bellows/" + part);
            BellowsRenderer.MODEL_KEYS[i] = new StandaloneModelKey<>(modelId::toString);
            event.register(BellowsRenderer.MODEL_KEYS[i], SimpleUnbakedStandaloneModel.blockStateModel(modelId));
        }
        event.register(MirrorRenderer.FRAME_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(MirrorRenderer.FRAME_MODEL_ID));
        event.register(EssentiaCrystalizerRenderer.CRYSTAL_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(EssentiaCrystalizerRenderer.CRYSTAL_MODEL_ID));
        event.register(TubeValveRenderer.MODEL, SimpleUnbakedStandaloneModel.blockStateModel(TubeValveRenderer.MODEL_ID));
        event.register(CentrifugeRenderer.SPINNER_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(CentrifugeRenderer.SPINNER_MODEL_ID));
        event.register(MirrorRenderer.FRAME_ESSENTIA_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(MirrorRenderer.FRAME_ESSENTIA_MODEL_ID));
    }
}
