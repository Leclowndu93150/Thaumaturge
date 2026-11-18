package com.leclowndu93150.thaumaturge.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.Test;

class PrimalCrusherGroundModelTest {
    @Test
    void groundPoseLaysTheSpriteFlatAndInheritsAllHandheldTransforms() throws Exception {
        TTModelProvider provider = new TTModelProvider(mock(PackOutput.class));
        Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();
        Field output = TTModelProvider.class.getDeclaredField("modelOutput");
        output.setAccessible(true);
        output.set(provider, (BiConsumer<ResourceLocation, Supplier<JsonElement>>) models::put);
        Method register = TTModelProvider.class.getDeclaredMethod("groundedHandheldItem", Item.class);
        register.setAccessible(true);
        Item crusher = TTItems.PRIMAL_CRUSHER.get();
        register.invoke(provider, crusher);

        JsonObject model =
                models.get(ModelLocationUtils.getModelLocation(crusher)).get().getAsJsonObject();
        assertEquals("minecraft:item/handheld", model.get("parent").getAsString());
        assertEquals(
                "thaumaturge:item/primal_crusher",
                model.getAsJsonObject("textures").get("layer0").getAsString());
        JsonObject display = model.getAsJsonObject("display");
        assertEquals(Set.of("ground"), display.keySet());
        JsonObject ground = display.getAsJsonObject("ground");
        assertEquals("[90.0,0.0,0.0]", ground.get("rotation").toString());
        assertEquals("[0.0,2.0,0.0]", ground.get("translation").toString());
        assertEquals("[0.5,0.5,0.5]", ground.get("scale").toString());
    }
}
