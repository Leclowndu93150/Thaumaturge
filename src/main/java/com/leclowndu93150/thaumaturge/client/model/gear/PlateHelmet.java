package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

final class PlateHelmet {
    static final String HELMET = "helmet";
    static final String MASK = "mask";
    static final String GOGGLES = "goggles";

    private static final PartPose HELMET_POSE = PartPose.offset(0.0F, -1.0F, 0.0F);
    private static final PartPose FLAPS_POSE = PartPose.offset(4.0F, -3.0F, -4.0F);
    private static final PartPose MASK_MOUNT = PartPose.offset(4.5F, -4.0F, -5.5F);
    private static final PartPose MASK_POSE = PartPose.offsetAndRotation(-0.5F, 2.0F, 0.5F, 0.0873F, 0.0F, 0.0F);
    private static final CubeDeformation PLATE = new CubeDeformation(-0.5F);
    private static final CubeDeformation VISOR = new CubeDeformation(-0.51F);
    private static final int GOGGLE_U = 176;
    private static final int GOGGLE_V = 0;

    private PlateHelmet() {}

    static PartDefinition shell(PartDefinition head) {
        PartDefinition helmet = head.addOrReplaceChild(
                HELMET,
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -9.0F, -5.0F, 10.0F, 6.0F, 10.0F, PLATE)
                        .texOffs(6, 32)
                        .addBox(-5.5F, -5.25F, -6.5F, 11.0F, 2.0F, 3.0F, PLATE),
                HELMET_POSE);
        helmet.addOrReplaceChild(
                "right_guard",
                CubeListBuilder.create()
                        .texOffs(8, 16)
                        .mirror()
                        .addBox(-0.5F, -2.5F, -1.0F, 2.0F, 6.0F, 10.0F, PLATE)
                        .mirror(false),
                PartPose.offsetAndRotation(4.5F, -1.5F, -4.0F, 0.0F, 0.0F, -0.2618F));
        helmet.addOrReplaceChild(
                "neck_guard",
                CubeListBuilder.create().texOffs(8, 16).addBox(-1.5F, -2.5F, -1.0F, 2.0F, 6.0F, 10.0F, PLATE),
                PartPose.offsetAndRotation(4.0F, -1.75F, 5.0F, 1.5708F, -1.309F, -1.5708F));
        helmet.addOrReplaceChild(
                "left_guard",
                CubeListBuilder.create().texOffs(8, 16).addBox(-1.5F, -2.5F, -1.0F, 2.0F, 6.0F, 10.0F, PLATE),
                PartPose.offsetAndRotation(-4.5F, -1.5F, -4.0F, 0.0F, 0.0F, 0.2618F));
        return helmet;
    }

    static PartDefinition flaps(PartDefinition helmet, String name, CubeListBuilder cubes) {
        PartDefinition flaps = helmet.addOrReplaceChild(name, cubes, FLAPS_POSE);
        flaps.addOrReplaceChild(
                "left_flap",
                CubeListBuilder.create().texOffs(22, 21).addBox(-2.5F, -2.5F, -1.0F, 3.0F, 3.0F, 2.0F, PLATE),
                PartPose.offsetAndRotation(-9.0F, 1.5F, -0.5F, 0.0F, -0.6981F, 0.2618F));
        flaps.addOrReplaceChild(
                "right_flap",
                CubeListBuilder.create()
                        .texOffs(22, 21)
                        .mirror()
                        .addBox(-0.5F, -2.5F, -1.0F, 3.0F, 3.0F, 2.0F, PLATE)
                        .mirror(false),
                PartPose.offsetAndRotation(1.0F, 1.5F, -0.5F, 0.0F, 0.6981F, -0.2618F));
        return flaps;
    }

    static PartDefinition mask(PartDefinition helmet) {
        return helmet.addOrReplaceChild(MASK, CubeListBuilder.create(), MASK_MOUNT);
    }

    static void visor(PartDefinition mask, String name, int u, int v) {
        mask.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(u, v).addBox(-9.0F, -3.0F, 0.0F, 10.0F, 7.0F, 2.0F, VISOR),
                MASK_POSE);
    }

    static void goggles(PartDefinition mask) {
        mask.addOrReplaceChild(
                GOGGLES,
                CubeListBuilder.create()
                        .texOffs(GOGGLE_U, GOGGLE_V)
                        .addBox(-8.0F, -1.5F, -0.5F, 4.0F, 4.0F, 1.0F)
                        .texOffs(GOGGLE_U, GOGGLE_V)
                        .addBox(-4.0F, -1.5F, -0.5F, 4.0F, 4.0F, 1.0F),
                MASK_POSE);
    }
}
