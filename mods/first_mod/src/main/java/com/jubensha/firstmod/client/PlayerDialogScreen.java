package com.jubensha.firstmod.client;

import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.AdvanceDialogPayload;
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
        if (!controller) {
            return true;
        }

        DialogTree.DialogNode node = currentNode();
        if (node == null) {
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

    private void advance(String nextNodeId, int choiceIndex) {
        ClientPlayNetworking.send(new AdvanceDialogPayload(targetPlayerId, nextNodeId == null ? "" : nextNodeId, choiceIndex));
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

        if (node != null && node.choices.isEmpty()) {
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
        if (node == null || node.choices.isEmpty()) {
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
