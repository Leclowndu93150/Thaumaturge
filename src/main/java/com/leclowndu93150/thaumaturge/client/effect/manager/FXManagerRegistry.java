package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.leclowndu93150.thaumaturge.TCIds;
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

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        for (AbstractFXManager<?> m : MANAGERS) m.renderAll(poseStack, camera, partialTick);
    }
}
