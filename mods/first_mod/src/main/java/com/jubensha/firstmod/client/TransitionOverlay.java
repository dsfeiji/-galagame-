package com.jubensha.firstmod.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class TransitionOverlay {
    private TransitionOverlay() {
    }

    public static void show(MinecraftClient client, int durationTicks) {
        int totalTicks = Math.max(20, durationTicks);
        client.setScreen(new TransitionScreen(client.currentScreen, totalTicks));
    }

    private static class TransitionScreen extends Screen {
        private final Screen previousScreen;
        private final int totalTicks;
        private final int fadeTicks;
        private int age;

        private TransitionScreen(Screen previousScreen, int totalTicks) {
            super(Text.empty());
            this.previousScreen = previousScreen;
            this.totalTicks = totalTicks;
            this.fadeTicks = Math.max(6, Math.min(12, totalTicks / 3));
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
