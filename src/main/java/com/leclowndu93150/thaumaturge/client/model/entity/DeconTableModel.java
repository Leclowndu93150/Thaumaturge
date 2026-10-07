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

public final class DeconTableModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;
    private static final Set<Direction> OPEN_TOP = EnumSet.complementOf(EnumSet.of(Direction.DOWN));
    private static final Set<Direction> LONG_APRON = EnumSet.complementOf(EnumSet.of(Direction.DOWN, Direction.WEST, Direction.EAST));
    private static final Set<Direction> SHORT_APRON = EnumSet.complementOf(EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH));
    private static final Set<Direction> LONG_STRETCHER = EnumSet.complementOf(EnumSet.of(Direction.WEST, Direction.EAST));
    private static final Set<Direction> SHORT_STRETCHER = EnumSet.complementOf(EnumSet.of(Direction.NORTH, Direction.SOUTH));

    public final ModelPart root;

    public DeconTableModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 3.0F, 16.0F), PartPose.ZERO);
        root.addOrReplaceChild("legs",
                CubeListBuilder.create().texOffs(64, 0).addBox(-8.0F, 3.0F, 5.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP).texOffs(64, 0).addBox(-8.0F, 3.0F, -8.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP).texOffs(64, 0)
                        .addBox(5.0F, 3.0F, 5.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP).texOffs(64, 0).addBox(5.0F, 3.0F, -8.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP),
                PartPose.ZERO);
        root.addOrReplaceChild("apron",
                CubeListBuilder.create().texOffs(76, 0).addBox(-5.0F, 3.0F, 6.0F, 10.0F, 5.0F, 1.0F, LONG_APRON).texOffs(76, 0).addBox(-5.0F, 3.0F, -7.0F, 10.0F, 5.0F, 1.0F, LONG_APRON).texOffs(76, 6)
                        .addBox(6.0F, 3.0F, -5.0F, 1.0F, 5.0F, 10.0F, SHORT_APRON).texOffs(76, 6).addBox(-7.0F, 3.0F, -5.0F, 1.0F, 5.0F, 10.0F, SHORT_APRON),
                PartPose.ZERO);
        root.addOrReplaceChild("stretchers",
                CubeListBuilder.create().texOffs(98, 0).addBox(-5.0F, 14.0F, 6.0F, 10.0F, 1.0F, 1.0F, LONG_STRETCHER).texOffs(98, 0).addBox(-5.0F, 14.0F, -7.0F, 10.0F, 1.0F, 1.0F, LONG_STRETCHER)
                        .texOffs(98, 2).addBox(6.0F, 14.0F, -5.0F, 1.0F, 1.0F, 10.0F, SHORT_STRETCHER).texOffs(98, 2).addBox(-7.0F, 14.0F, -5.0F, 1.0F, 1.0F, 10.0F, SHORT_STRETCHER),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
