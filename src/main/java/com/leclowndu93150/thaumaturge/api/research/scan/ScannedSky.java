package com.leclowndu93150.thaumaturge.api.research.scan;

/**
 * A scan that hit nothing, which sky scans read as looking at the sky.
 *
 * @since 1.0.0
 */
public record ScannedSky() implements ScanTarget {
    static final ScannedSky INSTANCE = new ScannedSky();
}
