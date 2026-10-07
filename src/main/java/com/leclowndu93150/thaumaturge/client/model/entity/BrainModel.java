package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.render.blockentity.JarBrainRenderState;
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

public final class BrainModel extends Model<JarBrainRenderState> {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 64;

    private final ModelPart brain;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart stem;
    private final ModelPart tip;

    public BrainModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        brain = root.getChild("root").getChild("brain");
        left = brain.getChild("left");
        right = brain.getChild("right");
        stem = brain.getChild("stem");
        tip = stem.getChild("tip");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition brain = root.addOrReplaceChild("brain", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, 0.0F));
        brain.addOrReplaceChild("left",
                CubeListBuilder.create().texOffs(1, 1).addBox(-4.5F, -4.5F, -7.0F, 4.5F, 7.0F, 14.0F).texOffs(39, 1)
                        .addBox(-4.0F, -6.0F, -5.5F, 3.5F, 1.5F, 11.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)).texOffs(69, 1)
                        .addBox(-6.5F, -2.0F, -3.5F, 2.0F, 5.0F, 8.0F, EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(90, 1)
                        .addBox(-4.0F, -3.0F, -8.0F, 3.5F, 4.5F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)),
                PartPose.offset(-0.5F, 0.0F, 0.0F));
        brain.addOrReplaceChild("right",
                CubeListBuilder.create().texOffs(1, 23).addBox(0.0F, -4.5F, -7.0F, 4.5F, 7.0F, 14.0F).texOffs(39, 23)
                        .addBox(0.5F, -6.0F, -5.5F, 3.5F, 1.5F, 11.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN)).texOffs(69, 23)
                        .addBox(4.5F, -2.0F, -3.5F, 2.0F, 5.0F, 8.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.DOWN, Direction.UP)).texOffs(90, 23)
                        .addBox(0.5F, -3.0F, -8.0F, 3.5F, 4.5F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)),
                PartPose.offset(0.5F, 0.0F, 0.0F));
        brain.addOrReplaceChild("hindbrain", CubeListBuilder.create().texOffs(100, 23).addBox(-4.0F, -1.0F, -2.0F, 8.0F, 1.5F, 5.5F).texOffs(1, 45).addBox(-3.0F, 0.5F, -1.5F, 6.0F, 1.0F, 4.5F,
                EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP)), PartPose.offset(0.0F, 4.0F, 3.0F));
        PartDefinition stem = brain.addOrReplaceChild("stem", CubeListBuilder.create().texOffs(23, 45).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, 1.5F, -0.3839724F, 0.0F, 0.0F));
        stem.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(32, 45).addBox(-0.5F, -0.25F, -0.5F, 1.0F, 1.5F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP)),
                PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, -0.2094395F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(JarBrainRenderState state) {
        super.setupAnim(state);
        float phase = state.ageInTicks / 14.0F;
        float pulse = state.xpResponse;
        brain.y -= pulse * 0.35F;
        brain.xRot += pulse * 5.0F * Mth.DEG_TO_RAD;
        left.x -= Math.abs(pulse) * 0.12F;
        right.x += Math.abs(pulse) * 0.12F;
        left.y -= pulse * 0.2F;
        right.y -= pulse * 0.15F;
        stem.xRot += (pulse * 8.0F - Mth.sin(phase + 0.45F) * 4.0F) * Mth.DEG_TO_RAD;
        stem.zRot -= Mth.sin(phase) * 2.0F * Mth.DEG_TO_RAD;
        tip.xRot += (pulse * 6.0F - Mth.sin(phase + 0.8F) * 5.0F) * Mth.DEG_TO_RAD;
    }
}
