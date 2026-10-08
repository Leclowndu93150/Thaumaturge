package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.decor.banner.BannerStandingBlock;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSitePiece;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

final class ObeliskSiteTemplates {
    static final ResourceLocation SITE = TTIds.rl("obelisk/site");
    static final ResourceLocation SITE_CULT = TTIds.rl("obelisk/site_cult");

    private static final int SIZE = 9;
    private static final int HEIGHT = 10;
    private static final int CENTER = 4;
    private static final int EDGE_MIN = 1;
    private static final int EDGE_MAX = 7;
    private static final int OBELISK_Y = 3;
    private static final int PILLAR_TOP = 7;
    private static final int[][] CAPSTONES = {{3, 1}, {5, 1}, {3, 7}, {5, 7}, {1, 3}, {1, 5}, {7, 3}, {7, 5}};
    private static final int[][] BANNERS = {
        {CENTER, EDGE_MIN, 8}, {CENTER, EDGE_MAX, 0}, {EDGE_MIN, CENTER, 4}, {EDGE_MAX, CENTER, 12}
    };

    private ObeliskSiteTemplates() {}

    static Map<ResourceLocation, SiteCanvas> all() {
        return Map.of(SITE, site(false), SITE_CULT, site(true));
    }

    private static SiteCanvas site(boolean cult) {
        SiteCanvas canvas = new SiteCanvas(SIZE, HEIGHT, SIZE);
        for (int x = EDGE_MIN; x <= EDGE_MAX; x++) {
            for (int z = EDGE_MIN; z <= EDGE_MAX; z++) {
                if ((x == EDGE_MIN || x == EDGE_MAX) && (z == EDGE_MIN || z == EDGE_MAX)) {
                    continue;
                }
                canvas.set(x, 0, z, LabyrinthBlocks.obsidianTile());
                for (int y = 1; y < HEIGHT; y++) {
                    canvas.set(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
        canvas.set(CENTER, 1, CENTER, TTBlocks.ELDRITCH_ALTAR.get().defaultBlockState());
        for (int[] cap : CAPSTONES) {
            canvas.set(cap[0], 1, cap[1], LabyrinthBlocks.capstone());
        }
        canvas.marker(CENTER, 2, CENTER, ObeliskSitePiece.NODE_MARKER);
        canvas.set(CENTER, OBELISK_Y, CENTER, LabyrinthBlocks.obelisk());
        for (int y = OBELISK_Y + 1; y <= PILLAR_TOP; y++) {
            canvas.set(CENTER, y, CENTER, LabyrinthBlocks.pillar());
        }
        if (cult) {
            for (int[] banner : BANNERS) {
                canvas.set(
                        banner[0],
                        1,
                        banner[1],
                        TTBlocks.BANNER_CRIMSON_CULT
                                .get()
                                .defaultBlockState()
                                .setValue(BannerStandingBlock.ROTATION, banner[2]));
            }
        }
        return canvas;
    }
}
