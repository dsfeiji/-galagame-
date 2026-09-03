package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TransitionPayload(int durationTicks) implements CustomPayload {
    public static final Id<TransitionPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "transition"));

    public static final PacketCodec<RegistryByteBuf, TransitionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, TransitionPayload::durationTicks,
            TransitionPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
