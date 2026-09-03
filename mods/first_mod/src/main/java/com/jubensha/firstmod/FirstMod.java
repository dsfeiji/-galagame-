package com.jubensha.firstmod;

import com.jubensha.firstmod.dialog.DialogStore;
import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.network.AdvanceDialogPayload;
import com.jubensha.firstmod.network.CloseDialogPayload;
import com.jubensha.firstmod.network.DialogPayload;
import com.jubensha.firstmod.network.SaveDialogPayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FirstMod implements ModInitializer {
    public static final String MOD_ID = "first_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Map<UUID, DialogSession> ACTIVE_DIALOGS = new HashMap<>();

    @Override
    public void onInitialize() {
        DialogStore.load();
        registerPayloadTypes();

        ServerPlayNetworking.registerGlobalReceiver(SaveDialogPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.isCreative()) {
                player.sendMessage(Text.literal("Only creative players can import dialog JSON."), false);
                return;
            }

            try {
                DialogTree tree = DialogTree.fromJsonStrict(payload.dialogJson());
                String roleId = payload.roleId().trim();
                if (!DialogStore.isValidRoleId(roleId)) {
                    player.sendMessage(Text.literal("Invalid role id. Use a-z, 0-9, _, -, . or /, max 64 chars."), false);
                    return;
                }
                DialogStore.saveDialog(roleId, payload.phase(), tree);
                player.sendMessage(Text.literal("Imported dialog JSON for role " + roleId + " phase " + payload.phase() + "."), false);
                refreshActiveDialogAfterImport(player, roleId, tree);
            } catch (RuntimeException exception) {
                player.sendMessage(Text.literal("Dialog JSON import failed: " + exception.getMessage()), false);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(AdvanceDialogPayload.ID, (payload, context) -> {
            ServerPlayerEntity actor = context.player();
            DialogSession session = ACTIVE_DIALOGS.get(actor.getUuid());
            if (session == null || !payload.targetPlayerId().equals(session.targetPlayerId)) {
                return;
            }

            ServerPlayerEntity target = actor.getServer().getPlayerManager().getPlayer(payload.targetPlayerId());
            if (target == null) {
                closeDialog(actor, null, payload.targetPlayerId());
                return;
            }

            DialogTree tree = DialogStore.getDialogForCurrentPhase(session.roleId);
            if (tree == null || payload.nextNodeId().isBlank() || !tree.hasNode(payload.nextNodeId())) {
                closeDialog(actor, target, target.getUuid());
                return;
            }
            showNode(actor, target, tree, payload.nextNodeId(), session);
        });

        registerCommands();

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            if (!(player instanceof ServerPlayerEntity actor) || !(entity instanceof ServerPlayerEntity target)) {
                return ActionResult.PASS;
            }

            String roleId = DialogStore.getClaimedRole(target.getUuid());
            if (roleId.isBlank()) {
                if (actor.isCreative()) {
                    actor.sendMessage(Text.literal("This player has not claimed a dialog role. Use /dialogrole claim <role_id> as that player."), false);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            }

            DialogTree tree = actor.isCreative()
                    ? DialogStore.getOrCreateDialogForCurrentPhase(roleId)
                    : DialogStore.getDialogForCurrentPhase(roleId);
            if (tree == null) {
                return ActionResult.PASS;
            }
            DialogSession session = new DialogSession(target.getUuid(), roleId);
            ACTIVE_DIALOGS.put(actor.getUuid(), session);
            showNode(actor, target, tree, tree.startNodeId, session);

            return ActionResult.SUCCESS;
        });

        LOGGER.info("First Mod initialized.");
    }

    private static void sendDialog(ServerPlayerEntity player, DialogPayload payload) {
        if (ServerPlayNetworking.canSend(player, DialogPayload.ID)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static void registerPayloadTypes() {
        try {
            PayloadTypeRegistry.playS2C().register(DialogPayload.ID, DialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playS2C().register(CloseDialogPayload.ID, CloseDialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(SaveDialogPayload.ID, SaveDialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playC2S().register(AdvanceDialogPayload.ID, AdvanceDialogPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void showNode(ServerPlayerEntity actor, ServerPlayerEntity target, DialogTree tree, String requestedNodeId, DialogSession session) {
        String nodeId = resolveNode(actor, tree, requestedNodeId, new HashSet<>());
        DialogTree.DialogNode node = tree.getNode(nodeId);
        if (node == null) {
            closeDialog(actor, target, target.getUuid());
            return;
        }
        giveRewards(actor, node, session);
        sendDialogPair(actor, target, tree, nodeId, session.roleId);
    }

    private static void sendDialogPair(ServerPlayerEntity actor, ServerPlayerEntity target, DialogTree tree, String nodeId, String roleId) {
        DialogPayload payload = new DialogPayload(
                target.getUuid(),
                target.getNameForScoreboard(),
                roleId,
                actor.getUuid(),
                nodeId,
                tree.toJson()
        );
        sendDialog(actor, payload);
        if (!actor.getUuid().equals(target.getUuid())) {
            sendDialog(target, payload);
        }
    }

    private static void closeDialog(ServerPlayerEntity actor, ServerPlayerEntity target, UUID targetId) {
        ACTIVE_DIALOGS.remove(actor.getUuid());
        CloseDialogPayload payload = new CloseDialogPayload(targetId, actor.getUuid());
        if (ServerPlayNetworking.canSend(actor, CloseDialogPayload.ID)) {
            ServerPlayNetworking.send(actor, payload);
        }
        if (target != null && !actor.getUuid().equals(target.getUuid()) && ServerPlayNetworking.canSend(target, CloseDialogPayload.ID)) {
            ServerPlayNetworking.send(target, payload);
        }
    }

    private static void refreshActiveDialogAfterImport(ServerPlayerEntity actor, String roleId, DialogTree tree) {
        DialogSession session = ACTIVE_DIALOGS.get(actor.getUuid());
        if (session == null || actor.getServer() == null) {
            return;
        }
        ServerPlayerEntity target = actor.getServer().getPlayerManager().getPlayer(session.targetPlayerId);
        if (target == null || !session.roleId.equals(roleId)) {
            return;
        }
        session.rewardedNodeIds.clear();
        showNode(actor, target, tree, tree.startNodeId, session);
    }

    private static String resolveNode(ServerPlayerEntity actor, DialogTree tree, String nodeId, Set<String> visited) {
        DialogTree.DialogNode node = tree.getNode(nodeId);
        if (node == null || !visited.add(nodeId)) {
            return nodeId;
        }
        for (DialogTree.ItemConditionJump conditionJump : node.conditionJumps) {
            if (tree.hasNode(conditionJump.nextNodeId) && hasItem(actor, conditionJump.item, conditionJump.count)) {
                return resolveNode(actor, tree, conditionJump.nextNodeId, visited);
            }
        }
        return nodeId;
    }

    private static boolean hasItem(ServerPlayerEntity player, String itemId, int count) {
        Item item = getItem(itemId);
        if (item == null) {
            return false;
        }
        int found = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(item)) {
                found += stack.getCount();
                if (found >= count) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void giveRewards(ServerPlayerEntity actor, DialogTree.DialogNode node, DialogSession session) {
        if (!session.rewardedNodeIds.add(node.id)) {
            return;
        }
        for (DialogTree.ItemReward reward : node.rewards) {
            Item item = getItem(reward.item);
            if (item != null) {
                actor.giveItemStack(new ItemStack(item, reward.count));
            }
        }
    }

    private static Item getItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        Identifier identifier = Identifier.tryParse(itemId);
        if (identifier == null) {
            return null;
        }
        Item item = Registries.ITEM.get(identifier);
        return item == Registries.ITEM.get(Identifier.of("minecraft", "air")) ? null : item;
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("dialogphase")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("setcount")
                        .then(argument("count", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int count = IntegerArgumentType.getInteger(context, "count");
                                    DialogStore.setPhaseCount(count);
                                    feedback(context.getSource(), "Dialog phase count set to " + count + ". Current phase: " + DialogStore.getCurrentPhase());
                                    return count;
                                })))
                .then(literal("next")
                        .executes(context -> {
                            int phase = DialogStore.nextPhase();
                            feedback(context.getSource(), "Dialog phase switched to " + phase + " / " + DialogStore.getPhaseCount());
                            return phase;
                        }))
                .then(literal("set")
                        .then(argument("phase", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    int phase = IntegerArgumentType.getInteger(context, "phase");
                                    DialogStore.setCurrentPhase(phase);
                                    feedback(context.getSource(), "Dialog phase switched to " + DialogStore.getCurrentPhase() + " / " + DialogStore.getPhaseCount());
                                    return DialogStore.getCurrentPhase();
                                })))
                .then(literal("info")
                        .executes(context -> {
                            feedback(context.getSource(), "Current dialog phase: " + DialogStore.getCurrentPhase() + " / " + DialogStore.getPhaseCount());
                            return DialogStore.getCurrentPhase();
                        })));

            dispatcher.register(literal("dialogrole")
                    .then(literal("claim")
                            .then(argument("role_id", StringArgumentType.word())
                                    .executes(context -> {
                                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                        String roleId = StringArgumentType.getString(context, "role_id").trim();
                                        if (!DialogStore.isValidRoleId(roleId)) {
                                            feedback(context.getSource(), "Invalid role id. Use a-z, 0-9, _, -, . or /, max 64 chars.");
                                            return 0;
                                        }
                                        DialogStore.claimRole(player.getUuid(), roleId);
                                        feedback(context.getSource(), player.getNameForScoreboard() + " claimed dialog role: " + roleId);
                                        return 1;
                                    })))
                    .then(literal("clear")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                DialogStore.clearRole(player.getUuid());
                                feedback(context.getSource(), player.getNameForScoreboard() + " cleared their dialog role.");
                                return 1;
                            }))
                    .then(literal("whoami")
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                String roleId = DialogStore.getClaimedRole(player.getUuid());
                                feedback(context.getSource(), roleId.isBlank() ? "You have not claimed a dialog role." : "Your dialog role: " + roleId);
                                return roleId.isBlank() ? 0 : 1;
                            }))
                    .then(literal("set")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(argument("player", EntityArgumentType.player())
                                    .then(argument("role_id", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                String roleId = StringArgumentType.getString(context, "role_id").trim();
                                                if (!DialogStore.isValidRoleId(roleId)) {
                                                    feedback(context.getSource(), "Invalid role id. Use a-z, 0-9, _, -, . or /, max 64 chars.");
                                                    return 0;
                                                }
                                                DialogStore.claimRole(target.getUuid(), roleId);
                                                feedback(context.getSource(), target.getNameForScoreboard() + " dialog role set to " + roleId);
                                                return 1;
                                            }))))
                    .then(literal("get")
                            .requires(source -> source.hasPermissionLevel(2))
                            .then(argument("player", EntityArgumentType.player())
                                    .executes(context -> {
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        String roleId = DialogStore.getClaimedRole(target.getUuid());
                                        feedback(context.getSource(), roleId.isBlank() ? target.getNameForScoreboard() + " has no dialog role." : target.getNameForScoreboard() + " dialog role: " + roleId);
                                        return roleId.isBlank() ? 0 : 1;
                                    }))));
        });
    }

    private static void feedback(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }

    private static class DialogSession {
        private final UUID targetPlayerId;
        private final String roleId;
        private final Set<String> rewardedNodeIds = new HashSet<>();

        private DialogSession(UUID targetPlayerId, String roleId) {
            this.targetPlayerId = targetPlayerId;
            this.roleId = roleId;
        }
    }
}
