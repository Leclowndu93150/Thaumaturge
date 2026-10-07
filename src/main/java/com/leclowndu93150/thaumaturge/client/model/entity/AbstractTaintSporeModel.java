package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.taint.TaintSporeRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public abstract class AbstractTaintSporeModel extends EntityModel<TaintSporeRenderState> {
    private final ModelPart body;
    private final ModelPart[] pods = new ModelPart[3];
    private final ModelPart[] cores = new ModelPart[3];
    private final ModelPart[] caps = new ModelPart[3];
    private final ModelPart[][] membranes = new ModelPart[3][4];

    protected AbstractTaintSporeModel(ModelPart root) {
        super(root);
        body = root.getChild("root").getChild("body");
        for (int i = 0; i < pods.length; i++) {
            String name = "pod" + i;
            ModelPart pod = body.getChild(name);
            pods[i] = pod;
            cores[i] = pod.getChild(name + "_core");
            caps[i] = pod.getChild(name + "_cap");
            membranes[i][0] = pod.getChild(name + "_front");
            membranes[i][1] = pod.getChild(name + "_back");
            membranes[i][2] = pod.getChild(name + "_left");
            membranes[i][3] = pod.getChild(name + "_right");
        }
    }

    @Override
    public void setupAnim(TaintSporeRenderState state) {
        super.setupAnim(state);
        float phase = state.ageInTicks * Mth.TWO_PI / 40.0F;
        float strongPhase = state.ageInTicks % 160.0F;
        float pulse = strongPhase < 30.0F ? (1.0F - Mth.cos(strongPhase * Mth.TWO_PI / 30.0F)) * 0.185F : 0.0F;
        float alive = 1.0F - Mth.clamp(state.burstProgress, 0.0F, 1.0F);
        float breathing = ((1.0F - Mth.cos(phase)) * 0.115F + pulse) * alive;
        float release = state.releaseTime > 0.0F ? Mth.square(Mth.sin((1.0F - state.releaseTime / 30.0F) * Mth.PI)) * alive : 0.0F;
        float hurtProgress = 1.0F - Mth.clamp(state.hurt / 10.0F, 0.0F, 1.0F);
        float hurt = Mth.sin(hurtProgress * Mth.PI) * Mth.sin(hurtProgress * Mth.TWO_PI) * alive;
        float growing = state.growth * state.growth * state.growth * alive;
        float burst = (float) Math.pow(Mth.clamp((state.burstProgress - 0.15F) / 0.85F, 0.0F, 1.0F), 0.7);
        body.xRot -= hurt * 5.0F * Mth.DEG_TO_RAD;
        body.zRot -= (Mth.sin(phase) * 0.7F * alive - hurt * 3.0F) * Mth.DEG_TO_RAD;
        for (int i = 0; i < pods.length; i++) {
            ModelPart pod = pods[i];
            pod.y += growing * (2.0F + i * 0.3F);
            pod.xRot -= hurt * (4.0F + i) * Mth.DEG_TO_RAD;
            pod.zRot -= Mth.sin(phase - i * 0.45F) * 0.75F * alive * Mth.DEG_TO_RAD;
            cores[i].y -= breathing * 0.35F + release * 0.8F - burst * 2.0F;
            cores[i].xRot -= hurt * 4.0F * Mth.DEG_TO_RAD;
            cores[i].yRot += release * 8.0F * Mth.DEG_TO_RAD;
            float spread = breathing + release * (1.0F + i * 0.12F) * 0.9F + burst * 4.0F;
            float angle = (breathing * 3.0F + release * 16.0F + burst * 42.0F - growing * 6.0F + hurt * 2.0F) * Mth.DEG_TO_RAD;
            for (int side = 0; side < membranes[i].length; side++) {
                ModelPart membrane = membranes[i][side];
                int dx = side == 2 ? -1 : side == 3 ? 1 : 0;
                int dz = side == 0 ? -1 : side == 1 ? 1 : 0;
                membrane.x += dx * spread;
                membrane.y -= burst * 2.0F;
                membrane.z += dz * spread;
                membrane.xRot -= dz * angle;
                membrane.zRot += dx * angle;
            }
            caps[i].y -= breathing * 0.7F + release * 0.25F + burst * 6.0F;
            caps[i].z += burst * 2.0F;
            caps[i].xRot -= (breathing * 4.0F + release * 62.0F + burst * 115.0F) * Mth.DEG_TO_RAD;
            caps[i].zRot -= hurt * 3.0F * Mth.DEG_TO_RAD;
        }
    }
}
