# First Mod

Minecraft 1.21 Fabric dialog mod for the script-killing map.

Use only the release jar in Minecraft's `mods` folder:

```text
first_mod-1.0.0.jar
```

Do not put source jars or Gradle build folders into the game `mods` folder.

## Dialog JSON

Put dialog files in the game run directory:

```text
./first_mod_dialogs
```

Creative players can press `O` to open the role dialog control panel, choose a JSON file, then import it to a role and phase.

Imported dialog, phase, role teleport, minigame, room-lock, and elimination data is saved inside the current world save:

```text
<world>/first_mod/
```

This means a finished map can be moved to another computer and keep its configured roles, phase dialogs, teleports, minigames, and locks. Players only need to claim or be assigned one of the existing role ids again.

Old files from the global Fabric `config` directory are copied into the world folder once if the world folder does not already have its own data.

Choices can spend stamina:

```json
{
  "text": "Press for the hidden answer",
  "nextNodeId": "hidden_answer",
  "staminaCost": 1
}
```

Missing `staminaCost` means `0`.

## Minigame JSON

Put minigame interaction files in the game run directory:

```text
./first_mod_minigames
```

Creative players can press `O`, switch to `小游戏JSON`, choose a JSON file, then import it. Imported minigames are stored on the server and trigger when the configured block or item is right-clicked.

Block example:

```json
{
  "id": "roster_lectern_timing",
  "protagonistOnly": true,
  "staminaCost": 1,
  "trigger": {
    "type": "use_block",
    "block": "minecraft:lectern",
    "phase": 1
  },
  "minigame": {
    "type": "timing",
    "title": "偷看点名册",
    "difficulty": 2,
    "speed": 0.78,
    "successStart": 0.39,
    "successWidth": 0.22
  },
  "success": {
    "message": "你成功看到了旧名单。",
    "rewards": [
      {
        "item": "minecraft:paper",
        "count": 1
      }
    ]
  },
  "failure": {
    "message": "老师突然回头，你没能看清点名册。"
  }
}
```

Optional exact block position:

```json
"world": "minecraft:overworld",
"x": 12,
"y": 64,
"z": -8
```

If position is omitted, every matching block in the configured phase can trigger the minigame.

Item example:

```json
{
  "id": "paper_note_timing",
  "protagonistOnly": true,
  "staminaCost": 1,
  "trigger": {
    "type": "use_item",
    "item": "minecraft:paper",
    "phase": 1
  },
  "minigame": {
    "type": "timing",
    "title": "辨认纸条暗号",
    "difficulty": 2,
    "speed": 0.9,
    "successStart": 0.42,
    "successWidth": 0.18
  },
  "success": {
    "message": "你看懂了纸条上的暗号。",
    "rewards": [
      {
        "item": "minecraft:echo_shard",
        "count": 1
      }
    ]
  },
  "failure": {
    "message": "你把纸条顺序看反了。"
  }
}
```

Dialog nodes can also include a minigame directly:

```json
"minigame": {
  "type": "timing",
  "title": "偷听老师讲话",
  "difficulty": 3,
  "speed": 1.0,
  "successStart": 0.44,
  "successWidth": 0.14,
  "successNodeId": "success",
  "failureNodeId": "failure"
}
```

`speed`, `successStart`, and `successWidth` are optional. If omitted, the mod uses the default values for the selected `difficulty`.

Arm wrestle minigame:

```json
"minigame": {
  "type": "arm_wrestle",
  "title": "扳手腕",
  "durationTicks": 100,
  "opponentAutoClicksPerSecond": 5.5,
  "pushPerClick": 0.08,
  "winProgress": 1.0,
  "winClickLead": 1,
  "successNodeId": "win",
  "failureNodeId": "lose"
}
```

In dialog nodes, arm wrestle is the right-clicking player versus the clicked player. Both players click or press space on the same shared progress bar. The right-clicking player pushes left; the clicked player pushes right. Whoever pushes the bar to their end wins. The right-clicking player's win or loss decides the dialog branch.

