package com.leclowndu93150.thaumaturge.registry;

import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;

public final class TTSoundTypes {
    public static final SoundType GORE = new DeferredSoundType(
            0.5F,
            1.0F,
            TTSounds.GORE::value,
            TTSounds.GORE::value,
            TTSounds.GORE::value,
            TTSounds.GORE::value,
            TTSounds.GORE::value);

    public static final SoundType CRYSTAL = new DeferredSoundType(
            0.5F,
            1.0F,
            TTSounds.CRYSTAL::value,
            TTSounds.CRYSTAL::value,
            TTSounds.CRYSTAL::value,
            TTSounds.CRYSTAL::value,
            TTSounds.CRYSTAL::value);

    public static final SoundType JAR = new DeferredSoundType(
            0.5F,
            1.0F,
            TTSounds.JAR::value,
            TTSounds.JAR::value,
            TTSounds.JAR::value,
            TTSounds.JAR::value,
            TTSounds.JAR::value);

    public static final SoundType URN = new DeferredSoundType(
            0.5F,
            1.5F,
            TTSounds.URNBREAK::value,
            TTSounds.URNBREAK::value,
            TTSounds.URNBREAK::value,
            TTSounds.URNBREAK::value,
            TTSounds.URNBREAK::value);

    private TTSoundTypes() {}
}
