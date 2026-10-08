package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public record LabyrinthGameplay(
        ResourceKey<LootTable> keyRoomLoot, boolean glyphHints, boolean tabletResonance, BlockState arrivalPad) {
    public static final Codec<LabyrinthGameplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceKey.codec(Registries.LOOT_TABLE)
                            .fieldOf("key_room_loot")
                            .forGetter(LabyrinthGameplay::keyRoomLoot),
                    Codec.BOOL.optionalFieldOf("glyph_hints", true).forGetter(LabyrinthGameplay::glyphHints),
                    Codec.BOOL.optionalFieldOf("tablet_resonance", true).forGetter(LabyrinthGameplay::tabletResonance),
                    BlockState.CODEC.fieldOf("arrival_pad").forGetter(LabyrinthGameplay::arrivalPad))
            .apply(instance, LabyrinthGameplay::new));
}
