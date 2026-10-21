package com.leclowndu93150.thaumaturge.api.golems.duty;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * An exclusive, server-side accessory duty. Register during common setup through {@link GolemDuties}.
 * State belongs to one golem and is persisted with {@link #version()}; incompatible saved versions
 * safely leave the duty. Callbacks run on the server thread and must not retain the context.
 *
 * @since 1.0.0
 */
public interface GolemDuty {
    Identifier id();

    /** The accessory that must remain equipped throughout the duty. */
    Identifier accessory();

    int version();

    default CompoundTag initialState() {
        return new CompoundTag();
    }

    /** Updates persistent state and issues movement or combat requests through the context. */
    void tick(Context context, CompoundTag state);

    /** Called after the previous home and normal behavior have been restored. */
    default void stopped(IGolemAPI golem, CompoundTag state, StopReason reason) {}

    enum StopReason {
        REQUESTED, ACCESSORY_REMOVED, OWNER_CHANGED, OWNER_UNAVAILABLE, REMOVED, UNLOADED, CALLBACK_FAILED
    }

    /** Requests are limited to loaded positions within 32 blocks of the online owner. */
    interface Context {
        IGolemAPI golem();

        ServerPlayer owner();

        /** Returns false for unloaded, protected or out-of-range destinations. */
        boolean moveTo(BlockPos position);

        /** Returns false for allies, protected positions, invalid targets or disabled PvP. */
        boolean attack(LivingEntity target);

        void stop();
    }
}
