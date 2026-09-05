import json
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
    1: ("校门与早自习", "刚进学校，所有线都能从介绍、试探、交换信息开始。"),
    2: ("教室与走廊", "课堂秩序和走廊巡查展开，每条线都能进入第一次实质推进。"),
    3: ("操场与楼梯间", "操场、器材室、楼梯目击交叉，每条线都能直接做关键行动。"),
    4: ("午休柜子区与广播室", "柜子、广播和空教室集中爆发，每条线都能完成或补救。"),
    5: ("办公室检查前", "最终检查前的补救阶段，每条学生线仍有最后完成机会。"),
    6: ("循环复盘", "本轮结束后的复盘阶段，每个 NPC 都能给下轮提示，但不再直接发学生信物。"),
}


ROLES = {
    "teacher_yuge": {
        "name": "雨哥",
        "role": "老师",
        "token": "minecraft:nether_star",
        "special_item": "minecraft:book",
        "keepsake": "minecraft:clock",
        "activity": "检查本轮信物",
        "topic": "日程、纪律和最终结算",
        "secret": "他知道主角是否接近全收集，但不会提前公布答案。",
    },
    "monitor_bc": {
        "name": "BC",
        "role": "班长",
        "token": "first_mod:chipped_attendance_tag",
        "special_item": "minecraft:paper",
        "keepsake": "minecraft:map",
        "activity": "整理点名和巡查路线",
        "topic": "点名册、巡查、柜子区空档",
        "secret": "名单被改过，王少栋和随小乐都和时间差有关。",
    },
    "athlete_goutou": {
        "name": "狗头",
        "role": "体育生",
        "token": "first_mod:broken_wristband",
        "special_item": "minecraft:leather",
        "keepsake": "minecraft:string",
        "activity": "守着器材室或操场入口",
        "topic": "器材室、护腕、操场集合",
        "secret": "器材被人动过，广播会让他短暂离开。",
        "minigame": {
            "type": "arm_wrestle",
            "title": "扳手腕：狗头",
            "pushPerClick": 0.06,
            "winProgress": 1.0,
        },
    },
    "scholar_liugu": {
        "name": "六谷",
        "role": "学霸",
        "token": "first_mod:broken_exam_paper",
        "special_item": "minecraft:book",
        "keepsake": "minecraft:paper",
        "activity": "用题目确认主角是否真的理解线索",
        "topic": "满分试卷、题目顺序、逻辑漏洞",
        "secret": "试卷不是分数问题，而是证明有人提前知道顺序。",
    },
    "timid_suixiaole": {
        "name": "随小乐",
        "role": "胆小鬼",
        "token": "first_mod:crumpled_witness_note",
        "special_item": "minecraft:string",
        "keepsake": "minecraft:feather",
        "activity": "躲在楼梯、厕所口或办公室外",
        "topic": "目击片段、空教室、广播前后顺序",
        "secret": "她看见过关键移动路线，但害怕说完整。",
        "minigame": {
            "type": "memory_flip_duel",
            "title": "记忆翻牌：随小乐",
            "durationTicks": 1200,
            "previewTicks": 50,
            "rounds": 6,
        },
    },
    "rich_sunwukong": {
        "name": "孙悟空",
        "role": "富家子",
        "token": "first_mod:cracked_phone_charm",
        "special_item": "minecraft:emerald",
        "keepsake": "minecraft:gold_nugget",
        "activity": "在柜子区谈交易",
        "topic": "交易、资源、柜子里的东西",
        "secret": "他知道王少栋盯着柜子，也知道赵子龙来过。",
        "minigame": {
            "type": "locker_search_duel",
            "title": "抢柜子：孙悟空",
            "durationTicks": 1200,
            "rounds": 60,
            "winClickLead": 1,
        },
    },
    "broadcaster_zhaozilong": {
        "name": "赵子龙",
        "role": "广播员",
        "token": "first_mod:stained_paintbrush",
        "special_item": "minecraft:note_block",
        "keepsake": "minecraft:redstone",
        "activity": "控制广播室和集合节奏",
        "topic": "广播、时间错位、人流调动",
        "secret": "广播能调开一部分人，但不能替代其他角色线。",
        "minigame": {
            "type": "rhythm_duel",
            "title": "节奏对抗：赵子龙",
            "durationTicks": 1200,
            "rounds": 60,
            "winClickLead": 1,
        },
    },
    "transfer_wangshaodong": {
        "name": "王少栋",
        "role": "转学生",
        "token": "first_mod:burnt_note",
        "special_item": "minecraft:map",
        "keepsake": "minecraft:charcoal",
        "activity": "在空教室和柜子区之间游走",
        "topic": "隐藏路线、空教室、被改过的名单",
        "secret": "他知道一个转学生不该知道的路线。",
        "minigame": {
            "type": "locker_search_duel",
            "title": "空教室对抗：王少栋",
            "durationTicks": 1200,
            "rounds": 60,
            "winClickLead": 1,
        },
    },
}


