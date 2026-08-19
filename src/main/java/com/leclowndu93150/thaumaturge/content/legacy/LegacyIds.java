package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

public final class LegacyIds {
    public static final String LEGACY_NAMESPACE = "thaumcraft";

    public static final Codec<Identifier> IDENTIFIER_CODEC = Identifier.CODEC.xmap(LegacyIds::migrate, id -> id);

    public static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = resourceKeyCodec(IAspect.REGISTRY_KEY);

    public static final Codec<WandVis> WAND_VIS_CODEC = Codec.unboundedMap(ASPECT_KEY_CODEC, ExtraCodecs.NON_NEGATIVE_INT).xmap(WandVis::new, WandVis::centivis);

    private LegacyIds() {}

    public static Identifier migrate(Identifier id) {
        return LEGACY_NAMESPACE.equals(id.getNamespace()) ? Identifier.fromNamespaceAndPath(TCIds.MODID, migratePath(id.getPath())) : id;
    }

    private static String migratePath(String path) {
        if (!path.contains(LEGACY_NAMESPACE)) {
            return path;
        }
        String[] segments = path.split("/");
        StringBuilder migrated = new StringBuilder(path.length());
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                migrated.append('/');
            }
            migrated.append(LEGACY_NAMESPACE.equals(segments[i]) ? TCIds.MODID : segments[i]);
        }
        return migrated.toString();
    }

    public static <T> Codec<ResourceKey<T>> resourceKeyCodec(ResourceKey<? extends Registry<T>> registry) {
        return IDENTIFIER_CODEC.xmap(id -> ResourceKey.create(registry, id), ResourceKey::identifier);
    }
}
