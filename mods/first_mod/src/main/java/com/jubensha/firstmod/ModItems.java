package com.jubensha.firstmod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item BROKEN_EXAM_PAPER = registerItem("broken_exam_paper");
    public static final Item BROKEN_WRISTBAND = registerItem("broken_wristband");
    public static final Item BURNT_NOTE = registerItem("burnt_note");
    public static final Item CHIPPED_ATTENDANCE_TAG = registerItem("chipped_attendance_tag");
    public static final Item CRACKED_PHONE_CHARM = registerItem("cracked_phone_charm");
    public static final Item CRUMPLED_WITNESS_NOTE = registerItem("crumpled_witness_note");
    public static final Item STAINED_PAINTBRUSH = registerItem("stained_paintbrush");

    private ModItems() {
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, Identifier.of(FirstMod.MOD_ID, "script_tokens"), createItemGroup());
    }

    private static Item registerItem(String id) {
        return Registry.register(Registries.ITEM, Identifier.of(FirstMod.MOD_ID, id), new Item(new Item.Settings()));
    }

    private static ItemGroup createItemGroup() {
        return FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.first_mod.script_tokens"))
                .icon(() -> new ItemStack(BROKEN_EXAM_PAPER))
                .entries((context, entries) -> {
                    entries.add(BROKEN_EXAM_PAPER);
                    entries.add(BROKEN_WRISTBAND);
                    entries.add(BURNT_NOTE);
                    entries.add(CHIPPED_ATTENDANCE_TAG);
                    entries.add(CRACKED_PHONE_CHARM);
                    entries.add(CRUMPLED_WITNESS_NOTE);
                    entries.add(STAINED_PAINTBRUSH);
                })
                .build();
    }
}
