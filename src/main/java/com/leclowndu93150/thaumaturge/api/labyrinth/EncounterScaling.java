package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * How an encounter grows with the number of players in the boss hall. Each participant beyond the first adds the given fraction of base max health and attack damage, up to
 * {@code maxCountedParticipants} players.
 *
 * @param healthPerParticipant   extra max health per additional participant, as a fraction of the base value
 * @param damagePerParticipant   extra attack damage per additional participant, as a fraction of the base value
 * @param maxCountedParticipants the participant count at which scaling stops
 * @since 1.0.0
 */
public record EncounterScaling(float healthPerParticipant, float damagePerParticipant, int maxCountedParticipants) {
    /**
     * Scaling used when an encounter does not specify one: half the base health and a tenth of the base damage per extra participant, counting at most five.
     */
    public static final EncounterScaling DEFAULT = new EncounterScaling(0.5F, 0.1F, 5);
    /**
     * Codec for the {@code scaling} object of an encounter entry. Every field is optional and falls back to {@link #DEFAULT}.
     */
    public static final Codec<EncounterScaling> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                            .optionalFieldOf("health_per_participant", DEFAULT.healthPerParticipant)
                            .forGetter(EncounterScaling::healthPerParticipant),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                            .optionalFieldOf("damage_per_participant", DEFAULT.damagePerParticipant)
                            .forGetter(EncounterScaling::damagePerParticipant),
                    ExtraCodecs.POSITIVE_INT
                            .optionalFieldOf("max_counted_participants", DEFAULT.maxCountedParticipants)
                            .forGetter(EncounterScaling::maxCountedParticipants))
            .apply(instance, EncounterScaling::new));

    /**
     * @param participants the number of players taking part
     * @return the max health multiplier for that many players
     */
    public float healthMultiplier(int participants) {
        return 1.0F + healthPerParticipant * extra(participants);
    }

    /**
     * @param participants the number of players taking part
     * @return the attack damage multiplier for that many players
     */
    public float damageMultiplier(int participants) {
        return 1.0F + damagePerParticipant * extra(participants);
    }

    private int extra(int participants) {
        return Math.max(0, Math.min(participants, maxCountedParticipants) - 1);
    }
}
