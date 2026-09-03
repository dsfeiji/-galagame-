package com.jubensha.firstmod.client;

import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.SaveDialogPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class DialogImportScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int MAX_ROWS = 8;

    private final Screen parent;
    private final String roleId;
    private final int phase;
    private final List<Path> jsonFiles = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private String message = "把 .json 文件放入目录后，在这里选择导入。";

    public DialogImportScreen(Screen parent, String roleId, int phase) {
        super(Text.literal("对话 JSON 文件夹"));
        this.parent = parent;
        this.roleId = roleId;
        this.phase = phase;
    }

    @Override
    protected void init() {
        reloadFiles();
        rebuildControls();
    }

    private void rebuildControls() {
        clearChildren();
        int panelWidth = Math.min(520, this.width - 48);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(30, this.height / 2 - 118);
        int listX = panelX + 18;
        int listY = panelY + 58;
        int listWidth = panelWidth - 36;

        int visibleRows = Math.min(MAX_ROWS, jsonFiles.size());
        for (int i = 0; i < visibleRows; i++) {
            int fileIndex = scrollOffset + i;
            if (fileIndex >= jsonFiles.size()) {
                break;
            }
            Path path = jsonFiles.get(fileIndex);
            String fileName = path.getFileName().toString();
            final int selected = fileIndex;
            addDrawableChild(ButtonWidget.builder(Text.literal(fileName), button -> {
                        selectedIndex = selected;
                        importSelected();
                    })
                    .dimensions(listX, listY + i * ROW_HEIGHT, listWidth, 20)
                    .build());
        }

        int buttonY = panelY + 58 + MAX_ROWS * ROW_HEIGHT + 10;
        addDrawableChild(ButtonWidget.builder(Text.literal("打开目录"), button -> openFolder())
                .dimensions(panelX + 18, buttonY, 92, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("刷新"), button -> {
                    reloadFiles();
                    rebuildControls();
                })
                .dimensions(panelX + 116, buttonY, 72, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("上翻"), button -> {
                    scrollOffset = Math.max(0, scrollOffset - 1);
                    rebuildControls();
                })
                .dimensions(panelX + 194, buttonY, 42, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("下翻"), button -> {
                    scrollOffset = Math.min(Math.max(0, jsonFiles.size() - MAX_ROWS), scrollOffset + 1);
                    rebuildControls();
                })
                .dimensions(panelX + 242, buttonY, 52, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("导入"), button -> importSelected())
                .dimensions(panelX + panelWidth - 154, buttonY, 58, 20)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal("返回"), button -> close())
                .dimensions(panelX + panelWidth - 88, buttonY, 58, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(520, this.width - 48);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = Math.max(30, this.height / 2 - 118);
        int panelBottom = panelY + 248;

        context.fill(panelX, panelY, panelX + panelWidth, panelBottom, 0xF0181A24);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFFE9C46A);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("对话 JSON：" + roleId + "  阶段 " + phase), this.width / 2, panelY + 14, 0xFFFFF2CC);
        context.drawTextWithShadow(this.textRenderer, Text.literal("目录：" + DialogJsonFolder.getDisplayPath()), panelX + 18, panelY + 34, 0xFFE2D1A0);
        context.drawTextWithShadow(this.textRenderer, Text.literal(message), panelX + 18, panelBottom - 18, 0xFFFFFFFF);

        if (jsonFiles.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("未找到 .json 文件"), this.width / 2, panelY + 112, 0xFFB8C7E0);
        } else if (selectedIndex >= 0 && selectedIndex < jsonFiles.size()) {
            int y = panelY + 58 + (selectedIndex - scrollOffset) * ROW_HEIGHT;
            if (selectedIndex >= scrollOffset && selectedIndex < scrollOffset + MAX_ROWS) {
                context.fill(panelX + 18, y, panelX + panelWidth - 18, y + 20, 0x44E9C46A);
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void blur() {
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void reloadFiles() {
        DialogJsonFolder.ensureExists();
        jsonFiles.clear();
        try (Stream<Path> stream = Files.list(DialogJsonFolder.getFolder())) {
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
            message = "请先选择一个 JSON 文件。";
            return;
        }
        Path path = jsonFiles.get(selectedIndex);
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            DialogTree.fromJsonStrict(json);
            ClientPlayNetworking.send(new SaveDialogPayload(roleId, phase, json));
            message = "已导入：" + path.getFileName();
        } catch (IOException exception) {
            message = "读取失败：" + exception.getMessage();
        } catch (RuntimeException exception) {
            message = "JSON 无效：" + exception.getMessage();
        }
    }

    private void openFolder() {
        if (DialogJsonFolder.openFolder()) {
            message = "目录已打开，放入文件后点刷新。";
        } else {
            message = "目录：" + DialogJsonFolder.getDisplayPath();
        }
    }
}
