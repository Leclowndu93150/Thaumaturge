package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.seals.SealPlacement;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;

public final class SealPlacements {
    public static final SealPlacement CONTAINER = (level, pos, face) -> InvHelper.getItemHandlerAt(level, pos, face) != null;
    public static final SealPlacement ANYWHERE = (level, pos, face) -> true;

    private SealPlacements() {}
}
