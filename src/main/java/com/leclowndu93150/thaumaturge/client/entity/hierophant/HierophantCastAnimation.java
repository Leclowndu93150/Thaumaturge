package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantCastAnimation {
    private HierophantCastAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(2.4F);
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.posVec(-0.0F, 2.5F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-0.0F, 4.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-7.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(7.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-2.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, 3.0F, 5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.posVec(-0.0F, 5.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.45F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
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
                                1.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_0",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, 3.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-0.0F, 4.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, 8.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(1.0F, -0.0F, -4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(1.75F, -0.0F, -7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_1",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-2.1213F, 2.1213F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-2.8284F, 2.8284F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-5.6569F, 5.6569F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-1.4142F, 1.4142F, -0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-1.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-1.75F, -0.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_2",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-3.0F, 0.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-4.0F, 0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-8.0F, 0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-2.0F, 0.0F, 0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(1.0F, -0.0F, -4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(1.75F, -0.0F, -7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_3",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-2.1213F, -2.1213F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-2.8284F, -2.8284F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-5.6569F, -5.6569F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-1.4142F, -1.4142F, -0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_3",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-1.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-1.75F, -0.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_4",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, -3.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-0.0F, -4.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, -8.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.0F, -2.0F, 0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_4",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(1.0F, -0.0F, -4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(1.75F, -0.0F, -7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_5",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(2.1213F, -2.1213F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(2.8284F, -2.8284F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(5.6569F, -5.6569F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(1.4142F, -1.4142F, -0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_5",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-1.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-1.75F, -0.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(3.0F, 0.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(4.0F, 0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(8.0F, 0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(2.0F, 0.0F, 0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(1.0F, -0.0F, -4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(1.75F, -0.0F, -7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-2.5F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_7",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(2.1213F, 2.1213F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(2.8284F, 2.8284F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(5.6569F, 5.6569F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(1.4142F, 1.4142F, -0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_7",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-1.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-1.75F, -0.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-48.0F, -12.0F, -20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-58.0F, -16.0F, -12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-82.0F, -4.0F, -5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-55.0F, -0.0F, -12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-40.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-50.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(30.0F, 18.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(35.0F, 20.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(55.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(20.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-30.1F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-39.56F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(13.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-27.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-33.25F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-43.7F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(15.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-27.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-36.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-47.84F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(16.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-27.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-10.5F, -0.0F, -7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-13.8F, -0.0F, -9.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(4.8F, -0.0F, 3.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-48.0F, 12.0F, 20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-58.0F, 16.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-82.0F, 4.0F, 5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-55.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-40.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-50.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(30.0F, -18.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(35.0F, -20.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(55.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(20.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-30.1F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-39.56F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(13.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-27.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-33.25F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-43.7F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(15.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-27.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-36.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-47.84F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(16.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-27.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-10.5F, -0.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-13.8F, -0.0F, 9.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(4.8F, -0.0F, -3.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, -0.9F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, -1.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, -4.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(0.9F, -0.0F, -1.08F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, -0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -1.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.8F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(1.25F, -0.0F, -0.35F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -0.7F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(6.0F, -0.0F, -1.68F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, -0.42F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(5.0F, -0.0F, -1.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(0.75F, -0.0F, 0.9F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, 1.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, 4.32F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(0.9F, -0.0F, 1.08F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, 0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, 1.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.8F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(1.25F, -0.0F, 0.35F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, 0.7F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(6.0F, -0.0F, 1.68F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, 0.42F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, 0.5F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(5.0F, -0.0F, 1.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(1.5F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(5.0F, -0.0F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(24.0F, -0.0F, 1.92F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(6.0F, -0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(2.5F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(5.0F, -0.0F, -1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, -2.88F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -0.72F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(3.25F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(6.5F, -0.0F, -1.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(15.6F, -0.0F, -3.84F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.9F, -0.0F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(2.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(5.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(13.92F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(17.28F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(4.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(3.3F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, 1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(15.84F, -0.0F, 2.88F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(3.96F, -0.0F, 0.72F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
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
                                KeyframeAnimations.degreeVec(3.95F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(7.9F, -0.0F, 1.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(18.96F, -0.0F, 3.84F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(4.74F, -0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
