package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public final class SealSettingValues {
    private final SealType type;
    private final Map<String, Boolean> values = new LinkedHashMap<>();

    public SealSettingValues(SealType type) {
        this.type = type;
        type.settings().forEach(setting -> values.put(setting.key(), setting.fallback()));
    }

    public static MapCodec<SealSettingValues> codec(SealType type) {
        return type.showsSettings() ? new Format(type) : MapCodec.unit(() -> new SealSettingValues(type));
    }

    public boolean get(SealSetting setting) {
        return values.getOrDefault(setting.key(), setting.fallback());
    }

    public void set(SealSetting setting, boolean value) {
        if (type.showsSettings() && values.containsKey(setting.key())) {
            values.put(setting.key(), value);
        }
    }

    private static final class Format extends MapCodec<SealSettingValues> {
        private final SealType type;

        private Format(SealType type) {
            this.type = type;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return type.settings().stream().map(setting -> ops.createString(setting.key()));
        }

        @Override
        public <T> DataResult<SealSettingValues> decode(DynamicOps<T> ops, MapLike<T> input) {
            SealSettingValues decoded = new SealSettingValues(type);
            for (SealSetting setting : type.settings()) {
                T stored = input.get(setting.key());
                if (stored != null) {
                    ops.getBooleanValue(stored).result().ifPresent(value -> decoded.values.put(setting.key(), value));
                }
            }
            return DataResult.success(decoded);
        }

        @Override
        public <T> RecordBuilder<T> encode(SealSettingValues input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            input.values.forEach((key, value) -> prefix.add(key, ops.createBoolean(value)));
            return prefix;
        }
    }
}
