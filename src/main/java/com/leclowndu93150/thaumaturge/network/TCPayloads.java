package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.golem.GolemPressClientHandler;
import com.leclowndu93150.thaumaturge.client.golem.SealClientHandler;
import com.leclowndu93150.thaumaturge.client.network.AspectGainClientHandler;
import com.leclowndu93150.thaumaturge.client.network.AspectIndexClientHandler;
import com.leclowndu93150.thaumaturge.client.network.AuraSnapshotClientHandler;
import com.leclowndu93150.thaumaturge.client.network.BoreDigClientHandler;
import com.leclowndu93150.thaumaturge.client.network.FocusImpactClientHandler;
import com.leclowndu93150.thaumaturge.client.network.InfusionSourceClientHandler;
import com.leclowndu93150.thaumaturge.client.network.KnowledgeGainClientHandler;
import com.leclowndu93150.thaumaturge.client.network.OpenThaumonomiconHandler;
import com.leclowndu93150.thaumaturge.client.network.SpawnParticleClientHandler;
import com.leclowndu93150.thaumaturge.client.network.StreamEffectClientHandler;
import com.leclowndu93150.thaumaturge.client.network.TubeEventClientHandler;
import com.leclowndu93150.thaumaturge.client.network.WispZapClientHandler;
import com.leclowndu93150.thaumaturge.client.screen.ThaumatoriumClientHandler;
import com.leclowndu93150.thaumaturge.client.warding.WardClientHandler;
import com.leclowndu93150.thaumaturge.client.warp.WarpFXClientHandler;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundBoreDigPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundFocusImpactPayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundInfusionSourcePayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundSpawnParticlePayload;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCPayloads {
    private static final String VERSION = "2";

    private TCPayloads() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(
                ClientboundAspectIndexPayload.TYPE,
                ClientboundAspectIndexPayload.STREAM_CODEC,
                (payload, context) -> AspectIndexClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundOpenThaumonomiconPayload.TYPE,
                ClientboundOpenThaumonomiconPayload.STREAM_CODEC,
                (payload, context) -> OpenThaumonomiconHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundSealPayload.TYPE,
                ClientboundSealPayload.STREAM_CODEC,
                (payload, context) -> SealClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundGolemPressStuffPayload.TYPE,
                ClientboundGolemPressStuffPayload.STREAM_CODEC,
                (payload, context) -> GolemPressClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundThaumatoriumRecipesPayload.TYPE,
                ClientboundThaumatoriumRecipesPayload.STREAM_CODEC,
                ThaumatoriumClientHandler::handle);
        registrar.playToServer(
                ServerboundThaumatoriumTogglePayload.TYPE,
                ServerboundThaumatoriumTogglePayload.STREAM_CODEC,
                ServerboundThaumatoriumToggleHandler::handle);
        registrar.playToServer(
                ServerboundGolemPressPayload.TYPE,
                ServerboundGolemPressPayload.STREAM_CODEC,
                ServerboundGolemPressHandler::handle);
        registrar.playToServer(
                ServerboundCloudJumpPayload.TYPE,
                ServerboundCloudJumpPayload.STREAM_CODEC,
                ServerboundCloudJumpPayload::handle);
        registrar.playToServer(
                ServerboundAdvanceStagePayload.TYPE,
                ServerboundAdvanceStagePayload.STREAM_CODEC,
                ServerboundAdvanceStageHandler::handle);
        registrar.playToClient(
                ClientboundAspectGainPayload.TYPE,
                ClientboundAspectGainPayload.STREAM_CODEC,
                (payload, context) -> AspectGainClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundUpdateJEIAspectListPayload.TYPE,
                ClientboundUpdateJEIAspectListPayload.STREAM_CODEC,
                (payload, context) -> AspectGainClientHandler.handleJEISync(payload, context));
        registrar.playToServer(
                ServerboundRequestSyncAspectPoolPayload.TYPE,
                ServerboundRequestSyncAspectPoolPayload.STREAM_CODEC,
                ServerboundRequestSyncAspectPoolPayload::handle);
        registrar.playToServer(
                ServerboundInventoryScanPayload.TYPE,
                ServerboundInventoryScanPayload.STREAM_CODEC,
                ServerboundInventoryScanPayload::handle);
        registrar.playToServer(
                ServerboundObtainNotePayload.TYPE,
                ServerboundObtainNotePayload.STREAM_CODEC,
                ServerboundObtainNotePayload::handle);
        registrar.playToServer(
                ServerboundTablePlaceAspectPayload.TYPE,
                ServerboundTablePlaceAspectPayload.STREAM_CODEC,
                ServerboundTablePlaceAspectPayload::handle);
        registrar.playToServer(
                ServerboundTableCombinePayload.TYPE,
                ServerboundTableCombinePayload.STREAM_CODEC,
                ServerboundTableCombinePayload::handle);
        registrar.playToServer(
                ServerboundTableDuplicatePayload.TYPE,
                ServerboundTableDuplicatePayload.STREAM_CODEC,
                ServerboundTableDuplicatePayload::handle);
        registrar.playToServer(
                ServerboundDeconCollectPayload.TYPE,
                ServerboundDeconCollectPayload.STREAM_CODEC,
                ServerboundDeconCollectPayload::handle);
        registrar.playToServer(
                ServerboundClearResearchFlagsPayload.TYPE,
                ServerboundClearResearchFlagsPayload.STREAM_CODEC,
                ServerboundClearResearchFlagsHandler::handle);
        registrar.playToServer(
                ServerboundUnlockResearchPayload.TYPE,
                ServerboundUnlockResearchPayload.STREAM_CODEC,
                ServerboundUnlockResearchHandler::handle);
        registrar.playToClient(
                ClientboundSpawnParticlePayload.TYPE,
                ClientboundSpawnParticlePayload.STREAM_CODEC,
                (payload, context) -> SpawnParticleClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundBoreDigPayload.TYPE,
                ClientboundBoreDigPayload.STREAM_CODEC,
                (payload, context) -> BoreDigClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundTubeVentPayload.TYPE,
                ClientboundTubeVentPayload.STREAM_CODEC,
                (payload, context) -> TubeEventClientHandler.handleVent(payload, context));
        registrar.playToClient(
                ClientboundTubeCreakPayload.TYPE,
                ClientboundTubeCreakPayload.STREAM_CODEC,
                (payload, context) -> TubeEventClientHandler.handleCreak(payload, context));
        registrar.playToServer(
                ServerboundRequestAuraChunkPayload.TYPE,
                ServerboundRequestAuraChunkPayload.STREAM_CODEC,
                ServerboundRequestAuraChunkHandler::handle);
        registrar.playToClient(
                ClientboundWarpFXPayload.TYPE,
                ClientboundWarpFXPayload.STREAM_CODEC,
                (payload, context) -> WarpFXClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundAuraSnapshotPayload.TYPE,
                ClientboundAuraSnapshotPayload.STREAM_CODEC,
                (payload, context) -> AuraSnapshotClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundStreamEffectPayload.TYPE,
                ClientboundStreamEffectPayload.STREAM_CODEC,
                (payload, context) -> StreamEffectClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundInfusionSourcePayload.TYPE,
                ClientboundInfusionSourcePayload.STREAM_CODEC,
                (payload, context) -> InfusionSourceClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundKnowledgeGainPayload.TYPE,
                ClientboundKnowledgeGainPayload.STREAM_CODEC,
                (payload, context) -> KnowledgeGainClientHandler.handle(payload, context));
        registrar.playToClient(
                ClientboundWispZapPayload.TYPE,
                ClientboundWispZapPayload.STREAM_CODEC,
                (payload, context) -> WispZapClientHandler.handle(payload, context));
        registrar.playToServer(
                ServerboundFocusDataPayload.TYPE,
                ServerboundFocusDataPayload.STREAM_CODEC,
                ServerboundFocusDataPayload::handle);
        registrar.playToClient(
                ClientboundFocusImpactPayload.TYPE,
                ClientboundFocusImpactPayload.STREAM_CODEC,
                (payload, context) -> FocusImpactClientHandler.handle(payload, context));
        registrar.playToServer(
                ServerboundFocusChangePayload.TYPE,
                ServerboundFocusChangePayload.STREAM_CODEC,
                ServerboundFocusChangePayload::handle);
        registrar.playToServer(
                ServerboundCasterKeyPayload.TYPE,
                ServerboundCasterKeyPayload.STREAM_CODEC,
                ServerboundCasterKeyPayload::handle);
        registrar.playToClient(
                ClientboundWardChunkPayload.TYPE,
                ClientboundWardChunkPayload.STREAM_CODEC,
                (payload, context) -> WardClientHandler.handleChunk(payload, context));
        registrar.playToClient(
                ClientboundWardUpdatePayload.TYPE,
                ClientboundWardUpdatePayload.STREAM_CODEC,
                (payload, context) -> WardClientHandler.handleUpdate(payload, context));
    }
}
