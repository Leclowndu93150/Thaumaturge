package com.leclowndu93150.thaumaturge.client.hud;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class KnowledgeGainOverlay implements GuiLayer {
    private static final Identifier BOOK = TCIds.rl("textures/item/thaumonomicon.png");
    private static final Identifier KNOW_OBSERVATION = TCIds.rl("textures/research/knowledge_observation.png");
    private static final Identifier KNOW_THEORY = TCIds.rl("textures/research/knowledge_theory.png");

    private static final LinkedBlockingQueue<Tracker> TRACKERS = new LinkedBlockingQueue<>();
    private static float bookFade;

    private static final float BOOK_FADE_MAX = 40.0F;
    private static final float BOOK_FADE_GAIN = 10.0F;
    private static final int BOOK_SIZE = 16;
    private static final int BOOK_CORNER_OFFSET = 17;
    private static final int ICON_TEX_SIZE = 16;
    private static final int ICON_ALPHA = 200;
    private static final int THEORY_EXTRA_TICKS = 10;
    private static final int PARTICLE_GRID = 64;
    private static final int BURST_FRAME_START = 320;
    private static final int BURST_FRAME_SPREAD = 16;
    private static final int QUAD_INTRINSIC_ROTATION = 90;
    private static final int MAX_SPARKS = 200;
    private static final float SPARK_SCALE = 24.0F;
    private static final float[] SPARK_ALPHA_KEYS = {0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F};

    private static final List<GuiSpark> SPARKS = new ArrayList<>();

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || (bookFade <= 0.0F && SPARKS.isEmpty())) {
            return;
        }
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        int ww = graphics.guiWidth();
        int hh = graphics.guiHeight();

        int bookTint = ARGB.color(Math.round(bookFade / BOOK_FADE_MAX * 255.0F), 255, 255, 255);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, ww - BOOK_CORNER_OFFSET, hh - BOOK_CORNER_OFFSET, 0.0F, 0.0F, BOOK_SIZE, BOOK_SIZE, BOOK_SIZE, BOOK_SIZE, BOOK_SIZE, BOOK_SIZE, bookTint);

        for (Tracker current : TRACKERS) {
            Random rand = new Random(current.seed);
            float s = 16.0F;
            float x = ww / 4.0F + rand.nextInt(32);
            float y = hh / 3.0F + rand.nextInt(32);
            float wot = 0.0F;
            if (current.progress < current.max * 0.66F) {
                float q = (current.progress - partial) / (current.max * 0.66F);
                s *= q;
                float m = (float) Math.sin(q * Math.PI - (Math.PI / 2)) * 0.5F + 0.5F;
                y *= m;
                float d = (float) Math.sin(m * Math.PI * 0.5);
                x *= d;
            } else {
                wot = current.max - current.progress + partial;
                float wot2 = wot / (current.max * 0.33F);
                float m = (float) Math.sin(wot2 * Math.PI * 2.0 - (Math.PI / 2)) * 0.5F + 1.5F;
                if (wot2 < 0.5F) {
                    s *= wot2 * 2.0F;
                }
                s *= m;
            }

            float xx = ww - 12 + rand.nextInt(8) - x;
            float yy = hh - 12 + rand.nextInt(8) - y;

            graphics.pose().pushMatrix();
            graphics.pose().translate(xx, yy);
            graphics.pose().rotate((float) Math.toRadians(84 + rand.nextInt(12) - QUAD_INTRINSIC_ROTATION));

            if (current.aspect != null) {
                drawCentered(graphics, current.aspect.value().texture(), s, ARGB.color(ICON_ALPHA, current.aspect.value().color()), false);
            } else {
                Identifier typeIcon = current.type == KnowledgeType.THEORY ? KNOW_THEORY : KNOW_OBSERVATION;
                drawCentered(graphics, typeIcon, s, ARGB.color(ICON_ALPHA, 255, 255, 255), false);
                Identifier categoryIcon = categoryIcon(mc, current.category);
                if (categoryIcon != null) {
                    drawCentered(graphics, categoryIcon, s * 0.75F, ARGB.color(ICON_ALPHA, 255, 255, 255), false);
                }
            }

            if (current.progress > current.max * 0.9F) {
                float wot3 = (current.max - current.progress + partial) / (current.max * 0.1F);
                drawBurst(graphics, mc, rand, wot3, 64.0F);
            }
            if (current.progress < current.max * 0.1F) {
                float wot3 = 1.0F - (current.progress - partial) / (current.max * 0.1F);
                drawBurst(graphics, mc, rand, wot3, 32.0F);
            }

            graphics.pose().popMatrix();

            if (mc.level != null && mc.level.getRandom().nextInt((int) (1.0F + (float) current.progress / current.max * 10.0F)) == 0) {
                spawnSpark(mc, xx, yy);
            }
        }
        renderSparks(graphics, partial);
    }

    private static void spawnSpark(Minecraft mc, float x, float y) {
        if (SPARKS.size() >= MAX_SPARKS) {
            return;
        }
        RandomSource rand = mc.level.getRandom();
        SPARKS.add(new GuiSpark(x + (float) rand.nextGaussian() * 5.0F, y + (float) rand.nextGaussian() * 5.0F, (float) rand.nextGaussian(), (float) rand.nextGaussian(), 32 + rand.nextInt(8),
                rand.nextInt(5), rand.nextFloat() < 0.2F ? BURST_FRAME_START : 512, Mth.nextInt(rand, 189, 255) / 255.0F, Mth.nextInt(rand, 64, 255) / 255.0F));
    }

    private static void renderSparks(GuiGraphicsExtractor graphics, float partial) {
        float texFrame = 1024.0F / PARTICLE_GRID;
        for (GuiSpark spark : SPARKS) {
            if (spark.delay > 0) {
                continue;
            }
            float life = (spark.age + partial) / spark.maxAge;
            float alpha = sampleKeys(SPARK_ALPHA_KEYS, life);
            if (alpha <= 0.0F) {
                continue;
            }
            float size = 0.2F * SPARK_SCALE * (1.0F + life);
            int frame = spark.startFrame + spark.age % BURST_FRAME_SPREAD;
            int frameU = frame % PARTICLE_GRID;
            int frameV = frame / PARTICLE_GRID;
            int tint = ARGB.colorFromFloat(alpha, 1.0F, spark.g, spark.b);
            float x = spark.xo + (spark.x - spark.xo) * partial;
            float y = spark.yo + (spark.y - spark.yo) * partial;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x - size / 2.0F, y - size / 2.0F);
            graphics.pose().scale(size / texFrame, size / texFrame);
            graphics.blit(TCRenderPipelines.GUI_TEXTURED_ADDITIVE, ParticleTextures.PARTICLES, 0, 0, frameU * texFrame, frameV * texFrame, (int) texFrame, (int) texFrame, (int) texFrame,
                    (int) texFrame, 1024, 1024, tint);
            graphics.pose().popMatrix();
        }
    }

    private static float sampleKeys(float[] keys, float life) {
        float position = Mth.clamp(life, 0.0F, 1.0F) * (keys.length - 1);
        int index = Math.min((int) position, keys.length - 2);
        float t = position - index;
        return keys[index] + (keys[index + 1] - keys[index]) * t;
    }

    private static void drawBurst(GuiGraphicsExtractor graphics, Minecraft mc, Random rand, float phase, float baseSize) {
        float m = (float) Math.sin(phase * Math.PI * 2.0 - (Math.PI / 2)) * 0.25F + 0.25F;
        float size = baseSize * m;
        graphics.pose().rotate((float) Math.toRadians(-rand.nextInt(360)));
        float g = Mth.nextInt(mc.level.getRandom(), 189, 255) / 255.0F;
        float b = Mth.nextInt(mc.level.getRandom(), 64, 255) / 255.0F;
        int tint = ARGB.colorFromFloat(ICON_ALPHA / 255.0F, 1.0F, g, b);
        int frame = BURST_FRAME_START + rand.nextInt(BURST_FRAME_SPREAD);
        int frameU = frame % PARTICLE_GRID;
        int frameV = frame / PARTICLE_GRID;
        float texFrame = 1024.0F / PARTICLE_GRID;
        graphics.pose().pushMatrix();
        graphics.pose().translate(-size / 2.0F, -size / 2.0F);
        graphics.pose().scale(size / texFrame, size / texFrame);
        graphics.blit(TCRenderPipelines.GUI_TEXTURED_ADDITIVE, ParticleTextures.PARTICLES, 0, 0, frameU * texFrame, frameV * texFrame, (int) texFrame, (int) texFrame, (int) texFrame, (int) texFrame,
                1024, 1024, tint);
        graphics.pose().popMatrix();
    }

    private static void drawCentered(GuiGraphicsExtractor graphics, Identifier texture, float size, int tint, boolean additive) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(-size / 2.0F, -size / 2.0F);
        graphics.pose().scale(size / ICON_TEX_SIZE, size / ICON_TEX_SIZE);
        graphics.blit(additive ? TCRenderPipelines.GUI_TEXTURED_ADDITIVE : RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0F, 0.0F, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE, ICON_TEX_SIZE,
                ICON_TEX_SIZE, ICON_TEX_SIZE, tint);
        graphics.pose().popMatrix();
    }

    private static @Nullable Identifier categoryIcon(Minecraft mc, @Nullable ResourceKey<IResearchCategory> category) {
        if (category == null || mc.level == null) {
            return null;
        }
        return mc.level.registryAccess().lookupOrThrow(IResearchCategory.REGISTRY_KEY).get(category).map(holder -> holder.value().icon()).orElse(null);
    }

    public static void addAspectTracker(Holder<IAspect> aspect, int duration, long seed) {
        TRACKERS.add(new Tracker(KnowledgeType.OBSERVATION, null, aspect, duration, seed));
    }

    public static void addTracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, int duration, long seed) {
        int total = type == KnowledgeType.THEORY ? duration + THEORY_EXTRA_TICKS : duration;
        TRACKERS.add(new Tracker(type, category, total, seed));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (TRACKERS.isEmpty()) {
            if (bookFade > 0.0F) {
                bookFade--;
            }
        } else {
            bookFade = Math.min(BOOK_FADE_MAX, bookFade + BOOK_FADE_GAIN);
            for (Tracker tracker : TRACKERS) {
                tracker.progress--;
            }
            TRACKERS.removeIf(tracker -> tracker.progress <= 0);
        }
        tickSparks();
    }

    private static void tickSparks() {
        Minecraft mc = Minecraft.getInstance();
        for (GuiSpark spark : SPARKS) {
            if (spark.delay > 0) {
                spark.delay--;
                continue;
            }
            spark.age++;
            spark.xo = spark.x;
            spark.yo = spark.y;
            spark.x += spark.vx;
            spark.y += spark.vy;
            spark.vx *= 0.9F;
            spark.vy *= 0.9F;
            spark.vy += 0.04F;
            if (mc.level != null) {
                spark.vx += (float) mc.level.getRandom().nextGaussian() * 0.025F;
                spark.vy += (float) mc.level.getRandom().nextGaussian() * 0.025F;
            }
        }
        SPARKS.removeIf(spark -> spark.age >= spark.maxAge);
    }

    private static final class GuiSpark {
        float x;
        float y;
        float xo;
        float yo;
        float vx;
        float vy;
        int age;
        final int maxAge;
        int delay;
        final int startFrame;
        final float g;
        final float b;

        GuiSpark(float x, float y, float vx, float vy, int maxAge, int delay, int startFrame, float g, float b) {
            this.x = x;
            this.y = y;
            this.xo = x;
            this.yo = y;
            this.vx = vx;
            this.vy = vy;
            this.maxAge = maxAge;
            this.delay = delay;
            this.startFrame = startFrame;
            this.g = g;
            this.b = b;
        }
    }

    private static final class Tracker {
        final KnowledgeType type;
        final @Nullable ResourceKey<IResearchCategory> category;
        final @Nullable Holder<IAspect> aspect;
        int progress;
        final int max;
        final long seed;

        Tracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, int duration, long seed) {
            this(type, category, null, duration, seed);
        }

        Tracker(KnowledgeType type, @Nullable ResourceKey<IResearchCategory> category, @Nullable Holder<IAspect> aspect, int duration, long seed) {
            this.type = type;
            this.category = category;
            this.aspect = aspect;
            this.progress = duration;
            this.max = duration;
            this.seed = seed;
        }
    }
}
