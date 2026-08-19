package com.leclowndu93150.thaumaturge.content.misc;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;

public final class Donators {
    private static final Map<String, UUID> BY_NAME = Map.ofEntries(Map.entry("Chubby_Boi", UUID.fromString("809c9dc2-e109-4ebb-b73a-93fc98528d46")),
            Map.entry("bookworm_221", UUID.fromString("d8b61842-3959-4bae-a2d9-d2245accdf88")), Map.entry("MrSterner", UUID.fromString("f7c8dd92-e3f0-422c-9f8c-91489818ec82")),
            Map.entry("kindwed", UUID.fromString("8322d6ab-8ae8-4ddc-a839-ee65d81b8731")), Map.entry("temyatemya", UUID.fromString("335a566a-6802-42d2-aa3d-b6df1e364cf0")),
            Map.entry("Bettername", UUID.fromString("443390ab-f244-4ebb-8f35-1eeb4f0546e2")), Map.entry("Bocha9031", UUID.fromString("f2d2b849-fab2-456d-8270-e9ba9995a4f6")),
            Map.entry("WillTarax", UUID.fromString("7c78529d-d1a6-4564-ba45-35d8892de403")), Map.entry("RainbowMarker", UUID.fromString("11e9193d-cb73-4337-9acc-771cc739e027")));

    private static final Set<UUID> IDS = Set.copyOf(BY_NAME.values());

    private Donators() {}

    public static Map<String, UUID> byName() {
        return BY_NAME;
    }

    public static Set<UUID> ids() {
        return IDS;
    }

    public static boolean is(UUID id) {
        return IDS.contains(id);
    }

    public static boolean is(Player player) {
        return IDS.contains(player.getUUID());
    }
}
