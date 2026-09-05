import json
import shutil
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MOD_DIR = ROOT / "mods" / "first_mod"
DIALOG_DIR = MOD_DIR / "story_v2_dialogs"
MINIGAME_DIR = MOD_DIR / "story_v2_minigames"
GUIDE_DIR = MOD_DIR / "story_v2_npc_guides"
README = MOD_DIR / "story_v2_README.md"
ZIP_PATH = MOD_DIR / "story_v2_json_pack.zip"


PHASES = [
    "校门与早自习",
    "教室与走廊",
    "操场与楼梯间",
    "午休柜子区与广播室",
    "校门口放学",
]

TOKENS = {
    "monitor_bc": "first_mod:chipped_attendance_tag",
    "athlete_goutou": "first_mod:broken_wristband",
    "scholar_liugu": "first_mod:broken_exam_paper",
    "timid_suixiaole": "first_mod:crumpled_witness_note",
    "rich_sunwukong": "first_mod:cracked_phone_charm",
    "broadcaster_zhaozilong": "first_mod:stained_paintbrush",
    "transfer_wangshaodong": "first_mod:burnt_note",
    "teacher_yuge": "minecraft:nether_star",
}

ROLE_NAMES = {
    "monitor_bc": "BC",
    "athlete_goutou": "狗头",
    "scholar_liugu": "六谷",
    "timid_suixiaole": "随小乐",
    "rich_sunwukong": "孙悟空",
    "broadcaster_zhaozilong": "赵子龙",
    "transfer_wangshaodong": "王少栋",
    "teacher_yuge": "雨哥",
}


def node(node_id, text, next_id="", choices=None, rewards=None, jumps=None, minigame=None):
    result = {
        "id": node_id,
        "text": text,
        "nextNodeId": next_id,
        "conditionJumps": jumps or [],
        "rewards": rewards or [],
        "choices": choices or [],
    }
    if minigame:
        result["minigame"] = minigame
    return result


def choice(text, next_id, stamina=0):
    result = {"text": text, "nextNodeId": next_id}
    if stamina:
        result["staminaCost"] = stamina
    return result


def reward(item, count=1):
    return {"item": item, "count": count}


def jump(item, next_id):
    return {"item": item, "count": 1, "nextNodeId": next_id}


def tree(nodes):
    return {"startNodeId": "start", "nodes": nodes}


def minigame(kind, title, success, failure):
    base = {
        "type": kind,
        "title": title,
        "durationTicks": 1200,
        "successNodeId": success,
        "failureNodeId": failure,
    }
    if kind == "arm_wrestle":
        base.update({"pushPerClick": 0.06, "winProgress": 1.0})
    elif kind == "memory_flip_duel":
        base.update({"previewTicks": 50, "rounds": 6, "columns": 5, "targetCount": 6})
    elif kind == "locker_search_duel":
        base.update({"rounds": 60, "winClickLead": 1})
    elif kind == "rhythm_duel":
        base.update({"rounds": 60, "winClickLead": 1})
    return base


def monitor_bc():
    return tree([
        node("start", "BC：你又迟到了？名单上每个人的位置我都记着。你想问事可以，但别挡着我点名。",
             jumps=[jump("minecraft:map", "has_route_map"), jump(TOKENS["transfer_wangshaodong"], "has_transfer_token")],
             choices=[
                 choice("帮你核对名单。", "help_roster", 1),
                 choice("我想知道谁的名字不对。", "ask_name"),
                 choice("放学前我再来找你。", "after_school"),
                 choice("离开", ""),
             ]),
        node("has_route_map", "BC：路线图？王少栋给你的？他刚来，却比班里很多人还清楚空教室在哪。你最好别只盯着他，也看看谁在替他改记录。",
             choices=[choice("继续查点名册。", "help_roster", 1), choice("追问改记录的人。", "ask_name")]),
        node("has_transfer_token", "BC：你已经拿到王少栋那边的东西了？那我没必要继续装不知道。点名册确实被动过，动的人不是老师。",
             choices=[choice("让我看看原来的名单。", "memory_intro", 1), choice("先离开", "")]),
        node("help_roster", "BC：行，你念名字，我划勾。等等，随小乐这一栏被擦过，王少栋这一栏是后来补的。你看见了吗？",
             choices=[choice("看见了，继续往下核。", "memory_intro", 1), choice("这是谁补的？", "ask_name")]),
        node("ask_name", "BC：我不能直接说。你要是能记住我刚才划过的顺序，我就把缺角的点名牌给你。记错了，我就当你只是在捣乱。",
             choices=[choice("开始记忆翻牌。", "memory_intro", 1), choice("我先去找随小乐。", "hint_suixiaole")]),
        node("hint_suixiaole", "BC：随小乐怕事，但她不会乱说。别逼她，逼急了她只会躲起来。"),
        node("memory_intro", "BC：十五个格子里有六个是我刚才点过的人。你只要把那六个找出来，我就承认你能帮上忙。",
             minigame=minigame("memory_flip_duel", "记忆翻牌：BC", "token", "fail")),
        node("token", "BC：好，你确实记住了。这个缺角点名牌你拿着。它只能证明我这条线，不会替你解决其他人的麻烦。",
             rewards=[reward(TOKENS["monitor_bc"])]),
        node("fail", "BC：错了。你现在问我什么，我都会按普通迟到处理。想单独做我这条线，下一轮从点名开始重来。"),
        node("after_school", "BC：放学门口我还会清一次名单。那时候如果你什么都没查到，我也只能把你当成普通路过的人。"),
    ])


