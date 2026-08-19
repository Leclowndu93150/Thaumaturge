package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityCondenser;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ItemResonator extends Item {
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH_BASE = 1.9F;
    private static final int TICKS_PER_SECOND = 20;

    public ItemResonator(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        IEssentiaTransport transport = level.getCapability(EssentiaCapabilities.TRANSPORT, pos, side);
        if (transport == null || player == null) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityTubeBuffer) {
            IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, pos, side);
            if (container != null) {
                for (AspectInstance entry : container.getAspects().sortedByTag()) {
                    player.sendSystemMessage(Component.translatable("tc.resonator1", String.valueOf(entry.amount()), AspectComponents.name(entry.aspect())));
                }
            }
        } else if (transport.getEssentiaType(side) != null) {
            Holder<IAspect> type = transport.getEssentiaType(side);
            player.sendSystemMessage(Component.translatable("tc.resonator1", String.valueOf(transport.getEssentiaAmount(side)), AspectComponents.name(type)));
        }
        Holder<IAspect> suction = transport.getSuctionType(side);
        Component suctionName = suction != null ? AspectComponents.name(suction) : Component.translatable("tc.resonator3");
        player.sendSystemMessage(Component.translatable("tc.resonator2", String.valueOf(transport.getSuctionAmount(side)), suctionName));
        level.playSound(null, pos, SoundEvents.SHIELD_BLOCK.value(), SoundSource.BLOCKS, SOUND_VOLUME, SOUND_PITCH_BASE + level.getRandom().nextFloat() * 0.1F);
        if (tile instanceof BlockEntityCondenser condenser) {
            player.sendSystemMessage(Component.translatable("tc.condenser1", String.valueOf(condenser.cost())));
            player.sendSystemMessage(Component.translatable("tc.condenser2", String.valueOf(condenser.interval()), String.valueOf(condenser.interval() / TICKS_PER_SECOND)));
        }
        return InteractionResult.SUCCESS;
    }
}
