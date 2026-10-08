package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantSwipeRightAnimation {
    private HierophantSwipeRightAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(1.2F);
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-4.0F, -24.0F, 5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -18.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(6.0F, 22.0F, -5.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-2.0F, 7.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "head",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(2.0F, 16.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-4.0F, -12.0F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-0.0F, 7.0F, 5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.0F, -6.0F, -5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-42.0F, 30.0F, -48.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-58.0F, 35.0F, -45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-90.0F, -35.0F, 5.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-55.0F, -20.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-45.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-48.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, -15.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 25.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(15.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-27.52F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-12.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(17.1F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-30.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-14.25F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(18.72F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-33.28F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-15.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(5.4F, -0.0F, 3.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, -6.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.45F, -0.0F, -0.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, -1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(2.25F, -0.0F, -2.7F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.45F, -0.0F, 0.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_tail_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.9F, -0.0F, -0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, -1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, 0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -0.21F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -0.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(3.75F, -0.0F, -1.05F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 0.21F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_tip_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, -0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.45F, -0.0F, 0.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, 1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(2.25F, -0.0F, 2.7F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.45F, -0.0F, -0.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_tail_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.9F, -0.0F, 0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, 1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, -0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, 0.21F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, 0.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(3.75F, -0.0F, 1.05F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, -0.21F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_tip_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, 0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, 1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, -0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "tabard",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(0.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, 0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(15.0F, -0.0F, 1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, -0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -1.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(1.95F, -0.0F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(5.2F, -0.0F, -1.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(9.75F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.95F, -0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(1.74F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(4.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(8.7F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.74F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(2.16F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(5.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(10.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.16F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(1.98F, -0.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(5.28F, -0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(9.9F, -0.0F, 1.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.98F, -0.0F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(2.37F, -0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(6.32F, -0.0F, 1.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(11.85F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.37F, -0.0F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
