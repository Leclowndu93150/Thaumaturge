package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public final class ElementalShovelItem extends Item implements IArchitect {
    private static final int MODE_PARALLEL = 0;
    private static final int MODE_VERTICAL = 1;
    private static final int MODE_COUNT = 3;
    private static final int SPAN = 1;
    private static final float PUFF_RED = 128.0F / 255.0F;
    private static final float PUFF_GREEN = 51.0F / 255.0F;
    private static final float PUFF_BLUE = 0.0F;
    private static final float PLACE_VOLUME = 0.6F;
    private static final float PITCH_MIN = 0.9F;
    private static final float PITCH_SPREAD = 0.2F;
    private static final double FALLBACK_REACH = 4.5;

    public ElementalShovelItem(Item.Properties properties) {
        super(properties);
    }

    public static int getOrientation(ItemStack stack) {
        return stack.getOrDefault(TTDataComponents.TOOL_ORIENTATION.get(), MODE_PARALLEL) % MODE_COUNT;
    }

    public static void cycleOrientation(ItemStack stack) {
        stack.set(TTDataComponents.TOOL_ORIENTATION.get(), (getOrientation(stack) + 1) % MODE_COUNT);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockState source = level.getBlockState(context.getClickedPos());
        if (player == null || source.hasBlockEntity()) {
            return InteractionResult.FAIL;
        }
        List<BlockPos> cells =
                cells(context.getItemInHand(), level, context.getClickedPos(), context.getClickedFace(), player);
        if (!(level instanceof ServerLevel server)) {
            return cells.stream().anyMatch(cell -> fits(level, cell, source))
                    ? InteractionResult.SUCCESS
                    : InteractionResult.FAIL;
        }
        ItemStack shovel = context.getItemInHand();
        int placed = 0;
        for (BlockPos cell : cells) {
            if (!fits(level, cell, source) || !mayPlace(server, player, cell, context.getClickedFace(), shovel)) {
                continue;
            }
            BlockState fill = pay(player, source);
            if (fill == null) {
                continue;
            }
            level.setBlock(cell, fill, Block.UPDATE_ALL);
            placed++;
            level.playSound(
                    null,
                    cell,
                    source.getSoundType(level, cell, player).getBreakSound(),
                    SoundSource.BLOCKS,
                    PLACE_VOLUME,
                    PITCH_MIN + level.getRandom().nextFloat() * PITCH_SPREAD);
            Effects.bamf(server, Vec3.atCenterOf(cell))
                    .color(PUFF_RED, PUFF_GREEN, PUFF_BLUE)
                    .side(context.getClickedFace())
                    .send();
            player.swing(context.getHand(), true);
            shovel.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            if (shovel.isEmpty()) {
                break;
            }
        }
        return placed > 0 ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    private static boolean mayPlace(ServerLevel level, Player player, BlockPos cell, Direction face, ItemStack shovel) {
        return player.mayUseItemAt(cell, face, shovel)
                && player.mayInteract(level, cell)
                && !EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, cell), face);
    }

    private static boolean fits(Level level, BlockPos cell, BlockState source) {
        return level.getBlockState(cell).canBeReplaced() && source.canSurvive(level, cell);
    }

    private static @Nullable BlockState pay(Player player, BlockState source) {
        if (player.hasInfiniteMaterials()) {
            return source;
        }
        if (take(player, source.getBlock().asItem())) {
            return source;
        }
        if (source.is(Blocks.GRASS_BLOCK) && take(player, Items.DIRT)) {
            return Blocks.DIRT.defaultBlockState();
        }
        return null;
    }

    private static boolean take(Player player, Item item) {
        if (item == Items.AIR) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static List<BlockPos> cells(ItemStack stack, Level level, BlockPos clicked, Direction face, Player player) {
        Direction.Axis[] axes = axes(getOrientation(stack), face, player.getDirection());
        BlockPos centre = clicked.relative(face);
        List<BlockPos> cells = new ArrayList<>();
        for (int first = -SPAN; first <= SPAN; first++) {
            for (int second = -SPAN; second <= SPAN; second++) {
                cells.add(centre.relative(axes[0], first).relative(axes[1], second));
            }
        }
        return cells;
    }

    private static Direction.Axis[] axes(int mode, Direction face, Direction facing) {
        Direction.Axis faceAxis = face.getAxis();
        if (mode == MODE_PARALLEL) {
            return switch (faceAxis) {
                case Y -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
                case Z -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Y};
                case X -> new Direction.Axis[] {Direction.Axis.Z, Direction.Axis.Y};
            };
        }
        if (faceAxis == Direction.Axis.Y) {
            Direction.Axis across = facing.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
            return new Direction.Axis[] {across, Direction.Axis.Y};
        }
        if (mode == MODE_VERTICAL) {
            return new Direction.Axis[] {faceAxis, Direction.Axis.Y};
        }
        return new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
    }

    @Override
    public List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        BlockState source = level.getBlockState(pos);
        return cells(stack, level, pos, side, player).stream()
                .filter(cell -> fits(level, cell, source))
                .toList();
    }

    @Override
    public @Nullable HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        double reach = caster instanceof Player player ? player.blockInteractionRange() : FALLBACK_REACH;
        return caster.pick(reach, 1.0F, false);
    }

    @Override
    public boolean replacesBlockHighlight(ItemStack stack) {
        return true;
    }

    @Override
    public boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        return false;
    }
}
