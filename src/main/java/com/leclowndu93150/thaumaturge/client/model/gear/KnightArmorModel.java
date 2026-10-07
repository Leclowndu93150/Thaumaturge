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

public final class KnightArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);

    private final ModelPart tabard;
    private final ModelPart tabardHem;
    private final ModelPart cape;
    private final ModelPart capeHem;

    public KnightArmorModel(ModelPart root) {
        super(root);
        tabard = body.getChild("tabard_front_upper");
        tabardHem = tabard.getChild("tabard_front_lower");
        cape = body.getChild("cape_center_upper");
        capeHem = cape.getChild("cape_center_lower");
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
        float forwardLeg = Math.min(0.0F, Math.min(leftLeg.xRot, rightLeg.xRot) - body.xRot);
        tabard.xRot = -7.0F * DEGREES_TO_RADIANS + forwardLeg - stride * 0.3F - Math.abs(body.yRot) - speed * 0.08F - crouch;
        tabard.y -= crouch;
        tabard.xScale = state.isPassenger ? 0.4F : 1.0F;
        tabardHem.xRot = -4.0F * DEGREES_TO_RADIANS - stride * 0.12F - flutter + crouch;
        cape.xRot = 6.0F * DEGREES_TO_RADIANS + stride * 0.32F + flutter + crouch * 0.16F + trailing;
        capeHem.xRot = 6.0F * DEGREES_TO_RADIANS + stride * 0.12F + flutter * 1.5F;
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(59, 1).addBox(-4.5F, -8.625F, -4.875F, 9.0F, 8.5F, 0.5F, new CubeDeformation(0.03125F)).texOffs(79, 1)
                        .addBox(-4.25F, -9.125F, -4.375F, 8.5F, 1.0F, 8.5F).texOffs(1, 17).addBox(-4.75F, -8.25F, -4.125F, 0.5F, 8.0F, 8.5F).texOffs(20, 17)
                        .addBox(4.25F, -8.25F, -4.125F, 0.5F, 8.0F, 8.5F).texOffs(39, 17).addBox(-4.125F, -8.375F, 4.25F, 8.5F, 8.0F, 0.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(1, 1).addBox(-4.25F, 0.75F, -2.5F, 8.5F, 10.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(29, 1).addBox(-4.5F, 10.375F, -2.75F, 9.0F, 1.5F, 5.5F)
                        .texOffs(58, 17).addBox(-3.0F, 0.875F, -3.125F, 6.0F, 9.5F, 0.5F).texOffs(20, 35).addBox(-3.875F, 0.125F, 2.75F, 1.0F, 1.0F, 1.5F).texOffs(26, 35)
                        .addBox(2.875F, 0.125F, 2.75F, 1.0F, 1.0F, 1.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition tabardFrontUpper = body.addOrReplaceChild("tabard_front_upper", CubeListBuilder.create().texOffs(72, 17).addBox(-2.75F, 0.0F, -0.25F, 5.5F, 5.5F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 11.5F, -3.25F, -0.122173F, 0.0F, 0.0F));
        tabardFrontUpper.addOrReplaceChild("tabard_front_lower", CubeListBuilder.create().texOffs(85, 17).addBox(-2.5F, -0.15F, -0.1875F, 5.0F, 3.0F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 5.525F, 0.0F, -0.0698132F, 0.0F, 0.0F));
        PartDefinition capeCenterUpper = body.addOrReplaceChild("cape_center_upper",
                CubeListBuilder.create().texOffs(97, 17).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 10.5F, 0.5F, new CubeDeformation(0.0625F)),
                PartPose.offsetAndRotation(0.0F, 1.0F, 3.25F, 0.1047198F, 0.0F, 0.0F));
        capeCenterUpper.addOrReplaceChild("cape_center_lower", CubeListBuilder.create().texOffs(1, 35).addBox(-4.25F, -0.125F, 0.0625F, 8.5F, 6.5F, 0.5F, new CubeDeformation(0.046875F)),
                PartPose.offsetAndRotation(0.0F, 10.5F, 0.0F, 0.1047198F, 0.0F, 0.0F));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(32, 35).addBox(-3.25F, -2.125F, -2.375F, 4.5F, 3.5F, 4.5F, new CubeDeformation(0.03125F)).texOffs(74, 35)
                        .addBox(-3.25F, 4.625F, -2.625F, 4.5F, 5.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(94, 35).addBox(-3.375F, 8.5F, -2.6875F, 4.5F, 0.5F, 5.5F).texOffs(1, 46)
                        .addBox(-3.375F, 6.0F, -2.6875F, 4.5F, 0.5F, 5.5F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("great_pauldron_right", CubeListBuilder.create().texOffs(51, 35).addBox(-2.75F, -0.75F, -2.75F, 5.5F, 4.0F, 5.5F),
                PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.2094395F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(58, 46).addBox(-1.25F, -2.125F, -2.375F, 4.5F, 3.5F, 4.5F, new CubeDeformation(0.03125F)).texOffs(100, 46)
                        .addBox(-1.25F, 4.625F, -2.625F, 4.5F, 5.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(1, 57).addBox(-1.375F, 8.5F, -2.6875F, 4.5F, 0.5F, 5.5F).texOffs(22, 57)
                        .addBox(-1.375F, 6.0F, -2.6875F, 4.5F, 0.5F, 5.5F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("great_pauldron_left", CubeListBuilder.create().texOffs(77, 46).addBox(-2.75F, -0.75F, -2.75F, 5.5F, 4.0F, 5.5F),
                PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.2094395F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(22, 46).addBox(-1.75F, -0.125F, -2.875F, 3.5F, 2.5F, 5.5F).texOffs(41, 46).addBox(-1.5F, 1.875F, -2.625F, 3.0F, 2.5F, 5.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(43, 57).addBox(-1.75F, -0.125F, -2.875F, 3.5F, 2.5F, 5.5F).texOffs(62, 57).addBox(-1.5F, 1.875F, -2.625F, 3.0F, 2.5F, 5.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        PartDefinition tabard = emptyChild(body, "tabard_front_upper");
        emptyChild(tabard, "tabard_front_lower");
        PartDefinition cape = emptyChild(body, "cape_center_upper");
        emptyChild(cape, "cape_center_lower");
        return mesh;
    }

    static MeshDefinition emptyMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        emptyChild(body, "frontcloth1");
        emptyChild(body, "frontcloth2");
        emptyChild(body, "cloak1");
        emptyChild(body, "cloak2");
        emptyChild(body, "cloak3");
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    static PartDefinition emptyChild(PartDefinition parent, String name) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
    }
}
