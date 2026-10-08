package com.leclowndu93150.thaumaturge.content.eldritch.guardian;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

public record PostState(int index, Optional<String> ward, BlockPos pos, List<UUID> guards, boolean cleared) {
    public static final Codec<PostState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("index").forGetter(PostState::index),
                    Codec.STRING.optionalFieldOf("ward").forGetter(PostState::ward),
                    BlockPos.CODEC.fieldOf("pos").forGetter(PostState::pos),
                    UUIDUtil.CODEC.listOf().fieldOf("guards").forGetter(PostState::guards),
                    Codec.BOOL.fieldOf("cleared").forGetter(PostState::cleared))
            .apply(instance, PostState::new));

    PostState clear() {
        return new PostState(index, ward, pos, guards, true);
    }

    boolean guards(String group) {
        return ward.filter(group::equals).isPresent();
    }
}
