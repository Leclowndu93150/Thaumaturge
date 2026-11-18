package com.leclowndu93150.thaumaturge.mixin.sable.particle;

import com.leclowndu93150.thaumaturge.client.particle.BoreSparkleParticle;
import com.leclowndu93150.thaumaturge.client.particle.BubbleParticle;
import com.leclowndu93150.thaumaturge.client.particle.CurlyWispParticle;
import com.leclowndu93150.thaumaturge.client.particle.FlashParticle;
import com.leclowndu93150.thaumaturge.client.particle.InfusionCrumbsParticle;
import com.leclowndu93150.thaumaturge.client.particle.PuffParticle;
import com.leclowndu93150.thaumaturge.client.particle.SparkleParticle;
import com.leclowndu93150.thaumaturge.client.particle.VisSparkleParticle;
import com.leclowndu93150.thaumaturge.client.particle.WispyMoteParticle;
import dev.ryanhcode.sable.api.particle.ParticleSubLevelKickable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    BoreSparkleParticle.class,
    BubbleParticle.class,
    CurlyWispParticle.class,
    FlashParticle.class,
    InfusionCrumbsParticle.class,
    PuffParticle.class,
    SparkleParticle.class,
    VisSparkleParticle.class,
    WispyMoteParticle.class
})
abstract class AttachedParticleMixin implements ParticleSubLevelKickable {
    @Override
    public boolean sable$shouldCareAboutIntersectingSubLevels() {
        return false;
    }

    @Override
    public boolean sable$shouldKickFromTracking() {
        return false;
    }

    @Override
    public boolean sable$shouldCollideWithTrackingSubLevel() {
        return false;
    }
}
