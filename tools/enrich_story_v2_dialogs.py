import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MOD_DIR = ROOT / "mods" / "first_mod"
DIALOG_DIR = MOD_DIR / "story_v2_dialogs"
GUIDE_PATH = MOD_DIR / "story_v2_npc_guides" / "NPC角色书.md"
ZIP_PATH = MOD_DIR / "story_v2_json_pack.zip"


def read_json(filename):
    path = DIALOG_DIR / filename
    return path, json.loads(path.read_text(encoding="utf-8"))


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def find_node(data, node_id):
    for entry in data["nodes"]:
        if entry["id"] == node_id:
            return entry
    raise KeyError(node_id)


def upsert_node(data, entry):
    for index, current in enumerate(data["nodes"]):
        if current["id"] == entry["id"]:
            data["nodes"][index] = entry
            return
    data["nodes"].append(entry)


def ensure_choice(node, text, next_id, stamina=0):
    choices = node.setdefault("choices", [])
    for item in choices:
        if item.get("nextNodeId") == next_id:
            item["text"] = text
            if stamina:
                item["staminaCost"] = stamina
            elif "staminaCost" in item:
                item.pop("staminaCost")
            return
    item = {"text": text, "nextNodeId": next_id}
    if stamina:
        item["staminaCost"] = stamina
    choices.insert(max(0, len(choices) - 1), item)


def ensure_jump(node, item, next_id, count=1):
    jumps = node.setdefault("conditionJumps", [])
    for jump in jumps:
        if jump.get("item") == item:
            jump["count"] = count
            jump["nextNodeId"] = next_id
            return
    jumps.append({"item": item, "count": count, "nextNodeId": next_id})


