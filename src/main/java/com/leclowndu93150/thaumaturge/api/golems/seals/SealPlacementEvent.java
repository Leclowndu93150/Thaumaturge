package com.leclowndu93150.thaumaturge.api.golems.seals;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Server-thread permission check posted on the NeoForge event bus before a seal is placed or paid
 * for. Protection integrations may cancel placement on the supporting block's indicated face.
 * Placement adds an attachment; it never replaces or breaks a block. The hand and placement are
 * revalidated after listeners return. Listeners should only inspect and veto the operation.
 */
public final class SealPlacementEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final SealPos position;
    private final Identifier type;
    private final ItemStack placer;

    public SealPlacementEvent(ServerPlayer player, SealPos position, Identifier type, ItemStack placer) {
        this.player = player;
        this.position = position;
        this.type = type;
        this.placer = placer.copy();
    }

    public ServerPlayer player() {
        return player;
    }

    public SealPos position() {
        return position;
    }

    public Identifier type() {
        return type;
    }

    public ItemStack placer() {
        return placer.copy();
    }
}
