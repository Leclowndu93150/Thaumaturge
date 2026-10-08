package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class HierophantGeometry {
    private HierophantGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root =
                partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(1, 1)
                        .addBox(-10.0F, -17.0F, -5.0F, 20.0F, 20.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 62)
                        .addBox(-13.0F, -19.0F, -7.0F, 26.0F, 7.0F, 15.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 86)
                        .addBox(-10.0F, 1.0F, -6.0F, 20.0F, 5.0F, 13.0F, new CubeDeformation(0.025F))
                        .texOffs(1, 151)
                        .addBox(-10.0F, 2.0F, -7.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.05F))
                        .texOffs(1, 140)
                        .addBox(-3.0F, 0.0F, -8.0F, 6.0F, 7.0F, 2.0F, new CubeDeformation(0.05F))
                        .texOffs(31, 124)
                        .addBox(-6.0F, -13.0F, -6.0F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(109, 124)
                        .addBox(-4.0F, -14.0F, -7.0F, 8.0F, 10.0F, 1.0F, new CubeDeformation(0.025F))
                        .texOffs(69, 86)
                        .addBox(7.0F, -16.0F, -7.0F, 3.0F, 17.0F, 1.0F, new CubeDeformation(0.012F))
                        .texOffs(69, 86)
                        .addBox(-10.0F, -16.0F, -7.0F, 3.0F, 17.0F, 1.0F, new CubeDeformation(0.012F)),
                PartPose.offset(0.0F, -46.0F, 0.0F));

        PartDefinition collar_right = body.addOrReplaceChild(
                "collar_right",
                CubeListBuilder.create()
                        .texOffs(81, 34)
                        .addBox(0.0F, -11.0F, -7.0F, 5.0F, 10.0F, 14.0F, new CubeDeformation(0.025F))
                        .texOffs(127, 106)
                        .addBox(0.0F, -11.0F, -7.0F, 5.0F, 1.0F, 14.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(8.0F, -13.0F, 0.0F, 0.0F, 0.0F, 0.2094F));

        PartDefinition collar_left = body.addOrReplaceChild(
                "collar_left",
                CubeListBuilder.create()
                        .texOffs(81, 34)
                        .addBox(-5.0F, -11.0F, -7.0F, 5.0F, 10.0F, 14.0F, new CubeDeformation(0.025F))
                        .texOffs(127, 106)
                        .addBox(-5.0F, -11.0F, -7.0F, 5.0F, 1.0F, 14.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-8.0F, -13.0F, 0.0F, 0.0F, 0.0F, -0.2094F));

        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(143, 1)
                        .addBox(-10.0F, -21.0F, 2.0F, 20.0F, 20.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(193, 86)
                        .addBox(-9.0F, -23.0F, -9.0F, 18.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(167, 106)
                        .addBox(-6.0F, -25.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(79, 86)
                        .addBox(-7.0F, -19.0F, -7.0F, 14.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(181, 140)
                        .addBox(-8.0F, -20.0F, -10.0F, 16.0F, 3.0F, 2.0F, new CubeDeformation(0.025F))
                        .texOffs(217, 106)
                        .addBox(6.0F, -17.0F, -9.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.035F))
                        .texOffs(217, 106)
                        .addBox(-8.0F, -17.0F, -9.0F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.035F))
                        .texOffs(47, 140)
                        .addBox(5.0F, -6.0F, -9.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.035F))
                        .texOffs(47, 140)
                        .addBox(-7.0F, -6.0F, -9.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.035F))
                        .texOffs(57, 140)
                        .addBox(-2.0F, -25.0F, -10.0F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.05F)),
                PartPose.offset(0.0F, -18.0F, 0.0F));

        PartDefinition hood_cheek_left_r1 = head.addOrReplaceChild(
                "hood_cheek_left_r1",
                CubeListBuilder.create()
                        .texOffs(107, 1)
                        .addBox(-2.0F, -8.5F, -6.5F, 4.0F, 17.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-9.0F, -10.5F, -2.5F, 0.0F, 0.0F, 0.0873F));

        PartDefinition hood_cheek_right_r1 = head.addOrReplaceChild(
                "hood_cheek_right_r1",
                CubeListBuilder.create()
                        .texOffs(107, 1)
                        .addBox(-2.0F, -8.5F, -6.5F, 4.0F, 17.0F, 13.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(9.0F, -10.5F, -2.5F, 0.0F, 0.0F, -0.0873F));

        PartDefinition eye = head.addOrReplaceChild(
                "eye",
                CubeListBuilder.create()
                        .texOffs(19, 140)
                        .addBox(-6.0F, -4.0F, 0.0F, 12.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -12.0F, -9.0F));

        PartDefinition halo =
                body.addOrReplaceChild("halo", CubeListBuilder.create(), PartPose.offset(0.0F, -30.0F, 10.0F));

        PartDefinition halo_segment_0 = halo.addOrReplaceChild(
                "halo_segment_0",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(227, 106)
                        .addBox(-2.0F, -15.0F, -1.0F, 4.0F, 11.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -18.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offset(0.0F, -26.0F, 0.0F));

        PartDefinition halo_segment_1 = halo.addOrReplaceChild(
                "halo_segment_1",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(187, 124)
                        .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-18.3848F, -18.3848F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition halo_segment_2 = halo.addOrReplaceChild(
                "halo_segment_2",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(187, 124)
                        .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-26.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition halo_segment_3 = halo.addOrReplaceChild(
                "halo_segment_3",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(187, 124)
                        .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-18.3848F, 18.3848F, 0.0F, 0.0F, 0.0F, -2.3562F));

        PartDefinition halo_segment_4 = halo.addOrReplaceChild(
                "halo_segment_4",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(0.0F, 26.0F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition halo_segment_5 = halo.addOrReplaceChild(
                "halo_segment_5",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(187, 124)
                        .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(18.3848F, 18.3848F, 0.0F, 0.0F, 0.0F, -3.927F));

        PartDefinition halo_segment_6 = halo.addOrReplaceChild(
                "halo_segment_6",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(187, 124)
                        .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(26.0F, 0.0F, 0.0F, 0.0F, 0.0F, -4.7124F));

        PartDefinition halo_segment_7 = halo.addOrReplaceChild(
                "halo_segment_7",
                CubeListBuilder.create()
                        .texOffs(129, 124)
                        .addBox(-8.0F, -3.0F, -2.0F, 16.0F, 6.0F, 5.0F, new CubeDeformation(0.025F))
                        .texOffs(95, 140)
                        .addBox(-8.0F, -4.0F, -2.0F, 16.0F, 1.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(187, 124)
                        .addBox(-2.0F, -11.0F, -1.0F, 4.0F, 7.0F, 3.0F, new CubeDeformation(0.025F))
                        .texOffs(139, 140)
                        .addBox(-1.0F, -14.0F, -1.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(18.3848F, -18.3848F, 0.0F, 0.0F, 0.0F, -5.4978F));

        PartDefinition arm_right = body.addOrReplaceChild(
                "arm_right",
                CubeListBuilder.create()
                        .texOffs(183, 34)
                        .addBox(0.0F, -1.0F, -3.0F, 6.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(16.0F, -13.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

        PartDefinition pauldron_right = arm_right.addOrReplaceChild(
                "pauldron_right",
                CubeListBuilder.create()
                        .texOffs(85, 62)
                        .addBox(-5.0F, -4.0F, -7.0F, 16.0F, 6.0F, 15.0F, new CubeDeformation(0.025F))
                        .texOffs(1, 106)
                        .addBox(-5.0F, -4.0F, -7.0F, 16.0F, 1.0F, 15.0F, new CubeDeformation(0.05F))
                        .texOffs(113, 86)
                        .addBox(-1.0F, 1.0F, -6.0F, 12.0F, 5.0F, 13.0F, new CubeDeformation(0.025F))
                        .texOffs(165, 86)
                        .addBox(7.0F, 2.0F, -4.0F, 4.0F, 9.0F, 9.0F, new CubeDeformation(0.025F)),
                PartPose.offsetAndRotation(1.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition forearm_right = arm_right.addOrReplaceChild(
                "forearm_right",
                CubeListBuilder.create()
                        .texOffs(203, 124)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(121, 34)
                        .addBox(-4.0F, 1.0F, -5.0F, 8.0F, 14.0F, 10.0F, new CubeDeformation(0.025F))
                        .texOffs(59, 124)
                        .addBox(-4.0F, 13.0F, -5.0F, 8.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(3.0F, 15.0F, 0.0F, -0.1047F, 0.0F, 0.2094F));

        PartDefinition hand_right = forearm_right.addOrReplaceChild(
                "hand_right",
                CubeListBuilder.create()
                        .texOffs(65, 106)
                        .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 9.0F, 7.0F, new CubeDeformation(0.035F))
                        .texOffs(151, 140)
                        .addBox(-2.0F, 0.0F, -5.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.025F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition finger_right_0 = hand_right.addOrReplaceChild(
                "finger_right_0",
                CubeListBuilder.create()
                        .texOffs(223, 124)
                        .addBox(-1.0F, 0.0F, -2.0F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.035F)),
                PartPose.offsetAndRotation(2.0F, 7.0F, -1.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition finger_tip_right_0 = finger_right_0.addOrReplaceChild(
                "finger_tip_right_0",
                CubeListBuilder.create()
                        .texOffs(69, 140)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 7.0F, -1.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition finger_right_1 = hand_right.addOrReplaceChild(
                "finger_right_1",
                CubeListBuilder.create()
                        .texOffs(97, 124)
                        .addBox(-1.0F, 0.0F, -2.0F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.035F)),
                PartPose.offset(-1.0F, 7.0F, -1.0F));

        PartDefinition finger_tip_right_1 = finger_right_1.addOrReplaceChild(
                "finger_tip_right_1",
                CubeListBuilder.create()
                        .texOffs(69, 140)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 9.0F, -1.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition finger_right_2 = hand_right.addOrReplaceChild(
                "finger_right_2",
                CubeListBuilder.create()
                        .texOffs(223, 124)
                        .addBox(-1.0F, 0.0F, -2.0F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.035F)),
                PartPose.offsetAndRotation(-4.0F, 7.0F, -1.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition finger_tip_right_2 = finger_right_2.addOrReplaceChild(
                "finger_tip_right_2",
                CubeListBuilder.create()
                        .texOffs(69, 140)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 7.0F, -1.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition thumb_right = hand_right.addOrReplaceChild(
                "thumb_right",
                CubeListBuilder.create()
                        .texOffs(173, 124)
                        .addBox(-3.0F, -1.0F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.035F))
                        .texOffs(219, 140)
                        .addBox(-3.0F, 6.0F, -3.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.6109F));

        PartDefinition arm_left = body.addOrReplaceChild(
                "arm_left",
                CubeListBuilder.create()
                        .texOffs(183, 34)
                        .addBox(-6.0F, -1.0F, -3.0F, 6.0F, 16.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-16.0F, -13.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

        PartDefinition pauldron_left = arm_left.addOrReplaceChild(
                "pauldron_left",
                CubeListBuilder.create()
                        .texOffs(85, 62)
                        .addBox(-11.0F, -4.0F, -7.0F, 16.0F, 6.0F, 15.0F, new CubeDeformation(0.025F))
                        .texOffs(1, 106)
                        .addBox(-11.0F, -4.0F, -7.0F, 16.0F, 1.0F, 15.0F, new CubeDeformation(0.05F))
                        .texOffs(113, 86)
                        .addBox(-11.0F, 1.0F, -6.0F, 12.0F, 5.0F, 13.0F, new CubeDeformation(0.025F))
                        .texOffs(165, 86)
                        .addBox(-11.0F, 2.0F, -4.0F, 4.0F, 9.0F, 9.0F, new CubeDeformation(0.025F)),
                PartPose.offsetAndRotation(-1.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition forearm_left = arm_left.addOrReplaceChild(
                "forearm_left",
                CubeListBuilder.create()
                        .texOffs(203, 124)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(121, 34)
                        .addBox(-4.0F, 1.0F, -5.0F, 8.0F, 14.0F, 10.0F, new CubeDeformation(0.025F))
                        .texOffs(59, 124)
                        .addBox(-4.0F, 13.0F, -5.0F, 8.0F, 3.0F, 10.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(-3.0F, 15.0F, 0.0F, 0.1745F, 0.0F, -0.2094F));

        PartDefinition hand_left = forearm_left.addOrReplaceChild(
                "hand_left",
                CubeListBuilder.create()
                        .texOffs(65, 106)
                        .addBox(-4.0F, -1.0F, -4.0F, 8.0F, 9.0F, 7.0F, new CubeDeformation(0.035F))
                        .texOffs(151, 140)
                        .addBox(-2.0F, 0.0F, -5.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.025F)),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition finger_left_0 = hand_left.addOrReplaceChild(
                "finger_left_0",
                CubeListBuilder.create()
                        .texOffs(223, 124)
                        .addBox(-1.0F, 0.0F, -2.0F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.035F)),
                PartPose.offsetAndRotation(2.0F, 7.0F, -1.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition finger_tip_left_0 = finger_left_0.addOrReplaceChild(
                "finger_tip_left_0",
                CubeListBuilder.create()
                        .texOffs(69, 140)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 7.0F, -1.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition finger_left_1 = hand_left.addOrReplaceChild(
                "finger_left_1",
                CubeListBuilder.create()
                        .texOffs(97, 124)
                        .addBox(-1.0F, 0.0F, -2.0F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.035F)),
                PartPose.offset(-1.0F, 7.0F, -1.0F));

        PartDefinition finger_tip_left_1 = finger_left_1.addOrReplaceChild(
                "finger_tip_left_1",
                CubeListBuilder.create()
                        .texOffs(69, 140)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 9.0F, -1.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition finger_left_2 = hand_left.addOrReplaceChild(
                "finger_left_2",
                CubeListBuilder.create()
                        .texOffs(223, 124)
                        .addBox(-1.0F, 0.0F, -2.0F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.035F)),
                PartPose.offsetAndRotation(-4.0F, 7.0F, -1.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition finger_tip_left_2 = finger_left_2.addOrReplaceChild(
                "finger_tip_left_2",
                CubeListBuilder.create()
                        .texOffs(69, 140)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 7.0F, -1.0F, 0.4712F, 0.0F, 0.0F));

        PartDefinition thumb_left = hand_left.addOrReplaceChild(
                "thumb_left",
                CubeListBuilder.create()
                        .texOffs(173, 124)
                        .addBox(-1.0F, -1.0F, -2.0F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.035F))
                        .texOffs(219, 140)
                        .addBox(-1.0F, 6.0F, -3.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, 2.0F, 0.0F, 0.0F, 0.0F, -0.6109F));

        PartDefinition robes = body.addOrReplaceChild(
                "robes",
                CubeListBuilder.create()
                        .texOffs(23, 34)
                        .addBox(-9.0F, 1.0F, -4.0F, 18.0F, 15.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition skirt_right = robes.addOrReplaceChild(
                "skirt_right",
                CubeListBuilder.create()
                        .texOffs(65, 1)
                        .addBox(-1.0F, -1.0F, -4.0F, 8.0F, 19.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(8.0F, 2.0F, 0.0F, 0.0F, 0.0F, -0.192F));

        PartDefinition skirt_tail_right = skirt_right.addOrReplaceChild(
                "skirt_tail_right",
                CubeListBuilder.create()
                        .texOffs(197, 1)
                        .addBox(-5.0F, -1.0F, -5.0F, 8.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(97, 106)
                        .addBox(0.0F, 13.0F, -5.0F, 3.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 124)
                        .addBox(-5.0F, 13.0F, -5.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.0F, 17.0F, 2.0F, 0.1396F, 0.0F, -0.0873F));

        PartDefinition stole_right = robes.addOrReplaceChild(
                "stole_right",
                CubeListBuilder.create()
                        .texOffs(237, 1)
                        .addBox(-2.0F, 0.0F, -1.0F, 5.0F, 24.0F, 2.0F, new CubeDeformation(0.012F)),
                PartPose.offsetAndRotation(6.0F, 1.0F, -6.0F, 0.0F, 0.0F, -0.0873F));

        PartDefinition stole_tip_right = stole_right.addOrReplaceChild(
                "stole_tip_right",
                CubeListBuilder.create()
                        .texOffs(235, 124)
                        .addBox(-2.0F, 0.0F, -1.0F, 5.0F, 8.0F, 2.0F, new CubeDeformation(0.012F))
                        .texOffs(231, 140)
                        .addBox(-1.0F, 8.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.1571F, 0.0F, 0.0F));

        PartDefinition skirt_left = robes.addOrReplaceChild(
                "skirt_left",
                CubeListBuilder.create()
                        .texOffs(65, 1)
                        .addBox(-7.0F, -1.0F, -4.0F, 8.0F, 19.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-8.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.192F));

        PartDefinition skirt_tail_left = skirt_left.addOrReplaceChild(
                "skirt_tail_left",
                CubeListBuilder.create()
                        .texOffs(197, 1)
                        .addBox(-3.0F, -1.0F, -5.0F, 8.0F, 15.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(97, 106)
                        .addBox(2.0F, 13.0F, -5.0F, 3.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 124)
                        .addBox(-3.0F, 13.0F, -5.0F, 3.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, 17.0F, 2.0F, 0.1396F, 0.0F, 0.0873F));

        PartDefinition stole_left = robes.addOrReplaceChild(
                "stole_left",
                CubeListBuilder.create()
                        .texOffs(237, 1)
                        .addBox(-3.0F, 0.0F, -1.0F, 5.0F, 24.0F, 2.0F, new CubeDeformation(0.012F)),
                PartPose.offsetAndRotation(-6.0F, 1.0F, -6.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition stole_tip_left = stole_left.addOrReplaceChild(
                "stole_tip_left",
                CubeListBuilder.create()
                        .texOffs(235, 124)
                        .addBox(-3.0F, 0.0F, -1.0F, 5.0F, 8.0F, 2.0F, new CubeDeformation(0.012F))
                        .texOffs(231, 140)
                        .addBox(-2.0F, 8.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.1571F, 0.0F, 0.0F));

        PartDefinition tabard = robes.addOrReplaceChild(
                "tabard",
                CubeListBuilder.create()
                        .texOffs(1, 34)
                        .addBox(-4.0F, -1.0F, -1.0F, 8.0F, 24.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(163, 140)
                        .addBox(-3.0F, 23.0F, -1.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(243, 140)
                        .addBox(-1.0F, 27.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.05F)),
                PartPose.offsetAndRotation(0.0F, 2.0F, -5.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition cape = body.addOrReplaceChild(
                "cape",
                CubeListBuilder.create()
                        .texOffs(149, 62)
                        .addBox(-12.0F, 0.0F, 0.0F, 24.0F, 18.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -16.0F, 7.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition cape_panel_0 = cape.addOrReplaceChild(
                "cape_panel_0",
                CubeListBuilder.create()
                        .texOffs(159, 34)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 21.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(8.0F, 17.0F, 2.0F, 0.0698F, 0.0F, 0.0F));

        PartDefinition cape_tip_0 = cape_panel_0.addOrReplaceChild(
                "cape_tip_0",
                CubeListBuilder.create()
                        .texOffs(205, 62)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 18.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(79, 140)
                        .addBox(-2.0F, 18.0F, -1.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 20.0F, 1.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition cape_panel_1 = cape.addOrReplaceChild(
                "cape_panel_1",
                CubeListBuilder.create()
                        .texOffs(159, 34)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 21.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 17.0F, 2.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition cape_tip_1 = cape_panel_1.addOrReplaceChild(
                "cape_tip_1",
                CubeListBuilder.create()
                        .texOffs(159, 34)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 21.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(79, 140)
                        .addBox(-2.0F, 21.0F, -1.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 20.0F, 1.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition cape_panel_2 = cape.addOrReplaceChild(
                "cape_panel_2",
                CubeListBuilder.create()
                        .texOffs(159, 34)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 21.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-8.0F, 17.0F, 2.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition cape_tip_2 = cape_panel_2.addOrReplaceChild(
                "cape_tip_2",
                CubeListBuilder.create()
                        .texOffs(205, 62)
                        .addBox(-4.0F, 0.0F, -1.0F, 8.0F, 18.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(79, 140)
                        .addBox(-2.0F, 18.0F, -1.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 20.0F, 1.0F, 0.1396F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }
}
