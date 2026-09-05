package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EliminationPayload(String reason, int durationTicks) implements CustomPayload {
    public static final Id<EliminationPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "elimination"));
    private static final int MAX_REASON_LENGTH = 512;

    public static final PacketCodec<RegistryByteBuf, EliminationPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(MAX_REASON_LENGTH), EliminationPayload::reason,
            PacketCodecs.INTEGER, EliminationPayload::durationTicks,
            EliminationPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
