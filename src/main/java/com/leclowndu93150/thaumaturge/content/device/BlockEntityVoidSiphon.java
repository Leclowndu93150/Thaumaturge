package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class BlockEntityVoidSiphon extends AbstractSyncedBlockEntity {
    public static final int PROGRESS_REQUIRED = 2000;
    private static final int WORK_INTERVAL = 20;
    private static final double RIFT_RANGE = 8.0;
    private static final int SHRINK_CHANCE = 33;

    private final ItemStackHandler output = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return resource.is(TTItems.VOID_SEED.get());
        }

        @Override
        protected void onContentsChanged(int index) {
            super.onContentsChanged(index);
            setChanged();
        }
    };

    private int counter;
    private int progress;

    public BlockEntityVoidSiphon(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VOID_SIPHON.get(), pos, state);
    }

    public ItemStackHandler output() {
        return output;
    }

    public int progress() {
        return progress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVoidSiphon siphon) {
        siphon.counter++;
        if (!state.getValue(BlockStateProperties.ENABLED)
                || siphon.counter % WORK_INTERVAL != 0
                || siphon.progress >= PROGRESS_REQUIRED && !siphon.hasOutputRoom()) {
            return;
        }
        if (siphon.hasOutputRoom()) {
            for (EntityFluxRift rift : siphon.getValidRifts(level, pos)) {
                double d = Math.sqrt(rift.getRiftSize());
                siphon.progress += (int) d;
                rift.setRiftStability((float) (rift.getRiftStability() - d / 15.0));
                if (level.getRandom().nextInt(SHRINK_CHANCE) == 0) {
                    rift.setRiftSize(rift.getRiftSize() - 1);
                }
            }
        }
        boolean changed = false;
        while (siphon.progress >= PROGRESS_REQUIRED && siphon.hasOutputRoom()) {
            siphon.progress -= PROGRESS_REQUIRED;
            ItemStack current = siphon.output.getStackInSlot(0).copy();
            if (current.isEmpty()) {
                siphon.output.setStackInSlot(0, new ItemStack(TTItems.VOID_SEED.get()).copyWithCount(1));
            } else {
                siphon.output.setStackInSlot(0, current.copyWithCount(current.getCount() + 1));
            }
            changed = true;
        }
        if (changed) {
            siphon.setChanged();
        }
    }

    private boolean hasOutputRoom() {
        ItemStack stack = output.getStackInSlot(0).copy();
        return stack.isEmpty() || stack.is(TTItems.VOID_SEED.get()) && stack.getCount() < stack.getMaxStackSize();
    }

    private List<EntityFluxRift> getValidRifts(Level level, BlockPos pos) {
        List<EntityFluxRift> found = new ArrayList<>();
        AABB box = new AABB(pos).inflate(RIFT_RANGE);
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, box)) {
            if (rift.isRemoved() || rift.getRiftSize() < 2) {
                continue;
            }
            Vec3 from = new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            Vec3 to = rift.position();
            from = from.add(to.subtract(from).normalize());
            HitResult hit = level.clip(new ClipContext(
                    from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == HitResult.Type.MISS) {
                found.add(rift);
            }
        }
        return found;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        progress = input.getInt("progress");
        if (input.contains("Output")) {
            output.deserializeNBT(registries, input.getCompound("Output"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output_, HolderLookup.Provider registries) {
        super.saveAdditional(output_, registries);
        output_.putInt("progress", progress);
        output_.put("Output", output.serializeNBT(registries));
    }
}
