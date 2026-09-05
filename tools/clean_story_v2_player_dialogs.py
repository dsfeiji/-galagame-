import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DIALOG_DIR = ROOT / "mods" / "first_mod" / "story_v2_dialogs"
GUIDE_DIR = ROOT / "mods" / "first_mod" / "story_v2_npc_guides"
README = ROOT / "mods" / "first_mod" / "story_v2_README.md"


TEXTS = {
    "阶段1_雨哥_校门开场.json": {
        "start": "都到校门口了，今天按学校日程走。想找谁说话就快一点，别耽误集合。",
        "schedule": "先早自习，再课间、操场、午休、办公室检查，最后集合。别问太多，今天事情不会少。",
        "early_classroom": "行，你先进去看一眼。记住，只能看，别乱碰教室里的东西。",
    },
    "阶段1_BC_点名开场.json": {
        "start": "你来得正好。我在核点名册，别挡门口。有什么事快说。",
        "help_roster": "名单有点乱。王少栋是刚加进来的，随小乐今天也一直不敢看人。",
        "ask_unusual": "反常的人不止一个。王少栋、随小乐，还有广播室那边，我都觉得不对。",
    },
    "阶段1_狗头_操场热身.json": {
        "start": "别光站着。有话就说，没话就让开，我还要热身。",
        "arm_game": "想让我认真听你说？那先来比一下。",
        "arm_success": "可以，你有点东西。器材室今天确实被人借用过。",
        "arm_failure": "就这点力气？想问我话，先练练再来。",
        "equipment": "器材室少过东西，绳子、球网、护腕都有人动过。",
    },
    "阶段1_六谷_知识问答开场.json": {
        "start": "你想从我这里拿信息，就先证明你不是乱猜。",
        "q1": "第一题内容待填写。选错的话，我就当你是在套话。",
        "q2": "第二题内容待填写。这一题答对，我才给你看资料。",
        "quiz_success": "可以，你至少跟得上逻辑。这份资料你拿去看。",
        "fail": "不对。你不是在推理，只是在碰运气。",
        "materials": "有一份满分试卷被损坏了。BC 和孙悟空都关注过它。",
    },
    "阶段1_随小乐_楼梯目击.json": {
        "start": "你别突然靠这么近……我只是路过，真的只是路过。",
        "comfort": "广播响起来之前，我好像看见有人从空教室那边出来。",
        "pressure": "别问了……柜子区，你去柜子区看看，别说是我说的。",
    },
    "阶段1_孙悟空_柜子交易.json": {
        "start": "想问事？可以。但我这里没有免费的消息。",
        "deal": "这算我先给你的好处。以后需要你帮我保住面子的时候，别装不认识。",
        "locker": "王少栋一直盯着某个柜子，赵子龙也来过。你自己想这是什么意思。",
    },
    "阶段1_赵子龙_广播安排.json": {
        "start": "广播不是谁想用就能用的。你要问安排，先说清楚你想干什么。",
        "broadcast_plan": "广播会影响集合时间。只要时间错开，人就会去错地方。",
        "rhythm_game": "想让我帮你插播？那就看你跟不跟得上节奏。",
        "rhythm_success": "行，我可以在关键时候帮你插一次广播。",
        "rhythm_failure": "你这个节奏太乱了。广播交给你，只会坏事。",
    },
    "阶段1_王少栋_转学生路线.json": {
        "start": "我刚转来，不太熟。你一直看我干什么？",
        "ask_locker": "我只是找错柜子了。你别把普通事情想得太复杂。",
        "route": "空教室在那边。别问我为什么知道，我不想解释。",
    },
    "阶段2_BC_巡查记忆.json": {
        "start": "课间别乱跑。我现在开始巡查，你最好有正当理由。",
        "has_route": "你知道空教室那条路？那我更不能随便放你过去。",
        "memory_game": "你说你记得清楚，那就证明给我看。",
        "token": "这次算你赢。这个给你，别让我后悔。",
        "locked": "不行，你的说法对不上。我会盯紧这条走廊。",
        "soft_request": "午休前柜子区会有一小段空档。其他的别问。",
    },
    "阶段2_狗头_器材室决胜.json": {
        "start": "器材室这边别乱碰。你要真想插手，就拿出点本事。",
        "trusted": "你上次赢过，我记得。说吧，你想让我怎么配合？",
        "arm_game": "废话少说，再比一次。",
        "token": "行，服了。这个给你，器材室的事我会按你说的做。",
        "observe": "你现在只能看，别想直接拿走东西。",
    },
    "阶段3_六谷_知识问答.json": {
        "start": "现在轮到关键题了。答不出来，就别继续追问。",
        "prepared": "你带了资料？那我直接问最后一题。",
        "q1": "第一题内容待填写。认真选。",
        "q2": "最后一题内容待填写。答对，这条线就结束。",
        "token": "答案成立。信物给你，别浪费这条线索。",
        "fail": "错了。本轮到此为止。",
    },
    "阶段3_随小乐_记忆证词.json": {
        "start": "我记不太清了……但如果一定要说，我可以试着回忆。",
        "pressured": "你又来问这个。我真的很怕，但我知道你不会停下。",
        "memory_game": "我只记得几个画面。你帮我把顺序拼出来。",
        "token": "就是这样……我想起来了。这个你拿走吧。",
        "fail": "不对，不是这个顺序。我越想越乱，别再问了。",
    },
    "阶段4_孙悟空_抢柜子.json": {
        "start": "午休时间最适合谈条件。你想拿什么，就看你抢不抢得到。",
        "has_money": "带了东西来？那就别绕弯子，直接开始。",
        "locker_game": "柜子里的东西，谁先翻到算谁的。",
        "token": "你赢了。东西归你，信物也给你。",
        "fail": "慢了。柜子里的东西已经不在这里了。",
        "deal": "这颗绿宝石先给你。别以为这就算两清。",
    },
    "阶段4_赵子龙_广播节奏.json": {
        "start": "广播室现在归我管。你要抢时间，就得抢过我的节奏。",
        "trusted": "我记得你之前赢过。给我一个理由，我就帮你播。",
        "rhythm_game": "准备好，跟上每一个节拍。",
        "token": "你抢到节奏了。广播这次听你的。",
        "fail": "时机过了。现在播什么都晚了。",
        "request": "我可以给你一次普通提醒，但别指望我替你完成整件事。",
    },
    "阶段4_王少栋_空教室对抗.json": {
        "start": "你跟到这里，是不是已经知道了什么？",
        "route_known": "你拿着那张路线图……看来我瞒不住了。",
        "locker_game": "想知道我藏了什么？那就看谁更快。",
        "token": "你找到了。信物给你，但别以为你已经知道全部。",
        "fail": "太慢了。我已经把重要的东西转走了。",
        "talk": "我承认我知道空教室，但原因现在不能告诉你。",
    },
    "阶段5_雨哥_最终检查.json": {
        "start": "到这里就别再绕了。把你这一轮拿到的东西给我看。",
        "check_monitor": "先看 BC 的信物。",
        "check_athlete": "再看狗头的信物。",
        "check_scholar": "六谷的信物呢？",
        "check_timid": "随小乐的信物也要有。",
        "check_rich": "孙悟空那边，你处理完了吗？",
        "check_broadcaster": "赵子龙的信物拿出来。",
        "check_transfer": "最后，王少栋的信物。",
        "complete": "七个都齐了。你这一轮确实走到底了，这个给你。",
        "missing": "还不够。少一个都不算完成，下一轮重新安排顺序。",
    },
    "阶段6_雨哥_循环复盘.json": {
        "start": "这一轮结束。你可以记住教训，但信物不会跟你去下一轮。",
        "hint": "每阶段只有五点体力。想全收集，就先准备能保留的道具，再安排顺序。",
    },
}


