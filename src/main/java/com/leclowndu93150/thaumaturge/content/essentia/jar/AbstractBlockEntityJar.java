package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaJar;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaStorage;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectEssentiaHost;
import com.leclowndu93150.thaumaturge.content.essentia.storage.SingleAspectStorage;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBlockEntityJar extends AbstractSyncedBlockEntity
        implements IEssentiaConduit, IAspectSource, SingleAspectEssentiaHost {
    public static final int CAPACITY = IEssentiaJar.DEFAULT_CAPACITY;

    private static final String ASPECT_KEY = "Aspect";
    private static final String FILTER_KEY = "AspectFilter";
    private static final String AMOUNT_KEY = "Amount";
    private static final String FACING_KEY = "Facing";
    private static final String BRACED_KEY = "Braced";
    private static final int FILL_INTERVAL = 5;
    private static final int FILL_POINTS = 1;
    private static final int FILTERED_SUCTION = 64;
    private static final int UNFILTERED_SUCTION = 32;

    private final SingleAspectStorage storage = new SingleAspectStorage(this);
    private @Nullable ResourceKey<IAspect> aspect;
    private @Nullable ResourceKey<IAspect> aspectFilter;
    private int amount;
    private Direction facing = Direction.DOWN;
    private boolean braced;
    private int fillTicks;

    protected AbstractBlockEntityJar(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int capacity() {
        return getBlockState().getBlock() instanceof IEssentiaJar jar ? jar.jarCapacity() : CAPACITY;
    }

    @Override
    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    @Override
    public @Nullable ResourceKey<IAspect> aspectFilterKey() {
        return aspectFilter;
    }

    @Override
    public int amount() {
        return amount;
    }

    public void setBraced(boolean newBraced) {
        braced = newBraced;
        setChangedAndSync();
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> newFilter) {
        aspectFilter = newFilter;
        setChangedAndSync();
    }

    public void setAspectFromLabel(@Nullable ResourceKey<IAspect> labelled) {
        if (amount > 0) {
            return;
        }
        aspect = labelled;
        setChangedAndSync();
    }

    public void clearAspectIfEmpty() {
        if (amount <= 0 && aspectFilter == null) {
            aspect = null;
            setChangedAndSync();
        }
    }

    public void emptyJar() {
        if (amount > 0) {
            if (level != null && !level.isClientSide() && aspectFilter == null) {
                AuraHelper.addFlux(level, worldPosition, amount);
            }
            storage.markChanged();
        }
        if (aspectFilter == null) {
            aspect = null;
        }
        amount = 0;
        setChangedAndSync();
    }

    public void setFacing(Direction newFacing) {
        facing = newFacing;
        setChangedAndSync();
    }

    public Direction facing() {
        return facing;
    }

    protected void replaceContents(ResourceKey<IAspect> newAspect, int newAmount) {
        aspect = newAspect;
        amount = newAmount;
        storage.markChanged();
        setChangedAndSync();
    }

    public int spillAmount() {
        return amount;
    }

    protected void clearAspect() {
        if (amount > 0) {
            storage.markChanged();
        }
        aspect = null;
        amount = 0;
        setChangedAndSync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractBlockEntityJar jar) {
        if (++jar.fillTicks % FILL_INTERVAL == 0 && jar.shouldFillFromAbove()) {
            jar.pullFromAbove(level, pos);
        }
    }

    protected boolean shouldFillFromAbove() {
        return amount < capacity();
    }

    private void pullFromAbove(Level level, BlockPos pos) {
        IEssentiaConduit source = EssentiaFlowHandler.conduit(level, pos.above(), Direction.DOWN);
        if (source == null || !source.releasesOn(Direction.DOWN)) {
            return;
        }
        int ownPull = pullStrength();
        if (ownPull <= source.pull(Direction.DOWN).strength()) {
            return;
        }
        Holder<IAspect> chosen = pickPulledAspect(level, source, ownPull);
        if (chosen == null) {
            return;
        }
        Optional<ResourceKey<IAspect>> key = chosen.unwrapKey();
        if (key.isEmpty() || source.extract(chosen, FILL_POINTS, Direction.DOWN, false) != FILL_POINTS) {
            return;
        }
        doAddToContainer(key.get(), FILL_POINTS);
    }

    private @Nullable Holder<IAspect> pickPulledAspect(Level level, IEssentiaConduit source, int ownPull) {
        ResourceKey<IAspect> fixed = aspectFilter != null ? aspectFilter : aspect;
        if (fixed != null) {
            return Aspects.resolve(level, fixed);
        }
        Holder<IAspect> offered = source.heldAspect(Direction.DOWN);
        boolean sourceTooThin = source.heldAmount(Direction.DOWN) < FILL_POINTS;
        return sourceTooThin || ownPull < source.pullThreshold() ? null : offered;
    }

    protected boolean refuses(ResourceKey<IAspect> key) {
        return aspectFilter != null && !aspectFilter.equals(key) || amount > 0 && !key.equals(aspect);
    }

    protected int acceptable(ResourceKey<IAspect> key, int requested) {
        if (requested <= 0 || refuses(key)) {
            return 0;
        }
        return Math.max(0, Math.min(requested, capacity() - amount));
    }

    protected int doAddToContainer(ResourceKey<IAspect> key, int requested) {
        if (requested <= 0) {
            return 0;
        }
        if (refuses(key)) {
            return requested;
        }
        int stored = Math.max(0, Math.min(requested, capacity() - amount));
        aspect = key;
        amount += stored;
        if (stored > 0) {
            storage.markChanged();
            setChangedAndSync();
        }
        return requested - stored;
    }

    protected boolean doTakeFromContainer(ResourceKey<IAspect> key, int requested) {
        if (amount < requested || !key.equals(aspect)) {
            return false;
        }
        amount -= requested;
        if (amount <= 0) {
            amount = 0;
            if (aspectFilter == null) {
                aspect = null;
            }
        }
        if (requested > 0) {
            storage.markChanged();
            setChangedAndSync();
        }
        return true;
    }

    public IEssentiaStorage storage(Direction side) {
        return storage.view(side);
    }

    @Override
    public int storageInsertLimit(int requested) {
        return Math.max(0, Math.min(requested, capacity() - amount));
    }

    @Override
    public void setStorageContents(@Nullable ResourceKey<IAspect> newAspect, int newAmount) {
        aspect = newAspect;
        amount = newAmount;
    }

    @Override
    public void onStorageCommitted() {
        setChangedAndSync();
    }

    private @Nullable AspectInstance heldInstance(HolderLookup.Provider registries) {
        if (aspect == null || amount <= 0) {
            return null;
        }
        Holder<IAspect> holder = Aspects.resolve(registries, aspect);
        return holder == null ? null : new AspectInstance(holder, amount);
    }

    public AspectList getContents(HolderLookup.Provider registries) {
        AspectInstance held = heldInstance(registries);
        return held == null ? AspectList.EMPTY : AspectList.of(held);
    }

    public EssentiaList getEssentiaContents(HolderLookup.Provider registries) {
        AspectInstance held = heldInstance(registries);
        return new EssentiaList(held == null ? AspectList.EMPTY : AspectList.of(held));
    }

    private static boolean isTopFace(Direction face) {
        return Direction.UP.equals(face);
    }

    @Override
    public boolean connectsOn(Direction face) {
        return isTopFace(face);
    }

    @Override
    public boolean receivesOn(Direction face) {
        return isTopFace(face);
    }

    @Override
    public boolean releasesOn(Direction face) {
        return isTopFace(face);
    }

    protected int pullStrength() {
        if (amount >= capacity()) {
            return 0;
        }
        return aspectFilter != null ? FILTERED_SUCTION : UNFILTERED_SUCTION;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        int strength = pullStrength();
        if (strength <= 0) {
            return EssentiaPull.NONE;
        }
        ResourceKey<IAspect> wanted = aspectFilter != null ? aspectFilter : aspect;
        return EssentiaPull.of(wanted == null ? null : Aspects.resolve(level, wanted), strength);
    }

    @Override
    public int pullThreshold() {
        return aspectFilter != null ? FILTERED_SUCTION : UNFILTERED_SUCTION;
    }

    @Override
    public int extract(Holder<IAspect> taken, int requested, Direction face, boolean simulate) {
        if (!isTopFace(face) || requested <= 0) {
            return 0;
        }
        Optional<ResourceKey<IAspect>> key = taken.unwrapKey();
        if (key.isEmpty()) {
            return 0;
        }
        if (simulate) {
            return amount >= requested && key.get().equals(aspect) ? requested : 0;
        }
        return doTakeFromContainer(key.get(), requested) ? requested : 0;
    }

    @Override
    public int insert(Holder<IAspect> added, int requested, Direction face, boolean simulate) {
        ResourceKey<IAspect> key = added.unwrapKey().orElse(null);
        if (key == null || face != Direction.UP) {
            return 0;
        }
        if (simulate) {
            return acceptable(key, requested);
        }
        return requested - doAddToContainer(key, requested);
    }

    @Override
    public int roomFor(Holder<IAspect> candidate, Direction face) {
        ResourceKey<IAspect> key = candidate.unwrapKey().orElse(null);
        return key == null || !isTopFace(face) ? 0 : acceptable(key, Integer.MAX_VALUE);
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
        aspectFilter = TTNbt.read(input, FILTER_KEY, LegacyIds.ASPECT_KEY_CODEC, registries)
                .orElse(null);
        amount = input.getInt(AMOUNT_KEY);
        facing = TTNbt.read(input, FACING_KEY, Direction.CODEC, registries).orElse(Direction.DOWN);
        braced = input.getBoolean(BRACED_KEY);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspect != null) TTNbt.store(output, ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, registries, aspect);
        if (aspectFilter != null) TTNbt.store(output, FILTER_KEY, LegacyIds.ASPECT_KEY_CODEC, registries, aspectFilter);
        output.putInt(AMOUNT_KEY, amount);
        TTNbt.store(output, FACING_KEY, Direction.CODEC, registries, facing);
        output.putBoolean(BRACED_KEY, braced);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (level != null && aspect != null && amount > 0) {
            AspectList held = getContents(level.registryAccess());
            if (!held.isEmpty()) {
                components.set(TTDataComponents.ESSENTIA_CONTENTS.get(), new EssentiaList(held));
            }
        }
        if (aspectFilter != null) {
            components.set(TTDataComponents.ASPECT_FILTER.get(), aspectFilter);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        EssentiaList stored = components.get(TTDataComponents.ESSENTIA_CONTENTS.get());
        if (stored != null && !stored.isEmpty()) {
            AspectInstance first = stored.contents().entries().getFirst();
            aspect = first.aspect().unwrapKey().orElse(null);
            amount = Math.min(first.amount(), capacity());
        }
        ResourceKey<IAspect> labelled = components.get(TTDataComponents.ASPECT_FILTER.get());
        if (labelled != null) {
            aspectFilter = labelled;
            if (aspect == null) {
                aspect = labelled;
            }
        }
    }

    @Override
    public AspectList getAspects() {
        return level == null ? AspectList.EMPTY : getContents(level.registryAccess());
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects.isEmpty()) {
            return;
        }
        AspectInstance first = aspects.entries().getFirst();
        aspect = first.aspect().unwrapKey().orElse(null);
        amount = Math.min(first.amount(), capacity());
        storage.markChanged();
        setChangedAndSync();
    }

    @Override
    public boolean accepts(Holder<IAspect> candidate) {
        return aspectFilter == null || candidate.is(aspectFilter);
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
    public boolean holds(Holder<IAspect> candidate, int requested) {
        return amount >= requested && aspect != null && candidate.is(aspect);
    }

    @Override
    public int amountOf(Holder<IAspect> candidate) {
        return aspect != null && candidate.is(aspect) ? amount : 0;
    }

    @Override
    public boolean isBlocked() {
        return braced;
    }
}
