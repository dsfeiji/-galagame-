# 学校循环 story_v2 JSON 包

## 目录

- `story_v2_dialogs`：正式剧情对话 JSON。当前版本为 5 阶段章节版，共 40 个对话文件。
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

## 使用方式

每个角色每个阶段都有单独 JSON。导入时按表格选择角色 ID 和阶段。每条学生线都可以从阶段 1 正常推进到阶段 5，也允许在后续阶段补救完成。

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
| `阶段5_雨哥_校门口放学.json` | `teacher_yuge` | 5 |
| `阶段5_BC_校门口放学.json` | `monitor_bc` | 5 |
| `阶段5_狗头_校门口放学.json` | `athlete_goutou` | 5 |
| `阶段5_六谷_校门口放学.json` | `scholar_liugu` | 5 |
| `阶段5_随小乐_校门口放学.json` | `timid_suixiaole` | 5 |
| `阶段5_孙悟空_校门口放学.json` | `rich_sunwukong` | 5 |
| `阶段5_赵子龙_校门口放学.json` | `broadcaster_zhaozilong` | 5 |
| `阶段5_王少栋_校门口放学.json` | `transfer_wangshaodong` | 5 |

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

## 信物

- BC：`first_mod:chipped_attendance_tag`
- 狗头：`first_mod:broken_wristband`
- 六谷：`first_mod:broken_exam_paper`
- 随小乐：`first_mod:crumpled_witness_note`
- 孙悟空：`first_mod:cracked_phone_charm`
- 赵子龙：`first_mod:stained_paintbrush`
- 王少栋：`first_mod:burnt_note`
- 雨哥：`minecraft:nether_star`
