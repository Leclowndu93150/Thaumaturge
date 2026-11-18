package com.leclowndu93150.thaumaturge.client.effect.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.client.effect.instance.BeamInstance;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class ChannelBeamTest {
    @Test
    void movingChannelKeepsItsIdentityAndSeparatesBranchesAndHands() {
        BeamInstance first = channel(1, Vec3.ZERO, new Vec3(0.0, 0.0, 8.0));
        BeamInstance moved = channel(1, new Vec3(4.0, 2.0, 1.0), new Vec3(8.0, 3.0, 0.0));

        assertTrue(first.sameBeamAs(moved));
        assertFalse(first.sameBeamAs(channel(2, Vec3.ZERO, new Vec3(0.0, 0.0, 8.0))));
        assertFalse(first.sameBeamAs(channel(9, Vec3.ZERO, new Vec3(0.0, 0.0, 8.0))));
        assertFalse(first.sameBeamAs(beam(Vec3.ZERO, new Vec3(0.0, 0.0, 8.0))));
    }

    @Test
    void channelRefreshPreservesGrowthAndCoversMaximumPulseInterval() {
        BeamInstance first = channel(1, Vec3.ZERO, new Vec3(0.0, 0.0, 8.0));
        for (int pulse = 0; pulse < 4; pulse++) {
            for (int tick = 0; tick < 12; tick++) {
                first.tick();
                assertEquals(0.4F, first.computeOpacity(0.0F), 1.0E-6F);
                assertFalse(first.isExpired());
            }
            int age = first.age();
            float size = first.computeSize(1.0F);

            first.refresh(channel(1, new Vec3(pulse + 1.0, 0.0, 0.0), new Vec3(4.0, 0.0, 8.0)));

            assertEquals(age, first.age());
            assertEquals(size, first.computeSize(1.0F));
            assertEquals(age + 16, first.maxAge());
        }
    }

    @Test
    void targetInterpolationSettlesAfterTheNextTick() {
        Vec3 before = new Vec3(0.0, 0.0, 8.0);
        Vec3 after = new Vec3(8.0, 0.0, 0.0);
        BeamInstance first = channel(1, Vec3.ZERO, before);

        first.refresh(channel(1, Vec3.ZERO, after));

        assertEquals(before, first.targetPos(0.0F));
        assertEquals(before.lerp(after, 0.5), first.targetPos(0.5F));
        first.tick();
        assertEquals(after, first.targetPos(0.0F));
        assertEquals(after, first.targetPos(0.5F));
        assertEquals(after, first.targetPos(1.0F));
    }

    @Test
    void channelCurveAnchorsBothEndsAndBendsAlongTheCasterLook() {
        Vec3 source = new Vec3(10.0, 64.0, -2.0);
        Vec3 target = source.add(8.0, 0.0, 0.0);
        Vec3 forward = new Vec3(0.0, 0.0, 1.0);

        assertEquals(source, BeamManager.channelPoint(source, target, forward, 0.0F));
        assertEquals(target, BeamManager.channelPoint(source, target, forward, 1.0F));
        assertEquals(source.add(2.0, 0.0, 2.0), BeamManager.channelPoint(source, target, forward, 0.5F));
        assertEquals(source.lerp(target, 0.5), BeamManager.channelPoint(source, target, target.subtract(source), 0.5F));
        assertEquals(source, BeamManager.channelPoint(source, source, forward, 0.5F));
    }

    @Test
    void stoppingAChannelFreezesItsVisualStateAndFadesPromptly() {
        BeamInstance beam = channel(1, Vec3.ZERO, new Vec3(0, 0, 8));
        beam.tick();
        beam.stopChannel();
        assertTrue(beam.isStopped());
        assertEquals(3, beam.maxAge());
        assertEquals(0.2F, beam.computeOpacity(0.0F), 1.0E-6F);
        beam.tick();
        beam.tick();
        assertEquals(0.0F, beam.computeOpacity(0.0F), 1.0E-6F);
        beam.tick();
        assertTrue(beam.isExpired());
    }

    @Test
    void channelRibbonSideKeepsItsOrientationThroughVerticalAim() {
        BeamInstance beam = channel(1, Vec3.ZERO, new Vec3(0, 0, 8));
        Vec3 horizontalSide = beam.ribbonSide(new Vec3(1, 0, 0));
        Vec3 verticalSide = beam.ribbonSide(new Vec3(0, 1, 0));
        assertTrue(horizontalSide.dot(verticalSide) > 0.99);
        assertEquals(0.0, verticalSide.dot(new Vec3(0, 1, 0)), 1.0E-6);
    }

    private static BeamInstance channel(int branch, Vec3 source, Vec3 target) {
        return beam(source, target).withChannel(branch, 16.0);
    }

    private static BeamInstance beam(Vec3 source, Vec3 target) {
        return new BeamInstance(
                source.x, source.y, source.z, target.x, target.y, target.z, 0x8000A0, 16, 0, 1.0F, false, 17, true,
                false, false);
    }
}
