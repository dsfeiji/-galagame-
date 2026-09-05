package com.jubensha.firstmod.minigame;

import com.jubensha.firstmod.dialog.DialogTree;

import java.util.ArrayList;
import java.util.List;

public class MinigameInteraction {
    public String id = "";
    public boolean protagonistOnly = true;
    public int staminaCost = 0;
    public Trigger trigger = new Trigger();
    public DialogTree.DialogMinigame minigame = new DialogTree.DialogMinigame();
    public Result success = new Result();
    public Result failure = new Result();

    public MinigameInteraction normalize() {
        if (id == null) {
            id = "";
        }
        if (staminaCost < 0) {
            staminaCost = 0;
        }
        if (trigger == null) {
            trigger = new Trigger();
        }
        trigger.normalize();
        if (minigame == null) {
            minigame = new DialogTree.DialogMinigame();
        }
        minigame.normalize();
        if (success == null) {
            success = new Result();
        }
        success.normalize();
        if (failure == null) {
            failure = new Result();
        }
        failure.normalize();
        return this;
    }

    public static class Trigger {
        public String type = "use_block";
        public String block = "";
        public String item = "";
        public int phase = 0;
        public List<Integer> phases = new ArrayList<>();
        public String world = "";
        public Integer x = null;
        public Integer y = null;
        public Integer z = null;

        public void normalize() {
            if (type == null || type.isBlank()) {
                type = "use_block";
            }
            if (block == null) {
                block = "";
            }
            if (item == null) {
                item = "";
            }
            if (phase < 0) {
                phase = 0;
            }
            if (phases == null) {
                phases = new ArrayList<>();
            }
            phases.removeIf(value -> value == null || value < 1);
            if (world == null) {
                world = "";
            }
        }
    }

    public static class Result {
        public String message = "";
        public List<DialogTree.ItemReward> rewards = new ArrayList<>();
        public String eliminateRole = "";
        public String eliminateReason = "";
        public boolean eliminateSelf = false;

        public void normalize() {
            if (message == null) {
                message = "";
            }
            if (rewards == null) {
                rewards = new ArrayList<>();
            }
            for (DialogTree.ItemReward reward : rewards) {
                reward.normalize();
            }
            if (eliminateRole == null) {
                eliminateRole = "";
            }
            if (eliminateReason == null) {
                eliminateReason = "";
            }
        }
    }
}
