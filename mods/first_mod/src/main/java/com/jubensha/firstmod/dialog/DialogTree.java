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
            for (ItemConditionJump conditionJump : conditionJumps) {
                conditionJump.normalize();
            }
            for (ItemReward reward : rewards) {
                reward.normalize();
            }
            for (DialogChoice choice : choices) {
                choice.normalize();
            }
        }
    }

    public static class DialogChoice {
        public String text = "";
        public String nextNodeId = "";

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
}
