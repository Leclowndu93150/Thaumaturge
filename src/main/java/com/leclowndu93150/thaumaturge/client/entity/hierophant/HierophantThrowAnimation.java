package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class HierophantThrowAnimation {
    private HierophantThrowAnimation() {}

    public static AnimationDefinition create() {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(3.6F);
        builder.addAnimation(
                "body",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.posVec(-0.0F, 3.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, -2.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(-0.0F, 12.0F, -5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.35F,
                                KeyframeAnimations.degreeVec(6.0F, 24.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-12.0F, -18.0F, 9.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.15F,
                                KeyframeAnimations.degreeVec(-8.0F, -8.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(-0.0F, -14.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-6.0F, -16.0F, 5.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(8.0F, 12.0F, -4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.POSITION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.05F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.15F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.45F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.posVec(0.072133F, -1.576427F, 0.663757F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.posVec(0.146213F, -4.845065F, 2.271685F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.posVec(0.030424F, -6.655536F, 3.96548F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.posVec(-0.319153F, -6.191106F, 5.516699F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.posVec(-0.68013F, -5.743422F, 6.875867F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.posVec(-1.711549F, -6.334689F, 8.149806F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.posVec(-3.928441F, -6.841148F, 9.526303F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.05F,
                                KeyframeAnimations.posVec(-6.858904F, -7.24078F, 10.797561F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.posVec(-10.016073F, -7.523066F, 11.836446F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.posVec(-12.907531F, -7.696445F, 12.595356F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.posVec(-15.043481F, -7.788994F, 13.086244F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.posVec(-15.970991F, -7.791702F, 13.436386F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.posVec(-16.09032F, -7.689056F, 13.112828F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.35F,
                                KeyframeAnimations.posVec(-16.118534F, -7.421878F, 11.844005F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.posVec(-15.814509F, -6.413942F, 9.299539F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.45F,
                                KeyframeAnimations.posVec(-15.429871F, -4.920265F, 6.019898F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.posVec(-15.107817F, -3.205631F, 2.776063F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.55F,
                                KeyframeAnimations.posVec(-15.642025F, -4.635611F, -5.624564F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.posVec(-19.148489F, -11.058699F, -19.279647F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.posVec(-24.684084F, -18.664921F, -31.038651F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.posVec(-28.048144F, -22.464013F, -35.127776F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.posVec(-28.474165F, -24.344297F, -34.194295F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.posVec(-28.229841F, -28.410363F, -32.192824F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.85F,
                                KeyframeAnimations.posVec(-26.985279F, -32.406448F, -28.967115F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.9F,
                                KeyframeAnimations.posVec(-24.978674F, -34.211368F, -25.490539F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.95F,
                                KeyframeAnimations.posVec(-22.75375F, -33.02396F, -23.148311F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.posVec(-20.213271F, -30.436857F, -21.423736F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.05F,
                                KeyframeAnimations.posVec(-17.191132F, -27.913372F, -19.066582F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.posVec(-13.812498F, -24.870702F, -15.73539F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.15F,
                                KeyframeAnimations.posVec(-10.670668F, -21.782179F, -12.350405F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.posVec(-7.914937F, -18.639021F, -9.201075F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.posVec(-5.526963F, -15.523438F, -6.488276F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.posVec(-3.58327F, -12.437751F, -4.300259F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.35F,
                                KeyframeAnimations.posVec(-2.189059F, -9.334668F, -2.685767F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.posVec(-1.302193F, -6.36971F, -1.619896F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.45F,
                                KeyframeAnimations.posVec(-0.738164F, -3.773149F, -0.930754F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.posVec(-0.344889F, -1.748308F, -0.445368F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.55F,
                                KeyframeAnimations.posVec(-0.090523F, -0.453697F, -0.119646F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.65F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.7F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.85F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.95F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.05F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.1F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.15F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.2F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.3F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.35F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.4F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.45F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.55F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.posVec(-0.0F, 0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.05F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.15F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.45F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-1.084014F, 0.016511F, 0.457268F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-3.95033F, 0.162423F, 1.650147F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-7.75761F, 0.556604F, 3.195372F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-11.833319F, 1.425651F, 4.749716F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-14.7305F, 2.570491F, 5.742787F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-16.430119F, 3.710531F, 6.240017F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-18.180747F, 4.988034F, 6.732315F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.05F,
                                KeyframeAnimations.degreeVec(-19.871338F, 6.308632F, 7.18417F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-21.396421F, 7.570044F, 7.563753F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(-22.654891F, 8.664095F, 7.842516F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-23.547943F, 9.479789F, 7.993946F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-23.976361F, 9.907314F, 7.991454F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-23.837233F, 9.843019F, 7.805479F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.35F,
                                KeyframeAnimations.degreeVec(-23.019851F, 9.195746F, 7.400036F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-19.786343F, 6.705117F, 6.066311F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.45F,
                                KeyframeAnimations.degreeVec(-14.255273F, 2.94581F, 3.839349F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(-7.068349F, -1.071627F, 0.952098F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.55F,
                                KeyframeAnimations.degreeVec(0.989724F, -9.218076F, -2.291909F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(9.495565F, -21.803063F, -7.614031F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(18.268697F, -32.697132F, -15.18471F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(24.495662F, -37.015225F, -20.785402F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(25.75021F, -36.682571F, -21.558915F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(24.895678F, -35.994209F, -20.672795F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.85F,
                                KeyframeAnimations.degreeVec(22.453677F, -34.907279F, -18.554646F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.9F,
                                KeyframeAnimations.degreeVec(19.029822F, -33.319743F, -15.684814F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.95F,
                                KeyframeAnimations.degreeVec(15.229708F, -31.171047F, -12.542148F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(11.570673F, -28.493124F, -9.536148F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.05F,
                                KeyframeAnimations.degreeVec(8.415574F, -25.407565F, -6.951414F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(5.952827F, -22.08718F, -4.930675F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.15F,
                                KeyframeAnimations.degreeVec(4.217895F, -18.70925F, -3.493988F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(3.098623F, -15.44973F, -2.554864F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(2.177785F, -11.222221F, -1.380121F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(1.40427F, -6.731478F, -0.432665F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.35F,
                                KeyframeAnimations.degreeVec(0.758255F, -3.446321F, -0.184592F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(0.315837F, -1.473852F, -0.240794F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.45F,
                                KeyframeAnimations.degreeVec(0.095297F, -0.419645F, -0.214011F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.019583F, 0.003367F, -0.105337F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.55F,
                                KeyframeAnimations.degreeVec(0.002154F, 0.039125F, -0.026755F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.65F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.7F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.85F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.95F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.05F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.15F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.35F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.45F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.55F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "halo_segment_6",
                new AnimationChannel(
                        AnimationChannel.Targets.SCALE,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.scaleVec(0.001F, 0.001F, 0.001F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.2F,
                                KeyframeAnimations.scaleVec(0.001F, 0.001F, 0.001F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "arm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.05F,
                                KeyframeAnimations.degreeVec(0.803901F, -0.050748F, -0.395793F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(5.848184F, -0.145853F, -4.765039F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.15F,
                                KeyframeAnimations.degreeVec(16.347298F, 1.159808F, -15.681626F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(27.251817F, 5.576531F, -30.340637F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(33.780901F, 10.162936F, -40.265305F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(36.733897F, 12.766634F, -44.862551F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(35.946695F, 15.194521F, -52.320046F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(28.202477F, 15.615386F, -64.87541F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.45F,
                                KeyframeAnimations.degreeVec(-2.371928F, -1.126838F, -58.437178F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(9.018807F, 0.388466F, -12.542452F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(24.579638F, 1.489145F, -14.447829F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(34.500865F, 4.207786F, -21.114157F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(40.942557F, 7.028861F, -26.304456F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(43.796733F, 8.201544F, -27.845751F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(46.680387F, 9.548725F, -29.529433F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(52.295917F, 13.687147F, -35.094219F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(59.06532F, 19.514654F, -41.390138F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(64.582994F, 23.141579F, -43.521603F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(69.113053F, 22.085766F, -39.260671F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(74.172338F, 19.056415F, -32.658371F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.05F,
                                KeyframeAnimations.degreeVec(79.322182F, 15.14231F, -25.836131F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(84.776867F, 10.931325F, -19.588792F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(90.382952F, 6.609156F, -14.18509F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(95.370292F, 2.713961F, -10.090745F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(99.64423F, 0.375402F, -7.936856F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(97.289468F, 0.867012F, -8.38304F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.35F,
                                KeyframeAnimations.degreeVec(90.395504F, 2.824268F, -10.424712F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(82.964731F, 7.388973F, -15.973533F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.45F,
                                KeyframeAnimations.degreeVec(76.71483F, 13.857388F, -25.079736F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(70.099108F, 21.54175F, -37.965222F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.55F,
                                KeyframeAnimations.degreeVec(47.041905F, 24.478679F, -60.603032F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-20.993923F, -17.651748F, -87.546778F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-82.468955F, -5.813748F, -14.251083F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-85.121026F, 29.027642F, 23.86792F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(-79.630751F, 29.620702F, 27.575611F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(-68.324997F, 28.025917F, 32.761733F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.85F,
                                KeyframeAnimations.degreeVec(-56.032173F, 24.427117F, 36.654863F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.9F,
                                KeyframeAnimations.degreeVec(-47.074503F, 20.812816F, 38.102283F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.95F,
                                KeyframeAnimations.degreeVec(-43.982873F, 19.107276F, 37.627918F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(-43.917018F, 18.238944F, 35.798829F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.05F,
                                KeyframeAnimations.degreeVec(-41.554571F, 14.603113F, 29.700748F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-33.439044F, 7.467136F, 16.892992F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.15F,
                                KeyframeAnimations.degreeVec(-24.433201F, 3.53418F, 8.599457F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-15.693767F, 1.352761F, 2.172344F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-7.393624F, 0.317473F, -2.709267F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-1.2318F, 0.028108F, -5.005642F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.35F,
                                KeyframeAnimations.degreeVec(1.804717F, -0.046203F, -4.687036F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(2.31324F, -0.095781F, -2.878467F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.45F,
                                KeyframeAnimations.degreeVec(1.575274F, -0.089981F, -1.081798F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(0.734744F, -0.047301F, -0.252949F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.55F,
                                KeyframeAnimations.degreeVec(0.197202F, -0.013065F, -0.039165F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.65F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.7F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.85F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.95F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.05F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.15F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.35F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.45F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.55F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "forearm_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.05F,
                                KeyframeAnimations.degreeVec(-0.476185F, 0.10867F, 0.210044F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(-2.783549F, 0.698686F, 3.135707F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.15F,
                                KeyframeAnimations.degreeVec(-7.084542F, 2.510725F, 12.360551F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-9.685651F, 5.780836F, 29.405471F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-9.993804F, 8.957827F, 46.589028F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-10.151161F, 10.811359F, 55.400819F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-4.72201F, 8.053385F, 61.750794F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(10.924657F, -4.104299F, 67.606288F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.45F,
                                KeyframeAnimations.degreeVec(83.979808F, -21.394772F, 14.271155F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(80.937585F, 30.716104F, -51.429847F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(63.459293F, 30.31844F, -64.60234F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(53.572184F, 24.960695F, -65.328252F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(46.956685F, 19.874307F, -62.264809F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(44.457713F, 17.40413F, -59.384895F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(43.378555F, 16.587214F, -58.62486F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(39.167962F, 14.182727F, -57.343498F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(31.925695F, 9.384875F, -51.251589F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(25.511863F, 5.574787F, -43.621015F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(25.779687F, 5.014319F, -40.196064F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(30.189964F, 5.613832F, -37.774713F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.05F,
                                KeyframeAnimations.degreeVec(35.083184F, 5.695615F, -33.712035F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(37.2573F, 4.591362F, -28.310463F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(35.514917F, 3.020443F, -23.430971F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(31.032576F, 1.902146F, -20.553364F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(24.627443F, 1.337683F, -20.143132F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(29.972563F, 1.565794F, -19.365465F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.35F,
                                KeyframeAnimations.degreeVec(45.308706F, 2.1765F, -18.089018F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(64.26954F, 4.083588F, -19.319426F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.45F,
                                KeyframeAnimations.degreeVec(79.363735F, 9.704065F, -25.002742F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(84.393472F, 19.550616F, -35.858725F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.55F,
                                KeyframeAnimations.degreeVec(67.715672F, 48.083843F, -85.492584F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-113.310994F, -28.746595F, -29.063008F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(-31.93221F, 15.465221F, 31.118662F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(5.589353F, -0.017907F, -16.993869F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(5.617441F, -0.016646F, -16.982908F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(5.321511F, -0.029089F, -16.909903F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.85F,
                                KeyframeAnimations.degreeVec(4.878578F, -0.049091F, -17.012958F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.9F,
                                KeyframeAnimations.degreeVec(4.500996F, -0.068909F, -17.263714F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.95F,
                                KeyframeAnimations.degreeVec(4.211331F, -0.084601F, -17.415482F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(3.87834F, -0.10001F, -17.396979F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.05F,
                                KeyframeAnimations.degreeVec(0.042232F, 0.104283F, -9.996233F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-12.539917F, 3.627929F, 9.962035F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.15F,
                                KeyframeAnimations.degreeVec(-20.156947F, 7.401561F, 19.11684F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-24.105319F, 9.903161F, 23.712662F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(-24.671159F, 10.182458F, 24.094775F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(-20.91503F, 7.627892F, 19.547277F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.35F,
                                KeyframeAnimations.degreeVec(-14.293697F, 4.173614F, 12.130168F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(-7.558823F, 1.736168F, 5.170676F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.45F,
                                KeyframeAnimations.degreeVec(-2.984093F, 0.602114F, 1.118231F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.999905F, 0.205923F, 0.036056F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.55F,
                                KeyframeAnimations.degreeVec(-0.242374F, 0.051733F, -0.015517F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.65F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.7F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.85F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.95F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.05F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.15F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.35F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.45F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.55F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
        builder.addAnimation(
                "hand_right",
                new AnimationChannel(
                        AnimationChannel.Targets.ROTATION,
                        new Keyframe(
                                0.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.05F,
                                KeyframeAnimations.degreeVec(0.423983F, -0.036193F, 0.24772F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.1F,
                                KeyframeAnimations.degreeVec(0.853176F, -0.034102F, 1.304698F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.15F,
                                KeyframeAnimations.degreeVec(-0.931471F, 0.939592F, 3.588328F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.2F,
                                KeyframeAnimations.degreeVec(-5.60485F, 4.866035F, 4.378675F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.25F,
                                KeyframeAnimations.degreeVec(-9.450376F, 10.579447F, 0.21337F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.3F,
                                KeyframeAnimations.degreeVec(-10.676846F, 13.514229F, -2.65287F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.35F,
                                KeyframeAnimations.degreeVec(-14.505407F, 16.778774F, -0.943305F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.4F,
                                KeyframeAnimations.degreeVec(-23.65236F, 25.204291F, 1.690167F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.45F,
                                KeyframeAnimations.degreeVec(-60.309054F, 49.369578F, 5.052655F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.5F,
                                KeyframeAnimations.degreeVec(-67.215037F, 58.879882F, 59.794016F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.55F,
                                KeyframeAnimations.degreeVec(-60.537525F, 59.15026F, 79.40158F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.6F,
                                KeyframeAnimations.degreeVec(-55.943019F, 59.05084F, 87.9774F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.65F,
                                KeyframeAnimations.degreeVec(-53.449708F, 57.925887F, 90.975692F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.7F,
                                KeyframeAnimations.degreeVec(-53.742997F, 56.928135F, 89.91742F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.75F,
                                KeyframeAnimations.degreeVec(-53.796136F, 58.257257F, 91.786687F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.8F,
                                KeyframeAnimations.degreeVec(-50.277766F, 60.668327F, 99.248261F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.85F,
                                KeyframeAnimations.degreeVec(-47.241866F, 60.619002F, 103.318995F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.9F,
                                KeyframeAnimations.degreeVec(-48.730828F, 58.635658F, 99.635442F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-57.077586F, 59.293709F, 89.838482F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.0F,
                                KeyframeAnimations.degreeVec(-72.918471F, 61.993919F, 73.53082F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.05F,
                                KeyframeAnimations.degreeVec(-93.910381F, 62.167811F, 51.203125F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.1F,
                                KeyframeAnimations.degreeVec(-110.71492F, 57.965167F, 31.757126F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.15F,
                                KeyframeAnimations.degreeVec(-118.691207F, 51.63939F, 20.50154F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.2F,
                                KeyframeAnimations.degreeVec(-120.499338F, 45.986505F, 15.849377F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.25F,
                                KeyframeAnimations.degreeVec(-118.300542F, 42.497269F, 16.410055F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.3F,
                                KeyframeAnimations.degreeVec(-122.273855F, 43.463663F, 13.17793F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.35F,
                                KeyframeAnimations.degreeVec(-133.964136F, 46.10789F, 3.807651F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-152.42631F, 48.940554F, -10.336837F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.45F,
                                KeyframeAnimations.degreeVec(-174.64695F, 51.530697F, -25.716328F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.5F,
                                KeyframeAnimations.degreeVec(160.462652F, 55.752222F, -41.205251F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.55F,
                                KeyframeAnimations.degreeVec(89.123604F, 55.713994F, -84.476445F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-97.635933F, -31.787138F, -109.005997F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.65F,
                                KeyframeAnimations.degreeVec(152.3733F, -15.876296F, -36.952206F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(129.951808F, 12.72518F, -5.155079F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(126.245916F, 15.187679F, -5.922483F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(116.651671F, 18.003935F, -8.366196F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.85F,
                                KeyframeAnimations.degreeVec(104.703444F, 19.530588F, -11.176446F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.9F,
                                KeyframeAnimations.degreeVec(94.887912F, 19.201555F, -13.173377F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                1.95F,
                                KeyframeAnimations.degreeVec(90.779459F, 17.52887F, -13.968822F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.0F,
                                KeyframeAnimations.degreeVec(89.718089F, 15.092228F, -13.720161F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.05F,
                                KeyframeAnimations.degreeVec(88.957644F, 11.789735F, -15.878499F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(90.703826F, 9.102979F, -23.150089F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.15F,
                                KeyframeAnimations.degreeVec(88.534621F, 7.804423F, -25.032532F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(83.796724F, 6.96685F, -24.025022F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.25F,
                                KeyframeAnimations.degreeVec(72.706979F, 5.062654F, -19.378605F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.3F,
                                KeyframeAnimations.degreeVec(55.043734F, 2.959173F, -11.411773F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.35F,
                                KeyframeAnimations.degreeVec(37.169658F, 2.161015F, -4.286893F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.4F,
                                KeyframeAnimations.degreeVec(23.046612F, 1.846149F, -0.604582F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.45F,
                                KeyframeAnimations.degreeVec(13.105922F, 1.230819F, 0.297645F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(6.178497F, 0.542745F, 0.203749F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.55F,
                                KeyframeAnimations.degreeVec(1.659865F, 0.123673F, 0.06081F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.65F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.7F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.75F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.85F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                2.95F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.0F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.05F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.1F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.15F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.2F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.25F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.3F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.35F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.4F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.45F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.55F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.LINEAR)));
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
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-76.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-76.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-76.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-76.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-76.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-76.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(15.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-65.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                0.95F,
                                KeyframeAnimations.degreeVec(-35.0F, -0.0F, -35.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.6F,
                                KeyframeAnimations.degreeVec(-35.0F, -0.0F, -35.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.75F,
                                KeyframeAnimations.degreeVec(10.0F, -0.0F, 10.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.5F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                0.8F,
                                KeyframeAnimations.degreeVec(-22.0F, -0.0F, 20.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.4F,
                                KeyframeAnimations.degreeVec(-35.0F, -8.0F, 30.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-55.0F, -0.0F, 12.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
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
                                1.3F,
                                KeyframeAnimations.degreeVec(-28.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(-10.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
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
                                1.2F,
                                KeyframeAnimations.degreeVec(8.0F, -0.0F, 4.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.7F,
                                KeyframeAnimations.degreeVec(24.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.1F,
                                KeyframeAnimations.degreeVec(-8.0F, -0.0F, 3.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.8F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
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
                                1.3F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(32.0F, -0.0F, -8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
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
                                1.3F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(32.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
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
                                1.3F,
                                KeyframeAnimations.degreeVec(12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                1.8F,
                                KeyframeAnimations.degreeVec(32.0F, -0.0F, 8.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.2F,
                                KeyframeAnimations.degreeVec(-12.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                2.9F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM),
                        new Keyframe(
                                3.6F,
                                KeyframeAnimations.degreeVec(-0.0F, -0.0F, 0.0F),
                                AnimationChannel.Interpolations.CATMULLROM)));
        return builder.build();
    }
}
