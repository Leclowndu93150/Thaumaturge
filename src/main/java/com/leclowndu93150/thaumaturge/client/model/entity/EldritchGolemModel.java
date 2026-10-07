package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.EldritchGolemRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class EldritchGolemModel extends EntityModel<EldritchGolemRenderState> {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float SHOULDER_TILT = 0.31416F;
    private static final float CORE_TILT = 0.7854F;

    private final ModelPart head;
    private final ModelPart core;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tabard;
    private final ModelPart tabardTip;
    private final ModelPart cloak;
    private final ModelPart cloakTip;

    public enum Material {
        BODY, CORE
    }

    public EldritchGolemModel(ModelPart root) {
        this(root, Material.BODY);
    }

    public EldritchGolemModel(ModelPart root, Material material) {
        super(root);
        ModelPart body = root.getChild("body");
        head = body.getChild("head_shell");
        core = body.getChild("phase_two_core");
        leftArm = body.getChild("left_arm");
        rightArm = body.getChild("right_arm");
        leftLeg = root.getChild("left_leg");
        rightLeg = root.getChild("right_leg");
        tabard = body.getChild("tabard");
        tabardTip = tabard.getChild("tabard_tip");
        cloak = body.getChild("cloak");
        cloakTip = cloak.getChild("cloak_tip");
        boolean coreOnly = material == Material.CORE;
        if (coreOnly) {
            for (ModelPart part : root.getAllParts()) {
                part.skipDraw = true;
            }
        }
        head.getChild("core_low_eye").skipDraw = !coreOnly;
        core.getChild("core_exposed_beam_core").skipDraw = !coreOnly;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition headShell = body.addOrReplaceChild("head_shell", CubeListBuilder.create(), PartPose.offset(0.0F, -22.0F, 0.0F));
        headShell.addOrReplaceChild("metal_recessed_head", CubeListBuilder.create().texOffs(52, 62).addBox(0.0F, -10.0F, 0.0F, 8.0F, 10.0F, 7.0F), PartPose.offset(-4.0F, 2.0F, -6.0F));
        headShell.addOrReplaceChild("stone_stone_brow", CubeListBuilder.create().texOffs(112, 62).addBox(0.0F, -4.0F, 0.0F, 10.0F, 4.0F, 9.0F), PartPose.offset(-5.0F, -5.0F, -7.0F));
        headShell.addOrReplaceChild("core_low_eye", CubeListBuilder.create().texOffs(243, 62).addBox(0.0F, -2.0F, 0.0F, 4.0F, 2.0F, 0.5F), PartPose.offset(-2.0F, -1.0F, -6.5F));
        PartDefinition phaseTwoCore = body.addOrReplaceChild("phase_two_core", CubeListBuilder.create(), PartPose.offset(0.0F, -24.0F, 0.0F));
        phaseTwoCore.addOrReplaceChild("core_exposed_beam_core", CubeListBuilder.create().texOffs(168, 85).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -1.0F, -1.0F, 0.0F, 0.0F, -CORE_TILT));
        PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(13.0F, -17.0F, 0.0F));
        leftArm.addOrReplaceChild("stone_hunched_shoulder", CubeListBuilder.create().texOffs(133, 1).addBox(-1.0F, -11.0F, -6.0F, 10.0F, 13.0F, 12.0F),
                PartPose.offsetAndRotation(-1.0F, 3.0F, 2.0F, 0.0F, 0.0F, SHOULDER_TILT));
        leftArm.addOrReplaceChild("dark_upper_arm", CubeListBuilder.create().texOffs(68, 37).addBox(0.0F, -15.0F, 0.0F, 6.0F, 15.0F, 7.0F), PartPose.offset(0.0F, 16.0F, -2.0F));
        leftArm.addOrReplaceChild("stone_massive_fist", CubeListBuilder.create().texOffs(84, 1).addBox(0.0F, -18.0F, 0.0F, 11.0F, 18.0F, 12.0F), PartPose.offset(-2.5F, 31.0F, -8.0F));
        leftArm.addOrReplaceChild("metal_fist_cuff", CubeListBuilder.create().texOffs(1, 62).addBox(0.0F, -4.2F, 0.0F, 11.4F, 4.2F, 12.4F), PartPose.offset(-2.7F, 17.0F, -8.2F));
        PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-13.0F, -17.0F, 0.0F));
        rightArm.addOrReplaceChild("stone_hunched_shoulder", CubeListBuilder.create().texOffs(133, 1).addBox(-9.0F, -11.0F, -6.0F, 10.0F, 13.0F, 12.0F),
                PartPose.offsetAndRotation(1.0F, 3.0F, 2.0F, 0.0F, 0.0F, -SHOULDER_TILT));
        rightArm.addOrReplaceChild("dark_upper_arm", CubeListBuilder.create().texOffs(68, 37).addBox(0.0F, -15.0F, 0.0F, 6.0F, 15.0F, 7.0F), PartPose.offset(-6.0F, 16.0F, -2.0F));
        rightArm.addOrReplaceChild("stone_massive_fist", CubeListBuilder.create().texOffs(84, 1).addBox(0.0F, -18.0F, 0.0F, 11.0F, 18.0F, 12.0F), PartPose.offset(-8.5F, 31.0F, -8.0F));
        rightArm.addOrReplaceChild("metal_fist_cuff", CubeListBuilder.create().texOffs(1, 62).addBox(0.0F, -4.2F, 0.0F, 11.4F, 4.2F, 12.4F), PartPose.offset(-8.7F, 17.0F, -8.2F));
        body.addOrReplaceChild("dark_core_socket", CubeListBuilder.create().texOffs(207, 62).addBox(0.0F, -3.0F, 0.0F, 6.0F, 3.0F, 4.0F), PartPose.offset(-3.0F, -20.0F, -2.0F));
        body.addOrReplaceChild("stone_barrel_torso", CubeListBuilder.create().texOffs(1, 1).addBox(0.0F, -19.0F, 0.0F, 26.0F, 19.0F, 14.0F), PartPose.offset(-13.0F, 1.0F, -5.0F));
        body.addOrReplaceChild("stone_upper_back", CubeListBuilder.create().texOffs(1, 37).addBox(0.0F, -12.0F, 0.0F, 22.0F, 12.0F, 10.0F), PartPose.offset(-11.0F, -13.0F, 0.0F));
        body.addOrReplaceChild("metal_waist_binding", CubeListBuilder.create().texOffs(97, 37).addBox(-2.25F, -6.0F, -0.25F, 26.5F, 6.0F, 14.5F), PartPose.offset(-11.0F, 4.0F, -5.0F));
        body.addOrReplaceChild("dark_chest_plaque", CubeListBuilder.create().texOffs(153, 62).addBox(0.0F, -9.0F, 0.0F, 14.0F, 9.0F, 2.0F), PartPose.offset(-7.0F, -6.0F, -7.0F));
        body.addOrReplaceChild("brass_chest_lock", CubeListBuilder.create().texOffs(230, 62).addBox(0.0F, -5.0F, 0.0F, 4.0F, 5.0F, 1.0F), PartPose.offset(-2.0F, -8.0F, -8.0F));
        PartDefinition tabard = body.addOrReplaceChild("tabard", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, -7.3F));
        tabard.addOrReplaceChild("cloth_tabard_upper", CubeListBuilder.create().texOffs(1, 85).addBox(-6.0F, 0.0F, -0.5F, 12.0F, 12.0F, 1.0F), PartPose.ZERO);
        PartDefinition tabardTip = tabard.addOrReplaceChild("tabard_tip", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        tabardTip.addOrReplaceChild("cloth_tabard_lower", CubeListBuilder.create().texOffs(31, 85).addBox(-6.0F, 0.0F, -0.5F, 12.0F, 12.0F, 1.0F), PartPose.ZERO);
        PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offset(0.0F, -24.0F, 10.6F));
        cloak.addOrReplaceChild("cloth_cloak_upper", CubeListBuilder.create().texOffs(61, 85).addBox(-11.0F, 0.0F, 0.0F, 22.0F, 18.0F, 1.0F), PartPose.ZERO);
        PartDefinition cloakTip = cloak.addOrReplaceChild("cloak_tip", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));
        cloakTip.addOrReplaceChild("cloth_cloak_lower", CubeListBuilder.create().texOffs(111, 85).addBox(-11.0F, 0.0F, 0.0F, 22.0F, 18.0F, 1.0F), PartPose.ZERO);
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(6.0F, 2.0F, 0.0F));
        leftLeg.addOrReplaceChild("dark_hip_joint", CubeListBuilder.create().texOffs(85, 62).addBox(0.0F, -9.0F, 0.0F, 6.0F, 9.0F, 6.0F), PartPose.offset(-2.0F, 7.0F, -2.0F));
        leftLeg.addOrReplaceChild("stone_short_shin", CubeListBuilder.create().texOffs(180, 1).addBox(0.0F, -15.0F, 0.0F, 8.0F, 15.0F, 9.0F), PartPose.offset(-3.0F, 19.0F, -4.0F));
        leftLeg.addOrReplaceChild("metal_wide_foot", CubeListBuilder.create().texOffs(182, 37).addBox(0.0F, -6.0F, 0.0F, 10.0F, 6.0F, 13.2F), PartPose.offset(-4.0F, 22.0F, -8.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-6.0F, 2.0F, 0.0F));
        rightLeg.addOrReplaceChild("dark_hip_joint", CubeListBuilder.create().texOffs(85, 62).addBox(0.0F, -9.0F, 0.0F, 6.0F, 9.0F, 6.0F), PartPose.offset(-4.0F, 7.0F, -2.0F));
        rightLeg.addOrReplaceChild("stone_short_shin", CubeListBuilder.create().texOffs(180, 1).addBox(0.0F, -15.0F, 0.0F, 8.0F, 15.0F, 9.0F), PartPose.offset(-5.0F, 19.0F, -4.0F));
        rightLeg.addOrReplaceChild("metal_wide_foot", CubeListBuilder.create().texOffs(182, 37).addBox(0.0F, -6.0F, 0.0F, 10.0F, 6.0F, 13.2F), PartPose.offset(-6.0F, 22.0F, -8.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(EldritchGolemRenderState state) {
        super.setupAnim(state);
        head.visible = !state.headless;
        core.visible = state.headless;
        head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.35F;
        head.xRot = state.xRot * Mth.DEG_TO_RAD * 0.5F;
        core.yRot = state.yRot * Mth.DEG_TO_RAD;
        core.xRot = state.xRot * Mth.DEG_TO_RAD;
        float stride = Mth.cos(state.walkAnimationPos * 0.4662F) * Math.min(state.walkAnimationSpeed, 1.0F);
        rightLeg.xRot = stride * 0.65F;
        leftLeg.xRot = -rightLeg.xRot;
        rightArm.xRot = -stride * 0.45F;
        leftArm.xRot = stride * 0.45F;
        float legAngle = Math.abs(rightLeg.xRot);
        root.y = -Math.max(0.0F, 22.0F * Mth.cos(legAngle) + 8.0F * Mth.sin(legAngle) - 22.0F);
        float idle = Mth.sin(state.ageInTicks * 0.06F) * 0.015F;
        float clothSwing = Math.abs(stride);
        tabard.xRot = -0.12F - clothSwing * 0.55F + idle;
        tabardTip.xRot = -0.06F - clothSwing * 0.12F + idle * 0.7F;
        cloak.xRot = 0.10F + clothSwing * 0.50F + idle;
        cloakTip.xRot = 0.06F + clothSwing * 0.10F + idle * 1.4F;
        leftArm.zRot = -idle;
        rightArm.zRot = idle;
        if (state.spawnTimer > 0.0F) {
            float awakening = Mth.clamp(state.spawnTimer / 100.0F, 0.0F, 1.0F);
            head.xRot += awakening * 0.7F;
            leftArm.xRot = rightArm.xRot = -awakening * 0.3F;
        } else if (state.attackTime > 0.0F) {
            float progress = 1.0F - Mth.clamp(state.attackTime / 10.0F, 0.0F, 1.0F);
            float swing = progress < 0.25F
                    ? easedSwing(progress / 0.25F, 0.0F, -2.25F)
                    : progress < 0.6F ? easedSwing((progress - 0.25F) / 0.35F, -2.25F, -0.3F) : easedSwing((progress - 0.6F) / 0.4F, -0.3F, 0.0F);
            leftArm.xRot = rightArm.xRot = swing;
        }
    }

    private static float easedSwing(float progress, float start, float end) {
        return Mth.lerp(progress * progress * (3.0F - 2.0F * progress), start, end);
    }
}