def athlete_goutou():
    return tree([
        node("start", "狗头：找我？先说好，别绕弯。操场的事用嘴说没用，谁手稳谁说了算。",
             jumps=[jump("minecraft:leather", "has_grip"), jump(TOKENS["broadcaster_zhaozilong"], "has_broadcast_token")],
             choices=[
                 choice("扳手腕，赢了你让路。", "arm_intro", 1),
                 choice("我想知道器材室谁动过。", "equipment"),
                 choice("放学时你会去哪里？", "after_school"),
                 choice("离开", ""),
             ]),
        node("has_grip", "狗头：你还留着那块护手皮？行，说明你不是第一次找我。少废话，直接来。",
             choices=[choice("开始扳手腕。", "arm_intro", 1), choice("问器材室。", "equipment")]),
        node("has_broadcast_token", "狗头：赵子龙那边你也碰过？怪不得广播一响就有人把我支开。你想查器材室，就别让广播乱响。",
             choices=[choice("让你帮我堵住器材室门。", "block_door", 1), choice("还是比一次。", "arm_intro", 1)]),
        node("equipment", "狗头：器材室少了一截旧绳和一个护腕。我没拿。谁拿的我不敢保证，但那人知道体育课集合前没人看门。",
             choices=[choice("我需要你帮我拖住集合。", "block_door", 1), choice("用比赛决定。", "arm_intro", 1)]),
        node("block_door", "狗头：可以。我站门口一分钟，够你进去看一眼。超过一分钟，我不替你背锅。",
             choices=[choice("拿走断裂护腕。", "token", 1), choice("算了", "")]),
        node("arm_intro", "狗头：规则简单，两边一起点。进度条推到对面那头就赢。别看字，看手。",
             minigame=minigame("arm_wrestle", "扳手腕：狗头", "arm_win", "arm_lose")),
        node("arm_win", "狗头：行，你手比我稳。器材室的门我替你挡一下，断掉的护腕你拿走。",
             choices=[choice("拿走断裂护腕。", "token")]),
        node("arm_lose", "狗头：你输了。今天我不会给你让路，但你还能从广播或柜子那边绕。"),
        node("token", "狗头：这东西算我的。拿了就别到处说是我给的。",
             rewards=[reward(TOKENS["athlete_goutou"])]),
        node("after_school", "狗头：放学我走操场侧门。你要那时候找我，也行，但别指望我还陪你耗体力。"),
    ])


