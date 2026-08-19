package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.jspecify.annotations.Nullable;

/**
 * A golem arm part.
 *
 * @since 1.0.0
 */
public final class GolemArm extends GolemPart {
    /** The registry key for golem arms. */
    public static final ResourceKey<Registry<GolemArm>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "golem_arm"));

    private final IArmFunction function;

    /**
     * @param research   research entries gating these arms; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param model      the model rendered for these arms
     * @param components the crafting components consumed
     * @param function   the behavior attached to these arms, or null when they have none
     * @param traits     traits granted by these arms
     */
    public GolemArm(List<Identifier> research, Identifier icon, @Nullable GolemPartModel model, List<GolemComponent> components, @Nullable IArmFunction function, List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, model);
        this.function = function;
    }

    @Override
    public @Nullable IArmFunction function() {
        return function;
    }

    /**
     * Behavior attached to golem arms, covering melee and ranged combat hooks.
     *
     * @since 1.0.0
     */
    public interface IArmFunction extends IGolemFunction {
        /**
         * Called after the golem lands a melee hit.
         *
         * @param golem  the attacking golem
         * @param target the entity that was hit
         */
        void onMeleeAttack(IGolemAPI golem, Entity target);

        /**
         * Called when the golem performs a ranged attack.
         *
         * @param golem  the attacking golem
         * @param target the attack target
         * @param power  attack charge in the range 0.1 to 1
         */
        void onRangedAttack(IGolemAPI golem, LivingEntity target, float power);

        /**
         * @param mob the golem, which implements {@link RangedAttackMob}
         * @return the ranged attack goal to install, or null when these arms provide none
         */
        @Nullable
        Goal createRangedAttackGoal(RangedAttackMob mob);
    }
}
