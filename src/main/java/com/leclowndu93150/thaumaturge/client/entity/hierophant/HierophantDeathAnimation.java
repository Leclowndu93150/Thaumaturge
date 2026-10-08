package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantDeathAnimation {
    private HierophantDeathAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(3.6F);
        builder.addAnimation(
                "root",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-0.0F, 7.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, -12.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-0.0F, -24.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "root",
                new AnimationChannel(
                        AnimationChannel.Targets.SCALE,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.scaleVec(0.7F, 0.85F, 0.7F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.scaleVec(0.03F, 0.03F, 0.03F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-8.0F, 5.0F, -4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(9.0F, -8.0F, 6.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-16.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-30.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-45.0F, -0.0F, 0.0F),
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
                                0.2F,
                                KeyframeAnimations.degreeVec(18.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-8.0F, -8.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(12.0F, 8.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(25.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(32.0F, -0.0F, 0.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(-0.0F, 3.0F, 6.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.posVec(-0.0F, 8.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.posVec(-0.0F, 14.0F, 16.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-0.0F, 22.0F, 20.0F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, -20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 90.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 2.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, -4.0F, 4.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-0.0F, 12.0F, 11.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, 26.0F, 19.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-0.0F, 42.0F, 29.04F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, 18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(11.25F, -0.0F, -45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(25.0F, -0.0F, -100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(40.0F, -0.0F, -160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(-1.4142F, 1.4142F, 1.76F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(2.8284F, -2.8284F, 3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-8.4853F, 8.4853F, 8.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-18.3848F, 18.3848F, 12.88F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-29.6985F, 29.6985F, 18.96F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, -18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-11.25F, -0.0F, 45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-40.0F, -0.0F, 160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(-2.0F, 0.0F, 2.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(4.0F, 0.0F, 4.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-12.0F, 0.0F, 11.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-26.0F, 0.0F, 19.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-42.0F, 0.0F, 29.04F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, 18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(11.25F, -0.0F, -45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(25.0F, -0.0F, -100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(40.0F, -0.0F, -160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(-1.4142F, -1.4142F, 1.76F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(2.8284F, 2.8284F, 3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-8.4853F, -8.4853F, 8.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-18.3848F, -18.3848F, 12.88F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-29.6985F, -29.6985F, 18.96F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, -18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-11.25F, -0.0F, 45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-40.0F, -0.0F, 160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(-0.0F, -2.0F, 2.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, 4.0F, 4.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-0.0F, -12.0F, 11.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, -26.0F, 19.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-0.0F, -42.0F, 29.04F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, 18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(11.25F, -0.0F, -45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(25.0F, -0.0F, -100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(40.0F, -0.0F, -160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(1.4142F, -1.4142F, 1.76F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-2.8284F, 2.8284F, 3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(8.4853F, -8.4853F, 8.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(18.3848F, -18.3848F, 12.88F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(29.6985F, -29.6985F, 18.96F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, -18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-11.25F, -0.0F, 45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-40.0F, -0.0F, 160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(2.0F, 0.0F, 2.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-4.0F, 0.0F, 4.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(12.0F, 0.0F, 11.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(26.0F, 0.0F, 19.12F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(42.0F, 0.0F, 29.04F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, 18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(11.25F, -0.0F, -45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(25.0F, -0.0F, -100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(40.0F, -0.0F, -160.0F),
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
                                0.7F,
                                KeyframeAnimations.posVec(1.4142F, 1.4142F, 1.76F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-2.8284F, -2.8284F, 3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(8.4853F, 8.4853F, 8.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(18.3848F, 18.3848F, 12.88F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(29.6985F, 29.6985F, 18.96F),
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
                                0.7F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(4.5F, -0.0F, -18.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-11.25F, -0.0F, 45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 100.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-40.0F, -0.0F, 160.0F),
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
                                0.3F,
                                KeyframeAnimations.degreeVec(-30.0F, -0.0F, -20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, -40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-80.0F, -0.0F, -45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-20.0F, -0.0F, -15.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -3.0F),
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
                                0.5F,
                                KeyframeAnimations.degreeVec(-20.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-50.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 0.0F),
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
                                KeyframeAnimations.degreeVec(-20.0F, -0.0F, -10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-35.0F, -0.0F, -15.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-30.1F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-55.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-38.7F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-12.9F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-39.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-27.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-33.25F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-61.75F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-42.75F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-14.25F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-39.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-27.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-36.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-67.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-46.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-15.6F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-39.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-27.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-10.5F, -0.0F, -7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-19.5F, -0.0F, -13.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-13.5F, -0.0F, -9.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, -3.0F),
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
                                0.3F,
                                KeyframeAnimations.degreeVec(-30.0F, -0.0F, 20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 40.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-80.0F, -0.0F, 45.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-20.0F, -0.0F, 15.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, 3.0F),
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
                                0.5F,
                                KeyframeAnimations.degreeVec(-20.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-50.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-25.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-5.0F, -0.0F, 0.0F),
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
                                KeyframeAnimations.degreeVec(-20.0F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-35.0F, -0.0F, 15.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-30.1F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-55.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-38.7F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-12.9F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-39.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-27.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-33.25F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-61.75F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-42.75F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-14.25F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-39.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-27.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-36.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-67.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-46.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-15.6F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-21.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-39.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-27.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-9.0F, -0.0F, 0.0F),
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
                                0.6F,
                                KeyframeAnimations.degreeVec(-10.5F, -0.0F, 7.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-19.5F, -0.0F, 13.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-13.5F, -0.0F, 9.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-4.5F, -0.0F, 3.0F),
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
                                0.5F,
                                KeyframeAnimations.degreeVec(1.8F, -0.0F, -2.16F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(3.3F, -0.0F, -3.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, -1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, 1.08F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, -1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, -2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.8F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -0.84F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(5.5F, -0.0F, -1.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -0.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.42F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(6.0F, -0.0F, -1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(11.0F, -0.0F, -2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(1.8F, -0.0F, 2.16F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(3.3F, -0.0F, 3.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, 1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, -1.08F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, 1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, 2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.8F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, 0.84F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(5.5F, -0.0F, 1.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, 0.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, -0.42F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(6.0F, -0.0F, 1.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(11.0F, -0.0F, 2.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(3.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(6.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(22.0F, -0.0F, 1.76F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-6.0F, -0.0F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(6.0F, -0.0F, -1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(11.0F, -0.0F, -2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, 0.72F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(7.8F, -0.0F, -1.92F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(14.3F, -0.0F, -3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(5.2F, -0.0F, -1.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.9F, -0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(6.96F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(12.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(4.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(8.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(15.84F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(5.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-4.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(7.92F, -0.0F, 1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(14.52F, -0.0F, 2.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(5.28F, -0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.96F, -0.0F, -0.72F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.5F,
                                KeyframeAnimations.degreeVec(9.48F, -0.0F, 1.92F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(17.38F, -0.0F, 3.52F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(6.32F, -0.0F, 1.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-4.74F, -0.0F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
