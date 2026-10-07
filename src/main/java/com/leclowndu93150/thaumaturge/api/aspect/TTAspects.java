package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Typed handles to the built-in Thaumaturge aspects. Each constant is a {@link ResourceKey}
 * that can be resolved against a {@link net.minecraft.core.HolderLookup.Provider} to obtain
 * the {@link net.minecraft.core.Holder Holder} or value at runtime.
 *
 * <p>Holding a {@code ResourceKey} rather than the value itself means code is safe to
 * reference these constants in {@code <clinit>} and is unaffected by datapack reloads or
 * registration order.
 *
 * <p>Addons should declare their own {@code ResourceKey} constants for any aspects they ship,
 * following the same pattern.
 *
 * @since 1.0.0
 */
public final class TTAspects {
    public static final ResourceKey<IAspect> AER = key("aer");
    public static final ResourceKey<IAspect> TERRA = key("terra");
    public static final ResourceKey<IAspect> IGNIS = key("ignis");
    public static final ResourceKey<IAspect> AQUA = key("aqua");
    public static final ResourceKey<IAspect> ORDO = key("ordo");
    public static final ResourceKey<IAspect> PERDITIO = key("perditio");

    public static final ResourceKey<IAspect> VACUOS = key("vacuos");
    public static final ResourceKey<IAspect> LUX = key("lux");
    public static final ResourceKey<IAspect> MOTUS = key("motus");
    public static final ResourceKey<IAspect> GELUM = key("gelum");
    public static final ResourceKey<IAspect> VITREUS = key("vitreus");
    public static final ResourceKey<IAspect> METALLUM = key("metallum");
    public static final ResourceKey<IAspect> VICTUS = key("victus");
    public static final ResourceKey<IAspect> MORTUUS = key("mortuus");
    public static final ResourceKey<IAspect> POTENTIA = key("potentia");
    public static final ResourceKey<IAspect> PERMUTATIO = key("permutatio");
    public static final ResourceKey<IAspect> PRAECANTATIO = key("praecantatio");
    public static final ResourceKey<IAspect> AURAM = key("auram");
    public static final ResourceKey<IAspect> ALKIMIA = key("alkimia");
    public static final ResourceKey<IAspect> VITIUM = key("vitium");
    public static final ResourceKey<IAspect> TENEBRAE = key("tenebrae");
    public static final ResourceKey<IAspect> ALIENIS = key("alienis");
    public static final ResourceKey<IAspect> VOLATUS = key("volatus");
    public static final ResourceKey<IAspect> HERBA = key("herba");
    public static final ResourceKey<IAspect> INSTRUMENTUM = key("instrumentum");
    public static final ResourceKey<IAspect> FABRICO = key("fabrico");
    public static final ResourceKey<IAspect> MACHINA = key("machina");
    public static final ResourceKey<IAspect> VINCULUM = key("vinculum");
    public static final ResourceKey<IAspect> SPIRITUS = key("spiritus");
    public static final ResourceKey<IAspect> COGNITIO = key("cognitio");
    public static final ResourceKey<IAspect> SENSUS = key("sensus");
    public static final ResourceKey<IAspect> AVERSIO = key("aversio");
    public static final ResourceKey<IAspect> PRAEMUNIO = key("praemunio");
    public static final ResourceKey<IAspect> DESIDERIUM = key("desiderium");
    public static final ResourceKey<IAspect> EXANIMIS = key("exanimis");
    public static final ResourceKey<IAspect> BESTIA = key("bestia");
    public static final ResourceKey<IAspect> HUMANUS = key("humanus");

    /**
     * The six primal aspects in canonical display order: aer, ignis, aqua, terra, ordo, perditio.
     * This order is used by wand vis pools and the arcane workbench crystal slots.
     */
    public static final List<ResourceKey<IAspect>> PRIMALS = List.of(AER, IGNIS, AQUA, TERRA, ORDO, PERDITIO);

    private TTAspects() {}

    private static ResourceKey<IAspect> key(String tag) {
        return ResourceKey.create(IAspect.REGISTRY_KEY, Identifier.fromNamespaceAndPath("thaumaturge", tag));
    }
}
