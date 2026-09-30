package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.Optional;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public enum HeldCandle implements StringRepresentable {
    NONE(null),
    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK);

    private final DyeColor dye;

    HeldCandle(DyeColor dye) {
        this.dye = dye;
    }

    public Optional<DyeColor> dye() {
        return Optional.ofNullable(dye);
    }

    public boolean isPresent() {
        return dye != null;
    }

    public Optional<BlockCandle> candle() {
        return dye().map(color -> TCBlocks.CANDLES.get(color).get());
    }

    public ItemStack toStack() {
        return dye().map(color -> new ItemStack(TCItems.CANDLES.get(color).get()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public String getSerializedName() {
        return dye == null ? "none" : dye.getSerializedName();
    }

    public static Optional<HeldCandle> of(ItemStack stack) {
        for (HeldCandle held : values()) {
            if (held.isPresent() && stack.is(TCItems.CANDLES.get(held.dye).get())) {
                return Optional.of(held);
            }
        }
        return Optional.empty();
    }
}
