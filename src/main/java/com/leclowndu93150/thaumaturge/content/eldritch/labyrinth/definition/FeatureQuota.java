package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.IntProvider;

public record FeatureQuota(HolderSet<RoomType> rooms, IntProvider count, Placement placement, int minPortalDistance) {
    public static final Codec<FeatureQuota> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RoomType.SET_CODEC.fieldOf("rooms").forGetter(FeatureQuota::rooms),
                    IntProvider.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(FeatureQuota::count),
                    Placement.CODEC
                            .optionalFieldOf("placement", Placement.DEAD_END)
                            .forGetter(FeatureQuota::placement),
                    ExtraCodecs.NON_NEGATIVE_INT
                            .optionalFieldOf("min_portal_distance", 0)
                            .forGetter(FeatureQuota::minPortalDistance))
            .apply(instance, FeatureQuota::new));

    public enum Placement implements StringRepresentable {
        DEAD_END("dead_end"),
        ANY("any");

        public static final Codec<Placement> CODEC = StringRepresentable.fromEnum(Placement::values);

        private final String name;

        Placement(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
