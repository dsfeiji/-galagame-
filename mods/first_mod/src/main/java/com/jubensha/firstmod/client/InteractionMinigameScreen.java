package com.jubensha.firstmod.client;

import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.InteractionMinigameResultPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class InteractionMinigameScreen extends Screen {
    private final String interactionId;
    private final DialogTree.DialogMinigame minigame;
    private final long openedAtNanos = System.nanoTime();
    private boolean submitted;
    private int playerClicks;

    public InteractionMinigameScreen(String interactionId, DialogTree.DialogMinigame minigame) {
        super(Text.literal(minigame.title == null || minigame.title.isBlank() ? defaultTitle(minigame.type) : minigame.title));
        this.interactionId = interactionId;
        this.minigame = minigame;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        press();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            press();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if ("arm_wrestle".equals(minigame.type) && !submitted && elapsedTicks() >= minigame.durationTicks) {
            submit(isArmWrestleWinning());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if ("arm_wrestle".equals(minigame.type)) {
            renderArmWrestle(context);
        } else {
            renderTiming(context);
        }
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

    private void press() {
        if (submitted) {
            return;
        }
        if ("arm_wrestle".equals(minigame.type)) {
            playerClicks++;
            if (isArmWrestleWinning() && clickLead() >= Math.max(1, minigame.winClickLead + 8)) {
                submit(true);
            }
        } else {
            submit(isTimingInSuccessZone());
        }
    }

    private void submit(boolean success) {
        if (submitted) {
            return;
        }
        submitted = true;
        ClientPlayNetworking.send(new InteractionMinigameResultPayload(interactionId, success));
        close();
    }

    private void renderTiming(DrawContext context) {
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

        renderBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title()), boxX + boxWidth / 2, boxY + 10, 0xFFFFF2CC);
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF121722);
        context.fill(zoneX, barY - 1, zoneX + zoneWidth, barY + barHeight + 1, 0xFF6FD08C);
        context.fill(pointerX - 1, barY - 7, pointerX + 2, barY + barHeight + 7, 0xFFFFF2CC);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("点击 / 空格"), boxX + boxWidth / 2, boxY + boxHeight - 11, 0xFFD8D2C0);
    }

    private void renderArmWrestle(DrawContext context) {
        int boxWidth = Math.min(310, this.width - 48);
        int boxHeight = 68;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(36, (this.height - boxHeight) / 2 - 18);
        int barX = boxX + 28;
        int barY = boxY + 37;
        int barWidth = boxWidth - 56;
        int centerX = barX + barWidth / 2;
        int markerX = centerX - Math.round(powerRatio() * (barWidth / 2));
        int ticksLeft = Math.max(0, minigame.durationTicks - elapsedTicks());

        renderBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title()), boxX + boxWidth / 2, boxY + 9, 0xFFFFF2CC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("你"), barX, barY - 13, 0xFF6FD08C);
        context.drawTextWithShadow(this.textRenderer, Text.literal("对手"), barX + barWidth - this.textRenderer.getWidth("对手"), barY - 13, 0xFFE85252);
        context.fill(barX, barY, barX + barWidth, barY + 8, 0xFF121722);
        context.fill(barX, barY, centerX, barY + 8, 0x6656C878);
        context.fill(centerX, barY, barX + barWidth, barY + 8, 0x66D85A5A);
        context.fill(centerX - 1, barY - 4, centerX + 1, barY + 12, 0xFFE6C879);
        context.fill(markerX - 2, barY - 7, markerX + 3, barY + 15, 0xFFFFF2CC);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("快速点击 / 空格  " + ticksLeft / 20 + "s"), boxX + boxWidth / 2, boxY + boxHeight - 14, 0xFFD8D2C0);
    }

    private void renderBox(DrawContext context, int boxX, int boxY, int boxWidth, int boxHeight) {
        context.fill(boxX + 3, boxY + 3, boxX + boxWidth + 3, boxY + boxHeight + 3, 0x77000000);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE8202430);
        context.fill(boxX + 12, boxY, boxX + boxWidth - 12, boxY + 1, 0xFFE6C879);
    }

    private boolean isTimingInSuccessZone() {
        float position = getTimingPosition();
        float start = successZoneStart();
        float width = successZoneWidthRatio();
        return position >= start && position <= start + width;
    }

    private boolean isArmWrestleWinning() {
        return clickLead() >= minigame.winClickLead;
    }

    private int clickLead() {
        return playerClicks - Math.round(elapsedSeconds() * minigame.opponentAutoClicksPerSecond);
    }

    private float powerRatio() {
        return Math.max(-1.0F, Math.min(1.0F, clickLead() / 18.0F));
    }

    private float getTimingPosition() {
        double cycle = (elapsedSeconds() * minigame.speed) % 2.0;
        return (float) (cycle <= 1.0 ? cycle : 2.0 - cycle);
    }

    private float successZoneStart() {
        if (minigame.successStart >= 0.0F) {
            return minigame.successStart;
        }
        return switch (minigame.difficulty) {
            case 1 -> 0.34F;
            case 2 -> 0.39F;
            case 3 -> 0.44F;
            default -> 0.47F;
        };
    }

    private float successZoneWidthRatio() {
        if (minigame.successWidth >= 0.0F) {
            return minigame.successWidth;
        }
        return switch (minigame.difficulty) {
            case 1 -> 0.32F;
            case 2 -> 0.22F;
            case 3 -> 0.14F;
            default -> 0.08F;
        };
    }

    private int successZoneWidth(int barWidth) {
        return Math.max(12, Math.round(successZoneWidthRatio() * barWidth));
    }

    private int elapsedTicks() {
        return Math.round(elapsedSeconds() * 20.0F);
    }

    private float elapsedSeconds() {
        return (System.nanoTime() - openedAtNanos) / 1_000_000_000.0F;
    }

    private String title() {
        return minigame.title == null || minigame.title.isBlank() ? defaultTitle(minigame.type) : minigame.title;
    }

    private static String defaultTitle(String type) {
        return "arm_wrestle".equals(type) ? "扳手腕" : "时机判定";
    }
}
