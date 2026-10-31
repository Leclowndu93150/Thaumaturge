package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
    private static final int STREAK_INTERVAL = 40;
    private static final int STREAK_VARIANTS = 8;
    private static final float STREAK_SCALE = 0.12F;
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
        if (siphon.counter % WORK_INTERVAL != 0) {
            return;
        }
        if (state.getValue(BlockStateProperties.ENABLED)
                && siphon.progress < PROGRESS_REQUIRED
                && siphon.hasOutputRoom()) {
            List<EntityFluxRift> drained = siphon.getValidRifts(level, pos);
            for (EntityFluxRift rift : drained) {
                double d = Math.sqrt(rift.currentSize());
                siphon.progress += (int) d;
                rift.adjustStability((float) (-d / 15.0));
                if (level.getRandom().nextInt(SHRINK_CHANCE) == 0) {
                    rift.resize(rift.currentSize() - 1);
                }
            }
            if (level instanceof ServerLevel server && siphon.counter % STREAK_INTERVAL == 0) {
                Vec3 top = LevelSpaces.toWorld(server, new Vec3(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5));
                drained.forEach(rift -> showStreak(server, rift, top));
            }
        }
        siphon.produce();
    }

    private void produce() {
        boolean changed = false;
        while (progress >= PROGRESS_REQUIRED && hasOutputRoom()) {
            progress -= PROGRESS_REQUIRED;
            ItemStack current = output.getStackInSlot(0).copy();
            if (current.isEmpty()) {
                output.setStackInSlot(0, new ItemStack(TTItems.VOID_SEED.get()).copyWithCount(1));
            } else {
                output.setStackInSlot(0, current.copyWithCount(current.getCount() + 1));
            }
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private boolean hasOutputRoom() {
        ItemStack stack = output.getStackInSlot(0).copy();
        return stack.isEmpty() || stack.is(TTItems.VOID_SEED.get()) && stack.getCount() < stack.getMaxStackSize();
    }

    private static void showStreak(ServerLevel level, EntityFluxRift rift, Vec3 target) {
        RandomSource random = level.getRandom();
        Vec3 from = rift.position();
        if (!rift.outline.isEmpty()) {
            from = from.add(rift.outline.get(random.nextInt(rift.outline.size())));
        }
        Effects.voidStream(level, from)
                .to(target)
                .seed(random.nextInt(STREAK_VARIANTS))
                .scale(STREAK_SCALE)
                .send();
    }

    private List<EntityFluxRift> getValidRifts(Level level, BlockPos pos) {
        List<EntityFluxRift> found = new ArrayList<>();
        AABB box = LevelSpaces.worldBox(level, pos, new AABB(pos).inflate(RIFT_RANGE));
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, box)) {
            if (rift.isRemoved() || rift.currentSize() < 2) {
                continue;
            }
            Vec3 from = LevelSpaces.toWorld(level, new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5));
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
