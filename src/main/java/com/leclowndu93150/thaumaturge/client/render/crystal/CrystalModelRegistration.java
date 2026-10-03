package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CrystalModelRegistration {
    public static final Identifier CRYSTAL_MODEL_ID = TCIds.rl("crystal");

    private CrystalModelRegistration() {}

    @SubscribeEvent
    public static void onRegisterBlockStateModels(RegisterBlockStateModels event) {
        event.registerModel(CRYSTAL_MODEL_ID, CrystalUnbakedModel.CODEC);
    }
}
