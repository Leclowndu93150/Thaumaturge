package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.client.render.BoxGeometry;
import com.leclowndu93150.thaumaturge.client.render.TCFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class WandItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    public record WandArg(WandCap cap, WandRod rod, boolean sceptre, boolean hasFocus, int focusColor) {}

    private static final ResourceLocation WAND_TEXTURE = TCIds.rl("textures/models/wand.png");
    private static final ResourceLocation SCRIPT_TEXTURE = TCIds.rl("textures/misc/script.png");

    private static final RenderType RUNES = TCRenderTypes.entityAdditiveEmissive(SCRIPT_TEXTURE);

    private static final float PX = 0.0625F;
    private static final int TEX_W = 32;
    private static final int TEX_H = 32;
    private static final int RUNE_LIGHT = 200;
    private static final float MODEL_LIFT = 0.5F;
    private static final float FOCUS_ALPHA = 0.95F;
    private static final int SCEPTRE_RUNE_COUNT = 10;
    private static final int STAFF_RUNE_SIDES = 4;
    private static final int STAFF_RUNE_LENGTH = 14;
    private static final int SCRIPT_GLYPHS = 16;
    private static final float STAFF_MODEL_SHIFT = 0.2F;
    private static final float FOCUS_STAFF_LIFT = -0.0475F;
    private static final float FOCUS_STAFF_SCALE_Y = 0.5525F;
    private static final float FOCUS_SCALE = 0.5F;
    private static final float FOCUS_TOP_PX = 6.0F;
    private static final float CAP_TOP_PX = 1.0F;
    private static final float CAP_STAFF_SCALE_Y = 1.1F;
    private static final float SCEPTRE_CAP_SCALE = 1.3F;

    public WandItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        WandArg arg = extract(stack);
        poseStack.pushPose();
        poseStack.translate(0.5F, MODEL_LIFT, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        submitParts(arg, poseStack, buffers, light);
        poseStack.popPose();
    }

    private record CapPlacement(float offsetY, float scaleXZ, float scaleY, float pivotPx) {}

    private record RuneSpot(float yaw, float distance, float height, float depth, int glyph) {}

    private static final float SCEPTRE_RUNE_DISTANCE = 0.16F;
    private static final float SCEPTRE_RUNE_DEPTH = -0.125F;
    private static final float STAFF_RUNE_START = 0.36F;
    private static final float STAFF_RUNE_STEP = 0.14F;
    private static final float STAFF_RUNE_DEPTH = -0.08F;
    private static final float RUNE_HEIGHT = -0.01F;
    private static final float CAP_PIVOT_BOTTOM_PX = 20.0F;

    public static void submitParts(WandArg arg, PoseStack poseStack, MultiBufferSource buffers, int light) {
        boolean staff = arg.rod().staff();
        float ticks = clientTicks();

        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, STAFF_MODEL_SHIFT, 0.0F);
        }
        submitRod(arg, poseStack, buffers, light, staff, ticks);
        submitCaps(arg, poseStack, buffers, light, staff);
        if (arg.hasFocus()) {
            submitFocus(arg, poseStack, buffers, staff, ticks);
        }
        for (RuneSpot spot : runeLayout(arg, ticks)) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(spot.yaw()));
            submitRune(poseStack, buffers, spot, ticks);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitRod(
            WandArg arg, PoseStack poseStack, MultiBufferSource buffers, int light, boolean staff, float ticks) {
        int rodLight = arg.rod().glow() ? (int) (200.0F + Mth.sin((int) ticks) * 5.0F + 5.0F) : light;
        RenderType rodType = TCFlatRenderTypes.entityCutoutFlat(arg.rod().texture());
        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
            poseStack.scale(1.2F, 2.0F, 1.2F);
        }
        box(poseStack.last(), buffers.getBuffer(rodType), -1.0F, 1.0F, -1.0F, 2, 18, 2, 0, 8, 0xFFFFFFFF, rodLight);
        poseStack.popPose();
    }

    private static List<CapPlacement> capLayout(boolean staff, boolean sceptre) {
        List<CapPlacement> layout = new ArrayList<>();
        if (sceptre) {
            layout.add(new CapPlacement(0.0F, SCEPTRE_CAP_SCALE, SCEPTRE_CAP_SCALE, 0.0F));
            layout.add(new CapPlacement(0.3F, 1.0F, 0.66F, 0.0F));
        } else {
            layout.add(new CapPlacement(0.0F, 1.0F, 1.0F, 0.0F));
        }
        if (staff) {
            layout.add(new CapPlacement(0.225F, 1.0F, 0.66F, 0.0F));
            layout.add(new CapPlacement(0.875F, 1.0F, 1.0F, CAP_PIVOT_BOTTOM_PX));
        } else {
            layout.add(new CapPlacement(0.0F, 1.0F, 1.0F, CAP_PIVOT_BOTTOM_PX));
        }
        return layout;
    }

    private static void submitCaps(
            WandArg arg, PoseStack poseStack, MultiBufferSource buffers, int light, boolean staff) {
        RenderType capType = TCFlatRenderTypes.entityCutoutFlat(arg.cap().texture());
        poseStack.pushPose();
        if (staff) {
            poseStack.scale(1.3F, CAP_STAFF_SCALE_Y, 1.3F);
        } else {
            poseStack.scale(1.2F, 1.0F, 1.2F);
        }
        for (CapPlacement placement : capLayout(staff, arg.sceptre())) {
            poseStack.pushPose();
            poseStack.translate(0.0F, placement.offsetY(), 0.0F);
            poseStack.scale(placement.scaleXZ(), placement.scaleY(), placement.scaleXZ());
            poseStack.translate(0.0F, placement.pivotPx() * PX, 0.0F);
            box(poseStack.last(), buffers.getBuffer(capType), -1.0F, -1.0F, -1.0F, 2, 2, 2, 0, 0, 0xFFFFFFFF, light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitFocus(
            WandArg arg, PoseStack poseStack, MultiBufferSource buffers, boolean staff, float ticks) {
        RenderType focusType = TCFlatRenderTypes.entityTranslucentFlat(WAND_TEXTURE);
        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, FOCUS_STAFF_LIFT, 0.0F);
            poseStack.scale(0.525F, FOCUS_STAFF_SCALE_Y, 0.525F);
        } else {
            poseStack.scale(FOCUS_SCALE, FOCUS_SCALE, FOCUS_SCALE);
        }
        int tint = ARGB32.color((int) (FOCUS_ALPHA * 255.0F), arg.focusColor());
        int focusLight = (int) (195.0F + Mth.sin(ticks / 3.0F) * 10.0F + 10.0F);
        box(poseStack.last(), buffers.getBuffer(focusType), -3.0F, -6.0F, -3.0F, 6, 6, 6, 0, 0, tint, focusLight);
        poseStack.popPose();
    }

    private static List<RuneSpot> runeLayout(WandArg arg, float ticks) {
        List<RuneSpot> spots = new ArrayList<>();
        if (arg.sceptre()) {
            for (int i = 0; i < SCEPTRE_RUNE_COUNT; i++) {
                spots.add(new RuneSpot(
                        360.0F / SCEPTRE_RUNE_COUNT * i + ticks,
                        SCEPTRE_RUNE_DISTANCE,
                        RUNE_HEIGHT,
                        SCEPTRE_RUNE_DEPTH,
                        i));
            }
        }
        if (arg.rod().runes()) {
            for (int side = 0; side < STAFF_RUNE_SIDES; side++) {
                float yaw = 360.0F / STAFF_RUNE_SIDES * (side + 1);
                for (int step = 0; step < STAFF_RUNE_LENGTH; step++) {
                    spots.add(new RuneSpot(
                            yaw,
                            STAFF_RUNE_START + step * STAFF_RUNE_STEP,
                            RUNE_HEIGHT,
                            STAFF_RUNE_DEPTH,
                            (step + side * 3) % SCRIPT_GLYPHS));
                }
            }
        }
        return spots;
    }

    public static float tipModelY(WandArg arg) {
        boolean staff = arg.rod().staff();
        if (arg.hasFocus()) {
            if (staff) {
                return STAFF_MODEL_SHIFT + FOCUS_STAFF_LIFT - FOCUS_TOP_PX * PX * FOCUS_STAFF_SCALE_Y;
            }
            return -FOCUS_TOP_PX * PX * FOCUS_SCALE;
        }
        if (staff) {
            return STAFF_MODEL_SHIFT - CAP_TOP_PX * PX * CAP_STAFF_SCALE_Y;
        }
        if (arg.sceptre()) {
            return -CAP_TOP_PX * PX * SCEPTRE_CAP_SCALE;
        }
        return -CAP_TOP_PX * PX;
    }

    private static float clientTicks() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null
                ? 0.0F
                : player.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }

    private static final int[][] RUNE_QUAD_SIGNS = {{-1, 1}, {1, 1}, {1, -1}, {-1, -1}};

    private static float pulse(float phase, float period, float base) {
        return Mth.sin(phase / period) * 0.1F + base;
    }

    private static void submitRune(PoseStack poseStack, MultiBufferSource buffers, RuneSpot spot, float ticks) {
        float phase = ticks + spot.glyph() * 5;
        float red = Math.min(1.0F, pulse(phase, 5.0F, 0.88F));
        float green = Math.min(1.0F, pulse(phase, 7.0F, 0.63F));
        float wobble = Mth.sin(phase / 10.0F) * 0.2F;
        int tint = ARGB32.colorFromFloat(Math.min(1.0F, wobble + 0.6F), red, green, 0.2F);
        float glyphU = spot.glyph() / (float) SCRIPT_GLYPHS;
        float glyphWidth = 1.0F / SCRIPT_GLYPHS;
        float half = 0.06F + wobble / 40.0F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(spot.distance(), spot.height(), spot.depth());
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(RUNES);
        for (int[] corner : RUNE_QUAD_SIGNS) {
            float u = corner[1] > 0 ? glyphU + glyphWidth : glyphU;
            float v = corner[0] < 0 ? 1.0F : 0.0F;
            buffer.addVertex(pose, corner[0] * half, corner[1] * half, 0.0F)
                    .setColor(tint)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(RUNE_LIGHT)
                    .setNormal(pose, 0.0F, 0.0F, 1.0F);
        }
        poseStack.popPose();
    }

    private static void box(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x,
            float y,
            float z,
            int dx,
            int dy,
            int dz,
            int u,
            int v,
            int tint,
            int light) {
        BoxGeometry.box(
                pose,
                buffer,
                x * PX,
                y * PX,
                z * PX,
                (x + dx) * PX,
                (y + dy) * PX,
                (z + dz) * PX,
                u,
                v,
                dx,
                dy,
                dz,
                TEX_W,
                TEX_H,
                tint,
                light,
                true);
    }

    public static WandArg extract(ItemStack stack) {
        WandParts parts = WandVisHelper.getParts(stack);
        ItemStack focusStack = ItemStack.EMPTY;
        SocketedFocus template = stack.get(TCDataComponents.SOCKETED_FOCUS.get());
        if (template != null) {
            focusStack = template.focus();
        }
        boolean hasFocus = focusStack.getItem() instanceof ItemFocus;
        int color = hasFocus ? ItemFocus.getFocusColor(focusStack) : 0xFFFFFF;
        return new WandArg(parts.cap(), parts.rod(), parts.sceptre(), hasFocus, color);
    }
}
