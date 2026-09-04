package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SaveMinigamePayload(String minigameJson) implements CustomPayload {
    public static final Id<SaveMinigamePayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "save_minigame"));
    private static final int MAX_JSON_LENGTH = 262144;

    public static final PacketCodec<RegistryByteBuf, SaveMinigamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.string(MAX_JSON_LENGTH), SaveMinigamePayload::minigameJson,
            SaveMinigamePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
