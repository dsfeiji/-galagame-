package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record DuelScorePayload(UUID controllerPlayerId, UUID targetPlayerId, String nodeId, int scoreDelta) implements CustomPayload {
    public static final Id<DuelScorePayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "duel_score"));

    public static final PacketCodec<RegistryByteBuf, DuelScorePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DuelScorePayload::controllerPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DuelScorePayload::targetPlayerId,
            PacketCodecs.STRING, DuelScorePayload::nodeId,
            PacketCodecs.INTEGER, DuelScorePayload::scoreDelta,
            DuelScorePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
