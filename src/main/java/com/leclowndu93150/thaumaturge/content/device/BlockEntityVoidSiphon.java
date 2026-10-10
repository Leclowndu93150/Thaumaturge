package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class BlockEntityVoidSiphon extends AbstractSyncedBlockEntity {
    public static final int PROGRESS_REQUIRED = 2000;

    private static final int WORK_INTERVAL = 20;
    private static final int STREAK_INTERVAL = 40;
    private static final int STREAK_VARIANTS = 8;
    private static final float STREAK_SCALE = 0.12F;
    private static final double RIFT_RANGE = 8.0;
    private static final int MIN_RIFT_SIZE = 2;
    private static final float STABILITY_DIVISOR = 15.0F;
    private static final int SHRINK_CHANCE = 33;
    private static final double BLOCK_CENTER = 0.5;
    private static final double TOP_CLEARANCE = 1.01;
    private static final double FOOTPRINT_EDGE = 0.5;
    private static final int SLOT_COUNT = 1;
    private static final int OUTPUT_SLOT = 0;
    private static final String PROGRESS_KEY = "progress";
    private static final String OUTPUT_KEY = "Output";

    private final SeedOutput output = new SeedOutput(this);
    private int progress;
    private int ticks;

    public BlockEntityVoidSiphon(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VOID_SIPHON.get(), pos, state);
    }

    public ItemStacksResourceHandler output() {
        return output;
    }

    public int progress() {
        return progress;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVoidSiphon siphon) {
        if (++siphon.ticks % WORK_INTERVAL != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        if (state.getValue(BlockStateProperties.ENABLED) && siphon.progress < PROGRESS_REQUIRED && siphon.trySeed(false)) {
            siphon.drain(server, pos);
        }
        siphon.produce();
    }

    private void drain(ServerLevel level, BlockPos pos) {
        Vec3 top = new Vec3(pos.getX() + BLOCK_CENTER, pos.getY() + TOP_CLEARANCE, pos.getZ() + BLOCK_CENTER);
        List<EntityFluxRift> drained = new ArrayList<>();
        for (EntityFluxRift rift : level.getEntitiesOfClass(EntityFluxRift.class, new AABB(pos).inflate(RIFT_RANGE))) {
            if (isTappable(level, top, rift)) {
                harvest(rift, level.getRandom());
                drained.add(rift);
            }
        }
        if (drained.isEmpty()) {
            return;
        }
        setChanged();
        if (ticks % STREAK_INTERVAL == 0) {
            drained.forEach(rift -> showStreak(level, rift, top));
        }
    }

    private static boolean isTappable(Level level, Vec3 top, EntityFluxRift rift) {
        if (!rift.isAlive() || rift.currentSize() < MIN_RIFT_SIZE) {
            return false;
        }
        Vec3 riftCenter = rift.position();
        return isUnobstructed(level, edgePoint(top, riftCenter), riftCenter);
    }

    private void harvest(EntityFluxRift rift, RandomSource random) {
        int size = rift.currentSize();
        float root = Mth.sqrt(size);
        progress += Mth.floor(root);
        rift.adjustStability(-root / STABILITY_DIVISOR);
        if (random.nextInt(SHRINK_CHANCE) == 0) {
            rift.resize(size - 1);
        }
    }

    private static void showStreak(ServerLevel level, EntityFluxRift rift, Vec3 top) {
        RandomSource random = level.getRandom();
        Vec3 from = rift.position();
        if (!rift.outline.isEmpty()) {
            from = from.add(rift.outline.get(random.nextInt(rift.outline.size())));
        }
        Effects.voidStream(level, from).to(top).seed(random.nextInt(STREAK_VARIANTS)).scale(STREAK_SCALE).send();
    }

    private void produce() {
        boolean made = false;
        while (progress >= PROGRESS_REQUIRED && trySeed(true)) {
            progress -= PROGRESS_REQUIRED;
            made = true;
        }
        if (made) {
            setChanged();
        }
    }

    private boolean trySeed(boolean commit) {
        ItemStack current = output.getResource(OUTPUT_SLOT).toStack(output.getAmountAsInt(OUTPUT_SLOT));
        ItemResource seed = ItemResource.of(TTItems.VOID_SEED.get());
        boolean room = current.isEmpty() || current.getCount() < current.getMaxStackSize();
        if (room && commit) {
            output.set(OUTPUT_SLOT, seed, current.getCount() + 1);
        }
        return room;
    }

    private static Vec3 edgePoint(Vec3 origin, Vec3 target) {
        Vec3 flat = new Vec3(target.x - origin.x, 0.0, target.z - origin.z);
        double span = flat.length();
        return span <= FOOTPRINT_EDGE ? origin : origin.add(flat.scale(FOOTPRINT_EDGE / span));
    }

    private static boolean isUnobstructed(Level level, Vec3 from, Vec3 to) {
        ClipContext context = new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        return level.clip(context).getType() == HitResult.Type.MISS;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        progress = input.getIntOr(PROGRESS_KEY, 0);
        input.child(OUTPUT_KEY).ifPresent(output::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putInt(PROGRESS_KEY, progress);
        output.serialize(valueOutput.child(OUTPUT_KEY));
    }

    private static final class SeedOutput extends ItemStacksResourceHandler {
        private final BlockEntity owner;

        private SeedOutput(BlockEntity owner) {
            super(SLOT_COUNT);
            this.owner = owner;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.is(TTItems.VOID_SEED.get());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            owner.setChanged();
        }
    }
}
