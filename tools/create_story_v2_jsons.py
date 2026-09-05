import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DIALOG_DIR = ROOT / "mods" / "first_mod" / "story_v2_dialogs"
MINIGAME_DIR = ROOT / "mods" / "first_mod" / "story_v2_minigames"


TOKENS = {
    "monitor": "first_mod:chipped_attendance_tag",
    "athlete": "first_mod:broken_wristband",
    "scholar": "first_mod:broken_exam_paper",
    "timid": "first_mod:crumpled_witness_note",
    "rich": "first_mod:cracked_phone_charm",
    "broadcaster": "first_mod:stained_paintbrush",
    "transfer": "first_mod:burnt_note",
    "teacher": "minecraft:nether_star",
}


def node(node_id, text, next_id="", choices=None, rewards=None, condition_jumps=None, minigame=None):
    data = {
        "id": node_id,
        "text": text,
        "nextNodeId": next_id,
        "conditionJumps": condition_jumps or [],
        "rewards": rewards or [],
        "choices": choices or [],
    }
    if minigame:
        data["minigame"] = minigame
    return data


def choice(text, next_id, stamina=0):
    data = {"text": text, "nextNodeId": next_id}
    if stamina:
        data["staminaCost"] = stamina
    return data


def reward(item, count=1):
    return {"item": item, "count": count}


def jump(item, next_id, count=1):
    return {"item": item, "count": count, "nextNodeId": next_id}


