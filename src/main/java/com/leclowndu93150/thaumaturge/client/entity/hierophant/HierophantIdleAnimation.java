package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantIdleAnimation {
    private HierophantIdleAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder =
                AnimationDefinition.Builder.withLength(4.0F).looping();
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.0F, 1.6888F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 2.2728F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.0F, 2.663F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0F, 2.8F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.0F, 2.663F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0F, 2.2728F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.0F, 1.6888F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.3112F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, -0.2728F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.0F, -0.663F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, -0.8F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.0F, -0.663F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.0F, -0.2728F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.3112F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.5753F, -0.0F, 0.3356F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.9345F, -0.0F, 0.5451F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.1515F, -0.0F, 0.6717F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.1931F, -0.0F, 0.696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.0531F, -0.0F, 0.6143F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-0.7528F, -0.0F, 0.4391F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.3378F, -0.0F, 0.1971F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.1285F, -0.0F, -0.075F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.5753F, -0.0F, -0.3356F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(0.9345F, -0.0F, -0.5451F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(1.1515F, -0.0F, -0.6717F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.1931F, -0.0F, -0.696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.0531F, -0.0F, -0.6143F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.7528F, -0.0F, -0.4391F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(0.3378F, -0.0F, -0.1971F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.1285F, -0.0F, 0.075F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.5753F, -0.0F, 0.3356F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "head",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(0.3223F, -3.7282F, 0.932F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(0.2004F, -3.999F, 0.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(0.3525F, -3.6611F, 0.9153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(0.7554F, -2.7658F, 0.6915F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(1.3478F, -1.4494F, 0.3624F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(2.0394F, 0.0876F, -0.0219F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.7251F, 1.6113F, -0.4028F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(3.3004F, 2.8897F, -0.7224F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.6777F, 3.7282F, -0.932F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(3.7996F, 3.999F, -0.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.6475F, 3.6611F, -0.9153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(3.2446F, 2.7658F, -0.6915F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(2.6522F, 1.4494F, -0.3624F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(1.9606F, -0.0876F, 0.0219F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.2749F, -1.6113F, 0.4028F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.6996F, -2.8897F, 0.7224F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(0.3223F, -3.7282F, 0.932F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "eye",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.4304F, 0.1435F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.5576F, 0.1859F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.5999F, 0.2F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.5509F, 0.1836F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.418F, 0.1393F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.2215F, 0.0738F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(0.0088F, -0.0029F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(0.2377F, -0.0792F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(0.4304F, -0.1435F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(0.5576F, -0.1859F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(0.5999F, -0.2F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(0.5509F, -0.1836F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(0.418F, -0.1393F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(0.2215F, -0.0738F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.0088F, 0.0029F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.2377F, 0.0792F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.4304F, 0.1435F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.798F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.0F, 1.7589F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 1.6043F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.0F, 1.3577F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.0566F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.7469F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.4757F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.2844F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.202F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.2411F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.3957F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.6423F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.9434F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.0F, 1.2531F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.0F, 1.5243F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.0F, 1.7156F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.798F, 2.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.8632F, -1.2948F, 2.5896F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.6043F, -0.9065F, 1.8129F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.2534F, -0.3801F, 0.7602F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(0.1361F, 0.2041F, -0.4082F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.5048F, 0.7573F, -1.5145F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(0.7968F, 1.1951F, -2.3903F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(0.9674F, 1.451F, -2.9021F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.9907F, 1.486F, -2.9721F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.8632F, 1.2948F, -2.5896F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(0.6043F, 0.9065F, -1.8129F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.2534F, 0.3801F, -0.7602F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.1361F, -0.2041F, 0.4082F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.5048F, -0.7573F, 1.5145F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.7968F, -1.1951F, 2.3903F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.9674F, -1.451F, 2.9021F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.9907F, -1.486F, 2.9721F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.8632F, -1.2948F, 2.5896F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_0",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.6F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.9061F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 1.1657F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.0F, 1.3391F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0F, 1.4F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.0F, 1.3391F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0F, 1.1657F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.9061F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.6F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.2939F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.0343F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.0F, -0.1391F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, -0.2F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.0F, -0.1391F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.0343F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.2939F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.6F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_1",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.8243F, 0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.9469F, 0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.9899F, 0.9899F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.9469F, 0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.8243F, 0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.6407F, 0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.4243F, 0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.2078F, 0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-0.0243F, 0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(0.0984F, -0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(0.1414F, -0.1414F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(0.0984F, -0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0243F, 0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.2078F, 0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.4243F, 0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.6407F, 0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.8243F, 0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_2",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-1.4F, 0.0F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-1.3391F, 0.0F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-1.1657F, 0.0F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.9061F, 0.0F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.6F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.2939F, 0.0F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0343F, 0.0F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(0.1391F, 0.0F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(0.2F, 0.0F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(0.1391F, 0.0F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0343F, 0.0F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.2939F, 0.0F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.6F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.9061F, 0.0F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-1.1657F, 0.0F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-1.3391F, 0.0F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-1.4F, 0.0F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_3",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.8243F, -0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.6407F, -0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.4243F, -0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.2078F, -0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0243F, -0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(0.0984F, 0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(0.1414F, 0.1414F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(0.0984F, 0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-0.0243F, -0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(-0.2078F, -0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.4243F, -0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.6407F, -0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.8243F, -0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.9469F, -0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.9899F, -0.9899F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.9469F, -0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.8243F, -0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_3",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_4",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, -0.6F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.0F, -0.2939F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, -0.0343F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.1391F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.2F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.1391F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-0.0F, -0.0343F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-0.0F, -0.2939F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-0.0F, -0.6F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(-0.0F, -0.9061F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, -1.1657F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.0F, -1.3391F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, -1.4F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.0F, -1.3391F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.0F, -1.1657F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.0F, -0.9061F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.0F, -0.6F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_4",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_5",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(0.0243F, -0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.0984F, 0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.1414F, 0.1414F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(-0.0984F, 0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(0.0243F, -0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(0.2078F, -0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(0.4243F, -0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(0.6407F, -0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(0.8243F, -0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(0.9469F, -0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(0.9899F, -0.9899F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(0.9469F, -0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(0.8243F, -0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(0.6407F, -0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(0.4243F, -0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(0.2078F, -0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(0.0243F, -0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_5",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.2F, 0.0F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.1391F, 0.0F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(0.0343F, 0.0F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(0.2939F, 0.0F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(0.6F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(0.9061F, 0.0F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(1.1657F, 0.0F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(1.3391F, 0.0F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(1.4F, 0.0F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(1.3391F, 0.0F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(1.1657F, 0.0F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(0.9061F, 0.0F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(0.6F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(0.2939F, 0.0F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(0.0343F, 0.0F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.1391F, 0.0F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(-0.2F, 0.0F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_7",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(0.0243F, 0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(0.2078F, 0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(0.4243F, 0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(0.6407F, 0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(0.8243F, 0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(0.9469F, 0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(0.9899F, 0.9899F, 0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(0.9469F, 0.9469F, 0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(0.8243F, 0.8243F, 0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(0.6407F, 0.6407F, 0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(0.4243F, 0.4243F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(0.2078F, 0.2078F, -0.1531F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(0.0243F, 0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.0984F, -0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.1414F, -0.1414F, -0.4F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.posVec(-0.0984F, -0.0984F, -0.3696F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.posVec(0.0243F, 0.0243F, -0.2828F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_7",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.2142F, -0.0F, 0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.9589F, -0.0F, 1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.5575F, -0.0F, 1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.9191F, -0.0F, 2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-1.9885F, -0.0F, 2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-1.7552F, -0.0F, 2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-1.2546F, -0.0F, 1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-0.5631F, -0.0F, 0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(0.2142F, -0.0F, -0.2677F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.9589F, -0.0F, -1.1986F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.5575F, -0.0F, -1.9469F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.9191F, -0.0F, -2.3989F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(1.9885F, -0.0F, -2.4856F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.7552F, -0.0F, -2.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(1.2546F, -0.0F, -1.5683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(0.5631F, -0.0F, -0.7038F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-2.5653F, 0.7174F, -0.5653F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-3.2077F, 0.3961F, -1.2077F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-3.9708F, 0.0146F, -1.9708F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-4.7383F, -0.3692F, -2.7383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-5.3934F, -0.6967F, -3.3934F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-5.8364F, -0.9182F, -3.8364F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-5.9998F, -0.9999F, -3.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-5.8587F, -0.9294F, -3.8587F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-5.4347F, -0.7174F, -3.4347F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-4.7923F, -0.3961F, -2.7923F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-4.0292F, -0.0146F, -2.0292F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-3.2617F, 0.3692F, -1.2617F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-2.6066F, 0.6967F, -0.6066F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.1636F, 0.9182F, -0.1636F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.0002F, 0.9999F, -0.0002F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-2.1413F, 0.9294F, -0.1413F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-2.5653F, 0.7174F, -0.5653F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.2578F, -0.0F, 0.2969F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-2.4266F, -0.0F, -0.1707F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-3.5306F, -0.0F, -0.6122F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-4.4014F, -0.0F, -0.9606F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-4.9067F, -0.0F, -1.1627F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-4.9695F, -0.0F, -1.1878F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-4.5802F, -0.0F, -1.0321F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-3.7981F, -0.0F, -0.7192F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-2.7422F, -0.0F, -0.2969F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-1.5734F, -0.0F, 0.1707F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.4694F, -0.0F, 0.6122F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(0.4014F, -0.0F, 0.9606F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.9067F, -0.0F, 1.1627F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.9695F, -0.0F, 1.1878F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(0.5802F, -0.0F, 1.0321F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.2019F, -0.0F, 0.7192F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.2578F, -0.0F, 0.2969F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.596F, -0.0F, -0.3973F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.6758F, -0.0F, -1.1172F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-2.5005F, -0.0F, -1.667F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-2.9445F, -0.0F, -1.963F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.9402F, -0.0F, -1.9601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-2.4883F, -0.0F, -1.6589F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-1.6576F, -0.0F, -1.1051F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.5745F, -0.0F, -0.383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.596F, -0.0F, 0.3973F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(1.6758F, -0.0F, 1.1172F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(2.5005F, -0.0F, 1.667F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.9445F, -0.0F, 1.963F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(2.9402F, -0.0F, 1.9601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(2.4883F, -0.0F, 1.6589F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.6576F, -0.0F, 1.1051F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.5745F, -0.0F, 0.383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.596F, -0.0F, -0.3973F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-2.7926F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-4.1467F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-5.9352F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-7.8858F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-9.7015F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-11.106F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-11.8853F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-11.9209F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-11.2074F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-9.8533F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-8.0648F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-6.1142F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-4.2985F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.894F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.1147F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-2.0791F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-2.7926F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.9093F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-3.3734F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-4.9329F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-6.3504F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-7.4101F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-7.9506F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-7.8897F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-7.2366F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-6.0907F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-4.6266F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.0671F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-1.6496F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.5899F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.0494F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.1103F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.7634F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.9093F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-4.8252F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-6.7137F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-8.6457F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-10.3273F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-11.5022F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-11.9918F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-11.7214F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-10.7322F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-9.1748F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-7.2863F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-5.3543F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-3.6727F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-2.4978F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.0082F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.2786F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-3.2678F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-4.8252F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-4.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-5.5307F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-6.8284F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-7.6955F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-7.6955F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-6.8284F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-5.5307F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-4.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-2.4693F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.1716F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.3045F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.3045F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-1.1716F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-2.4693F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-4.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.4992F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-9.365F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-10.8708F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-11.7873F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-11.975F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-11.4053F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-10.1649F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-8.4427F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-6.5008F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-4.635F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.1292F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-2.2127F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-2.025F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.5947F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-3.8351F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-5.5573F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-7.4992F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_right_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-6.0907F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-7.2366F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-7.8897F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-7.9506F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-7.4101F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-6.3504F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-4.9329F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-3.3734F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-1.9093F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-0.7634F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.1103F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.0494F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.5899F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-1.6496F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-3.0671F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-4.6266F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-6.0907F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.3061F, -0.0F, 1.1293F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.3825F, -0.0F, 0.4116F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-2.553F, -0.0F, -0.3687F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-3.6393F, -0.0F, -1.0929F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-4.476F, -0.0F, -1.6507F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-4.9358F, -0.0F, -1.9572F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-4.9486F, -0.0F, -1.9657F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-4.5125F, -0.0F, -1.675F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-3.6939F, -0.0F, -1.1293F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-2.6175F, -0.0F, -0.4116F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-1.447F, -0.0F, 0.3687F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.3607F, -0.0F, 1.0929F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.476F, -0.0F, 1.6507F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.9358F, -0.0F, 1.9572F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(0.9486F, -0.0F, 1.9657F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.5125F, -0.0F, 1.675F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.3061F, -0.0F, 1.1293F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "arm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-5.4347F, -0.7174F, 3.4347F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-5.8587F, -0.9294F, 3.8587F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-5.9998F, -0.9999F, 3.9998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-5.8364F, -0.9182F, 3.8364F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-5.3934F, -0.6967F, 3.3934F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-4.7383F, -0.3692F, 2.7383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-3.9708F, 0.0146F, 1.9708F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-3.2077F, 0.3961F, 1.2077F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-2.5653F, 0.7174F, 0.5653F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-2.1413F, 0.9294F, 0.1413F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-2.0002F, 0.9999F, 0.0002F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-2.1636F, 0.9182F, 0.1636F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-2.6066F, 0.6967F, 0.6066F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-3.2617F, 0.3692F, 1.2617F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-4.0292F, -0.0146F, 2.0292F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-4.7923F, -0.3961F, 2.7923F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-5.4347F, -0.7174F, 3.4347F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "forearm_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-4.9272F, -0.0F, 1.1709F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-4.9558F, -0.0F, 1.1823F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-4.5344F, -0.0F, 1.0138F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-3.7272F, -0.0F, 0.6909F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.657F, -0.0F, 0.2628F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-1.4868F, -0.0F, -0.2053F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.3948F, -0.0F, -0.6421F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.4529F, -0.0F, -0.9812F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.9272F, -0.0F, -1.1709F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(0.9558F, -0.0F, -1.1823F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.5344F, -0.0F, -1.0138F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.2728F, -0.0F, -0.6909F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-1.343F, -0.0F, -0.2628F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.5132F, -0.0F, 0.2053F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-3.6052F, -0.0F, 0.6421F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-4.4529F, -0.0F, 0.9812F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-4.9272F, -0.0F, 1.1709F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "hand_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-2.9215F, -0.0F, 1.9477F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-2.4383F, -0.0F, 1.6255F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.5839F, -0.0F, 1.0559F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.4883F, -0.0F, 0.3255F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.6816F, -0.0F, -0.4544F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.7477F, -0.0F, -1.1652F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.5478F, -0.0F, -1.6985F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(2.96F, -0.0F, -1.9733F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(2.9215F, -0.0F, -1.9477F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.4383F, -0.0F, -1.6255F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(1.5839F, -0.0F, -1.0559F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(0.4883F, -0.0F, -0.3255F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.6816F, -0.0F, 0.4544F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-1.7477F, -0.0F, 1.1652F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.5478F, -0.0F, 1.6985F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-2.96F, -0.0F, 1.9733F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-2.9215F, -0.0F, 1.9477F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-11.2074F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-11.9209F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-11.8853F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-11.106F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-9.7015F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-7.8858F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-5.9352F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-4.1467F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-2.7926F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-2.0791F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-2.1147F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-2.894F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-4.2985F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-6.1142F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-8.0648F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-9.8533F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-11.2074F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.9709F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-7.853F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-7.1486F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-5.9649F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-4.482F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-2.9257F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-1.533F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.5159F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-0.0291F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-0.147F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.8514F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-2.0351F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-3.518F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-5.0743F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-6.467F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-7.4841F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-7.9709F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-11.9989F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-11.6582F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-10.6083F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-9.0091F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-7.104F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-5.1831F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-3.5388F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-2.4214F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-2.0011F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-2.3418F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-3.3917F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-4.9909F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-6.896F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-8.8169F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-10.4612F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-11.5786F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-11.9989F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-7.6372F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-6.7233F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-5.3948F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-3.854F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.3354F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-1.0702F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.2511F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.0027F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-0.3628F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-1.2767F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-2.6052F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-4.146F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-5.6646F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-6.9298F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-7.7489F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-7.9973F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-7.6372F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-11.316F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-10.0215F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-8.267F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-6.3196F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-4.4758F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-3.0162F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-2.1632F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-2.0465F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-2.684F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-3.9785F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-5.733F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-7.6804F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-9.5242F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-10.9838F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-11.8368F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-11.9535F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-11.316F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "finger_tip_left_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-6.2307F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-4.7903F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-3.2296F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.7862F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.6798F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-0.0789F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.0749F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.6685F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-1.7693F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-3.2097F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-4.7704F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-6.2138F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-7.3202F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-7.9211F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-7.9251F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-7.3315F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-6.2307F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "thumb_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-4.9563F, -0.0F, 1.9709F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-4.9264F, -0.0F, 1.951F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-4.451F, -0.0F, 1.634F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-3.6024F, -0.0F, 1.0683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.5099F, -0.0F, 0.3399F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-1.3397F, -0.0F, -0.4402F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.2701F, -0.0F, -1.1533F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(0.5362F, -0.0F, -1.6908F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.9563F, -0.0F, -1.9709F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(0.9264F, -0.0F, -1.951F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.451F, -0.0F, -1.634F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.3976F, -0.0F, -1.0683F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-1.4901F, -0.0F, -0.3399F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.6603F, -0.0F, 0.4402F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-3.7299F, -0.0F, 1.1533F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-4.5362F, -0.0F, 1.6908F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-4.9563F, -0.0F, 1.9709F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(2.2884F, -0.0F, 0.6442F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(1.605F, -0.0F, 0.3025F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(0.8294F, -0.0F, -0.0853F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(0.0798F, -0.0F, -0.4601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.5297F, -0.0F, -0.7648F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-0.9063F, -0.0F, -0.9532F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-0.9927F, -0.0F, -0.9964F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.7757F, -0.0F, -0.8879F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-0.2884F, -0.0F, -0.6442F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(0.395F, -0.0F, -0.3025F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(1.1706F, -0.0F, 0.0853F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.9202F, -0.0F, 0.4601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(2.5297F, -0.0F, 0.7648F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(2.9063F, -0.0F, 0.9532F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(2.9927F, -0.0F, 0.9964F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(2.7757F, -0.0F, 0.8879F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(2.2884F, -0.0F, 0.6442F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_tail_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(1.3494F, -0.0F, 0.1498F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.0099F, -0.0F, -0.4328F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.2154F, -0.0F, -0.9495F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-2.0837F, -0.0F, -1.3216F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-2.4825F, -0.0F, -1.4925F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-2.3511F, -0.0F, -1.4362F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-1.7096F, -0.0F, -1.1613F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-0.6555F, -0.0F, -0.7095F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(0.6506F, -0.0F, -0.1498F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.0099F, -0.0F, 0.4328F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.2154F, -0.0F, 0.9495F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(4.0837F, -0.0F, 1.3216F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(4.4825F, -0.0F, 1.4925F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(4.3511F, -0.0F, 1.4362F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(3.7096F, -0.0F, 1.1613F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(2.6555F, -0.0F, 0.7095F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(1.3494F, -0.0F, 0.1498F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(0.0265F, -0.0F, -0.3115F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.7806F, -0.0F, -0.5698F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.3166F, -0.0F, -0.7413F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.4999F, -0.0F, -0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-1.3027F, -0.0F, -0.7368F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-0.7548F, -0.0F, -0.5615F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(0.0602F, -0.0F, -0.3007F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(1.0183F, -0.0F, 0.0058F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(1.9735F, -0.0F, 0.3115F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.7806F, -0.0F, 0.5698F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.3166F, -0.0F, 0.7413F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(3.4999F, -0.0F, 0.8F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(3.3027F, -0.0F, 0.7368F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(2.7548F, -0.0F, 0.5615F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.9398F, -0.0F, 0.3007F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.9817F, -0.0F, -0.0058F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(0.0265F, -0.0F, -0.3115F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_tip_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.3659F, -0.0F, -1.0939F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.9367F, -0.0F, -1.2794F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.9082F, -0.0F, -1.2702F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.2848F, -0.0F, -1.0675F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.1612F, -0.0F, -0.7024F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.2914F, -0.0F, -0.2303F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.8518F, -0.0F, 0.2768F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(4.2826F, -0.0F, 0.7418F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(5.3659F, -0.0F, 1.0939F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(5.9367F, -0.0F, 1.2794F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(5.9082F, -0.0F, 1.2702F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(5.2848F, -0.0F, 1.0675F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(4.1612F, -0.0F, 0.7024F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(2.7086F, -0.0F, 0.2303F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.1482F, -0.0F, -0.2768F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.2826F, -0.0F, -0.7418F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.3659F, -0.0F, -1.0939F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.2884F, -0.0F, 0.6442F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.7757F, -0.0F, 0.8879F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.9927F, -0.0F, 0.9964F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.9063F, -0.0F, 0.9532F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.5297F, -0.0F, 0.7648F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(0.0798F, -0.0F, 0.4601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(0.8294F, -0.0F, 0.0853F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(1.605F, -0.0F, -0.3025F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(2.2884F, -0.0F, -0.6442F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.7757F, -0.0F, -0.8879F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(2.9927F, -0.0F, -0.9964F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.9063F, -0.0F, -0.9532F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(2.5297F, -0.0F, -0.7648F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(1.9202F, -0.0F, -0.4601F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(1.1706F, -0.0F, -0.0853F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.395F, -0.0F, 0.3025F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.2884F, -0.0F, 0.6442F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "skirt_tail_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-2.3725F, -0.0F, 1.4453F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-2.474F, -0.0F, 1.4889F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-2.0467F, -0.0F, 1.3057F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-1.1556F, -0.0F, 0.9238F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.0638F, -0.0F, 0.4012F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.4256F, -0.0F, -0.1824F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.7227F, -0.0F, -0.7383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(3.7575F, -0.0F, -1.1818F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(4.3725F, -0.0F, -1.4453F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(4.474F, -0.0F, -1.4889F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(4.0467F, -0.0F, -1.3057F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(3.1556F, -0.0F, -0.9238F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.9362F, -0.0F, -0.4012F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.5744F, -0.0F, 0.1824F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.7227F, -0.0F, 0.7383F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.7575F, -0.0F, 1.1818F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-2.3725F, -0.0F, 1.4453F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.4636F, -0.0F, 0.7884F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.4387F, -0.0F, 0.7804F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.0425F, -0.0F, 0.6536F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.3354F, -0.0F, 0.4273F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.5751F, -0.0F, 0.136F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.5502F, -0.0F, -0.1761F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.4416F, -0.0F, -0.4613F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(3.1135F, -0.0F, -0.6763F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.4636F, -0.0F, -0.7884F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(3.4387F, -0.0F, -0.7804F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.0425F, -0.0F, -0.6536F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.3354F, -0.0F, -0.4273F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.4249F, -0.0F, -0.136F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.4498F, -0.0F, 0.1761F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.4416F, -0.0F, 0.4613F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.1135F, -0.0F, 0.6763F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.4636F, -0.0F, 0.7884F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "stole_tip_left",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.6372F, -0.0F, 1.1821F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.7233F, -0.0F, 0.8851F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(0.6052F, -0.0F, 0.4533F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(2.146F, -0.0F, -0.0474F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(3.6646F, -0.0F, -0.541F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(4.9298F, -0.0F, -0.9522F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(5.7489F, -0.0F, -1.2184F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(5.9973F, -0.0F, -1.2991F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(5.6372F, -0.0F, -1.1821F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(4.7233F, -0.0F, -0.8851F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.3948F, -0.0F, -0.4533F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.854F, -0.0F, 0.0474F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.3354F, -0.0F, 0.541F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.9298F, -0.0F, 0.9522F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-1.7489F, -0.0F, 1.2184F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.9973F, -0.0F, 1.2991F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.6372F, -0.0F, 1.1821F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "tabard",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.8907F, -0.0F, 0.6745F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.9777F, -0.0F, 0.6948F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.6115F, -0.0F, 0.6093F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.8476F, -0.0F, 0.4311F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.1975F, -0.0F, 0.1872F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.3648F, -0.0F, -0.0851F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.4766F, -0.0F, -0.3445F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(3.3635F, -0.0F, -0.5515F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.8907F, -0.0F, -0.6745F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(3.9777F, -0.0F, -0.6948F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.6115F, -0.0F, -0.6093F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.8476F, -0.0F, -0.4311F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.8025F, -0.0F, -0.1872F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.6352F, -0.0F, 0.0851F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.4766F, -0.0F, 0.3445F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.3635F, -0.0F, 0.5515F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.8907F, -0.0F, 0.6745F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.2912F, -0.0F, 0.3587F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.6729F, -0.0F, 0.4647F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.7998F, -0.0F, 0.4999F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.6527F, -0.0F, 0.4591F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-0.2541F, -0.0F, 0.3484F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(0.3355F, -0.0F, 0.1846F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(1.0263F, -0.0F, -0.0073F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(1.713F, -0.0F, -0.1981F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(2.2912F, -0.0F, -0.3587F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.6729F, -0.0F, -0.4647F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(2.7998F, -0.0F, -0.4999F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.6527F, -0.0F, -0.4591F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(2.2541F, -0.0F, -0.3484F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(1.6645F, -0.0F, -0.1846F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(0.9737F, -0.0F, 0.0073F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(0.287F, -0.0F, 0.1981F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.2912F, -0.0F, 0.3587F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.3301F, -0.0F, 0.5592F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.4994F, -0.0F, 0.5999F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-1.2882F, -0.0F, 0.5492F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.7286F, -0.0F, 0.4149F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.0941F, -0.0F, 0.2174F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.0548F, -0.0F, -0.0131F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.0071F, -0.0F, -0.2417F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(2.8061F, -0.0F, -0.4335F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.3301F, -0.0F, -0.5592F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(3.4994F, -0.0F, -0.5999F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.2882F, -0.0F, -0.5492F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.7286F, -0.0F, -0.4149F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.9059F, -0.0F, -0.2174F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.9452F, -0.0F, 0.0131F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.0071F, -0.0F, 0.2417F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-0.8061F, -0.0F, 0.4335F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.3301F, -0.0F, 0.5592F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_0",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-2.2584F, -0.0F, 0.9463F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.3775F, -0.0F, 0.7505F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(0.0176F, -0.0F, 0.4405F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(1.7145F, -0.0F, 0.0635F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(3.4548F, -0.0F, -0.3233F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(4.9737F, -0.0F, -0.6608F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.0398F, -0.0F, -0.8977F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(6.4909F, -0.0F, -0.998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(6.2584F, -0.0F, -0.9463F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(5.3775F, -0.0F, -0.7505F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(3.9824F, -0.0F, -0.4405F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.2855F, -0.0F, -0.0635F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.5452F, -0.0F, 0.3233F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.9737F, -0.0F, 0.6608F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.0398F, -0.0F, 0.8977F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-2.4909F, -0.0F, 0.998F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-2.2584F, -0.0F, 0.9463F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.4995F, -0.0F, 0.5999F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-1.3291F, -0.0F, 0.559F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.8041F, -0.0F, 0.433F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-0.0045F, -0.0F, 0.2411F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(0.948F, -0.0F, 0.0125F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(1.9085F, -0.0F, -0.218F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(2.7306F, -0.0F, -0.4153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(3.2893F, -0.0F, -0.5494F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.4995F, -0.0F, -0.5999F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(3.3291F, -0.0F, -0.559F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(2.8041F, -0.0F, -0.433F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(2.0045F, -0.0F, -0.2411F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(1.052F, -0.0F, -0.0125F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(0.0915F, -0.0F, 0.218F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.7306F, -0.0F, 0.4153F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.2893F, -0.0F, 0.5494F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.4995F, -0.0F, 0.5999F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_1",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.5013F, -0.0F, 0.7781F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.153F, -0.0F, 0.4785F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(1.523F, -0.0F, 0.106F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(3.2717F, -0.0F, -0.2826F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(4.8268F, -0.0F, -0.6282F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(5.9515F, -0.0F, -0.8781F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.4746F, -0.0F, -0.9944F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(6.3166F, -0.0F, -0.9592F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(5.5013F, -0.0F, -0.7781F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(4.153F, -0.0F, -0.4785F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(2.477F, -0.0F, -0.106F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(0.7283F, -0.0F, 0.2826F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.8268F, -0.0F, 0.6282F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-1.9515F, -0.0F, 0.8781F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.4746F, -0.0F, 0.9944F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-2.3166F, -0.0F, 0.9592F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.5013F, -0.0F, 0.7781F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_panel_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-1.3658F, -0.0F, 0.5678F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.8764F, -0.0F, 0.4503F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.1013F, -0.0F, 0.2643F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(0.8414F, -0.0F, 0.0381F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(1.8082F, -0.0F, -0.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(2.652F, -0.0F, -0.3965F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(3.2443F, -0.0F, -0.5386F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(3.495F, -0.0F, -0.5988F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.3658F, -0.0F, -0.5678F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.8764F, -0.0F, -0.4503F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(2.1013F, -0.0F, -0.2643F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(1.1586F, -0.0F, -0.0381F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(0.1918F, -0.0F, 0.194F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.652F, -0.0F, 0.3965F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-1.2443F, -0.0F, 0.5386F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.495F, -0.0F, 0.5988F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-1.3658F, -0.0F, 0.5678F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "cape_tip_2",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.3198F, -0.0F, 0.5155F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(1.3325F, -0.0F, 0.1483F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(3.0863F, -0.0F, -0.2414F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(4.6747F, -0.0F, -0.5944F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(5.856F, -0.0F, -0.8569F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(6.4502F, -0.0F, -0.9889F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(6.3669F, -0.0F, -0.9704F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(5.6188F, -0.0F, -0.8042F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(4.3198F, -0.0F, -0.5155F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.6675F, -0.0F, -0.1483F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.9137F, -0.0F, 0.2414F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.6747F, -0.0F, 0.5944F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-1.856F, -0.0F, 0.8569F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-2.4502F, -0.0F, 0.9889F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-2.3669F, -0.0F, 0.9704F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.75F,
                                KeyframeAnimations.degreeVec(-1.6188F, -0.0F, 0.8042F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                4.0F,
                                KeyframeAnimations.degreeVec(-0.3198F, -0.0F, 0.5155F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
