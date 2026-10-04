package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

public final class ClientSealHolder {
    private static final Map<SealPos, SealEntity> SEALS = new ConcurrentHashMap<>();

    private ClientSealHolder() {}

    public static @Nullable SealEntity get(SealPos pos) {
        return SEALS.get(pos);
    }

    public static Map<SealPos, SealEntity> all() {
        return SEALS;
    }

    public static void put(SealEntity seal) {
        SEALS.put(seal.pos(), seal);
    }

    public static void remove(SealPos pos) {
        SEALS.remove(pos);
    }

    public static void clear() {
        SEALS.clear();
    }
}
