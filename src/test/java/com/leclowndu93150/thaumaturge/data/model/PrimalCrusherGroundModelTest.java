package com.leclowndu93150.thaumaturge.data.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class PrimalCrusherGroundModelTest {
    @Test
    void crusherHasSolidExtrudedSidesAndAnUprightTiltedGroundPose() throws Exception {
        try (InputStream resource =
                getClass().getResourceAsStream("/assets/thaumaturge/models/item/primal_crusher.json")) {
            assertNotNull(resource);
            JsonObject model = JsonParser.parseReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertEquals("minecraft:block/block", model.get("parent").getAsString());
            assertEquals(
                    "thaumaturge:item/primal_crusher",
                    model.getAsJsonObject("textures").get("layer0").getAsString());
            assertEquals(
                    "thaumaturge:item/primal_crusher_edge",
                    model.getAsJsonObject("textures").get("edge").getAsString());
            var elements = model.getAsJsonArray("elements");
            assertEquals(16, elements.size());
            for (var entry : elements) {
                var faces = entry.getAsJsonObject().getAsJsonObject("faces");
                assertEquals(6, faces.size(), "Every solid scanline has front, back and edge faces");
                for (String side : new String[] {"up", "down", "east", "west"}) {
                    assertEquals(
                            "#edge", faces.getAsJsonObject(side).get("texture").getAsString());
                }
            }
            var rotation =
                    model.getAsJsonObject("display").getAsJsonObject("ground").getAsJsonArray("rotation");
            assertEquals(0, rotation.get(0).getAsInt(), "Never lay the item horizontally");
            assertFalse(rotation.get(2).getAsInt() == 0, "Drop pose should have a visible tilt");
        }
    }

    @Test
    void crusherEdgePixelsAreFullyOpaque() throws Exception {
        for (String texture : new String[] {"primal_crusher.png", "primal_crusher_edge.png"}) {
            try (InputStream resource =
                    getClass().getResourceAsStream("/assets/thaumaturge/textures/item/" + texture)) {
                assertNotNull(resource);
                var image = ImageIO.read(resource);
                assertNotNull(image);
                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int alpha = (image.getRGB(x, y) >>> 24) & 255;
                        assertTrue(alpha == 0 || alpha == 255, "No translucent silhouette pixels");
                    }
                }
            }
        }
    }
}
