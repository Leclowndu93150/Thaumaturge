package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanNode;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
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
    public static void onRender(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!holdsThaumometer(mc.player)) {
            resetAnimation();
            return;
        }
        if (mc.options.getCameraType().isFirstPerson() && mc.player.getMainHandItem().is(TCItems.THAUMOMETER.get()) && mc.player.getOffhandItem().isEmpty()) {
            resetAnimation();
            return;
        }
        HitResult result = ScanRaycastHelper.performRaycast(mc.player, ClipContext.Fluid.SOURCE_ONLY);
        if (result instanceof EntityHitResult entityResult) {
            Entity entityTarget = entityResult.getEntity();
            AspectList aspects = scannedAspects(mc.player, EntityAspects.of(entityTarget), ScanKeys.entity(entityTarget.getType()));
            if (aspects.isEmpty()) {
                resetAnimation();
                return;
            }
            advanceAnimation(entityTarget);
            Vec3 pos = entityTarget.position();
            drawTags(event.getPoseStack(), mc, pos.x - 0.5, pos.y + entityTarget.getBbHeight() + SPACE_ABOVE_LIFT, pos.z - 0.5, aspects, Direction.UP);
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
            drawTags(event.getPoseStack(), mc, pos.getX(), pos.getY() + SPACE_ABOVE_LIFT, pos.getZ(), nodeAspects, Direction.UP);
            return;
        }
        BlockState state = mc.level.getBlockState(pos);
        ItemStack pick = new ItemStack(state.getBlock().asItem());
        if (pick.isEmpty()) {
            resetAnimation();
            return;
        }
        AspectList aspects = scannedAspects(mc.player, AspectIndexAccess.index().of(pick), ScanKeys.item(pick.getItem()));
        if (aspects.isEmpty()) {
            resetAnimation();
            return;
        }
        advanceAnimation(pos.immutable());
        boolean topFree = mc.level.getBlockState(pos.above()).isAir();
        boolean topVisible = mc.player.getEyeY() > pos.getY() + 1.0;
        Direction dir = topFree && topVisible ? Direction.UP : hit.getDirection();
        drawTags(event.getPoseStack(), mc, pos.getX() + dir.getStepX() / 2.0, pos.getY() + dir.getStepY() / 2.0, pos.getZ() + dir.getStepZ() / 2.0, aspects, dir);
    }

    private static boolean holdsThaumometer(Player player) {
        return player.getMainHandItem().is(TCItems.THAUMOMETER.get()) || player.getOffhandItem().is(TCItems.THAUMOMETER.get());
    }

    private static AspectList scannedAspects(Player player, AspectList aspects, Identifier scanKey) {
        if (aspects.isEmpty() || !KnowledgeAccess.of(player).isResearchKnown(scanKey)) {
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

    private static void drawTags(PoseStack poseStack, Minecraft mc, double x, double y, double z, AspectList aspects, Direction dir) {
        Player player = mc.player;
        AspectTagWorldRenderer.renderTagCloud(poseStack, mc, x, y, z, aspects, dir, tagScale, TAG_ALPHA, aspect -> player != null && AspectPools.isDiscovered(player, aspect));
    }
}
