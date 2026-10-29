package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GogglesWorldOverlay {
    private static final float TAG_SCALE_CAP = 0.3F;
    private static final double TAG_SCALE_GROW_TICKS = 10.0D;
    private static final float SPACE_ABOVE_LIFT = 0.4F;
    private static final float JAR_READOUT_LIFT = 0.25F;
    private static final float TAG_ALPHA = 0.75F;

    private static @Nullable BlockPos lastTarget;
    private static double animationStartTime;

    record AspectReadout(AspectList aspects, boolean showAmounts) {}

    private GogglesWorldOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!GogglesAccess.wearsRevealingGear(mc.player)) {
            resetAnimation();
            return;
        }
        if (holdsThaumometer(mc.player)) {
            resetAnimation();
            return;
        }
        if (!(mc.hitResult instanceof BlockHitResult hit) || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            resetAnimation();
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (be instanceof BlockEntityNode node
                && !PlayerResearchAccess.of(mc.player).knows(ScanNode.researchKey(mc.level, pos))) {
            resetAnimation();
            return;
        }
        AspectReadout readout = readAspects(mc.level, be, pos, hit.getDirection());
        AspectList aspects = readout.aspects();
        if (aspects.isEmpty()) {
            resetAnimation();
            return;
        }
        double now = mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float scale = advanceAnimation(pos, now);

        boolean spaceAbove = mc.level.getBlockState(pos.above()).isAir();
        Direction dir = spaceAbove ? Direction.UP : mc.player.getDirection().getOpposite();
        if (!spaceAbove && (be instanceof AbstractBlockEntityJar || be instanceof BlockEntityEssentiaReservoir)) {
            dir = AspectReadoutPlacement.visibleSide(mc.level, pos, dir);
        }
        double y = pos.getY() + (spaceAbove ? SPACE_ABOVE_LIFT : 0.0F);
        if (be instanceof AbstractBlockEntityJar && dir == Direction.UP) {
            y += JAR_READOUT_LIFT - scale * 2.0F;
        }
        drawTags(
                event.getPoseStack(),
                mc,
                pos.getX(),
                y,
                pos.getZ(),
                aspects,
                dir,
                scale,
                readout.showAmounts(),
                be instanceof AbstractBlockEntityJar ? scale * 0.4F : 0.0F,
                be instanceof BlockEntityEssentiaReservoir ? 1.2F : scale * 4.0F,
                be instanceof BlockEntityEssentiaReservoir);
    }

    static AspectReadout readAspects(Level level, @Nullable BlockEntity blockEntity, BlockPos pos, Direction side) {
        if (blockEntity instanceof BlockEntityEssentiaReservoir reservoir) {
            return new AspectReadout(reservoir.contents(), true);
        }
        if (blockEntity instanceof AbstractBlockEntityJar jar) {
            return new AspectReadout(jar.getAspects(), true);
        }
        if (blockEntity instanceof BlockEntityInfusionMatrix matrix) {
            return new AspectReadout(matrix.remainingEssentia(), true);
        }
        IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, pos, side);
        if (container != null) {
            return new AspectReadout(container.getAspects(), true);
        }
        IAspectQuery query = level.getCapability(EssentiaCapabilities.ASPECT_QUERY, pos, side);
        return query == null
                ? new AspectReadout(AspectList.EMPTY, false)
                : new AspectReadout(query.queryAspects(), false);
    }

    static float advanceAnimation(BlockPos target, double now) {
        if (!target.equals(lastTarget)) {
            lastTarget = target.immutable();
            animationStartTime = now;
        }
        float progress = (float) Mth.clamp((now - animationStartTime) / TAG_SCALE_GROW_TICKS, 0.0D, 1.0D);
        float remainder = 1.0F - progress;
        return TAG_SCALE_CAP * (1.0F - remainder * remainder * remainder);
    }

    private static boolean holdsThaumometer(Player player) {
        return player.getMainHandItem().is(TTItems.THAUMOMETER.get())
                || player.getOffhandItem().is(TTItems.THAUMOMETER.get());
    }

    private static void resetAnimation() {
        lastTarget = null;
        animationStartTime = 0.0D;
    }

    private static void drawTags(
            PoseStack poseStack,
            Minecraft mc,
            double x,
            double y,
            double z,
            AspectList aspects,
            Direction dir,
            float scale,
            boolean showAmounts,
            float iconOffsetY,
            float columnSpacing,
            boolean centerHorizontally) {
        AspectTagWorldRenderer.renderTagCloud(
                poseStack,
                mc,
                x,
                y,
                z,
                aspects,
                dir,
                scale,
                TAG_ALPHA,
                aspect -> true,
                showAmounts,
                iconOffsetY,
                columnSpacing,
                centerHorizontally);
    }
}
