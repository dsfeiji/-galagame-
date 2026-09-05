package com.jubensha.firstmod.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class DialogTree {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String startNodeId = "start";
    public List<DialogNode> nodes = new ArrayList<>();

    public static DialogTree defaultTree(String targetName) {
        DialogTree tree = new DialogTree();
        DialogNode start = new DialogNode();
        start.id = "start";
        start.text = "You are talking to " + targetName + ".";
        start.nextNodeId = "";
        tree.nodes.add(start);
        return tree;
    }

    public static DialogTree fromJson(String json) {
        try {
            return fromJsonStrict(json);
        } catch (RuntimeException ignored) {
            return new DialogTree().normalize();
        }
    }

    public static DialogTree fromJsonStrict(String json) {
        DialogTree tree = GSON.fromJson(json, DialogTree.class);
        if (tree == null) {
            throw new IllegalArgumentException("empty dialog tree");
        }
        return tree.normalize();
    }

    public String toJson() {
        return GSON.toJson(normalize());
    }

    public DialogTree normalize() {
        if (startNodeId == null || startNodeId.isBlank()) {
            startNodeId = "start";
        }
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        if (nodes.isEmpty()) {
            DialogNode start = new DialogNode();
            start.id = startNodeId;
            start.text = "";
            nodes.add(start);
        }
        for (DialogNode node : nodes) {
            node.normalize();
        }
        if (getNode(startNodeId) == null) {
            startNodeId = nodes.getFirst().id;
        }
        return this;
    }

    public DialogNode getNode(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        for (DialogNode node : nodes) {
            if (nodeId.equals(node.id)) {
                return node;
            }
        }
        return null;
    }

    public boolean hasNode(String nodeId) {
        return getNode(nodeId) != null;
    }

    public static class DialogNode {
        public String id = "";
        public String text = "";
        public String nextNodeId = "";
        public List<ItemConditionJump> conditionJumps = new ArrayList<>();
        public List<ItemReward> rewards = new ArrayList<>();
        public List<DialogChoice> choices = new ArrayList<>();
        public DialogMinigame minigame = null;
        public String eliminateRole = "";
        public String eliminateReason = "";
        public boolean eliminateTarget = false;

        public void normalize() {
            if (id == null || id.isBlank()) {
                id = "node";
            }
            if (text == null) {
                text = "";
            }
            if (nextNodeId == null) {
                nextNodeId = "";
            }
            if (conditionJumps == null) {
                conditionJumps = new ArrayList<>();
            }
            if (rewards == null) {
                rewards = new ArrayList<>();
            }
            if (choices == null) {
                choices = new ArrayList<>();
            }
            if (eliminateRole == null) {
                eliminateRole = "";
            }
            if (eliminateReason == null) {
                eliminateReason = "";
            }
            for (ItemConditionJump conditionJump : conditionJumps) {
                conditionJump.normalize();
            }
            for (ItemReward reward : rewards) {
                reward.normalize();
            }
            for (DialogChoice choice : choices) {
                choice.normalize();
            }
            if (minigame != null) {
                minigame.normalize();
                if (minigame.type.isBlank()) {
                    minigame = null;
                }
            }
        }
    }

    public static class DialogChoice {
        public String text = "";
        public String nextNodeId = "";
        public int staminaCost = 0;

        public DialogChoice() {
        }

        public DialogChoice(String text, String nextNodeId) {
            this.text = text;
            this.nextNodeId = nextNodeId;
        }

        public void normalize() {
            if (text == null) {
                text = "";
            }
            if (nextNodeId == null) {
                nextNodeId = "";
            }
            if (staminaCost < 0) {
                staminaCost = 0;
            }
        }
    }

    public static class ItemConditionJump {
        public String item = "";
        public int count = 1;
        public String nextNodeId = "";

        public void normalize() {
            if (item == null) {
                item = "";
            }
            if (count < 1) {
                count = 1;
            }
            if (nextNodeId == null) {
                nextNodeId = "";
            }
        }
    }

    public static class ItemReward {
        public String item = "";
        public int count = 1;

        public void normalize() {
            if (item == null) {
                item = "";
            }
            if (count < 1) {
                count = 1;
            }
        }
    }

    public static class DialogMinigame {
        public String type = "";
        public String title = "";
        public int difficulty = 2;
        public float speed = 0.78F;
        public float successStart = -1.0F;
        public float successWidth = -1.0F;
        public int durationTicks = 100;
        public float opponentAutoClicksPerSecond = 5.5F;
        public float opponentAccuracy = 0.65F;
        public float pushPerClick = 0.06F;
        public float winProgress = 1.0F;
        public int gridSize = 9;
        public int targetIndex = -1;
        public int previewTicks = 40;
        public int rounds = 5;
        public int winClickLead = 1;
        public String successNodeId = "";
        public String failureNodeId = "";

        public void normalize() {
            if (type == null) {
                type = "";
            }
            if (title == null) {
                title = "";
            }
            if (difficulty < 1) {
                difficulty = 1;
            }
            if (difficulty > 4) {
                difficulty = 4;
            }
            if ("rhythm_duel".equals(type) && speed < 1.8F) {
                speed = 1.8F;
            } else if (speed <= 0.0F) {
                speed = 0.78F;
            }
            if (successStart > 1.0F) {
                successStart = 1.0F;
            }
            if (successWidth > 1.0F) {
                successWidth = 1.0F;
            }
            if (isScoreDuelType(type) && durationTicks < 1200) {
                durationTicks = 1200;
            } else if (durationTicks < 20) {
                durationTicks = 20;
            }
            if (durationTicks > 1200) {
                durationTicks = 1200;
            }
            if (opponentAutoClicksPerSecond < 0.0F) {
                opponentAutoClicksPerSecond = 0.0F;
            }
            if (opponentAccuracy < 0.0F) {
                opponentAccuracy = 0.0F;
            }
            if (opponentAccuracy > 1.0F) {
                opponentAccuracy = 1.0F;
            }
            if (pushPerClick <= 0.0F) {
                pushPerClick = 0.06F;
            }
            if (pushPerClick > 0.5F) {
                pushPerClick = 0.5F;
            }
            if (winProgress <= 0.1F) {
                winProgress = 1.0F;
            }
            if (winProgress > 1.0F) {
                winProgress = 1.0F;
            }
            if ("memory_flip_duel".equals(type)) {
                gridSize = 15;
            } else if (gridSize < 4) {
                gridSize = 4;
            }
            if (gridSize > 16) {
                gridSize = 16;
            }
            if (targetIndex >= gridSize) {
                targetIndex = gridSize - 1;
            }
            if (previewTicks < 10) {
                previewTicks = 10;
            }
            if (previewTicks > durationTicks) {
                previewTicks = Math.max(10, durationTicks / 2);
            }
            if (rounds < 1) {
                rounds = 1;
            }
            if ("rhythm_duel".equals(type) && rounds < 60) {
                rounds = 60;
            }
            if (rounds > 200) {
                rounds = 200;
            }
            if (winClickLead < 1) {
                winClickLead = 1;
            }
            if (successNodeId == null) {
                successNodeId = "";
            }
            if (failureNodeId == null) {
                failureNodeId = "";
            }
        }

        private boolean isScoreDuelType(String type) {
            return "locker_search_duel".equals(type) || "rhythm_duel".equals(type) || "memory_flip_duel".equals(type);
        }
    }
}
