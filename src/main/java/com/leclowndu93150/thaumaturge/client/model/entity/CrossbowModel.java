package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.TurretCrossbowRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class CrossbowModel extends EntityModel<TurretCrossbowRenderState> {
    private static final float LEG_SPREAD = (float) (Math.PI / 6);
    private static final float CROSS_ANGLE = 0.2443461F;
    private static final float LOADBAR_ANGLE = -0.5585054F;

    private final ModelPart crossbow;
    private final ModelPart loadbarcross;
    private final ModelPart loadbarl;
    private final ModelPart loadbarr;
    private final ModelPart crossl1;
    private final ModelPart crossl2;
    private final ModelPart crossl3;
    private final ModelPart crossr1;
    private final ModelPart crossr2;
    private final ModelPart crossr3;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;

    public CrossbowModel(ModelPart root) {
        super(root);
        crossbow = root.getChild("crossbow");
        loadbarcross = crossbow.getChild("loadbarcross");
        loadbarl = crossbow.getChild("loadbarl");
        loadbarr = crossbow.getChild("loadbarr");
        crossl1 = crossbow.getChild("crossl1");
        crossl2 = crossbow.getChild("crossl2");
        crossl3 = crossbow.getChild("crossl3");
        crossr1 = crossbow.getChild("crossr1");
        crossr2 = crossbow.getChild("crossr2");
        crossr3 = crossbow.getChild("crossr3");
        leg1 = root.getChild("leg1");
        leg2 = root.getChild("leg2");
        leg3 = root.getChild("leg3");
        leg4 = root.getChild("leg4");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition crossbow = root.addOrReplaceChild("crossbow", CubeListBuilder.create().texOffs(28, 14).mirror().addBox(-2.0F, 0.0F, -7.0F, 4, 2, 14), PartPose.offset(0.0F, 10.0F, 0.0F));
        crossbow.addOrReplaceChild("ammobox", CubeListBuilder.create().texOffs(38, 0).mirror().addBox(-2.0F, -5.0F, -6.0F, 4, 5, 9), PartPose.ZERO);
        crossbow.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(20, 28).mirror().addBox(-1.0F, -1.0F, -8.0F, 2, 2, 2), PartPose.ZERO);
        crossbow.addOrReplaceChild("basebarcross", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.0F, 0.5F, 10.0F, 4, 1, 1), PartPose.ZERO);
        crossbow.addOrReplaceChild("basebarr", CubeListBuilder.create().texOffs(40, 23).mirror().addBox(-1.0F, 0.0F, 7.0F, 1, 2, 5), PartPose.rotation(0.0F, -0.1396263F, 0.0F));
        crossbow.addOrReplaceChild("basebarl", CubeListBuilder.create().texOffs(40, 23).mirror().addBox(0.0F, 0.0F, 7.0F, 1, 2, 5), PartPose.rotation(0.0F, 0.1396263F, 0.0F));
        crossbow.addOrReplaceChild("loadbarcross", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-2.0F, -8.5F, -0.5F, 4, 1, 1), PartPose.rotation(LOADBAR_ANGLE, 0.0F, 0.0F));
        crossbow.addOrReplaceChild("loadbarl", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(2.0F, -9.0F, -1.0F, 1, 11, 2), PartPose.rotation(LOADBAR_ANGLE, 0.0F, 0.0F));
        crossbow.addOrReplaceChild("loadbarr", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-3.0F, -9.0F, -1.0F, 1, 11, 2), PartPose.rotation(LOADBAR_ANGLE, 0.0F, 0.0F));
        crossbow.addOrReplaceChild("crossl1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, 0.0F, -6.0F, 5, 2, 1), PartPose.rotation(0.0F, -CROSS_ANGLE, 0.0F));
        crossbow.addOrReplaceChild("crossl2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(4.0F, 0.0F, -5.0F, 3, 2, 1), PartPose.rotation(0.0F, -CROSS_ANGLE, 0.0F));
        crossbow.addOrReplaceChild("crossl3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(6.0F, 0.0F, -4.0F, 2, 2, 1), PartPose.rotation(0.0F, -CROSS_ANGLE, 0.0F));
        crossbow.addOrReplaceChild("crossr1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-5.0F, 0.0F, -6.0F, 5, 2, 1), PartPose.rotation(0.0F, CROSS_ANGLE, 0.0F));
        crossbow.addOrReplaceChild("crossr2", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0F, 0.0F, -5.0F, 3, 2, 1), PartPose.rotation(0.0F, CROSS_ANGLE, 0.0F));
        crossbow.addOrReplaceChild("crossr3", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8.0F, 0.0F, -4.0F, 2, 2, 1), PartPose.rotation(0.0F, CROSS_ANGLE, 0.0F));
        addTripod(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    static void addTripod(PartDefinition root) {
        root.addOrReplaceChild("tripod", CubeListBuilder.create().texOffs(13, 0).mirror().addBox(-1.5F, 0.0F, -1.5F, 3, 2, 3), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(20, 10).mirror().addBox(-1.0F, 1.0F, -1.0F, 2, 13, 2), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, LEG_SPREAD, 0.0F, 0.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(20, 10).mirror().addBox(-1.0F, 1.0F, -1.0F, 2, 13, 2),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, LEG_SPREAD, 1.570796F, 0.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(20, 10).mirror().addBox(-1.0F, 1.0F, -1.0F, 2, 13, 2),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, LEG_SPREAD, 3.141593F, 0.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(20, 10).mirror().addBox(-1.0F, 1.0F, -1.0F, 2, 13, 2),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, LEG_SPREAD, (float) (Math.PI * 3.0 / 2.0), 0.0F));
    }

    @Override
    public void setupAnim(TurretCrossbowRenderState state) {
        super.setupAnim(state);
        crossbow.yRot = state.yRot * Mth.DEG_TO_RAD;
        crossbow.xRot = state.xRot * Mth.DEG_TO_RAD;
        float swing = state.swingAnim;
        float crossSwing = Mth.sin(Mth.sqrt(swing) * Mth.TWO_PI) * 0.2F;
        crossl1.yRot = crossl2.yRot = crossl3.yRot = -0.2F + crossSwing;
        crossr1.yRot = crossr2.yRot = crossr3.yRot = 0.2F - crossSwing;
        float load = state.loadProgress;
        loadbarcross.xRot = loadbarl.xRot = loadbarr.xRot = -0.5F + Mth.sin(Mth.sqrt(load) * Mth.TWO_PI) * 0.5F;
        setupTripod(state.ridingMinecart, leg1, leg2, leg3, leg4);
    }

    static void setupTripod(boolean ridingMinecart, ModelPart... legs) {
        for (ModelPart leg : legs) {
            if (ridingMinecart) {
                leg.y = 12.0F - 8.0F;
                leg.xRot = 0.1F;
            } else {
                leg.y = 12.0F;
                leg.xRot = 0.5F;
            }
        }
    }
}
