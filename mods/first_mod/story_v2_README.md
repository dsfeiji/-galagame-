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
| `phase1_teacher_yuge.json` | `teacher_yuge` | 1 |
| `phase1_monitor_bc.json` | `monitor_bc` | 1 |
| `phase1_athlete_goutou.json` | `athlete_goutou` | 1 |
| `phase1_scholar_liugu.json` | `scholar_liugu` | 1 |
| `phase1_timid_suixiaole.json` | `timid_suixiaole` | 1 |
| `phase1_rich_sunwukong.json` | `rich_sunwukong` | 1 |
| `phase1_broadcaster_zhaozilong.json` | `broadcaster_zhaozilong` | 1 |
| `phase1_transfer_wangshaodong.json` | `transfer_wangshaodong` | 1 |
| `phase2_monitor_bc.json` | `monitor_bc` | 2 |
| `phase2_athlete_goutou.json` | `athlete_goutou` | 2 |
| `phase3_scholar_liugu.json` | `scholar_liugu` | 3 |
| `phase3_timid_suixiaole.json` | `timid_suixiaole` | 3 |
| `phase4_rich_sunwukong.json` | `rich_sunwukong` | 4 |
| `phase4_broadcaster_zhaozilong.json` | `broadcaster_zhaozilong` | 4 |
| `phase4_transfer_wangshaodong.json` | `transfer_wangshaodong` | 4 |
| `phase5_teacher_yuge.json` | `teacher_yuge` | 5 |
| `phase6_teacher_yuge.json` | `teacher_yuge` | 6 |

## 信物

- BC：`first_mod:chipped_attendance_tag`
- 狗头：`first_mod:broken_wristband`
- 六谷：`first_mod:broken_exam_paper`
- 随小乐：`first_mod:crumpled_witness_note`
- 孙悟空：`first_mod:cracked_phone_charm`
- 赵子龙：`first_mod:stained_paintbrush`
- 王少栋：`first_mod:burnt_note`
- 雨哥：`minecraft:nether_star`，当前先用原版物品代表老师信物。
