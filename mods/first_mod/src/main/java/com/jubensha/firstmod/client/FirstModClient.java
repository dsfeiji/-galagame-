package com.jubensha.firstmod.client;

import com.jubensha.firstmod.network.CloseDialogPayload;
import com.jubensha.firstmod.network.DialogPayload;
import com.jubensha.firstmod.network.SaveDialogPayload;
import com.jubensha.firstmod.network.AdvanceDialogPayload;
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
    private static KeyBinding controlPanelKey;

    @Override
    public void onInitializeClient() {
        DialogJsonFolder.ensureExists();
        registerPayloadTypes();
        registerKeyBinding();

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
            PayloadTypeRegistry.playC2S().register(AdvanceDialogPayload.ID, AdvanceDialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
