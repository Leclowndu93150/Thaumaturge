package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.construct.EntityTurretCrossbow;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;

public final class CrossbowModel extends HierarchicalModel<EntityTurretCrossbow> {
    protected final ModelPart root;

    @Override
    public ModelPart root() {
        return root;
    }

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float BOW_SWING = 0.2F;
    private static final float LOAD_SWING = 0.5F;
    private static final float LEG_Y = 12.0F;
    private static final float LEG_Y_MINECART = 4.0F;
    private static final float LEG_SPREAD = 0.5F;
    private static final float LEG_SPREAD_MINECART = 0.1F;

    private final ModelPart crossbow;
    private final ModelPart loader;
    private final ModelPart bowRight;
    private final ModelPart bowLeft;
    private final ModelPart[] legs;
    private final float loaderRest;
    private final float bowRightRest;
    private final float bowLeftRest;

    public CrossbowModel(ModelPart root) {
        this.root = root;
        crossbow = root.getChild("crossbow");
        loader = crossbow.getChild("loader");
        bowRight = crossbow.getChild("bow_right");
        bowLeft = crossbow.getChild("bow_left");
        legs = new ModelPart[] {
            root.getChild("leg1"), root.getChild("leg2"), root.getChild("leg3"), root.getChild("leg4")
        };
        loaderRest = loader.xRot;
        bowRightRest = bowRight.yRot;
        bowLeftRest = bowLeft.yRot;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "tripod",
                CubeListBuilder.create().texOffs(91, 21).addBox(-1.75F, -0.375F, -1.75F, 3.5F, 2.5F, 3.5F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition crossbow = root.addOrReplaceChild(
                "crossbow",
                CubeListBuilder.create()
                        .texOffs(1, 1)
                        .addBox(-2.0F, 0.0F, -8.0F, 4.0F, 2.0F, 17.0F)
                        .texOffs(44, 1)
                        .addBox(-1.5F, -0.5F, 8.5F, 3.0F, 3.0F, 2.5F)
                        .texOffs(56, 1)
                        .addBox(-2.25F, -5.0F, -6.0F, 4.5F, 5.0F, 9.0F)
                        .texOffs(84, 1)
                        .addBox(-2.5F, -1.5F, -4.75F, 5.0F, 1.0F, 1.0F)
                        .texOffs(97, 1)
                        .addBox(-2.5F, -1.5F, 1.25F, 5.0F, 1.0F, 1.0F)
                        .texOffs(110, 1)
                        .addBox(-1.0F, -0.5F, -10.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        crossbow.addOrReplaceChild(
                "bow_right",
                CubeListBuilder.create()
                        .texOffs(1, 21)
                        .addBox(-5.0F, -1.5F, -0.75F, 5.0F, 2.0F, 1.5F)
                        .texOffs(15, 21)
                        .addBox(-8.0F, -1.25F, 0.0F, 3.5F, 1.5F, 1.0F)
                        .texOffs(25, 21)
                        .addBox(-9.5F, -1.0F, 0.75F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 1.0F, -6.5F, 0.0F, 0.2443461F, 0.0F));
        crossbow.addOrReplaceChild(
                "bow_left",
                CubeListBuilder.create()
                        .texOffs(32, 21)
                        .addBox(0.0F, -1.5F, -0.75F, 5.0F, 2.0F, 1.5F)
                        .texOffs(46, 21)
                        .addBox(4.5F, -1.25F, 0.0F, 3.5F, 1.5F, 1.0F)
                        .texOffs(56, 21)
                        .addBox(7.5F, -1.0F, 0.75F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 1.0F, -6.5F, 0.0F, -0.2443461F, 0.0F));
        crossbow.addOrReplaceChild(
                "loader",
                CubeListBuilder.create()
                        .texOffs(63, 21)
                        .addBox(-3.375F, -9.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                        .texOffs(70, 21)
                        .addBox(2.375F, -9.0F, -1.0F, 1.0F, 9.0F, 2.0F)
                        .texOffs(77, 21)
                        .addBox(-2.75F, -9.5F, -0.5F, 5.5F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585054F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "leg1",
                CubeListBuilder.create()
                        .texOffs(106, 21)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(115, 21)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.4991642F, 0.0F, 0.0F));
        root.addOrReplaceChild(
                "leg2",
                CubeListBuilder.create()
                        .texOffs(1, 35)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(10, 35)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.4991642F, 1.5707963F, 0.0F));
        root.addOrReplaceChild(
                "leg3",
                CubeListBuilder.create()
                        .texOffs(21, 35)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(30, 35)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.4991642F, 3.1415927F, 0.0F));
        root.addOrReplaceChild(
                "leg4",
                CubeListBuilder.create()
                        .texOffs(41, 35)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(50, 35)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.4991642F, 4.712389F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(
            EntityTurretCrossbow entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partialTicks = ageInTicks - entity.tickCount;
        crossbow.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        crossbow.xRot = headPitch * Mth.DEG_TO_RAD;
        float swing = Mth.sin(Mth.sqrt(entity.swingAnim) * Mth.TWO_PI) * BOW_SWING;
        bowRight.yRot = bowRightRest - swing;
        bowLeft.yRot = bowLeftRest + swing;
        loader.xRot = loaderRest + Mth.sin(Mth.sqrt(entity.getLoadProgress(partialTicks)) * Mth.TWO_PI) * LOAD_SWING;
        for (ModelPart leg : legs) {
            leg.y = (entity.getVehicle() instanceof AbstractMinecart) ? LEG_Y_MINECART : LEG_Y;
            leg.xRot = (entity.getVehicle() instanceof AbstractMinecart) ? LEG_SPREAD_MINECART : LEG_SPREAD;
        }
    }
}
