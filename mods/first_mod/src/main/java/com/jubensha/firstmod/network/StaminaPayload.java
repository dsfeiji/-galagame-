package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StaminaPayload(int stamina, int maxStamina) implements CustomPayload {
    public static final Id<StaminaPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "stamina"));

    public static final PacketCodec<RegistryByteBuf, StaminaPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, StaminaPayload::stamina,
            PacketCodecs.INTEGER, StaminaPayload::maxStamina,
            StaminaPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
