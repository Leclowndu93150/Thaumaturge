package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.rifts.CapturedRift;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

final class RiftTransferJournal {
    private static final int MAX_BYTES = 65536;

    record Location(ResourceKey<Level> dimension, Vec3 position) {
        static final Codec<Location> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Location::dimension), Vec3.CODEC.fieldOf("position").forGetter(Location::position))
                        .apply(instance, Location::new));
    }

    record Checkpoint(int jarSlot, int wandSlot, ItemStack beforeJar, ItemStack beforeWand, ItemStack afterWand, Optional<UUID> previousToken) {
        static final Codec<Checkpoint> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.intRange(0, 35).fieldOf("jar_slot").forGetter(Checkpoint::jarSlot), Codec.intRange(0, 35).fieldOf("wand_slot").forGetter(Checkpoint::wandSlot),
                        ItemStack.CODEC.fieldOf("before_jar").forGetter(Checkpoint::beforeJar), ItemStack.CODEC.fieldOf("before_wand").forGetter(Checkpoint::beforeWand),
                        ItemStack.CODEC.fieldOf("after_wand").forGetter(Checkpoint::afterWand), UUIDUtil.CODEC.optionalFieldOf("previous_token").forGetter(Checkpoint::previousToken))
                .apply(instance, Checkpoint::new));
    }

    record Entry(CapturedRift captured, UUID owner, Location source, Optional<Location> target, boolean settled, Optional<Checkpoint> checkpoint) {
        Entry(CapturedRift captured, UUID owner, Location source, Optional<Location> target, boolean settled) {
            this(captured, owner, source, target, settled, Optional.empty());
        }
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(CapturedRift.CODEC.fieldOf("captured").forGetter(Entry::captured),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner), Location.CODEC.fieldOf("source").forGetter(Entry::source), Location.CODEC.optionalFieldOf("target").forGetter(Entry::target),
                Codec.BOOL.fieldOf("settled").forGetter(Entry::settled), Checkpoint.CODEC.optionalFieldOf("checkpoint").forGetter(Entry::checkpoint)).apply(instance, Entry::new));
    }

    private RiftTransferJournal() {}

    static void restore(ServerLevel level, UUID identity, Optional<Entry> previous) throws IOException {
        if (previous.isPresent()) {
            write(level, previous.get());
        } else {
            Files.deleteIfExists(path(level, identity));
        }
    }

    static Optional<Entry> read(ServerLevel level, UUID identity) throws IOException {
        Path path = path(level, identity);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        if (Files.size(path) > MAX_BYTES) {
            throw new IOException("Rift transfer journal exceeds size limit");
        }
        try (DataInputStream input = new DataInputStream(Files.newInputStream(path))) {
            CompoundTag tag = NbtIo.read(input, NbtAccounter.create(MAX_BYTES));
            Entry entry = Entry.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                    .getOrThrow(message -> new IOException("Invalid rift transfer journal: " + message));
            if (!entry.captured().identity().equals(identity)) {
                throw new IOException("Rift transfer journal identity mismatch");
            }
            return Optional.of(entry);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid rift transfer journal", exception);
        }
    }

    static void write(ServerLevel level, Entry entry) throws IOException {
        byte[] encoded;
        try {
            CompoundTag tag = (CompoundTag) Entry.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), entry).getOrThrow();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                NbtIo.write(tag, output);
            }
            encoded = bytes.toByteArray();
        } catch (RuntimeException exception) {
            throw new IOException("Invalid rift transfer journal", exception);
        }
        if (encoded.length > MAX_BYTES) {
            throw new IOException("Rift transfer journal exceeds size limit");
        }
        Path path = path(level, entry.captured().identity());
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(encoded);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
        Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static Path path(ServerLevel level, UUID identity) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data/thaumaturge/rift_transfers").resolve(identity + ".dat");
    }
}
