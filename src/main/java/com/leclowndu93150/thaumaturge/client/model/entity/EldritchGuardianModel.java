package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityEldritchWarden;
import java.util.EnumSet;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public final class EldritchGuardianModel<T extends Mob> extends HierarchicalModel<T> {
    protected final ModelPart root;

    @Override
    public ModelPart root() {
        return root;
    }

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart hover;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart tabard;
    private final ModelPart tabardTip;
    private final ModelPart cloak;
    private final ModelPart cloakTip;
    private final ModelPart leftDrape;
    private final ModelPart rightDrape;
    private final ModelPart leftDrapeTip;
    private final ModelPart rightDrapeTip;

    public EldritchGuardianModel(ModelPart root) {
        this.root = root;
        hover = root.getChild("root");
        body = root.getChild("root").getChild("body");
        head = root.getChild("root").getChild("body").getChild("head");
        leftArm = root.getChild("root").getChild("body").getChild("left_arm");
        rightArm = root.getChild("root").getChild("body").getChild("right_arm");
        tabard = root.getChild("root").getChild("body").getChild("tabard");
        tabardTip = root.getChild("root").getChild("body").getChild("tabard").getChild("tabard_tip");
        cloak = root.getChild("root").getChild("body").getChild("cloak");
        cloakTip = root.getChild("root").getChild("body").getChild("cloak").getChild("cloak_tip");
        leftDrape = root.getChild("root").getChild("body").getChild("left_drape");
        rightDrape = root.getChild("root").getChild("body").getChild("right_drape");
        leftDrapeTip =
                root.getChild("root").getChild("body").getChild("left_drape").getChild("left_drape_tip");
        rightDrapeTip =
                root.getChild("root").getChild("body").getChild("right_drape").getChild("right_drape_tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root =
                meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(140, 1)
                        .addBox(
                                -3.0F,
                                -8.0F,
                                -2.0F,
                                6.0F,
                                10.0F,
                                4.0F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH))
                        .texOffs(167, 1)
                        .addBox(-4.0F, -7.0F, -3.0F, 8.0F, 6.0F, 1.0F)
                        .texOffs(186, 1)
                        .addBox(-4.5F, 0.0F, -3.0F, 9.0F, 2.0F, 6.0F)
                        .texOffs(217, 1)
                        .addBox(-4.5F, 2.0F, -3.0F, 9.0F, 1.0F, 6.0F),
                PartPose.offset(0.0F, -22.0F, 0.0F));
        body.addOrReplaceChild(
                "left_hip_plate_0",
                CubeListBuilder.create().texOffs(47, 32).addBox(-1.0F, 0.0F, -0.3F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(4.25F, 2.0F, -3.5F, 0.0F, 0.0F, -0.1745329F));
        body.addOrReplaceChild(
                "left_hip_plate_1",
                CubeListBuilder.create().texOffs(54, 32).addBox(-1.0F, 2.0F, 0.75F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(4.25F, 2.0F, -3.5F, 0.0F, 0.0F, -0.1745329F));
        body.addOrReplaceChild(
                "right_hip_plate_0",
                CubeListBuilder.create().texOffs(61, 32).addBox(-1.0F, 0.0F, -0.3F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-4.25F, 2.0F, -3.5F, 0.0F, 0.0F, 0.1745329F));
        body.addOrReplaceChild(
                "right_hip_plate_1",
                CubeListBuilder.create().texOffs(68, 32).addBox(-1.0F, 2.0F, 0.75F, 2.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(-4.25F, 2.0F, -3.5F, 0.0F, 0.0F, 0.1745329F));
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(1, 1)
                        .addBox(-4.0F, -7.0F, 0.0F, 8.0F, 7.0F, 4.0F)
                        .texOffs(26, 1)
                        .addBox(-4.0F, -9.0F, -4.0F, 8.0F, 2.0F, 8.0F)
                        .texOffs(59, 1)
                        .addBox(-4.0F, -7.0F, -4.0F, 1.0F, 6.0F, 4.0F)
                        .texOffs(70, 1)
                        .addBox(3.0F, -7.0F, -4.0F, 1.0F, 6.0F, 4.0F)
                        .texOffs(81, 1)
                        .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 4.0F)
                        .texOffs(106, 1)
                        .addBox(-3.0F, -7.0F, -0.05F, 6.0F, 6.0F, 1.0F, EnumSet.of(Direction.NORTH)),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        head.addOrReplaceChild(
                "hood_swept_fold",
                CubeListBuilder.create().texOffs(121, 1).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 3.0F, 0.3490659F, 0.0F, 0.0F));
        body.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(1, 18)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                        .texOffs(18, 18)
                        .addBox(-2.5F, 6.0F, -2.5F, 5.0F, 3.0F, 5.0F)
                        .texOffs(39, 18)
                        .addBox(-1.5F, 9.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(5.5F, -7.0F, 0.0F, -0.8726646F, -0.0872665F, -0.122173F));
        body.addOrReplaceChild(
                "left_mantle",
                CubeListBuilder.create()
                        .texOffs(52, 18)
                        .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 6.0F)
                        .texOffs(73, 18)
                        .addBox(0.5F, 1.0F, -3.0F, 2.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(5.5F, -7.0F, 0.0F, -0.1047198F, -0.122173F, -0.3141593F));
        PartDefinition leftDrape = body.addOrReplaceChild(
                "left_drape",
                CubeListBuilder.create()
                        .texOffs(90, 18)
                        .addBox(-1.5F, 0.0F, 0.5F, 3.0F, 7.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offsetAndRotation(4.5F, 2.0F, 0.0F, 0.0F, 1.2217305F, 0.122173F));
        leftDrape.addOrReplaceChild(
                "left_drape_tip",
                CubeListBuilder.create()
                        .texOffs(99, 18)
                        .addBox(-1.5F, 0.0F, 0.5F, 3.0F, 8.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        body.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(108, 18)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F)
                        .texOffs(125, 18)
                        .addBox(-2.5F, 6.0F, -2.5F, 5.0F, 3.0F, 5.0F)
                        .texOffs(146, 18)
                        .addBox(-1.5F, 9.0F, -1.5F, 3.0F, 2.0F, 3.0F),
                PartPose.offsetAndRotation(-5.5F, -7.0F, 0.0F, -0.8726646F, 0.0872665F, 0.122173F));
        body.addOrReplaceChild(
                "right_mantle",
                CubeListBuilder.create()
                        .texOffs(159, 18)
                        .addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 6.0F)
                        .texOffs(180, 18)
                        .addBox(-2.5F, 1.0F, -3.0F, 2.0F, 3.0F, 6.0F),
                PartPose.offsetAndRotation(-5.5F, -7.0F, 0.0F, -0.1047198F, 0.122173F, 0.3141593F));
        PartDefinition rightDrape = body.addOrReplaceChild(
                "right_drape",
                CubeListBuilder.create()
                        .texOffs(197, 18)
                        .addBox(-1.5F, 0.0F, 0.5F, 3.0F, 7.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offsetAndRotation(-4.5F, 2.0F, 0.0F, 0.0F, -1.2217305F, -0.122173F));
        rightDrape.addOrReplaceChild(
                "right_drape_tip",
                CubeListBuilder.create()
                        .texOffs(206, 18)
                        .addBox(-1.5F, 0.0F, 0.5F, 3.0F, 8.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition tabard = body.addOrReplaceChild(
                "tabard",
                CubeListBuilder.create()
                        .texOffs(215, 18)
                        .addBox(-3.0F, 0.0F, 0.5F, 6.0F, 8.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offsetAndRotation(0.0F, 2.0F, -3.3F, -0.0698132F, 0.0F, 0.0F));
        tabard.addOrReplaceChild(
                "tabard_tip",
                CubeListBuilder.create()
                        .texOffs(230, 18)
                        .addBox(-3.0F, 0.0F, 0.5F, 6.0F, 9.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition cloak = body.addOrReplaceChild(
                "cloak",
                CubeListBuilder.create()
                        .texOffs(1, 32)
                        .addBox(-5.0F, 0.0F, 0.5F, 10.0F, 12.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 3.3F, 0.1047198F, 0.0F, 0.0F));
        cloak.addOrReplaceChild(
                "cloak_tip",
                CubeListBuilder.create()
                        .texOffs(24, 32)
                        .addBox(-5.0F, 0.0F, 0.5F, 10.0F, 13.0F, 0.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(
            T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partialTicks = ageInTicks - entity.tickCount;
        float armLiftL = entity instanceof EntityEldritchGuardian guardian
                ? guardian.arms().leftLift()
                : entity instanceof EntityEldritchWarden warden ? warden.arms().leftLift() : 0.0F;
        float armLiftR = entity instanceof EntityEldritchGuardian guardian
                ? guardian.arms().rightLift()
                : entity instanceof EntityEldritchWarden warden ? warden.arms().rightLift() : 0.0F;
        float death = Mth.clamp((entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F) / 20.0F, 0.0F, 1.0F);
        float alive = 1.0F - death;
        float phase = ageInTicks * Mth.PI / 20.0F;
        float movement = Math.min(limbSwingAmount, 1.0F) * alive;
        float wave = Mth.sin(phase);
        float trailing = Mth.sin(phase - 0.8F);
        float screech = Math.min(armLiftL, armLiftR) * alive;
        hover.y -= wave * 0.35F * alive;
        body.xRot += (-movement * 0.035F - wave * 0.012F + screech * 0.1F) * alive;
        body.zRot -= wave * 0.007F * alive;
        head.yRot += Mth.clamp(netHeadYaw, -65.0F, 65.0F) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(headPitch, -35.0F, 35.0F) * Mth.DEG_TO_RAD - screech * 0.18F + death * 0.3F;
        leftArm.xRot -= (armLiftL + Mth.sin(phase + 0.6F) * 0.035F) * alive;
        rightArm.xRot -= (armLiftR + Mth.sin(phase - 0.6F) * 0.035F) * alive;
        leftArm.yRot += armLiftL * 0.25F * alive;
        rightArm.yRot -= armLiftR * 0.25F * alive;
        leftArm.zRot -= (wave * 0.017F + screech * 0.2F) * alive;
        rightArm.zRot += (wave * 0.017F + screech * 0.2F) * alive;
        tabard.xRot += (-wave * 0.05F + movement * 0.18F) * alive;
        tabardTip.xRot += (-trailing * 0.085F + movement * 0.08F) * alive;
        cloak.xRot += (-wave * 0.05F + movement * 0.2F + screech * 0.07F) * alive;
        cloakTip.xRot += (-trailing * 0.085F + movement * 0.1F) * alive;
        leftDrape.xRot += (-Mth.sin(phase + 0.4F) * 0.05F + movement * 0.15F) * alive;
        rightDrape.xRot += (-Mth.sin(phase - 0.4F) * 0.05F + movement * 0.15F) * alive;
        leftDrapeTip.xRot -= Mth.sin(phase - 0.4F) * 0.085F * alive;
        rightDrapeTip.xRot -= Mth.sin(phase - 1.2F) * 0.085F * alive;
        leftDrape.zRot += (wave * 0.02F + screech * 0.08F) * alive;
        rightDrape.zRot -= (wave * 0.02F + screech * 0.08F) * alive;
        if (entity.getAttackAnim(partialTicks) > 0.0F) {
            float strike = Mth.sin(Mth.sqrt(entity.getAttackAnim(partialTicks)) * Mth.PI) * alive;
            ModelPart strikingArm = ((entity.swingingArm == net.minecraft.world.InteractionHand.MAIN_HAND
                                    ? entity.getMainArm()
                                    : entity.getMainArm().getOpposite())
                            == net.minecraft.world.entity.HumanoidArm.LEFT)
                    ? leftArm
                    : rightArm;
            strikingArm.xRot -= strike * 0.95F;
            body.yRot += strike
                    * (((entity.swingingArm == net.minecraft.world.InteractionHand.MAIN_HAND
                                            ? entity.getMainArm()
                                            : entity.getMainArm().getOpposite())
                                    == net.minecraft.world.entity.HumanoidArm.LEFT)
                            ? 0.18F
                            : -0.18F);
        }
        float flinch = Mth.sin(Math.max(0.0F, entity.hurtTime - partialTicks) * Mth.PI / 10.0F) * alive;
        body.xRot -= flinch * 0.05F;
        head.xRot -= flinch * 0.04F;
        leftArm.xRot += death * 0.7F;
        rightArm.xRot += death * 0.7F;
        cloak.xRot += death * 0.1F;
        cloakTip.xRot += death * 0.1F;
    }
}
