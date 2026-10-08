package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * A data point inside a labyrinth room template.
 *
 * <p>In a template a marker is a data-mode structure block whose metadata string is the marker's JSON, for example {@code {"type":"thaumaturge:landmark","id":"thaumaturge:arrival"}}. Room
 * templates are parsed once when they are first used, so a marker's fields are read once per template, not once per placement.
 *
 * @implNote {@link #stamp} runs on a worldgen thread. It may only touch the chunk it is given through {@link MarkerStampContext#level()}, and it must not read or write any other world, saved
 *           data or config state. {@link #trigger} runs on the server thread with full level access.
 * @since 1.0.0
 */
public interface LabyrinthMarker {
    /**
     * Dispatches on the {@code type} field through the {@link LabyrinthMarkerType} registry.
     */
    Codec<LabyrinthMarker> CODEC = Codec.lazyInitialized(() -> LabyrinthHelper.markerTypes()
            .byNameCodec()
            .dispatch("type", LabyrinthMarker::type, LabyrinthMarkerType::codec));

    /**
     * @return the registered type of this marker
     */
    LabyrinthMarkerType<?> type();

    /**
     * @return when this marker takes effect
     */
    MarkerPhase phase();

    /**
     * The landmark id recorded for this marker's position when the maze is created. {@link MarkerPhase#LANDMARK} markers only record; other markers may record a landmark and still take
     * effect, as the lock does. Standard ids are in {@link LabyrinthLandmarks}.
     *
     * @return the landmark id, or empty when the position is not recorded
     */
    default Optional<ResourceLocation> landmark() {
        return Optional.empty();
    }

    /**
     * Whether the marker's position is recorded as a wayfinding point that hints toward the current objective.
     *
     * @return true for wayfinding glyphs
     */
    default boolean wayfinding() {
        return false;
    }

    /**
     * The distance at which a player sets off a {@link MarkerPhase#TRIGGER} marker.
     *
     * @return the radius in blocks
     */
    default int triggerRadius() {
        return 0;
    }

    /**
     * Applies a {@link MarkerPhase#STAMP} marker while its chunk generates.
     *
     * @param context the placement context
     * @param pos     the marker's world position, already transformed with the room
     */
    default void stamp(MarkerStampContext context, BlockPos pos) {}

    /**
     * Reports whether a {@link MarkerPhase#TRIGGER} marker may fire now. A marker that answers false stays pending and is asked again on the next
     * check, so it is never used up while its conditions are not met.
     *
     * @param context the trigger context
     * @return true to fire the marker now
     */
    default boolean canTrigger(MarkerTriggerContext context) {
        return true;
    }

    /**
     * Fires a {@link MarkerPhase#TRIGGER} marker once. It is removed from the maze afterwards.
     *
     * @param context the trigger context
     * @param pos     the marker's world position
     */
    default void trigger(MarkerTriggerContext context, BlockPos pos) {}
}
