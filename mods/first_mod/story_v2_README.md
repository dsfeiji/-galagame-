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

## 对话导入对应表

| 文件 | 角色ID | 阶段 |
| --- | --- | --- |
| `阶段1_雨哥_校门开场.json` | `teacher_yuge` | 1 |
| `阶段1_BC_点名开场.json` | `monitor_bc` | 1 |
| `阶段1_狗头_操场热身.json` | `athlete_goutou` | 1 |
| `阶段1_六谷_知识问答开场.json` | `scholar_liugu` | 1 |
| `阶段1_随小乐_楼梯目击.json` | `timid_suixiaole` | 1 |
| `阶段1_孙悟空_柜子交易.json` | `rich_sunwukong` | 1 |
| `阶段1_赵子龙_广播安排.json` | `broadcaster_zhaozilong` | 1 |
| `阶段1_王少栋_转学生路线.json` | `transfer_wangshaodong` | 1 |
| `阶段2_BC_巡查记忆.json` | `monitor_bc` | 2 |
| `阶段2_狗头_器材室决胜.json` | `athlete_goutou` | 2 |
| `阶段3_六谷_知识问答.json` | `scholar_liugu` | 3 |
| `阶段3_随小乐_记忆证词.json` | `timid_suixiaole` | 3 |
| `阶段4_孙悟空_抢柜子.json` | `rich_sunwukong` | 4 |
| `阶段4_赵子龙_广播节奏.json` | `broadcaster_zhaozilong` | 4 |
| `阶段4_王少栋_空教室对抗.json` | `transfer_wangshaodong` | 4 |
| `阶段5_雨哥_最终检查.json` | `teacher_yuge` | 5 |
| `阶段6_雨哥_循环复盘.json` | `teacher_yuge` | 6 |

## NPC 扮演提示

- 对话 JSON 的 `text` 字段只放玩家能看到的说话内容。
- NPC 控场、信息边界和自由发挥规则放在 `story_v2_npc_guides/NPC角色书.md`。

## 信物

- BC：`first_mod:chipped_attendance_tag`
- 狗头：`first_mod:broken_wristband`
- 六谷：`first_mod:broken_exam_paper`
- 随小乐：`first_mod:crumpled_witness_note`
- 孙悟空：`first_mod:cracked_phone_charm`
- 赵子龙：`first_mod:stained_paintbrush`
- 王少栋：`first_mod:burnt_note`
- 雨哥：`minecraft:nether_star`，当前先用原版物品代表老师信物。
