package com.jubensha.firstmod.network;

import com.jubensha.firstmod.FirstMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SaveDialogPayload(String roleId, int phase, String dialogJson) implements CustomPayload {
    public static final Id<SaveDialogPayload> ID = new Id<>(Identifier.of(FirstMod.MOD_ID, "save_dialog"));
    private static final int MAX_JSON_LENGTH = 262144;

    public static final PacketCodec<RegistryByteBuf, SaveDialogPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SaveDialogPayload::roleId,
            PacketCodecs.INTEGER, SaveDialogPayload::phase,
            PacketCodecs.string(MAX_JSON_LENGTH), SaveDialogPayload::dialogJson,
            SaveDialogPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
