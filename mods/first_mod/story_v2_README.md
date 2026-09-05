# 学校循环 story_v2 JSON 包

## 目录

- `story_v2_dialogs`：正式剧情对话 JSON。当前版本为 8 条完整人物线，不再使用 48 条阶段样板。
- `story_v2_minigames`：可独立绑定物品或被对话触发的小游戏 JSON。
- `story_v2_npc_guides/NPC角色书.md`：给 NPC 玩家看的背景、控场和行动说明。对话框内只保留玩家能听到的话。

## 阶段

先执行：

```mcfunction
/dialogphase setcount 5
```

五个阶段：

1. 校门与早自习
2. 教室与走廊
3. 操场与楼梯间
4. 午休柜子区与广播室
5. 校门口放学

每个阶段默认 5 点体力。体力耗尽时，当前对话结束后才进入下一阶段。

## 导入方式

每个完整线 JSON 都可以导入到对应角色的第 1-5 阶段。这样每个阶段都能继续单独完成该角色线，但文件数量不会膨胀成 48 个。

| 文件 | 角色ID | 建议导入阶段 |
| --- | --- | --- |
| `BC完整线.json` | `monitor_bc` | 1-5 |
| `狗头完整线.json` | `athlete_goutou` | 1-5 |
| `六谷完整线.json` | `scholar_liugu` | 1-5 |
| `随小乐完整线.json` | `timid_suixiaole` | 1-5 |
| `孙悟空完整线.json` | `rich_sunwukong` | 1-5 |
| `赵子龙完整线.json` | `broadcaster_zhaozilong` | 1-5 |
| `王少栋完整线.json` | `transfer_wangshaodong` | 1-5 |
| `雨哥隐藏结算线.json` | `teacher_yuge` | 1-5 |

## 角色 ID

- 主角传送点：`protagonist`
- BC：`monitor_bc`
- 狗头：`athlete_goutou`
- 六谷：`scholar_liugu`
- 随小乐：`timid_suixiaole`
- 孙悟空：`rich_sunwukong`
- 赵子龙：`broadcaster_zhaozilong`
- 王少栋：`transfer_wangshaodong`
- 雨哥：`teacher_yuge`

设置示例：

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

## 信物

- BC：`first_mod:chipped_attendance_tag`
- 狗头：`first_mod:broken_wristband`
- 六谷：`first_mod:broken_exam_paper`
- 随小乐：`first_mod:crumpled_witness_note`
- 孙悟空：`first_mod:cracked_phone_charm`
- 赵子龙：`first_mod:stained_paintbrush`
- 王少栋：`first_mod:burnt_note`
- 雨哥：`minecraft:nether_star`
