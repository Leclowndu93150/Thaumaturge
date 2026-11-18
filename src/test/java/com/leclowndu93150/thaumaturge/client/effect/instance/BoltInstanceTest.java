package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BoltInstanceTest {
    @Test
    void wandBoltDrawsItsFirstRingAtTheMovingTipAndKeepsItsWorldTarget() {
        try (MockedStatic<Minecraft> minecraft = mockStatic(Minecraft.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(mock(Minecraft.class));
            Vec3 target = new Vec3(3, 4, 5);
            BoltInstance bolt = new BoltInstance(Vec3.ZERO, target, 0xFFFFFF, 1.0F, null);
            for (Vec3 tip : List.of(new Vec3(1, 2, 3), new Vec3(1, 1.5, 2))) {
                List<BoltInstance.PathStep> path = bolt.computePath(0.0F, tip);

                assertEquals(new BoltInstance.PathStep(0, 0, 0, 1), path.get(1));
                BoltInstance.PathStep end = path.get(path.size() - 2);
                assertEquals(target, tip.add(end.x(), end.y(), end.z()));
            }
        }
    }

    @Test
    void pathExtendsPastTheTargetSoTubeMeshCanRenderItsFinalSegment() {
        try (MockedStatic<Minecraft> minecraft = mockStatic(Minecraft.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(mock(Minecraft.class));
            BoltInstance bolt = new BoltInstance(0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0xFFFFFF, 1.0F);

            List<BoltInstance.PathStep> path = bolt.computePath(0.0F);

            assertTrue(path.size() >= 4);
            BoltInstance.PathStep previous = path.get(path.size() - 3);
            BoltInstance.PathStep target = path.get(path.size() - 2);
            BoltInstance.PathStep terminal = path.get(path.size() - 1);
            assertEquals(2.0, target.x());
            assertEquals(0.0, target.y());
            assertEquals(0.0, target.z());
            assertEquals(target.x() * 2.0 - previous.x(), terminal.x(), 1.0E-9);
            assertEquals(target.y() * 2.0 - previous.y(), terminal.y(), 1.0E-9);
            assertEquals(target.z() * 2.0 - previous.z(), terminal.z(), 1.0E-9);
            assertEquals(target.width(), terminal.width());
        }
    }
}