def scholar_liugu():
    return tree([
        node("start", "六谷：你要问线索，可以。先证明你听得懂逻辑。错一次，我就不会继续浪费时间。",
             jumps=[jump("minecraft:book", "has_book"), jump(TOKENS["monitor_bc"], "has_bc_token")],
             choices=[
                 choice("开始知识问答。", "q1", 1),
                 choice("问满分试卷。", "paper"),
                 choice("放学前能不能补问？", "after_school"),
                 choice("离开", ""),
             ]),
        node("has_book", "六谷：你带了资料书？那我跳过第一题。第二题开始，别靠猜。",
             choices=[choice("回答第二题。", "q2", 1), choice("问试卷。", "paper")]),
        node("has_bc_token", "六谷：BC 的点名牌在你手里？那名单被改这件事就能和试卷顺序连起来了。你问吧。",
             choices=[choice("问试卷顺序。", "paper"), choice("直接做最后一题。", "q3", 1)]),
        node("q1", "六谷：第一题。点名册上后来补进去的名字，最可能说明什么？",
             choices=[choice("有人试图改掉当天的行动顺序。", "q2"), choice("只是老师写错了名字。", "fail")]),
        node("q2", "六谷：第二题。如果广播让所有人晚到十秒，最容易被伪装的是什么？",
             choices=[choice("一个人原本不该出现的位置。", "q3"), choice("一张试卷的分数。", "fail")]),
        node("q3", "六谷：最后一题。破损试卷真正能证明的，不是分数，而是什么？",
             choices=[choice("有人提前知道还没发生的顺序。", "quiz_win"), choice("六谷故意藏了答案。", "fail")]),
        node("quiz_win", "六谷：你不是来抄答案的。破掉的试卷给你，它能证明有人提前知道顺序。",
             rewards=[reward(TOKENS["scholar_liugu"])]),
        node("paper", "六谷：满分试卷不是重点，重点是它被撕掉的那一角。那一角对应的是今天还没发生的题目顺序。",
             choices=[choice("继续追问。", "q2", 1), choice("先离开", "")]),
        node("fail", "六谷：错了。你现在问下去只会制造噪音。想单独完成我这条线，下一轮先准备资料书。"),
        node("after_school", "六谷：放学我会把错题纸扔掉。那时还能问，但只能问最后一题。"),
    ])


def timid_suixiaole():
    return tree([
        node("start", "随小乐：你别突然靠这么近。我没做什么，我只是刚好看见了几个人经过。",
             jumps=[jump("minecraft:string", "has_string"), jump(TOKENS["broadcaster_zhaozilong"], "has_broadcast_token")],
             choices=[
                 choice("我不逼你，你慢慢说。", "comfort"),
                 choice("你看见谁进了空教室？", "pressure", 1),
                 choice("放学我还能找你吗？", "after_school"),
                 choice("离开", ""),
             ]),
        node("has_string", "随小乐：这是楼梯扶手上掉的线？我记得它挂在那里的时候，有人刚从空教室跑出来。",
             choices=[choice("请她把顺序说完。", "comfort"), choice("直接追问名字。", "pressure", 1)]),
        node("has_broadcast_token", "随小乐：你已经知道广播那段了？那我可以少说一点。广播响之前，王少栋从楼梯那边下来过。",
             choices=[choice("让她写下来。", "write_note", 1), choice("先离开", "")]),
        node("comfort", "随小乐：我只记得顺序。先是脚步声，然后广播试音，然后有人把门关上。你别问我为什么当时没喊。",
             choices=[choice("请她写一张证词。", "write_note", 1), choice("继续问细节。", "detail")]),
        node("pressure", "随小乐：我不知道！我只看见衣角从楼梯拐过去。你越这样问，我越想不起来。",
             choices=[choice("道歉，重新慢慢问。", "comfort"), choice("继续逼问。", "fail", 1)]),
        node("detail", "随小乐：衣角很干净，不像刚跑过操场的人。那个人停了一下，像是在听广播室那边的声音。",
             choices=[choice("请她写下证词。", "write_note", 1)]),
        node("write_note", "随小乐：我写，但你别把我的名字说出去。皱掉也没关系，反正我写字本来就会抖。",
             rewards=[reward(TOKENS["timid_suixiaole"])]),
        node("fail", "随小乐：别问了。我不会再说。"),
        node("after_school", "随小乐：放学门口人多，我会轻松一点。你那时候问，我可能还能把最后一句补上。"),
    ])


