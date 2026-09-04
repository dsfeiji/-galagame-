package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record DuelFinishPayload(UUID controllerPlayerId, UUID targetPlayerId, String nodeId) implements CustomPayload {
    public static final Id<DuelFinishPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "duel_finish"));

    public static final PacketCodec<RegistryByteBuf, DuelFinishPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DuelFinishPayload::controllerPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DuelFinishPayload::targetPlayerId,
            PacketCodecs.STRING, DuelFinishPayload::nodeId,
            DuelFinishPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
