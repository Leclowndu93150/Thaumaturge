package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public final class HierophantModel extends HierarchicalModel<EntityEldritchHierophant> {
    private static final float MILLIS_PER_TICK = 50;
    private static final float BLEND_IN_TICKS = 3;
    private static final float BLEND_OUT_TICKS = 5;
    private static final float RECOIL_TICKS = 10;
    private final ModelPart root;
    private final Vector3f scratch = new Vector3f();
    private boolean hammerOnly;
    private final AnimationDefinition idle;
    private final AnimationDefinition glide;
    private final AnimationDefinition hurt;
    private final Map<HierophantAction, AnimationDefinition> actions = new EnumMap<>(HierophantAction.class);

    public HierophantModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        idle = HierophantIdleAnimation.create();
        glide = HierophantGlideAnimation.create();
        hurt = HierophantHurtAnimation.create();
        actions.put(HierophantAction.SUMMON, HierophantSummonAnimation.create());
        actions.put(HierophantAction.CAST, HierophantCastAnimation.create());
        actions.put(HierophantAction.SWIPE_RIGHT, HierophantSwipeRightAnimation.create());
        actions.put(HierophantAction.SWIPE_LEFT, HierophantSwipeLeftAnimation.create());
        actions.put(HierophantAction.NOVA, HierophantNovaAnimation.create());
        actions.put(HierophantAction.THROW, HierophantThrowAnimation.create());
        actions.put(HierophantAction.DEATH, HierophantDeathAnimation.create());
    }

    public void onlyEye() {
        for (ModelPart part : root.getAllParts().toList()) {
            part.skipDraw = true;
        }
        root.getChild("body").getChild("head").getChild("eye").skipDraw = false;
    }

    public void onlyHammer() {
        hammerOnly = true;
        for (ModelPart part : root.getAllParts().toList()) {
            part.skipDraw = true;
        }
        root.getChild("body").getChild("halo").getChild("halo_segment_6").skipDraw = false;
    }

    public Vector3f palm(boolean left) {
        final String side = left ? "left" : "right";
        final PoseStack pose = new PoseStack();
        root.translateAndRotate(pose);
        final ModelPart body = root.getChild("body");
        body.translateAndRotate(pose);
        final ModelPart arm = body.getChild("arm_" + side);
        arm.translateAndRotate(pose);
        final ModelPart forearm = arm.getChild("forearm_" + side);
        forearm.translateAndRotate(pose);
        final ModelPart hand = forearm.getChild("hand_" + side);
        hand.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f(0, 4.0F / 16, -1.0F / 16));
    }

    public void setupAnim(HierophantRenderState state) {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.getChild("body").getChild("halo").getChild("halo_segment_6").visible = hammerOnly
                || state.action != HierophantAction.THROW
                || state.actionTicks < HierophantAction.THROW.release()
                || state.actionTicks >= 64;
        final AnimationDefinition action = actions.get(state.action);
        final float blend = action == null
                ? 0
                : state.action == HierophantAction.SUMMON || state.action == HierophantAction.DEATH
                        ? 1
                        : Mth.clamp(
                                Math.min(
                                        state.actionTicks / BLEND_IN_TICKS,
                                        (state.action.duration() - state.actionTicks) / BLEND_OUT_TICKS),
                                0,
                                1);
        final long idleTime = (long) (state.ageInTicks * MILLIS_PER_TICK);
        KeyframeAnimations.animate(this, idle, idleTime, (1 - blend) * (1 - state.movement), scratch);
        KeyframeAnimations.animate(this, glide, idleTime, (1 - blend) * state.movement, scratch);
        if (action != null) {
            KeyframeAnimations.animate(this, action, (long) (state.actionTicks * MILLIS_PER_TICK), blend, scratch);
        }
        if (state.action == HierophantAction.IDLE && state.recoil > 0) {
            KeyframeAnimations.animate(
                    this, hurt, (long) ((RECOIL_TICKS - state.recoil) * MILLIS_PER_TICK), 0.6F, scratch);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            EntityEldritchHierophant entity,
            float limbSwing,
            float limbAmount,
            float ageInTicks,
            float yaw,
            float pitch) {
        setupAnim(HierophantRenderState.of(entity, ageInTicks - entity.tickCount));
    }
}
