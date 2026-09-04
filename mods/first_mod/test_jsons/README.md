# 测试 JSON 包

把这些文件复制到游戏运行目录对应文件夹后，在游戏内按 `O` 导入。

## 对话测试

复制到：

```text
./first_mod_dialogs
```

导入：

```text
dialog_full_test.json
dialog_arm_wrestle_test.json
```

建议角色 ID：`test_npc`

## 小游戏测试

复制到：

```text
./first_mod_minigames
```

导入：

```text
block_minigame_test.json
block_arm_wrestle_test.json
item_minigame_test.json
```

测试前先设置：

```mcfunction
/dialogphase setcount 3
/dialogphase set 1
/dialogprotagonist set <你的玩家名>
```

右键讲台 `minecraft:lectern` 可以测试方块小游戏。
右键橡木木板 `minecraft:oak_planks` 可以测试方块扳手腕小游戏。
手持纸 `minecraft:paper` 右键可以测试物品小游戏。
