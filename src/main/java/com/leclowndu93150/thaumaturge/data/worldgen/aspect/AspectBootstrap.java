package com.leclowndu93150.thaumaturge.data.worldgen.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class AspectBootstrap {
    private AspectBootstrap() {}

    public static void bootstrap(BootstrapContext<IAspect> ctx) {
        primal(ctx, TTAspects.AER, 0xFFFF7E, "e", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.TERRA, 0x56C000, "2", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.IGNIS, 0xFF5A01, "c", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.AQUA, 0x3CD4FC, "3", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.ORDO, 0xD5D4EC, "7", Aspect.DEFAULT_BLEND);
        primal(ctx, TTAspects.PERDITIO, 0x404040, "8", Aspect.CONTRAST_BLEND);

        compound(ctx, TTAspects.VACUOS, 0x888888, Aspect.CONTRAST_BLEND, TTAspects.AER, TTAspects.PERDITIO);
        compound(ctx, TTAspects.LUX, 0xFFFF80, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.IGNIS);
        compound(ctx, TTAspects.MOTUS, 0xCDCDF4, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.ORDO);
        compound(ctx, TTAspects.GELUM, 0xE1FFFF, Aspect.DEFAULT_BLEND, TTAspects.IGNIS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.VITREUS, 0x80FFFF, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.AER);
        compound(ctx, TTAspects.METALLUM, 0xB5B5CD, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.ORDO);
        compound(ctx, TTAspects.VICTUS, 0xDE0005, Aspect.DEFAULT_BLEND, TTAspects.TERRA, TTAspects.AQUA);
        compound(ctx, TTAspects.MORTUUS, 0x6A0005, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.POTENTIA, 0xC0FFFF, Aspect.DEFAULT_BLEND, TTAspects.ORDO, TTAspects.IGNIS);
        compound(ctx, TTAspects.PERMUTATIO, 0x578357, Aspect.DEFAULT_BLEND, TTAspects.PERDITIO, TTAspects.ORDO);
        compound(ctx, TTAspects.PRAECANTATIO, 0xCF00FF, Aspect.DEFAULT_BLEND, TTAspects.POTENTIA, TTAspects.AER);
        compound(ctx, TTAspects.AURAM, 0xFFC0FF, Aspect.DEFAULT_BLEND, TTAspects.PRAECANTATIO, TTAspects.AER);
        compound(ctx, TTAspects.ALKIMIA, 0x23AE1D, Aspect.DEFAULT_BLEND, TTAspects.PRAECANTATIO, TTAspects.AQUA);
        compound(ctx, TTAspects.VITIUM, 0x800080, Aspect.DEFAULT_BLEND, TTAspects.PERDITIO, TTAspects.PRAECANTATIO);
        compound(ctx, TTAspects.TENEBRAE, 0x222222, Aspect.DEFAULT_BLEND, TTAspects.VACUOS, TTAspects.LUX);
        compound(ctx, TTAspects.ALIENIS, 0x804000, Aspect.DEFAULT_BLEND, TTAspects.VACUOS, TTAspects.TENEBRAE);
        compound(ctx, TTAspects.VOLATUS, 0xE7E7D7, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.MOTUS);
        compound(ctx, TTAspects.HERBA, 0x01AC00, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.TERRA);
        compound(ctx, TTAspects.INSTRUMENTUM, 0x4040AE, Aspect.DEFAULT_BLEND, TTAspects.METALLUM, TTAspects.POTENTIA);
        compound(ctx, TTAspects.FABRICO, 0x809D80, Aspect.DEFAULT_BLEND, TTAspects.PERMUTATIO, TTAspects.INSTRUMENTUM);
        compound(ctx, TTAspects.MACHINA, 0x8080A0, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.INSTRUMENTUM);
        compound(ctx, TTAspects.VINCULUM, 0x9A0000, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.SPIRITUS, 0xEBEBFB, Aspect.DEFAULT_BLEND, TTAspects.VICTUS, TTAspects.MORTUUS);
        compound(ctx, TTAspects.COGNITIO, 0xFFC2BF, Aspect.DEFAULT_BLEND, TTAspects.IGNIS, TTAspects.SPIRITUS);
        compound(ctx, TTAspects.SENSUS, 0xC0FF00, Aspect.DEFAULT_BLEND, TTAspects.AER, TTAspects.SPIRITUS);
        compound(ctx, TTAspects.AVERSIO, 0xC04F50, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.PERDITIO);
        compound(ctx, TTAspects.PRAEMUNIO, 0x00C0C0, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.TERRA);
        compound(ctx, TTAspects.DESIDERIUM, 0xE6C284, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.VACUOS);
        compound(ctx, TTAspects.EXANIMIS, 0x3A4000, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.MORTUUS);
        compound(ctx, TTAspects.BESTIA, 0x9F6409, Aspect.DEFAULT_BLEND, TTAspects.MOTUS, TTAspects.VICTUS);
        compound(ctx, TTAspects.HUMANUS, 0xFFD7C0, Aspect.DEFAULT_BLEND, TTAspects.SPIRITUS, TTAspects.VICTUS);
    }

    private static void primal(BootstrapContext<IAspect> ctx, ResourceKey<IAspect> key, int color, String chatColor, int blend) {
        ctx.register(key, new Aspect(key.identifier().getPath(), color, List.of(), Optional.of(chatColor),
                Identifier.fromNamespaceAndPath(key.identifier().getNamespace(), "textures/aspects/" + key.identifier().getPath() + ".png"), blend));
    }

    private static void compound(BootstrapContext<IAspect> ctx, ResourceKey<IAspect> key, int color, int blend, ResourceKey<IAspect> componentA, ResourceKey<IAspect> componentB) {
        Holder<IAspect> a = ctx.lookup(IAspect.REGISTRY_KEY).getOrThrow(componentA);
        Holder<IAspect> b = ctx.lookup(IAspect.REGISTRY_KEY).getOrThrow(componentB);
        ctx.register(key, new Aspect(key.identifier().getPath(), color, List.of(a, b), Optional.empty(),
                Identifier.fromNamespaceAndPath(key.identifier().getNamespace(), "textures/aspects/" + key.identifier().getPath() + ".png"), blend));
    }
}
