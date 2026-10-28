package com.leclowndu93150.thaumaturge.content.taint.flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FluxGooContactPortTest {
    @Test
    void clientContactKeepsDragButDoesNotApplyVisExhaustion() {
        Level level = mock(Level.class);
        LivingEntity living = mock(LivingEntity.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        when(level.isClientSide()).thenReturn(true);
        when(level.getFluidState(pos)).thenReturn(FluxGooFluid.gooBlockState(4).getFluidState());
        when(living.blockPosition()).thenReturn(pos);
        when(living.getDeltaMovement()).thenReturn(new Vec3(2.0, 4.0, 6.0));

        FluxGooFluid.applyEntityInside(level, pos, living);

        verify(living).setDeltaMovement(1.5, 4.0, 4.5);
        verify(living, never()).addEffect(any(MobEffectInstance.class));
    }

    @Test
    void serverLivingContactAppliesVisExhaustionAtSourceDurationAndAmplifier() {
        Level level = mock(Level.class);
        LivingEntity living = mock(LivingEntity.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        when(level.isClientSide()).thenReturn(false);
        when(level.getFluidState(pos)).thenReturn(FluxGooFluid.gooBlockState(5).getFluidState());
        when(living.blockPosition()).thenReturn(pos);
        when(living.getDeltaMovement()).thenReturn(Vec3.ZERO);

        FluxGooFluid.applyEntityInside(level, pos, living);

        verify(living).setDeltaMovement(0.0, 0.0, 0.0);
        ArgumentCaptor<MobEffectInstance> effect = ArgumentCaptor.forClass(MobEffectInstance.class);
        verify(living).addEffect(effect.capture());
        assertEquals(TTMobEffects.VIS_EXHAUST, effect.getValue().getEffect());
        assertEquals(600, effect.getValue().getDuration());
        assertEquals(1, effect.getValue().getAmplifier());
    }

    @Test
    void emptyFluidStateHasNoContactEffectsOrDrag() {
        Level level = mock(Level.class);
        LivingEntity living = mock(LivingEntity.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        when(level.getFluidState(pos)).thenReturn(Fluids.EMPTY.defaultFluidState());

        FluxGooFluid.applyEntityInside(level, pos, living);

        verify(living, never()).getDeltaMovement();
        verify(living, never()).setDeltaMovement(anyDouble(), anyDouble(), anyDouble());
        verify(living, never()).addEffect(any(MobEffectInstance.class));
    }

    @Test
    void eligibleServerSlimeFeedWritesPositiveRemainderWithoutScheduling() {
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        ThaumicSlime slime = mock(ThaumicSlime.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        FluidState amountFour = FluxGooFluid.gooBlockState(4).getFluidState();
        FluidState amountThree = FluxGooFluid.gooBlockState(3).getFluidState();
        when(level.isClientSide()).thenReturn(false);
        when(level.getFluidState(pos)).thenReturn(amountFour, amountThree);
        when(level.getRandom()).thenReturn(random);
        when(random.nextBoolean()).thenReturn(true);
        when(slime.getSize()).thenReturn(1);

        FluxGooFluid.applyEntityInside(level, pos, slime);

        verify(slime).setSize(2, true);
        verify(level).setBlock(pos, FluxGooFluid.gooBlockState(3), Block.UPDATE_CLIENTS);
        verify(level, never()).scheduleTick(eq(pos), any(Fluid.class), anyInt());
    }

    @Test
    void ineligibleSlimeDoesNotRollOrConsumeGoo() {
        ServerLevel level = mock(ServerLevel.class);
        ThaumicSlime slime = mock(ThaumicSlime.class);
        BlockPos pos = new BlockPos(1, 2, 3);
        when(level.getFluidState(pos)).thenReturn(FluxGooFluid.gooBlockState(4).getFluidState());
        when(slime.getSize()).thenReturn(3);

        FluxGooFluid.applyEntityInside(level, pos, slime);

        verify(level, never()).getRandom();
        verify(level, never()).setBlock(eq(pos), any(BlockState.class), anyInt());
        verify(level, never()).scheduleTick(eq(pos), any(Fluid.class), anyInt());
        verify(slime, never()).setSize(anyInt(), anyBoolean());
    }
}