def rich_sunwukong():
    return tree([
        node("start", "孙悟空：想从我这里拿消息？可以，换。别跟我谈公平，学校里最没用的就是公平。",
             jumps=[jump("minecraft:emerald", "has_emerald"), jump(TOKENS["scholar_liugu"], "has_scholar_token")],
             choices=[
                 choice("做一笔柜子交易。", "deal", 1),
                 choice("抢柜子比一局。", "locker_game", 1),
                 choice("问王少栋盯着哪个柜子。", "ask_locker"),
                 choice("离开", ""),
             ]),
        node("has_emerald", "孙悟空：绿宝石？你还真懂规矩。行，少废话，柜子区我给你开一次口子。",
             choices=[choice("直接换消息。", "deal"), choice("还是比抢柜子。", "locker_game", 1)]),
        node("has_scholar_token", "孙悟空：六谷的试卷都到你手里了？那我知道你不是随便问问。王少栋看的柜子在广播室转角。",
             choices=[choice("追问柜子里的东西。", "ask_locker"), choice("换信物。", "token", 1)]),
        node("deal", "孙悟空：我给你一个位置，你给我一个保证。柜子区午休会空一小段，但广播室门口会有人经过。",
             choices=[choice("接受交易。", "token", 1), choice("不接受", "")]),
        node("ask_locker", "孙悟空：他盯的不是柜子，是柜子门缝里塞过的纸。那纸后来不在柜子里了。",
             choices=[choice("继续查柜子。", "locker_game", 1), choice("先离开", "")]),
        node("locker_game", "孙悟空：一分钟。点对加分，点错不扣。谁抢到的有效柜子多，谁说了算。",
             minigame=minigame("locker_search_duel", "抢柜子：孙悟空", "token", "locker_fail")),
        node("locker_fail", "孙悟空：手慢了。你还能用别人的线绕过来，但今天别指望我白送。"),
        node("token", "孙悟空：手机挂坠裂了，但还能证明你赢过我这边。拿着。",
             rewards=[reward(TOKENS["rich_sunwukong"])]),
    ])


def broadcaster_zhaozilong():
    return tree([
        node("start", "赵子龙：广播不是喊话，是节奏。谁在什么时候听见什么，比内容本身更重要。",
             jumps=[jump("minecraft:note_block", "has_note_block"), jump(TOKENS["timid_suixiaole"], "has_witness")],
             choices=[
                 choice("挑战节奏对抗。", "rhythm_intro", 1),
                 choice("请你改一次广播。", "broadcast_deal", 1),
                 choice("问广播前后谁动过。", "ask_timing"),
                 choice("离开", ""),
             ]),
        node("has_note_block", "赵子龙：你带着音符盒？那你至少知道广播室不是摆设。要我插一句话，就跟上我的节奏。",
             choices=[choice("开始节奏对抗。", "rhythm_intro", 1), choice("问时间点。", "ask_timing")]),
        node("has_witness", "赵子龙：随小乐肯写证词？那说明她真怕了。广播响前后确实有人借声音遮过去。",
             choices=[choice("让你帮我改广播。", "broadcast_deal", 1), choice("开始节奏对抗。", "rhythm_intro", 1)]),
        node("ask_timing", "赵子龙：试音在集合前，正式广播在午休后。中间那段空白，最适合让一个人从空教室变成‘一直在走廊’。",
             choices=[choice("请你改一次广播。", "broadcast_deal", 1), choice("用比赛决定。", "rhythm_intro", 1)]),
        node("broadcast_deal", "赵子龙：我可以晚播十秒。十秒不多，但够一个人错过该在的位置。",
             choices=[choice("收下带颜料痕的画笔。", "token"), choice("先不用", "")]),
        node("rhythm_intro", "赵子龙：点到有效区就换下一条，位置和宽度都不固定。你要赢我，就别等它慢慢过来。",
             minigame=minigame("rhythm_duel", "节奏对抗：赵子龙", "rhythm_win", "rhythm_fail")),
        node("rhythm_win", "赵子龙：跟上了。那支画笔归你，别问为什么在广播室。",
             choices=[choice("拿走画笔。", "token")]),
        node("rhythm_fail", "赵子龙：慢了。你听见的是广播，我听见的是节奏。"),
        node("token", "赵子龙：画笔给你。它能证明我的线，但不能证明是谁把它带进来的。",
             rewards=[reward(TOKENS["broadcaster_zhaozilong"])]),
    ])


