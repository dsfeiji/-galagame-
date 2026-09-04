package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ArmWrestleFinishPayload(UUID controllerPlayerId, UUID targetPlayerId, String nodeId) implements CustomPayload {
    public static final Id<ArmWrestleFinishPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "arm_wrestle_finish"));

    public static final PacketCodec<RegistryByteBuf, ArmWrestleFinishPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), ArmWrestleFinishPayload::controllerPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), ArmWrestleFinishPayload::targetPlayerId,
            PacketCodecs.STRING, ArmWrestleFinishPayload::nodeId,
            ArmWrestleFinishPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
