package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityManaPod extends BlockEntity {
    public static final int MAX_AGE = 7;
    private static final int ASPECT_STAGE = 3;
    private static final int HERBA_CHANCE = 8;

    private @Nullable ResourceKey<IAspect> aspect;

    public BlockEntityManaPod(BlockPos pos, BlockState state) {
        super(TCBlockEntities.MANA_POD.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public @Nullable Holder<IAspect> aspect() {
        if (aspect == null || getLevel() == null) {
            return null;
        }
        return Aspects.resolve(getLevel().registryAccess(), aspect);
    }

    public void setAspect(@Nullable ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
        setChanged();
        if (getLevel() != null && !getLevel().isClientSide()) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void checkGrowth() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        int age = state.getValue(BlockManaPod.AGE);
        if (age < MAX_AGE) {
            age++;
            level.setBlock(getBlockPos(), state.setValue(BlockManaPod.AGE, age), 3);
        }
        if (age <= ASPECT_STAGE - 1) {
            return;
        }
        RandomSource random = level.getRandom();
        if (age == ASPECT_STAGE) {
            Set<ResourceKey<IAspect>> nearby = new LinkedHashSet<>();
            if (aspect != null) {
                nearby.add(aspect);
            }
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (level.getBlockEntity(getBlockPos().relative(dir)) instanceof BlockEntityManaPod pod && pod.aspect != null) {
                    nearby.add(pod.aspect);
                }
            }
            if (nearby.size() > 1) {
                List<ResourceKey<IAspect>> keys = new ArrayList<>(nearby);
                List<ResourceKey<IAspect>> outlist = new ArrayList<>();
                for (int i = 0; i < keys.size(); i++) {
                    outlist.add(keys.get(i));
                    for (int j = 0; j < keys.size(); j++) {
                        if (i == j) {
                            continue;
                        }
                        Holder<IAspect> first = Aspects.resolve(level.registryAccess(), keys.get(i));
                        Holder<IAspect> second = Aspects.resolve(level.registryAccess(), keys.get(j));
                        if (first == null || second == null) {
                            continue;
                        }
                        Holder<IAspect> combo = AspectCombinations.result(level.registryAccess(), first, second);
                        if (combo == null || combo.getKey() == null) {
                            continue;
                        }
                        outlist.add(combo.getKey());
                        outlist.add(combo.getKey());
                    }
                }
                if (!outlist.isEmpty()) {
                    setAspect(outlist.get(random.nextInt(outlist.size())));
                }
            } else if (!nearby.isEmpty() && aspect == null) {
                setAspect(nearby.iterator().next());
            }
        }
        if (aspect == null) {
            assignWildAspect(level.registryAccess(), random);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void assignWildAspect(HolderLookup.Provider registries, RandomSource random) {
        if (random.nextInt(HERBA_CHANCE) == 0) {
            aspect = TCAspects.HERBA;
        } else {
            List<ResourceKey<IAspect>> primals = registries.lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(entry -> entry.value().isPrimal()).map(Holder.Reference::key).toList();
            aspect = primals.get(random.nextInt(primals.size()));
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null) {
            output.store("Aspect", LegacyIds.ASPECT_KEY_CODEC, aspect);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("Aspect", LegacyIds.ASPECT_KEY_CODEC).orElse(null);
    }

    @Override
    public void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        AspectInstance stored = components.get(TCDataComponents.CRYSTAL_ASPECT.get());
        if (stored != null) {
            aspect = stored.aspect().unwrapKey().orElse(null);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        Holder<IAspect> resolved = aspect();
        if (resolved != null) {
            components.set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(resolved, 1));
        }
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("Aspect");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(collector, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
