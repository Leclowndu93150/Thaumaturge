package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record NoteColorTint(int fallback) implements ItemTintSource {
    public static final MapCodec<NoteColorTint> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("fallback", 0x999999).forGetter(NoteColorTint::fallback)).apply(instance, NoteColorTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        ResearchNoteData data = stack.get(TCDataComponents.RESEARCH_NOTE.get());
        return ARGB.opaque(data == null ? fallback : data.color());
    }

    @Override
    public MapCodec<NoteColorTint> type() {
        return MAP_CODEC;
    }
}
