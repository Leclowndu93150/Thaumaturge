package com.leclowndu93150.thaumaturge.compat.ftblibrary;

import com.leclowndu93150.thaumaturge.client.screen.casters.focal.FocalManipulatorScreen;
import dev.ftb.mods.ftblibrary.api.client.FTBLibraryClientApi;

final class FtbLibrarySidebar {
    private FtbLibrarySidebar() {}

    static void hideOnFullScreenMenus() {
        FTBLibraryClientApi.get().addSidebarScreenBlacklist(FocalManipulatorScreen.class.getName());
    }
}
