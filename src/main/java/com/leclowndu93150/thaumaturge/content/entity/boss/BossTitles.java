package com.leclowndu93150.thaumaturge.content.entity.boss;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public record BossTitles(String nameKey, List<String> titles) {
    public static final String SAVE_KEY = "title";

    public int roll(RandomSource random) {
        return random.nextInt(titles.size());
    }

    public int clamp(int title) {
        return Math.floorMod(title, titles.size());
    }

    public Component name(int title, Component trait) {
        return Component.translatable(nameKey, titles.get(clamp(title)), trait);
    }
}
