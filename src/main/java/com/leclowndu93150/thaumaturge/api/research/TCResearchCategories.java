package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.ResourceKey;

/**
 * Typed registry keys for the built-in research categories shipped by Thaumaturge.
 *
 * <p>Addons may reference these keys to attach research entries to one of the canonical
 * categories without depending on the concrete implementation.
 *
 * @since 1.0.0
 */
public final class TCResearchCategories {
    public static final ResourceKey<IResearchCategory> BASICS = key("basics");
    public static final ResourceKey<IResearchCategory> AUROMANCY = key("auromancy");
    public static final ResourceKey<IResearchCategory> ALCHEMY = key("alchemy");
    public static final ResourceKey<IResearchCategory> ARTIFICE = key("artifice");
    public static final ResourceKey<IResearchCategory> INFUSION = key("infusion");
    public static final ResourceKey<IResearchCategory> GOLEMANCY = key("golemancy");
    public static final ResourceKey<IResearchCategory> ELDRITCH = key("eldritch");

    private TCResearchCategories() {}

    private static ResourceKey<IResearchCategory> key(String path) {
        return ResourceKey.create(IResearchCategory.REGISTRY_KEY, TCIds.rl(path));
    }
}
