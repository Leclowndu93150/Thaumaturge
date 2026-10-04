package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;

public final class ItemMatchSettings {
    public static final SealSetting MATCH_DAMAGE = new SealSetting("pmeta", "golem.prop.meta", true);
    public static final SealSetting MATCH_COMPONENTS = new SealSetting("pnbt", "golem.prop.nbt", true);
    public static final SealSetting MATCH_TAGS = new SealSetting("pore", "golem.prop.ore", false);
    public static final SealSetting MATCH_MOD = new SealSetting("pmod", "golem.prop.mod", false);
    public static final SealSetting[] ALL = {MATCH_DAMAGE, MATCH_COMPONENTS, MATCH_TAGS, MATCH_MOD};

    private ItemMatchSettings() {}

    public static InvHelper.InvFilter of(ISealEntity seal) {
        return new InvHelper.InvFilter(!seal.setting(MATCH_DAMAGE), !seal.setting(MATCH_COMPONENTS), seal.setting(MATCH_TAGS), seal.setting(MATCH_MOD));
    }
}
