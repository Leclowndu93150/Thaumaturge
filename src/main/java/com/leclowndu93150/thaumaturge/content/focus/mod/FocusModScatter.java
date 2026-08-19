package com.leclowndu93150.thaumaturge.content.focus.mod;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.CastContext;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusMod;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.api.casters.Trajectory;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class FocusModScatter implements FocusMod {
    private static final Identifier KEY = TCIds.rl("scatter");

    private static final float MIN_COMPLEXITY = 2.0F;
    private static final float CONE_COMPLEXITY_DIVISOR = 45.0F;
    private static final double JITTER_PER_DEGREE = 0.0075F;
    private static final float POWER_FORKS_DIVISOR = 2.0F;

    @Override
    public Identifier id() {
        return KEY;
    }

    @Override
    public ResearchGate research() {
        return new ResearchGate(TCIds.rl("focus_scatter"), Optional.empty(), false);
    }

    @Override
    public int complexity(FocusSettings settings) {
        return (int) Math.max(MIN_COMPLEXITY, 2.0F * (settings.value("forks") - settings.value("cone") / CONE_COMPLEXITY_DIVISOR));
    }

    @Override
    public List<SettingDefinition> settings() {
        int[] angles = new int[]{10, 30, 60, 90, 180, 270, 360};
        String[] anglesDesc = new String[]{"10", "30", "60", "90", "180", "270", "360"};
        return List.of(new SettingDefinition("forks", "focus.scatter.forks", new SettingDefinition.IntRange(2, 10)),
                new SettingDefinition("cone", "focus.scatter.cone", new SettingDefinition.IntList(angles, anglesDesc)));
    }

    @Override
    public Set<SupplyType> requires() {
        return SUPPLIES_TRAJECTORIES;
    }

    @Override
    public Set<SupplyType> supplies() {
        return SUPPLIES_TRAJECTORIES;
    }

    @Override
    public CastStreams modify(CastContext ctx, FocusSettings settings, CastStreams incoming) {
        Trajectory[] supplied = incoming.trajectories();
        List<Trajectory> out = new ArrayList<>();
        if (supplied != null) {
            int forks = settings.value("forks");
            int angle = settings.value("cone");
            RandomSource rand = ctx.level().getRandom();
            for (Trajectory sT : supplied) {
                for (int a = 0; a < forks; a++) {
                    Vec3 direction = sT.direction().normalize().add(rand.nextGaussian() * JITTER_PER_DEGREE * angle, rand.nextGaussian() * JITTER_PER_DEGREE * angle,
                            rand.nextGaussian() * JITTER_PER_DEGREE * angle);
                    out.add(new Trajectory(sT.source(), direction.normalize()));
                }
            }
        }
        return new CastStreams(out.toArray(new Trajectory[0]), null);
    }

    @Override
    public float powerMultiplier(FocusSettings settings) {
        return 1.0F / (settings.value("forks") / POWER_FORKS_DIVISOR);
    }

    @Override
    public boolean exclusive() {
        return true;
    }
}
