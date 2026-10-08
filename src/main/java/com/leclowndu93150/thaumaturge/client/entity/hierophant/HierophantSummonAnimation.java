package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantSummonAnimation {
    private HierophantSummonAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(3.0F);
        builder.addAnimation(
                "root",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, -48.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.posVec(-0.0F, -28.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.posVec(-0.0F, 5.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(5.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "head",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(24.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(18.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.9F,
                                KeyframeAnimations.degreeVec(-8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 8.0F, 16.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.posVec(-0.0F, 5.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, -35.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_0",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 14.0F, 13.68F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, 10.0F, 8.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-0.0F, 3.0F, 3.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, -1.0F, 0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-2.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_1",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-9.8995F, 9.8995F, 10.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-7.0711F, 7.0711F, 5.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-2.1213F, 2.1213F, 2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(0.7071F, -0.7071F, -0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.5F, -0.0F, 30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_2",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-14.0F, 0.0F, 13.68F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-10.0F, 0.0F, 8.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-3.0F, 0.0F, 3.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(1.0F, 0.0F, 0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-2.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_3",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-9.8995F, -9.8995F, 10.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-7.0711F, -7.0711F, 5.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-2.1213F, -2.1213F, 2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(0.7071F, 0.7071F, -0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_3",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.5F, -0.0F, 30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_4",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, -14.0F, 13.68F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, -10.0F, 8.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-0.0F, -3.0F, 3.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_4",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-2.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_5",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(9.8995F, -9.8995F, 10.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(7.0711F, -7.0711F, 5.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(2.1213F, -2.1213F, 2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.7071F, 0.7071F, -0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_5",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.5F, -0.0F, 30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(14.0F, 0.0F, 13.68F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(10.0F, 0.0F, 8.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(3.0F, 0.0F, 3.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-1.0F, 0.0F, 0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-2.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_7",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(9.8995F, 9.8995F, 10.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(7.0711F, 7.0711F, 5.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(2.1213F, 2.1213F, 2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.7071F, -0.7071F, -0.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_7",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.5F, -0.0F, 30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, -9.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-35.0F, -0.0F, -24.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-70.0F, -0.0F, -32.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, -16.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-45.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(20.0F, 15.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(30.0F, 25.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-20.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-27.52F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-8.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-22.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-30.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-9.5F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-24.96F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-33.28F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-10.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, -4.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, -6.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 9.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-35.0F, -0.0F, 24.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-70.0F, -0.0F, 32.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 16.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-45.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(20.0F, -15.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(30.0F, -25.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-20.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-27.52F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-8.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-22.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-30.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-9.5F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-24.96F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-33.28F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-10.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-14.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-19.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 4.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 6.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(2.25F, -0.0F, -2.7F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(3.3F, -0.0F, -3.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 0.9F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(0.6F, -0.0F, -0.72F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, -1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, -2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(3.75F, -0.0F, -1.05F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(5.5F, -0.0F, -1.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-1.25F, -0.0F, 0.35F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(1.0F, -0.0F, -0.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(11.0F, -0.0F, -2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, 0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(2.25F, -0.0F, 2.7F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(3.3F, -0.0F, 3.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, -0.9F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(0.6F, -0.0F, 0.72F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, 1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, 2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, -0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(3.75F, -0.0F, 1.05F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(5.5F, -0.0F, 1.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-1.25F, -0.0F, -0.35F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(1.0F, -0.0F, 0.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, 1.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(11.0F, -0.0F, 2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, -0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(15.0F, -0.0F, 1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(22.0F, -0.0F, 1.76F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, 0.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(7.5F, -0.0F, -1.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(11.0F, -0.0F, -2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(9.75F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(14.3F, -0.0F, -3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-3.25F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.6F, -0.0F, -0.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(8.7F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(12.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-2.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(10.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(15.84F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.88F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(9.9F, -0.0F, 1.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(14.52F, -0.0F, 2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-3.3F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(2.64F, -0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(11.85F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(17.38F, -0.0F, 3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-3.95F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(3.16F, -0.0F, 0.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
