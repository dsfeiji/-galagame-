package com.jubensha.firstmod.roomlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RoomLockStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STORE_TYPE = new TypeToken<Map<String, LockData>>() {
    }.getType();
    private static final Path CONFIG_STORE_PATH = FabricLoader.getInstance().getConfigDir().resolve("first_mod_room_locks.json");
    private static Path storePath = CONFIG_STORE_PATH;
    private static final Map<String, LockData> LOCKS = new LinkedHashMap<>();

    private RoomLockStore() {
    }

    public static void load() {
        LOCKS.clear();
        migrateConfigToWorldStore();
        if (!Files.exists(storePath)) {
            return;
        }
        try {
            String json = Files.readString(storePath);
            Map<String, LockData> loaded = GSON.fromJson(json, STORE_TYPE);
            if (loaded != null) {
                loaded.forEach((key, value) -> {
                    if (value != null) {
                        value.normalize();
                        LOCKS.put(key, value);
                    }
                });
            }
        } catch (RuntimeException | IOException ignored) {
        }
    }

    public static void useWorldDirectory(Path worldDirectory) {
        storePath = worldDirectory.resolve("first_mod").resolve("first_mod_room_locks.json");
        load();
    }

    public static void save() {
        try {
            Files.createDirectories(storePath.getParent());
            Files.writeString(storePath, GSON.toJson(LOCKS, STORE_TYPE));
        } catch (IOException ignored) {
        }
    }

    private static void migrateConfigToWorldStore() {
        if (CONFIG_STORE_PATH.equals(storePath) || Files.exists(storePath) || !Files.exists(CONFIG_STORE_PATH)) {
            return;
        }
        try {
            Files.createDirectories(storePath.getParent());
            Files.copy(CONFIG_STORE_PATH, storePath, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException ignored) {
        }
    }

    public static LockData get(String worldId, BlockPos pos) {
        return LOCKS.get(key(worldId, pos));
    }

    public static void install(String worldId, BlockPos pos) {
        LOCKS.computeIfAbsent(key(worldId, pos), ignored -> new LockData());
        save();
    }

    public static void setRequiredItem(String worldId, BlockPos pos, String itemId) {
        LockData data = LOCKS.computeIfAbsent(key(worldId, pos), ignored -> new LockData());
        data.requiredItem = itemId == null ? "" : itemId;
        save();
    }

    public static void remove(String worldId, BlockPos pos) {
        LOCKS.remove(key(worldId, pos));
        save();
    }

    private static String key(String worldId, BlockPos pos) {
        return worldId + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static class LockData {
        public String requiredItem = "";

        public void normalize() {
            if (requiredItem == null) {
                requiredItem = "";
            }
        }

        public boolean hasRequiredItem() {
            return !requiredItem.isBlank();
        }
    }
}
