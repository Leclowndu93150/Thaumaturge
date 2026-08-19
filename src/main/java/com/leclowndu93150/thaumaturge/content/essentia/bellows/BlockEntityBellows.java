package com.leclowndu93150.thaumaturge.content.essentia.bellows;

import com.leclowndu93150.thaumaturge.content.essentia.IBellowsPower;
import com.leclowndu93150.thaumaturge.mixin.world.level.block.entity.AbstractFurnaceBlockEntityAccessor;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityBellows extends BlockEntity implements IBellowsPower {

    public float inflation = 1.0F;
    boolean direction = false;
    boolean firstRun = true;
    public int delay = 0;

    public BlockEntityBellows(BlockPos worldPosition, BlockState blockState) {
        super(TCBlockEntities.BELLOWS.get(), worldPosition, blockState);
    }

    public void setCookTime(AbstractFurnaceBlockEntity ent, int hit) {
        ((AbstractFurnaceBlockEntityAccessor) ent).thaumaturge$setCookTime(hit);
    }

    public int getCookTime(AbstractFurnaceBlockEntity ent) {
        return ((AbstractFurnaceBlockEntityAccessor) ent).thaumaturge$getCookTime();
    }

    public int getTotalCookTime(AbstractFurnaceBlockEntity ent) {
        return ((AbstractFurnaceBlockEntityAccessor) ent).thaumaturge$getCookTimeTotal();
    }

    public static void staticTick(Level world, BlockPos pos, BlockState state, BlockEntityBellows blockEntity) {
        blockEntity.tick();
    }

    private void tick() {
        if (level == null)
            return;
        if (level.isClientSide()) {
            if (getBlockState().getValue(BlockBellows.ENABLED)) {
                if (this.firstRun) {
                    this.inflation = 0.35F + this.level.getRandom().nextFloat() * 0.55F;
                }

                this.firstRun = false;
                if (this.inflation > 0.35F && !this.direction) {
                    this.inflation -= 0.075F;
                }

                if (this.inflation <= 0.35F && !this.direction) {
                    this.direction = true;
                }

                if (this.inflation < 1.0F && this.direction) {
                    this.inflation += 0.025F;
                }

                if (this.inflation >= 1.0F && this.direction) {
                    this.direction = false;
                    this.level.playLocalSound(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.01F,
                            0.5F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F, false);
                }
            }
        } else if (getBlockState().getValue(BlockBellows.ENABLED)) {
            this.delay++;
            if (this.delay >= 2) {
                this.delay = 0;
                BlockEntity tile = level.getBlockEntity(getBlockPos().relative(getBlockState().getValue(BlockBellows.FACING)));
                if (tile != null && tile instanceof AbstractFurnaceBlockEntity) {
                    AbstractFurnaceBlockEntity tf = (AbstractFurnaceBlockEntity) tile;
                    int ct = this.getCookTime(tf);
                    if (ct > 0 && ct < this.getTotalCookTime(tf) - 5) {
                        this.setCookTime(tf, ct + 1);
                    }
                }
            }
        }
    }

    @Override
    public Direction bellowsFacing() {
        return getBlockState().getValue(BlockBellows.FACING);
    }

    @Override
    public boolean bellowsEnabled() {
        return getBlockState().getValue(BlockBellows.ENABLED);
    }
}
