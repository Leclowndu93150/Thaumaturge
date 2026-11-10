package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectEssentiaHost;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectStorage;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BlockEntityAlembic extends AbstractSyncedBlockEntity
        implements IEssentiaConduit, IAspectContainer, SingleAspectEssentiaHost {
    public static final int CAPACITY = 128;
    private static final int NO_SUCTION = 0;

    private static final String ASPECT_KEY = "Aspect";
    private static final String FILTER_KEY = "AspectFilter";
    private static final String AMOUNT_KEY = "Amount";
    private static final String FACING_KEY = "Facing";

    private final SingleAspectStorage storage = new SingleAspectStorage(this);
    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable ResourceKey<IAspect> filter;
    private int amount;
    private Direction spout = Direction.DOWN;

    public BlockEntityAlembic(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ALEMBIC.get(), pos, state);
    }

    @Override
    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    @Override
    public @Nullable ResourceKey<IAspect> aspectFilterKey() {
        return filter;
    }

    @Override
    public int amount() {
        return amount;
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> newFilter) {
        filter = newFilter;
        setChangedAndSync();
    }

    public void setAspectFromLabel(@Nullable ResourceKey<IAspect> labelled) {
        if (amount > 0) {
            return;
        }
        aspect = labelled;
        setChangedAndSync();
    }

    public void aimSpout(Direction newFacing) {
        if (spout == newFacing) {
            return;
        }
        spout = newFacing;
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
        setChangedAndSync();
    }

    public Direction spoutSide() {
        return spout;
    }

    @Override
    public boolean connectsOn(Direction face) {
        if (face == Direction.UP) {
            return spout != Direction.UP;
        }
        return face != Direction.DOWN && face != spout;
    }

    protected void clearAspect() {
        if (amount > 0) {
            storage.markChanged();
        }
        aspect = null;
        amount = 0;
        setChangedAndSync();
    }

    protected static boolean feedColumn(Level level, BlockPos start, Holder<IAspect> aspect) {
        ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
        if (key == null) {
            return false;
        }
        BlockEntityAlembic firstFree = null;
        BlockPos.MutableBlockPos cursor = start.mutable().move(Direction.UP);
        while (LoadedChunks.contains(level, cursor)
                && level.getBlockEntity(cursor) instanceof BlockEntityAlembic candidate) {
            if (candidate.canStart(key)) {
                if (candidate.amount > 0) {
                    return candidate.doAddToContainer(key, 1) == 0;
                }
                if (firstFree == null) {
                    firstFree = candidate;
                }
            }
            cursor.move(Direction.UP);
        }
        return firstFree != null && firstFree.doAddToContainer(key, 1) == 0;
    }

    private boolean canStart(ResourceKey<IAspect> key) {
        return amount < CAPACITY && (filter == null || filter.equals(key)) && (amount == 0 || key.equals(aspect));
    }

    protected int doAddToContainer(ResourceKey<IAspect> key, int requested) {
        if (requested <= 0) {
            return 0;
        }
        if (!canStart(key)) {
            return requested;
        }
        int stored = Math.min(requested, CAPACITY - amount);
        aspect = key;
        amount += stored;
        storage.markChanged();
        setChangedAndSync();
        return requested - stored;
    }

    protected boolean doTakeFromContainer(ResourceKey<IAspect> key, int requested) {
        if (amount < requested || !key.equals(aspect)) {
            return false;
        }
        amount -= requested;
        if (amount <= 0) {
            amount = 0;
            if (filter == null) {
                aspect = null;
            }
        }
        if (requested > 0) {
            storage.markChanged();
            setChangedAndSync();
        }
        return true;
    }

    public EssentiaList getEssentiaContents(HolderLookup.Provider registries) {
        return new EssentiaList(heldAspects(registries));
    }

    public IEssentiaStorage storage(Direction side) {
        return storage.view(side);
    }

    @Override
    public int capacity() {
        return CAPACITY;
    }

    @Override
    public int storageInsertLimit(int requested) {
        return 0;
    }

    @Override
    public void setStorageContents(@Nullable ResourceKey<IAspect> newAspect, int newAmount) {
        aspect = newAspect;
        amount = newAmount;
    }

    @Override
    public int pullThreshold() {
        return NO_SUCTION;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        return EssentiaPull.NONE;
    }

    @Override
    public int insert(Holder<IAspect> added, int requested, Direction face, boolean simulate) {
        return 0;
    }

    @Override
    public int roomFor(Holder<IAspect> added, Direction face) {
        return 0;
    }

    @Override
    public void onStorageCommitted() {
        setChangedAndSync();
    }

    public AspectList heldAspects(HolderLookup.Provider registries) {
        if (aspect != null && amount > 0) {
            Holder<IAspect> holder = Aspects.resolve(registries, aspect);
            if (holder != null) {
                return AspectList.of(new AspectInstance(holder, amount));
            }
        }
        return AspectList.EMPTY;
    }

    @Override
    public boolean releasesOn(Direction face) {
        return connectsOn(face);
    }

    @Override
    public int extract(Holder<IAspect> taken, int requested, Direction face, boolean simulate) {
        ResourceKey<IAspect> key = taken.unwrapKey().orElse(null);
        if (requested <= 0 || key == null || !releasesOn(face)) {
            return 0;
        }
        if (simulate) {
            return amount >= requested && key.equals(aspect) ? requested : 0;
        }
        return doTakeFromContainer(key, requested) ? requested : 0;
    }

    @Override
    public boolean receivesOn(Direction face) {
        return false;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        return aspect == null ? null : Aspects.resolve(level, aspect);
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        return amount;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspect = TTNbt.read(input, ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, registries)
                .orElse(null);
        filter = TTNbt.read(input, FILTER_KEY, LegacyIds.ASPECT_KEY_CODEC, registries)
                .orElse(null);
        amount = Math.clamp(input.getInt(AMOUNT_KEY), 0, CAPACITY);
        spout = TTNbt.read(input, FACING_KEY, Direction.CODEC, registries).orElse(Direction.DOWN);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspect != null) TTNbt.store(output, ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, registries, aspect);
        if (filter != null) TTNbt.store(output, FILTER_KEY, LegacyIds.ASPECT_KEY_CODEC, registries, filter);
        output.putInt(AMOUNT_KEY, amount);
        TTNbt.store(output, FACING_KEY, Direction.CODEC, registries, spout);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        AspectList held = level == null || amount <= 0 ? AspectList.EMPTY : heldAspects(level.registryAccess());
        if (!held.isEmpty()) {
            components.set(TTDataComponents.ESSENTIA_CONTENTS.get(), new EssentiaList(held));
        }
        if (filter != null) {
            components.set(TTDataComponents.ASPECT_FILTER.get(), filter);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        EssentiaList stored = components.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        if (stored != null && !stored.isEmpty()) {
            AspectInstance first = stored.contents().entries().getFirst();
            aspect = first.aspect().unwrapKey().orElse(null);
            amount = Math.min(first.amount(), CAPACITY);
        }
        ResourceKey<IAspect> labelled = components.get(TTDataComponents.ASPECT_FILTER.get());
        if (labelled != null) {
            filter = labelled;
            if (aspect == null) {
                aspect = labelled;
            }
        }
    }

    @Override
    public AspectList getAspects() {
        if (level == null) {
            return AspectList.EMPTY;
        }
        return heldAspects(level.registryAccess());
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects.isEmpty()) {
            return;
        }
        AspectInstance first = aspects.entries().getFirst();
        aspect = first.aspect().unwrapKey().orElse(null);
        amount = Math.min(first.amount(), CAPACITY);
        storage.markChanged();
        setChangedAndSync();
    }

    @Override
    public boolean accepts(Holder<IAspect> candidate) {
        return filter == null || filter.equals(candidate.unwrapKey().orElse(null));
    }

    @Override
    public int fill(Holder<IAspect> filled, int requested) {
        ResourceKey<IAspect> key = filled.unwrapKey().orElse(null);
        return key == null ? requested : doAddToContainer(key, requested);
    }

    @Override
    public boolean drain(Holder<IAspect> drained, int requested) {
        ResourceKey<IAspect> key = drained.unwrapKey().orElse(null);
        return key != null && doTakeFromContainer(key, requested);
    }

    @Override
    public boolean holds(Holder<IAspect> held, int requested) {
        return isHeld(held) && amount >= requested;
    }

    @Override
    public int amountOf(Holder<IAspect> queried) {
        if (!isHeld(queried)) {
            return 0;
        }
        return amount;
    }

    private boolean isHeld(Holder<IAspect> candidate) {
        return aspect != null
                && amount > 0
                && candidate.unwrapKey().filter(aspect::equals).isPresent();
    }
}
