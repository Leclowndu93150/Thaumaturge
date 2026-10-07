package com.leclowndu93150.thaumaturge.client.model.entity;

import java.util.EnumSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

public final class TaintSporeSwarmerModel extends AbstractTaintSporeModel {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;

    public TaintSporeSwarmerModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createShellLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create().texOffs(129, 1).addBox(-4.0625F, -2.0F, -4.0625F, 8.5F, 2.0F, 8.5F,
                EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition pod0 = body.addOrReplaceChild("pod0", CubeListBuilder.create().texOffs(190, 17).addBox(-3.125F, -1.625F, -3.125F, 6.5F, 2.5F, 6.5F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -1.0F, 0.0F, 0.0F, 0.1396263F));
        pod0.addOrReplaceChild("pod0_core", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));
        pod0.addOrReplaceChild("pod0_front", CubeListBuilder.create().texOffs(210, 1).addBox(-4.0F, -8.25F, -1.0F, 8.0F, 7.0F, 1.0F), PartPose.offset(0.0F, -0.25F, -4.0F));
        pod0.addOrReplaceChild("pod0_back", CubeListBuilder.create().texOffs(229, 1).addBox(-4.0F, -8.25F, 0.0F, 8.0F, 7.0F, 1.0F), PartPose.offset(0.0F, -0.25F, 4.0F));
        pod0.addOrReplaceChild("pod0_left", CubeListBuilder.create().texOffs(129, 17).addBox(-1.0F, -8.0F, -3.5F, 1.0F, 6.5F, 7.0F), PartPose.offset(-4.0F, -0.25F, 0.0F));
        pod0.addOrReplaceChild("pod0_right", CubeListBuilder.create().texOffs(146, 17).addBox(0.0F, -8.0F, -3.5F, 1.0F, 6.5F, 7.0F), PartPose.offset(4.0F, -0.25F, 0.0F));
        pod0.addOrReplaceChild("pod0_cap", CubeListBuilder.create().texOffs(163, 17).addBox(-3.1875F, -0.875F, -7.1875F, 6.5F, 2.0F, 6.5F), PartPose.offset(0.0F, -9.75F, 4.0F));
        PartDefinition pod1 = body.addOrReplaceChild("pod1", CubeListBuilder.create().texOffs(204, 32).addBox(-2.125F, -1.625F, -2.125F, 4.5F, 2.5F, 4.5F),
                PartPose.offsetAndRotation(3.25F, -0.75F, -0.625F, 0.0F, 0.0F, -0.2443461F));
        pod1.addOrReplaceChild("pod1_core", CubeListBuilder.create(), PartPose.offset(0.0F, -3.5F, 0.0F));
        pod1.addOrReplaceChild("pod1_front", CubeListBuilder.create().texOffs(129, 32).addBox(-3.0F, -5.25F, -1.0F, 6.0F, 4.0F, 1.0F), PartPose.offset(0.0F, -0.25F, -3.0F));
        pod1.addOrReplaceChild("pod1_back", CubeListBuilder.create().texOffs(144, 32).addBox(-3.0F, -5.25F, 0.0F, 6.0F, 4.0F, 1.0F), PartPose.offset(0.0F, -0.25F, 3.0F));
        pod1.addOrReplaceChild("pod1_left", CubeListBuilder.create().texOffs(159, 32).addBox(-1.0F, -5.0F, -2.5F, 1.0F, 3.5F, 5.0F), PartPose.offset(-3.0F, -0.25F, 0.0F));
        pod1.addOrReplaceChild("pod1_right", CubeListBuilder.create().texOffs(172, 32).addBox(0.0F, -5.0F, -2.5F, 1.0F, 3.5F, 5.0F), PartPose.offset(3.0F, -0.25F, 0.0F));
        pod1.addOrReplaceChild("pod1_cap", CubeListBuilder.create().texOffs(185, 32).addBox(-2.1875F, -0.875F, -5.1875F, 4.5F, 2.0F, 4.5F), PartPose.offset(0.0F, -6.75F, 3.0F));
        PartDefinition pod2 = body.addOrReplaceChild("pod2", CubeListBuilder.create().texOffs(204, 48).addBox(-2.125F, -1.625F, -2.125F, 4.5F, 2.5F, 4.5F),
                PartPose.offsetAndRotation(1.0F, -2.0F, 3.0F, 0.0F, 0.0F, -0.0523599F));
        pod2.addOrReplaceChild("pod2_core", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));
        pod2.addOrReplaceChild("pod2_front", CubeListBuilder.create().texOffs(129, 48).addBox(-3.0F, -10.25F, -1.0F, 6.0F, 9.0F, 1.0F), PartPose.offset(0.0F, -0.25F, -3.0F));
        pod2.addOrReplaceChild("pod2_back", CubeListBuilder.create().texOffs(144, 48).addBox(-3.0F, -10.25F, 0.0F, 6.0F, 9.0F, 1.0F), PartPose.offset(0.0F, -0.25F, 3.0F));
        pod2.addOrReplaceChild("pod2_left", CubeListBuilder.create().texOffs(159, 48).addBox(-1.0F, -10.0F, -2.5F, 1.0F, 8.5F, 5.0F), PartPose.offset(-3.0F, -0.25F, 0.0F));
        pod2.addOrReplaceChild("pod2_right", CubeListBuilder.create().texOffs(172, 48).addBox(0.0F, -10.0F, -2.5F, 1.0F, 8.5F, 5.0F), PartPose.offset(3.0F, -0.25F, 0.0F));
        pod2.addOrReplaceChild("pod2_cap", CubeListBuilder.create().texOffs(185, 48).addBox(-2.1875F, -0.875F, -5.1875F, 4.5F, 2.0F, 4.5F), PartPose.offset(0.0F, -11.75F, 3.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createCoreLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition pod0 = body.addOrReplaceChild("pod0", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, -1.0F, -1.0F, 0.0F, 0.0F, 0.1396263F));
        pod0.addOrReplaceChild("pod0_core", CubeListBuilder.create().texOffs(164, 1).addBox(-3.0F, -4.25F, -2.5F, 6.0F, 8.5F, 5.0F).texOffs(187, 1).addBox(-2.25F, -4.0F, -3.25F, 4.5F, 8.0F, 6.5F),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        pod0.addOrReplaceChild("pod0_front", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, -4.0F));
        pod0.addOrReplaceChild("pod0_back", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, 4.0F));
        pod0.addOrReplaceChild("pod0_left", CubeListBuilder.create(), PartPose.offset(-4.0F, -0.25F, 0.0F));
        pod0.addOrReplaceChild("pod0_right", CubeListBuilder.create(), PartPose.offset(4.0F, -0.25F, 0.0F));
        pod0.addOrReplaceChild("pod0_cap", CubeListBuilder.create(), PartPose.offset(0.0F, -9.75F, 4.0F));
        PartDefinition pod1 = body.addOrReplaceChild("pod1", CubeListBuilder.create(), PartPose.offsetAndRotation(3.25F, -0.75F, -0.625F, 0.0F, 0.0F, -0.2443461F));
        pod1.addOrReplaceChild("pod1_core", CubeListBuilder.create().texOffs(217, 17).addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.5F, 3.0F).texOffs(232, 17).addBox(-1.25F, -2.5F, -2.25F, 2.5F, 5.0F, 4.5F),
                PartPose.offset(0.0F, -3.5F, 0.0F));
        pod1.addOrReplaceChild("pod1_front", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, -3.0F));
        pod1.addOrReplaceChild("pod1_back", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, 3.0F));
        pod1.addOrReplaceChild("pod1_left", CubeListBuilder.create(), PartPose.offset(-3.0F, -0.25F, 0.0F));
        pod1.addOrReplaceChild("pod1_right", CubeListBuilder.create(), PartPose.offset(3.0F, -0.25F, 0.0F));
        pod1.addOrReplaceChild("pod1_cap", CubeListBuilder.create(), PartPose.offset(0.0F, -6.75F, 3.0F));
        PartDefinition pod2 = body.addOrReplaceChild("pod2", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, -2.0F, 3.0F, 0.0F, 0.0F, -0.0523599F));
        pod2.addOrReplaceChild("pod2_core", CubeListBuilder.create().texOffs(223, 32).addBox(-2.0F, -5.25F, -1.5F, 4.0F, 10.5F, 3.0F).texOffs(238, 32).addBox(-1.25F, -5.0F, -2.25F, 2.5F, 10.0F, 4.5F),
                PartPose.offset(0.0F, -6.0F, 0.0F));
        pod2.addOrReplaceChild("pod2_front", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, -3.0F));
        pod2.addOrReplaceChild("pod2_back", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, 3.0F));
        pod2.addOrReplaceChild("pod2_left", CubeListBuilder.create(), PartPose.offset(-3.0F, -0.25F, 0.0F));
        pod2.addOrReplaceChild("pod2_right", CubeListBuilder.create(), PartPose.offset(3.0F, -0.25F, 0.0F));
        pod2.addOrReplaceChild("pod2_cap", CubeListBuilder.create(), PartPose.offset(0.0F, -11.75F, 3.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
