package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** One of the four shared output ports of an Advanced Alchemical Furnace. */
public final class BlockEntityAdvancedAlchemicalFurnaceNozzle extends BlockEntity implements IEssentiaTransport {
    public BlockEntityAdvancedAlchemicalFurnaceNozzle(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get(), pos, state);
    }

    @Nullable
    BlockEntityAdvancedAlchemicalFurnace controller() {
        if (level == null) return null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
                return furnace;
            }
        }
        return null;
    }

    private @Nullable BlockEntityAdvancedAlchemicalFurnace furnace() {
        return controller();
    }

    private @Nullable Direction outputFace() {
        if (level == null) return null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(worldPosition.relative(direction))
                    instanceof BlockEntityAdvancedAlchemicalFurnace) {
                return direction.getOpposite();
            }
        }
        return null;
    }

    @Override
    public boolean isConnectable(Direction face) {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        return face == outputFace() && furnace != null && furnace.assembled();
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
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        return canOutputTo(face) && furnace != null ? furnace.takeEssentiaFromNozzle(aspect, amount) : 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        return furnace == null ? null : furnace.firstEssentia();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        return furnace == null ? 0 : furnace.aspects().totalAmount();
    }
}
