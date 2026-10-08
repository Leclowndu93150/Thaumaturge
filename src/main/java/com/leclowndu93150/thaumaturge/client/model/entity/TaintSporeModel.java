package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.taint.entity.AbstractTaintSpore;
import java.util.EnumSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

public final class TaintSporeModel<T extends AbstractTaintSpore> extends AbstractTaintSporeModel<T> {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;

    public TaintSporeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild(
                "root",
                CubeListBuilder.create()
                        .texOffs(1, 1)
                        .addBox(
                                -2.5625F,
                                -2.0F,
                                -2.5625F,
                                5.5F,
                                2.0F,
                                5.5F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition body =
                root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition pod0 = body.addOrReplaceChild(
                "pod0",
                CubeListBuilder.create().texOffs(45, 17).addBox(-3.125F, -1.625F, -3.125F, 6.5F, 2.5F, 6.5F),
                PartPose.offsetAndRotation(-3.0F, -1.0F, -1.0F, 0.0F, 0.0F, 0.1396263F));
        pod0.addOrReplaceChild(
                "pod0_core",
                CubeListBuilder.create()
                        .texOffs(24, 1)
                        .addBox(-3.0F, -4.25F, -2.5F, 6.0F, 8.5F, 5.0F)
                        .texOffs(47, 1)
                        .addBox(-2.25F, -4.0F, -3.25F, 4.5F, 8.0F, 6.5F),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        pod0.addOrReplaceChild(
                "pod0_front",
                CubeListBuilder.create().texOffs(70, 1).addBox(-4.0F, -8.25F, -1.0F, 8.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -0.25F, -4.0F));
        pod0.addOrReplaceChild(
                "pod0_back",
                CubeListBuilder.create().texOffs(89, 1).addBox(-4.0F, -8.25F, 0.0F, 8.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -0.25F, 4.0F));
        pod0.addOrReplaceChild(
                "pod0_left",
                CubeListBuilder.create().texOffs(108, 1).addBox(-1.0F, -8.0F, -3.5F, 1.0F, 6.5F, 7.0F),
                PartPose.offset(-4.0F, -0.25F, 0.0F));
        pod0.addOrReplaceChild(
                "pod0_right",
                CubeListBuilder.create().texOffs(1, 17).addBox(0.0F, -8.0F, -3.5F, 1.0F, 6.5F, 7.0F),
                PartPose.offset(4.0F, -0.25F, 0.0F));
        pod0.addOrReplaceChild(
                "pod0_cap",
                CubeListBuilder.create().texOffs(18, 17).addBox(-3.1875F, -0.875F, -7.1875F, 6.5F, 2.0F, 6.5F),
                PartPose.offset(0.0F, -9.75F, 4.0F));
        PartDefinition pod1 = body.addOrReplaceChild(
                "pod1",
                CubeListBuilder.create().texOffs(61, 32).addBox(-2.125F, -1.625F, -2.125F, 4.5F, 2.5F, 4.5F),
                PartPose.offsetAndRotation(3.25F, -0.75F, -0.625F, 0.0F, 0.0F, -0.2443461F));
        pod1.addOrReplaceChild(
                "pod1_core",
                CubeListBuilder.create()
                        .texOffs(72, 17)
                        .addBox(-2.0F, -2.75F, -1.5F, 4.0F, 5.5F, 3.0F)
                        .texOffs(87, 17)
                        .addBox(-1.25F, -2.5F, -2.25F, 2.5F, 5.0F, 4.5F),
                PartPose.offset(0.0F, -3.5F, 0.0F));
        pod1.addOrReplaceChild(
                "pod1_front",
                CubeListBuilder.create().texOffs(102, 17).addBox(-3.0F, -5.25F, -1.0F, 6.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -0.25F, -3.0F));
        pod1.addOrReplaceChild(
                "pod1_back",
                CubeListBuilder.create().texOffs(1, 32).addBox(-3.0F, -5.25F, 0.0F, 6.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, -0.25F, 3.0F));
        pod1.addOrReplaceChild(
                "pod1_left",
                CubeListBuilder.create().texOffs(16, 32).addBox(-1.0F, -5.0F, -2.5F, 1.0F, 3.5F, 5.0F),
                PartPose.offset(-3.0F, -0.25F, 0.0F));
        pod1.addOrReplaceChild(
                "pod1_right",
                CubeListBuilder.create().texOffs(29, 32).addBox(0.0F, -5.0F, -2.5F, 1.0F, 3.5F, 5.0F),
                PartPose.offset(3.0F, -0.25F, 0.0F));
        pod1.addOrReplaceChild(
                "pod1_cap",
                CubeListBuilder.create().texOffs(42, 32).addBox(-2.1875F, -0.875F, -5.1875F, 4.5F, 2.0F, 4.5F),
                PartPose.offset(0.0F, -6.75F, 3.0F));
        PartDefinition pod2 = body.addOrReplaceChild(
                "pod2",
                CubeListBuilder.create().texOffs(61, 46).addBox(-2.125F, -1.625F, -2.125F, 4.5F, 2.5F, 4.5F),
                PartPose.offsetAndRotation(1.0F, -2.0F, 3.0F, 0.0F, 0.0F, -0.0523599F));
        pod2.addOrReplaceChild(
                "pod2_core",
                CubeListBuilder.create()
                        .texOffs(80, 32)
                        .addBox(-2.0F, -4.25F, -1.5F, 4.0F, 8.5F, 3.0F)
                        .texOffs(95, 32)
                        .addBox(-1.25F, -4.0F, -2.25F, 2.5F, 8.0F, 4.5F),
                PartPose.offset(0.0F, -5.0F, 0.0F));
        pod2.addOrReplaceChild(
                "pod2_front",
                CubeListBuilder.create().texOffs(110, 32).addBox(-3.0F, -8.25F, -1.0F, 6.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -0.25F, -3.0F));
        pod2.addOrReplaceChild(
                "pod2_back",
                CubeListBuilder.create().texOffs(1, 46).addBox(-3.0F, -8.25F, 0.0F, 6.0F, 7.0F, 1.0F),
                PartPose.offset(0.0F, -0.25F, 3.0F));
        pod2.addOrReplaceChild(
                "pod2_left",
                CubeListBuilder.create().texOffs(16, 46).addBox(-1.0F, -8.0F, -2.5F, 1.0F, 6.5F, 5.0F),
                PartPose.offset(-3.0F, -0.25F, 0.0F));
        pod2.addOrReplaceChild(
                "pod2_right",
                CubeListBuilder.create().texOffs(29, 46).addBox(0.0F, -8.0F, -2.5F, 1.0F, 6.5F, 5.0F),
                PartPose.offset(3.0F, -0.25F, 0.0F));
        pod2.addOrReplaceChild(
                "pod2_cap",
                CubeListBuilder.create().texOffs(42, 46).addBox(-2.1875F, -0.875F, -5.1875F, 4.5F, 2.0F, 4.5F),
                PartPose.offset(0.0F, -9.75F, 3.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
