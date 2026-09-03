package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record AdvanceDialogPayload(UUID targetPlayerId, String nextNodeId) implements CustomPayload {
    public static final Id<AdvanceDialogPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "advance_dialog"));

    public static final PacketCodec<RegistryByteBuf, AdvanceDialogPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), AdvanceDialogPayload::targetPlayerId,
            PacketCodecs.STRING, AdvanceDialogPayload::nextNodeId,
            AdvanceDialogPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
