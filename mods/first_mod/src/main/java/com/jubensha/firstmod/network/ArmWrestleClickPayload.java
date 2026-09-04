package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ArmWrestleClickPayload(UUID controllerPlayerId, UUID targetPlayerId, String nodeId) implements CustomPayload {
    public static final Id<ArmWrestleClickPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "arm_wrestle_click"));

    public static final PacketCodec<RegistryByteBuf, ArmWrestleClickPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), ArmWrestleClickPayload::controllerPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), ArmWrestleClickPayload::targetPlayerId,
            PacketCodecs.STRING, ArmWrestleClickPayload::nodeId,
            ArmWrestleClickPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
