package com.jubensha.firstmod.client;

import net.minecraft.client.MinecraftClient;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DialogJsonFolder {
    private static final String FOLDER_NAME = "first_mod_dialogs";

    private DialogJsonFolder() {
    }

    public static Path getFolder() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve(FOLDER_NAME);
    }

    public static String getDisplayPath() {
        return "./" + FOLDER_NAME;
    }

    public static void ensureExists() {
        try {
            Files.createDirectories(getFolder());
        } catch (IOException ignored) {
        }
    }

    public static boolean openFolder() {
        ensureExists();
        if (!Desktop.isDesktopSupported()) {
            return false;
        }
        try {
            Desktop.getDesktop().open(getFolder().toFile());
            return true;
        } catch (IOException | RuntimeException ignored) {
            return false;
        }
    }
}
