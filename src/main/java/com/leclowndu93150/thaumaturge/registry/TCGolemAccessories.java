package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;

public final class TCGolemAccessories {
    public static final GolemAccessory TOP_HAT = GolemAccessories.register(new GolemAccessory(TCIds.rl("top_hat"), GolemAccessory.Group.HAT, 5, 1.0F, 1.0F, 1.0F, 0, false));
    public static final GolemAccessory FEZ = GolemAccessories.register(new GolemAccessory(TCIds.rl("fez"), GolemAccessory.Group.HAT, 0, 1.0F, 1.0F, 0.66F, 0, false));
    public static final GolemAccessory GLASSES = GolemAccessories.register(new GolemAccessory(TCIds.rl("glasses"), GolemAccessory.Group.EYES, 0, 1.1F, 1.0F, 1.0F, 0, false));
    public static final GolemAccessory BOWTIE = GolemAccessories.register(new GolemAccessory(TCIds.rl("bowtie"), GolemAccessory.Group.NONE, 0, 1.0F, 1.1F, 1.0F, 0, false));
    public static final GolemAccessory VISOR = GolemAccessories.register(new GolemAccessory(TCIds.rl("visor"), GolemAccessory.Group.EYES, 0, 1.0F, 1.0F, 1.0F, 1, true));

    private TCGolemAccessories() {}
}
