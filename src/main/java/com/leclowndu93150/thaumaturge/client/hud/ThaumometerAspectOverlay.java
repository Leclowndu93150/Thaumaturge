package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ThaumometerAspectOverlay {
    private static final float TAG_SCALE_CAP = 0.5F;
    private static final float TAG_SCALE_GROWTH = 0.031F;
    private static final float TAG_SCALE_DECAY_DIVISOR = 10.0F;
    private static final float SPACE_ABOVE_LIFT = 0.4F;
    private static final float TAG_ALPHA = 0.85F;

    private static float tagScale;
    private static @Nullable Object lastTarget;

    private ThaumometerAspectOverlay() {}

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        boolean beforeTranslucentBlocks = event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES;
        if (!beforeTranslucentBlocks && event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!holdsThaumometer(mc.player) || mc.options.getCameraType().isFirstPerson()) {
            resetAnimation();
            return;
        }
        HitResult result = ScanRaycastHelper.performRaycast(mc.player, ClipContext.Fluid.SOURCE_ONLY);
        if (result instanceof EntityHitResult entityResult) {
            if (beforeTranslucentBlocks) {
                return;
            }
            Entity entityTarget = entityResult.getEntity();
            AspectList aspects =
                    scannedAspects(mc.player, EntityAspects.of(entityTarget), ScanKeys.entity(entityTarget.getType()));
            if (aspects.isEmpty()) {
                resetAnimation();
                return;
            }
            advanceAnimation(entityTarget);
            Vec3 pos = entityTarget.position();
            drawTags(
                    event.getPoseStack(),
                    mc,
                    pos.x - 0.5,
                    pos.y + entityTarget.getBbHeight() + SPACE_ABOVE_LIFT,
                    pos.z - 0.5,
                    aspects,
                    Direction.UP);
            return;
        }
        if (!(result instanceof BlockHitResult hit) || result.getType() != HitResult.Type.BLOCK) {
            resetAnimation();
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (mc.level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            AspectList nodeAspects = scannedAspects(mc.player, node.getAspects(), ScanNode.researchKey(mc.level, pos));
            if (nodeAspects.isEmpty()) {
                resetAnimation();
                return;
            }
            advanceAnimation(pos.immutable());
            AspectReadoutPlacement.Placement placement = AspectReadoutPlacement.find(
                    mc.level,
                    pos,
                    mc.player.getDirection().getOpposite(),
                    true,
                    mc.gameRenderer.getMainCamera().getPosition());
            drawPlacedTags(
                    event.getPoseStack(),
                    mc,
                    pos,
                    placement,
                    nodeAspects,
                    tagScale * 4.0F,
                    false,
                    beforeTranslucentBlocks);
            return;
        }
        if (mc.level.getBlockEntity(pos) instanceof BlockEntityEssentiaReservoir reservoir) {
            AspectList stored = scannedAspects(
                    mc.player,
                    reservoir.contents(),
                    ScanKeys.item(reservoir.getBlockState().getBlock().asItem()));
            if (stored.isEmpty()) {
                resetAnimation();
                return;
            }
            advanceAnimation(pos.immutable());
            boolean topVisible = AspectReadoutPlacement.viewerAbove(mc.level, pos, mc.player.getEyePosition());
            AspectReadoutPlacement.Placement placement = AspectReadoutPlacement.find(
                    mc.level,
                    pos,
                    mc.player.getDirection().getOpposite(),
                    topVisible,
                    mc.gameRenderer.getMainCamera().getPosition());
            drawPlacedTags(event.getPoseStack(), mc, pos, placement, stored, 1.2F, true, beforeTranslucentBlocks);
            return;
        }
        ItemStack pick = ScanningManager.stackOf(mc.player, ScanTarget.block(pos));
        if (pick.isEmpty()) {
            resetAnimation();
            return;
        }
        AspectList aspects =
                scannedAspects(mc.player, AspectIndexAccess.index().of(pick), ScanKeys.item(pick.getItem()));
        if (aspects.isEmpty()) {
            resetAnimation();
            return;
        }
        advanceAnimation(pos.immutable());
        boolean topFree = AspectReadoutPlacement.canHost(mc.level.getBlockState(pos.above()));
        boolean topVisible = AspectReadoutPlacement.viewerAbove(mc.level, pos, mc.player.getEyePosition());
        AspectReadoutPlacement.Placement placement = AspectReadoutPlacement.find(
                mc.level,
                pos,
                mc.player.getDirection().getOpposite(),
                topFree && topVisible,
                mc.gameRenderer.getMainCamera().getPosition());
        if (placement == null) {
            resetAnimation();
            return;
        }
        if (placement.renderThroughGlass() != beforeTranslucentBlocks) {
            return;
        }
        drawPlacedTags(
                event.getPoseStack(), mc, pos, placement, aspects, tagScale * 4.0F, false, beforeTranslucentBlocks);
    }

    private static boolean holdsThaumometer(Player player) {
        return player.getMainHandItem().is(TTItems.THAUMOMETER.get())
                || player.getOffhandItem().is(TTItems.THAUMOMETER.get());
    }

    private static AspectList scannedAspects(Player player, AspectList aspects, ResourceLocation scanKey) {
        if (aspects.isEmpty() || !PlayerResearchAccess.of(player).knows(scanKey)) {
            return AspectList.EMPTY;
        }
        return aspects;
    }

    private static void advanceAnimation(Object target) {
        if (!target.equals(lastTarget)) {
            tagScale = 0.0F;
            lastTarget = target;
        }
        if (tagScale < TAG_SCALE_CAP) {
            tagScale = tagScale + (TAG_SCALE_GROWTH - tagScale / TAG_SCALE_DECAY_DIVISOR);
        }
    }

    private static void resetAnimation() {
        tagScale = 0.0F;
        lastTarget = null;
    }

    private static void drawTags(
            PoseStack poseStack, Minecraft mc, double x, double y, double z, AspectList aspects, Direction dir) {
        drawTags(poseStack, mc, x, y, z, aspects, dir, tagScale * 4.0F, false);
    }

    private static void drawTags(
            PoseStack poseStack,
            Minecraft mc,
            double x,
            double y,
            double z,
            AspectList aspects,
            Direction dir,
            float columnSpacing,
            boolean centerHorizontally) {
        Player player = mc.player;
        AspectTagWorldRenderer.renderTagCloud(
                poseStack,
                mc,
                x,
                y,
                z,
                aspects,
                dir,
                tagScale,
                TAG_ALPHA,
                aspect -> player != null && AspectPools.isDiscovered(player, aspect),
                true,
                0.0F,
                columnSpacing,
                centerHorizontally);
    }

    private static void drawPlacedTags(
            PoseStack poseStack,
            Minecraft mc,
            BlockPos origin,
            AspectReadoutPlacement.Placement placement,
            AspectList aspects,
            float columnSpacing,
            boolean centerHorizontally,
            boolean beforeTranslucentBlocks) {
        if (placement == null) {
            resetAnimation();
            return;
        }
        if (placement.renderThroughGlass() != beforeTranslucentBlocks) {
            return;
        }
        BlockPos diagonal = placement.diagonal();
        if (diagonal != null) {
            drawTags(
                    poseStack,
                    mc,
                    diagonal.getX() + 0.5,
                    diagonal.getY(),
                    diagonal.getZ() + 0.5,
                    aspects,
                    null,
                    columnSpacing,
                    centerHorizontally);
            return;
        }
        Direction side = placement.side();
        BlockEntity target = mc.level == null ? null : mc.level.getBlockEntity(origin);
        double y = origin.getY()
                + (side == Direction.UP ? SPACE_ABOVE_LIFT : side.getStepY() / 2.0)
                + (target == null ? 0.0F : AspectReadoutPlacement.topReadoutVerticalAdjustment(target, side));
        drawTags(
                poseStack,
                mc,
                origin.getX() + side.getStepX() / 2.0,
                y,
                origin.getZ() + side.getStepZ() / 2.0,
                aspects,
                side,
                columnSpacing,
                centerHorizontally);
    }
}
