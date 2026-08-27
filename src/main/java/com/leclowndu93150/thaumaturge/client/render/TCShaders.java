package com.leclowndu93150.thaumaturge.client.render;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCShaders {
    private static ShaderInstance ender;
    private static ShaderInstance occludingEffect;
    private static ShaderInstance fx;
    private static ShaderInstance fxAlphaTest;
    private static ShaderInstance portal;
    private static ShaderInstance voidStream;
    private static ShaderInstance wardAdd;

    private TCShaders() {}

    public static ShaderInstance ender() {
        return ender;
    }

    public static ShaderInstance occludingEffect() {
        return occludingEffect;
    }

    public static ShaderInstance fx() {
        return fx;
    }

    public static ShaderInstance fxAlphaTest() {
        return fxAlphaTest;
    }

    public static ShaderInstance portal() {
        return portal;
    }

    public static ShaderInstance voidStream() {
        return voidStream;
    }

    public static ShaderInstance wardAdd() {
        return wardAdd;
    }

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), TCIds.rl("tc_ender"), DefaultVertexFormat.POSITION),
                shader -> ender = shader);
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), TCIds.rl("tc_fx"), DefaultVertexFormat.PARTICLE),
                shader -> fx = shader);
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(), TCIds.rl("tc_fx_alpha_test"), DefaultVertexFormat.PARTICLE),
                shader -> fxAlphaTest = shader);
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(), TCIds.rl("tc_occluding_effect"), DefaultVertexFormat.NEW_ENTITY),
                shader -> occludingEffect = shader);
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(), TCIds.rl("tc_portal"), DefaultVertexFormat.POSITION_TEX),
                shader -> portal = shader);
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(), TCIds.rl("void_stream"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shader -> voidStream = shader);
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(), TCIds.rl("ward_add"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shader -> wardAdd = shader);
    }
}
