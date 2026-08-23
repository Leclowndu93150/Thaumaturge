package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.TCRenderTypes;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class GolemRenderer extends EntityRenderer<EntityThaumaturgeGolem> {
    private static final ResourceLocation BASE_MODEL = TCIds.rl("models/mesh/golem_base.tcmesh");
    private static final ResourceLocation FALLBACK_TEXTURE = TCIds.rl("textures/models/golem_decoration.png");
    private static final float GHOST_ALPHA = 0.15F;
    private static final int XRAY_COLOR = ARGB32.colorFromFloat(0.25F, 0.25F, 0.25F, 0.25F);

    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.3F;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThaumaturgeGolem entity) {
        if (entity.getProperties() instanceof GolemProperties props) {
            return props.getMaterial().texture();
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
        GolemRenderState state = build(entity, partialTick, light);
        if (state.props != null) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
            if (!state.invisible) {
                renderParts(state, poseStack, buffers, false, 0xFFFFFFFF);
            } else if (state.ghost) {
                renderParts(state, poseStack, buffers, false, ARGB32.colorFromFloat(GHOST_ALPHA, 1.0F, 1.0F, 1.0F));
            }
            if (state.xray) {
                renderParts(state, poseStack, buffers, true, XRAY_COLOR);
            }
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffers, light);
    }

    private static GolemRenderState build(EntityThaumaturgeGolem entity, float partialTick, int light) {
        GolemRenderState state = new GolemRenderState();
        state.props = (GolemProperties) entity.getProperties();
        state.color = entity.getGolemColor();
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
        float grinderSpeed = Math.max(entity.grinderSpeed, state.attackTime * 20.0F);
        entity.grinderRot += grinderSpeed;
        entity.grinderSpeed = grinderSpeed * 0.99F;
        state.grinderRot = entity.grinderRot;
        state.combat = entity.isInCombat();
        LocalPlayer player = Minecraft.getInstance().player;
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
        List<ItemStack> carrying = entity.getCarrying();
        ItemStack hauled = carrying.size() > 1 ? carrying.get(1) : ItemStack.EMPTY;
        state.haulingItem = !hauled.isEmpty();
        state.haulerItemIsBlock = hauled.getItem() instanceof BlockItem;
        state.haulerItem = hauled;
        state.accessories = entity.getAccessoryString();
        return state;
    }

    private void renderParts(
            GolemRenderState state, PoseStack poseStack, MultiBufferSource buffers, boolean xray, int color) {
        GolemProperties props = state.props;
        ResourceLocation matTexture = props.getMaterial().texture();
        boolean holding = state.holdingItem;
        boolean rolling = props.hasTrait(TCGolemTraits.WHEELED.get()) || props.hasTrait(TCGolemTraits.FLYER.get());
        float bry = 0.0F;
        float rx = (float) Math.toDegrees(Mth.sin(state.ageInTicks * 0.067F) * 0.03F);
        float rz = (float) Math.toDegrees(Mth.cos(state.ageInTicks * 0.09F) * 0.05F + 0.05F);
        float rrx;
        float rry = 0.0F;
        float rrz = 0.0F;
        float rlx;
        float rly = 0.0F;
        float rlz = 0.0F;
        if (holding) {
            rrx = 90.0F - rz / 2.0F;
            rrz = -2.0F;
            rlx = 90.0F - rz / 2.0F;
            rlz = 2.0F;
        } else {
            if (rolling) {
                rrx = rx * 2.0F;
                rlx = -rx * 2.0F;
            } else {
                float swing = Mth.cos(state.walkPos * 0.6662F + (float) Math.PI) * 2.0F * state.walkSpeed * 0.5F;
                rrx = (float) (Math.toDegrees(swing) + rx);
                swing = Mth.cos(state.walkPos * 0.6662F) * 2.0F * state.walkSpeed * 0.5F;
                rlx = (float) (Math.toDegrees(swing) - rx);
            }
            rrz += rz + 2.0F;
            rlz -= rz + 2.0F;
        }
        if (state.attackTime > 0.0F) {
            float wiggle = -Mth.sin(Mth.sqrt(state.attackTime) * (float) Math.PI * 2.0F) * 0.2F;
            bry = (float) Math.toDegrees(wiggle);
            rrz = -((float) Math.toDegrees(Mth.sin(wiggle) * 3.0F));
            rrx = (float) Math.toDegrees(-Mth.cos(wiggle) * 5.0F);
            rry += bry;
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(bry));
        float lean = rolling ? 75.0F : 25.0F;
        poseStack.mulPose(Axis.XN.rotationDegrees((float) (state.speedSq * lean)));
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) (state.speedSq * lean * 0.06 * state.yawDelta)));
        TCMesh base = GolemMeshes.get(BASE_MODEL);

        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        renderNamedPart(base, "chest", poseStack, buffers, matTexture, xray, color, state, matTexture);
        renderNamedPart(base, "waist", poseStack, buffers, matTexture, xray, color, state, matTexture);
        if (state.color > 0) {
            DyeColor dye = DyeColor.byId(state.color - 1);
            int flagColor = ARGB32.color(ARGB32.alpha(color), dye.getTextureDiffuseColor());
            renderNamedPart(base, "flag", poseStack, buffers, matTexture, xray, flagColor, state, matTexture);
        }
        for (GolemPartModel part : attachedParts(props, GolemPartModel.AttachPoint.BODY)) {
            renderPartModel(state, part, GolemPartModel.LimbSide.MIDDLE, poseStack, buffers, matTexture, xray, color);
        }
        if (!xray) {
            GolemAccessoryRenderer.submitBody(state, poseStack, buffers);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0, 0.75, -0.03125);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.headYawDelta));
        poseStack.mulPose(Axis.XN.rotationDegrees(state.pitch));
        for (GolemPartModel part : attachedParts(props, GolemPartModel.AttachPoint.HEAD)) {
            renderPartModel(state, part, GolemPartModel.LimbSide.MIDDLE, poseStack, buffers, matTexture, xray, color);
        }
        renderNamedPart(base, "head", poseStack, buffers, matTexture, xray, color, state, matTexture);
        if (!xray) {
            GolemAccessoryRenderer.submitHead(state, poseStack, buffers);
        }
        poseStack.popPose();

        List<GolemPartModel> armParts = attachedParts(props, GolemPartModel.AttachPoint.ARMS);
        poseStack.pushPose();
        poseStack.translate(0.20625, 0.6875, 0.0);
        if (!armParts.isEmpty()) {
            GolemPartRenderHook hook = GolemPartRenderHooks.hookFor(armParts.get(0));
            rrx = hook.armRotationX(state, GolemPartModel.LimbSide.RIGHT, rrx);
            rry = hook.armRotationY(state, GolemPartModel.LimbSide.RIGHT, rry);
            rrz = hook.armRotationZ(state, GolemPartModel.LimbSide.RIGHT, rrz);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(rrx));
        poseStack.mulPose(Axis.YP.rotationDegrees(rry));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rrz));
        renderNamedPart(base, "arm", poseStack, buffers, matTexture, xray, color, state, matTexture);
        for (GolemPartModel part : armParts) {
            renderPartModel(state, part, GolemPartModel.LimbSide.RIGHT, poseStack, buffers, matTexture, xray, color);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.20625, 0.6875, 0.0);
        if (!armParts.isEmpty()) {
            GolemPartRenderHook hook = GolemPartRenderHooks.hookFor(armParts.get(0));
            rlx = hook.armRotationX(state, GolemPartModel.LimbSide.LEFT, rlx);
            rly = hook.armRotationY(state, GolemPartModel.LimbSide.LEFT, rly);
            rlz = hook.armRotationZ(state, GolemPartModel.LimbSide.LEFT, rlz);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(rlx));
        poseStack.mulPose(Axis.YP.rotationDegrees(rly + 180.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(rlz));
        renderNamedPart(base, "arm", poseStack, buffers, matTexture, xray, color, state, matTexture);
        for (GolemPartModel part : armParts) {
            renderPartModel(state, part, GolemPartModel.LimbSide.LEFT, poseStack, buffers, matTexture, xray, color);
        }
        poseStack.popPose();

        List<GolemPartModel> legParts = attachedParts(props, GolemPartModel.AttachPoint.LEGS);
        poseStack.pushPose();
        poseStack.translate(0.09375, 0.375, 0.0);
        float legSwing = Mth.cos(state.walkPos * 0.6662F) * state.walkSpeed;
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(legSwing)));
        for (GolemPartModel part : legParts) {
            renderPartModel(state, part, GolemPartModel.LimbSide.RIGHT, poseStack, buffers, matTexture, xray, color);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.09375, 0.375, 0.0);
        legSwing = Mth.cos(state.walkPos * 0.6662F + (float) Math.PI) * state.walkSpeed;
        poseStack.mulPose(Axis.XP.rotationDegrees((float) Math.toDegrees(legSwing)));
        for (GolemPartModel part : legParts) {
            renderPartModel(state, part, GolemPartModel.LimbSide.LEFT, poseStack, buffers, matTexture, xray, color);
        }
        poseStack.popPose();

        if (!xray && state.holdingItem) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.625, 0.0);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - rz * 0.5F));
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            poseStack.scale(0.375F, 0.375F, 0.375F);
            poseStack.translate(0.0F, 0.25F, -1.5F);
            if (!state.heldItemIsBlock) {
                poseStack.translate(0.0F, -0.6F, 0.0F);
            }
            ItemRenderHelper.render(
                    state.heldItem,
                    ItemDisplayContext.HEAD,
                    poseStack,
                    buffers,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);
            poseStack.popPose();
        }
    }

    private static List<GolemPartModel> attachedParts(GolemProperties props, GolemPartModel.AttachPoint point) {
        List<GolemPartModel> out = new ArrayList<>();
        addPart(out, props.getHead().model(), point);
        addPart(out, props.getArms().model(), point);
        addPart(out, props.getLegs().model(), point);
        addPart(out, props.getAddon().model(), point);
        return out;
    }

    private static void addPart(List<GolemPartModel> out, GolemPartModel model, GolemPartModel.AttachPoint point) {
        if (model != null && model.attachPoint() == point) {
            out.add(model);
        }
    }

    private void renderPartModel(
            GolemRenderState state,
            GolemPartModel part,
            GolemPartModel.LimbSide side,
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation matTexture,
            boolean xray,
            int color) {
        TCMesh mesh = GolemMeshes.get(part.objModel());
        GolemPartRenderHook hook = GolemPartRenderHooks.hookFor(part);
        for (TCMeshPart objectPart : mesh.parts()) {
            poseStack.pushPose();
            ResourceLocation texture = part.useMaterialTextureForObjectPart(objectPart.name()) || part.texture() == null
                    ? matTexture
                    : part.texture();
            hook.preRenderObjectPart(objectPart.name(), state, poseStack, side, 0.0F);
            renderMeshPart(mesh, objectPart, poseStack, buffers, texture, xray, color, state, matTexture);
            hook.postRenderObjectPart(objectPart.name(), state, poseStack, buffers, side);
            poseStack.popPose();
        }
    }

    private static void renderNamedPart(
            TCMesh mesh,
            String name,
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation texture,
            boolean xray,
            int color,
            GolemRenderState state,
            ResourceLocation matTexture) {
        for (TCMeshPart part : mesh.parts()) {
            if (name.equals(part.name())) {
                renderMeshPart(mesh, part, poseStack, buffers, texture, xray, color, state, matTexture);
            }
        }
    }

    private static void renderMeshPart(
            TCMesh mesh,
            TCMeshPart part,
            PoseStack poseStack,
            MultiBufferSource buffers,
            ResourceLocation texture,
            boolean xray,
            int color,
            GolemRenderState state,
            ResourceLocation matTexture) {
        RenderType type = xray
                ? TCRenderTypes.entityTranslucentNoDepth(texture)
                : ARGB32.alpha(color) < 255 || !texture.equals(matTexture)
                        ? RenderType.entityTranslucent(texture)
                        : RenderType.entityCutout(texture);
        VertexConsumer buffer = buffers.getBuffer(type);
        GolemMeshes.renderPart(part, poseStack.last(), buffer, state.lightCoords, color);
    }
}
