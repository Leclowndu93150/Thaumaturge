package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public record RoomType(
        SimpleWeightedRandomList<ResourceLocation> templates,
        int width,
        int depth,
        List<RoomSocket> sockets,
        RoomTransforms transforms,
        int weight,
        int maxPerMaze,
        int minPortalDistance,
        Optional<Holder<StructureProcessorList>> processors,
        boolean decorate,
        Optional<ResourceLocation> varietyGroup) {
    public static final ResourceKey<Registry<RoomType>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("labyrinth_room"));
    public static final int MAX_FOOTPRINT = 4;
    public static final int UNLIMITED = Integer.MAX_VALUE;
    public static final Codec<HolderSet<RoomType>> SET_CODEC = RegistryCodecs.homogeneousList(REGISTRY_KEY);

    private static final Codec<Integer> FOOTPRINT = Codec.intRange(1, MAX_FOOTPRINT);

    public static final Codec<RoomType> CODEC = RecordCodecBuilder.<RoomType>create(instance -> instance.group(
                            SimpleWeightedRandomList.wrappedCodec(ResourceLocation.CODEC)
                                    .fieldOf("templates")
                                    .forGetter(RoomType::templates),
                            FOOTPRINT.optionalFieldOf("width", 1).forGetter(RoomType::width),
                            FOOTPRINT.optionalFieldOf("depth", 1).forGetter(RoomType::depth),
                            RoomSocket.CODEC.listOf().fieldOf("sockets").forGetter(RoomType::sockets),
                            RoomTransforms.CODEC
                                    .optionalFieldOf("transforms", RoomTransforms.ALL)
                                    .forGetter(RoomType::transforms),
                            ExtraCodecs.NON_NEGATIVE_INT
                                    .optionalFieldOf("weight", 1)
                                    .forGetter(RoomType::weight),
                            ExtraCodecs.POSITIVE_INT
                                    .optionalFieldOf("max_per_maze", UNLIMITED)
                                    .forGetter(RoomType::maxPerMaze),
                            ExtraCodecs.NON_NEGATIVE_INT
                                    .optionalFieldOf("min_portal_distance", 0)
                                    .forGetter(RoomType::minPortalDistance),
                            StructureProcessorType.LIST_CODEC
                                    .optionalFieldOf("processors")
                                    .forGetter(RoomType::processors),
                            Codec.BOOL.optionalFieldOf("decorate", true).forGetter(RoomType::decorate),
                            ResourceLocation.CODEC
                                    .optionalFieldOf("variety_group")
                                    .forGetter(RoomType::varietyGroup))
                    .apply(instance, RoomType::new))
            .validate(RoomType::validate);

    public static String id(Holder<RoomType> room) {
        return room.unwrapKey().map(key -> key.location().toString()).orElse("");
    }

    private static DataResult<RoomType> validate(RoomType room) {
        for (RoomSocket socket : room.sockets) {
            if (socket.cellX() >= room.width || socket.cellZ() >= room.depth) {
                return DataResult.error(() ->
                        "Room socket " + socket + " lies outside the " + room.width + "x" + room.depth + " footprint");
            }
        }
        if (room.sockets.isEmpty()) {
            return DataResult.error(() -> "Rooms need at least one socket");
        }
        return DataResult.success(room);
    }
}
