package com.leclowndu93150.thaumaturge.client.golem;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/** Copper golem clips used by the rebrand, adapted to the 1.21.1 animation engine. */
public final class CopperGolemAnimations {
    private CopperGolemAnimations() {}

    public static final AnimationDefinition COPPER_GOLEM_WALK = AnimationDefinition.Builder.withLength(0.8333F)
            .looping()
            .addAnimation(
                    "body",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(10.0F, 15.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.2083F,
                                    KeyframeAnimations.degreeVec(10.0F, -1.87F, -10.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(10.0F, -15.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.625F,
                                    KeyframeAnimations.degreeVec(10.0F, -0.82F, 10.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(10.0F, 15.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "head",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.2083F,
                                    KeyframeAnimations.degreeVec(-10.0F, 1.87F, 10.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.625F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.82F, -10.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "right_arm",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(-80.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "left_arm",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-80.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(70.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(-80.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "right_leg",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-60.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(-60.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "left_leg",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(-60.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(60.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .build();

    public static final AnimationDefinition COPPER_GOLEM_IDLE = AnimationDefinition.Builder.withLength(3.5F)
            .addAnimation(
                    "body",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    0.125F,
                                    KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    0.5F,
                                    KeyframeAnimations.degreeVec(0.0F, -35.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    0.625F,
                                    KeyframeAnimations.degreeVec(0.0F, 35.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    1.2083F,
                                    KeyframeAnimations.degreeVec(0.0F, 35.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    2.7083F,
                                    KeyframeAnimations.degreeVec(0.0F, 35.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    3.0F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    3.5F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR)))
            .addAnimation(
                    "head",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    0.125F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    0.5F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    0.625F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    1.2083F,
                                    KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    1.5F,
                                    KeyframeAnimations.degreeVec(0.0F, 300.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    1.6667F,
                                    KeyframeAnimations.degreeVec(0.0F, 300.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    1.75F,
                                    KeyframeAnimations.degreeVec(-25.0F, 300.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    2.7083F,
                                    KeyframeAnimations.degreeVec(-25.0F, 300.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    3.0F,
                                    KeyframeAnimations.degreeVec(0.0F, 360.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(
                                    3.5F,
                                    KeyframeAnimations.degreeVec(0.0F, 360.0F, 0.0F),
                                    AnimationChannel.Interpolations.LINEAR)))
            .build();

    public static final AnimationDefinition COPPER_GOLEM_WALK_ITEM = AnimationDefinition.Builder.withLength(0.8333F)
            .looping()
            .addAnimation(
                    "body",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(10.0F, 7.5F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.2083F,
                                    KeyframeAnimations.degreeVec(10.0F, -1.87F, -5.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(10.0F, -7.5F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.625F,
                                    KeyframeAnimations.degreeVec(10.0F, -0.82F, 5.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(10.0F, 7.5F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "head",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.2083F,
                                    KeyframeAnimations.degreeVec(-10.0F, 1.87F, 10.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.625F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.82F, -10.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "right_arm",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-59.78638F, -6.49053F, -3.76613F),
                                    AnimationChannel.Interpolations.LINEAR)))
            .addAnimation(
                    "left_arm",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-59.78638F, 6.49053F, 3.76613F),
                                    AnimationChannel.Interpolations.LINEAR)))
            .addAnimation(
                    "left_arm",
                    new AnimationChannel(
                            AnimationChannel.Targets.POSITION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.posVec(-0.21129F, -0.0212F, -0.07004F),
                                    AnimationChannel.Interpolations.LINEAR)))
            .addAnimation(
                    "right_leg",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation(
                    "left_leg",
                    new AnimationChannel(
                            AnimationChannel.Targets.ROTATION,
                            new Keyframe(
                                    0.0F,
                                    KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.4167F,
                                    KeyframeAnimations.degreeVec(-30.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(
                                    0.8333F,
                                    KeyframeAnimations.degreeVec(30.0F, 0.0F, 0.0F),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .build();
}