def base_node(node_id, text, next_id="", choices=None, rewards=None, condition_jumps=None, minigame=None):
    result = {
        "id": node_id,
        "text": text,
        "nextNodeId": next_id,
        "conditionJumps": condition_jumps or [],
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


def enrich_bc_phase1():
    path, data = read_json("阶段1_BC_点名开场.json")
    start = find_node(data, "start")
    ensure_jump(start, "minecraft:paper", "paper_roster")
    ensure_jump(start, "first_mod:burnt_note", "transfer_token_react")
    ensure_choice(start, "问点名册为什么被改过", "ask_roster_change")
    ensure_choice(start, "提到王少栋的名字", "mention_transfer")
    upsert_node(data, base_node("paper_roster", "你已经拿到纸条了？那你应该知道名单不是今天早上才乱的。去问王少栋，他比我更清楚空教室。"))
    upsert_node(data, base_node("transfer_token_react", "你连王少栋那边的东西都拿到了？那我没必要再替他遮掩。午休前柜子区会空出来一小段时间。"))
    upsert_node(data, base_node("ask_roster_change", "名单被改过，但改的人很懂班里的顺序。不是外人随便写两笔能做到的。"))
    upsert_node(data, base_node("mention_transfer", "别把所有问题都推给转学生。王少栋可疑，但盯着他的人也可疑。"))
    write_json(path, data)


def enrich_teacher_phase1():
    path, data = read_json("阶段1_雨哥_校门开场.json")
    start = find_node(data, "start")
    ensure_choice(start, "问为什么今天管得这么严", "strict_reason")
    ensure_choice(start, "问放学前能不能进办公室", "office_rule")
    upsert_node(data, base_node("strict_reason", "昨天已经有人在空教室和广播室之间乱跑了。今天我不想再看到这种情况。"))
    upsert_node(data, base_node("office_rule", "办公室不是你想进就进。等到检查阶段，有足够理由再来找我。"))
    write_json(path, data)


def enrich_goutou():
    path, data = read_json("阶段2_狗头_器材室决胜.json")
    start = find_node(data, "start")
    ensure_jump(start, "first_mod:chipped_attendance_tag", "bc_token_shortcut")
    ensure_jump(start, "minecraft:note_block", "broadcast_called")
    ensure_choice(start, "追问谁借走过护腕", "ask_wristband")
    ensure_choice(start, "用广播时间逼他离开", "use_broadcast", 1)
    upsert_node(data, base_node("bc_token_shortcut", "BC 都放你过来了？那你不是随便乱跑。说吧，器材室你想查哪一边？",
                                choices=[choice("检查护腕架", "token", 1), choice("先观察器材室", "observe")]))
    upsert_node(data, base_node("broadcast_called", "广播室那边开始催人了。我只能给你一点时间，想查就快。"))
    upsert_node(data, base_node("ask_wristband", "护腕不是自己丢的。有人拿走后又塞回来，位置还放反了。"))
    upsert_node(data, base_node("use_broadcast", "行，我去操场集合。你别把器材室翻得太明显。", rewards=[reward("minecraft:string")]))
    write_json(path, data)


def enrich_liugu():
    path, data = read_json("阶段3_六谷_知识问答.json")
    start = find_node(data, "start")
    ensure_jump(start, "first_mod:chipped_attendance_tag", "attendance_logic")
    ensure_jump(start, "first_mod:cracked_phone_charm", "rich_exam_hint")
    ensure_jump(start, "minecraft:paper", "rough_note")
    ensure_choice(start, "问满分试卷为什么重要", "exam_reason")
    ensure_choice(start, "请六谷帮你整理顺序", "order_help", 1)
    upsert_node(data, base_node("attendance_logic", "BC 的点名牌能证明时间被人动过。你如果要问答案，就从“谁有时间差”开始想。"))
    upsert_node(data, base_node("rich_exam_hint", "孙悟空的东西和试卷放在一起？那就不是普通炫耀了，他可能在换什么。"))
    upsert_node(data, base_node("rough_note", "这张纸不是答案，是草稿。草稿能证明有人提前知道题目顺序。"))
    upsert_node(data, base_node("exam_reason", "满分试卷重要，不是因为分数，而是因为它能证明谁提前看过题。"))
    upsert_node(data, base_node("order_help", "我只能帮你排除一个错误顺序：先去广播室再找随小乐，通常会晚。", rewards=[reward("minecraft:book")]))
    write_json(path, data)


def enrich_suixiaole():
    path, data = read_json("阶段3_随小乐_记忆证词.json")
    start = find_node(data, "start")
    ensure_jump(start, "first_mod:stained_paintbrush", "broadcast_memory")
    ensure_jump(start, "first_mod:burnt_note", "transfer_memory")
    ensure_jump(start, "minecraft:string", "string_memory")
    ensure_choice(start, "问她听到广播前看见了什么", "before_broadcast")
    ensure_choice(start, "把纸条递给她看", "show_note")
    upsert_node(data, base_node("broadcast_memory", "这个广播室的东西我见过。广播响之前，走廊上有人突然停了一下，好像在等信号。"))
    upsert_node(data, base_node("transfer_memory", "王少栋的那张东西……我好像在空教室门缝下面见过同样的烧痕。"))
    upsert_node(data, base_node("string_memory", "你拿着这个我更害怕了。楼梯扶手那里也缠过类似的线。"))
    upsert_node(data, base_node("before_broadcast", "广播前我听见柜子那边响了一下，然后有人从空教室方向走出来。"))
    upsert_node(data, base_node("show_note", "纸条上的字我不敢看完，但最后一个方向一定不是操场。"))
    write_json(path, data)


def enrich_sunwukong():
    path, data = read_json("阶段4_孙悟空_抢柜子.json")
    start = find_node(data, "start")
    ensure_jump(start, "first_mod:broken_exam_paper", "scholar_pressure")
    ensure_jump(start, "first_mod:stained_paintbrush", "broadcast_deal")
    ensure_jump(start, "minecraft:emerald", "emerald_deal")
    ensure_choice(start, "问他为什么盯着王少栋", "ask_transfer")
    ensure_choice(start, "拿试卷线索压他", "press_exam", 1)
    upsert_node(data, base_node("scholar_pressure", "你连六谷那张破试卷都拿到了？那我就不装了，柜子里的东西确实和它有关。",
                                choices=[choice("要求直接开柜", "locker_game", 1), choice("继续谈交易", "deal")]))
    upsert_node(data, base_node("broadcast_deal", "赵子龙那边已经被你说动了？可以，那我的条件也得变。你帮我挡一次广播，我给你柜子机会。"))
    upsert_node(data, base_node("emerald_deal", "还带着绿宝石？看来你记得交易。柜子区这次我可以让你先手。",
                                choices=[choice("开始抢柜子", "locker_game"), choice("离开", "")]))
    upsert_node(data, base_node("ask_transfer", "王少栋不像刚来的。他找柜子的速度，比很多老学生都熟。"))
    upsert_node(data, base_node("press_exam", "别拿试卷吓我。我可以让你查柜子，但你输了就别再提这件事。", next_id="locker_game"))
    write_json(path, data)


def enrich_zhaozilong():
    path, data = read_json("阶段4_赵子龙_广播节奏.json")
    start = find_node(data, "start")
    ensure_jump(start, "first_mod:cracked_phone_charm", "rich_contact")
    ensure_jump(start, "first_mod:crumpled_witness_note", "witness_broadcast")
    ensure_jump(start, "minecraft:map", "route_broadcast")
    ensure_choice(start, "问广播能调走谁", "ask_move")
    ensure_choice(start, "要求广播误导巡查", "mislead_patrol", 1)
    upsert_node(data, base_node("rich_contact", "孙悟空的东西在你手上？他果然还是把柜子区牵进来了。你想播什么，先说明后果。"))
    upsert_node(data, base_node("witness_broadcast", "随小乐的证词对得上。广播前后那一分钟，确实有人不在原位。"))
    upsert_node(data, base_node("route_broadcast", "你有路线图，那就知道广播不是答案，只是让人离开原位的办法。"))
    upsert_node(data, base_node("ask_move", "狗头会去操场，BC 会往走廊，雨哥会看办公室。你只能利用一次。"))
    upsert_node(data, base_node("mislead_patrol", "我可以把 BC 调去另一条走廊，但你得保证这次不浪费。", rewards=[reward("minecraft:redstone")]))
    write_json(path, data)


def enrich_wangshaodong():
    path, data = read_json("阶段4_王少栋_空教室对抗.json")
    start = find_node(data, "start")
    ensure_jump(start, "first_mod:chipped_attendance_tag", "roster_exposed")
    ensure_jump(start, "first_mod:crumpled_witness_note", "witness_exposed")
    ensure_jump(start, "first_mod:broken_exam_paper", "exam_exposed")
    ensure_choice(start, "问他为什么熟悉空教室", "ask_empty_room")
    ensure_choice(start, "把随小乐的证词摆出来", "show_witness", 1)
    upsert_node(data, base_node("roster_exposed", "BC 连点名牌都给你了？那我的名字什么时候出现，你应该已经知道了。"))
    upsert_node(data, base_node("witness_exposed", "随小乐看见我了？她不该看见的。既然这样，我们没必要继续装。",
                                choices=[choice("开始空教室对抗", "locker_game", 1), choice("追问他的目的", "ask_empty_room")]))
    upsert_node(data, base_node("exam_exposed", "试卷也在你手里？那你应该知道，我找的不是分数，是藏在试卷里的顺序。"))
    upsert_node(data, base_node("ask_empty_room", "我不是熟悉空教室，我是熟悉没人会检查的地方。"))
    upsert_node(data, base_node("show_witness", "她说得太多了。既然你已经拼到这里，就用速度决定东西归谁。", next_id="locker_game"))
    write_json(path, data)


def enrich_teacher_phase5():
    path, data = read_json("阶段5_雨哥_最终检查.json")
    start = find_node(data, "start")
    ensure_jump(start, "minecraft:book", "prepared_review")
    ensure_jump(start, "minecraft:redstone", "broadcast_proof")
    upsert_node(data, base_node("prepared_review", "你带着整理好的资料来，说明这一轮不是乱撞。现在开始逐个检查信物。", next_id="check_monitor"))
    upsert_node(data, base_node("broadcast_proof", "广播误导的证据也在？那我会按完整流程查，不会只听某一个人的说法。", next_id="check_monitor"))
    missing = find_node(data, "missing")
    ensure_choice(missing, "询问还缺什么方向", "missing_hint")
    upsert_node(data, base_node("missing_hint", "你缺的不是一句话，是顺序。回想谁需要前置道具，谁必须在午休前解决。"))
    write_json(path, data)


def enrich_phase6():
    path, data = read_json("阶段6_雨哥_循环复盘.json")
    start = find_node(data, "start")
    ensure_jump(start, "minecraft:nether_star", "teacher_complete")
    ensure_jump(start, "first_mod:burnt_note", "partial_transfer")
    ensure_choice(start, "复盘哪些道具能保留", "keep_items")
    upsert_node(data, base_node("teacher_complete", "老师信物已经在你手里了。下一次循环，你要考虑的是怎么把所有人安排到同一个时间点。"))
    upsert_node(data, base_node("partial_transfer", "你至少摸到了王少栋这条线。下次可以先从空教室和柜子区切入。"))
    upsert_node(data, base_node("keep_items", "信物会重置，但纸、书、地图、绿宝石这类普通道具可以作为下一轮的准备。"))
    write_json(path, data)


def update_npc_book():
    addition = """\n\n## 特殊物品触发对话表\n\n这些内容是给 NPC 玩家提前知道的。主角背包里有对应物品时，对话 JSON 会自动跳到特殊对白，NPC 要顺着这个信息继续演。\n\n| 触发对象 | 特殊物品 | 表演含义 |\n| --- | --- | --- |\n| BC | `minecraft:paper` | 主角已经拿到点名相关纸条，BC 可以承认名单不是今天才乱。 |\n| BC | `first_mod:burnt_note` | 主角已经推进王少栋线，BC 可以透露午休柜子区空档。 |\n| 狗头 | `first_mod:chipped_attendance_tag` | BC 线已推进，狗头认可主角不是乱跑。 |\n| 狗头 | `minecraft:note_block` | 广播已影响操场时间，狗头会短暂离开。 |\n| 六谷 | `first_mod:chipped_attendance_tag` | 点名顺序和时间差可以进入逻辑推理。 |\n| 六谷 | `first_mod:cracked_phone_charm` | 孙悟空线和试卷线产生联系。 |\n| 随小乐 | `first_mod:stained_paintbrush` | 广播前后的目击记忆被唤起。 |\n| 随小乐 | `first_mod:burnt_note` | 王少栋线与空教室门缝线索相连。 |\n| 孙悟空 | `first_mod:broken_exam_paper` | 六谷线已推进，可以用试卷压孙悟空。 |\n| 孙悟空 | `minecraft:emerald` | 主角带着交易道具，可进入先手机会。 |\n| 赵子龙 | `first_mod:crumpled_witness_note` | 随小乐证词证明广播前后有人不在原位。 |\n| 赵子龙 | `minecraft:map` | 主角知道路线图，广播只负责调动人流。 |\n| 王少栋 | `first_mod:crumpled_witness_note` | 随小乐证词指向王少栋，王少栋不再装作完全无辜。 |\n| 王少栋 | `first_mod:broken_exam_paper` | 试卷顺序和王少栋寻找的东西有关。 |\n| 雨哥 | `minecraft:book` | 主角带着整理资料，最终检查更像正式复盘。 |\n| 雨哥 | `minecraft:redstone` | 广播误导证据成立，进入完整检查流程。 |\n| 雨哥阶段6 | `minecraft:nether_star` | 老师信物已获得，进入完成后复盘。 |\n\n## NPC 衔接建议\n\n- 如果主角拿着别人的信物来找你，不要装作什么都没发生；可以承认“你已经找过他了”，但仍然只说本角色知道的部分。\n- 如果主角带着普通道具来找你，比如纸、书、地图、绿宝石，可以把它当成“上一轮或前一阶段的准备”，给出更短路线或更明确暗示。\n- 如果主角没有前置物品，就按普通对白走，不要主动补齐特殊分支的信息。\n"""
    GUIDE_PATH.parent.mkdir(parents=True, exist_ok=True)
    text = GUIDE_PATH.read_text(encoding="utf-8") if GUIDE_PATH.exists() else ""
    if "## 特殊物品触发对话表" not in text:
        GUIDE_PATH.write_text(text.rstrip() + addition + "\n", encoding="utf-8")


def rebuild_zip():
    include_roots = [
        MOD_DIR / "story_v2_README.md",
        MOD_DIR / "story_v2_dialogs",
        MOD_DIR / "story_v2_minigames",
        MOD_DIR / "story_v2_npc_guides",
    ]
    with zipfile.ZipFile(ZIP_PATH, "w", zipfile.ZIP_DEFLATED) as archive:
        for root in include_roots:
            if root.is_file():
                archive.write(root, root.relative_to(MOD_DIR).as_posix())
            else:
                for path in sorted(root.rglob("*")):
                    if path.is_file():
                        archive.write(path, path.relative_to(MOD_DIR).as_posix())


def enrich_dialogs():
    enrich_teacher_phase1()
    enrich_bc_phase1()
    enrich_goutou()
    enrich_liugu()
    enrich_suixiaole()
    enrich_sunwukong()
    enrich_zhaozilong()
    enrich_wangshaodong()
    enrich_teacher_phase5()
    enrich_phase6()
    update_npc_book()


def main():
    enrich_dialogs()
    rebuild_zip()
    print("enriched_dialog_files=10")
    print("zip=story_v2_json_pack.zip")


if __name__ == "__main__":
    main()