CROSS_ITEMS = {
    "teacher_yuge": ["first_mod:chipped_attendance_tag", "first_mod:broken_wristband", "first_mod:broken_exam_paper", "first_mod:crumpled_witness_note", "first_mod:cracked_phone_charm", "first_mod:stained_paintbrush", "first_mod:burnt_note"],
    "monitor_bc": ["first_mod:burnt_note", "first_mod:stained_paintbrush"],
    "athlete_goutou": ["first_mod:chipped_attendance_tag", "minecraft:note_block"],
    "scholar_liugu": ["first_mod:chipped_attendance_tag", "first_mod:cracked_phone_charm"],
    "timid_suixiaole": ["first_mod:stained_paintbrush", "first_mod:burnt_note"],
    "rich_sunwukong": ["first_mod:broken_exam_paper", "first_mod:burnt_note"],
    "broadcaster_zhaozilong": ["first_mod:crumpled_witness_note", "minecraft:map"],
    "transfer_wangshaodong": ["first_mod:crumpled_witness_note", "first_mod:broken_exam_paper"],
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


def filename(phase, info):
    return f"阶段{phase}_{info['name']}_{PHASES[phase][0]}.json"


def phase_text(phase, info):
    place, purpose = PHASES[phase]
    if phase == 6:
        return f"这一轮到复盘了。我是{info['name']}，你可以问我这轮哪里断了，但信物不会带到下一轮。"
    return f"现在是{place}。我是{info['name']}，我这边能聊的是{info['topic']}。你想从哪问起？"


def special_text(info):
    return f"你带着这个东西来，说明你不是空口问我。那我可以多说一句：{info['secret']}"


def cross_text(info, item):
    return f"你连 {item} 都拿到了？看来你已经接触过别的线了。那我只补我知道的部分：{info['secret']}"


def make_teacher_dialog(phase, role_id, info):
    if phase == 5:
        checks = []
        token_items = CROSS_ITEMS[role_id]
        for index, item in enumerate(token_items):
            next_id = f"check_{index + 1}" if index + 1 < len(token_items) else "complete"
            checks.append(node(f"check_{index}", f"第 {index + 1} 个信物，拿出来给我看。", "missing", jumps=[jump(item, next_id)]))
        return {
            "startNodeId": "start",
            "nodes": [
                node("start", "到办公室检查了。你说自己这一轮走完了，那就把七个学生信物逐个拿出来。", "check_0"),
                *checks,
                node("complete", "七个学生信物都齐了。这一轮你确实完整走到底了，老师信物给你。", rewards=[reward(info["token"])]),
                node("missing", "还不够。少一个都不算完成。下一轮别只想着单条线，顺序也要算进去。",
                     choices=[choice("询问下一轮怎么规划", "hint")]),
                node("hint", "先找能给普通道具的人，再用普通道具换特殊对话。信物最后一起结算，不要指望它们跨循环。"),
            ],
        }
    choices = [
        choice("询问当前阶段该注意什么", "phase_hint"),
        choice("询问体力和循环", "stamina_hint"),
        choice("结束对话", ""),
    ]
    return {
        "startNodeId": "start",
        "nodes": [
            node("start", phase_text(phase, info), choices=choices),
            node("phase_hint", f"{PHASES[phase][1]} 你可以现在就找任何人推进，不用等到后面阶段。"),
            node("stamina_hint", "每阶段只有五点体力。想全收集，就别把体力花在重复追问上。"),
        ],
    }


def make_student_dialog(phase, role_id, info):
    if phase == 6:
        return {
            "startNodeId": "start",
            "nodes": [
                node("start", phase_text(phase, info),
                     choices=[choice("问本轮这条线怎么更快", "route_hint"), choice("问能保留什么", "keep_hint"), choice("结束对话", "")]),
                node("route_hint", f"下轮你可以直接从{info['topic']}切入。别等阶段，见到我就能推进。"),
                node("keep_hint", f"信物不会保留，但像 {info['keepsake']} 这种普通道具可以作为前置准备。"),
            ],
        }

    jumps = [jump(info["special_item"], "special_item")]
    for idx, item in enumerate(CROSS_ITEMS.get(role_id, [])):
        jumps.append(jump(item, f"cross_{idx}"))

    choices = [
        choice("普通问话", "normal_talk"),
        choice("消耗体力直接推进这条线", "push_route", 1),
        choice("尝试用当前阶段完成这条线", "route_action", 1),
        choice("结束对话", ""),
    ]
    nodes = [
        node("start", phase_text(phase, info), choices=choices, jumps=jumps),
        node("normal_talk", f"我现在在做的是：{info['activity']}。你要是想查，就别只问一句。"),
        node("push_route", f"你愿意花体力继续问，那我就给你更明确的方向：{info['secret']}",
             rewards=[reward(info["keepsake"])]),
        node("special_item", special_text(info),
             choices=[choice("顺着特殊线索继续", "route_action"), choice("先拿普通道具", "push_route")]),
    ]

    for idx, item in enumerate(CROSS_ITEMS.get(role_id, [])):
        nodes.append(node(f"cross_{idx}", cross_text(info, item),
                          choices=[choice("把线索接到当前路线", "route_action", 1), choice("先离开", "")]))

    if role_id == "scholar_liugu":
        nodes.extend([
            node("route_action", "那就答题。第一题内容你后面自己填，答对就继续，答错这条线本轮锁住。",
                 choices=[choice("正确答案占位", "quiz_2"), choice("错误答案占位", "fail")]),
            node("quiz_2", "第二题内容你后面自己填。这题答对，我就认你这条线完成。",
                 choices=[choice("正确答案占位", "token"), choice("错误答案占位", "fail")]),
        ])
    elif "minigame" in info:
        minigame = dict(info["minigame"])
        minigame["successNodeId"] = "token"
        minigame["failureNodeId"] = "fail"
        nodes.append(node("route_action", f"那就现在解决。{info['activity']}这件事，赢了我就给你信物。", minigame=minigame))
    else:
        nodes.append(node("route_action", f"你这轮已经把我这里的关键点问到了。{info['secret']}",
                          choices=[choice("确认完成这条线", "token", 1), choice("先离开", "")]))

    nodes.extend([
        node("token", f"行，这条线算你完成。{info['role']}信物给你。", rewards=[reward(info["token"])]),
        node("fail", "这次不行。你还有别的路线能走，但我这条线暂时不会再往下说。"),
    ])
    return {"startNodeId": "start", "nodes": nodes}


def build_all_dialogs():
    DIALOG_DIR.mkdir(parents=True, exist_ok=True)
    for old in DIALOG_DIR.glob("*.json"):
        old.unlink()
    count = 0
    for phase in PHASES:
        for role_id, info in ROLES.items():
            data = make_teacher_dialog(phase, role_id, info) if role_id == "teacher_yuge" else make_student_dialog(phase, role_id, info)
            (DIALOG_DIR / filename(phase, info)).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            count += 1
    return count


ROLE_BOOK_APPEND = """\n\n## 全阶段可对话规则\n\n现在每个阶段每个 NPC 都有可导入对话。NPC 不需要等到指定阶段才“上线”，而是每个阶段都能提供普通问话、消耗体力推进、特殊物品反应和当前阶段完成路线的机会。\n\n执行原则：\n\n- 主角在任何阶段找到你，你都可以对话。\n- 如果主角没有特殊物品，按普通问话和当前阶段推进走。\n- 如果主角带着你的前置普通道具，直接进入更明确的特殊对白。\n- 如果主角带着其他 NPC 的信物，承认他已经推进过那条线，但只补充你知道的交叉信息。\n- 每条学生线在当前阶段都能推进到信物，不再要求等到后面阶段。\n- 阶段 6 是复盘阶段，不直接发学生信物，只给下一轮路线建议。\n\n## 重新规划后的节奏\n\n- 单线玩家：任何阶段找目标 NPC，都能尝试完成该 NPC 线。\n- 全收集玩家：每阶段 5 点体力，必须选择优先顺序；特殊物品可以减少绕路，但不能替代小游戏或关键选择。\n- NPC 表演：不要说“你必须等到第几阶段再来”，而是根据当前阶段环境换一种说法继续推进。\n"""


def update_role_book():
    GUIDE_DIR.mkdir(parents=True, exist_ok=True)
    path = GUIDE_DIR / "NPC角色书.md"
    text = path.read_text(encoding="utf-8") if path.exists() else "# NPC 角色书第一版\n"
    if "## 全阶段可对话规则" not in text:
        path.write_text(text.rstrip() + ROLE_BOOK_APPEND, encoding="utf-8")


def update_readme():
    text = README.read_text(encoding="utf-8")
    marker = "## 对话导入对应表\n"
    token_marker = "## 信物\n"
    intro = (
        "## 全阶段对话\n\n"
        "当前版本已经改为 6 个阶段每个 NPC 都可对话，共 48 个对话 JSON。"
        "每条学生线在当前阶段都能继续推进或完成；特殊物品只作为捷径和交叉反应，不再是唯一入口。\n\n"
    )
    if "## 全阶段对话" not in text and marker in text:
        text = text.replace(marker, intro + marker)
    if marker in text and token_marker in text:
        rows = ["| 文件 | 角色ID | 阶段 |", "| --- | --- | --- |"]
        for phase in PHASES:
            for role_id, info in ROLES.items():
                rows.append(f"| `{filename(phase, info)}` | `{role_id}` | {phase} |")
        table = marker + "\n" + "\n".join(rows) + "\n\n"
        before = text.split(marker, 1)[0]
        after = text.split(token_marker, 1)[1]
        text = before + table + token_marker + after
    README.write_text(text, encoding="utf-8")


def rebuild_zip():
    with zipfile.ZipFile(ZIP_PATH, "w", zipfile.ZIP_DEFLATED) as archive:
        for root in [README, DIALOG_DIR, MINIGAME_DIR, GUIDE_DIR]:
            if root.is_file():
                archive.write(root, root.relative_to(MOD_DIR).as_posix())
                continue
            for path in sorted(root.rglob("*")):
                if path.is_file():
                    archive.write(path, path.relative_to(MOD_DIR).as_posix())


def main():
    count = build_all_dialogs()
    update_role_book()
    update_readme()
    rebuild_zip()
    print(f"dialogs={count}")
    print("mode=full_phase_all_roles")


if __name__ == "__main__":
    main()
