package com.jubensha.firstmod;

import com.jubensha.firstmod.dialog.DialogStore;
import com.jubensha.firstmod.dialog.DialogTree;
import com.jubensha.firstmod.minigame.MinigameInteraction;
import com.jubensha.firstmod.minigame.MinigameStore;
import com.jubensha.firstmod.network.AdvanceDialogPayload;
import com.jubensha.firstmod.network.CloseDialogPayload;
import com.jubensha.firstmod.network.DialogPayload;
import com.jubensha.firstmod.network.InteractionMinigameResultPayload;
import com.jubensha.firstmod.network.MinigameResultPayload;
import com.jubensha.firstmod.network.SaveDialogPayload;
import com.jubensha.firstmod.network.SaveMinigamePayload;
import com.jubensha.firstmod.network.StartInteractionMinigamePayload;
import com.jubensha.firstmod.network.StaminaPayload;
import com.jubensha.firstmod.network.TransitionPayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FirstMod implements ModInitializer {
    public static final String MOD_ID = "first_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String PROTAGONIST_TELEPORT_ROLE_ID = "protagonist";
    private static final int PHASE_TRANSITION_BLACKOUT_TICKS = 40;
    private static final Map<UUID, DialogSession> ACTIVE_DIALOGS = new HashMap<>();
    private static final Map<UUID, String> ACTIVE_INTERACTION_MINIGAMES = new HashMap<>();

    @Override
    public void onInitialize() {
        DialogStore.load();
        MinigameStore.load();
        registerPayloadTypes();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncStamina(handler.player));
        registerSaveReceiver();
        registerSaveMinigameReceiver();
        registerAdvanceReceiver();
        registerMinigameReceiver();
        registerInteractionMinigameReceiver();
        registerCommands();
        registerBlockMinigames();
        registerItemMinigames();
        registerRightClickDialog();
        LOGGER.info("First Mod initialized.");
    }

    private static void registerSaveReceiver() {
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
    }

    private static void registerSaveMinigameReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(SaveMinigamePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.isCreative()) {
                player.sendMessage(Text.literal("Only creative players can import minigame JSON."), false);
                return;
            }

            try {
                MinigameInteraction interaction = MinigameStore.fromJsonStrict(payload.minigameJson());
                MinigameStore.saveInteraction(interaction);
                player.sendMessage(Text.literal("Imported minigame interaction: " + interaction.id), false);
            } catch (RuntimeException exception) {
                player.sendMessage(Text.literal("Minigame JSON import failed: " + exception.getMessage()), false);
            }
        });
    }

    private static void registerAdvanceReceiver() {
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
            String nextNodeId = resolveRequestedAdvance(actor, tree, session, payload);
            if (nextNodeId.isBlank() || tree == null || !tree.hasNode(nextNodeId)) {
                closeDialog(actor, target, target.getUuid());
                return;
            }
            showNode(actor, target, tree, nextNodeId, session);
        });
    }

    private static void registerRightClickDialog() {
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
                    actor.sendMessage(Text.literal("This player has not claimed a dialog role. Use /dialogrole claim <player> <role_id>."), false);
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
    }

    private static void registerBlockMinigames() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            Block block = world.getBlockState(pos).getBlock();
            String blockId = Registries.BLOCK.getId(block).toString();
            String worldId = world.getRegistryKey().getValue().toString();

            for (MinigameInteraction interaction : MinigameStore.all()) {
                if (matchesInteraction(interaction, serverPlayer, blockId, worldId, pos)) {
                    startInteractionMinigame(serverPlayer, interaction);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });
    }

    private static void registerItemMinigames() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (world.isClient() || hand != Hand.MAIN_HAND || !(player instanceof ServerPlayerEntity serverPlayer) || stack.isEmpty()) {
                return TypedActionResult.pass(stack);
            }

            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            String worldId = world.getRegistryKey().getValue().toString();
            for (MinigameInteraction interaction : MinigameStore.all()) {
                if (matchesItemInteraction(interaction, serverPlayer, itemId, worldId)) {
                    startInteractionMinigame(serverPlayer, interaction);
                    return TypedActionResult.success(stack);
                }
            }
            return TypedActionResult.pass(stack);
        });
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
            PayloadTypeRegistry.playS2C().register(StaminaPayload.ID, StaminaPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playS2C().register(TransitionPayload.ID, TransitionPayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            PayloadTypeRegistry.playS2C().register(StartInteractionMinigamePayload.ID, StartInteractionMinigamePayload.CODEC);
        } catch (IllegalArgumentException ignored) {
        }
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
    }

    private static void registerMinigameReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(MinigameResultPayload.ID, (payload, context) -> {
            ServerPlayerEntity actor = context.player();
            DialogSession session = ACTIVE_DIALOGS.get(actor.getUuid());
            if (session == null || !payload.targetPlayerId().equals(session.targetPlayerId) || !payload.nodeId().equals(session.currentNodeId)) {
                return;
            }

            ServerPlayerEntity target = actor.getServer().getPlayerManager().getPlayer(payload.targetPlayerId());
            if (target == null) {
                closeDialog(actor, null, payload.targetPlayerId());
                return;
            }

            DialogTree tree = DialogStore.getDialogForCurrentPhase(session.roleId);
            if (tree == null) {
                closeDialog(actor, target, target.getUuid());
                return;
            }
            DialogTree.DialogNode currentNode = tree.getNode(session.currentNodeId);
            if (currentNode == null || currentNode.minigame == null) {
                return;
            }

            String nextNodeId = payload.success() ? currentNode.minigame.successNodeId : currentNode.minigame.failureNodeId;
            if (nextNodeId.isBlank() || !tree.hasNode(nextNodeId)) {
                closeDialog(actor, target, target.getUuid());
                return;
            }
            showNode(actor, target, tree, nextNodeId, session);
        });
    }

    private static void registerInteractionMinigameReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(InteractionMinigameResultPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            String activeId = ACTIVE_INTERACTION_MINIGAMES.get(player.getUuid());
            if (activeId == null || !activeId.equals(payload.interactionId())) {
                return;
            }
            ACTIVE_INTERACTION_MINIGAMES.remove(player.getUuid());

            MinigameInteraction interaction = MinigameStore.get(payload.interactionId());
            if (interaction == null) {
                return;
            }
            applyInteractionResult(player, payload.success() ? interaction.success : interaction.failure);
        });
    }

    private static String resolveRequestedAdvance(ServerPlayerEntity actor, DialogTree tree, DialogSession session, AdvanceDialogPayload payload) {
        if (tree == null) {
            return "";
        }
        DialogTree.DialogNode currentNode = tree.getNode(session.currentNodeId);
        if (currentNode == null) {
            return "";
        }
        if (currentNode.minigame != null) {
            return session.currentNodeId;
        }

        if (!currentNode.choices.isEmpty()) {
            int choiceIndex = payload.choiceIndex();
            if (choiceIndex < 0 || choiceIndex >= currentNode.choices.size()) {
                return "";
            }
            DialogTree.DialogChoice choice = currentNode.choices.get(choiceIndex);
            if (!choice.nextNodeId.equals(payload.nextNodeId())) {
                return "";
            }
            if (!DialogStore.spendStamina(actor.getUuid(), choice.staminaCost)) {
                actor.sendMessage(Text.literal("体力不足，无法选择这个回答。"), false);
                syncStamina(actor);
                return session.currentNodeId;
            }
            syncStamina(actor);
            if (choice.staminaCost > 0 && DialogStore.getStamina(actor.getUuid()) == 0 && handleStaminaDepleted(actor)) {
                return "";
            }
            return choice.nextNodeId;
        }

        if (payload.choiceIndex() != -1 || !currentNode.nextNodeId.equals(payload.nextNodeId())) {
            return "";
        }
        return currentNode.nextNodeId;
    }

    private static boolean handleStaminaDepleted(ServerPlayerEntity actor) {
        if (actor.getServer() == null) {
            return false;
        }
        if (!DialogStore.isProtagonist(actor.getUuid())) {
            actor.sendMessage(Text.literal("体力已耗尽，但只有主角可以推动剧情阶段。"), false);
            return false;
        }
        advancePhaseNow(actor.getServer(), nextPhaseValue());
        return true;
    }

    private static int nextPhaseValue() {
        int next = DialogStore.getCurrentPhase() + 1;
        return next > DialogStore.getPhaseCount() ? 1 : next;
    }

    private static void advancePhaseNow(MinecraftServer server, int targetPhase) {
        closeAllDialogs(server);
        DialogStore.setCurrentPhase(targetPhase);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendTransition(player, PHASE_TRANSITION_BLACKOUT_TICKS);
            teleportForCurrentRole(server, player, targetPhase);
            DialogStore.resetStamina(player.getUuid());
            syncStamina(player);
        }
    }

    private static void showNode(ServerPlayerEntity actor, ServerPlayerEntity target, DialogTree tree, String requestedNodeId, DialogSession session) {
        String nodeId = resolveNode(actor, tree, requestedNodeId, new HashSet<>());
        DialogTree.DialogNode node = tree.getNode(nodeId);
        if (node == null) {
            closeDialog(actor, target, target.getUuid());
            return;
        }
        session.currentNodeId = nodeId;
        giveRewards(actor, node, session);
        sendDialogPair(actor, target, tree, nodeId, session.roleId);
    }

    private static void sendDialogPair(ServerPlayerEntity actor, ServerPlayerEntity target, DialogTree tree, String nodeId, String roleId) {
        DialogPayload payload = new DialogPayload(target.getUuid(), target.getNameForScoreboard(), roleId, actor.getUuid(), nodeId, tree.toJson());
        sendDialog(actor, payload);
        syncStamina(actor);
        if (!actor.getUuid().equals(target.getUuid())) {
            sendDialog(target, payload);
        }
    }

    private static void sendDialog(ServerPlayerEntity player, DialogPayload payload) {
        if (ServerPlayNetworking.canSend(player, DialogPayload.ID)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    private static void sendTransition(ServerPlayerEntity player, int durationTicks) {
        if (ServerPlayNetworking.canSend(player, TransitionPayload.ID)) {
            ServerPlayNetworking.send(player, new TransitionPayload(durationTicks));
        }
    }

    private static void closeAllDialogs(MinecraftServer server) {
        Map<UUID, DialogSession> sessions = new HashMap<>(ACTIVE_DIALOGS);
        for (Map.Entry<UUID, DialogSession> entry : sessions.entrySet()) {
            ServerPlayerEntity actor = server.getPlayerManager().getPlayer(entry.getKey());
            if (actor == null) {
                continue;
            }
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(entry.getValue().targetPlayerId);
            closeDialog(actor, target, entry.getValue().targetPlayerId);
        }
        ACTIVE_DIALOGS.clear();
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

    private static void teleportForCurrentRole(MinecraftServer server, ServerPlayerEntity player, int phase) {
        String roleId = DialogStore.getClaimedRole(player.getUuid());
        DialogStore.TeleportPoint point = DialogStore.isProtagonist(player.getUuid())
                ? DialogStore.getTeleportExact(phase, PROTAGONIST_TELEPORT_ROLE_ID)
                : null;
        if (point == null) {
            point = DialogStore.getTeleport(phase, roleId.isBlank() ? "default" : roleId);
        }
        if (point == null) {
            player.sendMessage(Text.literal("当前阶段没有你的角色传送点。"), false);
            return;
        }
        teleportToPoint(server, player, point);
    }

    private static void teleportToPoint(MinecraftServer server, ServerPlayerEntity player, DialogStore.TeleportPoint point) {
        Identifier worldId = Identifier.tryParse(point.world);
        if (worldId == null) {
            player.sendMessage(Text.literal("传送点世界无效：" + point.world), false);
            return;
        }
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
        ServerWorld world = server.getWorld(worldKey);
        if (world == null) {
            player.sendMessage(Text.literal("找不到传送点世界：" + point.world), false);
            return;
        }
        player.teleport(world, point.x, point.y, point.z, point.yaw, point.pitch);
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

    private static boolean matchesInteraction(MinigameInteraction interaction, ServerPlayerEntity player, String blockId, String worldId, BlockPos pos) {
        interaction.normalize();
        if (!"use_block".equals(interaction.trigger.type) || !interaction.trigger.block.equals(blockId)) {
            return false;
        }
        if (!matchesCommonInteractionRules(interaction, player, worldId)) {
            return false;
        }
        if (interaction.trigger.x != null && interaction.trigger.x != pos.getX()) {
            return false;
        }
        if (interaction.trigger.y != null && interaction.trigger.y != pos.getY()) {
            return false;
        }
        if (interaction.trigger.z != null && interaction.trigger.z != pos.getZ()) {
            return false;
        }
        return matchesPhase(interaction);
    }

    private static boolean matchesItemInteraction(MinigameInteraction interaction, ServerPlayerEntity player, String itemId, String worldId) {
        interaction.normalize();
        if (!"use_item".equals(interaction.trigger.type) || !interaction.trigger.item.equals(itemId)) {
            return false;
        }
        return matchesCommonInteractionRules(interaction, player, worldId) && matchesPhase(interaction);
    }

    private static boolean matchesCommonInteractionRules(MinigameInteraction interaction, ServerPlayerEntity player, String worldId) {
        if (interaction.protagonistOnly && !DialogStore.isProtagonist(player.getUuid())) {
            return false;
        }
        return interaction.trigger.world.isBlank() || interaction.trigger.world.equals(worldId);
    }

    private static boolean matchesPhase(MinigameInteraction interaction) {
        if (!interaction.trigger.phases.isEmpty()) {
            return interaction.trigger.phases.contains(DialogStore.getCurrentPhase());
        }
        return interaction.trigger.phase < 1 || interaction.trigger.phase == DialogStore.getCurrentPhase();
    }

    private static void startInteractionMinigame(ServerPlayerEntity player, MinigameInteraction interaction) {
        if (!DialogStore.spendStamina(player.getUuid(), interaction.staminaCost)) {
            player.sendMessage(Text.literal("体力不足，无法进行这个行动。"), false);
            syncStamina(player);
            return;
        }
        syncStamina(player);
        ACTIVE_INTERACTION_MINIGAMES.put(player.getUuid(), interaction.id);
        if (ServerPlayNetworking.canSend(player, StartInteractionMinigamePayload.ID)) {
            ServerPlayNetworking.send(player, new StartInteractionMinigamePayload(
                    interaction.id,
                    interaction.minigame.title,
                    interaction.minigame.difficulty,
                    interaction.minigame.speed,
                    interaction.minigame.successStart,
                    interaction.minigame.successWidth
            ));
        }
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

    private static void applyInteractionResult(ServerPlayerEntity player, MinigameInteraction.Result result) {
        result.normalize();
        if (!result.message.isBlank()) {
            player.sendMessage(Text.literal(result.message), false);
        }
        for (DialogTree.ItemReward reward : result.rewards) {
            Item item = getItem(reward.item);
            if (item != null) {
                player.giveItemStack(new ItemStack(item, reward.count));
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
                                int phase = nextPhaseValue();
                                advancePhaseNow(context.getSource().getServer(), phase);
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
                            }))
                    .then(literal("settp")
                            .then(argument("phase", IntegerArgumentType.integer(1))
                                    .then(argument("role_id", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                int phase = IntegerArgumentType.getInteger(context, "phase");
                                                String roleId = StringArgumentType.getString(context, "role_id").trim();
                                                if (!DialogStore.isValidRoleId(roleId)) {
                                                    feedback(context.getSource(), "Invalid role id. Use default or a-z, 0-9, _, -, . or /, max 64 chars.");
                                                    return 0;
                                                }
                                                DialogStore.TeleportPoint point = new DialogStore.TeleportPoint();
                                                point.world = player.getWorld().getRegistryKey().getValue().toString();
                                                point.x = player.getX();
                                                point.y = player.getY();
                                                point.z = player.getZ();
                                                point.yaw = player.getYaw();
                                                point.pitch = player.getPitch();
                                                DialogStore.setTeleport(phase, roleId, point);
                                                feedback(context.getSource(), "Saved phase " + phase + " teleport for role " + roleId + ".");
                                                return 1;
                                            }))))
                    .then(literal("tptest")
                            .then(argument("phase", IntegerArgumentType.integer(1))
                                    .then(argument("role_id", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                                int phase = IntegerArgumentType.getInteger(context, "phase");
                                                String roleId = StringArgumentType.getString(context, "role_id").trim();
                                                DialogStore.TeleportPoint point = DialogStore.getTeleport(phase, roleId);
                                                if (point == null) {
                                                    feedback(context.getSource(), "No teleport for phase " + phase + " role " + roleId + ".");
                                                    return 0;
                                                }
                                                teleportToPoint(context.getSource().getServer(), player, point);
                                                return 1;
                                            }))))
                    .then(literal("cleartp")
                            .then(argument("phase", IntegerArgumentType.integer(1))
                                    .then(argument("role_id", StringArgumentType.word())
                                            .executes(context -> {
                                                int phase = IntegerArgumentType.getInteger(context, "phase");
                                                String roleId = StringArgumentType.getString(context, "role_id").trim();
                                                DialogStore.clearTeleport(phase, roleId);
                                                feedback(context.getSource(), "Cleared phase " + phase + " teleport for role " + roleId + ".");
                                                return 1;
                                            }))))
                    .then(literal("tpinfo")
                            .then(argument("phase", IntegerArgumentType.integer(1))
                                    .executes(context -> {
                                        int phase = IntegerArgumentType.getInteger(context, "phase");
                                        String roles = DialogStore.getTeleportInfo(phase);
                                        feedback(context.getSource(), roles.isBlank() ? "No teleports for phase " + phase + "." : "Phase " + phase + " teleports: " + roles);
                                        return roles.isBlank() ? 0 : 1;
                                    }))));

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
                                    }))
                            .then(argument("player", EntityArgumentType.player())
                                    .requires(source -> source.hasPermissionLevel(2))
                                    .then(argument("role_id", StringArgumentType.word())
                                            .executes(context -> {
                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                                String roleId = StringArgumentType.getString(context, "role_id").trim();
                                                if (!DialogStore.isValidRoleId(roleId)) {
                                                    feedback(context.getSource(), "Invalid role id. Use a-z, 0-9, _, -, . or /, max 64 chars.");
                                                    return 0;
                                                }
                                                DialogStore.claimRole(target.getUuid(), roleId);
                                                feedback(context.getSource(), target.getNameForScoreboard() + " claimed dialog role: " + roleId);
                                                return 1;
                                            }))))
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

            dispatcher.register(literal("dialogprotagonist")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(literal("set")
                            .then(argument("player", EntityArgumentType.player())
                                    .executes(context -> {
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        DialogStore.setProtagonist(target.getUuid());
                                        feedback(context.getSource(), target.getNameForScoreboard() + " is now the protagonist. Protagonist teleport role id: " + PROTAGONIST_TELEPORT_ROLE_ID);
                                        return 1;
                                    })))
                    .then(literal("clear")
                            .executes(context -> {
                                DialogStore.clearProtagonist();
                                feedback(context.getSource(), "Protagonist cleared.");
                                return 1;
                            }))
                    .then(literal("get")
                            .executes(context -> {
                                String protagonistId = DialogStore.getProtagonistPlayerId();
                                if (protagonistId.isBlank()) {
                                    feedback(context.getSource(), "No protagonist set.");
                                    return 0;
                                }
                                ServerPlayerEntity player = null;
                                try {
                                    player = context.getSource().getServer().getPlayerManager().getPlayer(UUID.fromString(protagonistId));
                                } catch (RuntimeException ignored) {
                                }
                                String name = player == null ? "UUID: " + protagonistId : player.getNameForScoreboard();
                                feedback(context.getSource(), "Protagonist: " + name + ". Teleport role id: " + PROTAGONIST_TELEPORT_ROLE_ID);
                                return 1;
                            })));

            dispatcher.register(literal("dialogstamina")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(literal("reset")
                            .then(argument("player", EntityArgumentType.player())
                                    .executes(context -> {
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        DialogStore.resetStamina(target.getUuid());
                                        syncStamina(target);
                                        feedback(context.getSource(), target.getNameForScoreboard() + " stamina reset to " + DialogStore.MAX_STAMINA + ".");
                                        return DialogStore.MAX_STAMINA;
                                    })))
                    .then(literal("info")
                            .then(argument("player", EntityArgumentType.player())
                                    .executes(context -> {
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        int stamina = DialogStore.getStamina(target.getUuid());
                                        feedback(context.getSource(), target.getNameForScoreboard() + " stamina: " + stamina + " / " + DialogStore.MAX_STAMINA);
                                        return stamina;
                                    }))));
        });
    }

    private static void syncStamina(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, StaminaPayload.ID)) {
            ServerPlayNetworking.send(player, new StaminaPayload(DialogStore.getStamina(player.getUuid()), DialogStore.MAX_STAMINA));
        }
    }

    private static void feedback(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }

    private static class DialogSession {
        private final UUID targetPlayerId;
        private final String roleId;
        private final Set<String> rewardedNodeIds = new HashSet<>();
        private String currentNodeId = "";

        private DialogSession(UUID targetPlayerId, String roleId) {
            this.targetPlayerId = targetPlayerId;
            this.roleId = roleId;
        }
    }
}