def transfer_wangshaodong():
    return tree([
        node("start", "王少栋：我刚转来，很多地方还不熟。你要问路可以，但别问我为什么知道那条近路。",
             jumps=[jump("minecraft:map", "has_map"), jump(TOKENS["monitor_bc"], "has_bc_token")],
             choices=[
                 choice("问空教室的近路。", "route", 1),
                 choice("问柜子里的纸条。", "note"),
                 choice("放学你准备从哪走？", "after_school"),
                 choice("离开", ""),
             ]),
        node("has_map", "王少栋：你拿着我画的路线还来问我？那你应该知道，空教室的门不是我第一次推开的。",
             choices=[choice("追问第一次是谁。", "route", 1), choice("问纸条。", "note")]),
        node("has_bc_token", "王少栋：BC 给你看过名单？那我也不装了。我的名字被补进去之前，名单上空着一格。",
             choices=[choice("问空着的一格。", "note"), choice("逼他交出纸条。", "duel", 1)]),
        node("route", "王少栋：从楼梯间绕到空教室，不会经过正门。可这条路不该只有我知道。",
             choices=[choice("让他画路线图。", "map_reward", 1), choice("问纸条。", "note")]),
        node("map_reward", "王少栋：给你。别说是我画的。它只是路线，不是答案。",
             rewards=[reward("minecraft:map")]),
        node("note", "王少栋：纸条烧过一角，上面只剩半句话。要拿走可以，先证明你能比我更快找到它。",
             choices=[choice("开始抢柜子。", "duel", 1), choice("用路线图换。", "token", 1)]),
        node("duel", "王少栋：一分钟，谁先找到有效格子谁领先。点错不扣分，但浪费时间。",
             minigame=minigame("locker_search_duel", "空教室对抗：王少栋", "token", "duel_fail")),
        node("duel_fail", "王少栋：你慢了。纸条我先收着。你还能从 BC 或孙悟空那里绕回来。"),
        node("token", "王少栋：烧焦纸条给你。你别以为拿到它就懂我了。",
             rewards=[reward(TOKENS["transfer_wangshaodong"])]),
        node("after_school", "王少栋：放学我会走校门口，不走侧门。因为那时候所有人都看得见我。"),
    ])


def teacher_yuge():
    student_tokens = [
        TOKENS["monitor_bc"],
        TOKENS["athlete_goutou"],
        TOKENS["scholar_liugu"],
        TOKENS["timid_suixiaole"],
        TOKENS["rich_sunwukong"],
        TOKENS["broadcaster_zhaozilong"],
        TOKENS["transfer_wangshaodong"],
    ]
    checks = []
    for index, item in enumerate(student_tokens):
        next_id = f"check_{index + 1}" if index + 1 < len(student_tokens) else "complete"
        checks.append(node(f"check_{index}", f"雨哥：第 {index + 1} 个信物，拿出来。少一个都不算你走完这一轮。", "missing", jumps=[jump(item, next_id)]))
    return tree([
        node("start", "雨哥：今天按五个阶段走：校门、教室、操场、午休、放学。你可以单独查任何一条线，也可以试着一轮全收。",
             choices=[
                 choice("询问五个阶段。", "phase_info"),
                 choice("我已经集齐七个学生信物。", "check_0"),
                 choice("询问体力安排。", "stamina"),
                 choice("离开", ""),
             ]),
        node("phase_info", "雨哥：最后不是办公室检查，是校门口放学。放学阶段仍然能补线，但信物不会留到下一轮。"),
        node("stamina", "雨哥：每个阶段五点体力。单独做一条线够用，想全收就要少走重复选择。体力空了，当前对话结束后才进下一阶段。"),
        *checks,
        node("complete", "雨哥：七个学生信物都在同一轮里集齐了。现在给你老师信物。下一轮开始前，记住哪些普通道具能保留。",
             rewards=[reward(TOKENS["teacher_yuge"])]),
        node("missing", "雨哥：还不够。你可以单独完成其中一条学生线，但要拿我的信物，必须这一轮七条线都成。"),
    ])


DIALOGS = [
    ("BC完整线", "monitor_bc", monitor_bc),
    ("狗头完整线", "athlete_goutou", athlete_goutou),
    ("六谷完整线", "scholar_liugu", scholar_liugu),
    ("随小乐完整线", "timid_suixiaole", timid_suixiaole),
    ("孙悟空完整线", "rich_sunwukong", rich_sunwukong),
    ("赵子龙完整线", "broadcaster_zhaozilong", broadcaster_zhaozilong),
    ("王少栋完整线", "transfer_wangshaodong", transfer_wangshaodong),
    ("雨哥隐藏结算线", "teacher_yuge", teacher_yuge),
]


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_readme():
    rows = "\n".join(
        f"| `{file_name}.json` | `{role_id}` | 1-5 |" for file_name, role_id, _ in DIALOGS
    )
    README.write_text(f"""# 学校循环 story_v2 JSON 包

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
{rows}

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
""", encoding="utf-8")


