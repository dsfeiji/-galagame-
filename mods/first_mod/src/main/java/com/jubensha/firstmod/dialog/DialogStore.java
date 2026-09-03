package com.jubensha.firstmod.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public final class DialogStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type OLD_STORE_TYPE = new TypeToken<LinkedHashMap<String, DialogTree>>() {
    }.getType();
    private static final Pattern ROLE_ID_PATTERN = Pattern.compile("[a-z0-9_./-]{1,64}");
    private static final Path STORE_PATH = FabricLoader.getInstance().getConfigDir().resolve("first_mod_dialogs.json");
    private static StoreData data = new StoreData();

    private DialogStore() {
    }

    public static void load() {
        data = new StoreData();
        if (!Files.exists(STORE_PATH)) {
            return;
        }
        try {
            String json = Files.readString(STORE_PATH, StandardCharsets.UTF_8);
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            if (object.has("dialogs")) {
                StoreData loaded = GSON.fromJson(object, StoreData.class);
                data = loaded == null ? new StoreData() : loaded.normalize();
            } else {
                Map<String, DialogTree> oldDialogs = GSON.fromJson(json, OLD_STORE_TYPE);
                if (oldDialogs != null) {
                    oldDialogs.forEach((name, tree) -> data.dialogs
                            .computeIfAbsent(name, ignored -> new LinkedHashMap<>())
                            .put("1", tree.normalize()));
                }
                data.normalize();
                saveAll();
            }
        } catch (IOException | RuntimeException ignored) {
            data = new StoreData();
        }
    }

    public static DialogTree getDialogForCurrentPhase(String roleId) {
        return getDialog(roleId, data.currentPhase);
    }

    public static DialogTree getOrCreateDialogForCurrentPhase(String roleId) {
        return data.dialogs
                .computeIfAbsent(roleId, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(String.valueOf(data.currentPhase), ignored -> DialogTree.defaultTree(roleId))
                .normalize();
    }

    public static void saveDialog(String roleId, int phase, DialogTree tree) {
        int normalizedPhase = normalizePhase(phase);
        data.dialogs
                .computeIfAbsent(roleId, ignored -> new LinkedHashMap<>())
                .put(String.valueOf(normalizedPhase), tree.normalize());
        saveAll();
    }

    public static boolean isValidRoleId(String roleId) {
        return roleId != null && ROLE_ID_PATTERN.matcher(roleId).matches();
    }

    public static void claimRole(UUID playerId, String roleId) {
        data.playerRoles.put(playerId.toString(), roleId);
        saveAll();
    }

    public static void clearRole(UUID playerId) {
        data.playerRoles.remove(playerId.toString());
        saveAll();
    }

    public static String getClaimedRole(UUID playerId) {
        String roleId = data.playerRoles.get(playerId.toString());
        return roleId == null || roleId.isBlank() ? "" : roleId;
    }

    public static int getCurrentPhase() {
        return data.currentPhase;
    }

    public static int getPhaseCount() {
        return data.phaseCount;
    }

    public static void setPhaseCount(int phaseCount) {
        data.phaseCount = Math.max(1, phaseCount);
        data.currentPhase = normalizePhase(data.currentPhase);
        saveAll();
    }

    public static void setCurrentPhase(int phase) {
        data.currentPhase = normalizePhase(phase);
        saveAll();
    }

    public static int nextPhase() {
        data.currentPhase++;
        if (data.currentPhase > data.phaseCount) {
            data.currentPhase = 1;
        }
        saveAll();
        return data.currentPhase;
    }

    private static DialogTree getDialog(String roleId, int phase) {
        Map<String, DialogTree> phases = data.dialogs.get(roleId);
        if (phases == null) {
            return null;
        }
        DialogTree tree = phases.get(String.valueOf(normalizePhase(phase)));
        return tree == null ? null : tree.normalize();
    }

    private static int normalizePhase(int phase) {
        if (phase < 1) {
            return 1;
        }
        if (phase > data.phaseCount) {
            return ((phase - 1) % data.phaseCount) + 1;
        }
        return phase;
    }

    private static void saveAll() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            Files.writeString(STORE_PATH, GSON.toJson(data.normalize()), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static class StoreData {
        int phaseCount = 1;
        int currentPhase = 1;
        Map<String, Map<String, DialogTree>> dialogs = new LinkedHashMap<>();
        Map<String, String> playerRoles = new LinkedHashMap<>();

        StoreData normalize() {
            if (phaseCount < 1) {
                phaseCount = 1;
            }
            if (currentPhase < 1 || currentPhase > phaseCount) {
                currentPhase = normalizePhaseLocal(currentPhase, phaseCount);
            }
            if (dialogs == null) {
                dialogs = new LinkedHashMap<>();
            }
            if (playerRoles == null) {
                playerRoles = new LinkedHashMap<>();
            }
            playerRoles.entrySet().removeIf(entry -> !DialogStore.isValidRoleId(entry.getValue()));
            dialogs.values().forEach(phases -> phases.replaceAll((phase, tree) -> tree.normalize()));
            return this;
        }

        private static int normalizePhaseLocal(int phase, int phaseCount) {
            if (phase < 1) {
                return 1;
            }
            return ((phase - 1) % phaseCount) + 1;
        }
    }
}