GUIDE = """# NPC 扮演手册草稿

这个文件给你后面写成游戏内书本用。导入游戏的对话 JSON 里只放玩家能看到的说话内容；下面这些是 NPC 玩家需要提前知道的控场信息。

## 通用规则

- NPC 可以自由发挥语气、停顿和态度，但不能改变 JSON 里的奖励、体力消耗、小游戏胜负和阶段规则。
- 不要直接说完整通关流程，只能给本角色掌握的信息。
- 信物只在本轮结算有效；部分普通道具可以跨循环保留。
- 不要使用 Minecraft 里无法确认的线索，例如气味、心理旁白、玩家看不到的细节。
- 主角推动全局阶段，其他人可以参与表演和小游戏，但不能代替主角推进剧情。

## 雨哥

负责学校日程、阶段切换和最终结算。开场时只说明流程，不透露谁会出事。第五阶段检查七个学生信物，齐全才给老师信物。

## BC

负责点名、巡查和纪律压力。可以怀疑别人，但不要直接指出最终答案。主要把主角引向王少栋、随小乐、广播室和柜子区。

## 狗头

用比赛决定信任程度。态度直接，少解释。扳手腕赢了才给更多信息，输时可以嘲讽但不能封死其他路线。

## 六谷

学霸线用普通对话做知识问答。题目由作者自己填。答对给资料或信物，答错本轮锁线。不要替玩家解释正确答案。

## 随小乐

知道目击片段，但害怕卷入。说话可以犹豫、断续，只给地点和顺序线索，不要一次说完全部事实。

## 孙悟空

用交易和资源说话。可以要主角帮忙保面子，可以给可保留的资源道具，但信物只能在完成柜子对抗后给。

## 赵子龙

控制广播室和集合节奏。广播能制造错位，但不能直接完成路线。节奏对抗成功后才给关键广播帮助。

## 王少栋

转学生，不熟悉表面规则，但知道隐藏路线。要表现得谨慎，不主动解释为什么知道空教室和柜子区。
"""


def clean_dialogs():
    missing = []
    for filename, replacements in TEXTS.items():
        path = DIALOG_DIR / filename
        if not path.exists():
            missing.append(filename)
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        for node in data.get("nodes", []):
            if node.get("id") in replacements:
                node["text"] = replacements[node["id"]]
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if missing:
        raise FileNotFoundError("Missing dialog files: " + ", ".join(missing))


def write_guide():
    from create_story_v2_npc_books import write_npc_book

    write_npc_book()


def update_readme():
    text = README.read_text(encoding="utf-8")
    marker = "## 信物\n"
    insert = (
        "## NPC 扮演提示\n\n"
        "- 对话 JSON 的 `text` 字段只放玩家能看到的说话内容。\n"
        "- NPC 控场、信息边界和自由发挥规则放在 `story_v2_npc_guides/NPC角色书.md`。\n\n"
    )
    if insert.strip() not in text and marker in text:
        text = text.replace(marker, insert + marker)
    README.write_text(text, encoding="utf-8")


def main():
    clean_dialogs()
    write_guide()
    update_readme()
    print("cleaned_dialog_files", len(TEXTS))
    print("guide", GUIDE_DIR / "NPC角色书.md")


if __name__ == "__main__":
    main()