def write_guide():
    GUIDE_DIR.mkdir(parents=True, exist_ok=True)
    (GUIDE_DIR / "NPC角色书.md").write_text("""# NPC角色书

## 总规则

- 对话 JSON 里只放玩家能听见的话。
- NPC 的心理、行动、控场、什么时候出现，由这份角色书告诉扮演者。
- 一共有 5 个阶段，最后阶段是校门口放学。
- 每个学生线都能单独完成；全收集路线要求主角在同一轮里拿到七个学生信物。
- 信物不跨循环保留，普通道具可以作为下一轮提示或捷径。

## 阶段安排

1. 校门与早自习：所有角色露面，给出第一入口。
2. 教室与走廊：点名、试卷、路线、目击信息开始交叉。
3. 操场与楼梯间：狗头、随小乐、王少栋的行动线最活跃。
4. 午休柜子区与广播室：孙悟空和赵子龙控场，适合触发小游戏。
5. 校门口放学：所有单线都允许补完；雨哥负责隐藏结算。

## NPC个人说明

### BC

你负责点名册和秩序。你知道名单被改过，但不能一开口直接说答案。主角如果认真帮你核对，或者赢记忆翻牌，你才交出缺角点名牌。

### 狗头

你负责操场和器材室。你不喜欢长篇推理，主角要么用扳手腕赢你，要么拿到相关道具让你相信他。你的信物是断裂护腕。

### 六谷

你负责知识问答和试卷逻辑。题目由作者后续替换。你的重点不是考试分数，而是“有人提前知道顺序”。你的信物是破损试卷。

### 随小乐

你负责目击信息。你害怕卷入事件，所以不能被强逼。主角温和询问时，你会给出顺序；被逼急时，你会关闭对话。你的信物是皱巴巴证词。

### 孙悟空

你负责柜子区和交易。你什么都要交换，但不是纯坏人。主角可以通过交易或抢柜子赢你，拿到裂纹手机挂坠。

### 赵子龙

你负责广播和节奏。你知道广播能改变所有人的移动时机。主角可以赢节奏对抗，或用证词让你承认广播室的异常。你的信物是带痕画笔。

### 王少栋

你负责转学生和空教室路线。你知道不该知道的近路，但不会直接承认全部。主角可以从路线图、柜子纸条或 BC 的名单切入。你的信物是烧焦纸条。

### 雨哥

你负责阶段说明和最终结算。普通情况下只解释规则；当主角同一轮拿到七个学生信物时，给出老师信物。
""", encoding="utf-8")


def normalize_minigames():
    old_path = MINIGAME_DIR / "阶段5_物品_纸张复盘记忆.json"
    new_path = MINIGAME_DIR / "阶段5_物品_纸张放学记忆.json"
    if old_path.exists():
        old_path.replace(new_path)
    if new_path.exists():
        data = json.loads(new_path.read_text(encoding="utf-8"))
        if isinstance(data.get("minigame"), dict):
            data["minigame"]["title"] = "放学线索记忆"
        if isinstance(data.get("success"), dict):
            data["success"]["message"] = "你理清了放学前的正确顺序。"
        if isinstance(data.get("failure"), dict):
            data["failure"]["message"] = "你记错了放学前的顺序，本轮只能普通结算。"
        write_json(new_path, data)


def rebuild_zip():
    if ZIP_PATH.exists():
        ZIP_PATH.unlink()
    with zipfile.ZipFile(ZIP_PATH, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for folder in (DIALOG_DIR, MINIGAME_DIR, GUIDE_DIR):
            for path in sorted(folder.rglob("*")):
                if path.is_file():
                    archive.write(path, path.relative_to(MOD_DIR))
        archive.write(README, README.relative_to(MOD_DIR))


def main():
    if DIALOG_DIR.exists():
        shutil.rmtree(DIALOG_DIR)
    DIALOG_DIR.mkdir(parents=True, exist_ok=True)
    for file_name, _role_id, factory in DIALOGS:
        write_json(DIALOG_DIR / f"{file_name}.json", factory())
    normalize_minigames()
    write_readme()
    write_guide()
    rebuild_zip()
    print(f"dialogs={len(DIALOGS)}")
    print("phase_count=5")
    print("last_phase=校门口放学")


if __name__ == "__main__":
    main()
