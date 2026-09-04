# 第一版剧本包：第八节课

这是当前模组可直接导入的第一版剧情 JSON。每个文件对应一个角色在某个阶段的对话。

## 角色 ID

- 主角传送角色 ID：`protagonist`
- 班长：`monitor`
- 体育生：`athlete`
- 学霸：`scholar`
- 胆小学生：`timid`
- 富家学生：`rich`
- 广播员：`broadcaster`
- 转学生：`transfer`
- 老师：`teacher`

## 阶段设置

在游戏中先执行：

```mcfunction
/dialogphase setcount 5
```

建议导入顺序：

1. 第 1 阶段导入所有 `phase1_*.json`，用于开局收集信息。
2. 第 2 阶段导入 `phase2_monitor.json` 和 `phase2_athlete.json`。
3. 第 3 阶段导入 `phase3_scholar.json` 和 `phase3_timid.json`。
4. 第 4 阶段导入 `phase4_rich.json`、`phase4_broadcaster.json`、`phase4_transfer.json`。
5. 第 5 阶段导入 `phase5_teacher.json`，用于检查 7 个学生信物并获得老师信物。

## 信物物品

当前模组还不能给物品自定义名字，所以第一版先用原版物品代表信物：

- 班长信物：`minecraft:paper`
- 体育生信物：`minecraft:rabbit_foot`
- 学霸信物：`minecraft:glass_bottle`
- 胆小学生信物：`minecraft:ghast_tear`
- 富家学生信物：`minecraft:gold_nugget`
- 广播员信物：`minecraft:note_block`
- 转学生信物：`minecraft:echo_shard`
- 老师信物：`minecraft:red_dye`

## 玩法说明

这版是剧情原型，不是真正的实体死亡系统。所有“意外”先通过对话分支触发，成功后给右键玩家一个信物。后续阶段会检测主角背包中的信物，形成连锁路线。老师线会连续检查 7 个学生信物，全部集齐后给老师信物。
