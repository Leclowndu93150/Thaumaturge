package com.leclowndu93150.thaumaturge.registry;

import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.Lazy;

public final class TTSoundTypes {
    public static final Lazy<SoundType> GORE = Lazy
            .of(() -> new SoundType(0.5F, 1.0F, TTSounds.GORE.value(), TTSounds.GORE.value(), TTSounds.GORE.value(), TTSounds.GORE.value(), TTSounds.GORE.value()));

    public static final Lazy<SoundType> CRYSTAL = Lazy
            .of(() -> new SoundType(0.5F, 1.0F, TTSounds.CRYSTAL.value(), TTSounds.CRYSTAL.value(), TTSounds.CRYSTAL.value(), TTSounds.CRYSTAL.value(), TTSounds.CRYSTAL.value()));

    public static final Lazy<SoundType> JAR = Lazy.of(() -> new SoundType(0.5F, 1.0F, TTSounds.JAR.value(), TTSounds.JAR.value(), TTSounds.JAR.value(), TTSounds.JAR.value(), TTSounds.JAR.value()));

    public static final Lazy<SoundType> URN = Lazy
            .of(() -> new SoundType(0.5F, 1.5F, TTSounds.URNBREAK.value(), TTSounds.URNBREAK.value(), TTSounds.URNBREAK.value(), TTSounds.URNBREAK.value(), TTSounds.URNBREAK.value()));

    private TTSoundTypes() {}
}