def tree(nodes, start="start"):
    return {"startNodeId": start, "nodes": nodes}


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def dialog_files():
    return {
        "阶段1_雨哥_校门开场.json": tree([
            node("start", "【场景提示】校门口。雨哥负责开场控场：让所有人知道今天按学校日程推进，主角可以自由找人接触。不要直接透露通关路线。",
                 choices=[
                     choice("询问今天的日程", "schedule"),
                     choice("要求提前进教室观察", "early_classroom", 1),
                     choice("结束对话", ""),
                 ]),
            node("schedule", "【NPC发挥】雨哥可以说明：先早自习，再课间、操场、午休、办公室检查，最后集合。只说日程，不说谁会出事。", ""),
            node("early_classroom", "【机制】主角用 1 点体力提前进教室，适合先找 BC 或六谷。雨哥可以口头提醒不要乱碰东西。", ""),
        ]),
        "阶段1_BC_点名开场.json": tree([
            node("start", "【场景提示】BC 在教室门口整理点名。BC 可以维持秩序、盘问主角迟到原因，但不能直接给信物。",
                 choices=[
                     choice("帮 BC 整理点名册", "help_roster", 1),
                     choice("追问今天谁最反常", "ask_unusual"),
                     choice("离开", ""),
                 ]),
            node("help_roster", "【线索】主角帮忙后，BC 可以透露：王少栋的名字刚加进名单，随小乐一直在躲视线。", ""),
            node("ask_unusual", "【发挥边界】BC 可以怀疑所有人，但重点应引向王少栋、随小乐和广播室，不要直接点名凶手。", ""),
        ]),
        "阶段1_狗头_操场热身.json": tree([
            node("start", "【场景提示】狗头在操场入口热身。狗头不爱长谈，倾向用比赛和挑衅推进关系。",
                 choices=[
                     choice("接受狗头的力量挑衅", "arm_game", 1),
                     choice("问体育器材室情况", "equipment"),
                     choice("离开", ""),
                 ]),
            node("arm_game", "【小游戏】扳手腕。双方同步点击，进度条推到对方方向为胜。", minigame={
                "type": "arm_wrestle",
                "title": "扳手腕：狗头",
                "pushPerClick": 0.055,
                "winProgress": 1.0,
                "successNodeId": "arm_success",
                "failureNodeId": "arm_failure",
            }),
            node("arm_success", "【结果】主角赢了。狗头认可主角，可以透露器材室今天被人借用过。", rewards=[reward("minecraft:leather")]),
            node("arm_failure", "【结果】主角输了。狗头只会嘲笑或敷衍，本轮仍可从别的路线补线索。"),
            node("equipment", "【线索】狗头可以说器材室的绳子、球网或护腕少过，但不要描述无法验证的气味。", ""),
        ]),
        "阶段1_六谷_知识问答开场.json": tree([
            node("start", "【场景提示】六谷在座位上整理资料。学霸线用普通对话模拟知识问答，题目由作者后续替换。",
                 choices=[
                     choice("请六谷出第一题", "q1", 1),
                     choice("询问考试资料", "materials"),
                     choice("离开", ""),
                 ]),
            node("q1", "【知识问答占位】这里替换成作者设计的第一题。正确选项继续，错误选项锁线或浪费体力。",
                 choices=[
                     choice("正确答案占位", "q2"),
                     choice("错误答案占位", "fail"),
                 ]),
            node("q2", "【知识问答占位】这里替换成作者设计的第二题。",
                 choices=[
                     choice("正确答案占位", "quiz_success"),
                     choice("错误答案占位", "fail"),
                 ]),
            node("quiz_success", "【结果】六谷确认主角能跟上逻辑，允许主角查看一份资料。", rewards=[reward("minecraft:book")]),
            node("fail", "【失败】六谷认为主角只是在套话，本轮学霸线暂时难推进。"),
            node("materials", "【线索】六谷可以透露：有一份满分试卷被损坏，BC 和孙悟空都关注过它。", ""),
        ]),
        "阶段1_随小乐_楼梯目击.json": tree([
            node("start", "【场景提示】随小乐在楼梯附近徘徊。她看见过一些路线，但害怕被卷进去。",
                 choices=[
                     choice("温和安抚随小乐", "comfort"),
                     choice("逼问她看见了谁", "pressure", 1),
                     choice("离开", ""),
                 ]),
            node("comfort", "【线索】随小乐可以模糊提示：广播响起前，有人从空教室方向出来。"),
            node("pressure", "【后果】逼问会让随小乐更慌，但可以让她说出“柜子区”这个地点。", rewards=[reward("minecraft:string")]),
        ]),
        "阶段1_孙悟空_柜子交易.json": tree([
            node("start", "【场景提示】孙悟空在柜子区附近摆弄物品。他习惯谈交换，不轻易白给信息。",
                 choices=[
                     choice("谈一笔交换", "deal", 1),
                     choice("问柜子区发生了什么", "locker"),
                     choice("离开", ""),
                 ]),
            node("deal", "【机制】主角花体力换取一次资源型帮助。孙悟空可以要求主角以后帮他保住面子。", rewards=[reward("minecraft:gold_nugget")]),
            node("locker", "【线索】孙悟空可以透露：王少栋对某个柜子很在意，赵子龙也来过。"),
        ]),
        "阶段1_赵子龙_广播安排.json": tree([
            node("start", "【场景提示】赵子龙在广播室门口调试。广播员能制造全校信息差。",
                 choices=[
                     choice("请他解释广播安排", "broadcast_plan"),
                     choice("挑战广播节奏", "rhythm_game", 1),
                     choice("离开", ""),
                 ]),
            node("broadcast_plan", "【线索】赵子龙可以说明广播会影响集合时间，主角可利用广播制造错位。"),
            node("rhythm_game", "【小游戏】节奏对抗。点击后立刻刷新下一条，目标位置和宽度随机，一分钟结算。", minigame={
                "type": "rhythm_duel",
                "title": "节奏对抗：赵子龙",
                "durationTicks": 1200,
                "rounds": 60,
                "winClickLead": 1,
                "successNodeId": "rhythm_success",
                "failureNodeId": "rhythm_failure",
            }),
            node("rhythm_success", "【结果】赵子龙愿意在关键时刻插播一次提示。", rewards=[reward("minecraft:note_block")]),
            node("rhythm_failure", "【结果】赵子龙觉得主角跟不上节奏，只给普通日程信息。"),
        ]),
        "阶段1_王少栋_转学生路线.json": tree([
            node("start", "【场景提示】王少栋刚到学校，对很多地方不熟，但知道一个不该知道的地点。",
                 choices=[
                     choice("问他为什么盯着柜子区", "ask_locker"),
                     choice("提出一起找路线", "route", 1),
                     choice("离开", ""),
                 ]),
            node("ask_locker", "【线索】王少栋可以说自己只是找错柜子，但反应要显得谨慎。"),
            node("route", "【线索】王少栋愿意指出空教室方向，但不解释为什么知道。", rewards=[reward("minecraft:map")]),
        ]),

        "阶段2_BC_巡查记忆.json": tree([
            node("start", "【场景提示】课间走廊。BC 开始巡查，主角可以尝试拿到班长线核心信物。",
                 condition_jumps=[jump("minecraft:map", "has_route")],
                 choices=[
                     choice("挑战 BC 的巡查记忆", "memory_game", 1),
                     choice("请求她放松巡查", "soft_request"),
                     choice("离开", ""),
                 ]),
            node("has_route", "【背包检测】主角带着王少栋给的路线图，BC 意识到主角知道空教室动线。可直接进入记忆对抗。",
                 choices=[choice("开始记忆翻牌", "memory_game", 1), choice("离开", "")]),
            node("memory_game", "【小游戏】记忆翻牌：3x5 的 15 格中找 6 个发光格，点错直接失败并只扣一次分。", minigame={
                "type": "memory_flip_duel",
                "title": "记忆翻牌：BC",
                "durationTicks": 1200,
                "previewTicks": 50,
                "rounds": 6,
                "successNodeId": "token",
                "failureNodeId": "locked",
            }),
            node("token", "【结果】BC 的巡查被主角突破。发放班长信物。", rewards=[reward(TOKENS["monitor"])]),
            node("locked", "【失败】BC 加强巡查，本轮班长线需要换路线或下次循环再来。"),
            node("soft_request", "【发挥】BC 可以拒绝，也可以给一个轻微信息：午休前柜子区会短暂无监管。"),
        ]),
        "阶段2_狗头_器材室决胜.json": tree([
            node("start", "【场景提示】器材室附近。狗头线进入事故准备阶段。",
                 condition_jumps=[jump("minecraft:leather", "trusted")],
                 choices=[choice("再次扳手腕争取信任", "arm_game", 1), choice("观察器材室", "observe"), choice("离开", "")]),
            node("trusted", "【背包检测】主角带着狗头认可过的物品，可直接要求他配合一次器材安排。",
                 choices=[choice("安排器材意外", "token", 1), choice("离开", "")]),
            node("arm_game", "【小游戏】扳手腕，没有倒计时，到头结束。", minigame={
                "type": "arm_wrestle",
                "title": "扳手腕：狗头决胜",
                "pushPerClick": 0.06,
                "winProgress": 1.0,
                "successNodeId": "token",
                "failureNodeId": "observe",
            }),
            node("token", "【结果】狗头线完成。发放体育生信物。", rewards=[reward(TOKENS["athlete"])]),
            node("observe", "【线索】主角只能确认器材室可作为后续事故地点，但暂时拿不到信物。"),
        ]),

        "阶段3_六谷_知识问答.json": tree([
            node("start", "【场景提示】早自习后。六谷开始用题目考主角，作者可替换题面和选项。",
                 condition_jumps=[jump("minecraft:book", "prepared")],
                 choices=[choice("进入知识问答", "q1", 1), choice("离开", "")]),
            node("prepared", "【背包检测】主角带着资料书，六谷降低门槛，直接进入最后一问。",
                 choices=[choice("回答最后一问", "q2", 1), choice("离开", "")]),
            node("q1", "【题目占位】六谷提出第一道题。把这里替换成真实题目。",
                 choices=[choice("正确答案占位", "q2"), choice("错误答案占位", "fail")]),
            node("q2", "【题目占位】六谷提出关键题。把这里替换成真实题目。",
                 choices=[choice("正确答案占位", "token"), choice("错误答案占位", "fail")]),
            node("token", "【结果】学霸线完成。发放学霸信物。", rewards=[reward(TOKENS["scholar"])]),
            node("fail", "【失败】六谷不再提供关键资料，本轮学霸线锁定。"),
        ]),
        "阶段3_随小乐_记忆证词.json": tree([
            node("start", "【场景提示】楼梯间。随小乐需要确认自己记忆里的路线。",
                 condition_jumps=[jump("minecraft:string", "pressured")],
                 choices=[choice("陪她回忆路线", "memory_game", 1), choice("离开", "")]),
            node("pressured", "【背包检测】主角之前逼问过她，随小乐更紧张；继续会更快拿到线索，但她会明显抗拒。",
                 choices=[choice("继续追问", "memory_game", 1), choice("放她离开", "")]),
            node("memory_game", "【小游戏】记忆翻牌：15 格找 6 个发光格。", minigame={
                "type": "memory_flip_duel",
                "title": "记忆翻牌：随小乐",
                "durationTicks": 1200,
                "previewTicks": 45,
                "rounds": 6,
                "successNodeId": "token",
                "failureNodeId": "fail",
            }),
            node("token", "【结果】随小乐线完成。发放胆小鬼信物。", rewards=[reward(TOKENS["timid"])]),
            node("fail", "【失败】随小乐情绪崩溃，本轮无法继续提供稳定证词。"),
        ]),

        "阶段4_孙悟空_抢柜子.json": tree([
            node("start", "【场景提示】午休柜子区。孙悟空线需要在资源交换和抢柜子中完成。",
                 condition_jumps=[jump("minecraft:gold_nugget", "has_money")],
                 choices=[choice("直接抢柜子", "locker_game", 1), choice("继续谈条件", "deal"), choice("离开", "")]),
            node("has_money", "【背包检测】主角带着金粒，孙悟空愿意把冲突升级到柜子对抗。",
                 choices=[choice("开始抢柜子", "locker_game", 1), choice("离开", "")]),
            node("locker_game", "【小游戏】抢柜子：点对加分，点错不扣分，目标每次变化，一分钟结算。", minigame={
                "type": "locker_search_duel",
                "title": "抢柜子：孙悟空",
                "durationTicks": 1200,
                "rounds": 60,
                "winClickLead": 1,
                "successNodeId": "token",
                "failureNodeId": "fail",
            }),
            node("token", "【结果】富家子线完成。发放富家子信物。", rewards=[reward(TOKENS["rich"])]),
            node("fail", "【失败】孙悟空收回交易条件，本轮只能从其他人处补线索。"),
            node("deal", "【发挥】孙悟空可以开价，但只能给可保留道具或线索，不能直接发信物。", rewards=[reward("minecraft:emerald")]),
        ]),
        "阶段4_赵子龙_广播节奏.json": tree([
            node("start", "【场景提示】广播室。赵子龙线需要主角争夺广播节奏。",
                 condition_jumps=[jump("minecraft:note_block", "trusted")],
                 choices=[choice("节奏对抗", "rhythm_game", 1), choice("请求一次广播帮助", "request"), choice("离开", "")]),
            node("trusted", "【背包检测】主角之前赢过赵子龙的信任，可以直接请求关键广播，但仍需消耗体力。",
                 choices=[choice("触发关键广播", "token", 1), choice("离开", "")]),
            node("rhythm_game", "【小游戏】节奏对抗，一分钟，点击后立即刷新下一条。", minigame={
                "type": "rhythm_duel",
                "title": "节奏对抗：赵子龙决胜",
                "durationTicks": 1200,
                "rounds": 60,
                "winClickLead": 1,
                "successNodeId": "token",
                "failureNodeId": "fail",
            }),
            node("token", "【结果】广播员线完成。发放广播员信物。", rewards=[reward(TOKENS["broadcaster"])]),
            node("fail", "【失败】广播时机错过，本轮无法通过广播制造关键错位。"),
            node("request", "【发挥】赵子龙可以给一次普通广播提示，但不能替主角完成路线。"),
        ]),
        "阶段4_王少栋_空教室对抗.json": tree([
            node("start", "【场景提示】空教室。王少栋线要揭开他为什么熟悉学校隐藏路线。",
                 condition_jumps=[jump("minecraft:map", "route_known")],
                 choices=[choice("抢先翻找他的柜子线索", "locker_game", 1), choice("正面谈判", "talk"), choice("离开", "")]),
            node("route_known", "【背包检测】主角带着路线图，王少栋知道自己瞒不住，进入对抗。", choices=[choice("开始柜子对抗", "locker_game", 1), choice("离开", "")]),
            node("locker_game", "【小游戏】抢柜子：目标持续变化，一分钟结算。", minigame={
                "type": "locker_search_duel",
                "title": "抢柜子：王少栋",
                "durationTicks": 1200,
                "rounds": 60,
                "winClickLead": 1,
                "successNodeId": "token",
                "failureNodeId": "fail",
            }),
            node("token", "【结果】转学生线完成。发放转学生信物。", rewards=[reward(TOKENS["transfer"])]),
            node("fail", "【失败】王少栋转移了关键物品，本轮转学生线锁定。"),
            node("talk", "【发挥】王少栋可以承认自己知道空教室，但不解释最终原因。"),
        ]),

        "阶段5_雨哥_最终检查.json": tree([
            node("start", "【场景提示】办公室/最终集合前。雨哥检查主角是否在本轮集齐七个学生信物。",
                 next_id="check_monitor"),
            node("check_monitor", "【检测】检查 BC 信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["monitor"], "check_athlete")]),
            node("check_athlete", "【检测】检查狗头信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["athlete"], "check_scholar")]),
            node("check_scholar", "【检测】检查六谷信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["scholar"], "check_timid")]),
            node("check_timid", "【检测】检查随小乐信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["timid"], "check_rich")]),
            node("check_rich", "【检测】检查孙悟空信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["rich"], "check_broadcaster")]),
            node("check_broadcaster", "【检测】检查赵子龙信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["broadcaster"], "check_transfer")]),
            node("check_transfer", "【检测】检查王少栋信物。", next_id="missing",
                 condition_jumps=[jump(TOKENS["transfer"], "complete")]),
            node("complete", "【结算】七个学生信物齐全。雨哥进入最终控场场景，发放老师信物。", rewards=[reward(TOKENS["teacher"])]),
            node("missing", "【结算】信物不齐。雨哥只给普通结局提示，主角需要下一轮重新规划体力和顺序。"),
        ]),

        "阶段6_雨哥_循环复盘.json": tree([
            node("start", "【场景提示】循环重置点。雨哥宣布本轮结果，主角可准备进入下一次循环。此阶段用于复盘，不发学生信物。",
                 choices=[choice("结束本轮", ""), choice("询问下一轮建议", "hint")]),
            node("hint", "【提示】雨哥可以提示：每阶段只有 5 点体力，全收集需要提前准备可保留道具，信物本轮有效。"),
        ]),
    }


def minigame_files():
    return {
        "阶段2_方块_讲台点名册记忆.json": {
            "id": "phase2_roster_lectern_memory",
            "protagonistOnly": True,
            "staminaCost": 1,
            "trigger": {"type": "use_block", "block": "minecraft:lectern", "phase": 2},
            "minigame": {
                "type": "memory_flip_duel",
                "title": "点名册记忆",
                "durationTicks": 1200,
                "previewTicks": 50,
                "rounds": 6,
            },
            "success": {"message": "你记住了点名册的异常顺序。", "rewards": [reward("minecraft:paper")]},
            "failure": {"message": "你记错了顺序，本轮这条线索价值降低。"},
        },
        "阶段3_方块_器材室按钮扳手腕.json": {
            "id": "phase3_gym_button_arm_wrestle",
            "protagonistOnly": True,
            "staminaCost": 1,
            "trigger": {"type": "use_block", "block": "minecraft:stone_button", "phase": 3},
            "minigame": {
                "type": "arm_wrestle",
                "title": "器材室力量对抗",
                "pushPerClick": 0.06,
                "winProgress": 1.0,
            },
            "success": {"message": "你压过了对手，拿到器材室临时主动权。", "rewards": [reward("minecraft:leather")]},
            "failure": {"message": "你没有压住局面，对手暂时占据主动。"},
        },
        "阶段4_方块_箱子抢柜子.json": {
            "id": "phase4_locker_chest_search",
            "protagonistOnly": True,
            "staminaCost": 1,
            "trigger": {"type": "use_block", "block": "minecraft:chest", "phase": 4},
            "minigame": {
                "type": "locker_search_duel",
                "title": "抢柜子",
                "durationTicks": 1200,
                "rounds": 60,
                "winClickLead": 1,
            },
            "success": {"message": "你抢先翻到了柜子里的关键线索。", "rewards": [reward("minecraft:map")]},
            "failure": {"message": "对方抢先转移了柜子里的东西。"},
        },
        "阶段4_方块_音符盒广播节奏.json": {
            "id": "phase4_note_block_rhythm",
            "protagonistOnly": True,
            "staminaCost": 1,
            "trigger": {"type": "use_block", "block": "minecraft:note_block", "phase": 4},
            "minigame": {
                "type": "rhythm_duel",
                "title": "广播节奏对抗",
                "durationTicks": 1200,
                "rounds": 60,
                "winClickLead": 1,
            },
            "success": {"message": "你抢到了广播节奏，可以制造一次全校错位。", "rewards": [reward("minecraft:note_block")]},
            "failure": {"message": "广播节奏被对方控制，错位机会消失。"},
        },
        "阶段5_物品_纸张复盘记忆.json": {
            "id": "phase5_paper_review_memory",
            "protagonistOnly": True,
            "staminaCost": 1,
            "trigger": {"type": "use_item", "item": "minecraft:paper", "phase": 5},
            "minigame": {
                "type": "memory_flip_duel",
                "title": "复盘线索记忆",
                "durationTicks": 1200,
                "previewTicks": 50,
                "rounds": 6,
            },
            "success": {"message": "你复盘出了正确顺序。", "rewards": [reward("minecraft:book")]},
            "failure": {"message": "你复盘顺序出错，本轮只能普通结算。"},
        },
    }


def readme():
    return """# 学校循环 story_v2 JSON 包

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

## 信物

- BC：`first_mod:chipped_attendance_tag`
- 狗头：`first_mod:broken_wristband`
- 六谷：`first_mod:broken_exam_paper`
- 随小乐：`first_mod:crumpled_witness_note`
- 孙悟空：`first_mod:cracked_phone_charm`
- 赵子龙：`first_mod:stained_paintbrush`
- 王少栋：`first_mod:burnt_note`
- 雨哥：`minecraft:nether_star`，当前先用原版物品代表老师信物。
"""


def main():
    DIALOG_DIR.mkdir(parents=True, exist_ok=True)
    MINIGAME_DIR.mkdir(parents=True, exist_ok=True)
    for path in DIALOG_DIR.glob("*.json"):
        path.unlink()
    for path in MINIGAME_DIR.glob("*.json"):
        path.unlink()

    for name, data in dialog_files().items():
        write_json(DIALOG_DIR / name, data)
    for name, data in minigame_files().items():
        write_json(MINIGAME_DIR / name, data)
    (ROOT / "mods" / "first_mod" / "story_v2_README.md").write_text(readme(), encoding="utf-8")

    from clean_story_v2_player_dialogs import clean_dialogs, update_readme
    from create_story_v2_npc_books import write_npc_book

    clean_dialogs()
    write_npc_book()
    update_readme()

    print(f"dialogs={len(list(DIALOG_DIR.glob('*.json')))}")
    print(f"minigames={len(list(MINIGAME_DIR.glob('*.json')))}")


if __name__ == "__main__":
    main()
