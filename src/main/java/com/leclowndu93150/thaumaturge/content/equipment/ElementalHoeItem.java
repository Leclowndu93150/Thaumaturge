package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ElementalHoeItem extends HoeItem {
    private static final int TILL_RADIUS = 1;
    private static final int GRID_SIDE = TILL_RADIUS * 2 + 1;
    private static final int GRID_CELLS = GRID_SIDE * GRID_SIDE;
    private static final int GRID_CENTRE_INDEX = GRID_CELLS / 2;
    private static final int GROWTH_WEAR = 3;
    private static final int LEVEL_EVENT_BONE_MEAL = 1505;
    private static final int BONE_MEAL_PARTICLES = 15;
    private static final double CELL_CENTER = 0.5;
    private static final double PUFF_HEIGHT = 1.05;
    private static final float PUFF_RED = 0.56F;
    private static final float PUFF_GREEN = 0.33F;
    private static final float PUFF_BLUE = 0.19F;

    public ElementalHoeItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
        super(material, attackDamageBaseline, attackSpeedBaseline, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isSecondaryUseActive()) {
            return super.useOn(context);
        }
        if (tillSquare(context)) {
            return InteractionResult.SUCCESS;
        }
        return growCentre(context);
    }

    private boolean tillSquare(UseOnContext context) {
        BlockPos centre = context.getClickedPos();
        ItemStack hoe = context.getItemInHand();
        boolean tilledAny = false;
        for (int index = 0; index < GRID_CELLS && !hoe.isEmpty(); index++) {
            BlockPos cell = centre.offset(index / GRID_SIDE - TILL_RADIUS, 0, index % GRID_SIDE - TILL_RADIUS);
            tilledAny |= tillCell(context, cell, index == GRID_CENTRE_INDEX);
        }
        return tilledAny;
    }

    private static InteractionResult growCentre(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            if (player != null) {
                context.getItemInHand().hurtAndBreak(GROWTH_WEAR, player, context.getHand().asEquipmentSlot());
            }
            level.levelEvent(LEVEL_EVENT_BONE_MEAL, pos, BONE_MEAL_PARTICLES);
        }
        return InteractionResult.SUCCESS;
    }

    private boolean tillCell(UseOnContext context, BlockPos cell, boolean centre) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(cell), context.getClickedFace(), cell, false);
        UseOnContext shifted = new UseOnContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), hit);
        if (!super.useOn(shifted).consumesAction()) {
            return false;
        }
        if (context.getLevel() instanceof ServerLevel server) {
            broadcastPuff(server, cell, centre);
        }
        return true;
    }

    private static void broadcastPuff(ServerLevel level, BlockPos cell, boolean centre) {
        Vec3 top = new Vec3(cell.getX() + CELL_CENTER, cell.getY() + PUFF_HEIGHT, cell.getZ() + CELL_CENTER);
        Effects.Bamf puff = Effects.bamf(level, top).color(PUFF_RED, PUFF_GREEN, PUFF_BLUE).side(Direction.UP);
        if (centre) {
            puff.withSound();
        }
        puff.send();
    }
}
