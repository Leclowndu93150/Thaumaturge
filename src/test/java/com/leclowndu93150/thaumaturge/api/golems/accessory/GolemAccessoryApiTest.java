package com.leclowndu93150.thaumaturge.api.golems.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
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
        assertNull(accessory.behavior());
        assertEquals(2, accessory.healthBonus());
        assertEquals(3, accessory.armorBonus());
        assertTrue(accessory.killCredit());
    }

    @Test
    void behaviorContextExposesOwnerInactiveAndDefensiveState() {
        UUID owner = UUID.randomUUID();
        IGolemAPI golem = mock(IGolemAPI.class);
        when(golem.ownerIdentity()).thenReturn(Optional.of(owner));
        when(golem.isInactive()).thenReturn(true);
        State state = new State();
        GolemAccessory accessory = new GolemAccessory(
                ResourceLocation.fromNamespaceAndPath("test", "behavior"),
                GolemAccessory.Group.NONE,
                0,
                1,
                1,
                1,
                0,
                false,
                new GolemAccessoryBehavior() {});
        GolemAccessoryContext context = new GolemAccessoryContext(golem, accessory, state);
        assertEquals(owner, context.ownerIdentity().orElseThrow());
        assertTrue(context.isInactive());
        CompoundTag copy = state.persistentData();
        copy.putBoolean("changed", true);
        assertFalse(state.persistentData().getBoolean("changed"));
    }

    private static final class State implements GolemAccessoryState {
        private CompoundTag persistent = new CompoundTag();
        private CompoundTag synchronizedData = new CompoundTag();

        @Override
        public CompoundTag persistentData() {
            return persistent.copy();
        }

        @Override
        public void setPersistentData(CompoundTag data) {
            persistent = data.copy();
        }

        @Override
        public CompoundTag synchronizedData() {
            return synchronizedData.copy();
        }

        @Override
        public void setSynchronizedData(CompoundTag data) {
            synchronizedData = data.copy();
        }

        @Override
        public void clear() {
            persistent = new CompoundTag();
            synchronizedData = new CompoundTag();
        }
    }
}
