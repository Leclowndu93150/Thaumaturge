package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityManaPod extends AbstractSyncedBlockEntity {
    public static final int MAX_AGE = 7;
    private static final String ASPECT_KEY = "Aspect";
    private static final String SHOWCASE_KEY = "Showcase";
    private static final int MIX_STAGE = 3;
    private static final int WILD_HERBA_ROLL_BOUND = 8;
    private static final int COMPOUND_ORDERED_PAIR_WEIGHT = 2;
    private static final int COMPONENT_COUNT = 2;
    private static final int COMPONENT_ORDERINGS = 2;
    private static final int BASE_WEIGHT = 1;

    private @Nullable ResourceKey<IAspect> aspectKey;
    private boolean showcase;

    public BlockEntityManaPod(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MANA_POD.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspectKey;
    }

    public @Nullable Holder<IAspect> aspect() {
        if (aspectKey == null || level == null) {
            return null;
        }
        return Aspects.resolve(level, aspectKey);
    }

    public boolean isShowcase() {
        return showcase;
    }

    public void setShowcase() {
        showcase = true;
        setChanged();
    }

    public void setAspect(@Nullable ResourceKey<IAspect> key) {
        aspectKey = key;
        setChanged();
        syncToClient();
    }

    public void checkGrowth() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = level.getBlockState(worldPosition);
        if (!current.is(TTBlocks.MANA_POD.get())) {
            return;
        }
        int before = current.getValue(BlockManaPod.AGE);
        int after = before < MAX_AGE ? before + 1 : before;
        if (after > before) {
            level.setBlock(worldPosition, current.setValue(BlockManaPod.AGE, after), Block.UPDATE_ALL);
        }
        if (after < MIX_STAGE) {
            return;
        }
        if (after == MIX_STAGE) {
            mixWithNeighbours(level);
        }
        if (aspectKey != null) {
            return;
        }
        assignWildAspect(level.registryAccess(), level.getRandom());
        syncToClient();
    }

    public void assignWildAspect(HolderLookup.Provider registries, RandomSource random) {
        boolean herba = random.nextInt(WILD_HERBA_ROLL_BOUND) == 0;
        aspectKey = herba ? TTAspects.HERBA : pickPrimal(registries, random);
        setChanged();
    }

    private static ResourceKey<IAspect> pickPrimal(HolderLookup.Provider registries, RandomSource random) {
        List<ResourceKey<IAspect>> primals = registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(holder -> holder.value().isPrimal()).map(Holder.Reference::key).toList();
        if (primals.isEmpty()) {
            throw new IllegalStateException("Aspect registry contains no primal aspect");
        }
        return primals.get(random.nextInt(primals.size()));
    }

    private void mixWithNeighbours(Level level) {
        Set<ResourceKey<IAspect>> pool = new LinkedHashSet<>();
        pool.add(aspectKey);
        Direction.Plane.HORIZONTAL.stream().map(side -> level.getBlockEntity(worldPosition.relative(side))).filter(BlockEntityManaPod.class::isInstance)
                .map(neighbour -> ((BlockEntityManaPod) neighbour).aspectKey).forEach(pool::add);
        pool.remove(null);
        if (pool.size() > 1) {
            setAspect(drawWeighted(level, pool));
        } else if (aspectKey == null && !pool.isEmpty()) {
            setAspect(pool.iterator().next());
        }
    }

    private static boolean combinesPool(IAspect aspect, Set<ResourceKey<IAspect>> pool) {
        List<Holder<IAspect>> parts = aspect.components();
        if (parts.size() != COMPONENT_COUNT) {
            return false;
        }
        ResourceKey<IAspect> first = parts.get(0).unwrapKey().orElse(null);
        ResourceKey<IAspect> second = parts.get(1).unwrapKey().orElse(null);
        return first != null && second != null && !first.equals(second) && pool.contains(first) && pool.contains(second);
    }

    private static ResourceKey<IAspect> drawWeighted(Level level, Set<ResourceKey<IAspect>> pool) {
        Map<ResourceKey<IAspect>, Integer> weights = new LinkedHashMap<>();
        pool.forEach(key -> weights.put(key, BASE_WEIGHT));
        int compoundWeight = COMPOUND_ORDERED_PAIR_WEIGHT * COMPONENT_ORDERINGS;
        level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(entry -> combinesPool(entry.value(), pool))
                .forEach(entry -> weights.merge(entry.key(), compoundWeight, Integer::sum));
        int total = 0;
        for (int weight : weights.values()) {
            total += weight;
        }
        int ticket = level.getRandom().nextInt(total);
        ResourceKey<IAspect> chosen = null;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : weights.entrySet()) {
            chosen = entry.getKey();
            ticket -= entry.getValue();
            if (ticket < 0) {
                break;
            }
        }
        return chosen;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        Optional.ofNullable(aspectKey).ifPresent(key -> output.store(ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, key));
        if (showcase) {
            output.putBoolean(SHOWCASE_KEY, true);
        }
        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        Optional<ResourceKey<IAspect>> stored = input.read(ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC);
        super.loadAdditional(input);
        ResourceKey<IAspect> previous = aspectKey;
        aspectKey = stored.orElse(null);
        showcase = input.getBooleanOr(SHOWCASE_KEY, false);
        Level current = level;
        if (current != null && current.isClientSide() && !Objects.equals(previous, aspectKey)) {
            current.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        AspectInstance instance = components.get(TTDataComponents.CRYSTAL_ASPECT.get());
        if (instance != null) {
            aspectKey = instance.aspect().unwrapKey().orElse(null);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        Holder<IAspect> holder = aspect();
        if (holder != null) {
            components.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(holder, 1));
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard(ASPECT_KEY);
    }
}
