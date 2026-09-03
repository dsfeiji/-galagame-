package com.jubensha.firstmod.client;

import com.jubensha.firstmod.dialog.DialogStore;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class StaminaHud {
    private static int stamina = DialogStore.MAX_STAMINA;
    private static int maxStamina = DialogStore.MAX_STAMINA;

    private StaminaHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    public static void update(int current, int max) {
        maxStamina = Math.max(1, max);
        stamina = Math.max(0, Math.min(maxStamina, current));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int x = 8;
        int y = client.getWindow().getScaledHeight() - 58;
        int width = 86;
        int height = 23;
        context.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x66000000);
        context.fill(x, y, x + width, y + height, 0xD81B1F2B);
        context.fill(x + 6, y + 2, x + width - 6, y + 3, 0xFFE6C879);
        context.drawTextWithShadow(client.textRenderer, Text.literal("体力 " + stamina + "/" + maxStamina), x + 7, y + 7, 0xFFFFF2CC);

        int pipX = x + 54;
        for (int i = 0; i < maxStamina; i++) {
            int color = i < stamina ? 0xFFE85252 : 0xFF4B5263;
            context.fill(pipX + i * 6, y + 15, pipX + i * 6 + 4, y + 18, color);
        }
    }
}
