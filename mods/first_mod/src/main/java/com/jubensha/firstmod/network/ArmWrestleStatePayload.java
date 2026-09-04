package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record ArmWrestleStatePayload(UUID controllerPlayerId, UUID targetPlayerId, String nodeId, float progress) implements CustomPayload {
    public static final Id<ArmWrestleStatePayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "arm_wrestle_state"));

    public static final PacketCodec<RegistryByteBuf, ArmWrestleStatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), ArmWrestleStatePayload::controllerPlayerId,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString), ArmWrestleStatePayload::targetPlayerId,
            PacketCodecs.STRING, ArmWrestleStatePayload::nodeId,
            PacketCodecs.FLOAT, ArmWrestleStatePayload::progress,
            ArmWrestleStatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
