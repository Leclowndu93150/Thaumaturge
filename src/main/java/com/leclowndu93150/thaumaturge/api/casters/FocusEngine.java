package com.leclowndu93150.thaumaturge.api.casters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for resolving focus elements and executing spells.
 *
 * <p>A spell is a {@link FocusPackage}: immutable data referencing registered
 * {@link FocusElement} behaviors. {@link #cast(LivingEntity, FocusPackage)} starts a spell
 * from its caster; {@link #run(Level, FocusPackage, CastStreams)} resumes one from an
 * intermediary such as a projectile, cloud, or mine.
 *
 * @apiNote {@link #bindRegistry(Registry)} is called exactly once by the mod during init.
 *          Addons never call it; they register {@link FocusElementType} values into the
 *          registry instead.
 * @since 1.0.0
 */
public final class FocusEngine {
    private static Registry<FocusElementType> registry;

    private FocusEngine() {}

    /**
     * Binds the focus element registry this engine resolves elements from.
     *
     * @param elementRegistry the built focus element registry
     */
    public static void bindRegistry(Registry<FocusElementType> elementRegistry) {
        registry = elementRegistry;
    }

    /**
     * The registration entry of a focus element.
     *
     * @param key the element id
     * @return the entry, or null when no element is registered under {@code key}
     */
    public static @Nullable FocusElementType type(Identifier key) {
        return registry().getValue(key);
    }

    /**
     * The behavior singleton of a focus element.
     *
     * @param key the element id
     * @return the behavior, or null when no element is registered under {@code key}
     */
    public static @Nullable FocusElement element(Identifier key) {
        FocusElementType type = registry().getValue(key);
        return type != null ? type.element() : null;
    }

    /**
     * The icon registered for a focus element.
     *
     * @param key the element id
     * @return the icon texture, or null when no element is registered under {@code key}
     */
    public static @Nullable Identifier icon(Identifier key) {
        FocusElementType type = registry().getValue(key);
        return type != null ? type.icon() : null;
    }

    /**
     * The icon tint registered for a focus element.
     *
     * @param key the element id
     * @return the packed {@code 0xRRGGBB} color, or white when no element is registered
     *         under {@code key}
     */
    public static int color(Identifier key) {
        FocusElementType type = registry().getValue(key);
        return type != null ? type.color() : 0xFFFFFF;
    }

    /**
     * Casts a spell from its caster's eyes along the look vector.
     *
     * @param caster the casting entity
     * @param pack   the spell to cast
     */
    public static void cast(LivingEntity caster, FocusPackage pack) {
        cast(caster, pack, CastStreams.fromCaster(caster));
    }

    /**
     * Casts a spell with an explicit origin: fires {@link FocusEffect#onCast(LivingEntity)}
     * on every effect, then executes the spell against the origin streams.
     *
     * @param caster the casting entity
     * @param pack   the spell to cast
     * @param origin the initial streams
     */
    public static void cast(LivingEntity caster, FocusPackage pack, CastStreams origin) {
        for (Identifier effectId : effectIds(pack.units())) {
            if (element(effectId) instanceof FocusEffect effect) {
                effect.onCast(caster);
            }
        }
        CastExecutor.run(caster.level(), pack, caster, origin, UUID.randomUUID(), new HashSet<>());
    }

    /**
     * Resumes a spell from an intermediary. The caster resolves lazily from the package's
     * caster id.
     *
     * @param level   the level the spell executes in
     * @param pack    the spell remainder to execute
     * @param streams the streams supplied to the first node
     */
    public static void run(Level level, FocusPackage pack, CastStreams streams) {
        run(level, pack, null, streams);
    }

    /**
     * Resumes a spell from an intermediary with an already-resolved caster.
     *
     * @param level   the level the spell executes in
     * @param pack    the spell remainder to execute
     * @param caster  the casting entity, or null to resolve from the package's caster id
     * @param streams the streams supplied to the first node
     */
    public static void run(Level level, FocusPackage pack, @Nullable LivingEntity caster, CastStreams streams) {
        CastExecutor.run(level, pack, caster, streams, UUID.randomUUID(), new HashSet<>());
    }

    /**
     * Resumes a spell from raw trajectory and target arrays.
     *
     * @param level        the level the spell executes in
     * @param pack         the spell remainder to execute
     * @param trajectories the trajectories supplied to the first node, or null
     * @param targets      the targets supplied to the first node, or null
     */
    public static void run(Level level, FocusPackage pack, Trajectory @Nullable [] trajectories, HitResult @Nullable [] targets) {
        run(level, pack, null, new CastStreams(trajectories, targets));
    }

    /**
     * Collects the element ids of every effect in a spell, descending into split branches.
     *
     * @param pack the spell to inspect
     * @return the effect ids in encounter order; empty when none
     */
    public static List<Identifier> effectIds(FocusPackage pack) {
        return effectIds(pack.units());
    }

    /**
     * Collects the element ids of every effect in a unit list, descending into split
     * branches.
     *
     * @param units the units to inspect
     * @return the effect ids in encounter order; empty when none
     */
    public static List<Identifier> effectIds(List<FocusUnit> units) {
        List<Identifier> out = new ArrayList<>();
        collectEffectIds(units, out);
        return out;
    }

    private static void collectEffectIds(List<FocusUnit> units, List<Identifier> out) {
        for (FocusUnit unit : units) {
            if (element(unit.element()) instanceof FocusEffect) {
                out.add(unit.element());
            }
            for (FocusPackage branch : unit.branches()) {
                collectEffectIds(branch.units(), out);
            }
        }
    }

    private static Registry<FocusElementType> registry() {
        if (registry == null) {
            throw new IllegalStateException("FocusEngine used before Thaumaturge bound the focus element registry");
        }
        return registry;
    }
}
