package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantGlideAnimation {
    private HierophantGlideAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder =
                AnimationDefinition.Builder.withLength(1.6F).looping();
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.posVec(-0.0F, 2.2679F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.posVec(-0.0F, 2.495F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.posVec(-0.0F, 2.6467F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.posVec(-0.0F, 2.7F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 2.6467F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.posVec(-0.0F, 2.495F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.posVec(-0.0F, 2.2679F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.posVec(-0.0F, 1.7321F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.505F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-0.0F, 1.3533F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.posVec(-0.0F, 1.3F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, 1.3533F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.posVec(-0.0F, 1.505F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0F, 1.7321F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.posVec(-0.0F, 2.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(7.426F, -0.0F, 0.2296F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(6.9393F, -0.0F, 0.4243F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(6.6142F, -0.0F, 0.5543F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(6.5F, -0.0F, 0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(6.6142F, -0.0F, 0.5543F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(6.9393F, -0.0F, 0.4243F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(7.426F, -0.0F, 0.2296F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(8.574F, -0.0F, -0.2296F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(9.0607F, -0.0F, -0.4243F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(9.3858F, -0.0F, -0.5543F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(9.5F, -0.0F, -0.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.3858F, -0.0F, -0.5543F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(9.0607F, -0.0F, -0.4243F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(8.574F, -0.0F, -0.2296F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "head",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.0098F, -0.0F, 0.6732F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-7.181F, -0.0F, 0.7873F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-7.1725F, -0.0F, 0.7816F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-6.9854F, -0.0F, 0.657F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-6.6484F, -0.0F, 0.4322F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-6.2126F, -0.0F, 0.1417F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-5.7444F, -0.0F, -0.1704F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-5.3152F, -0.0F, -0.4565F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-4.9902F, -0.0F, -0.6732F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-4.819F, -0.0F, -0.7873F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-4.8275F, -0.0F, -0.7816F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-5.0146F, -0.0F, -0.657F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-5.3516F, -0.0F, -0.4322F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-5.7874F, -0.0F, -0.1417F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-6.2556F, -0.0F, 0.1704F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-6.6848F, -0.0F, 0.4565F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.0098F, -0.0F, 0.6732F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 2.4304F, 4.2869F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.posVec(-0.0F, 2.5576F, 4.3717F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.posVec(-0.0F, 2.5999F, 4.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.posVec(-0.0F, 2.5509F, 4.3673F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.posVec(-0.0F, 2.418F, 4.2787F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 2.2215F, 4.1477F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.posVec(-0.0F, 1.9912F, 3.9942F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.posVec(-0.0F, 1.7623F, 3.8415F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(-0.0F, 1.5696F, 3.7131F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.posVec(-0.0F, 1.4424F, 3.6283F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.4001F, 3.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-0.0F, 1.4491F, 3.6327F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.posVec(-0.0F, 1.582F, 3.7213F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-0.0F, 1.7785F, 3.8523F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.posVec(-0.0F, 2.0088F, 4.0058F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0F, 2.2377F, 4.1585F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.posVec(-0.0F, 2.4304F, 4.2869F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.4794F, -0.0F, 1.4383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-7.7788F, -0.0F, 2.3363F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-7.9595F, -0.0F, 2.8786F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-7.9942F, -0.0F, 2.9827F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-7.8776F, -0.0F, 2.6327F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-7.6273F, -0.0F, 1.8819F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-7.2815F, -0.0F, 0.8446F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-6.8929F, -0.0F, -0.3213F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-6.5206F, -0.0F, -1.4383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-6.2212F, -0.0F, -2.3363F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-6.0405F, -0.0F, -2.8786F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-6.0058F, -0.0F, -2.9827F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-6.1224F, -0.0F, -2.6327F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-6.3727F, -0.0F, -1.8819F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-6.7185F, -0.0F, -0.8446F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-7.1071F, -0.0F, 0.3213F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.4794F, -0.0F, 1.4383F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(13.4383F, -0.0F, -3.0411F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(12.3213F, -0.0F, -3.7858F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(11.1554F, -0.0F, -4.5631F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(10.1181F, -0.0F, -5.2546F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(9.3673F, -0.0F, -5.7552F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(9.0173F, -0.0F, -5.9885F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(9.1214F, -0.0F, -5.9191F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(9.6637F, -0.0F, -5.5575F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(10.5617F, -0.0F, -4.9589F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(11.6787F, -0.0F, -4.2142F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(12.8446F, -0.0F, -3.4369F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(13.8819F, -0.0F, -2.7454F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(14.6327F, -0.0F, -2.2448F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(14.9827F, -0.0F, -2.0115F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(14.8786F, -0.0F, -2.0809F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(14.3363F, -0.0F, -2.4425F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(13.4383F, -0.0F, -3.0411F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-15.3993F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-16.892F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-18.0967F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-18.8299F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-18.98F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-18.5242F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-17.5319F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-16.1542F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-14.6007F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-13.108F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-11.9033F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-11.1701F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-11.02F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-11.4758F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-12.4681F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-13.8458F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-15.3993F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-9.4383F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-10.3363F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-10.8786F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-10.9827F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-10.6327F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-9.8819F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-8.8446F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-7.6787F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-6.5617F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-5.6637F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-5.1214F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-5.0173F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-5.3673F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-6.1181F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-7.1554F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-8.3213F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-9.4383F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-13.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-6.88F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-15.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-7.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-16.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-8.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, -3.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-2.4F, -0.0F, -1.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, -2.4F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.5617F, -0.0F, 4.9589F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(9.6637F, -0.0F, 5.5575F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(9.1214F, -0.0F, 5.9191F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(9.0173F, -0.0F, 5.9885F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(9.3673F, -0.0F, 5.7552F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.1181F, -0.0F, 5.2546F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(11.1554F, -0.0F, 4.5631F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(12.3213F, -0.0F, 3.7858F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(13.4383F, -0.0F, 3.0411F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(14.3363F, -0.0F, 2.4425F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(14.8786F, -0.0F, 2.0809F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(14.9827F, -0.0F, 2.0115F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(14.6327F, -0.0F, 2.2448F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(13.8819F, -0.0F, 2.7454F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(12.8446F, -0.0F, 3.4369F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(11.6787F, -0.0F, 4.2142F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(10.5617F, -0.0F, 4.9589F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-18.5648F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-18.9878F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-18.8037F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-18.0405F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-16.8144F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-15.3121F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-13.7622F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-12.4009F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-11.4352F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-11.0122F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-11.1963F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-11.9595F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-13.1856F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-14.6879F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-16.2378F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-17.5991F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-18.5648F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.9925F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-10.8459F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-10.2661F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-9.3412F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-8.2122F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-7.0509F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-6.034F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-5.3165F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-5.0075F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-5.1541F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-5.7339F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-6.6588F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-7.7878F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-8.9491F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-9.966F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-10.6835F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-10.9925F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-13.76F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-6.88F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-10.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-15.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-7.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-11.4F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-16.64F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-8.32F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-12.48F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-9.6F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-7.2F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-4.8F, -0.0F, 3.2F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-2.4F, -0.0F, 1.6F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-3.6F, -0.0F, 2.4F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(9.6939F, -0.0F, -1.4354F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(8.6175F, -0.0F, -1.7942F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(7.447F, -0.0F, -2.1843F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(6.3607F, -0.0F, -2.5464F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(5.524F, -0.0F, -2.8253F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(5.0642F, -0.0F, -2.9786F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(5.0514F, -0.0F, -2.9829F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(5.4875F, -0.0F, -2.8375F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(6.3061F, -0.0F, -2.5646F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(7.3825F, -0.0F, -2.2058F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(8.553F, -0.0F, -1.8157F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(9.6393F, -0.0F, -1.4536F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(10.476F, -0.0F, -1.1747F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(10.9358F, -0.0F, -1.0214F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(10.9486F, -0.0F, -1.0171F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(10.5125F, -0.0F, -1.1625F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(9.6939F, -0.0F, -1.4354F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_tail_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(8.0866F, -0.0F, -2.3827F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(6.4645F, -0.0F, -2.7071F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(5.3806F, -0.0F, -2.9239F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(5.0F, -0.0F, -3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(5.3806F, -0.0F, -2.9239F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(6.4645F, -0.0F, -2.7071F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(8.0866F, -0.0F, -2.3827F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(11.9134F, -0.0F, -1.6173F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(13.5355F, -0.0F, -1.2929F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(14.6194F, -0.0F, -1.0761F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(15.0F, -0.0F, -1.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(14.6194F, -0.0F, -1.0761F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(13.5355F, -0.0F, -1.2929F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(11.9134F, -0.0F, -1.6173F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(8.6007F, -0.0F, -0.0998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(7.108F, -0.0F, -0.473F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.9033F, -0.0F, -0.7742F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(5.1701F, -0.0F, -0.9575F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(5.02F, -0.0F, -0.995F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(5.4758F, -0.0F, -0.8811F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(6.4681F, -0.0F, -0.633F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(7.8458F, -0.0F, -0.2885F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(9.3993F, -0.0F, 0.0998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(10.892F, -0.0F, 0.473F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(12.0967F, -0.0F, 0.7742F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(12.8299F, -0.0F, 0.9575F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(12.98F, -0.0F, 0.995F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(12.5242F, -0.0F, 0.8811F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(11.5319F, -0.0F, 0.633F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(10.1542F, -0.0F, 0.2885F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(8.6007F, -0.0F, -0.0998F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_tip_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(8.1347F, -0.0F, -0.9663F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(6.6728F, -0.0F, -1.3318F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(6.0219F, -0.0F, -1.4945F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(6.2811F, -0.0F, -1.4297F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(7.4109F, -0.0F, -1.1473F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(9.2395F, -0.0F, -0.6901F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(11.4882F, -0.0F, -0.1279F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(13.8149F, -0.0F, 0.4537F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(15.8653F, -0.0F, 0.9663F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(17.3272F, -0.0F, 1.3318F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(17.9781F, -0.0F, 1.4945F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(17.7189F, -0.0F, 1.4297F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(16.5891F, -0.0F, 1.1473F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(14.7605F, -0.0F, 0.6901F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(12.5118F, -0.0F, 0.1279F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(10.1851F, -0.0F, -0.4537F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(8.1347F, -0.0F, -0.9663F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(6.3061F, -0.0F, 2.5646F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(5.4875F, -0.0F, 2.8375F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.0514F, -0.0F, 2.9829F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(5.0642F, -0.0F, 2.9786F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(5.524F, -0.0F, 2.8253F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(6.3607F, -0.0F, 2.5464F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(7.447F, -0.0F, 2.1843F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(8.6175F, -0.0F, 1.7942F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(9.6939F, -0.0F, 1.4354F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(10.5125F, -0.0F, 1.1625F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(10.9486F, -0.0F, 1.0171F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(10.9358F, -0.0F, 1.0214F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(10.476F, -0.0F, 1.1747F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.6393F, -0.0F, 1.4536F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(8.553F, -0.0F, 1.8157F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(7.3825F, -0.0F, 2.2058F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(6.3061F, -0.0F, 2.5646F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_tail_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(5.3398F, -0.0F, 2.932F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(5.0012F, -0.0F, 2.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.4236F, -0.0F, 2.9153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(6.5427F, -0.0F, 2.6915F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(8.1882F, -0.0F, 2.3624F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.1095F, -0.0F, 1.9781F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(12.0141F, -0.0F, 1.5972F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(13.6121F, -0.0F, 1.2776F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(14.6602F, -0.0F, 1.068F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(14.9988F, -0.0F, 1.0002F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(14.5764F, -0.0F, 1.0847F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(13.4573F, -0.0F, 1.3085F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(11.8118F, -0.0F, 1.6376F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.8905F, -0.0F, 2.0219F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.9859F, -0.0F, 2.4028F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.3879F, -0.0F, 2.7224F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(5.3398F, -0.0F, 2.932F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(5.4352F, -0.0F, 0.8912F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(5.0122F, -0.0F, 0.997F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.1963F, -0.0F, 0.9509F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(5.9595F, -0.0F, 0.7601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(7.1856F, -0.0F, 0.4536F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(8.6879F, -0.0F, 0.078F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(10.2378F, -0.0F, -0.3094F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(11.5991F, -0.0F, -0.6498F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(12.5648F, -0.0F, -0.8912F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(12.9878F, -0.0F, -0.997F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(12.8037F, -0.0F, -0.9509F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(12.0405F, -0.0F, -0.7601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(10.8144F, -0.0F, -0.4536F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.3121F, -0.0F, -0.078F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.7622F, -0.0F, 0.3094F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.4009F, -0.0F, 0.6498F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(5.4352F, -0.0F, 0.8912F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_tip_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(6.05F, -0.0F, 1.4875F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(6.7988F, -0.0F, 1.3003F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(8.3394F, -0.0F, 0.9152F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(10.4373F, -0.0F, 0.3907F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(12.7731F, -0.0F, -0.1933F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(14.9912F, -0.0F, -0.7478F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(16.7539F, -0.0F, -1.1885F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(17.7929F, -0.0F, -1.4482F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(17.95F, -0.0F, -1.4875F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(17.2012F, -0.0F, -1.3003F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(15.6606F, -0.0F, -0.9152F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(13.5627F, -0.0F, -0.3907F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(11.2269F, -0.0F, 0.1933F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.0088F, -0.0F, 0.7478F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.2461F, -0.0F, 1.1885F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.2071F, -0.0F, 1.4482F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(6.05F, -0.0F, 1.4875F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "tabard",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(5.2718F, -0.0F, 0.932F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(5.001F, -0.0F, 0.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(5.3389F, -0.0F, 0.9153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(6.2342F, -0.0F, 0.6915F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(7.5506F, -0.0F, 0.3624F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(9.0876F, -0.0F, -0.0219F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(10.6113F, -0.0F, -0.4028F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(11.8897F, -0.0F, -0.7224F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(12.7282F, -0.0F, -0.932F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(12.999F, -0.0F, -0.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(12.6611F, -0.0F, -0.9153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(11.7658F, -0.0F, -0.6915F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(10.4494F, -0.0F, -0.3624F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(8.9124F, -0.0F, 0.0219F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.3887F, -0.0F, 0.4028F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.1103F, -0.0F, 0.7224F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(5.2718F, -0.0F, 0.932F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(10.5617F, -0.0F, 0.4794F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(9.6637F, -0.0F, 0.7788F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(9.1214F, -0.0F, 0.9595F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(9.0173F, -0.0F, 0.9942F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(9.3673F, -0.0F, 0.8776F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.1181F, -0.0F, 0.6273F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(11.1554F, -0.0F, 0.2815F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(12.3213F, -0.0F, -0.1071F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(13.4383F, -0.0F, -0.4794F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(14.3363F, -0.0F, -0.7788F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(14.8786F, -0.0F, -0.9595F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(14.9827F, -0.0F, -0.9942F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(14.6327F, -0.0F, -0.8776F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(13.8819F, -0.0F, -0.6273F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(12.8446F, -0.0F, -0.2815F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(11.6787F, -0.0F, 0.1071F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(10.5617F, -0.0F, 0.4794F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(8.0834F, -0.0F, 0.7833F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(7.1921F, -0.0F, 0.9616F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(7.0328F, -0.0F, 0.9934F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(7.6297F, -0.0F, 0.8741F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(8.892F, -0.0F, 0.6216F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(10.6274F, -0.0F, 0.2745F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(12.5718F, -0.0F, -0.1144F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(14.4291F, -0.0F, -0.4858F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(15.9166F, -0.0F, -0.7833F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(16.8079F, -0.0F, -0.9616F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(16.9672F, -0.0F, -0.9934F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(16.3703F, -0.0F, -0.8741F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(15.108F, -0.0F, -0.6216F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(13.3726F, -0.0F, -0.2745F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(11.4282F, -0.0F, 0.1144F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(9.5709F, -0.0F, 0.4858F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(8.0834F, -0.0F, 0.7833F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(7.0175F, -0.0F, 1.4962F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(7.3596F, -0.0F, 1.423F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(8.7125F, -0.0F, 1.133F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(10.8705F, -0.0F, 0.6706F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(13.5048F, -0.0F, 0.1061F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(16.2146F, -0.0F, -0.4746F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(18.5872F, -0.0F, -0.983F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(20.2615F, -0.0F, -1.3417F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(20.9825F, -0.0F, -1.4962F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(20.6404F, -0.0F, -1.423F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(19.2875F, -0.0F, -1.133F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(17.1295F, -0.0F, -0.6706F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(14.4952F, -0.0F, -0.1061F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(11.7854F, -0.0F, 0.4746F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(9.4128F, -0.0F, 0.983F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(7.7385F, -0.0F, 1.3417F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(7.0175F, -0.0F, 1.4962F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(7.1822F, -0.0F, 0.9636F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(7.0371F, -0.0F, 0.9926F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(7.6476F, -0.0F, 0.8705F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(8.9206F, -0.0F, 0.6159F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(10.6625F, -0.0F, 0.2675F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(12.608F, -0.0F, -0.1216F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(14.4609F, -0.0F, -0.4922F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(15.9392F, -0.0F, -0.7878F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(16.8178F, -0.0F, -0.9636F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(16.9629F, -0.0F, -0.9926F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(16.3524F, -0.0F, -0.8705F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(15.0794F, -0.0F, -0.6159F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(13.3375F, -0.0F, -0.2675F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(11.392F, -0.0F, 0.1216F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(9.5391F, -0.0F, 0.4922F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(8.0608F, -0.0F, 0.7878F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(7.1822F, -0.0F, 0.9636F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(7.3759F, -0.0F, 1.4195F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(8.7462F, -0.0F, 1.1258F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(10.9163F, -0.0F, 0.6608F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(13.5558F, -0.0F, 0.0952F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(16.263F, -0.0F, -0.4849F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(18.6257F, -0.0F, -0.9912F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(20.2841F, -0.0F, -1.3466F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(20.9859F, -0.0F, -1.497F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(20.6241F, -0.0F, -1.4195F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(19.2538F, -0.0F, -1.1258F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(17.0837F, -0.0F, -0.6608F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(14.4442F, -0.0F, -0.0952F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(11.737F, -0.0F, 0.4849F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.3743F, -0.0F, 0.9912F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.7159F, -0.0F, 1.3466F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(7.0141F, -0.0F, 1.497F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(7.3759F, -0.0F, 1.4195F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(7.0417F, -0.0F, 0.9917F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(7.6656F, -0.0F, 0.8669F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(8.9495F, -0.0F, 0.6101F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(10.6977F, -0.0F, 0.2605F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(12.6442F, -0.0F, -0.1288F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(14.4927F, -0.0F, -0.4985F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(15.9616F, -0.0F, -0.7923F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(16.8274F, -0.0F, -0.9655F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(16.9583F, -0.0F, -0.9917F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(16.3344F, -0.0F, -0.8669F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(15.0505F, -0.0F, -0.6101F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(13.3023F, -0.0F, -0.2605F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(11.3558F, -0.0F, 0.1288F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(9.5073F, -0.0F, 0.4985F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(8.0384F, -0.0F, 0.7923F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(7.1726F, -0.0F, 0.9655F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(7.0417F, -0.0F, 0.9917F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(8.7801F, -0.0F, 1.1186F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(10.9622F, -0.0F, 0.651F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(13.6068F, -0.0F, 0.0842F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(16.3113F, -0.0F, -0.4953F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(18.6639F, -0.0F, -0.9994F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(20.3065F, -0.0F, -1.3514F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(20.989F, -0.0F, -1.4976F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(20.6074F, -0.0F, -1.4159F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(19.2199F, -0.0F, -1.1186F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(17.0378F, -0.0F, -0.651F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(14.3932F, -0.0F, -0.0842F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(11.6887F, -0.0F, 0.4953F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(9.3361F, -0.0F, 0.9994F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(7.6935F, -0.0F, 1.3514F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(7.011F, -0.0F, 1.4976F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(7.3926F, -0.0F, 1.4159F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(8.7801F, -0.0F, 1.1186F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
