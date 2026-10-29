package com.leclowndu93150.thaumaturge.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric bootstrap entrypoint. Thaumaturge's NeoForge systems are not migrated yet.
 */
public final class ThaumaturgeFabric implements ModInitializer {
    public static final String MOD_ID = "thaumaturge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.warn("Thaumaturge Fabric 26.3 port bootstrap loaded; gameplay systems are not migrated yet.");
    }
}
