package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.particle.support.BoltLineShape;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.content.particle.AirGustParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.TaintSwarmParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

final class ParticleBehaviorPortTest {
    @Test
    void nitorCoreDrawsBeforeFlamesWithoutMovingOtherLayers() {
        ParticleRenderType core = TTParticleLayers.additive(TTParticleSheets.sheet("nitor_core"));
        ParticleRenderType flame = TTParticleLayers.additiveSoft(TTParticleSheets.sheet("wisp_flame"));
        ParticleRenderType other = TTParticleLayers.additive(TTParticleSheets.sheet("sparkle"));
        Set<ParticleRenderType> layers = new LinkedHashSet<>(List.of(ParticleRenderType.CUSTOM, flame, other, core));

        Set<ParticleRenderType> ordered = TTParticleLayers.orderNitorLayers(layers);

        assertIterableEquals(List.of(ParticleRenderType.CUSTOM, core, flame, other), ordered);
        assertSame(ordered, TTParticleLayers.orderNitorLayers(ordered));
        Set<ParticleRenderType> withoutCore = Set.of(flame, other);
        assertSame(withoutCore, TTParticleLayers.orderNitorLayers(withoutCore));
        assertTrue(TTParticleLayers.usesNitorAdditiveBlend(core));
        assertTrue(TTParticleLayers.usesNitorAdditiveBlend(flame));
        assertFalse(TTParticleLayers.usesNitorAdditiveBlend(other));
    }

    @Test
    void taintSwarmUsesOrbGlowFramesAndFullBrightLighting() throws Exception {
        TaintSwarmParticle particle = construct(
                TaintSwarmParticle.class,
                new Class<?>[] {
                    ClientLevel.class, double.class, double.class, double.class, TaintSwarmParticleOptions.class
                },
                mock(ClientLevel.class),
                0.0,
                0.0,
                0.0,
                new TaintSwarmParticleOptions(1));

        Field sheet = TTParticle.class.getDeclaredField("sheet");
        sheet.setAccessible(true);
        assertEquals(ParticleTextures.ORB_GLOW, ((ParticleSheet) sheet.get(particle)).texture());
        assertEquals(0, TaintSwarmParticle.frameAtAge(0));
        assertEquals(2, TaintSwarmParticle.frameAtAge(1));
        assertEquals(14, TaintSwarmParticle.frameAtAge(7));
        assertEquals(0, TaintSwarmParticle.frameAtAge(8));

        Method getLightColor = TaintSwarmParticle.class.getDeclaredMethod("getLightColor", float.class);
        getLightColor.setAccessible(true);
        assertEquals(LightTexture.FULL_BRIGHT, getLightColor.invoke(particle, 0.0F));
    }

    @Test
    void visSparkleAndWispyMoteRemoveAtTheirExactArrivalDistances() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        VisSparkleParticle sparkle = construct(
                VisSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    VisSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                new VisSparkleParticleOptions(0.2, 0.0, 0.0, 0),
                null);
        invokeUpdate(sparkle);
        assertFalse(sparkle.isAlive());

