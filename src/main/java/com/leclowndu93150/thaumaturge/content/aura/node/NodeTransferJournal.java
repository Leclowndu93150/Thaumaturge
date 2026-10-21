package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.nodes.CapturedNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

final class NodeTransferJournal {
    static final int MAX_BYTES = 65536;

    record Entry(CapturedNode captured, UUID owner, GlobalPos source, Optional<GlobalPos> target, boolean settled) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(CapturedNode.CODEC.fieldOf("captured").forGetter(Entry::captured),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner), GlobalPos.CODEC.fieldOf("source").forGetter(Entry::source), GlobalPos.CODEC.optionalFieldOf("target").forGetter(Entry::target),
                Codec.BOOL.fieldOf("settled").forGetter(Entry::settled)).apply(instance, Entry::new));
    }

    private NodeTransferJournal() {}

    static Optional<Entry> read(ServerLevel level, UUID identity) throws IOException {
        Path path = path(level, identity);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        if (Files.size(path) > MAX_BYTES) {
            throw new IOException("Node transfer journal exceeds size limit");
        }
        CompoundTag tag;
        try (DataInputStream input = new DataInputStream(Files.newInputStream(path))) {
            tag = NbtIo.read(input, NbtAccounter.create(MAX_BYTES));
        } catch (RuntimeException exception) {
            throw new IOException("Invalid node transfer journal", exception);
        }
        if (tag == null) {
            throw new IOException("Missing node transfer journal");
        }
        try {
            Entry entry = Entry.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag)
                    .getOrThrow(message -> new IOException("Invalid node transfer journal: " + message));
            if (!entry.captured().snapshot().identity().equals(identity)) {
                throw new IOException("Node transfer journal identity mismatch");
            }
            return Optional.of(entry);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid node transfer journal", exception);
        }
    }

    static Optional<Entry> legacySource(ServerLevel level, net.minecraft.core.BlockPos pos) throws IOException {
        Path directory = path(level, new UUID(0, 0)).getParent();
        if (!Files.isDirectory(directory)) {
            return Optional.empty();
        }
        try (var paths = Files.newDirectoryStream(directory, "*.dat")) {
            for (Path file : paths) {
                UUID identity;
                try {
                    String name = file.getFileName().toString();
                    identity = UUID.fromString(name.substring(0, name.length() - 4));
                } catch (IllegalArgumentException exception) {
                    throw new IOException("Invalid node transfer journal filename", exception);
                }
                Optional<Entry> found = read(level, identity);
                if (found.isPresent() && found.get().source().dimension().equals(level.dimension()) && found.get().source().pos().equals(pos)) {
                    return found;
                }
            }
        }
        return Optional.empty();
    }

    static void write(ServerLevel level, Entry entry) throws IOException {
        CompoundTag tag = (CompoundTag) Entry.CODEC.encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), entry)
                .getOrThrow(message -> new IOException("Invalid node transfer journal: " + message));
        Entry.CODEC.parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow(message -> new IOException("Invalid node transfer journal: " + message));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(tag, output);
        }
        if (bytes.size() > MAX_BYTES) {
            throw new IOException("Node transfer journal exceeds size limit");
        }
        Path path = path(level, entry.captured().snapshot().identity());
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes.toByteArray());
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        }
        Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static Path path(ServerLevel level, UUID identity) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data/thaumaturge/node_transfers").resolve(identity + ".dat");
    }
}
