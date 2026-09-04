package com.jubensha.firstmod.client;

import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.InteractionMinigameResultPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class InteractionMinigameScreen extends Screen {
    private final String interactionId;
    private final DialogTree.DialogMinigame minigame;
    private final long openedAtNanos = System.nanoTime();
    private final boolean[] openedCells;
    private boolean submitted;
    private float armProgress;
    private int playerScore;
    private int rhythmScoredRound = -1;
    private int gridTargetIndex = -1;
    private int gridSecondTargetIndex = -1;
    private final int[] memoryTargetIndexes = new int[6];
    private int memoryHitsInSet;
    private int memoryPreviewUntilTick;
    private int rhythmRoundIndex;
    private int rhythmRoundStartedTick;
    private float rhythmZoneCenter = -1.0F;
    private float rhythmZoneWidth = 0.18F;

    public InteractionMinigameScreen(String interactionId, DialogTree.DialogMinigame minigame) {
        super(Text.literal(minigame.title == null || minigame.title.isBlank() ? defaultTitle(minigame.type) : minigame.title));
        this.interactionId = interactionId;
        this.minigame = minigame;
        this.openedCells = new boolean[minigame.gridSize];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        press(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER) {
            press(-1, -1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (submitted) {
            return;
        }
        if ("arm_wrestle".equals(minigame.type)) {
            armProgress = Math.max(-minigame.winProgress, armProgress - minigame.opponentAutoClicksPerSecond * minigame.pushPerClick / 20.0F);
            if (armProgress <= -minigame.winProgress) {
                submit(false);
            }
        } else if (isScoreDuelType() && elapsedTicks() >= minigame.durationTicks) {
            submit(playerScore >= aiScore() + minigame.winClickLead);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        switch (minigame.type) {
            case "arm_wrestle" -> renderArmWrestle(context);
            case "locker_search_duel" -> renderGridGame(context, "抢先找到发光格子");
            case "memory_flip_duel" -> renderGridGame(context, memoryPreviewActive() ? "记住发光格子" : "翻出刚才的位置");
            case "rhythm_duel" -> renderRhythm(context);
            default -> renderTiming(context);
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

    private void press(double mouseX, double mouseY) {
        if (submitted) {
            return;
        }
        switch (minigame.type) {
            case "arm_wrestle" -> {
                armProgress = Math.min(minigame.winProgress, armProgress + minigame.pushPerClick);
                if (armProgress >= minigame.winProgress) {
                    submit(true);
                }
            }
            case "locker_search_duel" -> clickGrid(mouseX, mouseY, false);
            case "memory_flip_duel" -> {
                if (!memoryPreviewActive()) {
                    clickGrid(mouseX, mouseY, true);
                }
            }
            case "rhythm_duel" -> clickRhythm();
            default -> submit(isTimingInSuccessZone());
        }
    }

    private void clickGrid(double mouseX, double mouseY, boolean memoryMode) {
        ensureGridTargets();
        int index = cellAt(mouseX, mouseY);
        if (index < 0 || openedCells[index]) {
            return;
        }
        openedCells[index] = true;
        if ((!memoryMode && index == targetIndex()) || (memoryMode && isMemoryTarget(index))) {
            playerScore++;
            if (memoryMode) {
                memoryHitsInSet++;
                if (memoryHitsInSet >= memoryTargetCount()) {
                    chooseNextGridTargets();
                }
            } else {
                chooseNextGridTargets();
            }
        } else {
            if (memoryMode) {
                playerScore--;
                chooseNextGridTargets();
            }
        }
    }

    private void clickRhythm() {
        ensureRhythmRound();
        int round = rhythmRound();
        if (round == rhythmScoredRound) {
            return;
        }
        rhythmScoredRound = round;
        playerScore += isRhythmHitWindow() ? 1 : -1;
        nextRhythmRound();
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
        int markerX = centerX + Math.round(armProgress * (barWidth / 2));

        renderBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title()), boxX + boxWidth / 2, boxY + 9, 0xFFFFF2CC);
        String left = localPlayerName();
        String right = "AI";
        context.drawTextWithShadow(this.textRenderer, Text.literal(left), barX, barY - 13, 0xFF6FD08C);
        context.drawTextWithShadow(this.textRenderer, Text.literal(right), barX + barWidth - this.textRenderer.getWidth(right), barY - 13, 0xFFE85252);
        context.fill(barX, barY, barX + barWidth, barY + 8, 0xFF121722);
        context.fill(barX, barY, centerX, barY + 8, 0x6656C878);
        context.fill(centerX, barY, barX + barWidth, barY + 8, 0x66D85A5A);
        context.fill(centerX - 1, barY - 4, centerX + 1, barY + 12, 0xFFE6C879);
        context.fill(markerX - 2, barY - 7, markerX + 3, barY + 15, 0xFFFFF2CC);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("快速点击 / 空格，推到底获胜"), boxX + boxWidth / 2, boxY + boxHeight - 14, 0xFFD8D2C0);
    }

    private void renderGridGame(DrawContext context, String hint) {
        int columns = gridColumns();
        int rows = (int) Math.ceil(minigame.gridSize / (double) columns);
        int cell = 24;
        int gap = 5;
        int gridWidth = columns * cell + (columns - 1) * gap;
        int boxWidth = Math.max(210, gridWidth + 48);
        int boxHeight = 52 + rows * cell + (rows - 1) * gap;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(26, (this.height - boxHeight) / 2 - 10);
        int gridX = boxX + (boxWidth - gridWidth) / 2;
        int gridY = boxY + 34;

        renderBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title()), boxX + boxWidth / 2, boxY + 8, 0xFFFFF2CC);
        for (int i = 0; i < minigame.gridSize; i++) {
            ensureGridTargets();
            int x = gridX + (i % columns) * (cell + gap);
            int y = gridY + (i / columns) * (cell + gap);
            boolean target = "memory_flip_duel".equals(minigame.type) ? isMemoryTarget(i) : i == targetIndex();
            boolean preview = "memory_flip_duel".equals(minigame.type) && memoryPreviewActive() && target;
            int color = openedCells[i] ? (target ? 0xFF6FD08C : 0xFF3A4050) : (preview ? 0xFFE6C879 : 0xFF202638);
            context.fill(x, y, x + cell, y + cell, color);
            context.fill(x + 1, y + 1, x + cell - 1, y + cell - 1, openedCells[i] || preview ? color : 0xFF151B28);
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint + "  你 " + playerScore + " / AI " + aiScore() + "  " + secondsLeft() + "s"), boxX + boxWidth / 2, boxY + boxHeight - 13, 0xFFD8D2C0);
    }

    private void renderRhythm(DrawContext context) {
        int boxWidth = Math.min(300, this.width - 48);
        int boxHeight = 72;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(36, (this.height - boxHeight) / 2 - 18);
        int barX = boxX + 28;
        int barY = boxY + 38;
        int barWidth = boxWidth - 56;
        int pulseX = barX + Math.round(rhythmProgress() * barWidth);
        ensureRhythmRound();
        int centerX = barX + Math.round(rhythmZoneCenter * barWidth);
        int zone = Math.max(3, Math.round(rhythmZoneWidth * barWidth / 2.0F));

        renderBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title()), boxX + boxWidth / 2, boxY + 9, 0xFFFFF2CC);
        context.fill(barX, barY, barX + barWidth, barY + 8, 0xFF121722);
        context.fill(centerX - zone, barY - 1, centerX + zone, barY + 9, 0xFF6FD08C);
        context.fill(pulseX - 2, barY - 7, pulseX + 3, barY + 15, 0xFFFFF2CC);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("分数 " + playerScore + " / AI " + aiScore() + "  " + secondsLeft() + "s"), boxX + boxWidth / 2, boxY + boxHeight - 14, 0xFFD8D2C0);
    }

    private void renderBox(DrawContext context, int boxX, int boxY, int boxWidth, int boxHeight) {
        context.fill(boxX + 3, boxY + 3, boxX + boxWidth + 3, boxY + boxHeight + 3, 0x77000000);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE8202430);
        context.fill(boxX + 12, boxY, boxX + boxWidth - 12, boxY + 1, 0xFFE6C879);
    }

    private boolean isScoreDuelType() {
        return "locker_search_duel".equals(minigame.type) || "rhythm_duel".equals(minigame.type) || "memory_flip_duel".equals(minigame.type);
    }

    private int cellAt(double mouseX, double mouseY) {
        if (mouseX < 0 || mouseY < 0) {
            return -1;
        }
        int columns = gridColumns();
        int rows = (int) Math.ceil(minigame.gridSize / (double) columns);
        int cell = 24;
        int gap = 5;
        int gridWidth = columns * cell + (columns - 1) * gap;
        int boxWidth = Math.max(210, gridWidth + 48);
        int boxHeight = 52 + rows * cell + (rows - 1) * gap;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(26, (this.height - boxHeight) / 2 - 10);
        int gridX = boxX + (boxWidth - gridWidth) / 2;
        int gridY = boxY + 34;
        int column = (int) ((mouseX - gridX) / (cell + gap));
        int row = (int) ((mouseY - gridY) / (cell + gap));
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            return -1;
        }
        int index = row * columns + column;
        int cellX = gridX + column * (cell + gap);
        int cellY = gridY + row * (cell + gap);
        return index < minigame.gridSize && mouseX >= cellX && mouseX <= cellX + cell && mouseY >= cellY && mouseY <= cellY + cell ? index : -1;
    }

    private int targetIndex() {
        ensureGridTargets();
        return gridTargetIndex;
    }

    private int secondTargetIndex() {
        ensureGridTargets();
        return gridSecondTargetIndex;
    }

    private int gridColumns() {
        return "memory_flip_duel".equals(minigame.type) ? 5 : (minigame.gridSize <= 9 ? 3 : 4);
    }

    private int aiScore() {
        int maxScore = switch (minigame.type) {
            case "locker_search_duel" -> 1;
            case "memory_flip_duel" -> 6;
            default -> minigame.rounds;
        };
        return Math.round(maxScore * minigame.opponentAccuracy);
    }

    private boolean isTimingInSuccessZone() {
        float position = getTimingPosition();
        float start = successZoneStart();
        float width = successZoneWidthRatio();
        return position >= start && position <= start + width;
    }

    private float getTimingPosition() {
        double cycle = (elapsedSeconds() * minigame.speed) % 2.0;
        return (float) (cycle <= 1.0 ? cycle : 2.0 - cycle);
    }

    private float rhythmProgress() {
        ensureRhythmRound();
        float roundLength = rhythmRoundLength();
        return ((elapsedTicks() - rhythmRoundStartedTick) % roundLength) / roundLength;
    }

    private int rhythmRound() {
        ensureRhythmRound();
        return rhythmRoundIndex;
    }

    private boolean isRhythmHitWindow() {
        return Math.abs(rhythmProgress() - rhythmZoneCenter) <= rhythmZoneWidth / 2.0F;
    }

    private void ensureGridTargets() {
        if (gridTargetIndex < 0 || gridTargetIndex >= minigame.gridSize) {
            chooseNextGridTargets();
        }
    }

    private void chooseNextGridTargets() {
        Arrays.fill(openedCells, false);
        int baseTarget = Math.max(0, minigame.targetIndex);
        int seed = (interactionId + ":" + baseTarget + ":" + elapsedTicks() + ":" + playerScore + ":" + System.nanoTime()).hashCode();
        gridTargetIndex = Math.floorMod(seed, minigame.gridSize);
        int offset = Math.max(1, Math.floorMod(seed / 17, minigame.gridSize - 1) + 1);
        gridSecondTargetIndex = (gridTargetIndex + offset) % minigame.gridSize;
        for (int i = 0; i < memoryTargetIndexes.length; i++) {
            memoryTargetIndexes[i] = -1;
        }
        int targetCount = memoryTargetCount();
        int cursor = Math.floorMod(seed, minigame.gridSize);
        for (int i = 0; i < targetCount; i++) {
            while (containsMemoryTarget(cursor, i)) {
                cursor = (cursor + 1) % minigame.gridSize;
            }
            memoryTargetIndexes[i] = cursor;
            int step = Math.max(1, Math.floorMod(seed / Math.max(1, i + 3), minigame.gridSize - 1) + 1);
            cursor = (cursor + step) % minigame.gridSize;
        }
        memoryHitsInSet = 0;
        memoryPreviewUntilTick = elapsedTicks() + minigame.previewTicks;
    }

    private boolean allCellsOpened() {
        for (int i = 0; i < minigame.gridSize; i++) {
            if (!openedCells[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean memoryPreviewActive() {
        return elapsedTicks() < memoryPreviewUntilTick;
    }

    private boolean isMemoryTarget(int index) {
        ensureGridTargets();
        return containsMemoryTarget(index, memoryTargetCount());
    }

    private boolean containsMemoryTarget(int index, int limit) {
        for (int i = 0; i < limit && i < memoryTargetIndexes.length; i++) {
            if (memoryTargetIndexes[i] == index) {
                return true;
            }
        }
        return false;
    }

    private int memoryTargetCount() {
        return Math.min(6, minigame.gridSize);
    }

    private void ensureRhythmRound() {
        if (rhythmZoneCenter < 0.0F) {
            nextRhythmRound();
        }
    }

    private void nextRhythmRound() {
        rhythmRoundIndex++;
        rhythmRoundStartedTick = elapsedTicks();
        int seed = Math.floorMod((interactionId + ":rhythm:" + rhythmRoundIndex + ":" + System.nanoTime()).hashCode(), 1000);
        rhythmZoneCenter = 0.22F + seed / 999.0F * 0.56F;
        int widthSeed = Math.floorMod(seed * 31 + rhythmRoundIndex * 17, 1000);
        rhythmZoneWidth = 0.035F + widthSeed / 999.0F * 0.155F;
    }

    private float rhythmRoundLength() {
        return Math.max(10.0F, 34.0F / Math.max(0.5F, minigame.speed));
    }

    private String localPlayerName() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.player == null ? "玩家" : client.player.getNameForScoreboard();
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

    private int secondsLeft() {
        return Math.max(0, (minigame.durationTicks - elapsedTicks()) / 20);
    }

    private String title() {
        return minigame.title == null || minigame.title.isBlank() ? defaultTitle(minigame.type) : minigame.title;
    }

    private static String defaultTitle(String type) {
        return switch (type) {
            case "arm_wrestle" -> "扳手腕";
            case "locker_search_duel" -> "抢柜子";
            case "rhythm_duel" -> "节奏对抗";
            case "memory_flip_duel" -> "记忆翻牌";
            default -> "时机判定";
        };
    }
}
