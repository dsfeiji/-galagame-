package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record DialogPayload(
        UUID targetPlayerId,
        String targetPlayerName,
        String roleId,
        UUID controllerPlayerId,
        String currentNodeId,
        String dialogJson
) implements CustomPayload {
    public static final Id<DialogPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "show_dialog"));
    private static final int MAX_JSON_LENGTH = 262144;

    public static final PacketCodec<RegistryByteBuf, DialogPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DialogPayload::targetPlayerId,
            PacketCodecs.STRING, DialogPayload::targetPlayerName,
            PacketCodecs.STRING, DialogPayload::roleId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), DialogPayload::controllerPlayerId,
            PacketCodecs.STRING, DialogPayload::currentNodeId,
            PacketCodecs.string(MAX_JSON_LENGTH), DialogPayload::dialogJson,
            DialogPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
