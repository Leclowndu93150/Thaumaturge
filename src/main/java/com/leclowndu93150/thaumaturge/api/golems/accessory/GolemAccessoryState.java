package com.leclowndu93150.thaumaturge.api.golems.accessory;

import net.minecraft.nbt.CompoundTag;

/**
 * Namespaced accessory state. Snapshots are defensive; persistent state is saved and synchronized
 * state is explicitly bounded for rendering/status. Mutators are server-thread only.
 */
public interface GolemAccessoryState {
    CompoundTag persistentData();

    void setPersistentData(CompoundTag data);

    CompoundTag synchronizedData();

    void setSynchronizedData(CompoundTag data);

    void clear();
}
