package com.leclowndu93150.thaumaturge.data.worldgen.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class AspectBootstrap {
    public static final List<AspectDefinition> DEFINITIONS = List.of(
            primal(TTAspects.AER, 0xFFFF7E, "e", Aspect.DEFAULT_BLEND),
            primal(TTAspects.TERRA, 0x56C000, "2", Aspect.DEFAULT_BLEND),
            primal(TTAspects.IGNIS, 0xFF5A01, "c", Aspect.DEFAULT_BLEND),
            primal(TTAspects.AQUA, 0x3CD4FC, "3", Aspect.DEFAULT_BLEND),
            primal(TTAspects.ORDO, 0xD5D4EC, "7", Aspect.DEFAULT_BLEND),
            primal(TTAspects.PERDITIO, 0x404040, "8", Aspect.CONTRAST_BLEND),
            compound(TTAspects.VACUOS, 0x888888, Aspect.CONTRAST_BLEND, TTAspects.AER, TTAspects.PERDITIO),
            compound(TTAspects.LUX, 0xFFFF80, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.IGNIS),
            compound(TTAspects.MOTUS, 0xCDCDF4, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.ORDO),
            compound(TTAspects.GELUM, 0xE1FFFF, Aspect.DEFAULT_BLEND, TTAspects.IGNIS, TTAspects.PERDITIO),
            compound(TTAspects.VITREUS, 0x80FFFF, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.AER),
            compound(TTAspects.METALLUM, 0xB5B5CD, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.ORDO),
            compound(TTAspects.VICTUS, 0xDE0005, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.AQUA),
            compound(TTAspects.PERMUTATIO, 0x578357, Aspect.DEFAULT_BLEND, TTAspects.PERDITIO, TTAspects.ORDO),
            compound(TTAspects.POTENTIA, 0xC0FFFF, Aspect.DEFAULT_BLEND, TTAspects.ORDO, TTAspects.IGNIS),
            compound(TTAspects.VOLATUS, 0xE7E7D7, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.MOTUS),
            compound(TTAspects.VINCULUM, 0x9A0000, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.PERDITIO),
            compound(TTAspects.TENEBRAE, 0x222222, Aspect.DEFAULT_BLEND, TTAspects.VACUOS, TTAspects.LUX),
            compound(TTAspects.MORTUUS, 0x6A0005, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.PERDITIO),
            compound(TTAspects.HERBA, 0x01AC00, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.TERRA),
            compound(TTAspects.PRAECANTATIO, 0xCF00FF, Aspect.DEFAULT_BLEND, TTAspects.POTENTIA, TTAspects.AER),
            compound(TTAspects.INSTRUMENTUM, 0x4040AE, Aspect.DEFAULT_BLEND, TTAspects.METALLUM, TTAspects.POTENTIA),
            compound(TTAspects.ALIENIS, 0x804000, Aspect.DEFAULT_BLEND, TTAspects.VACUOS, TTAspects.TENEBRAE),
            compound(TTAspects.SPIRITUS, 0xEBEBFB, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.MORTUUS),
            compound(TTAspects.AURAM, 0xFFC0FF, Aspect.DEFAULT_BLEND, TTAspects.PRAECANTATIO, TTAspects.AER),
            compound(TTAspects.ALKIMIA, 0x23AE1D, Aspect.DEFAULT_BLEND, TTAspects.PRAECANTATIO, TTAspects.AQUA),
            compound(TTAspects.VITIUM, 0x800080, Aspect.DEFAULT_BLEND, TTAspects.PERDITIO, TTAspects.PRAECANTATIO),
            compound(TTAspects.FABRICO, 0x809D80, Aspect.DEFAULT_BLEND, TTAspects.PERMUTATIO, TTAspects.INSTRUMENTUM),
            compound(TTAspects.MACHINA, 0x8080A0, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.INSTRUMENTUM),
            compound(TTAspects.EXANIMIS, 0x3A4000, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.MORTUUS),
            compound(TTAspects.BESTIA, 0x9F6409, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.VICTUS),
            compound(TTAspects.COGNITIO, 0xFFC2BF, Aspect.DEFAULT_BLEND, TTAspects.IGNIS, TTAspects.SPIRITUS),
            compound(TTAspects.SENSUS, 0xC0FF00, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.SPIRITUS),
            compound(TTAspects.AVERSIO, 0xC04F50, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.PERDITIO),
            compound(TTAspects.PRAEMUNIO, 0x00C0C0, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.TERRA),
            compound(TTAspects.DESIDERIUM, 0xE6C284, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.VACUOS),
            compound(TTAspects.HUMANUS, 0xFFD7C0, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.VICTUS));

    private AspectBootstrap() {}

    public static void bootstrap(BootstrapContext<IAspect> ctx) {
        HolderGetter<IAspect> aspects = ctx.lookup(IAspect.REGISTRY_KEY);
        for (AspectDefinition definition : DEFINITIONS) {
            ctx.register(definition.key(), definition.create(aspects));
        }
    }

    private static AspectDefinition primal(ResourceKey<IAspect> key, int color, String chatColor, int blend) {
        return new AspectDefinition(key, color, Optional.of(chatColor), blend, List.of());
    }

    private static AspectDefinition compound(
            ResourceKey<IAspect> key,
            int color,
            int blend,
            ResourceKey<IAspect> componentA,
            ResourceKey<IAspect> componentB) {
        return new AspectDefinition(key, color, Optional.empty(), blend, List.of(componentA, componentB));
    }
}
