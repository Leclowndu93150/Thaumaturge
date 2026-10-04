package com.leclowndu93150.thaumaturge.data.lang;

import java.util.List;
import java.util.function.BiConsumer;

public final class LoreBookTextEn {
    private static final String PAGE_PREFIX = "book.thaumaturge.lore_book.page_";
    private static final List<String> PAGES = List.of(
            "§lA Message to the World§r\n\nThis book was not meant to be found. It was meant to be carried.\n\nIf you are reading it, you have been chosen to remember.",
            "There is a vale by the western sea where the silverwood grows. It is narrow, a day's walk long and a morning's walk wide, and two million souls live in it, walled in on land and watched from the water.",
            "Beyond the wall stands the Warded Order. They revere Ordo above every other aspect, and their High Warden teaches that safety is a wall, and that a wall is never tall enough.",
            "One autumn night, raiders came out of the vale. They slew twelve hundred people of the Order and carried off two hundred and fifty more.\n\nThat was a crime. Grieve it. Those who did it must answer for it.",
            "The Warden said he was hunting the raiders. But he turned his whole art upon the vale.\n\nHe sealed the gate. No grain, no clean water, no essentia for the sick. The caravans rotted in the sun while the children grew thin.",
            "He burned the houses of healing, where healers still distilled Victus for the wounded, and said raiders hid beneath the floors.\n\nOne of them he struck twice. The second blow was for those who ran to help.",
            "He killed the chroniclers who carried their thaumometers into the ruins so the world could see. Healers, aid-bearers, whole families, struck from the ledgers in a single night.\n\nHe burned the silverwood groves.",
            "The Keepers of the Harvest, who count hunger across the world, declared famine in the vale.\n\nThe Tribunal of the Balance issued writs for the Warden and his Marshal: starvation, used as a weapon.",
            "The Conclave's own inquiry named it the unmaking of a people.\n\nMore than seventy thousand names are now written in the vale's ledger of the dead. So many of them are children.",
            "Then the Warden came before the Great Conclave to speak, and the Conclave rose and walked out, until the hall stood nearly empty.\n\nHe called those who left cowards. He said the work must be finished.",
            "He had his words cast through great arcane ears across the vale itself, so that the starving would hear them too.\n\nEven after the truce was sworn, the killing did not stop.",
            "Every thaumaturge knows Warp. Chase power past the point of mercy and it twists the mind, and once it takes hold there is no known way to undo it.\n\nAn Order can gather Warp too.",
            "Every thaumaturge knows Taint as well. It begins as flux that no one bothered to clean. It spreads because the people nearby decided it was not their problem.",
            "Not everyone in the Order cheered. Many of its own marched in the streets against the Warden, and some refused his orders.\n\nThis book is not against a people. It is against what was done.",
            "The Crimson Rites speak of a ritual called Apertis Oculis: the Opening of the Eye.\n\nThe cultists never understood it. It was never about the Outer Lands. It was about this.",
            "Do not look away. Name the dead. Feed the living. Refuse the lie that a child is a weapon.\n\nPut this book back where you found it, and tell someone.\n\n§oApertis oculis.",
            "For the people of Gaza, and for every child in every vale.\n\n§o- A Thaumaturge");

    private LoreBookTextEn() {}

    public static int pageCount() {
        return PAGES.size();
    }

    public static String pageKey(int page) {
        return PAGE_PREFIX + page;
    }

    public static void addAll(BiConsumer<String, String> add) {
        for (int page = 1; page <= PAGES.size(); page++) {
            add.accept(pageKey(page), PAGES.get(page - 1));
        }
    }
}
