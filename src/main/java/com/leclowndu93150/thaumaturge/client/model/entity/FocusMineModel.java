package com.leclowndu93150.thaumaturge.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class FocusMineModel {
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 16;
    private static final float CENTER_HEIGHT = 2.0F;
    private static final float CORE_TILT_X = 45.0F * Mth.DEG_TO_RAD;
    private static final float CORE_TILT_Z = 35.26F * Mth.DEG_TO_RAD;
    private static final float SPIKE_SPREAD = 109.47F * Mth.DEG_TO_RAD;
    private static final int LOWER_SPIKES = 3;
    private static final float LOWER_SPIKE_STEP = 120.0F * Mth.DEG_TO_RAD;

    public final ModelPart root;

    public FocusMineModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition center = root.addOrReplaceChild("center", CubeListBuilder.create(), PartPose.offset(0.0F, CENTER_HEIGHT, 0.0F));
        center.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.rotation(CORE_TILT_X, 0.0F, CORE_TILT_Z));
        center.addOrReplaceChild("spike_up", spike(), PartPose.ZERO);
        for (int i = 0; i < LOWER_SPIKES; i++) {
            center.addOrReplaceChild("spike_" + i, spike(), PartPose.rotation(SPIKE_SPREAD, i * LOWER_SPIKE_STEP, 0.0F));
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static CubeListBuilder spike() {
        return CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 3.0F, 2.0F).texOffs(20, 0).addBox(-0.5F, 4.0F, -0.5F, 1.0F, 2.0F, 1.0F);
    }
}
