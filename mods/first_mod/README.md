# First Mod

Minecraft 1.21 Fabric dialog mod for the script-killing map.

## Dialog JSON

Put dialog files in the game run directory:

```text
./first_mod_dialogs
```

Creative players can press `O` to open the role dialog control panel, choose a JSON file, then import it to a role and phase.

Choices can spend stamina:

```json
{
  "text": "Press for the hidden answer",
  "nextNodeId": "hidden_answer",
  "staminaCost": 1
}
```

Missing `staminaCost` means `0`.

## Roles

Players claim role ids before playing:

```mcfunction
/dialogrole claim detective
/dialogrole whoami
/dialogrole clear
```

Operators can set or inspect another player:

```mcfunction
/dialogrole set <player> <role_id>
/dialogrole get <player>
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

When the protagonist spends their last stamina point, the global phase advances immediately, players teleport by their claimed role for the new phase, and stamina resets to 5.

## Stamina

Operators can inspect or reset stamina:

```mcfunction
/dialogstamina info <player>
/dialogstamina reset <player>
```
