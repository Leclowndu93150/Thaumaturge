package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public final class BlockEntityEldritchReliquary extends AbstractSyncedBlockEntity {
    private static final String MAZE = "maze";
    private static final String ROLE = "role";
    private static final String LOOT = "loot";
    private static final String CACHE_PREFIX = "cache/";
    private static final int VIEW_INTERVAL = 20;

    private Optional<MazeId> maze = Optional.empty();
    private ReliquaryRole role = ReliquaryRole.CACHE;
    private Optional<ResourceKey<LootTable>> loot = Optional.empty();
    private final Map<UUID, ReliquaryView> sent = new HashMap<>();

    public BlockEntityEldritchReliquary(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ELDRITCH_RELIQUARY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEldritchReliquary reliquary) {
        if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % VIEW_INTERVAL == 0) {
            ReliquaryViews.push(serverLevel, reliquary);
        }
    }

    public void configure(MazeId maze, ReliquaryRole role, Optional<ResourceKey<LootTable>> loot) {
        this.maze = Optional.of(maze);
        this.role = role;
        this.loot = loot;
        setChangedAndSync();
    }

    public Optional<MazeId> maze() {
        return maze;
    }

    public ReliquaryRole role() {
        return role;
    }

    Optional<ResourceKey<LootTable>> loot() {
        return loot;
    }

    String claimKey() {
        return role == ReliquaryRole.CACHE ? CACHE_PREFIX + worldPosition.asLong() : role.getSerializedName();
    }

    Map<UUID, ReliquaryView> sentViews() {
        return sent;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        maze = TTNbt.read(input, MAZE, MazeId.CODEC, registries);
        role = TTNbt.read(input, ROLE, ReliquaryRole.CODEC, registries).orElse(ReliquaryRole.CACHE);
        loot = TTNbt.read(input, LOOT, ResourceKey.codec(Registries.LOOT_TABLE), registries);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (maze.orElse(null) != null) TTNbt.store(output, MAZE, MazeId.CODEC, registries, maze.orElse(null));
        TTNbt.store(output, ROLE, ReliquaryRole.CODEC, registries, role);
        if (loot.orElse(null) != null)
            TTNbt.store(output, LOOT, ResourceKey.codec(Registries.LOOT_TABLE), registries, loot.orElse(null));
    }
}
