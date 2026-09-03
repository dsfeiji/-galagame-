package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record CloseDialogPayload(UUID targetPlayerId, UUID controllerPlayerId) implements CustomPayload {
    public static final Id<CloseDialogPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "close_dialog"));

    public static final PacketCodec<RegistryByteBuf, CloseDialogPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), CloseDialogPayload::targetPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), CloseDialogPayload::controllerPlayerId,
            CloseDialogPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
