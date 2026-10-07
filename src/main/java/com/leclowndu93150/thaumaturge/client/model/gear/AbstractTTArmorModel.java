package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Rotations;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public abstract class AbstractTTArmorModel extends HumanoidModel<LivingEntity> {
    private final ModelPart root;

    protected AbstractTTArmorModel(ModelPart root) {
        super(root);
        this.root = root;
    }

    @Override
    public void setupAnim(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity instanceof ArmorStand stand) {
            setRotation(head, stand.getHeadPose());
            setRotation(body, stand.getBodyPose());
            setRotation(leftArm, stand.getLeftArmPose());
            setRotation(rightArm, stand.getRightArmPose());
            setRotation(leftLeg, stand.getLeftLegPose());
            setRotation(rightLeg, stand.getRightLegPose());
        }
    }

    private static void setRotation(ModelPart part, Rotations rotation) {
        part.xRot = (float) Math.toRadians(rotation.getX());
        part.yRot = (float) Math.toRadians(rotation.getY());
        part.zRot = (float) Math.toRadians(rotation.getZ());
    }
}
