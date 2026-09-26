package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.core.Rotations;

public abstract class AbstractTCArmorModel extends HumanoidModel<HumanoidRenderState> {
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);

    protected AbstractTCArmorModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        if (state instanceof ZombieRenderState zombie) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombie.isAggressive, zombie);
        }
        if (state instanceof ArmorStandRenderState stand) {
            applyStandPose(stand);
        }
    }

    private void applyStandPose(ArmorStandRenderState stand) {
        setRotation(this.head, stand.headPose);
        setRotation(this.body, stand.bodyPose);
        setRotation(this.leftArm, stand.leftArmPose);
        setRotation(this.rightArm, stand.rightArmPose);
        setRotation(this.leftLeg, stand.leftLegPose);
        setRotation(this.rightLeg, stand.rightLegPose);
    }

    private static void setRotation(ModelPart part, Rotations rotations) {
        part.xRot = DEGREES_TO_RADIANS * rotations.x();
        part.yRot = DEGREES_TO_RADIANS * rotations.y();
        part.zRot = DEGREES_TO_RADIANS * rotations.z();
    }
}
