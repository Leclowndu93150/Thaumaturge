package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.casters.WandTipTracker;
import com.leclowndu93150.thaumaturge.client.effect.LateWorldRenderQueue;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class FXManagerRegistry {
    private static final List<AbstractFXManager<?>> MANAGERS = List.of(
            BeamManager.INSTANCE,
            EssentiaStreamManager.INSTANCE,
            BoreVoidStreamManager.INSTANCE,
            BoreDigEffectManager.INSTANCE);

    private FXManagerRegistry() {}

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ClientLevel cl)) return;
        for (AbstractFXManager<?> m : MANAGERS) m.tickAll(cl);
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        var stage = event.getStage();
        if (stage != RenderLevelStageEvent.Stage.AFTER_PARTICLES && stage != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            PoseStack poseStack = event.getPoseStack();
            WandTipTracker.setWorldProjection(event.getProjectionMatrix());
            for (AbstractFXManager<?> manager : MANAGERS) {
                if (manager != EssentiaStreamManager.INSTANCE) {
                    manager.renderAll(poseStack, camera, partialTick);
                }
            }
        }
        if (Minecraft.useShaderTransparency()) {
            if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
                EssentiaStreamManager.INSTANCE.renderAll(event.getPoseStack(), camera, partialTick);
            }
        } else if (stage == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            // The main target now contains the clouds' color and depth.
            EssentiaStreamManager.INSTANCE.renderAll(LateWorldRenderQueue.renderPose(event), camera, partialTick);
        }
    }

    public static void clearAll() {
        for (AbstractFXManager<?> manager : MANAGERS) {
            manager.clear();
        }
    }
}
