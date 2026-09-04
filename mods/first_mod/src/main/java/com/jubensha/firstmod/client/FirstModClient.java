package com.jubensha.firstmod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.ArmWrestleClickPayload;
import com.jubensha.firstmod.network.ArmWrestleFinishPayload;
import com.jubensha.firstmod.network.CloseDialogPayload;
import com.jubensha.firstmod.network.DialogPayload;
import com.jubensha.firstmod.network.DuelFinishPayload;
import com.jubensha.firstmod.network.DuelScorePayload;
import com.jubensha.firstmod.network.SaveDialogPayload;
import com.jubensha.firstmod.network.AdvanceDialogPayload;
import com.jubensha.firstmod.network.MinigameResultPayload;
import com.jubensha.firstmod.network.StaminaPayload;
import com.jubensha.firstmod.network.InteractionMinigameResultPayload;
import com.jubensha.firstmod.network.SaveMinigamePayload;
import com.jubensha.firstmod.network.StartInteractionMinigamePayload;
import com.jubensha.firstmod.network.TransitionPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class FirstModClient implements ClientModInitializer {
    private static final Gson GSON = new GsonBuilder().create();
    private static KeyBinding controlPanelKey;

    @Override
    public void onInitializeClient() {
        DialogJsonFolder.ensureExists();
        MinigameJsonFolder.ensureExists();
        registerPayloadTypes();
        registerKeyBinding();
        StaminaHud.register();

        ClientPlayNetworking.registerGlobalReceiver(DialogPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> client.setScreen(new PlayerDialogScreen(
                    payload.targetPlayerId(),
                    payload.targetPlayerName(),
                    payload.roleId(),
                    payload.controllerPlayerId(),
                    payload.currentNodeId(),
                    payload.dialogJson()
            )));
        });
        ClientPlayNetworking.registerGlobalReceiver(CloseDialogPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            client.execute(() -> {
                if (client.currentScreen instanceof PlayerDialogScreen screen && screen.matches(payload.targetPlayerId(), payload.controllerPlayerId())) {
                    client.setScreen(null);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(StaminaPayload.ID, (payload, context) -> context.client().execute(() -> StaminaHud.update(payload.stamina(), payload.maxStamina())));
        ClientPlayNetworking.registerGlobalReceiver(TransitionPayload.ID, (payload, context) -> context.client().execute(() -> {
            TransitionOverlay.show(context.client(), payload.durationTicks());
        }));
        ClientPlayNetworking.registerGlobalReceiver(StartInteractionMinigamePayload.ID, (payload, context) -> context.client().execute(() -> {
            DialogTree.DialogMinigame minigame = GSON.fromJson(payload.minigameJson(), DialogTree.DialogMinigame.class);
            if (minigame != null) {
                minigame.normalize();
                context.client().setScreen(new InteractionMinigameScreen(payload.interactionId(), minigame));
            }
        }));
    }

    private static void registerKeyBinding() {
        controlPanelKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.first_mod.role_control_panel",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.first_mod.dialog"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (controlPanelKey.wasPressed()) {
                if (client.player != null && client.player.isCreative()) {
                    client.setScreen(new RoleControlScreen());
                } else if (client.player != null) {
                    client.player.sendMessage(net.minecraft.text.Text.literal("Only creative players can open the dialog control panel."), false);
                }
            }
        });
    }

    private static void registerPayloadTypes() {
        try {
            PayloadTypeRegistry.playC2S().register(SaveDialogPayload.ID, SaveDialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(SaveMinigamePayload.ID, SaveMinigamePayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(AdvanceDialogPayload.ID, AdvanceDialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(MinigameResultPayload.ID, MinigameResultPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(InteractionMinigameResultPayload.ID, InteractionMinigameResultPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(ArmWrestleClickPayload.ID, ArmWrestleClickPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(ArmWrestleFinishPayload.ID, ArmWrestleFinishPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(DuelScorePayload.ID, DuelScorePayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(DuelFinishPayload.ID, DuelFinishPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playS2C().register(StartInteractionMinigamePayload.ID, StartInteractionMinigamePayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
