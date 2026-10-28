package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class PhialItem extends Item {
    public static final int BASE_AMOUNT = 10;

    private static final Direction CONTAINER_FACE = Direction.UP;
    private static final String EMPTY_SUFFIX = ".empty";
    private static final String FILLED_SUFFIX = ".filled";
    private static final float SOUND_VOLUME = 0.25F;
    private static final float SOUND_PITCH = 1.0F;

    public PhialItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack makeFilled(Holder<IAspect> aspect) {
        return makeFilled(aspect, BASE_AMOUNT);
    }

    public static ItemStack makeFilled(Holder<IAspect> aspect, int amount) {
        ItemStack phial = TTItems.PHIAL.get().getDefaultInstance();
        ComponentEssentia.phial(phial).setAspects(AspectList.of(new AspectInstance(aspect, amount)));
        return phial;
    }

    @Override
    public Component getName(ItemStack stack) {
        List<AspectInstance> contents =
                ComponentEssentia.phial(stack).getAspects().entries();
        if (contents.isEmpty()) {
            return Component.translatable(getDescriptionId() + EMPTY_SUFFIX);
        }
        return Component.translatable(
                getDescriptionId() + FILLED_SUFFIX,
                AspectComponents.name(contents.getFirst().aspect()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || !(level.getBlockEntity(pos) instanceof IEssentiaConduit container)) {
            return InteractionResult.PASS;
        }
        boolean jar = container instanceof AbstractBlockEntityJar;
        if (!jar && !(container instanceof BlockEntityAlembic)) {
            return InteractionResult.PASS;
        }
        ItemStack held = context.getItemInHand();
        AspectList stored = ComponentEssentia.phial(held).getAspects();
        Interaction interaction = new Interaction(level, pos, player, context.getHand(), held);
        if (stored.isEmpty()) {
            return interaction.draw(container);
        }
        return jar ? interaction.store(container, stored.entries().getFirst()) : InteractionResult.PASS;
    }

    private enum Flow {
        WITHDRAW,
        DEPOSIT
    }

    private record Transfer(IEssentiaConduit container, Holder<IAspect> aspect, int amount, Flow flow) {
        int apply(boolean simulate) {
            return flow == Flow.DEPOSIT
                    ? container.insert(aspect, amount, CONTAINER_FACE, simulate)
                    : container.extract(aspect, amount, CONTAINER_FACE, simulate);
        }

        void revert(int moved) {
            if (moved <= 0) {
                return;
            }
            if (flow == Flow.DEPOSIT) {
                container.extract(aspect, moved, CONTAINER_FACE, false);
            } else {
                container.insert(aspect, moved, CONTAINER_FACE, false);
            }
        }

        void settle(Player player, InteractionHand hand, ItemStack held) {
            boolean withdraw = flow == Flow.WITHDRAW;
            if (!withdraw && player.getAbilities().instabuild) {
                return;
            }
            spendOne(player, hand, held);
            give(player, withdraw ? makeFilled(aspect) : new ItemStack(TTItems.PHIAL.get()));
        }
    }

    private record Interaction(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held) {
        InteractionResult draw(IEssentiaConduit container) {
            Holder<IAspect> aspect = container.heldAspect(CONTAINER_FACE);
            if (aspect == null || container.heldAmount(CONTAINER_FACE) < BASE_AMOUNT) {
                return InteractionResult.PASS;
            }
            return run(new Transfer(container, aspect, BASE_AMOUNT, Flow.WITHDRAW));
        }

        InteractionResult store(IEssentiaConduit container, AspectInstance contents) {
            if (container.roomFor(contents.aspect(), CONTAINER_FACE) < contents.amount()) {
                return InteractionResult.PASS;
            }
            return run(new Transfer(container, contents.aspect(), contents.amount(), Flow.DEPOSIT));
        }

        private InteractionResult preview(Transfer transfer) {
            boolean possible = transfer.apply(true) == transfer.amount();
            if (possible) {
                player.swing(hand);
            }
            return possible ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        private InteractionResult run(Transfer transfer) {
            if (level.isClientSide()) {
                return preview(transfer);
            }
            int moved = transfer.apply(false);
            boolean complete = moved == transfer.amount();
            if (complete) {
                transfer.settle(player, hand, held);
                level.playSound(null, pos, TTSounds.JAR.get(), SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH);
            } else {
                transfer.revert(moved);
            }
            return complete ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }

    private static void spendOne(Player player, InteractionHand hand, ItemStack held) {
        if (player.getAbilities().instabuild) {
            return;
        }
        held.shrink(1);
        if (held.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
