package com.jubensha.firstmod.elimination;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jubensha.firstmod.dialog.DialogStore;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EliminationStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STORE_TYPE = new TypeToken<LinkedHashMap<String, String>>() {
    }.getType();
    private static final Path STORE_PATH = FabricLoader.getInstance().getConfigDir().resolve("first_mod_eliminations.json");
    private static final Map<String, String> eliminatedRoles = new LinkedHashMap<>();

    private EliminationStore() {
    }

    public static void load() {
        eliminatedRoles.clear();
        if (!Files.exists(STORE_PATH)) {
            return;
        }
        try {
            Map<String, String> loaded = GSON.fromJson(Files.readString(STORE_PATH, StandardCharsets.UTF_8), STORE_TYPE);
            if (loaded != null) {
                loaded.forEach((roleId, reason) -> {
                    if (DialogStore.isValidRoleId(roleId)) {
                        eliminatedRoles.put(roleId, reason == null ? "" : reason);
                    }
                });
            }
        } catch (IOException | RuntimeException ignored) {
            eliminatedRoles.clear();
        }
    }

    public static boolean isEliminated(String roleId) {
        return roleId != null && eliminatedRoles.containsKey(roleId);
    }

    public static String getReason(String roleId) {
        String reason = eliminatedRoles.get(roleId);
        return reason == null ? "" : reason;
    }

    public static void eliminate(String roleId, String reason) {
        if (!DialogStore.isValidRoleId(roleId)) {
            return;
        }
        eliminatedRoles.put(roleId, reason == null ? "" : reason);
        saveAll();
    }

    public static void revive(String roleId) {
        if (roleId != null && eliminatedRoles.remove(roleId) != null) {
            saveAll();
        }
    }

    public static void reset() {
        eliminatedRoles.clear();
        saveAll();
    }

    public static Map<String, String> all() {
        return new LinkedHashMap<>(eliminatedRoles);
    }

    private static void saveAll() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            Files.writeString(STORE_PATH, GSON.toJson(eliminatedRoles), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
