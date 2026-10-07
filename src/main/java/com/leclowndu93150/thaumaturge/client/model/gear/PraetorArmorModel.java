package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;

public final class PraetorArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final float SEATED_CLOTH_LIFT = 0.3F;

    private final ModelPart rightCloth;
    private final ModelPart rightHem;
    private final ModelPart leftCloth;
    private final ModelPart leftHem;
    private final ModelPart cape;
    private final ModelPart capeHem;

    public PraetorArmorModel(ModelPart root) {
        super(root);
        rightCloth = body.getChild("tabard_right_upper");
        rightHem = rightCloth.getChild("tabard_right_lower");
        leftCloth = body.getChild("tabard_left_upper");
        leftHem = leftCloth.getChild("tabard_left_lower");
        cape = body.getChild("cape_mantle_upper");
        capeHem = cape.getChild("cape_mantle_lower");
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ArmorStandRenderState) {
            return;
        }
        float speed = Mth.clamp(state.walkAnimationSpeed, 0.0F, 1.0F);
        float phase = state.walkAnimationPos * 0.6662F;
        float stride = Math.abs(Mth.cos(phase)) * speed;
        float crouch = state.isCrouching ? 1.0F : 0.0F;
        float flutter = Mth.sin(state.ageInTicks * 0.16F) * (0.012F + speed * 0.028F);
        float trailing = state.isFallFlying || state.isVisuallySwimming ? 0.65F : 0.0F;
        animateCloth(rightCloth, rightHem, rightLeg, state, stride, speed, crouch, flutter);
        animateCloth(leftCloth, leftHem, leftLeg, state, stride, speed, crouch, -flutter);
        cape.xRot = 8.0F * DEGREES_TO_RADIANS + stride * 0.32F + flutter + crouch * 0.16F + trailing;
        capeHem.xRot = 6.0F * DEGREES_TO_RADIANS + stride * 0.12F + flutter * 1.5F;
    }

    private void animateCloth(ModelPart cloth, ModelPart hem, ModelPart leg, HumanoidRenderState state, float stride, float speed, float crouch, float flutter) {
        float forwardLeg = Math.min(0.0F, leg.xRot - body.xRot);
        cloth.xRot = -8.0F * DEGREES_TO_RADIANS + forwardLeg - stride * 0.3F - Math.abs(body.yRot) - speed * 0.08F - crouch - (state.isPassenger ? SEATED_CLOTH_LIFT : 0.0F);
        cloth.y -= crouch;
        cloth.xScale = state.isPassenger ? 0.4F : 1.0F;
        hem.xRot = -4.0F * DEGREES_TO_RADIANS - stride * 0.12F - flutter + crouch;
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(108, 1).addBox(-4.25F, -8.75F, -4.9375F, 8.5F, 8.5F, 0.5F, new CubeDeformation(0.03125F)).texOffs(1, 17)
                .addBox(-4.25F, -9.375F, -4.375F, 8.5F, 1.0F, 8.5F, new CubeDeformation(0.03125F)).texOffs(36, 17).addBox(-4.125F, -8.375F, 4.25F, 8.5F, 8.0F, 0.5F, new CubeDeformation(0.03125F))
                .texOffs(55, 17).addBox(-4.75F, -8.125F, -4.1875F, 0.5F, 8.0F, 8.5F, new CubeDeformation(0.03125F)).texOffs(74, 17)
                .addBox(4.25F, -8.125F, -4.1875F, 0.5F, 8.0F, 8.5F, new CubeDeformation(0.03125F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("swept_cheek_-1", CubeListBuilder.create().texOffs(93, 17).addBox(-0.625F, -0.25F, -2.0F, 1.0F, 4.5F, 4.0F),
                PartPose.offsetAndRotation(-4.25F, -4.5F, -2.5F, 0.0F, -0.2094395F, 0.0698132F));
        head.addOrReplaceChild("swept_cheek_1", CubeListBuilder.create().texOffs(104, 17).addBox(-0.375F, -0.25F, -2.0F, 1.0F, 4.5F, 4.0F),
                PartPose.offsetAndRotation(4.25F, -4.5F, -2.5F, 0.0F, 0.2094395F, -0.0698132F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(1, 1).addBox(-4.25F, 0.75F, -2.5F, 8.5F, 10.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(29, 1).addBox(-4.25F, 1.0F, -3.875F, 8.5F, 7.0F, 1.0F)
                        .texOffs(49, 1).addBox(-2.5F, 2.625F, -4.875F, 5.0F, 5.0F, 0.5F).texOffs(61, 1).addBox(-4.5F, 10.375F, -2.875F, 9.0F, 1.5F, 6.0F).texOffs(92, 1)
                        .addBox(-4.5F, 0.875F, -4.375F, 3.0F, 9.5F, 0.5F).texOffs(100, 1).addBox(1.5F, 0.875F, -4.375F, 3.0F, 9.5F, 0.5F).texOffs(1, 35).addBox(-5.0F, -0.25F, -5.5F, 10.0F, 2.5F, 0.5F)
                        .texOffs(23, 35).addBox(-4.75F, -1.875F, 4.875F, 9.5F, 4.0F, 0.5F).texOffs(101, 35).addBox(-4.0F, 0.5F, 3.75F, 1.0F, 1.0F, 1.5F).texOffs(122, 35)
                        .addBox(3.0F, 0.5F, 3.75F, 1.0F, 1.0F, 1.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        body.addOrReplaceChild("gorget_side_-1", CubeListBuilder.create().texOffs(44, 35).addBox(-0.25F, -1.5F, -4.875F, 0.5F, 2.5F, 9.5F),
                PartPose.offsetAndRotation(-5.0F, 1.0F, 0.0F, 0.1396263F, 0.0F, 0.0698132F));
        body.addOrReplaceChild("gorget_side_1", CubeListBuilder.create().texOffs(65, 35).addBox(-0.25F, -1.5F, -4.875F, 0.5F, 2.5F, 9.5F),
                PartPose.offsetAndRotation(5.0F, 1.0F, 0.0F, 0.1396263F, 0.0F, -0.0698132F));
        PartDefinition tabardRightUpper = body.addOrReplaceChild("tabard_right_upper", CubeListBuilder.create().texOffs(86, 35).addBox(-1.5F, 0.0F, -0.25F, 3.0F, 5.5F, 0.5F),
                PartPose.offsetAndRotation(-2.875F, 10.5F, -4.375F, -0.1396263F, 0.0F, 0.0F));
        tabardRightUpper.addOrReplaceChild("tabard_right_lower", CubeListBuilder.create().texOffs(94, 35).addBox(-1.25F, -0.125F, -0.1875F, 2.5F, 4.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 5.5F, 0.0F, -0.0698132F, 0.0F, 0.0F));
        PartDefinition tabardLeftUpper = body.addOrReplaceChild("tabard_left_upper", CubeListBuilder.create().texOffs(107, 35).addBox(-1.5F, 0.0F, -0.25F, 3.0F, 4.0F, 0.5F),
                PartPose.offsetAndRotation(2.875F, 10.5F, -4.375F, -0.1396263F, 0.0F, 0.0F));
        tabardLeftUpper.addOrReplaceChild("tabard_left_lower", CubeListBuilder.create().texOffs(115, 35).addBox(-1.25F, -0.125F, -0.1875F, 2.5F, 2.5F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.0698132F, 0.0F, 0.0F));
        PartDefinition capeMantleUpper = body.addOrReplaceChild("cape_mantle_upper", CubeListBuilder.create().texOffs(1, 48).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 12.0F, 0.5F),
                PartPose.offsetAndRotation(-1.0F, 0.75F, 4.375F, 0.1396263F, 0.0F, 0.0F));
        capeMantleUpper.addOrReplaceChild("cape_mantle_lower", CubeListBuilder.create().texOffs(21, 48).addBox(-4.25F, -0.125F, 0.0625F, 8.5F, 3.5F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 48).addBox(-3.25F, -1.875F, -2.375F, 4.5F, 3.5F, 4.5F, new CubeDeformation(0.03125F)).texOffs(82, 48)
                        .addBox(-3.25F, 4.625F, -2.625F, 4.5F, 5.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(102, 48).addBox(-3.375F, 8.1875F, -2.6875F, 4.5F, 0.5F, 5.5F).texOffs(1, 62)
                        .addBox(-3.375F, 5.6875F, -2.6875F, 4.5F, 0.5F, 5.5F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("legate_pauldron_right", CubeListBuilder.create().texOffs(59, 48).addBox(-2.75F, -0.25F, -2.875F, 5.5F, 3.0F, 5.5F),
                PartPose.offsetAndRotation(-1.0F, -1.75F, 0.0F, 0.0F, 0.0F, 0.0872665F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(60, 62).addBox(-1.25F, -1.875F, -2.375F, 4.5F, 3.5F, 4.5F, new CubeDeformation(0.03125F)).texOffs(102, 62)
                        .addBox(3.75F, -4.5F, -2.625F, 0.5F, 4.0F, 5.0F).texOffs(1, 73).addBox(-1.25F, 4.625F, -2.625F, 4.5F, 5.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(21, 73)
                        .addBox(-1.375F, 8.1875F, -2.6875F, 4.5F, 0.5F, 5.5F).texOffs(42, 73).addBox(-1.375F, 5.6875F, -2.6875F, 4.5F, 0.5F, 5.5F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("legate_pauldron_left", CubeListBuilder.create().texOffs(79, 62).addBox(-2.75F, -0.75F, -2.875F, 5.5F, 4.5F, 5.5F),
                PartPose.offsetAndRotation(1.0F, -1.75F, 0.0F, 0.0F, 0.0F, -0.2094395F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(22, 62).addBox(-1.75F, 0.375F, -3.125F, 3.5F, 3.0F, 6.0F).texOffs(42, 62).addBox(-1.5F, 2.375F, -2.875F, 3.0F, 3.0F, 5.5F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(63, 73).addBox(-1.75F, 0.375F, -3.125F, 3.5F, 3.0F, 6.0F).texOffs(83, 73).addBox(-1.5F, 2.375F, -2.875F, 3.0F, 3.0F, 5.5F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        for (String side : new String[]{"right", "left"}) {
            PartDefinition cloth = KnightArmorModel.emptyChild(body, "tabard_" + side + "_upper");
            KnightArmorModel.emptyChild(cloth, "tabard_" + side + "_lower");
        }
        PartDefinition cape = KnightArmorModel.emptyChild(body, "cape_mantle_upper");
        KnightArmorModel.emptyChild(cape, "cape_mantle_lower");
        return mesh;
    }
}
