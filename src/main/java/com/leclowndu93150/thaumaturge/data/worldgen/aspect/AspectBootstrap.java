package com.leclowndu93150.thaumaturge.data.worldgen.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
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
        primal(ctx, TCAspects.AER, 0xFFFF7E, "e", Aspect.DEFAULT_BLEND);
        primal(ctx, TCAspects.TERRA, 0x56C000, "2", Aspect.DEFAULT_BLEND);
        primal(ctx, TCAspects.IGNIS, 0xFF5A01, "c", Aspect.DEFAULT_BLEND);
        primal(ctx, TCAspects.AQUA, 0x3CD4FC, "3", Aspect.DEFAULT_BLEND);
        primal(ctx, TCAspects.ORDO, 0xD5D4EC, "7", Aspect.DEFAULT_BLEND);
        primal(ctx, TCAspects.PERDITIO, 0x404040, "8", Aspect.CONTRAST_BLEND);

        compound(ctx, TCAspects.VACUOS, 0x888888, Aspect.CONTRAST_BLEND, TCAspects.AER, TCAspects.PERDITIO);
        compound(ctx, TCAspects.LUX, 0xFFFF80, Aspect.DEFAULT_BLEND, TCAspects.AER, TCAspects.IGNIS);
        compound(ctx, TCAspects.MOTUS, 0xCDCDF4, Aspect.DEFAULT_BLEND, TCAspects.AER, TCAspects.ORDO);
        compound(ctx, TCAspects.GELUM, 0xE1FFFF, Aspect.DEFAULT_BLEND, TCAspects.IGNIS, TCAspects.PERDITIO);
        compound(ctx, TCAspects.VITREUS, 0x80FFFF, Aspect.DEFAULT_BLEND, TCAspects.TERRA, TCAspects.AER);
        compound(ctx, TCAspects.METALLUM, 0xB5B5CD, Aspect.DEFAULT_BLEND, TCAspects.TERRA, TCAspects.ORDO);
        compound(ctx, TCAspects.VICTUS, 0xDE0005, Aspect.DEFAULT_BLEND, TCAspects.TERRA, TCAspects.AQUA);
        compound(ctx, TCAspects.MORTUUS, 0x6A0005, Aspect.DEFAULT_BLEND, TCAspects.VICTUS, TCAspects.PERDITIO);
        compound(ctx, TCAspects.POTENTIA, 0xC0FFFF, Aspect.DEFAULT_BLEND, TCAspects.ORDO, TCAspects.IGNIS);
        compound(ctx, TCAspects.PERMUTATIO, 0x578357, Aspect.DEFAULT_BLEND, TCAspects.PERDITIO, TCAspects.ORDO);
        compound(ctx, TCAspects.PRAECANTATIO, 0xCF00FF, Aspect.DEFAULT_BLEND, TCAspects.POTENTIA, TCAspects.AER);
        compound(ctx, TCAspects.AURAM, 0xFFC0FF, Aspect.DEFAULT_BLEND, TCAspects.PRAECANTATIO, TCAspects.AER);
        compound(ctx, TCAspects.ALKIMIA, 0x23AE1D, Aspect.DEFAULT_BLEND, TCAspects.PRAECANTATIO, TCAspects.AQUA);
        compound(ctx, TCAspects.VITIUM, 0x800080, Aspect.DEFAULT_BLEND, TCAspects.PERDITIO, TCAspects.PRAECANTATIO);
        compound(ctx, TCAspects.TENEBRAE, 0x222222, Aspect.DEFAULT_BLEND, TCAspects.VACUOS, TCAspects.LUX);
        compound(ctx, TCAspects.ALIENIS, 0x804000, Aspect.DEFAULT_BLEND, TCAspects.VACUOS, TCAspects.TENEBRAE);
        compound(ctx, TCAspects.VOLATUS, 0xE7E7D7, Aspect.DEFAULT_BLEND, TCAspects.AER, TCAspects.MOTUS);
        compound(ctx, TCAspects.HERBA, 0x01AC00, Aspect.DEFAULT_BLEND, TCAspects.VICTUS, TCAspects.TERRA);
        compound(ctx, TCAspects.INSTRUMENTUM, 0x4040AE, Aspect.DEFAULT_BLEND, TCAspects.METALLUM, TCAspects.POTENTIA);
        compound(ctx, TCAspects.FABRICO, 0x809D80, Aspect.DEFAULT_BLEND, TCAspects.PERMUTATIO, TCAspects.INSTRUMENTUM);
        compound(ctx, TCAspects.MACHINA, 0x8080A0, Aspect.DEFAULT_BLEND, TCAspects.MOTUS, TCAspects.INSTRUMENTUM);
        compound(ctx, TCAspects.VINCULUM, 0x9A0000, Aspect.DEFAULT_BLEND, TCAspects.MOTUS, TCAspects.PERDITIO);
        compound(ctx, TCAspects.SPIRITUS, 0xEBEBFB, Aspect.DEFAULT_BLEND, TCAspects.VICTUS, TCAspects.MORTUUS);
        compound(ctx, TCAspects.COGNITIO, 0xFFC2BF, Aspect.DEFAULT_BLEND, TCAspects.IGNIS, TCAspects.SPIRITUS);
        compound(ctx, TCAspects.SENSUS, 0xC0FF00, Aspect.DEFAULT_BLEND, TCAspects.AER, TCAspects.SPIRITUS);
        compound(ctx, TCAspects.AVERSIO, 0xC04F50, Aspect.DEFAULT_BLEND, TCAspects.SPIRITUS, TCAspects.PERDITIO);
        compound(ctx, TCAspects.PRAEMUNIO, 0x00C0C0, Aspect.DEFAULT_BLEND, TCAspects.SPIRITUS, TCAspects.TERRA);
        compound(ctx, TCAspects.DESIDERIUM, 0xE6C284, Aspect.DEFAULT_BLEND, TCAspects.SPIRITUS, TCAspects.VACUOS);
        compound(ctx, TCAspects.EXANIMIS, 0x3A4000, Aspect.DEFAULT_BLEND, TCAspects.MOTUS, TCAspects.MORTUUS);
        compound(ctx, TCAspects.BESTIA, 0x9F6409, Aspect.DEFAULT_BLEND, TCAspects.MOTUS, TCAspects.VICTUS);
        compound(ctx, TCAspects.HUMANUS, 0xFFD7C0, Aspect.DEFAULT_BLEND, TCAspects.SPIRITUS, TCAspects.VICTUS);
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
