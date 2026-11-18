package com.leclowndu93150.thaumaturge.api.spell.cast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class SpellTargetTest {
    @Test
    void wandTargetingUsesTheExactEyeRayForEitherHandAndMainArm() {
        Player caster = caster();
        for (HumanoidArm arm : HumanoidArm.values()) {
            when(caster.getMainArm()).thenReturn(arm);
            for (InteractionHand hand : InteractionHand.values()) {
                SpellTarget target = SpellTarget.wandOrigin(caster, hand);

                assertEquals(caster.getEyePosition(), target.position());
                assertEquals(caster.getEyePosition(), target.hit().getLocation());
                assertEquals(caster.getLookAngle(), target.direction());
                assertSame(caster, target.entity().orElseThrow());
            }
        }
    }

    @Test
    void visualWandOriginKeepsTheHandOffsetSeparateFromTheTargetingRay() {
        Player caster = caster();
        for (HumanoidArm arm : HumanoidArm.values()) {
            when(caster.getMainArm()).thenReturn(arm);
            for (InteractionHand hand : InteractionHand.values()) {
                boolean right = (hand == InteractionHand.MAIN_HAND) == (arm == HumanoidArm.RIGHT);
                Vec3 tip = SpellTarget.visualWandOrigin(caster, hand);

                assertEquals(right ? 9.7 : 10.3, tip.x, 1.0E-6);
                assertEquals(64.55, tip.y, 1.0E-6);
                assertEquals(-1.65, tip.z, 1.0E-6);
                assertEquals(
                        new Vec3(10.0, 65.0, -2.0),
                        SpellTarget.wandOrigin(caster, hand).position());
            }
        }
    }

    private static Player caster() {
        Player caster = mock(Player.class);
        when(caster.getEyePosition()).thenReturn(new Vec3(10.0, 65.0, -2.0));
        when(caster.getLookAngle()).thenReturn(new Vec3(0.0, 0.0, 1.0));
        when(caster.getYRot()).thenReturn(0.0F);
        return caster;
    }
}
