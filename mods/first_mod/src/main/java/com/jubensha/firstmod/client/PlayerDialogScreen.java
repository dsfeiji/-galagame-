package com.jubensha.firstmod.client;

import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.AdvanceDialogPayload;
import com.jubensha.firstmod.network.ArmWrestleClickPayload;
import com.jubensha.firstmod.network.DuelFinishPayload;
import com.jubensha.firstmod.network.DuelScorePayload;
import com.jubensha.firstmod.network.MinigameResultPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDialogScreen extends Screen {
    private static final int PANEL_HEIGHT = 82;
    private static final int MODEL_WIDTH = 68;
    private static final int MAX_PANEL_WIDTH = 540;
    private static final int CHOICE_HEIGHT = 12;

    private final UUID targetPlayerId;
    private final String targetPlayerName;
    private final String roleId;
    private final UUID controllerPlayerId;
    private final DialogTree dialogTree;
    private final String currentNodeId;
    private final boolean controller;
    private final List<ChoiceHitbox> choiceHitboxes = new ArrayList<>();
    private final long openedAtNanos = System.nanoTime();
    private boolean minigameSubmitted;
    private float armWrestleProgress;
    private final boolean[] openedCells = new boolean[16];
    private int duelScore;
    private int syncedActorDuelScore;
    private int syncedTargetDuelScore;
    private int rhythmScoredRound = -1;
    private boolean duelFinishSent;

    public PlayerDialogScreen(UUID targetPlayerId, String targetPlayerName, String roleId, UUID controllerPlayerId, String currentNodeId, String dialogJson) {
        super(Text.literal(targetPlayerName));
        this.targetPlayerId = targetPlayerId;
        this.targetPlayerName = targetPlayerName;
        this.roleId = roleId;
        this.controllerPlayerId = controllerPlayerId;
        this.dialogTree = DialogTree.fromJson(dialogJson);
        this.currentNodeId = currentNodeId == null || currentNodeId.isBlank() ? this.dialogTree.startNodeId : currentNodeId;
        MinecraftClient client = MinecraftClient.getInstance();
        this.controller = client.player != null && client.player.getUuid().equals(controllerPlayerId);
    }

    @Override
    protected void init() {
        clearChildren();
        choiceHitboxes.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        DialogTree.DialogNode node = currentNode();
        if (node == null) {
            return true;
        }
        if (node.minigame != null && "arm_wrestle".equals(node.minigame.type)) {
            submitArmWrestleClick(node);
            return true;
        }
        if (node.minigame != null && isScoreDuelType(node.minigame.type)) {
            handleDuelClick(node, mouseX, mouseY);
            return true;
        }
        if (!controller) {
            return true;
        }
        if (node.minigame != null) {
            submitMinigameResult(node);
            return true;
        }
        if (!node.choices.isEmpty()) {
            for (ChoiceHitbox hitbox : choiceHitboxes) {
                if (hitbox.contains(mouseX, mouseY)) {
                    advance(hitbox.choice.nextNodeId, hitbox.choiceIndex);
                    return true;
                }
            }
            return true;
        }
        advance(node.nextNodeId, -1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            DialogTree.DialogNode node = currentNode();
            if (node != null && node.minigame != null) {
                if ("arm_wrestle".equals(node.minigame.type)) {
                    submitArmWrestleClick(node);
                } else if (isScoreDuelType(node.minigame.type)) {
                    handleDuelClick(node, -1, -1);
                } else if (controller) {
                    submitMinigameResult(node);
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        DialogTree.DialogNode node = currentNode();
        if (controller && node != null && node.minigame != null && isScoreDuelType(node.minigame.type)
                && !duelFinishSent && elapsedTicks() >= node.minigame.durationTicks) {
            duelFinishSent = true;
            ClientPlayNetworking.send(new DuelFinishPayload(controllerPlayerId, targetPlayerId, node.id));
        }
    }

    private void advance(String nextNodeId, int choiceIndex) {
        ClientPlayNetworking.send(new AdvanceDialogPayload(targetPlayerId, nextNodeId == null ? "" : nextNodeId, choiceIndex));
    }

    private void submitMinigameResult(DialogTree.DialogNode node) {
        if (minigameSubmitted) {
            return;
        }
        minigameSubmitted = true;
        ClientPlayNetworking.send(new MinigameResultPayload(targetPlayerId, node.id, isTimingInSuccessZone(node.minigame)));
    }

    private void submitArmWrestleClick(DialogTree.DialogNode node) {
        ClientPlayNetworking.send(new ArmWrestleClickPayload(controllerPlayerId, targetPlayerId, node.id));
    }

    public void updateArmWrestleState(UUID controllerPlayerId, UUID targetPlayerId, String nodeId, float progress) {
        if (this.controllerPlayerId.equals(controllerPlayerId) && this.targetPlayerId.equals(targetPlayerId) && currentNodeId.equals(nodeId)) {
            this.armWrestleProgress = Math.max(-1.0F, Math.min(1.0F, progress));
        }
    }

    public void updateDuelState(UUID controllerPlayerId, UUID targetPlayerId, String nodeId, int actorScore, int targetScore) {
        if (this.controllerPlayerId.equals(controllerPlayerId) && this.targetPlayerId.equals(targetPlayerId) && currentNodeId.equals(nodeId)) {
            this.syncedActorDuelScore = actorScore;
            this.syncedTargetDuelScore = targetScore;
            this.duelScore = controller ? actorScore : targetScore;
        }
    }

    private void handleDuelClick(DialogTree.DialogNode node, double mouseX, double mouseY) {
        int delta = switch (node.minigame.type) {
            case "locker_search_duel" -> clickGrid(node, mouseX, mouseY, false);
            case "memory_flip_duel" -> elapsedTicks() < node.minigame.previewTicks ? 0 : clickGrid(node, mouseX, mouseY, true);
            case "rhythm_duel" -> clickRhythm(node);
            default -> 0;
        };
        if (delta != 0) {
            duelScore += delta;
            if (controller) {
                syncedActorDuelScore = duelScore;
            } else {
                syncedTargetDuelScore = duelScore;
            }
            ClientPlayNetworking.send(new DuelScorePayload(controllerPlayerId, targetPlayerId, node.id, delta));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(MAX_PANEL_WIDTH, this.width - 72);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = this.height - PANEL_HEIGHT - 16;
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + PANEL_HEIGHT;
        int textX = panelX + MODEL_WIDTH + 16;
        int textWidth = panelRight - textX - 16;
        DialogTree.DialogNode node = currentNode();

        renderChoices(context, node, panelX, panelY, panelRight, mouseX, mouseY);
        renderMinigame(context, node, panelX, panelY, panelRight);

        context.fill(panelX + 5, panelY + 5, panelRight - 5, panelBottom, 0xB8000000);
        context.fill(panelX + 12, panelY, panelRight - 12, panelBottom - 5, 0xF0202430);
        context.fill(panelX + 18, panelY + 2, panelRight - 18, panelY + 3, 0xFFE6C879);
        context.fill(panelX + 28, panelBottom - 9, panelRight - 28, panelBottom - 8, 0x66E6C879);
        context.fill(panelX + MODEL_WIDTH, panelY + 11, panelX + MODEL_WIDTH + 1, panelBottom - 13, 0x3FE6C879);

        int nameX = textX;
        int nameY = panelY - 16;
        int nameWidth = Math.max(72, this.textRenderer.getWidth(targetPlayerName) + 22);
        context.fill(nameX + 3, nameY + 3, nameX + nameWidth + 3, nameY + 20, 0x88000000);
        context.fill(nameX, nameY, nameX + nameWidth, nameY + 18, 0xEE252B3C);
        context.fill(nameX + 8, nameY, nameX + nameWidth - 8, nameY + 1, 0xFFE8CA78);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(targetPlayerName), nameX + nameWidth / 2, nameY + 5, 0xFFFFF2CC);
        String roleText = roleId == null || roleId.isBlank() ? "" : "ID " + roleId;
        context.drawTextWithShadow(this.textRenderer, Text.literal(roleText), panelRight - this.textRenderer.getWidth(roleText) - 18, panelY + 8, 0xFFE8CA78);

        LivingEntity target = findTargetPlayer();
        if (target != null) {
            InventoryScreen.drawEntity(context, panelX + 10, panelY + 7, panelX + MODEL_WIDTH - 8, panelBottom - 7, 28, 0.06F, mouseX, mouseY, target);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("?"), panelX + MODEL_WIDTH / 2, panelY + 38, 0xFFFFFFFF);
        }

        String dialogText = node == null ? "" : node.text;
        drawWrappedText(context, dialogText, textX, panelY + 20, textWidth, 4);

        if (node != null && node.minigame != null) {
            String hint = "arm_wrestle".equals(node.minigame.type) ? "快速点击 / 空格" : (controller ? "点击或按空格判定" : "等待对方判定");
            context.drawTextWithShadow(this.textRenderer, Text.literal(hint), panelRight - this.textRenderer.getWidth(hint) - 18, panelBottom - 18, 0xFFD8D2C0);
        } else if (node != null && node.choices.isEmpty()) {
            String hint = controller ? "点击继续" : "等待对方";
            context.drawTextWithShadow(this.textRenderer, Text.literal(hint), panelRight - this.textRenderer.getWidth(hint) - 18, panelBottom - 18, 0xFFD8D2C0);
        } else if (!controller) {
            String hint = "等待对方选择";
            context.drawTextWithShadow(this.textRenderer, Text.literal(hint), panelRight - this.textRenderer.getWidth(hint) - 18, panelBottom - 18, 0xFFD8D2C0);
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

    public boolean matches(UUID targetPlayerId, UUID controllerPlayerId) {
        return this.targetPlayerId.equals(targetPlayerId) && this.controllerPlayerId.equals(controllerPlayerId);
    }

    private DialogTree.DialogNode currentNode() {
        return dialogTree.getNode(currentNodeId);
    }

    private AbstractClientPlayerEntity findTargetPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }
        PlayerEntity player = client.world.getPlayerByUuid(targetPlayerId);
        return player instanceof AbstractClientPlayerEntity clientPlayer ? clientPlayer : null;
    }

    private void drawWrappedText(DrawContext context, String text, int x, int y, int width, int maxRows) {
        List<OrderedText> lines = this.textRenderer.wrapLines(Text.literal(text), width);
        int rows = Math.min(lines.size(), maxRows);
        for (int i = 0; i < rows; i++) {
            context.drawTextWithShadow(this.textRenderer, lines.get(i), x, y + i * 12, 0xFFF7F4EA);
        }
    }

    private void renderChoices(DrawContext context, DialogTree.DialogNode node, int panelX, int panelY, int panelRight, int mouseX, int mouseY) {
        choiceHitboxes.clear();
        if (node == null || node.minigame != null || node.choices.isEmpty()) {
            return;
        }

        int choiceWidth = Math.min(108, panelRight - panelX - MODEL_WIDTH - 28);
        int x = panelRight - choiceWidth - 2;
        int y = panelY - node.choices.size() * (CHOICE_HEIGHT + 3) - 5;
        for (int i = 0; i < node.choices.size(); i++) {
            DialogTree.DialogChoice choice = node.choices.get(i);
            ChoiceHitbox hitbox = new ChoiceHitbox(x, y, choiceWidth, CHOICE_HEIGHT, choice, i);
            choiceHitboxes.add(hitbox);
            boolean hovered = controller && hitbox.contains(mouseX, mouseY);
            int bg = hovered ? 0xF0383446 : 0xD81E2535;
            int line = hovered ? 0xFFFFE0A3 : 0xAAE6C16A;
            context.fill(x + 2, y + 2, x + choiceWidth + 2, y + CHOICE_HEIGHT + 2, 0x55000000);
            context.fill(x, y, x + choiceWidth, y + CHOICE_HEIGHT, bg);
            context.fill(x + 8, y, x + choiceWidth - 8, y + 1, line);
            context.fill(x + 8, y + CHOICE_HEIGHT - 1, x + choiceWidth - 8, y + CHOICE_HEIGHT, line);

            String answer = choice.text.isBlank() ? "..." : choice.text;
            int textOffset = choice.staminaCost > 0 ? 10 : 0;
            if (choice.staminaCost > 0) {
                drawStaminaBolt(context, x + 8, y + 2, hovered ? 0xFFFFE0A3 : 0xFFE85252);
            }

            int maxTextWidth = choiceWidth - 16 - textOffset;
            if (this.textRenderer.getWidth(answer) > maxTextWidth) {
                answer = this.textRenderer.trimToWidth(answer, maxTextWidth - this.textRenderer.getWidth("> "));
            }
            context.drawTextWithShadow(this.textRenderer, Text.literal("> " + answer), x + 7 + textOffset, y + 2, hovered ? 0xFFFFF2CC : 0xFFF7F4EA);
            y += CHOICE_HEIGHT + 3;
        }
    }

    private void renderMinigame(DrawContext context, DialogTree.DialogNode node, int panelX, int panelY, int panelRight) {
        if (node == null || node.minigame == null) {
            return;
        }
        if ("arm_wrestle".equals(node.minigame.type)) {
            renderArmWrestle(context, node, panelX, panelY, panelRight);
            return;
        }
        if (isScoreDuelType(node.minigame.type)) {
            renderScoreDuel(context, node, panelX, panelY, panelRight);
            return;
        }
        if (!"timing".equals(node.minigame.type)) {
            return;
        }

        int boxWidth = Math.min(260, panelRight - panelX - 80);
        int boxHeight = 48;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(30, panelY - boxHeight - 30);
        int barX = boxX + 24;
        int barY = boxY + 29;
        int barWidth = boxWidth - 48;
        int barHeight = 6;
        int pointerX = barX + Math.round(getTimingPosition() * barWidth);
        int zoneWidth = successZoneWidth(node.minigame.difficulty, barWidth);
        int zoneX = barX + Math.round(successZoneStart(node.minigame.difficulty) * barWidth);

        context.fill(boxX + 3, boxY + 3, boxX + boxWidth + 3, boxY + boxHeight + 3, 0x77000000);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE8202430);
        context.fill(boxX + 12, boxY, boxX + boxWidth - 12, boxY + 1, 0xFFE6C879);
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF121722);
        context.fill(zoneX, barY - 1, zoneX + zoneWidth, barY + barHeight + 1, 0xFF6FD08C);
        context.fill(pointerX - 1, barY - 6, pointerX + 2, barY + barHeight + 6, 0xFFFFF2CC);

        String title = node.minigame.title.isBlank() ? "时机判定" : node.minigame.title;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title), boxX + boxWidth / 2, boxY + 9, 0xFFFFF2CC);
    }

    private void renderArmWrestle(DrawContext context, DialogTree.DialogNode node, int panelX, int panelY, int panelRight) {
        int boxWidth = Math.min(300, panelRight - panelX - 80);
        int boxHeight = 62;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(30, panelY - boxHeight - 26);
        int barX = boxX + 26;
        int barY = boxY + 34;
        int barWidth = boxWidth - 52;
        int centerX = barX + barWidth / 2;
        int markerX = centerX + Math.round(armWrestleProgress * (barWidth / 2));

        context.fill(boxX + 3, boxY + 3, boxX + boxWidth + 3, boxY + boxHeight + 3, 0x77000000);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE8202430);
        context.fill(boxX + 12, boxY, boxX + boxWidth - 12, boxY + 1, 0xFFE6C879);
        String title = node.minigame.title.isBlank() ? "扳手腕" : node.minigame.title;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title), boxX + boxWidth / 2, boxY + 8, 0xFFFFF2CC);
        String left = controller ? "你" : targetPlayerName;
        String right = controller ? targetPlayerName : "你";
        context.drawTextWithShadow(this.textRenderer, Text.literal(left), barX, barY - 12, 0xFF6FD08C);
        context.drawTextWithShadow(this.textRenderer, Text.literal(right), barX + barWidth - this.textRenderer.getWidth(right), barY - 12, 0xFFE85252);
        context.fill(barX, barY, barX + barWidth, barY + 8, 0xFF121722);
        context.fill(barX, barY, centerX, barY + 8, 0x6656C878);
        context.fill(centerX, barY, barX + barWidth, barY + 8, 0x66D85A5A);
        context.fill(centerX - 1, barY - 4, centerX + 1, barY + 12, 0xFFE6C879);
        context.fill(markerX - 2, barY - 7, markerX + 3, barY + 15, 0xFFFFF2CC);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("快速点击 / 空格，推到底获胜"), boxX + boxWidth / 2, boxY + boxHeight - 12, 0xFFD8D2C0);
    }

    private void renderScoreDuel(DrawContext context, DialogTree.DialogNode node, int panelX, int panelY, int panelRight) {
        if ("rhythm_duel".equals(node.minigame.type)) {
            renderRhythmDuel(context, node, panelX, panelY, panelRight);
            return;
        }
        int columns = node.minigame.gridSize <= 9 ? 3 : 4;
        int rows = (int) Math.ceil(node.minigame.gridSize / (double) columns);
        int cell = 22;
        int gap = 5;
        int gridWidth = columns * cell + (columns - 1) * gap;
        int boxWidth = Math.min(300, Math.max(210, gridWidth + 48));
        int boxHeight = 50 + rows * cell + (rows - 1) * gap;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(28, panelY - boxHeight - 24);
        int gridX = boxX + (boxWidth - gridWidth) / 2;
        int gridY = boxY + 30;

        drawMiniBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(minigameTitle(node)), boxX + boxWidth / 2, boxY + 7, 0xFFFFF2CC);
        for (int i = 0; i < node.minigame.gridSize; i++) {
            int x = gridX + (i % columns) * (cell + gap);
            int y = gridY + (i / columns) * (cell + gap);
            boolean target = i == targetIndex(node) || ("memory_flip_duel".equals(node.minigame.type) && i == secondTargetIndex(node));
            boolean preview = "memory_flip_duel".equals(node.minigame.type) && elapsedTicks() < node.minigame.previewTicks && target;
            int color = openedCells[i] ? (target ? 0xFF6FD08C : 0xFF3A4050) : (preview ? 0xFFE6C879 : 0xFF151B28);
            context.fill(x, y, x + cell, y + cell, 0xFF202638);
            context.fill(x + 1, y + 1, x + cell - 1, y + cell - 1, color);
        }
        String hint = "memory_flip_duel".equals(node.minigame.type) && elapsedTicks() < node.minigame.previewTicks ? "记住发光格子" : "抢先得分";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(hint + "  " + scoreLine() + "  " + Math.max(0, node.minigame.durationTicks - elapsedTicks()) / 20 + "s"), boxX + boxWidth / 2, boxY + boxHeight - 12, 0xFFD8D2C0);
    }

    private void renderRhythmDuel(DrawContext context, DialogTree.DialogNode node, int panelX, int panelY, int panelRight) {
        int boxWidth = Math.min(300, panelRight - panelX - 80);
        int boxHeight = 58;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(30, panelY - boxHeight - 26);
        int barX = boxX + 26;
        int barY = boxY + 33;
        int barWidth = boxWidth - 52;
        int centerX = barX + barWidth / 2;
        int pulseX = barX + Math.round(rhythmProgress(node) * barWidth);

        drawMiniBox(context, boxX, boxY, boxWidth, boxHeight);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(minigameTitle(node)), boxX + boxWidth / 2, boxY + 8, 0xFFFFF2CC);
        context.fill(barX, barY, barX + barWidth, barY + 8, 0xFF121722);
        context.fill(centerX - 18, barY - 1, centerX + 18, barY + 9, 0xFF6FD08C);
        context.fill(pulseX - 2, barY - 7, pulseX + 3, barY + 15, 0xFFFFF2CC);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(scoreLine() + "  " + Math.max(0, node.minigame.durationTicks - elapsedTicks()) / 20 + "s"), boxX + boxWidth / 2, boxY + boxHeight - 11, 0xFFD8D2C0);
    }

    private void drawMiniBox(DrawContext context, int boxX, int boxY, int boxWidth, int boxHeight) {
        context.fill(boxX + 3, boxY + 3, boxX + boxWidth + 3, boxY + boxHeight + 3, 0x77000000);
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE8202430);
        context.fill(boxX + 12, boxY, boxX + boxWidth - 12, boxY + 1, 0xFFE6C879);
    }

    private boolean isTimingInSuccessZone(DialogTree.DialogMinigame minigame) {
        float position = getTimingPosition();
        float start = successZoneStart(minigame.difficulty);
        float width = successZoneWidthRatio(minigame.difficulty);
        return position >= start && position <= start + width;
    }

    private int clickGrid(DialogTree.DialogNode node, double mouseX, double mouseY, boolean memoryMode) {
        int index = cellAt(node, mouseX, mouseY);
        if (index < 0 || openedCells[index]) {
            return 0;
        }
        openedCells[index] = true;
        if (index == targetIndex(node) || (memoryMode && index == secondTargetIndex(node))) {
            return 1;
        }
        return -1;
    }

    private int clickRhythm(DialogTree.DialogNode node) {
        int round = rhythmRound(node);
        if (round == rhythmScoredRound || round >= node.minigame.rounds) {
            return 0;
        }
        rhythmScoredRound = round;
        return Math.abs(rhythmProgress(node) - 0.5F) <= 0.15F ? 1 : -1;
    }

    private int cellAt(DialogTree.DialogNode node, double mouseX, double mouseY) {
        if (mouseX < 0 || mouseY < 0) {
            return -1;
        }
        int columns = node.minigame.gridSize <= 9 ? 3 : 4;
        int rows = (int) Math.ceil(node.minigame.gridSize / (double) columns);
        int cell = 22;
        int gap = 5;
        int gridWidth = columns * cell + (columns - 1) * gap;
        int boxWidth = Math.min(300, Math.max(210, gridWidth + 48));
        int boxHeight = 50 + rows * cell + (rows - 1) * gap;
        int boxX = (this.width - boxWidth) / 2;
        int boxY = Math.max(28, this.height - PANEL_HEIGHT - 16 - boxHeight - 24);
        int gridX = boxX + (boxWidth - gridWidth) / 2;
        int gridY = boxY + 30;
        int column = (int) ((mouseX - gridX) / (cell + gap));
        int row = (int) ((mouseY - gridY) / (cell + gap));
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            return -1;
        }
        int index = row * columns + column;
        int cellX = gridX + column * (cell + gap);
        int cellY = gridY + row * (cell + gap);
        return index < node.minigame.gridSize && mouseX >= cellX && mouseX <= cellX + cell && mouseY >= cellY && mouseY <= cellY + cell ? index : -1;
    }

    private int targetIndex(DialogTree.DialogNode node) {
        if (node.minigame.targetIndex >= 0) {
            return node.minigame.targetIndex;
        }
        return Math.floorMod(node.id.hashCode(), node.minigame.gridSize);
    }

    private int secondTargetIndex(DialogTree.DialogNode node) {
        return (targetIndex(node) + Math.max(1, node.minigame.gridSize / 2)) % node.minigame.gridSize;
    }

    private float rhythmProgress(DialogTree.DialogNode node) {
        float roundLength = Math.max(8.0F, node.minigame.durationTicks / (float) node.minigame.rounds);
        return (elapsedTicks() % roundLength) / roundLength;
    }

    private int rhythmRound(DialogTree.DialogNode node) {
        float roundLength = Math.max(8.0F, node.minigame.durationTicks / (float) node.minigame.rounds);
        return (int) (elapsedTicks() / roundLength);
    }

    private boolean isScoreDuelType(String type) {
        return "locker_search_duel".equals(type) || "rhythm_duel".equals(type) || "memory_flip_duel".equals(type);
    }

    private String scoreLine() {
        int ownScore = controller ? syncedActorDuelScore : syncedTargetDuelScore;
        int opponentScore = controller ? syncedTargetDuelScore : syncedActorDuelScore;
        return "你 " + ownScore + " / 对手 " + opponentScore;
    }

    private String minigameTitle(DialogTree.DialogNode node) {
        if (!node.minigame.title.isBlank()) {
            return node.minigame.title;
        }
        return switch (node.minigame.type) {
            case "locker_search_duel" -> "抢柜子";
            case "rhythm_duel" -> "节奏对抗";
            case "memory_flip_duel" -> "记忆翻牌";
            default -> "小游戏";
        };
    }

    private float getTimingPosition() {
        double seconds = (System.nanoTime() - openedAtNanos) / 1_000_000_000.0;
        DialogTree.DialogNode node = currentNode();
        float speed = node == null || node.minigame == null || node.minigame.speed <= 0.0F ? 0.78F : node.minigame.speed;
        double cycle = (seconds * speed) % 2.0;
        return (float) (cycle <= 1.0 ? cycle : 2.0 - cycle);
    }

    private float successZoneStart(int difficulty) {
        DialogTree.DialogNode node = currentNode();
        if (node != null && node.minigame != null && node.minigame.successStart >= 0.0F) {
            return Math.min(1.0F, node.minigame.successStart);
        }
        return switch (Math.max(1, Math.min(4, difficulty))) {
            case 1 -> 0.34F;
            case 2 -> 0.39F;
            case 3 -> 0.44F;
            default -> 0.47F;
        };
    }

    private float successZoneWidthRatio(int difficulty) {
        DialogTree.DialogNode node = currentNode();
        if (node != null && node.minigame != null && node.minigame.successWidth >= 0.0F) {
            return Math.min(1.0F, node.minigame.successWidth);
        }
        return switch (Math.max(1, Math.min(4, difficulty))) {
            case 1 -> 0.32F;
            case 2 -> 0.22F;
            case 3 -> 0.14F;
            default -> 0.08F;
        };
    }

    private int successZoneWidth(int difficulty, int barWidth) {
        return Math.max(12, Math.round(successZoneWidthRatio(difficulty) * barWidth));
    }

    private int elapsedTicks() {
        return Math.round(((System.nanoTime() - openedAtNanos) / 1_000_000_000.0F) * 20.0F);
    }

    private void drawStaminaBolt(DrawContext context, int x, int y, int color) {
        context.fill(x + 3, y, x + 6, y + 1, color);
        context.fill(x + 2, y + 1, x + 5, y + 2, color);
        context.fill(x + 1, y + 2, x + 4, y + 3, color);
        context.fill(x + 3, y + 3, x + 6, y + 4, color);
        context.fill(x + 2, y + 4, x + 5, y + 5, color);
        context.fill(x + 1, y + 5, x + 3, y + 6, color);
    }

    private record ChoiceHitbox(int x, int y, int width, int height, DialogTree.DialogChoice choice, int choiceIndex) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}
