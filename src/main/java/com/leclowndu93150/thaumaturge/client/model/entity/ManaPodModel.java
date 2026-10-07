package com.leclowndu93150.thaumaturge.client.model.entity;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

public final class ManaPodModel {
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 32;
    private static final Set<Direction> OPEN_BELOW = EnumSet.complementOf(EnumSet.of(Direction.UP));
    private static final Set<Direction> OPEN_ABOVE = EnumSet.complementOf(EnumSet.of(Direction.DOWN));

    public final ModelPart core;
    public final ModelPart shell;

    public ManaPodModel(ModelPart root) {
        this.core = root.getChild("core");
        this.shell = root.getChild("shell");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 2.5F, -1.5F, 3.0F, 5.0F, 3.0F), PartPose.ZERO);
        root.addOrReplaceChild("shell",
                CubeListBuilder.create().texOffs(20, 11).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F, OPEN_BELOW).texOffs(0, 11).addBox(-2.5F, 1.0F, -2.5F, 5.0F, 1.0F, 5.0F, OPEN_BELOW).texOffs(0, 0)
                        .addBox(-3.5F, 2.0F, -3.5F, 7.0F, 4.0F, 7.0F).texOffs(0, 17).addBox(-2.5F, 6.0F, -2.5F, 5.0F, 2.0F, 5.0F, OPEN_ABOVE).texOffs(20, 15)
                        .addBox(-1.5F, 8.0F, -1.5F, 3.0F, 1.0F, 3.0F, OPEN_ABOVE).texOffs(20, 19).addBox(-0.5F, 9.0F, -0.5F, 1.0F, 1.0F, 1.0F, OPEN_ABOVE),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
