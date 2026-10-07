package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.TaintacleRenderState;
import java.util.EnumSet;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class TaintacleModel extends EntityModel<TaintacleRenderState> {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart[] segments = new ModelPart[9];
    private final ModelPart base;
    private final ModelPart tip;
    private final ModelPart core;
    private final ModelPart northLobe;
    private final ModelPart southLobe;
    private final ModelPart westLobe;
    private final ModelPart eastLobe;

    public TaintacleModel(ModelPart root) {
        super(root);
        base = root.getChild("root");
        ModelPart parent = base.getChild("base");
        for (int i = 0; i < segments.length; i++) {
            parent = parent.getChild("stem_" + i);
            segments[i] = parent;
        }
        tip = parent.getChild("tip");
        core = tip.getChild("core");
        northLobe = tip.getChild("lobe_north");
        southLobe = tip.getChild("lobe_south");
        westLobe = tip.getChild("lobe_west");
        eastLobe = tip.getChild("lobe_east");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition base = root.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(1, 1).addBox(-4.75F, -3.5F, -4.75F, 9.5F, 3.5F, 9.5F, EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH))
                        .texOffs(40, 1).addBox(-6.0F, -2.25F, -2.0F, 3.0F, 2.0F, 4.0F, EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)).texOffs(55, 1)
                        .addBox(3.0F, -2.25F, -2.0F, 3.0F, 2.0F, 4.0F, EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)).texOffs(70, 1)
                        .addBox(-2.0F, -2.5F, -6.0F, 4.0F, 2.0F, 3.0F, EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)).texOffs(85, 1).addBox(-2.0F,
                                -2.75F, 3.0F, 4.0F, 2.0F, 3.0F, EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition stem0 = base.addOrReplaceChild("stem_0",
                CubeListBuilder.create().texOffs(1, 15).addBox(-4.0F, -4.0F, -3.25F, 8.0F, 6.0F, 6.5F).texOffs(31, 15).addBox(-3.25F, -3.75F, -4.0F, 6.5F, 5.5F, 8.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        PartDefinition stem1 = stem0.addOrReplaceChild("stem_1",
                CubeListBuilder.create().texOffs(61, 15).addBox(-3.75F, -4.0F, -3.0F, 7.5F, 6.0F, 6.0F).texOffs(89, 15).addBox(-3.0F, -3.75F, -3.75F, 6.0F, 5.5F, 7.5F),
                PartPose.offset(0.0F, -4.5F, 0.0F));
        PartDefinition stem2 = stem1.addOrReplaceChild("stem_2",
                CubeListBuilder.create().texOffs(1, 30).addBox(-3.5F, -4.0F, -2.75F, 7.0F, 6.0F, 5.5F).texOffs(27, 30).addBox(-2.75F, -3.75F, -3.5F, 5.5F, 5.5F, 7.0F),
                PartPose.offset(0.0F, -4.5F, 0.0F));
        PartDefinition stem3 = stem2.addOrReplaceChild("stem_3",
                CubeListBuilder.create().texOffs(53, 30).addBox(-3.0F, -4.0F, -2.25F, 6.0F, 6.0F, 4.5F).texOffs(75, 30).addBox(-2.25F, -3.75F, -3.0F, 4.5F, 5.5F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, -0.0523599F, 0.0F, 0.0F));
        PartDefinition stem4 = stem3.addOrReplaceChild("stem_4",
                CubeListBuilder.create().texOffs(97, 30).addBox(-2.75F, -4.0F, -2.0F, 5.5F, 6.0F, 4.0F).texOffs(1, 44).addBox(-2.0F, -3.75F, -2.75F, 4.0F, 5.5F, 5.5F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, -0.0523599F, 0.0F, 0.0F));
        PartDefinition stem5 = stem4.addOrReplaceChild("stem_5",
                CubeListBuilder.create().texOffs(21, 44).addBox(-2.5F, -4.0F, -1.75F, 5.0F, 6.0F, 3.5F).texOffs(39, 44).addBox(-1.75F, -3.75F, -2.5F, 3.5F, 5.5F, 5.0F),
                PartPose.offset(0.0F, -4.5F, 0.0F));
        PartDefinition stem6 = stem5.addOrReplaceChild("stem_6",
                CubeListBuilder.create().texOffs(57, 44).addBox(-2.25F, -4.0F, -1.5F, 4.5F, 6.0F, 3.0F).texOffs(73, 44).addBox(-1.5F, -3.75F, -2.25F, 3.0F, 5.5F, 4.5F),
                PartPose.offset(0.0F, -4.5F, 0.0F));
        PartDefinition stem7 = stem6.addOrReplaceChild("stem_7",
                CubeListBuilder.create().texOffs(89, 44).addBox(-2.0F, -4.0F, -1.25F, 4.0F, 6.0F, 2.5F).texOffs(103, 44).addBox(-1.25F, -3.75F, -2.0F, 2.5F, 5.5F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        PartDefinition stem8 = stem7.addOrReplaceChild("stem_8",
                CubeListBuilder.create().texOffs(1, 56).addBox(-1.75F, -4.0F, -1.0F, 3.5F, 6.0F, 2.0F).texOffs(13, 56).addBox(-1.0F, -3.75F, -1.75F, 2.0F, 5.5F, 3.5F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0523599F, 0.0F, 0.0F));
        PartDefinition tip = stem8.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(25, 56).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F), PartPose.offset(0.0F, -3.25F, 0.0F));
        tip.addOrReplaceChild("core", CubeListBuilder.create().texOffs(46, 56).addBox(-1.25F, -2.5F, -1.25F, 2.5F, 4.0F, 2.5F), PartPose.offset(0.0F, -2.5F, 0.0F));
        tip.addOrReplaceChild("lobe_north", CubeListBuilder.create().texOffs(57, 56).addBox(-2.0F, -4.75F, -1.25F, 1.0F, 4.5F, 1.5F).texOffs(63, 56).addBox(1.0F, -4.75F, -1.25F, 1.0F, 4.5F, 1.5F),
                PartPose.offset(0.0F, 0.0F, -1.75F));
        tip.addOrReplaceChild("lobe_south", CubeListBuilder.create().texOffs(69, 56).addBox(-2.0F, -4.75F, -0.25F, 4.0F, 4.5F, 1.5F), PartPose.offset(0.0F, 0.0F, 1.75F));
        tip.addOrReplaceChild("lobe_west", CubeListBuilder.create().texOffs(81, 56).addBox(-1.25F, -5.5F, -1.5F, 1.5F, 5.0F, 3.0F), PartPose.offset(-1.75F, 0.0F, 0.0F));
        tip.addOrReplaceChild("lobe_east", CubeListBuilder.create().texOffs(91, 56).addBox(-0.25F, -5.5F, -1.5F, 1.5F, 5.0F, 3.0F), PartPose.offset(1.75F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(TaintacleRenderState state) {
        super.setupAnim(state);
        float death = Mth.clamp(state.deathTime / 20.0F, 0.0F, 1.0F);
        float alive = 1.0F - death;
        float agitation = Math.max(Mth.clamp((state.flail - 1.0F) * 0.5F, 0.0F, 1.0F), state.enrage * 0.8F);
        float phase = state.ageInTicks * Mth.PI / 20.0F;
        float strikeTime = 1.0F - state.strikeTime / 20.0F;
        float strike = state.strikeTime > 0.0F ? Mth.sin(Mth.PI * Mth.clamp((strikeTime - 0.15F) / 0.65F, 0.0F, 1.0F)) * alive : 0.0F;
        float hurt = Mth.sin(Mth.PI * Mth.clamp(state.hurt / 10.0F, 0.0F, 1.0F)) * alive;
        float active = Math.max(strike, hurt);
        float emergence = 1.0F - state.emergence;
        base.y += 49.0F * emergence * emergence * emergence + death * 2.0F;
        for (int k = 0; k < segments.length; k++) {
            float idleX = Mth.sin(phase - k * 0.65F) * 4.0F;
            float flailX = Mth.sin(phase * 2.0F - k * 0.65F) * 10.0F;
            float idleZ = Mth.sin(phase - k * 0.4F) * 2.2F;
            float flailZ = Mth.sin(phase * 2.0F - k * 0.4F) * 5.5F;
            float x = Mth.lerp(agitation, idleX, flailX) * (1.0F - active * 0.85F) * alive - strike * (4.0F + k * 1.3F) + hurt * Mth.sin(k * 0.7F) * 8.0F + death * (4.0F + k * 1.5F);
            float z = Mth.lerp(agitation, idleZ, flailZ) * (1.0F - active * 0.85F) * alive;
            float weight = k == 0 ? 0.2F : 1.0F;
            segments[k].xRot -= x * weight * Mth.DEG_TO_RAD;
            segments[k].zRot -= z * weight * Mth.DEG_TO_RAD;
        }
        tip.xRot -= (-strike * 12.0F + hurt * 8.0F + death * 15.0F) * Mth.DEG_TO_RAD;
        tip.yRot += Mth.sin(phase) * 3.0F * alive * Mth.DEG_TO_RAD;
        core.yRot += Mth.sin(phase) * 7.0F * alive * Mth.DEG_TO_RAD;
        float gape = ((2.0F + Mth.sin(phase) * 2.0F) * alive + strike * 9.0F + death * 7.0F) * Mth.DEG_TO_RAD;
        northLobe.xRot += gape;
        southLobe.xRot -= gape;
        westLobe.zRot -= gape;
        eastLobe.zRot += gape;
    }
}
