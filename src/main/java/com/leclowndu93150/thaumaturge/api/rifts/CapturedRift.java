package com.leclowndu93150.thaumaturge.api.rifts;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** A bounded carrier payload; unknown versions remain intact but cannot authorize release. */
public record CapturedRift(int version, UUID identity, UUID token, String state) {
    public static final int VERSION = 1;
    public static final int MAX_STATE_BYTES = 16384;
    private static final Codec<String> STATE_CODEC = Codec.STRING
            .validate(value -> value.getBytes(StandardCharsets.UTF_8).length <= MAX_STATE_BYTES ? DataResult.success(value) : DataResult.error(() -> "Captured rift state exceeds size limit"));
    public static final Codec<CapturedRift> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.intRange(1, Integer.MAX_VALUE).fieldOf("version").forGetter(CapturedRift::version), UUIDUtil.CODEC.fieldOf("identity").forGetter(CapturedRift::identity),
                    UUIDUtil.CODEC.fieldOf("token").forGetter(CapturedRift::token), STATE_CODEC.fieldOf("state").forGetter(CapturedRift::state)).apply(instance, CapturedRift::new));

    public CapturedRift {
        Objects.requireNonNull(identity);
        Objects.requireNonNull(token);
        Objects.requireNonNull(state);
        if (version < 1 || state.getBytes(StandardCharsets.UTF_8).length > MAX_STATE_BYTES) {
            throw new IllegalArgumentException("Invalid captured rift payload");
        }
    }

    public static CapturedRift create(UUID identity, RiftContents contents) {
        String state = RiftContents.CODEC.encodeStart(JsonOps.INSTANCE, contents).getOrThrow().toString();
        return new CapturedRift(VERSION, identity, UUID.randomUUID(), state);
    }

    public Optional<RiftContents> contents() {
        if (version != VERSION) {
            return Optional.empty();
        }
        try {
            return RiftContents.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(state)).result();
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }
}
