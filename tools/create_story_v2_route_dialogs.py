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


PHASES = {
    1: "校门与早自习",
    2: "教室与走廊",
    3: "操场与楼梯间",
    4: "午休柜子区与广播室",
    5: "校门口放学",
}

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

VANILLA = {
    "monitor_bc": "minecraft:paper",
    "athlete_goutou": "minecraft:leather",
    "scholar_liugu": "minecraft:book",
    "timid_suixiaole": "minecraft:string",
    "rich_sunwukong": "minecraft:emerald",
    "broadcaster_zhaozilong": "minecraft:note_block",
    "transfer_wangshaodong": "minecraft:map",
}

ROLES = {
    "teacher_yuge": "雨哥",
    "monitor_bc": "BC",
    "athlete_goutou": "狗头",
    "scholar_liugu": "六谷",
    "timid_suixiaole": "随小乐",
    "rich_sunwukong": "孙悟空",
    "broadcaster_zhaozilong": "赵子龙",
    "transfer_wangshaodong": "王少栋",
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
    data = {
        "type": kind,
        "title": title,
        "durationTicks": 1200,
        "successNodeId": success,
        "failureNodeId": failure,
    }
    if kind == "arm_wrestle":
        data.update({"pushPerClick": 0.06, "winProgress": 1.0})
    elif kind == "memory_flip_duel":
        data.update({"previewTicks": 50, "rounds": 6, "columns": 5, "targetCount": 6})
    elif kind in {"locker_search_duel", "rhythm_duel"}:
        data.update({"rounds": 60, "winClickLead": 1})
    return data


def student_dialog(role_id, phase):
    name = ROLES[role_id]
    if role_id == "monitor_bc":
        return bc_dialog(phase)
    if role_id == "athlete_goutou":
        return goutou_dialog(phase)
    if role_id == "scholar_liugu":
        return liugu_dialog(phase)
    if role_id == "timid_suixiaole":
        return suixiaole_dialog(phase)
    if role_id == "rich_sunwukong":
        return sunwukong_dialog(phase)
    if role_id == "broadcaster_zhaozilong":
        return zhaozilong_dialog(phase)
    if role_id == "transfer_wangshaodong":
        return wangshaodong_dialog(phase)
    raise ValueError(f"unknown role {name}")


def bc_dialog(phase):
    if phase == 1:
        return tree([
            node("start", "BC：你来得正好。今天点名册有两处擦痕，我一个人查不完。你要帮，就从名字顺序开始。",
                 choices=[choice("我帮你核对点名册。", "help", 1), choice("哪两处不对？", "ask"), choice("先不管。", "")]),
            node("help", "BC：随小乐的名字旁边被擦过，王少栋的名字像是后来补上去的。这个纸角你拿着，之后查到东西再回来对。", rewards=[reward(VANILLA["monitor_bc"])]),
            node("ask", "BC：我只能说，名单不是今天早上才出问题。有人希望大家以为顺序从一开始就是这样。"),
        ])
    if phase == 2:
        return tree([
            node("start", "BC：走廊现在最乱。你要查我这条线，就记住我刚才巡过的六个人。",
                 jumps=[jump(VANILLA["monitor_bc"], "prepared")],
                 choices=[choice("我现在记。", "memory", 1), choice("我想问王少栋。", "transfer"), choice("离开", "")]),
            node("prepared", "BC：你还留着点名册纸角？好，那你不是临时起意。直接开始记忆翻牌，赢了我给你看原名单。", choices=[choice("开始。", "memory", 1)]),
            node("transfer", "BC：王少栋不是最大的问题，最大的问题是谁给他留了位置。"),
            node("memory", "BC：十五个格子里有六个发光格子。找出来，别点错。", minigame=minigame("memory_flip_duel", "记忆翻牌：BC", "win", "lose")),
            node("win", "BC：你记得比我想得清楚。原名单确实少了一格，午休前去柜子区能接上。", rewards=[reward("minecraft:compass")]),
            node("lose", "BC：错了。你这阶段还能继续查别人，我这边等下一次机会。"),
        ])
    if phase == 3:
        return tree([
            node("start", "BC：操场集合时，我会离开教室门口。有人就是等这个空档。",
                 jumps=[jump("minecraft:compass", "has_compass"), jump(TOKENS["transfer_wangshaodong"], "has_transfer")],
                 choices=[choice("我去确认空档。", "gap", 1), choice("谁会利用空档？", "who"), choice("离开", "")]),
            node("has_compass", "BC：你拿着巡查指针，就能知道我什么时候不在门口。别浪费，这阶段就能把线推进到底。", choices=[choice("锁定空档。", "gap", 1)]),
            node("has_transfer", "BC：王少栋那张烧焦纸条说明他知道空教室，但改名单的人还没露面。"),
            node("gap", "BC：广播试音后，我会去操场清人。那一分钟，教室和柜子区都没人盯。"),
            node("who", "BC：能利用空档的人，必须同时知道点名、广播和柜子。你已经碰到不止一条线了。"),
        ])
    if phase == 4:
        return tree([
            node("start", "BC：午休柜子区没人看着，但这不代表没人记得。你要我的信物，现在还有机会。",
                 jumps=[jump("minecraft:compass", "ready")],
                 choices=[choice("帮你重新清一次名单。", "memory", 1), choice("我已经知道柜子区空档。", "ready"), choice("离开", "")]),
            node("ready", "BC：那就不用绕了。你说出六个被调动过的人，我给你缺角点名牌。", choices=[choice("开始记忆翻牌。", "memory", 1), choice("直接说明顺序。", "token", 1)]),
            node("memory", "BC：最后一次。还是十五格，六个发光格。", minigame=minigame("memory_flip_duel", "记忆翻牌：BC", "token", "lose")),
            node("token", "BC：缺角点名牌给你。它证明班长线完成，但不会带到下一轮。", rewards=[reward(TOKENS["monitor_bc"])]),
            node("lose", "BC：这次错了，我不会再让你碰名单。"),
        ])
    return tree([
        node("start", "BC：放学门口我在清最后一遍名单。你之前没拿到信物，现在是最后补救。",
             jumps=[jump(TOKENS["monitor_bc"], "already")],
             choices=[choice("最后核对一次。", "token", 1), choice("问全收集顺序。", "hint"), choice("离开", "")]),
        node("already", "BC：你已经拿过我的点名牌了。放学阶段别在我这里浪费体力，去补没完成的人。"),
        node("token", "BC：行，最后这次算你跟上了。缺角点名牌拿着。", rewards=[reward(TOKENS["monitor_bc"])]),
        node("hint", "BC：全收集别先乱花体力。点名、路线、广播、柜子这四件事要连着看。"),
    ])


def goutou_dialog(phase):
    if phase == 1:
        return tree([
            node("start", "狗头：早上就找我？我还没热完身。想让我认你，先别光说。",
                 choices=[choice("比一次扳手腕。", "arm", 1), choice("问器材室。", "room"), choice("离开", "")]),
            node("arm", "狗头：两边一起点，推到头就赢。", minigame=minigame("arm_wrestle", "扳手腕：狗头", "win", "lose")),
            node("win", "狗头：行，你有劲。护手皮给你，后面找我别再从头解释。", rewards=[reward(VANILLA["athlete_goutou"])]),
            node("lose", "狗头：手太软。要查我这条线，后面还得再来。"),
            node("room", "狗头：器材室早上没开，但我看见有人盯着钥匙箱。"),
        ])
    if phase == 2:
        return tree([
            node("start", "狗头：走廊别挡我。体育课前器材室会开，那时候才是真机会。",
                 jumps=[jump(VANILLA["athlete_goutou"], "trusted")],
                 choices=[choice("让你帮我看门。", "guard", 1), choice("再比一次。", "arm", 1), choice("离开", "")]),
            node("trusted", "狗头：护手皮还在？行，我信你一次。器材室开门时我能替你挡一分钟。", choices=[choice("拜托你看门。", "guard", 1)]),
            node("guard", "狗头：一分钟。你进去只能拿一个东西，别贪。", rewards=[reward("minecraft:string")]),
            node("arm", "狗头：赢我，我就不问你为什么要进器材室。", minigame=minigame("arm_wrestle", "扳手腕：器材室", "guard", "lose")),
            node("lose", "狗头：今天这阶段你别想从我这边进器材室。"),
        ])
    if phase == 3:
        return tree([
            node("start", "狗头：操场集合了。你现在查我这条线正合适，错过就只能放学补。",
                 jumps=[jump("minecraft:string", "has_string"), jump(TOKENS["broadcaster_zhaozilong"], "broadcast")],
                 choices=[choice("安排器材意外。", "setup", 1), choice("问广播为什么支开你。", "broadcast"), choice("离开", "")]),
            node("has_string", "狗头：你拿到旧绳了？那就能知道器材不是自然断的。"),
            node("broadcast", "狗头：广播一响，我会去操场另一边。有人知道我的习惯。"),
            node("setup", "狗头：你要我承认这条线，就再赢我一次。赢了，断裂护腕归你。", minigame=minigame("arm_wrestle", "扳手腕：最终", "token", "lose")),
            node("token", "狗头：断裂护腕给你。别说是我输给你的。", rewards=[reward(TOKENS["athlete_goutou"])]),
            node("lose", "狗头：输了就别提护腕。"),
        ])
    if phase == 4:
        return tree([
            node("start", "狗头：午休我不在柜子区，但器材室的事已经能收尾。你要补，就现在补。",
                 choices=[choice("最后比一次。", "arm", 1), choice("用旧绳说明问题。", "token", 1), choice("离开", "")]),
            node("arm", "狗头：来，到头结束。", minigame=minigame("arm_wrestle", "扳手腕：补救", "token", "lose")),
            node("token", "狗头：行，护腕给你。这条线算你完成。", rewards=[reward(TOKENS["athlete_goutou"])]),
            node("lose", "狗头：这阶段没了。放学还可以最后问一次。"),
        ])
    return tree([
        node("start", "狗头：放学了。你还没拿护腕？最后一次，不绕。",
             jumps=[jump(TOKENS["athlete_goutou"], "already")],
             choices=[choice("最后扳一次。", "arm", 1), choice("直接要护腕。", "token", 1), choice("离开", "")]),
        node("already", "狗头：护腕已经给你了，别在我这拖时间。"),
        node("arm", "狗头：最后一次。", minigame=minigame("arm_wrestle", "扳手腕：放学", "token", "lose")),
        node("token", "狗头：拿着。今天就到这。", rewards=[reward(TOKENS["athlete_goutou"])]),
        node("lose", "狗头：到点了，下一轮再来。"),
    ])


def liugu_dialog(phase):
    questions = {
        1: ("六谷：第一题。被补进点名册的名字，说明什么？", "有人改过行动顺序。", "只是笔迹不好。"),
        2: ("六谷：第二题。广播晚十秒，最容易改变什么？", "某个人在场的时间。", "作业本的数量。"),
        3: ("六谷：第三题。破损试卷缺的不是答案，而是什么？", "事件顺序。", "老师签名。"),
        4: ("六谷：第四题。柜子区和广播室同时异常，说明什么？", "有人把人流和物品放在一起设计。", "午休太吵了。"),
        5: ("六谷：最后一题。单独完成一条线最重要的是什么？", "在每阶段接住本角色给出的证据。", "等所有人都出事再问。"),
    }
    q, good, bad = questions[phase]
    reward_item = TOKENS["scholar_liugu"] if phase >= 4 else VANILLA["scholar_liugu"]
    return tree([
        node("start", f"六谷：第 {phase} 阶段，你还能继续问我。我的线不等别人，但你必须答对逻辑。",
             jumps=[jump(VANILLA["scholar_liugu"], "prepared"), jump(TOKENS["monitor_bc"], "bc_link")],
             choices=[choice("开始答题。", "q", 1), choice("问破损试卷。", "paper"), choice("离开", "")]),
        node("prepared", "六谷：资料书还在，说明你记得前面的题。那就直接问关键题。", choices=[choice("回答关键题。", "q", 1)]),
        node("bc_link", "六谷：BC 的点名牌能证明顺序被动过，这和我的试卷能接上。"),
        node("paper", "六谷：试卷被撕掉的一角，对应的是还没发生的顺序。你要拿它，就先答题。", choices=[choice("答题。", "q", 1)]),
        node("q", q, choices=[choice(good, "win"), choice(bad, "fail")]),
        node("win", "六谷：答对了。这个证据给你。越到后面，它越能直接指向完整答案。", rewards=[reward(reward_item)]),
        node("fail", "六谷：错。我的线可以单独完成，但不能靠猜。"),
    ])


def suixiaole_dialog(phase):
    texts = {
        1: "随小乐：我只看见有人从楼梯那边过去，你别问太快。",
        2: "随小乐：走廊那次脚步声停了一下，像是在听广播试音。",
        3: "随小乐：操场集合前，有人从空教室方向跑出来。",
        4: "随小乐：午休柜子区最乱，我看见纸条被换过地方。",
        5: "随小乐：放学门口人多，我终于敢把证词交给你。",
    }
    reward_item = TOKENS["timid_suixiaole"] if phase >= 4 else VANILLA["timid_suixiaole"]
    return tree([
        node("start", texts[phase],
             jumps=[jump(VANILLA["timid_suixiaole"], "gentle"), jump(TOKENS["broadcaster_zhaozilong"], "broadcast_link")],
             choices=[choice("慢慢说，我不逼你。", "comfort"), choice("直接问你看见谁。", "pressure", 1), choice("离开", "")]),
        node("gentle", "随小乐：你还留着那根线？那我知道你真的查过楼梯。"),
        node("broadcast_link", "随小乐：赵子龙也说了广播？那我没记错，声音确实遮住了脚步。"),
        node("comfort", "随小乐：我可以写下来，但你别把我的名字说出去。", choices=[choice("请她写证词。", "note", 1), choice("继续问细节。", "detail")]),
        node("pressure", "随小乐：你这样问我会乱。再逼我，我什么都不会说。", choices=[choice("道歉。", "comfort"), choice("继续逼问。", "fail", 1)]),
        node("detail", "随小乐：顺序是脚步、试音、关门。这个顺序很重要。"),
        node("note", "随小乐：给你。纸有点皱，但我写的是真的。", rewards=[reward(reward_item)]),
        node("fail", "随小乐：别问了。"),
    ])


def sunwukong_dialog(phase):
    if phase == 1:
        line = "孙悟空：早上想打听柜子？可以，先讲交换。"
    elif phase == 2:
        line = "孙悟空：走廊人多，消息贵。你要问王少栋，就拿有用的东西换。"
    elif phase == 3:
        line = "孙悟空：操场那边一乱，柜子区就会空。你现在问得正是时候。"
    elif phase == 4:
        line = "孙悟空：午休柜子区是我的地盘。要东西，就比谁找得快。"
    else:
        line = "孙悟空：放学前最后一笔。过了校门，今天的信物就不算了。"
    reward_item = TOKENS["rich_sunwukong"] if phase >= 4 else VANILLA["rich_sunwukong"]
    return tree([
        node("start", line,
             jumps=[jump(VANILLA["rich_sunwukong"], "paid"), jump(TOKENS["scholar_liugu"], "paper_link")],
             choices=[choice("做交易。", "deal", 1), choice("抢柜子比一局。", "game", 1), choice("问王少栋看的柜子。", "locker"), choice("离开", "")]),
        node("paid", "孙悟空：你带着绿宝石？规矩你懂，那我给你先手机会。", choices=[choice("换消息。", "deal"), choice("比抢柜子。", "game", 1)]),
        node("paper_link", "孙悟空：六谷的试卷都到你手里了？那我说实话，柜子里藏过半张纸。"),
        node("deal", "孙悟空：广播室转角那个柜子，有人上午盯过。这个消息值你一点体力。", rewards=[reward(reward_item)]),
        node("locker", "孙悟空：王少栋看的不是柜子，是门缝里塞过的纸。"),
        node("game", "孙悟空：一分钟，点对加分，点错不扣。谁分高，谁拿东西。", minigame=minigame("locker_search_duel", "抢柜子：孙悟空", "win", "lose")),
        node("win", "孙悟空：你赢了。裂纹手机挂坠给你，这条线算你做成。", rewards=[reward(TOKENS["rich_sunwukong"])]),
        node("lose", "孙悟空：手慢了。还能交易，但别想白拿。"),
    ])


def zhaozilong_dialog(phase):
    lines = {
        1: "赵子龙：早上的广播只试音，不通知。试音本身也能让人停一下。",
        2: "赵子龙：走廊乱，是因为大家都在等广播。等声音的人，会暴露习惯。",
        3: "赵子龙：操场集合靠广播卡点。十秒，足够让一个人不在原位。",
        4: "赵子龙：午休广播室最关键。你要让我改一次节奏，就赢我。",
        5: "赵子龙：放学广播不会再响。现在问，是最后一次补线。",
    }
    reward_item = TOKENS["broadcaster_zhaozilong"] if phase >= 4 else VANILLA["broadcaster_zhaozilong"]
    return tree([
        node("start", lines[phase],
             jumps=[jump(VANILLA["broadcaster_zhaozilong"], "has_note"), jump(TOKENS["timid_suixiaole"], "witness_link")],
             choices=[choice("挑战节奏对抗。", "rhythm", 1), choice("请你晚播十秒。", "delay", 1), choice("问广播前后谁动了。", "timing"), choice("离开", "")]),
        node("has_note", "赵子龙：音符盒在你手上？那你知道节奏比内容重要。"),
        node("witness_link", "赵子龙：随小乐愿意写证词？那广播遮住脚步这件事就坐实了。"),
        node("timing", "赵子龙：试音前没人动，试音后有人从楼梯间出来。正式广播只是把怀疑打散。"),
        node("delay", "赵子龙：我可以晚播十秒。十秒不多，但够一条线接上。", rewards=[reward(reward_item)]),
        node("rhythm", "赵子龙：点中有效区就换下一条，位置和宽度都随机。", minigame=minigame("rhythm_duel", "节奏对抗：赵子龙", "win", "lose")),
        node("win", "赵子龙：跟上了。带痕画笔给你，别问它为什么在广播室。", rewards=[reward(TOKENS["broadcaster_zhaozilong"])]),
        node("lose", "赵子龙：慢了。听见声音和抓住节奏是两回事。"),
    ])


def wangshaodong_dialog(phase):
    lines = {
        1: "王少栋：我刚转来，按理说不该知道近路。可我确实知道。",
        2: "王少栋：走廊太多人盯着我，我只能把路线画给你。",
        3: "王少栋：楼梯间那条路能绕到空教室，但不是我第一个发现的。",
        4: "王少栋：柜子里的纸条被人换过位置。你要拿，就和我比谁找得快。",
        5: "王少栋：放学校门口，我不会再绕路。你要问，就现在问完。",
    }
    reward_item = TOKENS["transfer_wangshaodong"] if phase >= 4 else VANILLA["transfer_wangshaodong"]
    return tree([
        node("start", lines[phase],
             jumps=[jump(VANILLA["transfer_wangshaodong"], "has_map"), jump(TOKENS["monitor_bc"], "bc_link")],
             choices=[choice("问空教室路线。", "route", 1), choice("问烧焦纸条。", "note"), choice("抢柜子比一局。", "game", 1), choice("离开", "")]),
        node("has_map", "王少栋：你拿着我画的图还来问？那你应该知道，有人比我更早走过这条路。"),
        node("bc_link", "王少栋：BC 的点名牌在你手里？那我承认，我的名字确实不是一开始就在名单上。"),
        node("route", "王少栋：从楼梯间绕过去，不经过正门。给你路线图，但别说是我画的。", rewards=[reward(VANILLA["transfer_wangshaodong"])]),
        node("note", "王少栋：纸条烧过一角，剩下的半句话指向柜子区。", choices=[choice("要走纸条。", "token", 1), choice("比一局。", "game", 1)]),
        node("game", "王少栋：一分钟，谁先找到更多有效格子，谁拿纸条。", minigame=minigame("locker_search_duel", "空教室对抗：王少栋", "token", "lose")),
        node("token", "王少栋：烧焦纸条给你。你别以为拿到它就懂我了。", rewards=[reward(reward_item)]),
        node("lose", "王少栋：这次我先收着。你还能从 BC 或孙悟空那边绕回来。"),
    ])


def teacher_dialog(phase):
    if phase < 5:
        return tree([
            node("start", f"雨哥：现在是第 {phase} 阶段，地点是{PHASES[phase]}。每个人这阶段都有自己的对话，不要等到后面才找人。",
                 choices=[choice("问这个阶段怎么推进。", "phase"), choice("问体力。", "stamina"), choice("离开", "")]),
            node("phase", "雨哥：单独做线，就盯住一个人把本阶段证据拿到；想全收集，就按点名、路线、广播、柜子的顺序少走重复路。"),
            node("stamina", "雨哥：每阶段五点体力。体力空了不是立刻切，当前对话结束后再进下一阶段。"),
        ])
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
        checks.append(node(f"check_{index}", f"雨哥：第 {index + 1} 个学生信物，拿出来。", "missing", jumps=[jump(item, next_id)]))
    return tree([
        node("start", "雨哥：放学了。单独完成的线，到这里可以最后补信物；如果你说自己全收集，那就交七个学生信物。",
             choices=[choice("提交七个学生信物。", "check_0"), choice("问放学补线。", "hint"), choice("离开", "")]),
        node("hint", "雨哥：放学是最后阶段。没完成的人现在还能补，但出了校门就进入下一轮。"),
        *checks,
        node("complete", "雨哥：七个学生信物都在同一轮里集齐了。老师信物给你。", rewards=[reward(TOKENS["teacher_yuge"])]),
        node("missing", "雨哥：还差。单条线完成不等于全收集，少一个都不能拿老师信物。"),
    ])


def build_dialogs():
    dialogs = {}
    role_order = [
        "teacher_yuge",
        "monitor_bc",
        "athlete_goutou",
        "scholar_liugu",
        "timid_suixiaole",
        "rich_sunwukong",
        "broadcaster_zhaozilong",
        "transfer_wangshaodong",
    ]
    for phase, phase_name in PHASES.items():
        for role_id in role_order:
            name = ROLES[role_id]
            file_name = f"阶段{phase}_{name}_{phase_name}.json"
            dialogs[(file_name, role_id, phase)] = teacher_dialog(phase) if role_id == "teacher_yuge" else student_dialog(role_id, phase)
    return dialogs


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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


def write_readme(dialogs):
    rows = "\n".join(
        f"| `{file_name}` | `{role_id}` | {phase} |" for (file_name, role_id, phase) in dialogs.keys()
    )
    README.write_text(f"""# 学校循环 story_v2 JSON 包

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
    GUIDE_DIR.joinpath("NPC角色书.md").write_text("""# NPC角色书

## 总规则

- 一共 5 个阶段，最后阶段是校门口放学。
- 每个 NPC 每个阶段都有对白，玩家不需要等几个阶段才继续某条线。
- 对话框里只显示玩家能听到的话；行动、心理和控场说明看这里。
- 每条学生线都能单独完成，信物不跨循环保留。
- 普通道具可以作为本轮后续阶段的捷径或特殊分支。

## 阶段安排

1. 校门与早自习：建立入口。BC 给点名纸角，狗头给护手皮，六谷开启答题，随小乐给楼梯线索，孙悟空谈交易，赵子龙讲试音，王少栋给路线入口。
2. 教室与走廊：第一次实质推进。BC 记忆翻牌，狗头器材室看门，六谷第二题，随小乐补脚步顺序，孙悟空指出柜子，赵子龙说明广播，王少栋画路线。
3. 操场与楼梯间：中段交叉。狗头和王少栋最活跃，BC 确认空档，随小乐补目击，赵子龙解释十秒错位。
4. 午休柜子区与广播室：多数学生线可以完成。BC、狗头、六谷、随小乐、孙悟空、赵子龙、王少栋都能拿到各自信物。
5. 校门口放学：最后补救与雨哥结算。没完成的学生线可以最后补一次；集齐七个学生信物后找雨哥拿老师信物。

## NPC个人说明

### BC

你负责点名册和秩序。每阶段都围绕名单、巡查、空档推进。不要直接说凶手，只承认名单被动过。

### 狗头

你负责操场和器材室。你用扳手腕确认主角是否值得信任。主角赢你或拿到旧绳、广播线索时，你可以交出断裂护腕。

### 六谷

你负责知识问答和试卷逻辑。题目已经能直接玩，后续也可以替换。你的核心信息是“顺序被提前知道”。

### 随小乐

你负责目击证词。主角温和询问时你会逐步说出脚步、试音、关门顺序；被逼问时关闭对话。

### 孙悟空

你负责柜子区和交易。你可以收普通道具，也可以通过抢柜子决定是否给信物。

### 赵子龙

你负责广播和节奏。广播不是答案，但能解释为什么时间和位置会错开。

### 王少栋

你负责转学生和空教室路线。你知道不该知道的路线，但不一开始承认全部。

### 雨哥

你负责解释阶段和放学结算。阶段 1-4 只提醒玩法；阶段 5 检查七个学生信物。
""", encoding="utf-8")


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
    dialogs = build_dialogs()
    for (file_name, _role_id, _phase), data in dialogs.items():
        write_json(DIALOG_DIR / file_name, data)
    normalize_minigames()
    write_readme(dialogs)
    write_guide()
    rebuild_zip()
    print(f"dialogs={len(dialogs)}")
    print("phase_count=5")
    print("last_phase=校门口放学")


if __name__ == "__main__":
    main()
