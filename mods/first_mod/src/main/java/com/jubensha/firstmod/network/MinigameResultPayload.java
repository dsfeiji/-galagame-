package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record MinigameResultPayload(UUID targetPlayerId, String nodeId, boolean success) implements CustomPayload {
    public static final Id<MinigameResultPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "minigame_result"));

    public static final PacketCodec<RegistryByteBuf, MinigameResultPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), MinigameResultPayload::targetPlayerId,
            PacketCodecs.STRING, MinigameResultPayload::nodeId,
            PacketCodecs.BOOL, MinigameResultPayload::success,
            MinigameResultPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
