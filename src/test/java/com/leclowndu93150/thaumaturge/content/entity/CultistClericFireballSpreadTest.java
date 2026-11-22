package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.content.entity.cult.CultSpells;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class CultistClericFireballSpreadTest {
    @Test
    void cultistFireballIsNotSavedAsAnUnprotectedVanillaFireball() throws ReflectiveOperationException {
        // Vanilla SMALL_FIREBALL is the underlying entity type. Saving this subclass
        // would restore it as a vanilla fireball without the cultist config guard.
        Class<?> projectileType = Arrays.stream(CultSpells.class.getDeclaredClasses())
                .filter(SmallFireball.class::isAssignableFrom)
                .findFirst()
                .orElseThrow();
        Constructor<?> constructor = projectileType.getDeclaredConstructor(ServerLevel.class, Mob.class, Vec3.class);
        constructor.setAccessible(true);
        SmallFireball fireball = (SmallFireball)
                constructor.newInstance(mock(ServerLevel.class), mock(Mob.class), new Vec3(0.0, 0.0, 1.0));

        assertFalse(fireball.shouldBeSaved());
    }

    @Test
    void fanSpreadScalesWithTheSquareRootOfDistanceFactor() {
        assertEquals(0.0F, CultSpells.fireballFanSpread(0.0F));
        assertEquals(2.5F, CultSpells.fireballFanSpread(1.0F));
        assertEquals(5.0F, CultSpells.fireballFanSpread(4.0F));
    }
}
