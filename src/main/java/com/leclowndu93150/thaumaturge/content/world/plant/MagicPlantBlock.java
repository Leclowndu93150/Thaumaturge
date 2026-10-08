package com.leclowndu93150.thaumaturge.content.world.plant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class MagicPlantBlock extends AbstractTTPlant {
    public static final MapCodec<MagicPlantBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    TagKey.codec(Registries.BLOCK).fieldOf("soil").forGetter(plant -> plant.soil),
                    PlantAura.CODEC.fieldOf("aura").forGetter(plant -> plant.aura),
                    PlantContactEffect.CODEC.optionalFieldOf("contact_effect").forGetter(plant -> plant.contactEffect),
                    propertiesCodec())
            .apply(instance, MagicPlantBlock::new));

    private final TagKey<Block> soil;
    private final PlantAura aura;
    private final Optional<PlantContactEffect> contactEffect;

    public MagicPlantBlock(
            TagKey<Block> soil,
            PlantAura aura,
            Optional<PlantContactEffect> contactEffect,
            BlockBehaviour.Properties properties) {
        super(properties);
        this.soil = soil;
        this.aura = aura;
        this.contactEffect = contactEffect;
    }

    @Override
    protected MapCodec<MagicPlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(soil);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        aura.emit(level, pos.getBottomCenter().add(state.getOffset(level, pos)), random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        contactEffect.ifPresent(effect -> effect.touch(level, entity));
    }
}
