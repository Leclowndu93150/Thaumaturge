package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class BeamRefreshTest {
    @Test
    void repeatedUnanchoredSourceRefreshesRatherThanCreatesASeparateIdentity() {
        BeamInstance first = beam(0.0, 0.0, 0.0, 2, BeamPayloadIds.NO_ENTITY);
        BeamInstance nearby = beam(0.05, 0.0, 0.0, 1, BeamPayloadIds.NO_ENTITY);
        BeamInstance distant = beam(0.2, 0.0, 0.0, 1, BeamPayloadIds.NO_ENTITY);

        assertTrue(first.sameSourceAs(nearby));
        assertTrue(first.sameBeamAs(nearby));
        assertFalse(first.sameSourceAs(distant));
    }

    @Test
    void splitBeamsFromOneSourceKeepSeparateTargets() {
        BeamInstance left = new BeamInstance(0, 0, 0, -2, 0, 4, 0x80C0FF, 2, 0, 1.0F, false, 17, false);
        BeamInstance right = new BeamInstance(0, 0, 0, 2, 0, 4, 0x80C0FF, 2, 0, 1.0F, false, 17, false);

        assertTrue(left.sameSourceAs(right));
        assertFalse(left.sameBeamAs(right));
    }

    @Test
    void entityAnchoredSourcesUseEntityIdentityAndRefreshTargetAndLifetime() {
        BeamInstance first = beam(1.0, 2.0, 3.0, 1, 17);
        BeamInstance next = new BeamInstance(20, 30, 40, 8, 9, 10, 0x80C0FF, 4, 0, 1.0F, false, 17, false, true);
        BeamInstance otherEntity = beam(1.0, 2.0, 3.0, 2, 18);
        Vec3 before = first.targetPos(0.0F);

        first.refresh(next);

        assertFalse(first.sameSourceAs(otherEntity));
        assertEquals(new Vec3(8, 9, 10), first.targetPos(1.0F));
        assertEquals(before, first.targetPos(0.0F));
        assertEquals(4, first.maxAge());
        assertTrue(first.hit());
        assertFalse(first.isExpired());
    }

    private static BeamInstance beam(double x, double y, double z, int age, int sourceEntity) {
        return new BeamInstance(x, y, z, 1, 2, 3, 0x80C0FF, age, 0, 1.0F, false, sourceEntity, false);
    }
}
