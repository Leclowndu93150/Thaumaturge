package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class GolemEmoteParticle extends TCParticle {
    private static final int CONFUSED_BASE = 3;
    private static final int CONFUSED_FRAMES = 10;
    private static final int TASK_BASE = 13;
    private static final int TASK_FRAMES = 3;
    private static final float ALPHA = 0.5F;

    private final int icon;
    private final int taskBase;

    private GolemEmoteParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, GolemEmoteParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.alpha = ALPHA;
        this.lifetime = Math.max(1, options.age());
        this.quadSize = options.scale() * 0.1F;
        this.icon = options.icon();
        this.taskBase = TASK_BASE + (this.random.nextBoolean() ? 0 : TASK_FRAMES);
        update();
    }

    @Override
    protected void update() {
        switch (this.icon) {
            case GolemEmoteParticleOptions.ICON_STAY -> frame(1);
            case GolemEmoteParticleOptions.ICON_FAIL -> frame(2);
            case GolemEmoteParticleOptions.ICON_CONFUSED -> frame(CONFUSED_BASE + Math.min((int) (progress() * CONFUSED_FRAMES), CONFUSED_FRAMES - 1));
            case GolemEmoteParticleOptions.ICON_TASK -> frame(this.taskBase + Math.min((int) (progress() * TASK_FRAMES), TASK_FRAMES - 1));
            default -> frame(0);
        }
    }

    @Override
    public Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<GolemEmoteParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("golem_emote");

        @Override
        public Particle createParticle(GolemEmoteParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new GolemEmoteParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
