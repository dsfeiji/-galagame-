# 学校循环 story_v2 JSON 包

## 目录

- `story_v2_dialogs`：对话 JSON。每个文件导入时选择对应角色 ID 和阶段。
- `story_v2_minigames`：右键物品/方块触发的小游戏 JSON。导入到小游戏面板。

## 阶段

先执行：

```mcfunction
/dialogphase setcount 6
```

阶段建议：

1. 校门口与早自习前
2. 教室与走廊
3. 操场与楼梯间
4. 午休柜子区与广播室
5. 办公室与最终检查
6. 循环重置与复盘

每阶段默认 5 点体力。体力耗尽后，当前对话结束才会切到下一阶段。

## 角色 ID

- 主角传送点：`protagonist`
- 班长 BC：`monitor_bc`
- 体育生 狗头：`athlete_goutou`
- 学霸 六谷：`scholar_liugu`
- 胆小鬼 随小乐：`timid_suixiaole`
- 富家子 孙悟空：`rich_sunwukong`
- 广播员 赵子龙：`broadcaster_zhaozilong`
- 转学生 王少栋：`transfer_wangshaodong`
- 老师 雨哥：`teacher_yuge`

设置玩家角色示例：

```mcfunction
/dialogrole set 玩家名 monitor_bc
/dialogrole set 玩家名 athlete_goutou
/dialogrole set 玩家名 scholar_liugu
/dialogrole set 玩家名 timid_suixiaole
/dialogrole set 玩家名 rich_sunwukong
/dialogrole set 玩家名 broadcaster_zhaozilong
/dialogrole set 玩家名 transfer_wangshaodong
/dialogrole set 玩家名 teacher_yuge
/dialogprotagonist set 玩家名
```

## 全阶段对话

当前版本已经改为 6 个阶段每个 NPC 都可对话，共 48 个对话 JSON。每条学生线在当前阶段都能继续推进或完成；特殊物品只作为捷径和交叉反应，不再是唯一入口。

## 对话导入对应表

| 文件 | 角色ID | 阶段 |
| --- | --- | --- |
| `阶段1_雨哥_校门与早自习.json` | `teacher_yuge` | 1 |
| `阶段1_BC_校门与早自习.json` | `monitor_bc` | 1 |
| `阶段1_狗头_校门与早自习.json` | `athlete_goutou` | 1 |
| `阶段1_六谷_校门与早自习.json` | `scholar_liugu` | 1 |
| `阶段1_随小乐_校门与早自习.json` | `timid_suixiaole` | 1 |
| `阶段1_孙悟空_校门与早自习.json` | `rich_sunwukong` | 1 |
| `阶段1_赵子龙_校门与早自习.json` | `broadcaster_zhaozilong` | 1 |
| `阶段1_王少栋_校门与早自习.json` | `transfer_wangshaodong` | 1 |
| `阶段2_雨哥_教室与走廊.json` | `teacher_yuge` | 2 |
| `阶段2_BC_教室与走廊.json` | `monitor_bc` | 2 |
| `阶段2_狗头_教室与走廊.json` | `athlete_goutou` | 2 |
| `阶段2_六谷_教室与走廊.json` | `scholar_liugu` | 2 |
| `阶段2_随小乐_教室与走廊.json` | `timid_suixiaole` | 2 |
| `阶段2_孙悟空_教室与走廊.json` | `rich_sunwukong` | 2 |
| `阶段2_赵子龙_教室与走廊.json` | `broadcaster_zhaozilong` | 2 |
| `阶段2_王少栋_教室与走廊.json` | `transfer_wangshaodong` | 2 |
| `阶段3_雨哥_操场与楼梯间.json` | `teacher_yuge` | 3 |
| `阶段3_BC_操场与楼梯间.json` | `monitor_bc` | 3 |
| `阶段3_狗头_操场与楼梯间.json` | `athlete_goutou` | 3 |
| `阶段3_六谷_操场与楼梯间.json` | `scholar_liugu` | 3 |
| `阶段3_随小乐_操场与楼梯间.json` | `timid_suixiaole` | 3 |
| `阶段3_孙悟空_操场与楼梯间.json` | `rich_sunwukong` | 3 |
| `阶段3_赵子龙_操场与楼梯间.json` | `broadcaster_zhaozilong` | 3 |
| `阶段3_王少栋_操场与楼梯间.json` | `transfer_wangshaodong` | 3 |
| `阶段4_雨哥_午休柜子区与广播室.json` | `teacher_yuge` | 4 |
| `阶段4_BC_午休柜子区与广播室.json` | `monitor_bc` | 4 |
| `阶段4_狗头_午休柜子区与广播室.json` | `athlete_goutou` | 4 |
| `阶段4_六谷_午休柜子区与广播室.json` | `scholar_liugu` | 4 |
| `阶段4_随小乐_午休柜子区与广播室.json` | `timid_suixiaole` | 4 |
| `阶段4_孙悟空_午休柜子区与广播室.json` | `rich_sunwukong` | 4 |
| `阶段4_赵子龙_午休柜子区与广播室.json` | `broadcaster_zhaozilong` | 4 |
| `阶段4_王少栋_午休柜子区与广播室.json` | `transfer_wangshaodong` | 4 |
| `阶段5_雨哥_办公室检查前.json` | `teacher_yuge` | 5 |
| `阶段5_BC_办公室检查前.json` | `monitor_bc` | 5 |
| `阶段5_狗头_办公室检查前.json` | `athlete_goutou` | 5 |
| `阶段5_六谷_办公室检查前.json` | `scholar_liugu` | 5 |
| `阶段5_随小乐_办公室检查前.json` | `timid_suixiaole` | 5 |
| `阶段5_孙悟空_办公室检查前.json` | `rich_sunwukong` | 5 |
| `阶段5_赵子龙_办公室检查前.json` | `broadcaster_zhaozilong` | 5 |
| `阶段5_王少栋_办公室检查前.json` | `transfer_wangshaodong` | 5 |
| `阶段6_雨哥_循环复盘.json` | `teacher_yuge` | 6 |
| `阶段6_BC_循环复盘.json` | `monitor_bc` | 6 |
| `阶段6_狗头_循环复盘.json` | `athlete_goutou` | 6 |
| `阶段6_六谷_循环复盘.json` | `scholar_liugu` | 6 |
| `阶段6_随小乐_循环复盘.json` | `timid_suixiaole` | 6 |
| `阶段6_孙悟空_循环复盘.json` | `rich_sunwukong` | 6 |
| `阶段6_赵子龙_循环复盘.json` | `broadcaster_zhaozilong` | 6 |
| `阶段6_王少栋_循环复盘.json` | `transfer_wangshaodong` | 6 |

## 信物

- BC：`first_mod:chipped_attendance_tag`
- 狗头：`first_mod:broken_wristband`
- 六谷：`first_mod:broken_exam_paper`
- 随小乐：`first_mod:crumpled_witness_note`
- 孙悟空：`first_mod:cracked_phone_charm`
- 赵子龙：`first_mod:stained_paintbrush`
- 王少栋：`first_mod:burnt_note`
- 雨哥：`minecraft:nether_star`，当前先用原版物品代表老师信物。
