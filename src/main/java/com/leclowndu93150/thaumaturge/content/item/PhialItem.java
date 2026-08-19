package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.api.aspect.*;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class PhialItem extends Item implements IEssentiaContainerItem {
    public static final int BASE_AMOUNT = 10;

    public PhialItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack makeFilled(Holder<IAspect> aspect, int amount) {
        ItemStack stack = new ItemStack(TCItems.PHIAL.get());
        stack.set(TCDataComponents.ASPECTS.get(), AspectList.of(new AspectInstance(aspect, amount)));
        return stack;
    }

    public static ItemStack makeFilled(Holder<IAspect> aspect) {
        return makeFilled(aspect, BASE_AMOUNT);
    }

    @Override
    public Component getName(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects.isEmpty()) {
            return Component.translatable(this.getDescriptionId() + ".empty");
        }
        Holder<IAspect> first = aspects.entries().getFirst().aspect();
        MutableComponent aspectName = AspectComponents.name(first);
        return Component.translatable(this.getDescriptionId() + ".filled", aspectName);
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        AspectList stored = stack.get(TCDataComponents.ASPECTS.get());
        return stored == null ? AspectList.EMPTY : stored;
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) {
            stack.remove(TCDataComponents.ASPECTS.get());
            return;
        }
        stack.set(TCDataComponents.ASPECTS.get(), aspects);
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null)
            return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof BlockEntityJar jar) {
            return interactWith(stack, player, context.getHand(), level, pos, jar, true);
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityAlembic alembic) {
            return interactWith(stack, player, context.getHand(), level, pos, alembic, false);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult interactWith(ItemStack stack, Player player, InteractionHand hand, Level level, BlockPos pos, IEssentiaTransport container, boolean canDeposit) {
        AspectList aspects = getAspects(stack);
        // We use Direction.UP to allow insertion/extraction from all faces with fials
        if (aspects.isEmpty()) {
            if (container.getEssentiaAmount(Direction.UP) >= BASE_AMOUNT) {
                if (level.isClientSide()) {
                    player.swing(hand);
                    return InteractionResult.SUCCESS;
                }
                Holder<IAspect> aspect = container.getEssentiaType(Direction.UP);
                if (container.takeEssentia(aspect, BASE_AMOUNT, Direction.UP) == BASE_AMOUNT) {
                    if (!player.getAbilities().instabuild)
                        stack.shrink(1);
                    ItemStack phial = makeFilled(aspect);
                    if (!player.addItem(phial)) {
                        player.drop(phial, false);
                    }
                    level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.25f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (canDeposit) {
            AspectInstance first = aspects.entries().getFirst();
            if (container.getEssentiaAmount(Direction.UP) + first.amount() <= BlockEntityJar.CAPACITY) {
                if (level.isClientSide()) {
                    player.swing(hand);
                    return InteractionResult.SUCCESS;
                }
                if (container.addEssentia(first.aspect(), first.amount(), Direction.UP) == first.amount()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        ItemStack empty = new ItemStack(TCItems.PHIAL.get());
                        if (!player.addItem(empty)) {
                            player.drop(empty, false);
                        }
                    }
                    level.playSound(null, pos, TCSounds.JAR.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
