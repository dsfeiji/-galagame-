package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StartInteractionMinigamePayload(String interactionId, String title, int difficulty, float speed, float successStart, float successWidth) implements CustomPayload {
    public static final Id<StartInteractionMinigamePayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "start_interaction_minigame"));

    public static final PacketCodec<RegistryByteBuf, StartInteractionMinigamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, StartInteractionMinigamePayload::interactionId,
            PacketCodecs.STRING, StartInteractionMinigamePayload::title,
            PacketCodecs.INTEGER, StartInteractionMinigamePayload::difficulty,
            PacketCodecs.FLOAT, StartInteractionMinigamePayload::speed,
            PacketCodecs.FLOAT, StartInteractionMinigamePayload::successStart,
            PacketCodecs.FLOAT, StartInteractionMinigamePayload::successWidth,
            StartInteractionMinigamePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
