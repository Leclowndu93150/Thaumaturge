package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;

public final class TCGolemAccessories {
    public static final GolemAccessory TOP_HAT =
            new GolemAccessory(TCIds.rl("top_hat"), GolemAccessory.Group.HAT, 5, 1.0F, 1.0F, 1.0F, 0, false);
    public static final GolemAccessory FEZ =
            new GolemAccessory(TCIds.rl("fez"), GolemAccessory.Group.HAT, 0, 1.0F, 1.0F, 0.66F, 0, false);
    public static final GolemAccessory GLASSES =
            new GolemAccessory(TCIds.rl("glasses"), GolemAccessory.Group.EYES, 0, 1.1F, 1.0F, 1.0F, 0, false);
    public static final GolemAccessory BOWTIE =
            new GolemAccessory(TCIds.rl("bowtie"), GolemAccessory.Group.NONE, 0, 1.0F, 1.1F, 1.0F, 0, false);
    public static final GolemAccessory VISOR =
            new GolemAccessory(TCIds.rl("visor"), GolemAccessory.Group.EYES, 0, 1.0F, 1.0F, 1.0F, 1, true);

    private TCGolemAccessories() {}

    public static void register() {
        GolemAccessories.register(TOP_HAT);
        GolemAccessories.register(FEZ);
        GolemAccessories.register(GLASSES);
        GolemAccessories.register(BOWTIE);
        GolemAccessories.register(VISOR);
    }
}
