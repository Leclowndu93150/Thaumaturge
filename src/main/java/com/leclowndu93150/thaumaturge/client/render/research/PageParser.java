package com.leclowndu93150.thaumaturge.client.render.research;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class PageParser {
    public static final int PAGE_WIDTH = 140;
    public static final int PAGE_HEIGHT = 210;

    private static final int BASE_HEIGHT_REMAINING = 182;
    private static final int PAGE_RESET_HEIGHT = 210;
    private static final int KNOWLEDGETYPES_TOP_PAD = 2;
    private static final int KNOWLEDGETYPES_ROW_STEP = 20;
    private static final int KNOWLEDGETYPES_DIVIDER = 12;
    private static final int REQUIREMENT_ROW = 18;
    private static final int REQUIREMENT_DIVIDER = 15;
    private static final float BONUS_BREAK_FRACTION = 0.66F;
    private static final int IMAGE_GAP = 2;

    private static final Identifier KNOWLEDGETYPES_ID = Identifier.fromNamespaceAndPath("thaumaturge", "knowledge_types");
    private static final String ADDENDUM_TEXT_KEY = "tc.addendumtext";

    private PageParser() {}

    public static List<Page> parse(Font font, String stageTextKey, int contentHeightBudget) {
        return paginate(font, stageTextKey, List.of(), contentHeightBudget);
    }

    public static List<Page> parse(Font font, Identifier entryId, String stageTextKey, List<String> addendaTextKeys, int knowledgeTypeRowCount, boolean isComplete, boolean hasRequiredResearch, boolean hasObtain, boolean hasCraft, boolean hasKnowledge) {
        int heightRemaining = BASE_HEIGHT_REMAINING;
        int dividerSpace = 0;
        if (entryId != null && entryId.equals(KNOWLEDGETYPES_ID)) {
            heightRemaining -= KNOWLEDGETYPES_TOP_PAD;
            heightRemaining -= KNOWLEDGETYPES_ROW_STEP * knowledgeTypeRowCount;
            dividerSpace = KNOWLEDGETYPES_DIVIDER;
        }
        if (!isComplete) {
            if (hasCraft) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
            if (hasObtain) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
            if (hasKnowledge) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
            if (hasRequiredResearch) {
                heightRemaining -= REQUIREMENT_ROW;
                dividerSpace = REQUIREMENT_DIVIDER;
            }
        }
        heightRemaining -= dividerSpace;
        return paginate(font, stageTextKey, addendaTextKeys, heightRemaining);
    }

    private static final String[][] MARKUP_SENTINELS = {{"<BR>", "~B\n\n"}, {"<BR/>", "~B\n\n"}, {"<LINE>", "~L"}, {"<LINE/>", "~L"}, {"<DIV>", "~D"}, {"<DIV/>", "~D"}, {"<PAGE>", "~P"},
            {"<PAGE/>", "~P"},};
    private static final String MARKER_PRECEDENCE = "PDLI";
    private static final String IMAGE_OPEN = "<IMG>";
    private static final String IMAGE_CLOSE = "</IMG>";

    private static List<Page> paginate(Font font, String stageTextKey, List<String> addendaTextKeys, int initialBudget) {
        StringBuilder assembled = new StringBuilder(Component.translatable(stageTextKey).getString());
        for (int i = 0; i < addendaTextKeys.size(); i++) {
            assembled.append("<PAGE>").append(Component.translatable(ADDENDUM_TEXT_KEY, i + 1).getString()).append("<BR>").append(Component.translatable(addendaTextKeys.get(i)).getString());
        }
        String normalized = assembled.toString();
        for (String[] sentinel : MARKUP_SENTINELS) {
            normalized = normalized.replace(sentinel[0], sentinel[1]);
        }
        List<PageImage> images = new ArrayList<>();
        normalized = extractImages(normalized, images);

        Paginator paginator = new Paginator(font, initialBudget, images);
        for (String token : tokenize(normalized)) {
            paginator.feed(token);
        }
        return paginator.finish();
    }

    private static String extractImages(String text, List<PageImage> images) {
        StringBuilder out = new StringBuilder(text.length());
        int cursor = 0;
        while (true) {
            int open = text.indexOf(IMAGE_OPEN, cursor);
            if (open < 0) {
                return out.append(text, cursor, text.length()).toString().replace(IMAGE_CLOSE, "");
            }
            int close = text.indexOf(IMAGE_CLOSE, open + IMAGE_OPEN.length());
            if (close < 0) {
                return out.append(text, cursor, text.length()).toString().replace(IMAGE_OPEN, "").replace(IMAGE_CLOSE, "");
            }
            out.append(text, cursor, open);
            PageImage image = PageImage.parse(text.substring(open + IMAGE_OPEN.length(), close));
            if (image == null) {
                out.append('\n');
            } else {
                images.add(image);
                out.append("~I");
            }
            cursor = close + IMAGE_CLOSE.length();
        }
    }

    private static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        int cursor = 0;
        while (cursor <= text.length()) {
            int marker = nextMarker(text, cursor);
            if (marker < 0) {
                tokens.add(text.substring(cursor));
                break;
            }
            tokens.add(text.substring(cursor, marker));
            if (markerSurvives(text, marker)) {
                tokens.add(text.substring(marker, marker + 2));
            }
            cursor = marker + 2;
        }
        return tokens;
    }

    private static int nextMarker(String text, int from) {
        for (int i = from; i < text.length() - 1; i++) {
            if (text.charAt(i) == '~' && MARKER_PRECEDENCE.indexOf(text.charAt(i + 1)) >= 0) {
                return i;
            }
        }
        return -1;
    }

    private static boolean markerSurvives(String text, int at) {
        int precedence = MARKER_PRECEDENCE.indexOf(text.charAt(at + 1));
        int cursor = at + 2;
        while (cursor < text.length()) {
            int next = nextMarker(text, cursor);
            if (next != cursor) {
                return true;
            }
            int otherPrecedence = MARKER_PRECEDENCE.indexOf(text.charAt(next + 1));
            if (otherPrecedence < precedence) {
                return false;
            }
            if (otherPrecedence > precedence) {
                return true;
            }
            cursor = next + 2;
        }
        return false;
    }

    private static final class Paginator {
        private final Font font;
        private final List<PageImage> images;
        private final List<PageImage> pendingImages = new ArrayList<>();
        private final List<Page> pages = new ArrayList<>();
        private Page current = new Page();
        private int budget;

        Paginator(Font font, int budget, List<PageImage> images) {
            this.font = font;
            this.budget = budget;
            this.images = images;
        }

        void feed(String token) {
            switch (token) {
                case "~I" -> {
                    if (!images.isEmpty()) {
                        pendingImages.add(images.remove(0));
                    }
                    settle();
                }
                case "~L" -> {
                    pendingImages.add(PageImage.LINE_DIVIDER);
                    settle();
                }
                case "~D" -> {
                    pendingImages.add(PageImage.SECTION_DIVIDER);
                    settle();
                }
                case "~P" -> {
                    budget = PAGE_RESET_HEIGHT;
                    pages.add(current);
                    current = new Page();
                    settle();
                }
                default -> {
                    for (FormattedText wrapped : font.getSplitter().splitLines(token, PAGE_WIDTH, Style.EMPTY)) {
                        appendLine(wrapped.getString().trim(), lineStyle(wrapped));
                        settle();
                    }
                }
            }
        }

        private void appendLine(String line, Style style) {
            if (line.isEmpty()) {
                return;
            }
            boolean paragraphBreak = line.endsWith("~B");
            if (paragraphBreak) {
                line = line.substring(0, line.length() - 2).stripTrailing();
            }
            current.add(new PageElement.Text(line, style, paragraphBreak));
            budget -= font.lineHeight;
            if (paragraphBreak) {
                budget = (int) (budget - font.lineHeight * BONUS_BREAK_FRACTION);
            }
        }

        private void settle() {
            while (!pendingImages.isEmpty() && budget >= pendingImages.get(0).renderedHeight() + IMAGE_GAP) {
                budget -= pendingImages.get(0).renderedHeight() + IMAGE_GAP;
                current.add(new PageElement.Image(pendingImages.remove(0)));
            }
            if (budget < font.lineHeight && !current.elements().isEmpty()) {
                budget = PAGE_RESET_HEIGHT;
                pages.add(current);
                current = new Page();
            }
        }

        List<Page> finish() {
            if (!current.elements().isEmpty()) {
                pages.add(current);
            }
            current = new Page();
            budget = PAGE_RESET_HEIGHT;
            while (!pendingImages.isEmpty()) {
                PageImage head = pendingImages.get(0);
                if (budget < head.renderedHeight() + IMAGE_GAP) {
                    budget = PAGE_RESET_HEIGHT;
                    pages.add(current);
                    current = new Page();
                } else {
                    budget -= head.renderedHeight() + IMAGE_GAP;
                    current.add(new PageElement.Image(pendingImages.remove(0)));
                }
            }
            if (!current.elements().isEmpty()) {
                pages.add(current);
            }
            if (pages.isEmpty()) {
                pages.add(new Page());
            }
            return pages;
        }
    }

    public static final class Page {
        private final List<PageElement> elements = new ArrayList<>();

        public List<PageElement> elements() {
            return elements;
        }

        void add(PageElement element) {
            elements.add(element);
        }
    }

    public sealed interface PageElement permits PageElement.Text, PageElement.Image {
        record Text(String content, Style style, boolean paragraphBreak) implements PageElement {
        }

        record Image(PageImage image) implements PageElement {
        }
    }

    private record StyledLine(String content, Style style) {
    }

    private static Style lineStyle(FormattedText line) {
        return line.visit((style, text) -> Optional.of(style), Style.EMPTY).orElse(Style.EMPTY);
    }

    public static final class PageImage {
        public static final PageImage LINE_DIVIDER = new PageImage(Identifier.fromNamespaceAndPath("thaumaturge", "textures/gui/gui_researchbook.png"), 24, 184, 95, 6, 1.0F);
        public static final PageImage SECTION_DIVIDER = new PageImage(Identifier.fromNamespaceAndPath("thaumaturge", "textures/gui/gui_researchbook.png"), 28, 192, 140, 6, 1.0F);

        public final Identifier texture;
        public final int u;
        public final int v;
        public final int w;
        public final int h;
        public final float scale;
        public final int renderedWidth;
        public final int renderedHeight;

        public PageImage(Identifier texture, int u, int v, int w, int h, float scale) {
            this.texture = texture;
            this.u = u;
            this.v = v;
            this.w = w;
            this.h = h;
            this.scale = scale;
            this.renderedWidth = (int) (w * scale);
            this.renderedHeight = (int) (h * scale);
        }

        public int renderedHeight() {
            return renderedHeight;
        }

        public int renderedWidth() {
            return renderedWidth;
        }

        public static PageImage parse(String descriptor) {
            String[] parts = descriptor.split(":");
            if (parts.length != 7)
                return null;
            try {
                Identifier id = Identifier.fromNamespaceAndPath(parts[0], parts[1]);
                int u = Integer.parseInt(parts[2]);
                int v = Integer.parseInt(parts[3]);
                int w = Integer.parseInt(parts[4]);
                int h = Integer.parseInt(parts[5]);
                float scale = Float.parseFloat(parts[6]);
                PageImage pi = new PageImage(id, u, v, w, h, scale);
                return pi.renderedHeight <= 208 && pi.renderedWidth <= PAGE_WIDTH ? pi : null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
