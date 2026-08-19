package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public final class BlockEntityVoidSiphon extends BlockEntity {
    public static final int PROGRESS_REQUIRED = 2000;
    private static final int WORK_INTERVAL = 20;
    private static final double RIFT_RANGE = 8.0;
    private static final int SHRINK_CHANCE = 33;

    private final ItemStacksResourceHandler output = new ItemStacksResourceHandler(1) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(TCItems.VOID_SEED.get());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
        }
    };

    private int counter;
    private int progress;

    public BlockEntityVoidSiphon(BlockPos pos, BlockState state) {
        super(TCBlockEntities.VOID_SIPHON.get(), pos, state);
    }

    public ItemStacksResourceHandler output() {
        return output;
    }

    public int progress() {
        return progress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVoidSiphon siphon) {
        siphon.counter++;
        if (!state.getValue(BlockStateProperties.ENABLED) || siphon.counter % WORK_INTERVAL != 0 || siphon.progress >= PROGRESS_REQUIRED && !siphon.hasOutputRoom()) {
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
            ItemStack current = siphon.output.getResource(0).toStack(siphon.output.getAmountAsInt(0));
            if (current.isEmpty()) {
                siphon.output.set(0, ItemResource.of(new ItemStack(TCItems.VOID_SEED.get())), 1);
            } else {
                siphon.output.set(0, siphon.output.getResource(0), current.getCount() + 1);
            }
            changed = true;
        }
        if (changed) {
            siphon.setChanged();
        }
    }

    private boolean hasOutputRoom() {
        ItemStack stack = output.getResource(0).toStack(output.getAmountAsInt(0));
        return stack.isEmpty() || stack.is(TCItems.VOID_SEED.get()) && stack.getCount() < stack.getMaxStackSize();
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
            HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == HitResult.Type.MISS) {
                found.add(rift);
            }
        }
        return found;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr("progress", 0);
        input.child("Output").ifPresent(output::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output_) {
        super.saveAdditional(output_);
        output_.putInt("progress", progress);
        output.serialize(output_.child("Output"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(out);
            nbt.merge(out.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
