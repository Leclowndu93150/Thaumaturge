package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Fields every encounter shares. Encounter codecs include {@link #CODEC} as flat fields next to their own.
 *
 * @param name         the encounter name shown on the boss bar and in announcements
 * @param announcement the title shown to everyone in the maze when the door opens
 * @param arena        an optional structure template placed over the hall floor while the lock charges
 * @param reward       the loot table each eligible player rolls from the boss reliquary
 * @param scaling      how the encounter grows with participants
 * @param victoryDelay ticks between the last primary death and the reward appearing
 * @param barColor     the colour of the shared boss bar
 * @since 1.0.0
 */
public record EncounterSettings(
        Component name,
        Component announcement,
        Optional<ResourceLocation> arena,
        ResourceKey<LootTable> reward,
        EncounterScaling scaling,
        int victoryDelay,
        BossEvent.BossBarColor barColor) {
    /**
     * Ticks between the last primary entity dying and the maze being marked conquered, when an encounter does not set its own delay.
     */
    public static final int DEFAULT_VICTORY_DELAY = 20;
    /**
     * Codec for the settings fields shared by every encounter type. Encounter type codecs include it alongside their own fields.
     */
    public static final MapCodec<EncounterSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(EncounterSettings::name),
                    ComponentSerialization.CODEC.fieldOf("announcement").forGetter(EncounterSettings::announcement),
                    ResourceLocation.CODEC.optionalFieldOf("arena").forGetter(EncounterSettings::arena),
                    ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("reward").forGetter(EncounterSettings::reward),
                    EncounterScaling.CODEC
                            .optionalFieldOf("scaling", EncounterScaling.DEFAULT)
                            .forGetter(EncounterSettings::scaling),
                    ExtraCodecs.NON_NEGATIVE_INT
                            .optionalFieldOf("victory_delay", DEFAULT_VICTORY_DELAY)
                            .forGetter(EncounterSettings::victoryDelay),
                    Codec.STRING
                            .xmap(BossEvent.BossBarColor::byName, BossEvent.BossBarColor::getName)
                            .optionalFieldOf("bar_color", BossEvent.BossBarColor.PURPLE)
                            .forGetter(EncounterSettings::barColor))
            .apply(instance, EncounterSettings::new));
}
