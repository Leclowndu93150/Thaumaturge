package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityThaumatoriumTop extends BlockEntity implements IEssentiaTransport {

    public BlockEntityThaumatoriumTop(BlockPos pos, BlockState state) {
        super(TCBlockEntities.THAUMATORIUM_TOP.get(), pos, state);
    }

    private @Nullable BlockEntityThaumatorium base() {
        return level != null && level.getBlockEntity(getBlockPos().below()) instanceof BlockEntityThaumatorium machine ? machine : null;
    }

    @Override
    public boolean isConnectable(Direction face) {
        BlockEntityThaumatorium base = base();
        return base != null && face != Direction.DOWN && base.isConnectable(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        BlockEntityThaumatorium base = base();
        return base != null ? base.getSuctionType(face) : null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        BlockEntityThaumatorium base = base();
        return base != null ? base.getSuctionAmount(face) : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        BlockEntityThaumatorium base = base();
        return base != null && canInputFrom(face) ? base.addEssentia(aspect, amount, face) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
