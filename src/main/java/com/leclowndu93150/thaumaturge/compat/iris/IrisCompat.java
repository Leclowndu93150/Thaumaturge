package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TCIds;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.Nullable;

public final class IrisCompat {
    private static final String IMPLEMENTATION = IrisCompat.class.getName() + "Impl";
    private static final Class<?>[] NO_ARGUMENTS = new Class<?>[0];
    private static final Object[] NO_VALUES = new Object[0];

    private static @Nullable Class<?> implementation;
    private static boolean unavailable;

    private IrisCompat() {}

    public static boolean shadersActive() {
        return invokeBoolean("shadersActive", NO_ARGUMENTS, NO_VALUES);
    }

    /**
     * Gets Iris's translucent particle program for Thaumaturge's particle-format effects. This is the 1.21.1
     * equivalent of assigning {@code IrisProgram.PARTICLES_TRANSLUCENT} to a modern render pipeline.
     */
    public static ShaderInstance particleTranslucentShader() {
        return invoke("particleTranslucentShader", NO_ARGUMENTS, NO_VALUES, ShaderInstance.class);
    }

    /**
     * Returns true only while Iris is applying its translucent-particle shader to Thaumaturge's node atlas.
     *
     * <p>Using the currently bound texture is intentional. Iris applies the shader after the RenderType has already
     * bound its texture, so this remains correct even when Iris buffers and reorders block-entity draws. It also avoids
     * changing the shader-pack particle rules for vanilla particles or unrelated Thaumaturge effects.</p>
     */
    public static boolean isNodeParticleShaderPass(Object shader) {
        return invokeBoolean("isNodeParticleShaderPass", new Class<?>[] {Object.class}, new Object[] {shader});
    }

    /** True while the node atlas is bound, used by Iris fallback-shader compatibility. */
    public static boolean isNodeTextureBound() {
        return invokeBoolean("isNodeTextureBound", NO_ARGUMENTS, NO_VALUES);
    }

    /** Sets the Iris particle alpha cutoff for the duration of a Thaumaturge effect render type. */
    public static void setParticleAlphaTest(float threshold) {
        invoke("setParticleAlphaTest", new Class<?>[] {float.class}, new Object[] {threshold}, Void.class);
    }

    /**
     * Adds Iris's entity render context to deferred visual effects. Effects submitted after the normal entity pass do
     * not pass through Iris's {@code EntityRenderDispatcher} hook, so they need the same wrapper explicitly.
     */
    public static MultiBufferSource entityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive()
                ? invoke(
                        "entityEffectBuffers",
                        new Class<?>[] {MultiBufferSource.class},
                        new Object[] {buffers},
                        MultiBufferSource.class)
                : buffers;
    }

    /**
     * Adds Iris's block-entity render context to deferred visual effects. Effects submitted after the normal block
     * entity pass do not pass through Iris's {@code BlockEntityRenderDispatcher} hook.
     */
    public static MultiBufferSource blockEntityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive()
                ? invoke(
                        "blockEntityEffectBuffers",
                        new Class<?>[] {MultiBufferSource.class},
                        new Object[] {buffers},
                        MultiBufferSource.class)
                : buffers;
    }

    private static boolean invokeBoolean(String method, Class<?>[] parameterTypes, Object[] arguments) {
        return invoke(method, parameterTypes, arguments, Boolean.class);
    }

    private static <T> T invoke(String method, Class<?>[] parameterTypes, Object[] arguments, Class<T> resultType) {
        Class<?> impl = implementation();
        if (impl == null) {
            return resultType == Boolean.class ? resultType.cast(false) : null;
        }
        try {
            Method target = impl.getMethod(method, parameterTypes);
            Object result = target.invoke(null, arguments);
            return resultType == Void.class ? null : resultType.cast(result);
        } catch (ReflectiveOperationException | LinkageError exception) {
            unavailable = true;
            return resultType == Boolean.class ? resultType.cast(false) : null;
        }
    }

    private static @Nullable Class<?> implementation() {
        if (unavailable || !ModList.get().isLoaded(TCIds.IRIS)) {
            return null;
        }
        if (implementation != null) {
            return implementation;
        }
        try {
            implementation = Class.forName(IMPLEMENTATION);
            return implementation;
        } catch (ClassNotFoundException | LinkageError exception) {
            unavailable = true;
            return null;
        }
    }
}
