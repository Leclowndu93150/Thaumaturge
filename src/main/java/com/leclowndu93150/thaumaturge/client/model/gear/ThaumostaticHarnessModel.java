package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ThaumostaticHarnessModel extends AbstractTCArmorModel {
    private static final int TEX_WIDTH = 64;
    private static final int TEX_HEIGHT = 64;
    private static final CubeDeformation SHELL_INFLATE = new CubeDeformation(0.5F);
    private static final CubeDeformation PISTON_INFLATE = new CubeDeformation(-0.25F);
    private static final CubeDeformation BOTTLE_INFLATE = new CubeDeformation(-0.1F);
    private static final float DIAGONAL = (float) (Math.PI / 4);
    private static final float BACK_TILT = (float) (Math.PI / 12);
    private static final float BOTTLE_TILT = (float) (Math.PI / 24);

    public ThaumostaticHarnessModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        body.addOrReplaceChild("harness_body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, SHELL_INFLATE), PartPose.ZERO);
        PartDefinition front = body.addOrReplaceChild("harness_front",
                CubeListBuilder.create().texOffs(24, 11).addBox(-3.0F, -2.0F, -1.5F, 6, 3, 1).texOffs(32, 1).addBox(-2.5F, 1.0F, -1.5F, 5, 2, 1).texOffs(20, 1).addBox(-2.5F, -4.0F, -1.5F, 5, 2, 1),
                PartPose.offset(0.0F, 5.0F, -2.0F));
        front.addOrReplaceChild("harness_clasp", CubeListBuilder.create().texOffs(25, 7).addBox(-1.5F, -1.5F, -0.5F, 3, 3, 1), PartPose.offsetAndRotation(0.0F, -0.5F, -1.5F, 0.0F, 0.0F, DIAGONAL));
        PartDefinition back = body.addOrReplaceChild("harness_back", CubeListBuilder.create().texOffs(0, 24).addBox(-3.0F, -10.0F, -1.0F, 6, 5, 3), PartPose.offset(0.0F, 12.0F, 3.75F));
        back.addOrReplaceChild("harness_piston", CubeListBuilder.create().texOffs(34, 28).addBox(-1.5F, -1.5F, -1.25F, 3, 3, 3, PISTON_INFLATE).texOffs(24, 23).addBox(-2.0F, -2.0F, 1.0F, 4, 4, 1)
                .texOffs(15, 23).addBox(-1.5F, -1.5F, 1.5F, 3, 3, 1), PartPose.offsetAndRotation(0.0F, -7.5F, 3.0F, 0.0F, 0.0F, DIAGONAL));
        back.addOrReplaceChild("harness_back_bottom", CubeListBuilder.create().texOffs(1, 16).addBox(-2.0F, 0.0F, -3.0F, 4, 5, 3),
                PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, -BACK_TILT, 0.0F, 0.0F));
        back.addOrReplaceChild("harness_back_top", CubeListBuilder.create().texOffs(15, 16).addBox(-2.0F, -4.0F, -3.0F, 4, 4, 3),
                PartPose.offsetAndRotation(0.0F, -10.0F, 2.0F, BACK_TILT, 0.0F, 0.0F));
        PartDefinition lowerBelt = body.addOrReplaceChild("belt_lower", belt(39, 21, 44, 0, 54, 0, 39, 17), PartPose.offset(0.0F, 12.0F, -2.0F));
        lowerBelt.addOrReplaceChild("bottle_left", CubeListBuilder.create().texOffs(24, 28).addBox(0.0F, 0.0F, -1.5F, 2, 2, 3, BOTTLE_INFLATE).texOffs(18, 28).addBox(0.0F, 0.0F, -2.25F, 2, 2, 1)
                .texOffs(18, 31).addBox(0.0F, 0.0F, 1.25F, 2, 2, 1), PartPose.offsetAndRotation(4.75F, -1.0F, 2.0F, 0.0F, 0.0F, BOTTLE_TILT));
        lowerBelt.addOrReplaceChild("bottle_right", CubeListBuilder.create().mirror().texOffs(18, 37).addBox(-1.7F, 0.0F, 1.25F, 2, 2, 1).texOffs(24, 34)
                .addBox(-1.7F, 0.0F, -1.5F, 2, 2, 3, BOTTLE_INFLATE).texOffs(18, 34).addBox(-1.7F, 0.0F, -2.25F, 2, 2, 1), PartPose.offsetAndRotation(-5.05F, -1.0F, 2.0F, 0.0F, 0.0F, -BOTTLE_TILT));
        body.addOrReplaceChild("belt_upper", belt(39, 25, 44, 6, 54, 6, 39, 13), PartPose.offset(0.0F, 5.5F, -2.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    private static CubeListBuilder belt(int frontU, int frontV, int leftU, int leftV, int rightU, int rightV, int backU, int backV) {
        return CubeListBuilder.create().texOffs(frontU, frontV).addBox(-5.0F, -2.0F, -1.0F, 10, 2, 1).texOffs(leftU, leftV).addBox(4.0F, -2.0F, 0.0F, 1, 2, 4).texOffs(rightU, rightV)
                .addBox(-5.0F, -2.0F, 0.0F, 1, 2, 4).texOffs(backU, backV).addBox(-5.0F, -2.0F, 4.0F, 10, 2, 1);
    }
}
