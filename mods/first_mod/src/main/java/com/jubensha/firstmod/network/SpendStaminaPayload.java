package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpendStaminaPayload() implements CustomPayload {
    public static final Id<SpendStaminaPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "spend_stamina"));

    public static final PacketCodec<RegistryByteBuf, SpendStaminaPayload> CODEC = PacketCodec.unit(new SpendStaminaPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
