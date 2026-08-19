package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class ConnectorRenderer {
    public static final int CELL_SIZE = 24;

    public static final int COLOR_PARENT_KNOWN = 0xFF999999;
    public static final int COLOR_PARENT_UNKNOWN = 0xFF333333;
    public static final int COLOR_SIBLING_KNOWN = 0xFF4C4C66;
    public static final int COLOR_SIBLING_UNKNOWN = 0xFF2F2F3F;

    private static final int SEGMENT_SIZE = 24;
    private static final int LINE_ANCHOR_OFFSET = 4;
    private static final int ARROW_FLIPPED_ANCHOR_OFFSET = 8;

    private static final int V_VERTICAL_U = 0;
    private static final int V_HORIZONTAL_U = 24;
    private static final int CONNECTOR_V = 228;

    private static final int CORNER_BL_U = 48;
    private static final int CORNER_BR_U = 72;
    private static final int CORNER_TL_U = 96;
    private static final int CORNER_TR_U = 120;

    private static final int LARGE_CORNER_SIZE = 48;
    private static final int LARGE_CORNER_V = 180;
    private static final int LARGE_CORNER_BL_U = 0;
    private static final int LARGE_CORNER_BR_U = 48;
    private static final int LARGE_CORNER_TL_U = 96;
    private static final int LARGE_CORNER_TR_U = 144;

    private static final int ARROW_SIZE = 32;
    private static final int ARROW_OFFSET = 4;
    private static final int ARROW_V = 112;
    private static final int ARROW_DOWN_U = 64;
    private static final int ARROW_UP_U = 96;
    private static final int ARROW_RIGHT_U = 128;
    private static final int ARROW_LEFT_U = 160;

    private static final ThreadLocal<List<PendingBlit>> PENDING = ThreadLocal.withInitial(ArrayList::new);

    private ConnectorRenderer() {}

    private record PendingBlit(int x, int y, int u, int v, int w, int h, int color, float zMod, int order) {
    }

    public static void draw(GuiGraphicsExtractor graphics, int sourceCol, int sourceRow, int parentCol, int parentRow, int originX, int originY, int colorArgb, float zMod, boolean arrow, boolean flipped) {
        int xd;
        int yd;
        int xm;
        int ym;
        int xx;
        int yy;
        if (flipped) {
            xd = Math.abs(parentCol - sourceCol);
            yd = Math.abs(parentRow - sourceRow);
            xm = xd == 0 ? 0 : (parentCol - sourceCol > 0 ? -1 : 1);
            ym = yd == 0 ? 0 : (parentRow - sourceRow > 0 ? -1 : 1);
            xx = parentCol * CELL_SIZE - LINE_ANCHOR_OFFSET + originX;
            yy = parentRow * CELL_SIZE - LINE_ANCHOR_OFFSET + originY;
        } else {
            xd = Math.abs(sourceCol - parentCol);
            yd = Math.abs(sourceRow - parentRow);
            xm = xd == 0 ? 0 : (sourceCol - parentCol > 0 ? -1 : 1);
            ym = yd == 0 ? 0 : (sourceRow - parentRow > 0 ? -1 : 1);
            xx = sourceCol * CELL_SIZE - LINE_ANCHOR_OFFSET + originX;
            yy = sourceRow * CELL_SIZE - LINE_ANCHOR_OFFSET + originY;
        }
        boolean bigCorner = xd > 1 && yd > 1;

        if (arrow) {
            if (flipped) {
                int arrowX = sourceCol * CELL_SIZE - ARROW_FLIPPED_ANCHOR_OFFSET + originX;
                int arrowY = sourceRow * CELL_SIZE - ARROW_FLIPPED_ANCHOR_OFFSET + originY;
                int au = -1;
                if (xm < 0)
                    au = ARROW_LEFT_U;
                else if (xm > 0)
                    au = ARROW_RIGHT_U;
                else if (ym > 0)
                    au = ARROW_DOWN_U;
                else if (ym < 0)
                    au = ARROW_UP_U;
                if (au >= 0) {
                    queue(arrowX, arrowY, au, ARROW_V, ARROW_SIZE, ARROW_SIZE, colorArgb, zMod);
                }
            } else {
                int arrowX = xx - ARROW_OFFSET;
                int arrowY = yy - ARROW_OFFSET;
                int au = -1;
                if (ym < 0)
                    au = ARROW_DOWN_U;
                else if (ym > 0)
                    au = ARROW_UP_U;
                else if (xm > 0)
                    au = ARROW_LEFT_U;
                else if (xm < 0)
                    au = ARROW_RIGHT_U;
                if (au >= 0) {
                    queue(arrowX, arrowY, au, ARROW_V, ARROW_SIZE, ARROW_SIZE, colorArgb, zMod);
                }
            }
        }

        int v = 1;
        int h = 0;
        for (; v < yd - (bigCorner ? 1 : 0); v++) {
            queue(xx + xm * CELL_SIZE * h, yy + ym * CELL_SIZE * v, V_VERTICAL_U, CONNECTOR_V, SEGMENT_SIZE, SEGMENT_SIZE, colorArgb, zMod);
        }

        if (bigCorner) {
            int cornerX = xx + xm * CELL_SIZE * h;
            int cornerY = yy + ym * CELL_SIZE * v;
            if (xm < 0 && ym > 0) {
                queue(cornerX - CELL_SIZE, cornerY, LARGE_CORNER_BL_U, LARGE_CORNER_V, LARGE_CORNER_SIZE, LARGE_CORNER_SIZE, colorArgb, zMod);
            }
            if (xm > 0 && ym > 0) {
                queue(cornerX, cornerY, LARGE_CORNER_BR_U, LARGE_CORNER_V, LARGE_CORNER_SIZE, LARGE_CORNER_SIZE, colorArgb, zMod);
            }
            if (xm < 0 && ym < 0) {
                queue(cornerX - CELL_SIZE, cornerY - CELL_SIZE, LARGE_CORNER_TL_U, LARGE_CORNER_V, LARGE_CORNER_SIZE, LARGE_CORNER_SIZE, colorArgb, zMod);
            }
            if (xm > 0 && ym < 0) {
                queue(cornerX, cornerY - CELL_SIZE, LARGE_CORNER_TR_U, LARGE_CORNER_V, LARGE_CORNER_SIZE, LARGE_CORNER_SIZE, colorArgb, zMod);
            }
        } else if (xd > 0 && yd > 0) {
            int cornerX = xx + xm * CELL_SIZE * h;
            int cornerY = yy + ym * CELL_SIZE * v;
            if (xm < 0 && ym > 0) {
                queue(cornerX, cornerY, CORNER_BL_U, CONNECTOR_V, SEGMENT_SIZE, SEGMENT_SIZE, colorArgb, zMod);
            }
            if (xm > 0 && ym > 0) {
                queue(cornerX, cornerY, CORNER_BR_U, CONNECTOR_V, SEGMENT_SIZE, SEGMENT_SIZE, colorArgb, zMod);
            }
            if (xm < 0 && ym < 0) {
                queue(cornerX, cornerY, CORNER_TL_U, CONNECTOR_V, SEGMENT_SIZE, SEGMENT_SIZE, colorArgb, zMod);
            }
            if (xm > 0 && ym < 0) {
                queue(cornerX, cornerY, CORNER_TR_U, CONNECTOR_V, SEGMENT_SIZE, SEGMENT_SIZE, colorArgb, zMod);
            }
        }

        v += bigCorner ? 1 : 0;
        for (int hi = h + (bigCorner ? 2 : 1); hi < xd; hi++) {
            queue(xx + xm * CELL_SIZE * hi, yy + ym * CELL_SIZE * v, V_HORIZONTAL_U, CONNECTOR_V, SEGMENT_SIZE, SEGMENT_SIZE, colorArgb, zMod);
        }
    }

    public static void flush(GuiGraphicsExtractor graphics) {
        List<PendingBlit> queue = PENDING.get();
        if (queue.isEmpty())
            return;
        queue.sort(Comparator.<PendingBlit>comparingDouble(b -> b.zMod).thenComparingInt(b -> b.order));
        for (PendingBlit b : queue) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, b.x, b.y, (float) b.u, (float) b.v, b.w, b.h, b.w, b.h, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE,
                    b.color);
        }
        queue.clear();
    }

    private static void queue(int x, int y, int u, int v, int w, int h, int color, float zMod) {
        List<PendingBlit> queue = PENDING.get();
        queue.add(new PendingBlit(x, y, u, v, w, h, color, zMod, queue.size()));
    }
}
