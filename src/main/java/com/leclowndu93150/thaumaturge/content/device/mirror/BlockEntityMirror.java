package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirror extends BlockEntityMirrorBase {
    private static final String ITEMS_KEY = "Items";
    private static final String INBOX_KEY = "Inbox";
    private static final int INSTABILITY_THRESHOLD = 128;
    private static final int INSTABILITY_PER_ITEM = 1;
    private static final int EJECT_MIN_AGE = 20;
    private static final double EJECT_HEIGHT = 0.25;
    private static final double EJECT_SPEED = 0.15;
    private static final double CENTER = 0.5;
    private static final int PORTAL_COOLDOWN = 20;
    private static final int INBOX_SLOT = 0;
    private static final int MOTE_COUNT = 4;
    private static final int MOTE_AGE = 24;
    private static final double MOTE_INSET = 0.3;
    private static final double MOTE_DRIFT = 0.02;
    private static final double MOTE_JITTER = 0.01;
    private static final float MOTE_RED = 0.42F;
    private static final float MOTE_GREEN = 0.3F;
    private static final float MOTE_BLUE = 0.58F;

    private final List<ItemStack> queue = new ArrayList<>();
    private final MirrorInbox inbox = new MirrorInbox();
    private int age;

    public BlockEntityMirror(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MIRROR.get(), pos, state);
    }

    @Override
    protected int instabilityThreshold() {
        return INSTABILITY_THRESHOLD;
    }

    @Override
    protected boolean isSameKind(BlockEntity other) {
        return other instanceof BlockEntityMirror;
    }

    public ResourceHandler<ItemResource> inbox() {
        return inbox;
    }

    public void serverTick(Level level, BlockPos pos) {
        age++;
        forwardInbox(level, pos);
        if (!queue.isEmpty() && age > EJECT_MIN_AGE) {
            eject(level, pos);
        }
        tickLink();
    }

    public void transport(ItemEntity dropped) {
        if (!(level instanceof ServerLevel server) || dropped.getItem().isEmpty()) {
            return;
        }
        BlockEntityMirror partner = linkedMirror();
        if (partner == null) {
            return;
        }
        partner.addStack(dropped.getItem());
        dropped.discard();
        pileOn(INSTABILITY_PER_ITEM);
        puff(server, worldPosition);
    }

    public boolean transportDirect(ItemStack stack) {
        boolean accepted = !stack.isEmpty();
        if (accepted) {
            addStack(stack);
        }
        return accepted;
    }

    public void addStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            queue.add(stack.copy());
            setChanged();
        }
    }

    private void forwardInbox(Level level, BlockPos pos) {
        int amount = inbox.getAmountAsInt(INBOX_SLOT);
        if (amount <= 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        ItemStack stack = inbox.getResource(INBOX_SLOT).toStack(amount);
        inbox.set(INBOX_SLOT, ItemResource.EMPTY, 0);
        BlockEntityMirror partner = linkedMirror();
        if (partner == null) {
            launch(server, pos, stack);
            return;
        }
        partner.addStack(stack);
        pileOn(INSTABILITY_PER_ITEM);
        puff(server, pos);
    }

    private void eject(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int index = server.getRandom().nextInt(queue.size());
        ItemStack source = queue.get(index);
        ItemStack single = source.split(1);
        if (source.isEmpty()) {
            queue.remove(index);
        }
        setChanged();
        if (single.isEmpty()) {
            return;
        }
        launch(server, pos, single);
        pileOn(INSTABILITY_PER_ITEM);
    }

    private void launch(ServerLevel level, BlockPos pos, ItemStack stack) {
        Direction facing = getBlockState().getValue(BlockMirror.FACING);
        ItemEntity thrown = new ItemEntity(level, pos.getX() + CENTER, pos.getY() + CENTER + EJECT_HEIGHT, pos.getZ() + CENTER, stack, facing.getStepX() * EJECT_SPEED, facing.getStepY() * EJECT_SPEED,
                facing.getStepZ() * EJECT_SPEED);
        thrown.setPortalCooldown(PORTAL_COOLDOWN);
        level.addFreshEntity(thrown);
        puff(level, pos);
    }

    private void puff(ServerLevel level, BlockPos pos) {
        Direction facing = getBlockState().getValue(BlockMirror.FACING);
        RandomSource random = level.getRandom();
        Vec3 out = facing.getUnitVec3();
        Vec3 face = Vec3.atCenterOf(pos).subtract(out.scale(MOTE_INSET));
        for (int mote = 0; mote < MOTE_COUNT; mote++) {
            Vec3 drift = out.scale(MOTE_DRIFT).add(random.triangle(0.0, MOTE_JITTER), random.triangle(0.0, MOTE_JITTER), random.triangle(0.0, MOTE_JITTER));
            Effects.wispyMotes(level, face).color(MOTE_RED, MOTE_GREEN, MOTE_BLUE).age(MOTE_AGE).motion(drift.x, drift.y, drift.z).send();
        }
    }

    private boolean acceptsInserts() {
        return level != null && !level.isClientSide() && partnerAvailable() && pairingIntact();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        queue.clear();
        input.listOrEmpty(ITEMS_KEY, ItemStack.CODEC).forEach(queue::add);
        input.child(INBOX_KEY).ifPresent(inbox::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput.TypedOutputList<ItemStack> items = output.list(ITEMS_KEY, ItemStack.CODEC);
        queue.stream().filter(stack -> !stack.isEmpty()).forEach(items::add);
        inbox.serialize(output.child(INBOX_KEY));
    }

    private @Nullable BlockEntityMirror linkedMirror() {
        if (!partnerAvailable() || !verifyPairing()) {
            return null;
        }
        return partner() instanceof BlockEntityMirror mirror ? mirror : null;
    }

    private final class MirrorInbox extends ItemStacksResourceHandler {
        private MirrorInbox() {
            super(INBOX_SLOT + 1);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return acceptsInserts();
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}
