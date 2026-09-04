package com.jubensha.firstmod.client;

import com.google.gson.JsonParser;
import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.SaveDialogPayload;
import com.jubensha.firstmod.network.SaveMinigamePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class RoleControlScreen extends Screen {
    private static final int PANEL_WIDTH = 468;
    private static final int PANEL_HEIGHT = 252;
    private static final int ROW_HEIGHT = 18;
    private static final int MAX_ROWS = 7;

    private final List<Path> jsonFiles = new ArrayList<>();
    private TextFieldWidget roleField;
    private TextFieldWidget phaseField;
    private boolean minigameMode;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private String message = "选择 JSON 文件并导入";

    public RoleControlScreen() {
        super(Text.literal("剧情控制面板"));
    }

    @Override
    protected void init() {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = panelY();

        roleField = new TextFieldWidget(this.textRenderer, panelX + 30, panelY + 72, 150, 18, Text.literal("角色ID"));
        roleField.setMaxLength(64);
        roleField.setText("role_1");

        phaseField = new TextFieldWidget(this.textRenderer, panelX + 30, panelY + 120, 54, 18, Text.literal("阶段"));
        phaseField.setMaxLength(3);
        phaseField.setText("1");

        reloadFiles();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearChildren();
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int listX = panelX + 210;
        int listY = panelY + 58;
        int listWidth = PANEL_WIDTH - 236;

        addDrawableChild(ButtonWidget.builder(Text.literal("对话JSON"), button -> switchMode(false))
                .dimensions(panelX + 30, panelY + 36, 72, 18)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("小游戏JSON"), button -> switchMode(true))
                .dimensions(panelX + 108, panelY + 36, 82, 18)
                .build());

        if (!minigameMode) {
            addDrawableChild(roleField);
            addDrawableChild(phaseField);
        }

        int visibleRows = Math.min(MAX_ROWS, jsonFiles.size());
        for (int i = 0; i < visibleRows; i++) {
            int fileIndex = scrollOffset + i;
            if (fileIndex >= jsonFiles.size()) {
                break;
            }
            Path path = jsonFiles.get(fileIndex);
            String fileName = trimMiddle(path.getFileName().toString(), 46);
            final int selected = fileIndex;
            addDrawableChild(ButtonWidget.builder(Text.literal(fileName), button -> {
                        selectedIndex = selected;
                        message = "已选择：" + path.getFileName();
                        rebuildButtons();
                    })
                    .dimensions(listX, listY + i * ROW_HEIGHT, listWidth, 16)
                    .build());
        }

        int leftButtonY = panelY + 152;
        addDrawableChild(ButtonWidget.builder(Text.literal("打开文件夹"), button -> openFolder())
                .dimensions(panelX + 30, leftButtonY, 82, 18)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("刷新"), button -> {
                    reloadFiles();
                    rebuildButtons();
                })
                .dimensions(panelX + 118, leftButtonY, 48, 18)
                .build());

        int listButtonY = panelY + 190;
        addDrawableChild(ButtonWidget.builder(Text.literal("上翻"), button -> {
                    scrollOffset = Math.max(0, scrollOffset - 1);
                    rebuildButtons();
                })
                .dimensions(panelX + 210, listButtonY, 46, 18)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("下翻"), button -> {
                    scrollOffset = Math.min(Math.max(0, jsonFiles.size() - MAX_ROWS), scrollOffset + 1);
                    rebuildButtons();
                })
                .dimensions(panelX + 262, listButtonY, 46, 18)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("导入"), button -> importSelected())
                .dimensions(panelX + 318, listButtonY, 50, 18)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("关闭"), button -> close())
                .dimensions(panelX + 376, listButtonY, 48, 18)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = panelY();
        int panelRight = panelX + PANEL_WIDTH;
        int panelBottom = panelY + PANEL_HEIGHT;

        context.fill(panelX + 4, panelY + 4, panelRight + 4, panelBottom + 4, 0x70000000);
        context.fill(panelX, panelY, panelRight, panelBottom, 0xF0191D28);
        context.fill(panelX, panelY, panelRight, panelY + 29, 0xF0242B3A);
        context.fill(panelX + 14, panelY + 2, panelRight - 14, panelY + 3, 0xFFE6C879);
        context.fill(panelX + 196, panelY + 42, panelX + 197, panelY + 212, 0x44E6C879);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("剧情控制面板"), this.width / 2, panelY + 11, 0xFFFFF2CC);

        context.fill(panelX + (minigameMode ? 108 : 30), panelY + 55, panelX + (minigameMode ? 190 : 102), panelY + 56, 0xFFE6C879);
        if (minigameMode) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("小游戏配置"), panelX + 30, panelY + 70, 0xFFE6C879);
            context.drawTextWithShadow(this.textRenderer, Text.literal("右键地图方块触发小游戏"), panelX + 30, panelY + 92, 0xFFE4D7B4);
            context.drawTextWithShadow(this.textRenderer, Text.literal("导入后立即保存在服务器配置"), panelX + 30, panelY + 108, 0xFFB9C6D6);
        } else {
            context.drawTextWithShadow(this.textRenderer, Text.literal("角色对话配置"), panelX + 30, panelY + 58, 0xFFE6C879);
            context.drawTextWithShadow(this.textRenderer, Text.literal("角色ID"), panelX + 30, panelY + 62, 0xFFE4D7B4);
            context.drawTextWithShadow(this.textRenderer, Text.literal("阶段"), panelX + 30, panelY + 110, 0xFFE4D7B4);
            context.drawTextWithShadow(this.textRenderer, Text.literal("/dialogrole claim <角色ID>"), panelX + 30, panelY + 182, 0xFFB9C6D6);
        }

        context.drawTextWithShadow(this.textRenderer, Text.literal("JSON 文件"), panelX + 210, panelY + 44, 0xFFE6C879);
        context.drawTextWithShadow(this.textRenderer, Text.literal("目录：" + currentDisplayPath()), panelX + 30, panelBottom - 34, 0xFFB9C6D6);
        context.drawTextWithShadow(this.textRenderer, Text.literal(trimMiddle(message, 54)), panelX + 30, panelBottom - 17, 0xFFFFFFFF);

        if (jsonFiles.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("未找到 .json 文件"), panelX + 326, panelY + 118, 0xFFB9C6D6);
        } else if (selectedIndex >= scrollOffset && selectedIndex < scrollOffset + MAX_ROWS) {
            int y = panelY + 58 + (selectedIndex - scrollOffset) * ROW_HEIGHT;
            context.fill(panelX + 210, y, panelRight - 26, y + 16, 0x44E6C879);
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

    private void switchMode(boolean minigameMode) {
        if (this.minigameMode == minigameMode) {
            return;
        }
        this.minigameMode = minigameMode;
        selectedIndex = -1;
        scrollOffset = 0;
        message = minigameMode ? "选择小游戏 JSON 文件并导入" : "选择对话 JSON 文件并导入";
        reloadFiles();
        rebuildButtons();
    }

    private void reloadFiles() {
        ensureCurrentFolder();
        jsonFiles.clear();
        try (Stream<Path> stream = Files.list(currentFolder())) {
            stream.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".json"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .forEach(jsonFiles::add);
        } catch (IOException ignored) {
        }
        if (selectedIndex >= jsonFiles.size()) {
            selectedIndex = jsonFiles.isEmpty() ? -1 : 0;
        }
        scrollOffset = Math.min(scrollOffset, Math.max(0, jsonFiles.size() - MAX_ROWS));
    }

    private void importSelected() {
        if (selectedIndex < 0 || selectedIndex >= jsonFiles.size()) {
            message = "请先选择一个 JSON 文件";
            return;
        }

        Path path = jsonFiles.get(selectedIndex);
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            if (minigameMode) {
                JsonParser.parseString(json).getAsJsonObject();
                ClientPlayNetworking.send(new SaveMinigamePayload(json));
                message = "已导入小游戏：" + path.getFileName();
            } else {
                importDialog(json);
            }
        } catch (IOException exception) {
            message = "读取失败：" + exception.getMessage();
        } catch (RuntimeException exception) {
            message = "JSON 无效：" + exception.getMessage();
        }
    }

    private void importDialog(String json) {
        String roleId = roleField.getText().trim();
        if (!roleId.matches("[a-z0-9_./-]{1,64}")) {
            message = "角色ID无效，只能用小写英文、数字、_、-、.、/";
            return;
        }
        int phase;
        try {
            phase = Math.max(1, Integer.parseInt(phaseField.getText().trim()));
        } catch (NumberFormatException exception) {
            message = "阶段必须是数字";
            return;
        }
        DialogTree.fromJsonStrict(json);
        ClientPlayNetworking.send(new SaveDialogPayload(roleId, phase, json));
        message = "已导入到 " + roleId + " 的第 " + phase + " 阶段";
    }

    private void openFolder() {
        boolean opened = minigameMode ? MinigameJsonFolder.openFolder() : DialogJsonFolder.openFolder();
        if (opened) {
            message = "文件夹已打开，放入文件后点刷新";
        } else {
            message = "目录：" + currentDisplayPath();
        }
    }

    private void ensureCurrentFolder() {
        if (minigameMode) {
            MinigameJsonFolder.ensureExists();
        } else {
            DialogJsonFolder.ensureExists();
        }
    }

    private Path currentFolder() {
        return minigameMode ? MinigameJsonFolder.getFolder() : DialogJsonFolder.getFolder();
    }

    private String currentDisplayPath() {
        return minigameMode ? MinigameJsonFolder.getDisplayPath() : DialogJsonFolder.getDisplayPath();
    }

    private int panelY() {
        return Math.max(24, (this.height - PANEL_HEIGHT) / 2);
    }

    private static String trimMiddle(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        int side = Math.max(1, (maxLength - 3) / 2);
        return value.substring(0, side) + "..." + value.substring(value.length() - side);
    }
}
