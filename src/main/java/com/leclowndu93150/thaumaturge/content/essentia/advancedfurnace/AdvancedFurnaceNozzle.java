package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public final class AdvancedFurnaceNozzle implements IEssentiaTransport {
    private final BlockEntityAdvancedAlchemicalFurnace furnace;
    private final Direction outputFace;

    AdvancedFurnaceNozzle(BlockEntityAdvancedAlchemicalFurnace furnace, Direction outputFace) {
        this.furnace = furnace;
        this.outputFace = outputFace;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == outputFace && furnace.isAssembled();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return canOutputTo(face) ? furnace.takeEssentia(aspect, amount) : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (!simulate) {
            return takeEssentia(aspect, amount, face);
        }
        if (!canOutputTo(face) || amount <= 0) {
            return 0;
        }
        return Math.min(amount, furnace.aspects().amountOf(aspect));
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return furnace.randomEssentia();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return furnace.aspects().totalAmount();
    }
}
