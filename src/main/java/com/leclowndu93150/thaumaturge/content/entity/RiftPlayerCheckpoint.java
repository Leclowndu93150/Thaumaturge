package com.leclowndu93150.thaumaturge.content.entity;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

final class RiftPlayerCheckpoint {
    private static final long MAX_PLAYER_BYTES = 16 * 1024 * 1024;

    private RiftPlayerCheckpoint() {}

    static void save(ServerPlayer player, UUID token) throws IOException {
        player.getPersistentData().putBoolean(key(token), true);
        player.level().getServer().getPlayerList().saveAll();
        if (!committed(player.level(), player.getUUID(), token)) {
            throw new IOException("Rift payment player checkpoint was not saved");
        }
    }

    static void clear(ServerPlayer player, UUID token) {
        player.getPersistentData().remove(key(token));
    }

    static boolean committed(ServerLevel level, UUID player, UUID token) throws IOException {
        Path path = level.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(player + ".dat");
        if (!Files.exists(path)) {
            Path previous = path.resolveSibling(player + ".dat_old");
            if (!Files.exists(previous)) {
                return false;
            }
            path = previous;
        }
        if (Files.size(path) > MAX_PLAYER_BYTES) {
            throw new IOException("Player checkpoint exceeds size limit");
        }
        boolean present;
        try {
            var data = NbtIo.readCompressed(path, NbtAccounter.create(MAX_PLAYER_BYTES));
            present = data.getCompound("NeoForgeData").map(compound -> compound.getBooleanOr(key(token), false)).orElse(false);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid player checkpoint", exception);
        }
        if (present) {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
        }
        return present;
    }

    private static String key(UUID token) {
        return "thaumaturge:rift_payment/" + token;
    }
}
