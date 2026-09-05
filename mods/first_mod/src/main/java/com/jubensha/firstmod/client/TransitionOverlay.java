package com.jubensha.firstmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public final class TransitionOverlay {
    private TransitionOverlay() {
    }

    public static void show(MinecraftClient client, int durationTicks) {
        show(client, durationTicks, "");
    }

    public static void show(MinecraftClient client, int durationTicks, String message) {
        int totalTicks = Math.max(20, durationTicks);
        client.setScreen(new TransitionScreen(client.currentScreen, totalTicks, message));
    }

    private static class TransitionScreen extends Screen {
        private final Screen previousScreen;
        private final int totalTicks;
        private final int fadeTicks;
        private final String message;
        private int age;

        private TransitionScreen(Screen previousScreen, int totalTicks, String message) {
            super(Text.empty());
            this.previousScreen = previousScreen;
            this.totalTicks = totalTicks;
            this.fadeTicks = Math.max(6, Math.min(12, totalTicks / 3));
            this.message = message == null ? "" : message;
        }

        @Override
        public void tick() {
            age++;
            if (age >= totalTicks && this.client != null && this.client.currentScreen == this) {
                this.client.setScreen(null);
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            if (previousScreen != null && age < fadeTicks) {
                previousScreen.render(context, mouseX, mouseY, delta);
            }
            context.fill(0, 0, this.width, this.height, alpha() << 24);
            renderMessage(context);
        }

        private void renderMessage(DrawContext context) {
            if (message.isBlank()) {
                return;
            }
            int alpha = alpha();
            if (alpha < 12) {
                return;
            }
            int textAlpha = Math.min(255, alpha + 24);
            int color = (textAlpha << 24) | 0xF1E9D8;
            int maxWidth = Math.min(320, Math.max(120, this.width - 48));
            List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal(message), maxWidth);
            int lineHeight = 13;
            int startY = (this.height - lines.size() * lineHeight) / 2;
            for (int index = 0; index < lines.size(); index++) {
                OrderedText line = lines.get(index);
                int x = (this.width - this.textRenderer.getWidth(line)) / 2;
                context.drawTextWithShadow(this.textRenderer, line, x, startY + index * lineHeight, color);
            }
        }

        private int alpha() {
            if (age < fadeTicks) {
                return Math.min(255, age * 255 / fadeTicks);
            }
            int fadeOutStart = totalTicks - fadeTicks;
            if (age > fadeOutStart) {
                int remaining = Math.max(0, totalTicks - age);
                return Math.min(255, remaining * 255 / fadeTicks);
            }
            return 255;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return true;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return true;
        }

        @Override
        public boolean shouldPause() {
            return false;
        }

        @Override
        public boolean shouldCloseOnEsc() {
            return false;
        }

        @Override
        public void blur() {
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        }
    }
}
