package com.leclowndu93150.thaumaturge.registry;

import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.Lazy;

public final class TCSoundTypes {
    public static final Lazy<SoundType> GORE = Lazy
            .of(() -> new SoundType(0.5F, 1.0F, TCSounds.GORE.value(), TCSounds.GORE.value(), TCSounds.GORE.value(), TCSounds.GORE.value(), TCSounds.GORE.value()));

    public static final Lazy<SoundType> CRYSTAL = Lazy
            .of(() -> new SoundType(0.5F, 1.0F, TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value(), TCSounds.CRYSTAL.value()));

    public static final Lazy<SoundType> JAR = Lazy.of(() -> new SoundType(0.5F, 1.0F, TCSounds.JAR.value(), TCSounds.JAR.value(), TCSounds.JAR.value(), TCSounds.JAR.value(), TCSounds.JAR.value()));

    public static final Lazy<SoundType> URN = Lazy
            .of(() -> new SoundType(0.5F, 1.5F, TCSounds.URNBREAK.value(), TCSounds.URNBREAK.value(), TCSounds.URNBREAK.value(), TCSounds.URNBREAK.value(), TCSounds.URNBREAK.value()));

    private TCSoundTypes() {}
}
