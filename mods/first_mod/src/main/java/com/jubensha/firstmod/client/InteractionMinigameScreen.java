package com.jubensha.firstmod.client;

import com.jubensha.firstmod.network.InteractionMinigameResultPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class InteractionMinigameScreen extends Screen {
    private final String interactionId;
    private final String minigameTitle;
    private final int difficulty;
    private final float speed;
    private final float customSuccessStart;
    private final float customSuccessWidth;
    private final long openedAtNanos = System.nanoTime();
    private boolean submitted;

    public InteractionMinigameScreen(String interactionId, String title, int difficulty, float speed, float successStart, float successWidth) {
        super(Text.literal(title == null || title.isBlank() ? "时机判定" : title));
        this.interactionId = interactionId;
        this.minigameTitle = title == null || title.isBlank() ? "时机判定" : title;
        this.difficulty = Math.max(1, Math.min(4, difficulty));
        this.speed = speed > 0.0F ? speed : 0.78F;
        this.customSuccessStart = successStart >= 0.0F ? Math.min(1.0F, successStart) : -1.0F;
        this.customSuccessWidth = successWidth >= 0.0F ? Math.min(1.0F, successWidth) : -1.0F;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        submit();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int boxWidth = Math.min(280, this.width - 48);
        int boxHeight = 56;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(36, (this.height - boxHeight) / 2 - 18);
        int barX = boxX + 24;
        int barY = boxY + 34;
        int barWidth = boxWidth - 48;
        int barHeight = 7;
        int pointerX = barX + Math.round(getTimingPosition() * barWidth);
        int zoneWidth = successZoneWidth(barWidth);
        int zoneX = barX + Math.round(successZoneStart() * barWidth);

        context.fill(boxX + 3, boxY + 3, boxX + boxWidth + 3, boxY + boxHeight + 3, 0x77000000);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE8202430);
        context.fill(boxX + 12, boxY, boxX + boxWidth - 12, boxY + 1, 0xFFE6C879);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(minigameTitle), boxX + boxWidth / 2, boxY + 10, 0xFFFFF2CC);
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF121722);
        context.fill(zoneX, barY - 1, zoneX + zoneWidth, barY + barHeight + 1, 0xFF6FD08C);
        context.fill(pointerX - 1, barY - 7, pointerX + 2, barY + barHeight + 7, 0xFFFFF2CC);

        String hint = "点击 / 空格";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint), boxX + boxWidth / 2, boxY + boxHeight - 11, 0xFFD8D2C0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void blur() {
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void submit() {
        if (submitted) {
            return;
        }
        submitted = true;
        ClientPlayNetworking.send(new InteractionMinigameResultPayload(interactionId, isTimingInSuccessZone()));
        close();
    }

    private boolean isTimingInSuccessZone() {
        float position = getTimingPosition();
        float start = successZoneStart();
        float width = successZoneWidthRatio();
        return position >= start && position <= start + width;
    }

    private float getTimingPosition() {
        double seconds = (System.nanoTime() - openedAtNanos) / 1_000_000_000.0;
        double cycle = (seconds * speed) % 2.0;
        return (float) (cycle <= 1.0 ? cycle : 2.0 - cycle);
    }

    private float successZoneStart() {
        if (customSuccessStart >= 0.0F) {
            return customSuccessStart;
        }
        return switch (difficulty) {
            case 1 -> 0.34F;
            case 2 -> 0.39F;
            case 3 -> 0.44F;
            default -> 0.47F;
        };
    }

    private float successZoneWidthRatio() {
        if (customSuccessWidth >= 0.0F) {
            return customSuccessWidth;
        }
        return switch (difficulty) {
            case 1 -> 0.32F;
            case 2 -> 0.22F;
            case 3 -> 0.14F;
            default -> 0.08F;
        };
    }

    private int successZoneWidth(int barWidth) {
        return Math.max(12, Math.round(successZoneWidthRatio() * barWidth));
    }
}
