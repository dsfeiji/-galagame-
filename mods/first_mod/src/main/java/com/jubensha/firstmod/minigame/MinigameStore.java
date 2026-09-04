package com.jubensha.firstmod.minigame;

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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class MinigameStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<Collection<MinigameInteraction>>() {
    }.getType();
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_./-]{1,64}");
    private static final Path STORE_PATH = FabricLoader.getInstance().getConfigDir().resolve("first_mod_minigames.json");
    private static final Map<String, MinigameInteraction> interactions = new LinkedHashMap<>();

    private MinigameStore() {
    }

    public static void load() {
        interactions.clear();
        if (!Files.exists(STORE_PATH)) {
            return;
        }
        try {
            JsonObject object = JsonParser.parseString(Files.readString(STORE_PATH, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!object.has("interactions")) {
                return;
            }
            Collection<MinigameInteraction> loaded = GSON.fromJson(object.get("interactions"), LIST_TYPE);
            if (loaded != null) {
                for (MinigameInteraction interaction : loaded) {
                    put(interaction);
                }
            }
        } catch (IOException | RuntimeException ignored) {
            interactions.clear();
        }
    }

    public static MinigameInteraction fromJsonStrict(String json) {
        MinigameInteraction interaction = GSON.fromJson(json, MinigameInteraction.class);
        if (interaction == null) {
            throw new IllegalArgumentException("empty minigame interaction");
        }
        interaction.normalize();
        if (!isValidId(interaction.id)) {
            throw new IllegalArgumentException("invalid id");
        }
        if (!"use_block".equals(interaction.trigger.type) && !"use_item".equals(interaction.trigger.type)) {
            throw new IllegalArgumentException("only use_block and use_item triggers are supported");
        }
        if ("use_block".equals(interaction.trigger.type) && interaction.trigger.block.isBlank()) {
            throw new IllegalArgumentException("trigger.block is required");
        }
        if ("use_item".equals(interaction.trigger.type) && interaction.trigger.item.isBlank()) {
            throw new IllegalArgumentException("trigger.item is required");
        }
        if (!"timing".equals(interaction.minigame.type)) {
            throw new IllegalArgumentException("only timing minigame is supported");
        }
        return interaction;
    }

    public static void saveInteraction(MinigameInteraction interaction) {
        put(interaction);
        saveAll();
    }

    public static MinigameInteraction get(String id) {
        MinigameInteraction interaction = interactions.get(id);
        return interaction == null ? null : interaction.normalize();
    }

    public static Collection<MinigameInteraction> all() {
        return interactions.values();
    }

    public static boolean isValidId(String id) {
        return id != null && ID_PATTERN.matcher(id).matches();
    }

    private static void put(MinigameInteraction interaction) {
        if (interaction != null) {
            interaction.normalize();
            if (isValidId(interaction.id)) {
                interactions.put(interaction.id, interaction);
            }
        }
    }

    private static void saveAll() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            JsonObject root = new JsonObject();
            root.add("interactions", GSON.toJsonTree(interactions.values()));
            Files.writeString(STORE_PATH, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
