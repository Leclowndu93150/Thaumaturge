package com.leclowndu93150.thaumaturge.api.rifts;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Controlled server transfers preserving a rift's identity and one-time release authority. */
public final class RiftTransfers {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RiftTransfers");

    private RiftTransfers() {}

    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public static Optional<CapturedRift> captured(ItemStack stack) {
        return BINDING.get().captured(stack);
    }

    /**
     * Quotes the actual jar and payment slots without changing the entity or inventories.
     * Nonzero payment requires a player's main-inventory jar and built-in component-backed wand;
     * custom storage and delegated machine payments are not supported.
     */
    public static Optional<Quote> prepareCapture(RiftOperationContext context, UUID identity, long revision, int jarSlot, int wandSlot) {
        return BINDING.get().prepareCapture(context, identity, revision, jarSlot, wandSlot);
    }

    /** A pending payment checkpoint returns BUSY and suspends the source until recovery resolves it. */
    public static RiftResult tryCapture(RiftOperationContext context, Quote quote) {
        return BINDING.get().tryCapture(context, quote);
    }

    public static RiftResult tryRelease(RiftOperationContext context, int jarSlot, Vec3 destination) {
        return BINDING.get().tryRelease(context, jarSlot, destination);
    }

    /** Recovers an interrupted transfer for its original owner; duplicated carriers share one authority. */
    public static RiftResult recover(RiftOperationContext context, UUID identity, int jarSlot) {
        return BINDING.get().recover(context, identity, jarSlot);
    }

    /** Opaque preparation bound to one authority, entity revision, inventory and server tick. */
    public interface Quote {
        UUID identity();
        long revision();
    }

    public interface Bindings {
        Optional<CapturedRift> captured(ItemStack stack);
        Optional<Quote> prepareCapture(RiftOperationContext context, UUID identity, long revision, int jarSlot, int wandSlot);
        RiftResult tryCapture(RiftOperationContext context, Quote quote);
        RiftResult tryRelease(RiftOperationContext context, int jarSlot, Vec3 destination);
        RiftResult recover(RiftOperationContext context, UUID identity, int jarSlot);
    }
}
