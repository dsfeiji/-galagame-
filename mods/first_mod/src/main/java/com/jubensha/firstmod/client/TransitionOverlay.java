package com.jubensha.firstmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TransitionOverlay {
    private static boolean visible;
    private static String message = "";

    private TransitionOverlay() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    public static void update(boolean blackScreen, String text) {
        visible = blackScreen;
        message = text == null ? "" : text;
    }

    private static void render(DrawContext context) {
        if (!visible) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        context.fill(0, 0, width, height, 0xF8000000);
        if (!message.isBlank()) {
            int textWidth = client.textRenderer.getWidth(message);
            context.drawTextWithShadow(client.textRenderer, Text.literal(message), (width - textWidth) / 2, height / 2 - 5, 0xFFFFF2CC);
        }
    }
}
