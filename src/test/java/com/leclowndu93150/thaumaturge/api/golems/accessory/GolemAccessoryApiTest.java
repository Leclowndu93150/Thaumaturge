package com.leclowndu93150.thaumaturge.api.golems.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class GolemAccessoryApiTest {
    @Test
    void legacyConstructorRemainsStatOnly() {
        GolemAccessory accessory = new GolemAccessory(
                ResourceLocation.fromNamespaceAndPath("test", "stat_only"),
                GolemAccessory.Group.HAT,
                2,
                1.1F,
                1.2F,
                0.8F,
                3,
                true);
        assertTrue(accessory.behavior().isEmpty());
        assertEquals(2, accessory.healthBonus());
        assertEquals(3, accessory.armorBonus());
        assertTrue(accessory.killCredit());
    }
}
