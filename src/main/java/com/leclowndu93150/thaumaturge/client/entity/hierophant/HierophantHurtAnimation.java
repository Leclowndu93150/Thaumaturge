package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantHurtAnimation {
    private HierophantHurtAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(0.5F);
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.posVec(-0.0F, -1.0F, 2.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.posVec(-0.0F, 0.5F, 0.5F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "head",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(10.0F, -5.0F, -3.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-4.0F, 2.0F, 1.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.15F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-12.0F, -0.0F, -6.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-12.0F, -0.0F, 6.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(3.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, -1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.45F, -0.0F, 0.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, 0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, -0.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, 0.21F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(1.2F, -0.0F, 1.44F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.45F, -0.0F, -0.54F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, -0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(2.0F, -0.0F, 0.56F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.75F, -0.0F, -0.21F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, -0.3F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(2.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.9F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.64F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-3.0F, -0.0F, -0.24F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(4.0F, -0.0F, -0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-1.5F, -0.0F, 0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.2F, -0.0F, -1.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-1.95F, -0.0F, 0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(4.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-1.74F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-2.16F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.28F, -0.0F, 0.96F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-1.98F, -0.0F, -0.36F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
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
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(6.32F, -0.0F, 1.28F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-2.37F, -0.0F, -0.48F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
