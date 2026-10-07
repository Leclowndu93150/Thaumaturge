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

public final class ResearchTableModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    private static final Set<Direction> OPEN_TOP = EnumSet.complementOf(EnumSet.of(Direction.DOWN));

    private static final Set<Direction> RESTING = EnumSet.complementOf(EnumSet.of(Direction.UP));

    private static final Set<Direction> RAIL_SIDES =
            EnumSet.complementOf(EnumSet.of(Direction.DOWN, Direction.WEST, Direction.EAST));

    private static final Set<Direction> STRETCHER_SIDES =
            EnumSet.complementOf(EnumSet.of(Direction.NORTH, Direction.SOUTH));

    private static final Set<Direction> SHELF_SIDES = EnumSet.complementOf(EnumSet.of(Direction.WEST, Direction.EAST));

    public final ModelPart root;
    public final ModelPart table;
    public final ModelPart inkwell;
    public final ModelPart scrollTube;
    public final ModelPart scrollRibbon;

    public final ModelPart shelfScrolls;

    public ResearchTableModel(ModelPart root) {
        this.root = root;
        this.table = root.getChild("table");
        this.inkwell = root.getChild("inkwell");
        this.scrollTube = root.getChild("scroll_tube");
        this.scrollRibbon = root.getChild("scroll_ribbon");
        this.shelfScrolls = root.getChild("shelf_scrolls");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition table = root.addOrReplaceChild("table", CubeListBuilder.create(), PartPose.ZERO);
        table.addOrReplaceChild(
                "top",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 32.0F, 3.0F, 16.0F),
                PartPose.ZERO);
        table.addOrReplaceChild(
                "legs",
                CubeListBuilder.create()
                        .texOffs(96, 0)
                        .addBox(-8.0F, 3.0F, 5.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP)
                        .texOffs(96, 0)
                        .addBox(-8.0F, 3.0F, -8.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP)
                        .texOffs(96, 0)
                        .addBox(21.0F, 3.0F, 5.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP)
                        .texOffs(96, 0)
                        .addBox(21.0F, 3.0F, -8.0F, 3.0F, 13.0F, 3.0F, OPEN_TOP),
                PartPose.ZERO);
        table.addOrReplaceChild(
                "rails",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(-5.0F, 3.0F, 7.0F, 26.0F, 1.0F, 1.0F, RAIL_SIDES)
                        .texOffs(0, 19)
                        .addBox(-5.0F, 3.0F, -8.0F, 26.0F, 1.0F, 1.0F, RAIL_SIDES),
                PartPose.ZERO);
        table.addOrReplaceChild(
                "shelf",
                CubeListBuilder.create()
                        .texOffs(72, 32)
                        .addBox(-8.0F, 12.0F, -5.0F, 3.0F, 1.0F, 10.0F, STRETCHER_SIDES)
                        .texOffs(72, 32)
                        .addBox(21.0F, 12.0F, -5.0F, 3.0F, 1.0F, 10.0F, STRETCHER_SIDES)
                        .texOffs(0, 21)
                        .addBox(-5.0F, 12.0F, -5.0F, 26.0F, 1.0F, 10.0F, SHELF_SIDES)
                        .texOffs(96, 16)
                        .addBox(12.0F, 8.0F, -4.0F, 7.0F, 4.0F, 9.0F, RESTING),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "inkwell",
                CubeListBuilder.create().texOffs(0, 44).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 2.0F, 3.0F),
                PartPose.offset(-6.0F, -2.0F, 3.0F));
        root.addOrReplaceChild(
                "scroll_tube",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-21.0F, -0.5F, -8.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild(
                "scroll_ribbon",
                CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-15.1F, -0.275F, -6.75F, 1.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild(
                "shelf_scrolls",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, 10.0F, 2.0F, 8.0F, 2.0F, 2.0F, RESTING)
                        .texOffs(0, 0)
                        .addBox(-3.0F, 10.0F, -1.0F, 8.0F, 2.0F, 2.0F, RESTING)
                        .texOffs(0, 0)
                        .addBox(-3.0F, 10.0F, -4.0F, 8.0F, 2.0F, 2.0F, RESTING),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
