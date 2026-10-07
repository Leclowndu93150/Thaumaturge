package com.leclowndu93150.thaumaturge.content.donator;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;

public final class Donators {
    private static final Map<String, UUID> DONATORS = Map.ofEntries(Map.entry("Chubby_Boi", UUID.fromString("809c9dc2-e109-4ebb-b73a-93fc98528d46")),
            Map.entry("bookworm_221", UUID.fromString("d8b61842-3959-4bae-a2d9-d2245accdf88")), Map.entry("MrSterner", UUID.fromString("f7c8dd92-e3f0-422c-9f8c-91489818ec82")),
            Map.entry("kindwed", UUID.fromString("8322d6ab-8ae8-4ddc-a839-ee65d81b8731")), Map.entry("temyatemya", UUID.fromString("335a566a-6802-42d2-aa3d-b6df1e364cf0")),
            Map.entry("Bettername", UUID.fromString("443390ab-f244-4ebb-8f35-1eeb4f0546e2")), Map.entry("Bocha9031", UUID.fromString("f2d2b849-fab2-456d-8270-e9ba9995a4f6")),
            Map.entry("WillTarax", UUID.fromString("7c78529d-d1a6-4564-ba45-35d8892de403")), Map.entry("RainbowMarker", UUID.fromString("11e9193d-cb73-4337-9acc-771cc739e027")),
            Map.entry("anatevka", UUID.fromString("c8ca8c63-688f-4703-87b5-3abe18fabe1e")), Map.entry("Demonseed", UUID.fromString("41aa8244-03f4-4e22-9dc7-69ac7bd541a6")),
            Map.entry("SoyGoulden", UUID.fromString("d91707fa-b0a6-453d-9251-03d6aeebf6fa")), Map.entry("Ordeaux", UUID.fromString("f140f8e1-67eb-4693-96aa-c2541ba24944")),
            Map.entry("WoXayZ", UUID.fromString("d83a8dbc-0c6f-48b2-9ae7-8737575d1148")));

    private static final Map<String, UUID> DEVELOPERS = Map.ofEntries(Map.entry("JustReclipse", UUID.fromString("119d9971-936d-478f-96bd-82d66c0e978d")),
            Map.entry("Leclowndu93150", UUID.fromString("3add57e3-ce76-4b0f-b21b-7298b0706be7")), Map.entry("iglee42", UUID.fromString("9725f535-c691-43b6-b1a9-f3f17d69e015")),
            Map.entry("Saereth", UUID.fromString("4ecf6284-b1e8-45bb-b2b3-151c95c3b10f")), Map.entry("Deadsix", UUID.fromString("f3717fe8-6d84-47ae-933d-56a22daa90d8")));

    private static final Set<UUID> IDS = Stream.concat(DONATORS.values().stream(), DEVELOPERS.values().stream()).collect(Collectors.toUnmodifiableSet());
    private static final String DEV_NAME = "Dev";

    private Donators() {}

    public static Map<String, UUID> donators() {
        return DONATORS;
    }

    public static Map<String, UUID> developers() {
        return DEVELOPERS;
    }

    public static Set<UUID> ids() {
        return IDS;
    }

    public static boolean is(UUID id, String name) {
        return IDS.contains(id) || (!FMLEnvironment.isProduction() && DEV_NAME.equals(name));
    }

    public static boolean is(Player player) {
        return is(player.getUUID(), player.getGameProfile().name());
    }
}
