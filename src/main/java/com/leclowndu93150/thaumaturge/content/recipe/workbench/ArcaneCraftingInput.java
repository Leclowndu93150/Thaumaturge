package com.leclowndu93150.thaumaturge.content.recipe.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;

public class ArcaneCraftingInput implements IArcaneCraftingInput {
    public static final ArcaneCraftingInput EMPTY = new ArcaneCraftingInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final StackedItemContents stackedContents = new StackedItemContents();
    private final int ingredientCount;
    private Player player;

    protected ArcaneCraftingInput(int width, int height, List<ItemStack> items) {
        this.width = width;
        this.height = height;
        this.items = items;
        int ingredientCount = 0;

        if (items.isEmpty()) {
            this.ingredientCount = 0;
            return;
        }

        for (ItemStack item : items.subList(0, width * height)) {
            if (!item.isEmpty()) {
                ++ingredientCount;
                this.stackedContents.accountStack(item, 1);
            }
        }

        this.ingredientCount = ingredientCount;
    }

    public static ArcaneCraftingInput of(int width, int height, List<ItemStack> items) {
        return ofPositioned(width, height, items).input();
    }

    public static ArcaneCraftingInput.Positioned ofPositioned(int width, int height, List<ItemStack> items) {
        if (width != 0 && height != 0) {
            int left = width - 1;
            int right = 0;
            int top = height - 1;
            int bottom = 0;

            for (int y = 0; y < height; ++y) {
                boolean rowEmpty = true;

                for (int x = 0; x < width; ++x) {
                    ItemStack item = items.get(x + y * width);
                    if (!item.isEmpty()) {
                        left = Math.min(left, x);
                        right = Math.max(right, x);
                        rowEmpty = false;
                    }
                }

                if (!rowEmpty) {
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }

            int newWidth = right - left + 1;
            int newHeight = bottom - top + 1;
            if (newWidth > 0 && newHeight > 0) {
                if (newWidth == width && newHeight == height) {
                    return new ArcaneCraftingInput.Positioned(new ArcaneCraftingInput(width, height, items), left, top);
                } else {
                    List<ItemStack> newItems = new ArrayList<>(newWidth * newHeight + 6);

                    for (int y = 0; y < newHeight; ++y) {
                        for (int xx = 0; xx < newWidth; ++xx) {
                            int index = xx + left + (y + top) * width;
                            newItems.add(items.get(index));
                        }
                    }

                    for (int i = 9; i < 16; i++) {
                        newItems.add(items.get(i));
                    }

                    return new ArcaneCraftingInput.Positioned(new ArcaneCraftingInput(newWidth, newHeight, newItems), left, top);
                }
            } else {
                return ArcaneCraftingInput.Positioned.EMPTY;
            }
        } else {
            return ArcaneCraftingInput.Positioned.EMPTY;
        }
    }

    public ArcaneCraftingInput withPlayer(Player player) {
        this.player = player;
        return this;
    }

    public @Nullable Player player() {
        return player;
    }

    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    public ItemStack getItem(int x, int y) {
        return this.items.get(x + y * this.width);
    }

    public ItemStack wandStack() {
        if (items.isEmpty())
            return ItemStack.EMPTY;
        return items.getLast();
    }

    public int size() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.ingredientCount == 0;
    }

    public StackedItemContents stackedContents() {
        return this.stackedContents;
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public int ingredientCount() {
        return this.ingredientCount;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            boolean var10000;
            if (obj instanceof ArcaneCraftingInput) {
                ArcaneCraftingInput input = (ArcaneCraftingInput) obj;
                var10000 = this.width == input.width && this.height == input.height && this.ingredientCount == input.ingredientCount && ItemStack.listMatches(this.items, input.items);
            } else {
                var10000 = false;
            }

            return var10000;
        }
    }

    public int hashCode() {
        int result = ItemStack.hashStackList(this.items);
        result = 31 * result + this.width;
        return 31 * result + this.height;
    }

    @Override
    public AspectList availableCrystals() {
        if (items.size() < 10 && height == 3 && width == 3)
            return AspectList.EMPTY;
        if (items.size() - 7 < 0)
            return AspectList.EMPTY;
        List<ItemStack> crystals = items.subList(items.size() - 7, items.size() - 1);
        List<AspectInstance> aspects = new ArrayList<>();
        for (ItemStack crystal : crystals) {
            if (crystal.is(TCItems.ESSENTIA_CRYSTAL) && crystal.has(TCDataComponents.CRYSTAL_ASPECT)) {
                Holder<IAspect> aspect = crystal.get(TCDataComponents.CRYSTAL_ASPECT.get()).aspect();
                aspects.add(new AspectInstance(aspect, crystal.count()));
            }
        }
        return AspectList.ofEntries(aspects);
    }

    public static record Positioned(ArcaneCraftingInput input, int left, int top) {
        public static final ArcaneCraftingInput.Positioned EMPTY = new ArcaneCraftingInput.Positioned(ArcaneCraftingInput.EMPTY, 0, 0);
    }
}
