package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ElementalShovelItem extends Item implements IArchitect {
    private static final int BAMF_COLOR = 8401408;
    private static final int ORIENTATION_COUNT = 3;

    public ElementalShovelItem(Properties properties) {
        super(properties);
    }

    public static int getOrientation(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.TOOL_ORIENTATION.get(), 0);
    }

    public static void cycleOrientation(ItemStack stack) {
        stack.set(TCDataComponents.TOOL_ORIENTATION.get(), (getOrientation(stack) + 1) % ORIENTATION_COUNT);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        if (player == null || level.getBlockEntity(pos) != null) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState bs = level.getBlockState(pos);
        int orientation = getOrientation(context.getItemInHand());
        boolean placed = false;
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
                BlockPos p2 = pos.relative(side).offset(offsetFor(orientation, side, player.getYRot(), aa, bb));
                if (!level.getBlockState(p2).canBeReplaced() || !bs.canSurvive(level, p2)) {
                    continue;
                }
                if (player.hasInfiniteMaterials() || consumeItem(player, new ItemStack(bs.getBlock()))) {
                    placeBlock(level, player, context, p2, bs, side);
                    placed = true;
                } else if (bs.is(Blocks.GRASS_BLOCK) && (player.hasInfiniteMaterials() || consumeItem(player, new ItemStack(Blocks.DIRT)))) {
                    placeBlock(level, player, context, p2, Blocks.DIRT.defaultBlockState(), side);
                    placed = true;
                    if (context.getItemInHand().isEmpty()) {
                        break;
                    }
                }
            }
        }
        return placed ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private static void placeBlock(Level level, Player player, UseOnContext context, BlockPos p2, BlockState state, Direction side) {
        level.playSound(null, p2, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.6F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.setBlockAndUpdate(p2, state);
        context.getItemInHand().hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
        if (level instanceof ServerLevel serverLevel) {
            Effects.bamf(serverLevel, p2).color(((BAMF_COLOR >> 16) & 0xFF) / 255.0F, ((BAMF_COLOR >> 8) & 0xFF) / 255.0F, (BAMF_COLOR & 0xFF) / 255.0F).side(side).send();
        }
        player.swing(context.getHand());
    }

    private static boolean consumeItem(Player player, ItemStack wanted) {
        if (wanted.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, wanted)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
                return true;
            }
        }
        return false;
    }

    private static BlockPos offsetFor(int orientation, Direction side, float yRot, int aa, int bb) {
        int xx = 0;
        int yy = 0;
        int zz = 0;
        if (orientation == 1) {
            yy = bb;
            if (side.ordinal() <= 1) {
                int l = Mth.floor(yRot * 4.0F / 360.0F + 0.5) & 3;
                if (l != 0 && l != 2) {
                    zz = aa;
                } else {
                    xx = aa;
                }
            } else if (side.ordinal() <= 3) {
                zz = aa;
            } else {
                xx = aa;
            }
        } else if (orientation == 2) {
            if (side.ordinal() <= 1) {
                int l = Mth.floor(yRot * 4.0F / 360.0F + 0.5) & 3;
                yy = bb;
                if (l != 0 && l != 2) {
                    zz = aa;
                } else {
                    xx = aa;
                }
            } else {
                zz = bb;
                xx = aa;
            }
        } else if (side.ordinal() <= 1) {
            xx = aa;
            zz = bb;
        } else if (side.ordinal() <= 3) {
            xx = aa;
            yy = bb;
        } else {
            zz = aa;
            yy = bb;
        }
        return new BlockPos(xx, yy, zz);
    }

    @Override
    public List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        List<BlockPos> blocks = new ArrayList<>();
        if (!player.isShiftKeyDown()) {
            return blocks;
        }
        BlockState bs = level.getBlockState(pos);
        int orientation = getOrientation(stack);
        for (int aa = -1; aa <= 1; aa++) {
            for (int bb = -1; bb <= 1; bb++) {
                BlockPos p2 = pos.relative(side).offset(offsetFor(orientation, side, player.getYRot(), aa, bb));
                if (level.getBlockState(p2).canBeReplaced() && bs.canSurvive(level, p2)) {
                    blocks.add(p2);
                }
            }
        }
        return blocks;
    }

    @Override
    public @Nullable HitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity caster) {
        double range = caster instanceof Player player ? player.blockInteractionRange() : 4.5;
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getViewVector(1.0F).scale(range));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, caster));
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis) {
        return false;
    }
}
