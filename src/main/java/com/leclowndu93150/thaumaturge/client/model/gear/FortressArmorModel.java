package com.leclowndu93150.thaumaturge.client.model.gear;

import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import java.util.EnumSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.Direction;

public final class FortressArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart mask0;
    private final ModelPart mask1;
    private final ModelPart mask2;
    private final ModelPart goggles;

    public FortressArmorModel(ModelPart root) {
        super(root);
        mask0 = head.getChild("mask_0");
        mask1 = head.getChild("mask_1");
        mask2 = head.getChild("mask_2");
        goggles = head.getChild("goggles");
        mask0.visible = false;
        mask1.visible = false;
        mask2.visible = false;
        goggles.visible = false;
    }

    @Override
    public void setupAnim(HumanoidRenderState state) {
        super.setupAnim(state);
        int mask = FortressArmorItem.mask(state.headEquipment);
        mask0.visible = mask == 0;
        mask1.visible = mask == 1;
        mask2.visible = mask == 2;
        goggles.visible = FortressArmorItem.hasGoggles(state.headEquipment);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(1, 9).addBox(-4.5F, -8.75F, -4.5F, 9.0F, 2.5F, 9.0F, new CubeDeformation(0.03125F)).texOffs(38, 9)
                        .addBox(-4.75F, -6.5F, -5.0F, 9.5F, 1.0F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(60, 9)
                        .addBox(-5.0F, -6.125F, -3.75F, 1.0F, 6.0F, 7.5F, new CubeDeformation(0.03125F)).texOffs(78, 9).addBox(4.0F, -6.125F, -3.75F, 1.0F, 6.0F, 7.5F, new CubeDeformation(0.03125F))
                        .texOffs(96, 9).addBox(-4.25F, -5.875F, 4.125F, 8.5F, 5.5F, 0.5F, EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(119, 9)
                        .addBox(-4.25F, -7.875F, -4.875F, 1.5F, 1.0F, 0.5F).texOffs(1, 24).addBox(2.75F, -7.875F, -4.875F, 1.5F, 1.0F, 0.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("swept_crest_-1", CubeListBuilder.create().texOffs(115, 9).addBox(-0.5F, -3.5F, -0.625F, 1.0F, 4.0F, 0.5F),
                PartPose.offsetAndRotation(-2.5F, -8.0F, -4.5F, 0.0F, 0.0F, -0.5235988F));
        head.addOrReplaceChild("swept_crest_1", CubeListBuilder.create().texOffs(124, 9).addBox(-0.5F, -3.5F, -0.625F, 1.0F, 4.0F, 0.5F),
                PartPose.offsetAndRotation(2.5F, -8.0F, -4.5F, 0.0F, 0.0F, 0.5235988F));
        head.addOrReplaceChild("mask_0", CubeListBuilder.create().texOffs(1, 1).addBox(-4.5F, -5.25F, -4.75F, 9.0F, 5.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask_1", CubeListBuilder.create().texOffs(21, 1).addBox(-4.5F, -5.25F, -4.75F, 9.0F, 5.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("mask_2", CubeListBuilder.create().texOffs(41, 1).addBox(-4.5F, -5.25F, -4.75F, 9.0F, 5.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("goggles", CubeListBuilder.create().texOffs(61, 1).addBox(-4.5F, -6.125F, -5.5F, 9.0F, 5.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.SOUTH)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(81, 1).addBox(-4.5F, 10.75F, -2.75F, 9.0F, 1.0F, 5.5F).texOffs(111, 1)
                        .addBox(-0.75F, 11.125F, -3.375F, 1.5F, 0.5F, 1.0F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(6, 24)
                        .addBox(-4.25F, 1.25F, -3.5F, 8.5F, 2.5F, 1.0F).texOffs(26, 24).addBox(-4.25F, 3.75F, -3.375F, 8.5F, 2.5F, 1.0F).texOffs(46, 24).addBox(-4.25F, 6.25F, -3.25F, 8.5F, 2.5F, 1.0F)
                        .texOffs(66, 24).addBox(-1.5F, 1.125F, -3.875F, 3.0F, 2.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(74, 24)
                        .addBox(-4.25F, 1.25F, 2.4375F, 8.5F, 9.0F, 0.5F, EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(93, 24)
                        .addBox(-4.625F, 2.0625F, -2.375F, 0.5F, 8.0F, 4.5F).texOffs(104, 24).addBox(4.125F, 2.0625F, -2.375F, 0.5F, 8.0F, 4.5F).texOffs(115, 24)
                        .addBox(0.25F, 1.5F, 3.125F, 3.5F, 5.0F, 1.5F).texOffs(1, 38).addBox(-3.75F, 3.5F, 3.25F, 2.0F, 6.0F, 2.0F).texOffs(10, 38).addBox(-3.875F, 6.0F, 3.125F, 2.5F, 1.0F, 2.5F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(21, 38).addBox(-3.5F, -2.5F, -2.75F, 5.0F, 2.5F, 5.5F).texOffs(43, 38).addBox(-3.625F, 0.125F, -2.875F, 5.5F, 1.5F, 5.5F).texOffs(66, 38)
                        .addBox(-3.625F, 2.125F, -2.75F, 5.5F, 1.5F, 5.5F).texOffs(89, 38).addBox(-3.25F, 5.625F, -2.625F, 4.5F, 4.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(1, 48)
                        .addBox(-3.375F, 8.875F, -2.875F, 4.5F, 1.0F, 5.5F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(77, 48).addBox(-1.5F, -2.5F, -2.75F, 5.0F, 2.5F, 5.5F).texOffs(99, 48).addBox(-1.625F, 0.125F, -2.875F, 5.5F, 1.5F, 5.5F).texOffs(1, 59)
                        .addBox(-1.625F, 2.125F, -2.75F, 5.5F, 1.5F, 5.5F).texOffs(24, 59).addBox(-1.25F, 5.625F, -2.625F, 4.5F, 4.0F, 5.0F, new CubeDeformation(0.03125F)).texOffs(44, 59)
                        .addBox(-1.375F, 8.875F, -2.875F, 4.5F, 1.0F, 5.5F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(22, 48).addBox(-1.85F, 0.25F, -3.125F, 3.5F, 2.5F, 0.5F).texOffs(31, 48).addBox(-1.85F, 2.75F, -3.0F, 3.5F, 2.5F, 0.5F).texOffs(40, 48)
                        .addBox(-1.85F, 5.25F, -2.875F, 3.5F, 2.5F, 0.5F).texOffs(49, 48).addBox(-2.725F, 0.5F, -2.25F, 0.5F, 5.0F, 4.5F, new CubeDeformation(0.03125F)).texOffs(60, 48)
                        .addBox(-1.85F, 0.75F, 2.5F, 3.5F, 4.5F, 0.5F, EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(69, 48).addBox(-1.725F, 8.0F,
                                -2.5F, 3.0F, 2.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(65, 59).addBox(-1.65F, 0.25F, -3.125F, 3.5F, 2.5F, 0.5F).texOffs(74, 59).addBox(-1.65F, 2.75F, -3.0F, 3.5F, 2.5F, 0.5F).texOffs(83, 59)
                        .addBox(-1.65F, 5.25F, -2.875F, 3.5F, 2.5F, 0.5F).texOffs(92, 59).addBox(2.225F, 0.5F, -2.25F, 0.5F, 5.0F, 4.5F, new CubeDeformation(0.03125F)).texOffs(103, 59)
                        .addBox(-1.65F, 0.75F, 2.5F, 3.5F, 4.5F, 0.5F, EnumSet.of(Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)).texOffs(112, 59)
                        .addBox(-1.525F, 8.0F, -2.5F, 3.0F, 2.0F, 0.5F, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.WEST, Direction.DOWN, Direction.UP)),
                PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("mask_0", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("mask_1", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("mask_2", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("goggles", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }
}
