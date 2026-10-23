package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

final class ArmorCloth {
    static final float FLIPPED = -1.0F;
    private static final float WALK_FREQUENCY = 0.6662F;
    private static final float FLUTTER_FREQUENCY = 0.16F;
    private static final float FLUTTER_IDLE = 0.012F;
    private static final float FLUTTER_WALK = 0.028F;
    private static final float FRONT_STRIDE = 0.3F;
    private static final float FRONT_SPEED = 0.08F;
    private static final float SEATED_LIFT = 0.3F;
    private static final float SEATED_WIDTH = 0.4F;
    private static final float HEM_STRIDE = 0.12F;
    private static final float BACK_LEG_PUSH = 1.25F;
    private static final float BACK_STRIDE = 0.1F;
    private static final float BACK_CROUCH = 0.5F;
    private static final float BACK_HEM_FLUTTER = 1.5F;
    private static final float TRAILING_LIFT = 0.65F;

    private ArmorCloth() {}

    static Motion sample(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks) {
        float speed = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float stride = Math.abs(Mth.cos(limbSwing * WALK_FREQUENCY)) * speed;
        float crouch = entity.isCrouching() ? 1.0F : 0.0F;
        float flutter = Mth.sin(ageInTicks * FLUTTER_FREQUENCY) * (FLUTTER_IDLE + speed * FLUTTER_WALK);
        float trailing = entity.isFallFlying() || entity.isVisuallySwimming() ? TRAILING_LIFT : 0.0F;
        return new Motion(speed, stride, crouch, flutter, trailing, entity.isPassenger());
    }

    static float forwardSwing(ModelPart leg, ModelPart body) {
        return Math.min(0.0F, leg.xRot - body.xRot);
    }

    static float backwardSwing(ModelPart leg, ModelPart body) {
        return Math.max(0.0F, leg.xRot - body.xRot);
    }

    static void swayFront(
            ModelPart cloth, ModelPart hem, ModelPart body, float legSwing, float flutter, Motion motion) {
        float seatedLift = motion.seated() ? SEATED_LIFT : 0.0F;
        cloth.xRot += legSwing
                - motion.stride() * FRONT_STRIDE
                - Math.abs(body.yRot)
                - motion.speed() * FRONT_SPEED
                - motion.crouch()
                - seatedLift;
        cloth.y -= motion.crouch();
        cloth.xScale = motion.seated() ? SEATED_WIDTH : 1.0F;
        hem.xRot += -motion.stride() * HEM_STRIDE - flutter + motion.crouch();
    }

    static void swayBack(ModelPart cloth, ModelPart hem, float legSwing, float facing, Motion motion) {
        cloth.xRot += facing
                * (legSwing * BACK_LEG_PUSH
                        + motion.stride() * BACK_STRIDE
                        + motion.flutter()
                        + motion.crouch() * BACK_CROUCH
                        + motion.trailing());
        hem.xRot += facing * (motion.stride() * HEM_STRIDE + motion.flutter() * BACK_HEM_FLUTTER);
    }

    record Motion(float speed, float stride, float crouch, float flutter, float trailing, boolean seated) {}
}
