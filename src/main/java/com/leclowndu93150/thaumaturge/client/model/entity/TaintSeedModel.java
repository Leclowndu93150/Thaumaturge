package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.content.entity.AbstractTaintSeed;
import java.util.EnumSet;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public final class TaintSeedModel extends HierarchicalModel<AbstractTaintSeed> {
    protected final ModelPart root;

    @Override
    public ModelPart root() {
        return root;
    }

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart[] growth = new ModelPart[5];
    private final ModelPart anchor;
    private final ModelPart body;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;

    public TaintSeedModel(ModelPart root) {
        this.root = root;
        anchor = root.getChild("root");
        body = anchor.getChild("base").getChild("body");
        front = body.getChild("front");
        back = body.getChild("back");
        left = body.getChild("left");
        right = body.getChild("right");
        ModelPart parent = body;
        for (int k = 0; k < growth.length; k++) {
            parent = parent.getChild("growth_" + k);
            growth[k] = parent;
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();
        PartDefinition root =
                meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition base = root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(1, 1)
                        .addBox(
                                -4.75F,
                                -3.5F,
                                -4.75F,
                                9.5F,
                                3.5F,
                                9.5F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH))
                        .texOffs(40, 1)
                        .addBox(
                                -8.0F,
                                -2.75F,
                                -2.0F,
                                4.0F,
                                2.5F,
                                4.0F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH))
                        .texOffs(57, 1)
                        .addBox(
                                4.0F,
                                -2.75F,
                                -2.0F,
                                4.0F,
                                2.5F,
                                4.0F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH))
                        .texOffs(74, 1)
                        .addBox(
                                -2.0F,
                                -3.0F,
                                -8.0F,
                                4.0F,
                                2.5F,
                                4.0F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH))
                        .texOffs(91, 1)
                        .addBox(
                                -2.0F,
                                -3.25F,
                                4.0F,
                                4.0F,
                                2.5F,
                                4.0F,
                                EnumSet.of(
                                        Direction.DOWN,
                                        Direction.WEST,
                                        Direction.NORTH,
                                        Direction.EAST,
                                        Direction.SOUTH)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = base.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(1, 15).addBox(-5.0F, -19.75F, -5.0F, 10.0F, 21.0F, 10.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        body.addOrReplaceChild(
                "front",
                CubeListBuilder.create()
                        .texOffs(42, 15)
                        .addBox(-6.0F, -16.5F, -4.75F, 12.0F, 17.5F, 4.5F)
                        .texOffs(64, 47)
                        .addBox(-4.0F, -7.5F, -5.5F, 3.5F, 3.5F, 1.5F),
                PartPose.offset(0.0F, -2.0F, -3.0F));
        body.addOrReplaceChild(
                "back",
                CubeListBuilder.create().texOffs(76, 15).addBox(-5.5F, -18.25F, 0.5F, 11.0F, 19.5F, 4.5F),
                PartPose.offset(0.0F, -2.0F, 3.0F));
        body.addOrReplaceChild(
                "left",
                CubeListBuilder.create()
                        .texOffs(1, 47)
                        .addBox(-6.0F, -15.75F, -5.25F, 5.5F, 15.0F, 10.5F)
                        .texOffs(75, 47)
                        .addBox(-6.75F, -9.5F, 0.0F, 1.5F, 3.5F, 3.0F),
                PartPose.offset(-3.0F, -2.0F, 0.0F));
        body.addOrReplaceChild(
                "right",
                CubeListBuilder.create().texOffs(34, 47).addBox(0.5F, -12.75F, -5.5F, 3.5F, 13.0F, 11.0F),
                PartPose.offset(3.0F, -2.0F, 0.0F));
        PartDefinition growth0 = body.addOrReplaceChild(
                "growth_0",
                CubeListBuilder.create()
                        .texOffs(85, 47)
                        .addBox(-5.5F, -8.0F, -4.0F, 11.0F, 10.0F, 8.0F)
                        .texOffs(1, 74)
                        .addBox(-4.5F, -7.75F, -5.0F, 9.0F, 9.5F, 10.0F),
                PartPose.offsetAndRotation(-1.5F, -16.5F, 0.5F, 0.0F, 0.0F, 0.0349066F));
        PartDefinition growth1 = growth0.addOrReplaceChild(
                "growth_1",
                CubeListBuilder.create()
                        .texOffs(40, 74)
                        .addBox(-3.5F, -6.0F, -2.0F, 7.0F, 8.0F, 4.0F)
                        .texOffs(63, 74)
                        .addBox(-2.5F, -5.75F, -3.0F, 5.0F, 7.5F, 6.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, 0.0872665F, 0.0F, 0.0349066F));
        PartDefinition growth2 = growth1.addOrReplaceChild(
                "growth_2",
                CubeListBuilder.create()
                        .texOffs(86, 74)
                        .addBox(-2.5F, -5.0F, -1.25F, 5.0F, 7.0F, 2.5F)
                        .texOffs(102, 74)
                        .addBox(-1.5F, -4.75F, -2.25F, 3.0F, 6.5F, 4.5F),
                PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, 0.1745329F, 0.0F, 0.0349066F));
        PartDefinition growth3 = growth2.addOrReplaceChild(
                "growth_3",
                CubeListBuilder.create().texOffs(1, 95).addBox(-1.75F, -4.5F, -1.5F, 3.5F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, -4.75F, 0.0F, 0.2617994F, 0.0F, 0.0349066F));
        growth3.addOrReplaceChild(
                "growth_4",
                CubeListBuilder.create().texOffs(15, 95).addBox(-1.25F, -3.0F, -1.25F, 2.5F, 4.5F, 2.5F),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.3490659F, 0.0F, 0.0349066F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(
            AbstractTaintSeed entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partialTicks = ageInTicks - entity.tickCount;
        float deathTime =
                Mth.clamp((entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F) / 20.0F, 0.0F, 1.0F);
        float death = deathTime * deathTime * (3.0F - 2.0F * deathTime);
        float alive = 1.0F - death;
        float agitation = Mth.clamp(entity.attackAnim * 2.0F, 0.0F, 1.0F);
        float phase = ageInTicks * Mth.PI / 20.0F;
        float wave = Mth.lerp(agitation, Mth.sin(phase), Mth.sin(phase * 2.0F));
        float pulse = (0.5F + 0.5F * wave) * Mth.lerp(agitation, 0.35F, 0.9F) * alive;
        float attackTime = 1.0F - Math.max(0.0F, entity.strikeTicks() - partialTicks) / 20.0F;
        float strike = Math.max(0.0F, entity.strikeTicks() - partialTicks) > 0.0F
                ? Mth.sin(Mth.PI * Mth.clamp((attackTime - 0.12F) / 0.65F, 0.0F, 1.0F))
                : 0.0F;
        float recoil = Math.max(0.0F, entity.strikeTicks() - partialTicks) > 0.0F && attackTime < 0.12F
                ? -Mth.sin(Mth.PI * attackTime / 0.12F) * 0.15F
                : 0.0F;
        float motion = (strike + recoil) * alive;
        float hurt =
                Mth.sin(Mth.PI * Mth.clamp(Math.max(0.0F, entity.hurtTime - partialTicks) / 10.0F, 0.0F, 1.0F)) * alive;
        float emergence = 1.0F - Mth.clamp(ageInTicks / (entity.getBbHeight() * 10.0F), 0.0F, 1.0F);
        anchor.y += 49.0F * emergence * emergence * emergence + death * 1.5F;
        body.xRot -= (-motion * 11.0F + hurt * 6.0F - death * 26.0F) * Mth.DEG_TO_RAD;
        body.zRot -= Mth.sin(phase) * 0.8F * alive * Mth.DEG_TO_RAD;
        animateLobe(front, 0.0F, -1.0F, pulse, death);
        animateLobe(back, 0.0F, 1.0F, pulse, death);
        animateLobe(left, -1.0F, 0.0F, pulse, death);
        animateLobe(right, 1.0F, 0.0F, pulse, death);
        for (int k = 0; k < growth.length; k++) {
            float x = Mth.lerp(agitation, Mth.sin(phase - k * 0.65F) * 1.7F, Mth.sin(phase * 2.0F - k * 0.65F) * 3.5F)
                    * alive;
            float z = Mth.lerp(agitation, Mth.sin(phase - k * 0.55F) * 0.85F, Mth.sin(phase * 2.0F - k * 0.55F) * 1.75F)
                    * alive;
            x += -motion * (6.0F + k * 1.4F) + hurt * Mth.sin(k * 0.8F + 1.0F) * 7.0F - death * (3.0F + k * 3.0F);
            growth[k].xRot -= x * Mth.DEG_TO_RAD;
            growth[k].zRot -= z * Mth.DEG_TO_RAD;
        }
    }

    private void animateLobe(ModelPart lobe, float x, float z, float pulse, float death) {
        lobe.x += x * pulse * 0.65F;
        lobe.z += z * pulse * 0.65F;
        lobe.xRot -= z * (pulse * 1.4F + death * 6.0F) * Mth.DEG_TO_RAD;
        lobe.zRot += x * (pulse * 1.8F + death * 8.0F) * Mth.DEG_TO_RAD;
    }
}