In block or item interactions, arm wrestle is the player versus an automatic opponent controlled by `opponentAutoClicksPerSecond`.

Pure UI duel minigames:

```json
"minigame": {
  "type": "locker_search_duel",
  "title": "抢柜子",
  "durationTicks": 120,
  "gridSize": 9,
  "targetIndex": 4,
  "opponentAccuracy": 0.5,
  "winClickLead": 1,
  "successNodeId": "win",
  "failureNodeId": "lose"
}
```

Supported pure UI duel types:

- `locker_search_duel`: both players flip grid cells; finding the target gives points.
- `rhythm_duel`: click when the marker reaches the green center zone.
- `memory_flip_duel`: remember highlighted cells, then flip them after the preview ends.

In dialog nodes, both players can participate and send scores. The right-clicking player's score decides the branch. In block or item interactions, the player competes with AI, controlled by `opponentAccuracy`.

## Elimination JSON

Dialog nodes can eliminate a role when the node is reached. The eliminated player is switched to spectator mode and sees a black fade screen with the configured reason.

Eliminate the player who was right-clicked:

```json
{
  "id": "bc_exit",
  "text": "这段对话结束后，当前被右键的角色退场。",
  "nextNodeId": "",
  "eliminateTarget": true,
  "eliminateReason": "班长疯了之后跑走了"
}
```

Eliminate a specific role id:

```json
{
  "id": "specific_exit",
  "text": "这段对话结束后，指定角色退场。",
  "nextNodeId": "",
  "eliminateRole": "bc",
  "eliminateReason": "BC 意识到时间线已经无法挽回，冲出教室后失踪了。"
}
```

Minigame interaction results can also eliminate a role:

```json
"success": {
  "message": "小游戏成功。",
  "eliminateRole": "bc",
  "eliminateReason": "BC 离开了这次循环。"
}
```

Or eliminate the player who triggered the item/block minigame:

```json
"failure": {
  "message": "小游戏失败。",
  "eliminateSelf": true,
  "eliminateReason": "你被迫退出了当前行动线。"
}
```

Operator test commands:

```mcfunction
/dialogeliminate role <role_id> <reason>
/dialogeliminate revive <role_id>
/dialogeliminate reset
/dialogeliminate info
```

## Roles

Players claim role ids before playing:

```mcfunction
/dialogrole claim detective
/dialogrole whoami
/dialogrole clear
```

Operators can assign or inspect another player:

```mcfunction
/dialogrole claim <player> <role_id>
/dialogrole set <player> <role_id>
/dialogrole get <player>
```

Recommended test NPC format:

```mcfunction
/dialogrole claim npc1 role1
```

## Protagonist

Only the protagonist can advance the whole story phase when stamina reaches 0.

```mcfunction
/dialogprotagonist set <player>
/dialogprotagonist get
/dialogprotagonist clear
```

Other players can spend stamina for dialog choices, but stamina reaching 0 will not change the phase.

The protagonist has a dedicated teleport role id:

```text
protagonist
```

This id is only used for phase teleports. It does not replace the player's claimed dialog role.

## Phases And Teleports

The phase is global for all players:

```mcfunction
/dialogphase setcount <count>
/dialogphase next
/dialogphase set <phase>
/dialogphase info
```

Each phase can have a different teleport point for each role. Stand at the target position and run:

```mcfunction
/dialogphase settp <phase> <role_id>
```

Use `default` as a fallback point:

```mcfunction
/dialogphase settp 2 default
```

Set a protagonist-only teleport point:

```mcfunction
/dialogphase settp 2 protagonist
```

Other teleport commands:

```mcfunction
/dialogphase tptest <phase> <role_id>
/dialogphase cleartp <phase> <role_id>
/dialogphase tpinfo <phase>
```

When the protagonist spends their last stamina point, the global phase advances immediately, players briefly see a black screen with no text, players teleport by their claimed role for the new phase, and stamina resets to 5.

## Stamina

Operators can inspect or reset stamina:

```mcfunction
/dialogstamina info <player>
/dialogstamina reset <player>
```
