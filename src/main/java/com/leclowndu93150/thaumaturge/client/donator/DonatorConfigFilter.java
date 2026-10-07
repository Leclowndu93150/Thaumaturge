package com.leclowndu93150.thaumaturge.client.donator;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import javax.annotation.Nullable;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Context;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Element;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen.Filter;

public final class DonatorConfigFilter implements Filter {
    @Override
    public @Nullable Element filterEntry(Context context, String key, Element original) {
        boolean donatorOnly = context.modSpec() == ThaumaturgeClientConfig.SPEC
                && context.keylist().isEmpty()
                && ThaumaturgeClientConfig.DONATOR_CAPE_KEY.equals(key);
        return donatorOnly && !DonatorCapeClient.isLocalDonator() ? null : original;
    }
}
