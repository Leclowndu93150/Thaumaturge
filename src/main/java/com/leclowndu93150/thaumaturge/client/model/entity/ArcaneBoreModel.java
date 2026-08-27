package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public final class ArcaneBoreModel extends HierarchicalModel<EntityArcaneBore> {
    private final ModelPart root;
    private final ModelPart base;

    public ArcaneBoreModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        base = root.getChild("base");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition base = root.addOrReplaceChild(
                "base",
                CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        base.addOrReplaceChild(
                "crystal", CubeListBuilder.create().texOffs(32, 25).addBox(-1.0F, -4.0F, 5.0F, 2, 2, 2), PartPose.ZERO);
        base.addOrReplaceChild(
                "domebase",
                CubeListBuilder.create().texOffs(32, 19).addBox(-2.0F, -5.0F, 3.0F, 4, 4, 1),
                PartPose.ZERO);
        base.addOrReplaceChild(
                "dome", CubeListBuilder.create().texOffs(44, 16).addBox(-2.0F, -5.0F, 4.0F, 4, 4, 4), PartPose.ZERO);
        base.addOrReplaceChild(
                "magbase",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, -4.0F, -6.0F, 2, 2, 3),
                PartPose.ZERO);
        base.addOrReplaceChild(
                "tip",
                CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-1.5F, 0.0F, -1.5F, 3, 3, 3),
                PartPose.offsetAndRotation(0.0F, -3.0F, -6.0F, -1.570796F, 0.0F, 0.0F));
        CrossbowModel.addTripod(root);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public void setAim(float yaw, float pitch) {
        base.yRot = yaw * Mth.DEG_TO_RAD;
        base.xRot = pitch * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(
            EntityArcaneBore entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        setAim(netHeadYaw, headPitch);
    }
}
