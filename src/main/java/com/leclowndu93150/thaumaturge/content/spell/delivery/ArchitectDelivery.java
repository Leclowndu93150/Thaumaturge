package com.leclowndu93150.thaumaturge.content.spell.delivery;

import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
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

public record ArchitectDelivery(double range) implements SpellBehavior, IArchitect {
    public static final MapCodec<ArchitectDelivery> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    Codec.doubleRange(1.0, 64.0).optionalFieldOf("range", 16.0).forGetter(ArchitectDelivery::range))
            .apply(i, ArchitectDelivery::new));

    private static final String METHOD = "method";
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
            List<BlockPos> found = collect(casterStack, ctx.level(), hit.getBlockPos(), hit.getDirection(), method);
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
    public @Nullable HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        Vec3 eyes = caster.getEyePosition();
        return SpellRayTrace.clipBlocks(
                level, caster, eyes, eyes.add(caster.getLookAngle().scale(range)));
    }

    @Override
    public boolean replacesBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        return collect(stack, level, pos, side, methodOf(stack, level));
    }

    @Override
    public boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
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
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return FULL;
        }
        Spell spell = Spells.spellOf(caster.getFocusStack(casterStack));
        if (spell == null) {
            return FULL;
        }
        for (SpellNode node : spell.nodes()) {
            Optional<SpellPart> part = Spells.part(level.registryAccess(), node.part());
            if (part.isPresent() && part.get().behavior() instanceof ArchitectDelivery) {
                return part.get()
                        .setting(METHOD)
                        .map(spec -> spec.clamp(node.settings().getOrDefault(METHOD, spec.defaultValue())))
                        .orElse(FULL);
            }
        }
        return FULL;
    }

    private static List<BlockPos> collect(ItemStack stack, Level level, BlockPos origin, Direction side, int method) {
        if (stack.isEmpty()) {
            return new ArrayList<>();
        }
        int ax = CasterManager.getAreaX(stack);
        int ay = CasterManager.getAreaY(stack);
        int az = CasterManager.getAreaZ(stack);
        return method == FULL ? volume(level, origin, side, ax, ay, az) : surface(level, origin, side, ax, az);
    }

    private static List<BlockPos> volume(Level level, BlockPos origin, Direction side, int ax, int ay, int az) {
        BlockPos min = new BlockPos(
                origin.getX() - ax - ax * side.getStepX(),
                origin.getY() - ay - ay * side.getStepY(),
                origin.getZ() - az - az * side.getStepZ());
        BlockPos max = new BlockPos(
                origin.getX() + ax - ax * side.getStepX(),
                origin.getY() + ay - ay * side.getStepY(),
                origin.getZ() + az - az * side.getStepZ());
        return flood(
                origin,
                pos -> within(pos, min, max),
                pos -> !level.getBlockState(pos).isAir(),
                side,
                false);
    }

    private static List<BlockPos> surface(Level level, BlockPos origin, Direction side, int ax, int az) {
        BlockState match = level.getBlockState(origin);
        List<Direction.Axis> plane = planeAxes(side);
        Direction.Axis first = plane.get(0);
        Direction.Axis second = plane.get(1);
        return flood(
                origin,
                pos -> Math.abs(pos.get(first) - origin.get(first)) <= ax
                        && Math.abs(pos.get(second) - origin.get(second)) <= az,
                pos -> !match.isAir() && level.getBlockState(pos) == match && exposed(level, pos),
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

    private static boolean within(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() >= min.getX()
                && pos.getX() <= max.getX()
                && pos.getY() >= min.getY()
                && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ()
                && pos.getZ() <= max.getZ();
    }

    private static boolean exposed(Level level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            if (!level.getBlockState(pos.relative(face)).isSolidRender(level, pos.relative(face))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean standalone() {
        return false;
    }
}
