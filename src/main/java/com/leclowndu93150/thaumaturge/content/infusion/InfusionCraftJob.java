package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

public final class InfusionCraftJob {
    public static final Codec<InfusionCraftJob> CODEC = RecordCodecBuilder.create(i -> i.group(ItemStack.CODEC.listOf().fieldOf("ingredients").forGetter(j -> j.ingredients),
            AspectList.CODEC.fieldOf("essentia").forGetter(j -> j.essentia), ItemStack.CODEC.fieldOf("result").forGetter(j -> j.result), ItemStack.CODEC.fieldOf("catalyst").forGetter(j -> j.catalyst),
            Codec.INT.fieldOf("instability").forGetter(j -> j.instability), UUIDUtil.CODEC.optionalFieldOf("player").forGetter(j -> j.player)).apply(i, InfusionCraftJob::new));

    private final List<ItemStack> ingredients;
    private AspectList essentia;
    private final ItemStack result;
    private final ItemStack catalyst;
    private final int instability;
    private final Optional<UUID> player;

    public InfusionCraftJob(List<ItemStack> ingredients, AspectList essentia, ItemStack result, ItemStack catalyst, int instability, Optional<UUID> player) {
        this.ingredients = new ArrayList<>(ingredients);
        this.essentia = essentia;
        this.result = result;
        this.catalyst = catalyst;
        this.instability = instability;
        this.player = player;
    }

    public List<ItemStack> ingredients() {
        return ingredients;
    }

    public AspectList essentia() {
        return essentia;
    }

    public void setEssentia(AspectList essentia) {
        this.essentia = essentia;
    }

    public ItemStack result() {
        return result;
    }

    public ItemStack catalyst() {
        return catalyst;
    }

    public int instability() {
        return instability;
    }

    public Optional<UUID> player() {
        return player;
    }
}
