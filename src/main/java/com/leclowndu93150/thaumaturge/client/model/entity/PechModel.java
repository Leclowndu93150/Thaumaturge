package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.PechRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class PechModel extends EntityModel<PechRenderState> {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float TORSO_LEAN = 0.2094F;
    private static final float PACK_TILT = -0.1047F;
    private static final float WALK_FREQUENCY = 0.62F;
    private static final float LEG_SWING = 1.3F;
    private static final float ARM_SWING = 0.9F;
    private static final float JAW_MUMBLE_OPEN = 0.38F;
    private static final float JAW_MUMBLE_RATE = 0.14F;
    private static final float JAW_WALK_CHATTER = 0.12F;
    private static final float POUCH_SWAY = 0.22F;
    private static final float POUCH_BOUNCE = 0.12F;
    private static final float PACK_SWAY = 0.05F;
    private static final float IDLE_ARM_RATE = 0.08F;
    private static final float IDLE_ARM_SPREAD = 0.06F;
    private static final float ATTACK_LIFT = 1.4F;
    private static final float ATTACK_TWIST = 0.35F;

    public final ModelPart head;
    public final ModelPart jowls;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    public final ModelPart pack;
    public final ModelPart pouch;

    public PechModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.jowls = root.getChild("jowls");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.pack = root.getChild("pack");
        this.pouch = root.getChild("pouch");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(87, 23).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 3.0F, 4.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        body.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(90, 0).addBox(-3.5F, -7.0F, -2.5F, 7.0F, 7.0F, 5.0F), PartPose.rotation(TORSO_LEAN, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 23).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 5.0F).texOffs(85, 35).addBox(-3.5F, -5.0F, -3.5F, 7.0F, 1.0F, 1.0F)
                .texOffs(78, 35).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 9.0F, -1.5F));
        root.addOrReplaceChild("jowls", CubeListBuilder.create().texOffs(41, 23).addBox(-3.5F, -1.0F, -4.0F, 7.0F, 3.0F, 4.0F), PartPose.offset(0.0F, 9.0F, -1.5F));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().mirror().texOffs(108, 23).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F).texOffs(48, 35).addBox(-1.5F, 4.0F, -2.5F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(-1.5F, 19.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 35).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F).texOffs(63, 35).addBox(-1.5F, 4.0F, -2.5F, 3.0F, 1.0F, 4.0F),
                PartPose.offset(1.5F, 19.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().mirror().texOffs(23, 23).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(-3.5F, 10.0F, -1.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 23).addBox(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(3.5F, 10.0F, -1.0F));
        root.addOrReplaceChild("pack", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.0F, 0.0F, 12.0F, 13.0F, 9.0F).texOffs(43, 0).addBox(-6.5F, -8.0F, -0.5F, 13.0F, 2.0F, 10.0F)
                .texOffs(13, 35).addBox(-7.0F, -11.0F, 2.5F, 14.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 11.0F, 2.0F, PACK_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("pouch", CubeListBuilder.create().texOffs(64, 23).addBox(-4.0F, 0.0F, -0.5F, 8.0F, 4.0F, 3.0F), PartPose.offset(0.0F, 16.0F, 2.5F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void setupAnim(PechRenderState state) {
        super.setupAnim(state);
        float phase = state.walkAnimationPos * WALK_FREQUENCY;
        float speed = Math.min(state.walkAnimationSpeed, 1.0F);
        float stride = Mth.sin(phase) * speed;
        this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
        this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
        this.jowls.yRot = this.head.yRot;
        this.jowls.xRot = this.head.xRot + JAW_MUMBLE_OPEN * Mth.abs(Mth.sin(state.mumble * JAW_MUMBLE_RATE)) + JAW_WALK_CHATTER * speed * Mth.abs(Mth.sin(phase * 2.0F));
        this.rightLeg.xRot = stride * LEG_SWING;
        this.leftLeg.xRot = -stride * LEG_SWING;
        this.rightArm.xRot = -stride * ARM_SWING;
        this.leftArm.xRot = stride * ARM_SWING;
        float spread = IDLE_ARM_SPREAD * (1.0F + Mth.sin(state.ageInTicks * IDLE_ARM_RATE));
        this.rightArm.zRot = spread;
        this.leftArm.zRot = -spread;
        this.pouch.zRot = stride * POUCH_SWAY;
        this.pouch.xRot = POUCH_BOUNCE * speed * Mth.abs(Mth.cos(phase));
        this.pack.zRot = stride * PACK_SWAY;
        if (state.attackTime > 0.0F) {
            float arc = Mth.sin(Mth.sqrt(state.attackTime) * Mth.PI);
            this.rightArm.xRot -= arc * ATTACK_LIFT;
            this.rightArm.yRot = -Mth.sin(state.attackTime * Mth.PI) * ATTACK_TWIST;
        }
    }
}
