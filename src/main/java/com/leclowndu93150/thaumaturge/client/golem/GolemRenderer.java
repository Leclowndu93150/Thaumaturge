package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderHook;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.GolemTrace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class GolemRenderer extends EntityRenderer<EntityThaumaturgeGolem> {
    private static final ResourceLocation FALLBACK_TEXTURE = GolemSkins.COPPER;
    private static final float GHOST_ALPHA = 0.15F;
    private static final int XRAY_COLOR = ARGB32.colorFromFloat(0.25F, 0.25F, 0.25F, 0.25F);

    private final GolemAccessoryRenderTable accessoryRenderers;
    private final CopperGolemRig model;
    // Weak keys ensure despawned entities never remain in diagnostic state.
    private final Map<EntityThaumaturgeGolem, RenderSample> lastRender = new WeakHashMap<>();

    private record RenderSample(Vec3 position, long nanos, int tick) {}

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.accessoryRenderers = GolemAccessoryRenderTable.collect(context);
        this.model = new CopperGolemRig(context.bakeLayer(TTModelLayers.GOLEM));
        this.shadowRadius = 0.3F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThaumaturgeGolem entity) {
        if (entity.properties() instanceof GolemProperties props) {
            return props.material().texture();
        }
        return FALLBACK_TEXTURE;
    }

    @Override
    public void render(
            EntityThaumaturgeGolem entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        if (GolemTrace.DEBUG_TRACE) traceRenderedMovement(entity, partialTick);
        GolemRenderState state = build(entity, partialTick, light);
        if (state.props != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
            poseStack.scale(-CopperGolemRig.SCALE, -CopperGolemRig.SCALE, CopperGolemRig.SCALE);
            poseStack.translate(0, -1.5F, 0);
            model.setupAnim(state);
            if (!state.invisible) {
                renderParts(model, accessoryRenderers, state, poseStack, buffers, false, 0xFFFFFFFF);
            } else if (state.ghost) {
                renderParts(
                        model,
                        accessoryRenderers,
                        state,
                        poseStack,
                        buffers,
                        false,
                        ARGB32.colorFromFloat(GHOST_ALPHA, 1.0F, 1.0F, 1.0F));
            }
            if (state.xray) {
                renderParts(model, accessoryRenderers, state, poseStack, buffers, true, XRAY_COLOR);
            }
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    private void traceRenderedMovement(EntityThaumaturgeGolem entity, float partialTick) {
        // This is the interpolated entity origin used by Minecraft's renderer.
        // Compare render frames, not server ticks, to identify visible position snaps.
        Vec3 rendered = new Vec3(
                Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
        long now = System.nanoTime();
        RenderSample prior = lastRender.put(entity, new RenderSample(rendered, now, entity.tickCount));
        if (prior == null) return;
        Vec3 delta = rendered.subtract(prior.position());
        if (delta.horizontalDistanceSqr() < 0.65 * 0.65 && Math.abs(delta.y) < 0.45) return;
        // A two-second render-thread stall is not a single-frame entity teleport.
        // Keep that distinction explicit, rather than reporting it as a snap.
        String event = now - prior.nanos() > 200_000_000L ? "render_gap" : "render_snap";
        GolemTrace.log(
                "[golem-client] event={} golem={} tick={} priorTick={} renderDelta={} "
                        + "renderFrom={} renderTo={} frameMs={} partialTick={} old=({},{},{}) current={} velocity={}",
                event,
                entity.getUUID(),
                entity.tickCount,
                prior.tick(),
                delta,
                prior.position(),
                rendered,
                (now - prior.nanos()) / 1_000_000.0,
                partialTick,
                entity.xOld,
                entity.yOld,
                entity.zOld,
                entity.position(),
                entity.getDeltaMovement());
    }

    private static GolemRenderState build(EntityThaumaturgeGolem entity, float partialTick, int light) {
        GolemRenderState state = new GolemRenderState();
        state.props = (GolemProperties) entity.properties();
        state.color = entity.color();
        state.ageInTicks = entity.tickCount + partialTick;
        state.lightCoords = light;
        state.bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        state.headYawDelta = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot) - state.bodyRot;
        state.pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.walkPos = entity.walkAnimation.position(partialTick);
        state.walkSpeed = Math.min(1.0F, entity.walkAnimation.speed(partialTick));
        state.attackTime = entity.getAttackAnim(partialTick);
        double dx = entity.getX() - entity.xOld;
        double dz = entity.getZ() - entity.zOld;
        state.speedSq = dx * dx + dz * dz;
        state.yawDelta = entity.getYRot() - entity.yRotO;
        state.wheelRotation = entity.wheelRotation;
        state.combat = entity.isFighting();
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.shouldEntityAppearGlowing(entity)) {
            int teamColor = entity.getTeamColor();
            state.outlineColor =
                    ARGB32.color(255, ARGB32.red(teamColor), ARGB32.green(teamColor), ARGB32.blue(teamColor));
        }
        state.invisible = entity.isInvisible();
        state.ghost = state.invisible && player != null && !entity.isInvisibleTo(player);
        state.xray = player != null
                && player.isShiftKeyDown()
                && (player.getMainHandItem().getItem() instanceof ISealDisplayer
                        || player.getOffhandItem().getItem() instanceof ISealDisplayer)
                && !player.hasLineOfSight(entity);
        ItemStack held = entity.getMainHandItem();
        state.holdingItem = !held.isEmpty();
        state.heldItemIsBlock = held.getItem() instanceof BlockItem;
        state.heldItem = held;
        List<ItemStack> carrying = entity.hands().contents();
        ItemStack hauled = carrying.size() > 1 ? carrying.get(1) : ItemStack.EMPTY;
        state.haulingItem = !hauled.isEmpty();
        state.haulerItemIsBlock = hauled.getItem() instanceof BlockItem;
        state.haulerItem = hauled;
        state.accessories = entity.getAccessories();
        state.accessoryStates = entity.syncedAccessoryStates();
        return state;
    }

    public static void renderParts(
            CopperGolemRig model,
            GolemAccessoryRenderTable accessories,
            GolemRenderState state,
            PoseStack pose,
            MultiBufferSource buffers,
            boolean xray,
            int color) {
        ResourceLocation material = state.props.material().texture();
        ResourceLocation skin = GolemSkins.forMaterial(material);
        RenderType type = renderType(skin, xray, color);
        model.renderToBuffer(pose, buffers.getBuffer(type), state.lightCoords, OverlayTexture.NO_OVERLAY, color);
        if (!xray) {
            model.renderToBuffer(
                    pose,
                    buffers.getBuffer(
                            ARGB32.alpha(color) < 255
                                    ? TTRenderTypes.entityTranslucentNoDepthWrite(GolemSkins.EYES)
                                    : RenderType.eyes(GolemSkins.EYES)),
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    color);
        }
        for (GolemAccessoryAnchor anchor : GolemAccessoryAnchor.values()) {
            pose.pushPose();
            model.translateToAnchor(pose, anchor);
            GolemPartModel.AttachPoint point = anchor == GolemAccessoryAnchor.HEAD
                    ? GolemPartModel.AttachPoint.HEAD
                    : GolemPartModel.AttachPoint.BODY;
            for (GolemPartModel part : attachedParts(state.props, point)) {
                renderPartModel(state, part, GolemPartModel.LimbSide.MIDDLE, pose, buffers, material, xray, color);
            }
            if (!xray) {
                accessories.render(anchor, state, pose, buffers);
                if (anchor == GolemAccessoryAnchor.BODY) {
                    GolemEquipmentRenderer.renderColorBand(state, pose, buffers, color);
                }
            }
            pose.popPose();
        }
        for (GolemPartModel.AttachPoint point :
                List.of(GolemPartModel.AttachPoint.ARMS, GolemPartModel.AttachPoint.LEGS)) {
            for (GolemPartModel.LimbSide side : List.of(GolemPartModel.LimbSide.RIGHT, GolemPartModel.LimbSide.LEFT)) {
                pose.pushPose();
                model.translateToLimb(pose, point, side);
                for (GolemPartModel part : attachedParts(state.props, point)) {
                    renderPartModel(state, part, side, pose, buffers, material, xray, color);
                }
                pose.popPose();
            }
        }
        if (!xray && state.holdingItem) {
            pose.pushPose();
            model.translateToAnchor(pose, GolemAccessoryAnchor.BODY);
            pose.translate(0, 2.0F / 16, -8.0F / 16);
            pose.scale(0.5F, 0.5F, 0.5F);
            ItemRenderHelper.render(
                    state.heldItem,
                    ItemDisplayContext.FIXED,
                    pose,
                    buffers,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
            pose.popPose();
        }
    }

    private static List<GolemPartModel> attachedParts(GolemProperties props, GolemPartModel.AttachPoint point) {
        List<GolemPartModel> out = new ArrayList<>();
        addPart(out, props.head(), point);
        addPart(out, props.arms(), point);
        addPart(out, props.legs(), point);
        addPart(out, props.addon(), point);
        return out;
    }

    private static void addPart(List<GolemPartModel> out, GolemPart part, GolemPartModel.AttachPoint point) {
        for (GolemPartModel model : part.models()) {
            if (model.attachPoint() == point) {
                out.add(model);
            }
        }
    }

    private static void renderPartModel(
            GolemRenderState state,
            GolemPartModel part,
            GolemPartModel.LimbSide side,
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation matTexture,
            boolean xray,
            int color) {
        TTMesh mesh = GolemMeshes.get(part.objModel());
        GolemPartRenderHook hook = GolemPartRenderHooks.hookFor(part);
        for (TTMeshPart objectPart : mesh.parts()) {
            poseStack.pushPose();
            ResourceLocation texture = part.useMaterialTextureForObjectPart(objectPart.name()) || part.texture() == null
                    ? matTexture
                    : part.texture();
            texture = GolemMeshes.texture(objectPart, texture);
            hook.posePiece(objectPart.name(), state, poseStack, side);
            renderMeshPart(objectPart, poseStack, buffers, texture, xray, color, state.lightCoords);
            if (!xray) {
                hook.decoratePiece(objectPart.name(), state, poseStack, buffers, side);
            }
            poseStack.popPose();
        }
    }

    private static void renderMeshPart(
            TTMeshPart part,
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation texture,
            boolean xray,
            int color,
            int light) {
        RenderType type = renderType(texture, xray, color);
        VertexConsumer buffer = buffers.getBuffer(type);
        GolemMeshes.renderPart(part, poseStack.last(), buffer, light, color);
    }

    static RenderType renderType(ResourceLocation texture, boolean xray, int color) {
        if (xray) {
            return TTRenderTypes.entityTranslucentNoDepth(texture);
        }
        return ARGB32.alpha(color) < 255
                ? TTRenderTypes.entityTranslucentNoDepthWrite(texture)
                : RenderType.entityCutout(texture);
    }
}
