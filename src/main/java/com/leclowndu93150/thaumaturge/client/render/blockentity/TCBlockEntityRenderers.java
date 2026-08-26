package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchAltar;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCBlockEntityRenderers {
    private static final float RECHARGE_PEDESTAL_ITEM_SCALE = 1.5F;

    private TCBlockEntityRenderers() {}

    @SubscribeEvent
    public static void onRegister(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TCBlockEntities.INFUSION_MATRIX.get(), InfusionMatrixRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.VIS_RELAY.get(), VisRelayRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.FOCAL_MANIPULATOR.get(), FocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PEDESTAL.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(
                TCBlockEntities.RECHARGE_PEDESTAL.get(),
                context -> new RechargePedestalRenderer(context, RECHARGE_PEDESTAL_ITEM_SCALE));
        event.registerBlockEntityRenderer(TCBlockEntities.JAR.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_VOID.get(), JarRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_BRAIN.get(), JarBrainRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.DIOPTRA.get(), DioptraRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CENTRIFUGE.get(), CentrifugeRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.GOLEM_BUILDER.get(), GolemBuilderRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.HUNGRY_CHEST.get(), HungryChestRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ALEMBIC.get(), AlembicRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.BANNER.get(), BannerRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.BELLOWS.get(), BellowsRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.TUBE_VALVE.get(), TubeValveRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ELDRITCH_OBELISK.get(), EldritchObeliskRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.RESEARCH_TABLE.get(), ResearchTableRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.DECONSTRUCTION_TABLE.get(), DeconstructionTableRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MANA_POD.get(), ManaPodRenderer::new);
        event.registerBlockEntityRenderer(
                TCBlockEntities.ELDRITCH_CAP.get(),
                context -> new EldritchCapRenderer<>(
                        context, EldritchCapRenderer.CAP_TEXTURE, EldritchCapRenderer.CAP_TEXTURE_OUTER, cap -> 0));
        event.registerBlockEntityRenderer(
                TCBlockEntities.ELDRITCH_ALTAR.get(),
                context -> new EldritchCapRenderer<>(
                        context,
                        EldritchCapRenderer.ALTAR_TEXTURE,
                        EldritchCapRenderer.ALTAR_TEXTURE,
                        BlockEntityEldritchAltar::getEyes));
        event.registerBlockEntityRenderer(TCBlockEntities.ELDRITCH_PORTAL.get(), EldritchPortalRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ELDRITCH_NOTHING.get(), EldritchNothingRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.ELDRITCH_LOCK.get(), EldritchLockRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.PATTERN_CRAFTER.get(), PatternCrafterRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.NODE.get(), NodeRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.JAR_NODE.get(), NodeRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.NODE_STABILIZER.get(), NodeStabilizerRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.NODE_TRANSDUCER.get(), NodeTransducerRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MIRROR.get(), MirrorRenderer::new);
        event.registerBlockEntityRenderer(TCBlockEntities.MIRROR_ESSENTIA.get(), MirrorRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (ModelResourceLocation modelId : BellowsRenderer.MODEL_IDS) {
            event.register(modelId);
        }
        event.register(MirrorRenderer.FRAME_MODEL_ID);
        event.register(MirrorRenderer.FRAME_ESSENTIA_MODEL_ID);
        event.register(TubeValveRenderer.MODEL_ID);
    }
}