        Entity target = mock(Entity.class);
        when(level.getEntity(8)).thenReturn(target);
        when(target.isAlive()).thenReturn(true);
        when(target.position()).thenReturn(new Vec3(0.25, 0.0, 0.0));
        WispyMoteParticle mote = construct(
                WispyMoteParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    WispyMoteParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new WispyMoteParticleOptions(0, 40, 0.0F, 8),
                null);
        invokeUpdate(mote);
        assertFalse(mote.isAlive());
    }

    @Test
    void visSparkleKeepsHomingSpeedBoundedAndShrinksWhileNearTarget() throws Exception {
        VisSparkleParticle sparkle = construct(
                VisSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    VisSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                mock(ClientLevel.class),
                0.0,
                0.0,
                0.0,
                new VisSparkleParticleOptions(10.0, 0.0, 0.0, 0),
                null);

        invokeUpdate(sparkle);
        assertTrue(Math.abs(particleVelocity(sparkle, "xd")) <= 0.1);
        assertTrue(Math.abs(particleVelocity(sparkle, "yd")) <= 0.1);
        assertTrue(Math.abs(particleVelocity(sparkle, "zd")) <= 0.1);

        sparkle.setPos(9.0, 0.0, 0.0);
        invokeUpdate(sparkle);
        Field proximityScale = VisSparkleParticle.class.getDeclaredField("proximityScale");
        proximityScale.setAccessible(true);
        assertEquals(0.95F, proximityScale.getFloat(sparkle), 1.0E-6F);

        Field age = Particle.class.getDeclaredField("age");
        age.setAccessible(true);
        age.setInt(sparkle, 10);
        invokeUpdate(sparkle);
        assertEquals(0.95F * 0.95F, proximityScale.getFloat(sparkle), 1.0E-6F);
    }

    @Test
    void wispFollowsOwnerPositionDeltaAndIgnoresRemovedOwner() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        when(level.getRandom()).thenReturn(RandomSource.create(12L));
        Entity owner = mock(Entity.class);
        when(level.getEntity(12)).thenReturn(owner);
        when(owner.getX()).thenReturn(20.0);
        when(owner.getZ()).thenReturn(30.0);
        when(owner.getDeltaMovement()).thenReturn(new Vec3(0.25, 0.0, 0.5));
        owner.xo = 18.0;
        owner.zo = 27.0;
        WispParticle wisp = construct(
                WispParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    WispParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                5.0,
                4.0,
                6.0,
                new WispParticleOptions(12),
                null);
        wisp.setParticleSpeed(0.0, 0.0, 0.0);

        wisp.tick();
        assertEquals(new Vec3(7.0, 4.0, 9.0), wisp.getPos());

        when(owner.isRemoved()).thenReturn(true);
        wisp.setPos(7.0, 4.0, 9.0);
        wisp.tick();
        assertEquals(new Vec3(7.0, 4.0, 9.0), wisp.getPos());
    }

    @Test
    void seekerUsesResolvedTargetForInitialLifetimeAndSkipsRemovedTargets() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        Entity target = mock(Entity.class);
        when(level.getEntity(23)).thenReturn(target);
        when(target.getX()).thenReturn(2.0);
        when(target.getEyeY()).thenReturn(0.0);
        when(target.getZ()).thenReturn(0.0);
        BoreSparkleParticleOptions farFallback = new BoreSparkleParticleOptions(23, 100.0, 0.0, 0.0, 1.0F, 1.0F, 1.0F);

        Particle resolved = construct(
                BoreSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    BoreSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                farFallback,
                null);
        assertTrue(resolved.getLifetime() >= 10 && resolved.getLifetime() < 30);

        when(target.isRemoved()).thenReturn(true);
        Particle removed = construct(
                BoreSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    BoreSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                farFallback,
                null);
        assertTrue(removed.getLifetime() >= 500);
    }

    @Test
    void ventCullingUsesGraphicsRangeAndSkipsUninitializedCamera() {
        Camera camera = mock(Camera.class);
        when(camera.isInitialized()).thenReturn(false);
        assertFalse(VentParticle.beyondSpawnRange(1000.0, 0.0, 0.0, camera, true));
        verify(camera, never()).getPosition();

        when(camera.isInitialized()).thenReturn(true);
        when(camera.getPosition()).thenReturn(Vec3.ZERO);
        assertFalse(VentParticle.beyondSpawnRange(25.0, 0.0, 0.0, camera, true));
        assertTrue(VentParticle.beyondSpawnRange(25.01, 0.0, 0.0, camera, true));
        assertFalse(VentParticle.beyondSpawnRange(50.0, 0.0, 0.0, camera, false));
        assertTrue(VentParticle.beyondSpawnRange(50.01, 0.0, 0.0, camera, false));
    }

    @Test
    void ventClampsZeroScaleAndStillFinishesGrowing() throws Exception {
        VentParticle vent = construct(
                VentParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    VentParticleOptions.class,
                    ParticleSheet.class
                },
                mock(ClientLevel.class),
                0.0,
                0.0,
                0.0,
                new VentParticleOptions(0.0, 0.0, 0.0, 0xFFFFFFFF, 0.0F, false),
                null);

        Field fullSize = VentParticle.class.getDeclaredField("fullSize");
        fullSize.setAccessible(true);
        assertEquals(0.05F, fullSize.getFloat(vent), 1.0E-6F);
        for (int tick = 0; tick < 80 && vent.isAlive(); tick++) {
            invokeUpdate(vent);
        }
        assertFalse(vent.isAlive());
    }

    @Test
    void nitorCorePulseReturnsToBaselineWithSmoothMidpoint() {
        assertEquals(1.0F, NitorCoreParticle.pulseScale(0, 10, 1.12F), 1.0E-6F);
        assertEquals(1.12F, NitorCoreParticle.pulseScale(5, 10, 1.12F), 1.0E-6F);
        assertEquals(1.0F, NitorCoreParticle.pulseScale(10, 10, 1.12F), 1.0E-6F);
        assertEquals(1.0F, NitorCoreParticle.pulseScale(20, 10, 1.12F), 1.0E-6F);
    }

    @Test
    void smokeSpiralUsesOnlyItsFourActiveAtlasFrames() {
        assertEquals(0, SmokeSpiralParticle.frameAt(0.0F));
        assertEquals(1, SmokeSpiralParticle.frameAt(0.25F));
        assertEquals(2, SmokeSpiralParticle.frameAt(0.5F));
        assertEquals(3, SmokeSpiralParticle.frameAt(0.75F));
        assertEquals(3, SmokeSpiralParticle.frameAt(1.0F));
        assertEquals(0, SmokeSpiralParticle.frameAt(-1.0F));
    }

    @Test
    void taintSplosionUsesFiniteLaunchAndLifetimeAtRandomBounds() {
        assertEquals(new Vec3(0.0, 0.1, 0.0), TaintSplosionParticle.launchVelocity(0.0, 0.0, 0.0, 0.0, 0.0));

        Vec3 velocity = TaintSplosionParticle.launchVelocity(1.0, 0.0, 0.0, 0.5, 0.5);
        assertEquals(0.2892, velocity.x, 1.0E-12);
        assertEquals(0.1, velocity.y, 1.0E-12);
        assertEquals(0.0, velocity.z, 1.0E-12);
        assertEquals(660, TaintSplosionParticle.lifetime(0.0));
        assertEquals(66, TaintSplosionParticle.lifetime(1.0));
    }

    @Test
    void fluxGooDropletLifetimeIsSkewedTowardShorterParticles() {
        assertEquals(50, FluxGooDropletParticle.skewedLifetime(0.0F));
        assertEquals(84, FluxGooDropletParticle.skewedLifetime(0.5F));
        assertEquals(600, FluxGooDropletParticle.skewedLifetime(1.0F));
    }

    @Test
    void infusionCrumbsUseSmallTintedPatchesOnThe1211SharedItemBlockAtlas() throws Exception {
        TextureAtlasSprite sprite = mock(TextureAtlasSprite.class);
        InfusionCrumbsParticle particle = construct(
                InfusionCrumbsParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    InfusionCrumbsParticleOptions.class,
                    TextureAtlasSprite.class
                },
                mock(ClientLevel.class),
                0.0,
                0.0,
                0.0,
                new InfusionCrumbsParticleOptions(ItemStack.EMPTY, 1.0, 2.0, 3.0, 0.0, 0.0, 0.0),
                sprite);

        Field alpha = Particle.class.getDeclaredField("alpha");
        alpha.setAccessible(true);
        assertEquals(0.4F, alpha.getFloat(particle), 1.0E-6F);

        Field size = SingleQuadParticle.class.getDeclaredField("quadSize");
        size.setAccessible(true);
        float quadSize = size.getFloat(particle);
        assertTrue(quadSize >= 0.5F / 32.0F && quadSize <= 2.0F / 32.0F);
        assertEquals(ParticleRenderType.TERRAIN_SHEET, particle.getRenderType());
    }

    @Test
    void slimyBubbleKeepsSwellFloatAndPopFramesInsideItsMinimumLifetime() {
        assertEquals(11, SlimyBubbleParticle.particleLifetime(-10));
        assertEquals(11, SlimyBubbleParticle.particleLifetime(0));
        assertEquals(23, SlimyBubbleParticle.particleLifetime(23));

        assertEquals(0, SlimyBubbleParticle.frameFor(0, 11));
        assertEquals(1, SlimyBubbleParticle.frameFor(2, 11));
        assertEquals(3, SlimyBubbleParticle.frameFor(5, 11));
        assertEquals(4, SlimyBubbleParticle.frameFor(6, 11));
        assertEquals(5, SlimyBubbleParticle.frameFor(7, 11));
        assertEquals(6, SlimyBubbleParticle.frameFor(9, 11));
    }

    @Test
    void boreSparklePulseUsesTheRevisedAmplitudeAndPeriod() {
        assertEquals(1.0F, BoreSparkleParticle.pulseAt(0.0F), 1.0E-6F);
        assertEquals(1.3F, BoreSparkleParticle.pulseAt((float) Math.PI), 1.0E-6F);
        assertEquals(1.0F, BoreSparkleParticle.pulseAt((float) (2.0 * Math.PI)), 1.0E-6F);
    }

    @Test
    void stabilizerRuneClampsNonpositiveLifetimesAndStartsWithItsFadedAlpha() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        for (int life : new int[] {0, -1}) {
            StabilizerRuneParticle particle = construct(
                    StabilizerRuneParticle.class,
                    new Class<?>[] {
                        ClientLevel.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class,
                        StabilizerRuneParticleOptions.class,
                        ParticleSheet.class
                    },
                    level,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    new StabilizerRuneParticleOptions(life),
                    null);
            assertEquals(1, particle.getLifetime());

            Field alpha = Particle.class.getDeclaredField("alpha");
            alpha.setAccessible(true);
            assertEquals(0.3F, alpha.getFloat(particle), 1.0E-6F);
        }
    }

    @Test
    void boltInterpolatesWorldCoordinatesBeforeCameraOffsetAndFloatConversion() throws Exception {
        double originX = 16_777_216.0;
        double originY = 64.0;
        double originZ = -16_777_216.0;
        BoltParticle bolt = construct(
                BoltParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    BoltParticleOptions.class,
                    ParticleSheet.class
                },
                mock(ClientLevel.class),
                originX,
                originY,
                originZ,
                new BoltParticleOptions(originX + 10.0, originY + 1.0, originZ, 1.0F, 1.0F, 1.0F, 0.6F),
                null);
        Field shapeField = BoltParticle.class.getDeclaredField("shape");
        shapeField.setAccessible(true);
        BoltLineShape shape = (BoltLineShape) shapeField.get(bolt);

        Camera camera = mock(Camera.class);
        when(camera.getPosition()).thenReturn(new Vec3(originX, originY, originZ));
        when(camera.rotation()).thenReturn(new Quaternionf());
        List<Vec3> vertices = new ArrayList<>();
        VertexConsumer buffer = mock(VertexConsumer.class, invocation -> {
            if (invocation.getMethod().getName().equals("addVertex") && invocation.getArguments().length == 3) {
                Object[] arguments = invocation.getArguments();
                vertices.add(new Vec3(
                        ((Float) arguments[0]).floatValue(),
                        ((Float) arguments[1]).floatValue(),
                        ((Float) arguments[2]).floatValue()));
            }
            return invocation.getMock();
        });
        float partialTick = 0.5F;
        bolt.render(buffer, camera, partialTick);

        double amplitude = partialTick / 24.0F;
        double expectedX =
                Mth.lerp(0.25, shape.coordinate(0, 0, amplitude), shape.coordinate(1, 0, amplitude)) - originX;
        double expectedY =
                Mth.lerp(0.25, shape.coordinate(0, 1, amplitude), shape.coordinate(1, 1, amplitude)) - originY;
        double expectedZ =
                Mth.lerp(0.25, shape.coordinate(0, 2, amplitude), shape.coordinate(1, 2, amplitude)) - originZ;
        Vec3 firstVertex = vertices.get(0);
        assertEquals(expectedX, firstVertex.x - 0.1F, 1.0E-5);
        assertEquals(expectedY, firstVertex.y + 0.1F, 1.0E-5);
        assertEquals(expectedZ, firstVertex.z, 1.0E-5);
    }

    @Test
    void boltCurlyWispAndGolemTrailUseRevisedMotionProfiles() throws Exception {
        assertEquals(4096, intConstant(BoltParticle.class, "SEED_BOUND"));
        assertEquals(24.0F, floatConstant(BoltParticle.class, "SWAY_TIME_DIVISOR"));
        assertEquals(0.2F, floatConstant(BoltParticle.class, "MIN_ALPHA"));

        assertEquals(20, intConstant(CurlyWispParticle.class, "LIFETIME_BASE"));
        assertEquals(4, intConstant(CurlyWispParticle.class, "LIFETIME_RANGE_PER_SEED"));
        assertEquals(55.0, doubleConstant(CurlyWispParticle.class, "TILT_DEVIATION_DEGREES"));
        assertEquals(1.05F, floatConstant(CurlyWispParticle.class, "END_SIZE_BASE"));
        assertEquals(1.1F, floatConstant(CurlyWispParticle.class, "SPIN_BASE"));

        assertEquals(16, intConstant(GolemTrailParticle.class, "BASE_LIFETIME"));
        assertEquals(8, intConstant(GolemTrailParticle.class, "LIFETIME_RANGE"));
        assertEquals(0.45F, floatConstant(GolemTrailParticle.class, "END_SIZE"));
        assertEquals(4.0E-4, doubleConstant(GolemTrailParticle.class, "WIND_SCALE"));
    }

    @Test
    void runeMistAndScanGlyphUseRevisedScaleAndAnimationProfiles() throws Exception {
        assertEquals(0.17F, floatConstant(BlockRunesParticle.class, "QUAD_RADIUS"));
        assertEquals(0.08F, floatConstant(BlockRunesParticle.class, "SIZE_DEVIATION"));
        assertEquals(-0.505F, floatConstant(BlockRunesParticle.class, "FACE_OFFSET"));

        assertEquals(120, intConstant(MistFlatParticle.class, "LIFETIME_RANGE"));
        assertEquals(0.1F, floatConstant(MistFlatParticle.class, "START_SIZE"));
        assertEquals(0.1F, floatConstant(MistFlatParticle.class, "SPIN_SPEED"));
        assertEquals(3.0E-4, doubleConstant(MistFlatParticle.class, "WIND_SCALE"));

        assertEquals(2, intConstant(ScanGlyphParticle.class, "TICKS_PER_FRAME"));
        assertEquals(0.9F, floatConstant(ScanGlyphParticle.class, "HOLD_ALPHA"));
    }

    @Test
    void puffAndPechCurseUseTheRevisedLifetimeAndVisualProfiles() throws Exception {
        assertEquals(18, intConstant(PuffParticle.class, "LIFETIME_RANGE"));
        assertEquals(0.12F, floatConstant(PuffParticle.class, "START_SIZE"));
        assertEquals(0.24F, floatConstant(PuffParticle.class, "END_SIZE_BASE"));
        assertEquals(0.12F, floatConstant(PuffParticle.class, "END_ALPHA"));
        assertEquals(0.72F, floatConstant(PuffParticle.class, "DRAG"));
        assertEquals(0.03F, floatConstant(PuffParticle.class, "SHADE_JITTER"));

        assertEquals(51, intConstant(PechCurseParticle.class, "LIFETIME_RANGE"));
        assertEquals(1.0F, floatConstant(PechCurseParticle.class, "START_RED"));
        assertEquals(0.85F, floatConstant(PechCurseParticle.class, "START_ALPHA"));
        assertEquals(1.1F, floatConstant(PechCurseParticle.class, "SPIN_BASE"));
        assertEquals(60.0, doubleConstant(PechCurseParticle.class, "TILT_DEVIATION_DEGREES"));
    }

    @Test
    void blockMistUsesTheCurrentMainFadeAndMotionProfile() throws Exception {
        assertEquals(50, intConstant(BlockMistParticle.class, "LIFETIME_BASE"));
        assertEquals(25, intConstant(BlockMistParticle.class, "LIFETIME_RANGE"));
        assertEquals(0.1F, floatConstant(BlockMistParticle.class, "GRAVITY"));
        assertEquals(0.5F, floatConstant(BlockMistParticle.class, "PEAK_ALPHA"));
        assertEquals(1.0F, floatConstant(BlockMistParticle.class, "SPIN_SPEED"));
        assertEquals(1.0E-3, doubleConstant(BlockMistParticle.class, "WIND_SCALE"));

        Field fadeCurve = BlockMistParticle.class.getDeclaredField("FADE_CURVE");
        fadeCurve.setAccessible(true);
        assertArrayEquals(new float[] {0.0F, 0.5F, 0.4F, 0.3F, 0.2F, 0.1F, 0.0F}, (float[]) fadeCurve.get(null));
    }

    @Test
    void airGustAndFlameFanAnimationsUseTheirSheetFrameCounts() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(10)).thenReturn(0);
        when(random.nextFloat()).thenReturn(0.0F);
        when(random.nextGaussian()).thenReturn(0.0);
        ParticleSheet sheet = mock(ParticleSheet.class);
        when(sheet.frames()).thenReturn(7);

        AirGustParticle airGust = construct(
                AirGustParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    AirGustParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new AirGustParticleOptions(1.0F),
                sheet);
        FlameFanParticle flameFan = construct(
                FlameFanParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    FlameFanParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new FlameFanParticleOptions(1.0F, 0.0F, 1.0F),
                sheet);
        setIntField(airGust, Particle.class, "age", 10);
        setIntField(airGust, Particle.class, "lifetime", 20);
        setIntField(flameFan, Particle.class, "age", 5);
        setIntField(flameFan, Particle.class, "lifetime", 10);

        invokeUpdate(airGust);
        invokeUpdate(flameFan);

        assertEquals(3, airGust.frame);
        assertEquals(3, flameFan.frame);
    }

    private static void invokeUpdate(Object particle) throws Exception {
        Method update = particle.getClass().getDeclaredMethod("update");
        update.setAccessible(true);
        update.invoke(particle);
    }

    private static double particleVelocity(Particle particle, String name) throws Exception {
        Field velocity = Particle.class.getDeclaredField(name);
        velocity.setAccessible(true);
        return velocity.getDouble(particle);
    }

    private static int intConstant(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    private static float floatConstant(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getFloat(null);
    }

    private static double doubleConstant(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.getDouble(null);
    }

    private static void setIntField(Object target, Class<?> declaringType, String name, int value) throws Exception {
        Field field = declaringType.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(target, value);
    }

    private static <T> T construct(Class<T> type, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(arguments);
    }
}
