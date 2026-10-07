package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.render.blockentity.BannerRenderState;
import java.util.EnumSet;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class TTBannerModel extends Model<BannerRenderState> {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    private final boolean clothOnly;
    private final ModelPart standingSupport;
    private final ModelPart wallMount;
    private final ModelPart header;
    private final ModelPart loops;
    private final ModelPart[] cloth;

    public TTBannerModel(ModelPart root, boolean clothOnly) {
        super(root, RenderTypes::entityCutout);
        this.clothOnly = clothOnly;
        ModelPart banner = root.getChild("root");
        standingSupport = banner.getChild("standing_support");
        wallMount = banner.getChild("wall_mount");
        header = banner.getChild("header");
        loops = header.getChild("loops");
        ModelPart upper = header.getChild("upper");
        ModelPart middle = upper.getChild("middle");
        ModelPart lower = middle.getChild("lower");
        ModelPart tipUpper = lower.getChild("tip_upper");
        cloth = new ModelPart[]{upper, middle, lower, tipUpper, tipUpper.getChild("tip_lower")};
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("standing_support",
                CubeListBuilder.create().texOffs(1, 1).addBox(-1.0F, -30.0F, 2.0F, 2.0F, 29.0F, 2.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)).texOffs(10, 1)
                        .addBox(-2.5F, -1.0F, 0.5F, 5.0F, 1.0F, 5.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)).texOffs(31, 1)
                        .addBox(-1.5F, -2.0F, 1.5F, 3.0F, 1.0F, 3.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("wall_mount",
                CubeListBuilder.create().texOffs(44, 1).addBox(-2.0F, -30.0F, 3.0F, 4.0F, 3.0F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP))
                        .texOffs(55, 1).addBox(-0.5F, -29.5F, 0.5F, 1.0F, 0.5F, 3.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition header = root.addOrReplaceChild("header",
                CubeListBuilder.create().texOffs(55, 33).addBox(-8.0F, -1.0F, 0.0F, 16.0F, 1.5F, 2.5F).texOffs(93, 33).addBox(-8.5F, -1.5F, -0.5F, 1.0F, 2.5F, 3.5F).texOffs(103, 33)
                        .addBox(7.5F, -1.5F, -0.5F, 1.0F, 2.5F, 3.5F).texOffs(113, 33)
                        .addBox(-1.5F, -2.0F, 1.5F, 3.0F, 1.0F, 3.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)).texOffs(1, 40)
                        .addBox(-1.0F, -3.5F, 2.0F, 2.0F, 1.5F, 2.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)).texOffs(10, 40).addBox(-0.5F, -4.0F,
                                2.5F, 1.0F, 0.5F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)),
                PartPose.offset(0.0F, -29.0F, 0.0F));
        header.addOrReplaceChild("loops",
                CubeListBuilder.create().texOffs(15, 40).addBox(-5.0F, -1.5F, -0.5F, 1.0F, 3.0F, 3.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN))
                        .texOffs(25, 40).addBox(4.0F, -1.5F, -0.5F, 1.0F, 3.0F, 3.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition upper = header.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(64, 1).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 8.0F, 0.5F), PartPose.offset(0.0F, 1.0F, -0.25F));
        PartDefinition middle = upper.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(90, 1).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 8.0F, 0.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition lower = middle.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(1, 33).addBox(-6.0F, 0.0F, 0.0F, 12.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition tipUpper = lower.addOrReplaceChild("tip_upper", CubeListBuilder.create().texOffs(27, 33).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 3.0F, 0.5F), PartPose.offset(0.0F, 4.0F, 0.0F));
        tipUpper.addOrReplaceChild("tip_lower", CubeListBuilder.create().texOffs(45, 33).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(BannerRenderState state) {
        super.setupAnim(state);
        standingSupport.visible = !clothOnly && !state.onWall;
        wallMount.visible = !clothOnly && state.onWall;
        wallMount.z += 0.5F;
        header.skipDraw = clothOnly;
        loops.visible = clothOnly;
        cloth[0].visible = clothOnly;
        for (int i = 0; i < cloth.length; i++) {
            cloth[i].xRot = clothBend(state.phase, i);
        }
    }

    public static float clothBend(float phase, int segment) {
        float amplitude = segment == 0 ? 1.15F : 0.6F;
        return -amplitude * (1.0F - Mth.sin(phase - segment * 0.65F)) * Mth.DEG_TO_RAD;
    }
}
