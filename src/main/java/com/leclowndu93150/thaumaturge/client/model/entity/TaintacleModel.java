package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.TaintacleRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public final class TaintacleModel extends EntityModel<TaintacleRenderState> {
    public static final int TAINTACLE_LENGTH = 10;
    public static final int TAINTACLE_SMALL_LENGTH = 6;

    private static final int TEXTURE_SIZE = 64;
    private static final float SEGMENT_SCALE = 0.88F;
    private static final float SEGMENT_OFFSET = -8.0F * SEGMENT_SCALE;
    private static final float ANGLE_MOD = 0.0625F * 0.2F;

    private final ModelPart[] segments;
    private final int length;

    public TaintacleModel(ModelPart root, int length) {
        super(root, RenderTypes::entityTranslucent);
        this.length = length;
        this.segments = new ModelPart[length - 1];
        ModelPart parent = root.getChild("base");
        for (int k = 0; k < length - 1; k++) {
            parent = parent.getChild("seg" + k);
            this.segments[k] = parent;
        }
    }

    public static LayerDefinition createLayer(int length) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot().addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition parent = base;
        for (int k = 0; k < length - 1; k++) {
            parent = parent.addOrReplaceChild("seg" + k, CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                    PartPose.offset(0.0F, SEGMENT_OFFSET, 0.0F).withScale(SEGMENT_SCALE));
        }
        parent.addOrReplaceChild("orb", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, SEGMENT_OFFSET, 0.0F).withScale(SEGMENT_SCALE));
        parent.addOrReplaceChild("end", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F), PartPose.offset(0.0F, SEGMENT_OFFSET, 0.0F).withScale(SEGMENT_SCALE));
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void setupAnim(TaintacleRenderState state) {
        super.setupAnim(state);
        float flail = state.flail;
        float fs = flail > 1.0F ? 3.0F : 1.0F - ANGLE_MOD;
        float fi = flail + (state.hurt > 0.0F ? ANGLE_MOD : -ANGLE_MOD);
        for (int k = 0; k < length - 1; k++) {
            segments[k].xRot = 0.15F * fi * Mth.sin(state.ageInTicks * 0.1F * fs - k / 2.0F);
            segments[k].zRot = 0.1F / fi * Mth.sin(state.ageInTicks * 0.15F - k / 2.0F);
        }
    }
}
