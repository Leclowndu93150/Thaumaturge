package com.leclowndu93150.thaumaturge.api.research.scan;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

/**
 * A subject the thaumometer can scan: it recognises targets and names the research a scan grants.
 *
 * <p>Register code-driven subjects with {@link ScanningManager#register} during mod construction. Subjects that only list blocks,
 * items or entity types are better written as {@link ScanEntry} datapack entries. Every method runs on the logical side that performs
 * the scan; {@link #onScanned} runs on the server.
 *
 * @since 1.0.0
 */
public interface IScannable {
    /**
     * @param player the scanning player
     * @param target what the thaumometer points at
     * @return whether this subject applies to the target
     */
    boolean matches(Player player, ScanTarget target);

    /**
     * Names the research this scan advances. A subject that returns null has side effects only: it stays scannable for as long as
     * {@link #matches} holds, runs {@link #onScanned} and suppresses the "known object" feedback.
     *
     * @param player the scanning player
     * @param target the matched target
     * @return the research key, or null
     */
    @Nullable
    ResourceLocation research(Player player, ScanTarget target);

    /**
     * Runs on the server after a successful scan.
     *
     * @param player the scanning player
     * @param target the matched target
     */
    default void onScanned(Player player, ScanTarget target) {}

    /**
     * @param player the scanning player
     * @param target the matched target
     * @return whether the target may be scanned again once its research is known; false by default
     */
    default boolean rescannable(Player player, ScanTarget target) {
        return false;
    }

    /**
     * Refuses a matched scan with a message, for example when the player lacks the knowledge to understand the target.
     *
     * @param player the scanning player
     * @param target the matched target
     * @return the message shown instead of scanning, or null to allow the scan
     */
    default @Nullable Component refusal(Player player, ScanTarget target) {
        return null;
    }
}
