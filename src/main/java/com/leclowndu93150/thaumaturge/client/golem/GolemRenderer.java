package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TTRenderPipelines;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GolemRenderer extends EntityRenderer<EntityThaumaturgeGolem, GolemRenderState> {
    private static final float GHOST_ALPHA = 0.15F;
    private static final int XRAY_COLOR = ARGB.colorFromFloat(0.25F, 0.25F, 0.25F, 0.25F);
    private static final Map<Identifier, RenderType> XRAY_TYPES = new ConcurrentHashMap<>();

    private final CopperGolemRig model;
    private final ItemModelResolver itemModelResolver;
    private final GolemAccessoryRenderTable accessoryRenderers;

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CopperGolemRig(context.bakeLayer(ModelLayers.COPPER_GOLEM));
        this.itemModelResolver = context.getItemModelResolver();
        this.accessoryRenderers = GolemAccessoryRenderTable.collect(context);
        this.shadowRadius = 0.3F;
    }

    private static RenderType xrayType(Identifier texture) {
        return XRAY_TYPES.computeIfAbsent(texture, tex -> RenderType.create("tc_golem_xray_" + tex.getPath().hashCode(),
                RenderSetup.builder(TTRenderPipelines.ENTITY_TRANSLUCENT_NO_DEPTH).withTexture("Sampler0", tex).createRenderSetup()));
    }

    @Override
    public GolemRenderState createRenderState() {
        return new GolemRenderState();
    }

    @Override
    public void extractRenderState(EntityThaumaturgeGolem entity, GolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTicks);
        state.props = entity.properties();
        state.color = entity.color();
        state.bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        state.headYawDelta = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot) - state.bodyRot;
        state.pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.yRot = state.headYawDelta;
        state.xRot = state.pitch;
        // Render states are recreated every frame; keep the animation epoch fixed across ticks.
        state.idleAnimationState.start(0);
        state.walkPos = state.walkAnimationPos = entity.walkAnimation.position(partialTicks);
        state.walkSpeed = state.walkAnimationSpeed = Math.min(1.0F, entity.walkAnimation.speed(partialTicks));
        state.attackTime = entity.getAttackAnim(partialTicks);
        double dx = entity.getX() - entity.xOld;
        double dz = entity.getZ() - entity.zOld;
        state.speedSq = dx * dx + dz * dz;
        state.yawDelta = entity.getYRot() - entity.yRotO;
        state.wheelRotation = entity.wheelRotation;
        state.grinderRot = state.ageInTicks * 12.0F + state.attackTime * 180.0F;
        state.combat = entity.isInCombat();
        LocalPlayer player = Minecraft.getInstance().player;
        state.invisible = entity.isInvisible();
        state.ghost = state.invisible && player != null && !entity.isInvisibleTo(player);
        state.xray = player != null && player.isShiftKeyDown() && (player.getMainHandItem().getItem() instanceof ISealDisplayer || player.getOffhandItem().getItem() instanceof ISealDisplayer)
                && !player.hasLineOfSight(entity);
        ItemStack held = entity.getMainHandItem();
        state.holdingItem = !held.isEmpty();
        state.heldItemIsBlock = held.getItem() instanceof BlockItem;
        itemModelResolver.updateForTopItem(state.heldItem, held, ItemDisplayContext.FIXED, entity.level(), entity, 0);
        List<ItemStack> carrying = entity.hands().contents();
        ItemStack hauled = carrying.size() > 1 ? carrying.get(1) : ItemStack.EMPTY;
        state.haulingItem = !hauled.isEmpty();
        state.haulerItemIsBlock = hauled.getItem() instanceof BlockItem;
        itemModelResolver.updateForTopItem(state.haulerItem, hauled, ItemDisplayContext.FIXED, entity.level(), entity, 0);
        state.accessories = entity.getAccessories();
        state.accessoryStates = entity.syncedAccessoryStates();
    }

    @Override
    public void submit(GolemRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (state.props == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
        poseStack.scale(-CopperGolemRig.SCALE, -CopperGolemRig.SCALE, CopperGolemRig.SCALE);
        poseStack.translate(0.0F, -1.5F, 0.0F);
        model.setupAnim(state);
        if (!state.invisible) {
            submitParts(model, accessoryRenderers, state, poseStack, collector, false, 0xFFFFFFFF);
        } else if (state.ghost) {
            submitParts(model, accessoryRenderers, state, poseStack, collector, false, ARGB.colorFromFloat(GHOST_ALPHA, 1.0F, 1.0F, 1.0F));
        }
        if (state.xray) {
            submitParts(model, accessoryRenderers, state, poseStack, collector, true, XRAY_COLOR);
        }
        poseStack.popPose();
    }

    public static void submitParts(CopperGolemRig model, GolemAccessoryRenderTable accessoryRenderers, GolemRenderState state, PoseStack pose, SubmitNodeCollector collector, boolean xray, int color) {
        Identifier material = state.props.material().texture();
        Identifier skin = GolemSkins.forMaterial(material);
        RenderType skinType = xray ? xrayType(skin) : ARGB.alpha(color) < 255 ? RenderTypes.entityTranslucent(skin) : RenderTypes.entityCutout(skin);
        collector.submitModel(model, state, pose, skinType, state.lightCoords, OverlayTexture.NO_OVERLAY, color, null, 0, null);
        if (!xray) {
            collector.submitModel(model, state, pose, ARGB.alpha(color) < 255 ? RenderTypes.entityTranslucent(GolemSkins.EYES) : RenderTypes.eyes(GolemSkins.EYES), state.lightCoords,
                    OverlayTexture.NO_OVERLAY, color, null, 0, null);
        }
        for (GolemAccessoryAnchor anchor : GolemAccessoryAnchor.values()) {
            pose.pushPose();
            model.translateToAnchor(pose, anchor);
            GolemPartModel.AttachPoint point = anchor == GolemAccessoryAnchor.HEAD ? GolemPartModel.AttachPoint.HEAD : GolemPartModel.AttachPoint.BODY;
            for (GolemPartModel part : attachedParts(state.props, point)) {
                renderPartModel(state, part, GolemPartModel.LimbSide.MIDDLE, pose, collector, material, xray, color);
            }
            if (!xray) {
                accessoryRenderers.submit(anchor, state, pose, collector, color);
                if (anchor == GolemAccessoryAnchor.BODY) {
                    GolemEquipmentRenderer.submitColorBand(state, pose, collector, color);
                }
            }
            pose.popPose();
        }
        for (GolemPartModel.AttachPoint point : List.of(GolemPartModel.AttachPoint.ARMS, GolemPartModel.AttachPoint.LEGS)) {
            for (GolemPartModel.LimbSide side : List.of(GolemPartModel.LimbSide.RIGHT, GolemPartModel.LimbSide.LEFT)) {
                pose.pushPose();
                model.translateToLimb(pose, point, side);
                for (GolemPartModel part : attachedParts(state.props, point)) {
                    renderPartModel(state, part, side, pose, collector, material, xray, color);
                }
                pose.popPose();
            }
        }
        if (!xray && state.holdingItem) {
            pose.pushPose();
            model.translateToAnchor(pose, GolemAccessoryAnchor.BODY);
            pose.translate(0.0F, 2.0F / 16.0F, -8.0F / 16.0F);
            pose.scale(0.5F, 0.5F, 0.5F);
            state.heldItem.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
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

    private static void renderPartModel(GolemRenderState state, GolemPartModel part, GolemPartModel.LimbSide side, PoseStack poseStack, SubmitNodeCollector collector, Identifier matTexture, boolean xray, int color) {
        var mesh = GolemMeshes.get(part.objModel());
        GolemPartRenderHook hook = GolemPartRenderHooks.hookFor(part);
        for (TTMeshPart objectPart : mesh.parts()) {
            poseStack.pushPose();
            Identifier texture = part.useMaterialTextureForObjectPart(objectPart.name()) || part.texture() == null ? matTexture : part.texture();
            texture = GolemMeshes.texture(objectPart, texture);
            hook.preRenderObjectPart(objectPart.name(), state, poseStack, side, 0.0F);
            submitMeshPart(objectPart, poseStack, collector, texture, xray, color, state);
            if (!xray) {
                hook.postRenderObjectPart(objectPart.name(), state, poseStack, collector, side);
            }
            poseStack.popPose();
        }
    }

    private static void submitMeshPart(TTMeshPart part, PoseStack poseStack, SubmitNodeCollector collector, Identifier texture, boolean xray, int color, GolemRenderState state) {
        boolean translucent = ARGB.alpha(color) < 255;
        RenderType type = xray ? xrayType(texture) : translucent ? RenderTypes.entityTranslucent(texture) : RenderTypes.entityCutout(texture);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> GolemMeshes.renderPart(part, pose, buffer, light, color));
    }
}
