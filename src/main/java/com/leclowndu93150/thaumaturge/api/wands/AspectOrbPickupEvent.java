package com.leclowndu93150.thaumaturge.api.wands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Fired on the game event bus before a server-side orb pickup. The base applies the final
 * credit atomically. Listeners must not charge the wand themselves. The source is consumed only
 * when the entire credit fits; zero credit refuses collection. Fractional bonuses are expressed
 * as integer centivis and rounded by the listener before setting the credit.
 */
public final class AspectOrbPickupEvent extends Event {
    private final ServerPlayer player;
    private final ItemStack wand;
    private final AspectOrbSnapshot orb;
    private final int capacityCentivis;
    private int creditCentivis;

    public AspectOrbPickupEvent(ServerPlayer player, ItemStack wand, AspectOrbSnapshot orb, int capacityCentivis, int creditCentivis) {
        this.player = player;
        this.wand = wand.copy();
        this.orb = orb;
        this.capacityCentivis = capacityCentivis;
        this.creditCentivis = creditCentivis;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    /** Returns a defensive copy of the selected recipient. */
    public ItemStack getWand() {
        return wand.copy();
    }

    public AspectOrbSnapshot getOrb() {
        return orb;
    }

    public int getCreditCentivis() {
        return creditCentivis;
    }

    public int getCapacityCentivis() {
        return capacityCentivis;
    }

    /** Sets the credit, bounded by the selected recipient's effective capacity. */
    public void setCreditCentivis(int centivis) {
        if (centivis < 0 || centivis > capacityCentivis) {
            throw new IllegalArgumentException("Orb credit must be between zero and wand capacity");
        }
        creditCentivis = centivis;
    }
}
