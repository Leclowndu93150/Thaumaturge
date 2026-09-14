package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.Objects;

/** Context-evaluated resources required by an arcane craft. */
public record ArcanePaymentSummary(int auraVisRequired, AspectList crystalsRequired, boolean affordable) {
    public ArcanePaymentSummary {
        if (auraVisRequired < 0) throw new IllegalArgumentException("auraVisRequired must not be negative");
        Objects.requireNonNull(crystalsRequired, "crystalsRequired");
    }

    public static ArcanePaymentSummary from(ArcaneCraftCost cost) {
        return new ArcanePaymentSummary(cost.auraVis(), cost.crystalsNeeded(), cost.affordable());
    }
}
