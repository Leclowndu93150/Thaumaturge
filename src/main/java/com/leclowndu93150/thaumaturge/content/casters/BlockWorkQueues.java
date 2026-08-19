package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockWorkQueues {
    private final List<BreakerTask> breakers = new ArrayList<>();
    private final List<SwapperTask> swappers = new ArrayList<>();

    public List<BreakerTask> breakers() {
        return breakers;
    }

    public List<SwapperTask> swappers() {
        return swappers;
    }

    public record SwapContext(ServerLevel level, Player player, BlockPos pos) {
    }

    public record BreakerTask(BlockPos pos, BlockState source, UUID playerId, boolean fx, boolean silk, int fortune, float strength, float durability, float durabilityMax, int delay, float visCost) {

        BreakerTask withDelay(int delay) {
            return new BreakerTask(pos, source, playerId, fx, silk, fortune, strength, durability, durabilityMax, delay, visCost);
        }

        BreakerTask withDurability(float durability) {
            return new BreakerTask(pos, source, playerId, fx, silk, fortune, strength, durability, durabilityMax, delay, visCost);
        }

        public static final class Builder {
            private final BlockPos pos;
            private final BlockState source;
            private final UUID playerId;
            private boolean fx;
            private boolean silk;
            private int fortune;
            private float strength = 1.0F;
            private float durability;
            private float durabilityMax = 1.0F;
            private int delay;
            private float visCost;

            Builder(BlockPos pos, BlockState source, Player player) {
                this.pos = pos.immutable();
                this.source = source;
                this.playerId = player.getUUID();
            }

            public Builder showFx() {
                this.fx = true;
                return this;
            }

            public Builder silkTouch(boolean silk) {
                this.silk = silk;
                return this;
            }

            public Builder fortune(int fortune) {
                this.fortune = fortune;
                return this;
            }

            public Builder strength(float strength) {
                this.strength = strength;
                return this;
            }

            public Builder durability(float durability) {
                this.durability = durability;
                this.durabilityMax = durability;
                return this;
            }

            public Builder delay(int ticks) {
                this.delay = ticks;
                return this;
            }

            public Builder visCost(float visCost) {
                this.visCost = visCost;
                return this;
            }

            public void queue(ServerLevel level) {
                level.getData(TCAttachments.BLOCK_WORK_QUEUES).breakers().add(new BreakerTask(pos, source, playerId, fx, silk, fortune, strength, durability, durabilityMax, delay, visCost));
            }
        }
    }

    public record SwapperTask(BlockPos pos, @Nullable BlockState source, @Nullable BlockState target, boolean consumeTarget, int lifespan, UUID playerId, boolean fx, boolean fancy, int color,
            boolean pickup, boolean silk, int fortune, Predicate<SwapContext> allowSwap, float visCost) {

        SwapperTask spreadTo(BlockPos neighbour) {
            return new SwapperTask(neighbour, source, target, consumeTarget, lifespan - 1, playerId, fx, fancy, color, pickup, silk, fortune, allowSwap, visCost);
        }

        public static final class Builder {
            private final BlockPos pos;
            private final @Nullable BlockState source;
            private final @Nullable BlockState target;
            private final UUID playerId;
            private boolean consumeTarget;
            private int lifespan;
            private boolean fx;
            private boolean fancy;
            private int color = 0xFFFFFF;
            private boolean pickup;
            private boolean silk;
            private int fortune;
            private Predicate<SwapContext> allowSwap = context -> true;
            private float visCost;

            Builder(BlockPos pos, @Nullable BlockState source, @Nullable BlockState target, Player player) {
                this.pos = pos.immutable();
                this.source = source;
                this.target = target;
                this.playerId = player.getUUID();
            }

            public Builder consumeTarget() {
                this.consumeTarget = true;
                return this;
            }

            public Builder lifespan(int lifespan) {
                this.lifespan = lifespan;
                return this;
            }

            public Builder showFx(int color, boolean fancy) {
                this.fx = true;
                this.color = color;
                this.fancy = fancy;
                return this;
            }

            public Builder pickupDrops() {
                this.pickup = true;
                return this;
            }

            public Builder silkTouch(boolean silk) {
                this.silk = silk;
                return this;
            }

            public Builder fortune(int fortune) {
                this.fortune = fortune;
                return this;
            }

            public Builder allowSwap(Predicate<SwapContext> allowSwap) {
                this.allowSwap = allowSwap;
                return this;
            }

            public Builder visCost(float visCost) {
                this.visCost = visCost;
                return this;
            }

            public void queue(ServerLevel level) {
                level.getData(TCAttachments.BLOCK_WORK_QUEUES).swappers()
                        .add(new SwapperTask(pos, source, target, consumeTarget, lifespan, playerId, fx, fancy, color, pickup, silk, fortune, allowSwap, visCost));
            }
        }
    }
}
