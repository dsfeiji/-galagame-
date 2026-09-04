package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record DuelStatePayload(UUID controllerPlayerId, UUID targetPlayerId, String nodeId, int actorScore, int targetScore) implements CustomPayload {
    public static final Id<DuelStatePayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "duel_state"));

    public static final PacketCodec<RegistryByteBuf, DuelStatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DuelStatePayload::controllerPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DuelStatePayload::targetPlayerId,
            PacketCodecs.STRING, DuelStatePayload::nodeId,
            PacketCodecs.INTEGER, DuelStatePayload::actorScore,
            PacketCodecs.INTEGER, DuelStatePayload::targetScore,
            DuelStatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
