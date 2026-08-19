package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.TaintSeedRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public final class TaintSeedModel extends EntityModel<TaintSeedRenderState> {
    private static final int LENGTH = 8;
    private static final int TEXTURE_SIZE = 64;
    private static final float ANGLE_MOD = 0.0625F * 0.2F;
    private static final float BASE_HEIGHT = 1.6F;
    private static final float[] SEGMENT_BASE_Y = {-8.0F, -8.0F, -8.0F, -8.0F, -8.0F, -4.0F, -4.0F};

    private final ModelPart[] segments;

    public TaintSeedModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.segments = new ModelPart[LENGTH - 1];
        ModelPart parent = root.getChild("base");
        for (int k = 0; k < LENGTH - 1; k++) {
            parent = parent.getChild("seg" + k);
            this.segments[k] = parent;
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parent = mesh.getRoot().addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        for (int k = 0; k < LENGTH - 1; k++) {
            CubeListBuilder cube;
            if (k < LENGTH - 4) {
                cube = CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
            } else {
                cube = CubeListBuilder.create().texOffs(0, k == LENGTH - 4 ? 48 : 56).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F);
            }
            parent = parent.addOrReplaceChild("seg" + k, cube, PartPose.offset(0.0F, SEGMENT_BASE_Y[k], 0.0F));
        }
        return LayerDefinition.create(mesh, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    public void setupAnim(TaintSeedRenderState state) {
        super.setupAnim(state);
        float ht = state.hurt;
        float fi = (0.1F + (ht > 0.0F ? ANGLE_MOD : -ANGLE_MOD)) * 3.0F;
        for (int k = 0; k < LENGTH - 1; k++) {
            ModelPart segment = segments[k];
            segment.xRot = 0.1F / fi * Mth.sin(state.ageInTicks * 0.06F - k / 2.0F) / 5.0F + ht + state.attackAnim;
            segment.zRot = 0.1F / fi * Mth.sin(state.ageInTicks * 0.05F - k / 2.0F) / 5.0F;
            float qq = (float) (Math.PI * (k / 7.0F));
            float base = BASE_HEIGHT - Mth.sin(qq);
            float pulse = Mth.sin(state.ageInTicks / 12.0F - qq) * 0.33F;
            pulse *= pulse;
            float sxz = base + pulse;
            float sy = base * 0.9F;
            segment.xScale = sxz;
            segment.zScale = sxz;
            segment.yScale = sy;
            segment.y = SEGMENT_BASE_Y[k] * sy;
        }
    }
}
