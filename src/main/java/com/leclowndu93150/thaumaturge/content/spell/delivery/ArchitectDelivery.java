package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.casters.FocusCaster;
import com.leclowndu93150.thaumaturge.api.items.AreaPreviewer;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.spell.casting.CasterHands;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellRayTrace;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.registry.TTSpellBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record ArchitectDelivery(double range) implements SpellBehavior, AreaPreviewer {
    public static final MapCodec<ArchitectDelivery> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.doubleRange(1.0, 64.0).optionalFieldOf("range", 16.0).forGetter(ArchitectDelivery::range))
            .apply(i, ArchitectDelivery::new));

    private static final String METHOD = "method";
    private static final String BLOCK_TYPE = "block_type";
    private static final int FULL = 0;
    private static final int DIM_ALL = 0;
    private static final int DIM_X = 1;
    private static final int DIM_Z = 2;
    private static final int DIM_Y = 3;

    @Override
    public SpellBehaviorType<?> type() {
        return TTSpellBehaviors.ARCHITECT.get();
    }

    @Override
    public void execute(CastContext ctx, List<SpellTarget> incoming) {
        if (!(ctx.caster() instanceof Player player)) {
            return;
        }
        ItemStack casterStack = CasterHands.held(player);
        int method = ctx.setting(METHOD);
        List<SpellTarget> out = new ArrayList<>();
        for (SpellTarget origin : incoming) {
            BlockHitResult hit = SpellRayTrace.clipBlocks(
                    ctx.level(),
                    player,
                    origin.position(),
                    origin.position().add(origin.direction().scale(range)));
            if (hit.getType() != HitResult.Type.BLOCK) {
                continue;
            }
            int blockType = blockType(ctx.node(), method);
            List<BlockPos> found =
                    collect(casterStack, ctx.level(), hit.getBlockPos(), hit.getDirection(), method, blockType);
            found.sort(Comparator.comparingDouble(pos -> pos.distSqr(hit.getBlockPos())));
            for (BlockPos pos : found) {
                out.add(new SpellTarget(
                        new BlockHitResult(Vec3.atCenterOf(pos), hit.getDirection(), pos, false),
                        Vec3.atCenterOf(pos),
                        origin.direction()));
            }
        }
        ctx.proceed(out);
    }

    @Override
    public @Nullable HitResult traceAnchor(ItemStack stack, Level level, LivingEntity caster) {
        return caster.pick(range, 1.0F, false);
    }

    @Override
    public boolean hidesBlockOutline(ItemStack stack) {
        return false;
    }

    @Override
    public List<BlockPos> previewArea(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        int method = methodOf(stack, level);
        return collect(stack, level, pos, side, method, blockTypeOf(stack, level, method));
    }

    @Override
    public boolean showsAxisArrows(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        if (stack.isEmpty()) {
            return false;
        }
        int dim = CasterManager.getAreaDim(stack);
        if (methodOf(stack, level) == FULL) {
            return dim == DIM_ALL || axisDim(axis) == dim;
        }
        List<Direction.Axis> plane = planeAxes(side);
        int slot = plane.indexOf(axis);
        return slot >= 0 && (dim == DIM_ALL || dim == slot + 1);
    }

    private static int axisDim(Direction.Axis axis) {
        return switch (axis) {
            case X -> DIM_X;
            case Y -> DIM_Y;
            case Z -> DIM_Z;
        };
    }

    private static List<Direction.Axis> planeAxes(Direction side) {
        return switch (side.getAxis()) {
            case Y -> List.of(Direction.Axis.X, Direction.Axis.Z);
            case Z -> List.of(Direction.Axis.Y, Direction.Axis.X);
            case X -> List.of(Direction.Axis.Y, Direction.Axis.Z);
        };
    }

    public static int methodOf(ItemStack casterStack, Level level) {
        return planNode(casterStack, level)
                .map(node -> Math.clamp(node.settings().getOrDefault(METHOD, FULL), 0, 1))
                .orElse(FULL);
    }

    private static int blockTypeOf(ItemStack stack, Level level, int method) {
        return planNode(stack, level).map(node -> blockType(node, method)).orElse(0);
    }

    public static int blockType(SpellNode node, int method) {
        return Math.clamp(node.settings().getOrDefault(BLOCK_TYPE, method == FULL ? 0 : 1), 0, 1);
    }

    private static Optional<SpellNode> planNode(ItemStack casterStack, Level level) {
        if (!(casterStack.getItem() instanceof FocusCaster caster)) {
            return Optional.empty();
        }
        Spell spell = Spells.spellOf(caster.socketedFocus(casterStack));
        if (spell == null) {
            return Optional.empty();
        }
        for (SpellNode node : spell.nodes()) {
            Optional<SpellPart> part = Spells.part(level.registryAccess(), node.part());
            if (part.isPresent() && part.get().behavior() instanceof ArchitectDelivery) {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    private static List<BlockPos> collect(
            ItemStack stack, Level level, BlockPos origin, Direction side, int method, int blockType) {
        if (stack.isEmpty()) {
            return new ArrayList<>();
        }
        int ax = CasterManager.getAreaX(stack);
        int ay = CasterManager.getAreaY(stack);
        int az = CasterManager.getAreaZ(stack);
        SpaceFrame frame = LevelSpaces.frameOf(level, origin);
        List<BlockPos> result = method == FULL
                ? volume(level, origin, side, ax, ay, az, frame)
                : surface(level, origin, side, ax, az, frame, blockType != 0);
        if (method == FULL && blockType != 0) {
            var block = level.getBlockState(origin).getBlock();
            result.removeIf(pos -> !level.getBlockState(pos).is(block));
        }
        return result;
    }

    static List<BlockPos> volume(
            Level level, BlockPos origin, Direction side, int ax, int ay, int az, SpaceFrame frame) {
        BlockPos min = new BlockPos(
                origin.getX() - ax - ax * side.getStepX(),
                origin.getY() - ay - ay * side.getStepY(),
                origin.getZ() - az - az * side.getStepZ());
        BlockPos max = new BlockPos(
                origin.getX() + ax - ax * side.getStepX(),
                origin.getY() + ay - ay * side.getStepY(),
                origin.getZ() + az - az * side.getStepZ());
        List<BlockPos> affected = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (sameSpace(level, frame, pos) && !level.getBlockState(pos).isAir()) {
                affected.add(pos.immutable());
            }
        }
        return affected;
    }

    static List<BlockPos> surface(
            Level level, BlockPos origin, Direction side, int ax, int az, SpaceFrame frame, boolean sameType) {
        BlockState match = level.getBlockState(origin);
        List<Direction.Axis> plane = planeAxes(side);
        Direction.Axis first = plane.get(0);
        Direction.Axis second = plane.get(1);
        return flood(
                origin,
                pos -> Math.abs(pos.get(first) - origin.get(first)) <= ax
                        && Math.abs(pos.get(second) - origin.get(second)) <= az,
                pos -> !match.isAir()
                        && sameSpace(level, frame, pos)
                        && !level.getBlockState(pos).isAir()
                        && (!sameType || level.getBlockState(pos).is(match.getBlock()))
                        && exposed(level, pos, frame),
                side,
                true);
    }

    private static List<BlockPos> flood(
            BlockPos origin,
            Predicate<BlockPos> inBounds,
            Predicate<BlockPos> accepts,
            Direction side,
            boolean planar) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        Deque<BlockPos> open = new ArrayDeque<>();
        open.add(origin);
        seen.add(origin);
        while (!open.isEmpty()) {
            BlockPos pos = open.poll();
            boolean accepted = accepts.test(pos);
            if (accepted) {
                out.add(pos);
            } else if (planar) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                if (planar && direction.getAxis() == side.getAxis()) {
                    continue;
                }
                BlockPos next = pos.relative(direction);
                if (inBounds.test(next) && seen.add(next)) {
                    open.add(next);
                }
            }
        }
        return out;
    }

    private static boolean exposed(Level level, BlockPos pos, SpaceFrame frame) {
        for (Direction face : Direction.values()) {
            BlockPos neighbor = pos.relative(face);
            if (!sameSpace(level, frame, neighbor)
                    || !level.getBlockState(neighbor).isSolidRender(level, neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameSpace(Level level, SpaceFrame frame, BlockPos pos) {
        if (LevelSpaces.isWorldOnly()) {
            return true;
        }
        return frame.isWorld() ? !LevelSpaces.isPlot(level, pos) : frame.equals(LevelSpaces.frameOf(level, pos));
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
