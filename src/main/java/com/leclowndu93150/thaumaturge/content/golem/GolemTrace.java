package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.Thaumaturge;

/**
 * Temporary server- and client-side golem diagnostics. Toggle this single constant and rebuild
 * to enable/disable golem task, seal, navigation, packet/interpolation, and movement traces.
 *
 * <p>Messages intentionally use INFO so they appear in logs/latest.log without
 * editing the game's logging configuration. Leave this false for normal builds.
 */
public final class GolemTrace {
    public static final boolean DEBUG_TRACE = false;
    // The original task/seal debug prints produce tens of thousands of log lines
    // per minute. Turn this on only to inspect individual task filters.
    public static final boolean VERBOSE_TASK_TRACE = false;

    private GolemTrace() {}

    public static void log(String format, Object... args) {
        if (DEBUG_TRACE && (VERBOSE_TASK_TRACE || !format.startsWith("[golem-task]"))) {
            Thaumaturge.LOGGER.info(format, args);
        }
    }
}
