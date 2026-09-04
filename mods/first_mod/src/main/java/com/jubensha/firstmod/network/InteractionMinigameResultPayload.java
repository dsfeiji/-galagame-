package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record InteractionMinigameResultPayload(String interactionId, boolean success) implements CustomPayload {
    public static final Id<InteractionMinigameResultPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "interaction_minigame_result"));

    public static final PacketCodec<RegistryByteBuf, InteractionMinigameResultPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, InteractionMinigameResultPayload::interactionId,
            PacketCodecs.BOOL, InteractionMinigameResultPayload::success,
            InteractionMinigameResultPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
