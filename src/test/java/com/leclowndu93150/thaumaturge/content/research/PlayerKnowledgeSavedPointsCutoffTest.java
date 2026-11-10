package com.leclowndu93150.thaumaturge.content.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.Test;

final class PlayerKnowledgeSavedPointsCutoffTest {
    private static final ResourceLocation ENTRY = ResourceLocation.parse("thaumaturge:test_research");
    private static final ResourceKey<IResearchCategory> CATEGORY =
            ResourceKey.create(IResearchCategory.REGISTRY_KEY, ResourceLocation.parse("thaumaturge:test_category"));

    @Test
    void savedPointsIgnoreNonpositiveAmountsInCategorizedAndUncategorizedSlots() {
        for (boolean categorized : new boolean[] {false, true}) {
            for (int amount : new int[] {-33, 0, 65}) {
                JsonArray points = new JsonArray();
                points.add(point(KnowledgeType.THEORY, categorized, amount));
                PlayerResearch restored = decode(saved(points));
                ResourceKey<IResearchCategory> category = categorized ? CATEGORY : null;
                int expected = Math.max(0, amount);

                assertEquals(expected, restored.knowledgePoints(KnowledgeType.THEORY, category));
                assertEquals(
                        expected / KnowledgeType.THEORY.progression(),
                        restored.knowledgeLevel(KnowledgeType.THEORY, category));
                assertEquals(amount > 0 ? 1 : 0, savedPointCount((PlayerKnowledge) restored));
            }
        }
    }

    @Test
    void duplicateRecordsKeepTheLastPositiveAmountWithoutInvalidRecordsOverwritingIt() {
        int[][] sequences = {
            {-9, 65}, {65, -9}, {65, 0}, {0, 65},
            {65, 97}, {97, 65}, {-9, 0}, {65, -9, 97, 0}
        };
        int[] expected = {65, 65, 65, 65, 97, 65, 0, 97};
        for (boolean categorized : new boolean[] {false, true}) {
            for (int index = 0; index < sequences.length; index++) {
                JsonArray points = new JsonArray();
                for (int amount : sequences[index]) {
                    points.add(point(KnowledgeType.THEORY, categorized, amount));
                }
                PlayerKnowledge restored = decode(saved(points));
                ResourceKey<IResearchCategory> category = categorized ? CATEGORY : null;

                assertEquals(expected[index], restored.knowledgePoints(KnowledgeType.THEORY, category));
                assertEquals(expected[index] > 0 ? 1 : 0, savedPointCount(restored));
                PlayerKnowledge roundTripped = decode(encode(restored));
                assertEquals(expected[index], roundTripped.knowledgePoints(KnowledgeType.THEORY, category));
            }
        }
    }

    @Test
    void codecReserializationPreservesResearchMetadataAndLegacyCategoryIdsWhileDiscardingInvalidPoints() {
        PlayerKnowledge restored = decode(mixedSavedRecord());
        assertMixedRecord(restored);
        JsonObject encoded = encode(restored);
        assertEquals(2, encoded.getAsJsonArray("knowledge").size());
        for (var point : encoded.getAsJsonArray("knowledge")) {
            assertTrue(point.getAsJsonObject().get("amount").getAsInt() > 0);
        }
        assertMixedRecord(decode(encoded));
    }

    @Test
    void attachmentStreamRoundTripKeepsOnlyValidPointsAndPreservesResearchMetadata() {
        PlayerKnowledge restored = decode(mixedSavedRecord());
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY, ConnectionType.OTHER);
        try {
            PlayerKnowledge.STREAM_CODEC.encode(buffer, restored);
            PlayerKnowledge received = PlayerKnowledge.STREAM_CODEC.decode(buffer);
            assertMixedRecord(received);
            assertEquals(2, savedPointCount(received));
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    private static JsonObject mixedSavedRecord() {
        JsonArray points = new JsonArray();
        points.add(point(KnowledgeType.THEORY, true, 65));
        points.add(point(KnowledgeType.OBSERVATION, false, 49));
        points.add(point(KnowledgeType.THEORY, false, -33));
        points.add(point(KnowledgeType.OBSERVATION, true, 0));
        JsonObject saved = saved(points);
        JsonObject research = new JsonObject();
        research.addProperty("id", "thaumcraft:test_research");
        research.addProperty("stage", 3);
        research.addProperty("complete", true);
        JsonArray flags = new JsonArray();
        flags.add("popup");
        research.add("flags", flags);
        JsonArray entries = new JsonArray();
        entries.add(research);
        saved.add("research", entries);
        return saved;
    }

    private static void assertMixedRecord(PlayerResearch research) {
        assertEquals(65, research.knowledgePoints(KnowledgeType.THEORY, CATEGORY));
        assertEquals(2, research.knowledgeLevel(KnowledgeType.THEORY, CATEGORY));
        assertEquals(49, research.knowledgePoints(KnowledgeType.OBSERVATION, null));
        assertEquals(3, research.knowledgeLevel(KnowledgeType.OBSERVATION, null));
        assertEquals(0, research.knowledgePoints(KnowledgeType.THEORY, null));
        assertEquals(0, research.knowledgePoints(KnowledgeType.OBSERVATION, CATEGORY));
        assertTrue(research.knows(ENTRY));
        assertTrue(research.hasCompleted(ENTRY));
        assertEquals(3, research.stageOf(ENTRY));
        assertTrue(research.hasFlag(ENTRY, ResearchFlag.POPUP));
    }

    private static JsonObject point(KnowledgeType type, boolean categorized, int amount) {
        JsonObject point = new JsonObject();
        point.addProperty("type", type.getSerializedName());
        if (categorized) {
            point.addProperty("category", "thaumcraft:test_category");
        }
        point.addProperty("amount", amount);
        return point;
    }

    private static JsonObject saved(JsonArray points) {
        JsonObject saved = new JsonObject();
        saved.add("knowledge", points);
        return saved;
    }

    private static PlayerKnowledge decode(JsonObject saved) {
        return PlayerKnowledge.CODEC.codec().parse(JsonOps.INSTANCE, saved).getOrThrow();
    }

    private static JsonObject encode(PlayerKnowledge knowledge) {
        return PlayerKnowledge.CODEC
                .codec()
                .encodeStart(JsonOps.INSTANCE, knowledge)
                .getOrThrow()
                .getAsJsonObject();
    }

    private static int savedPointCount(PlayerKnowledge knowledge) {
        JsonArray points = encode(knowledge).getAsJsonArray("knowledge");
        return points == null ? 0 : points.size();
    }
}
