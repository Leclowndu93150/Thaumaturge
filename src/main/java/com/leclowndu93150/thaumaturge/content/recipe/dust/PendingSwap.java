package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record PendingSwap(BlockPos pos, BlockState originalState, Optional<ItemStack> resultStack, Optional<BlockState> resultState, int ticksRemaining) {
    public static final Codec<PendingSwap> CODEC = RecordCodecBuilder
            .create(i -> i.group(BlockPos.CODEC.fieldOf("pos").forGetter(PendingSwap::pos), BlockState.CODEC.fieldOf("original_state").forGetter(PendingSwap::originalState),
                    ItemStack.CODEC.optionalFieldOf("result_stack").forGetter(PendingSwap::resultStack), BlockState.CODEC.optionalFieldOf("result_state").forGetter(PendingSwap::resultState),
                    Codec.INT.fieldOf("ticks_remaining").forGetter(PendingSwap::ticksRemaining)).apply(i, PendingSwap::new));

    public PendingSwap withTicks(int ticks) {
        return new PendingSwap(this.pos, this.originalState, this.resultStack, this.resultState, ticks);
    }

    public static PendingSwap dropItem(BlockPos pos, BlockState original, ItemStack stack, int delay) {
        return new PendingSwap(pos.immutable(), original, Optional.of(stack.copy()), Optional.empty(), delay);
    }

    public static PendingSwap placeBlock(BlockPos pos, BlockState original, BlockState placed, int delay) {
        return new PendingSwap(pos.immutable(), original, Optional.empty(), Optional.of(placed), delay);
    }

    public static PendingSwap clearOnly(BlockPos pos, BlockState original, int delay) {
        return new PendingSwap(pos.immutable(), original, Optional.empty(), Optional.empty(), delay);
    }

    public @Nullable ItemStack stackResult() {
        return this.resultStack.orElse(null);
    }

    public @Nullable BlockState stateResult() {
        return this.resultState.orElse(null);
    }
}
