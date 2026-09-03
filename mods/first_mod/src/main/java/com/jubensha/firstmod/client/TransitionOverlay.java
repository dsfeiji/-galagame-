package com.jubensha.firstmod.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class TransitionOverlay {
    private static int remainingTicks;

    private TransitionOverlay() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (remainingTicks > 0) {
                remainingTicks--;
            }
        });
    }

    public static void show(int durationTicks) {
        remainingTicks = Math.max(0, durationTicks);
    }

    private static void render(DrawContext context) {
        if (remainingTicks <= 0) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        context.fill(0, 0, width, height, 0xF8000000);
    }
}
