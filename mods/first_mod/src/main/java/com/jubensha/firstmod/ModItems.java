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
    public static final Item CHIPPED_ATTENDANCE_TAG = registerItem("chipped_attendance_tag");
    public static final Item BROKEN_WRISTBAND = registerItem("broken_wristband");
    public static final Item BROKEN_EXAM_PAPER = registerItem("broken_exam_paper");
    public static final Item CRUMPLED_WITNESS_NOTE = registerItem("crumpled_witness_note");
    public static final Item CRACKED_PHONE_CHARM = registerItem("cracked_phone_charm");
    public static final Item STAINED_PAINTBRUSH = registerItem("stained_paintbrush");
    public static final Item BURNT_NOTE = registerItem("burnt_note");
    public static final Item WRONG_TIME_WATCH = registerItem("wrong_time_watch");
    public static final Item EQUIPMENT_HOOK = registerItem("equipment_hook");
    public static final Item GRADING_RULE = registerItem("grading_rule");
    public static final Item STRANGE_SPYGLASS = registerItem("strange_spyglass");
    public static final Item TIME_CHIP = registerItem("time_chip");
    public static final Item MIRROR_PIGMENT = registerItem("mirror_pigment");
    public static final Item EVACUATION_POINTER = registerItem("evacuation_pointer");
    public static final Item ROOM_LOCKER = registerItem("room_locker");

    private ModItems() {
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, Identifier.of(FirstMod.MOD_ID, "script_tokens"), createItemGroup());
    }

    private static Item registerItem(String id) {
        return Registry.register(Registries.ITEM, Identifier.of(FirstMod.MOD_ID, id), new Item(new Item.Settings().maxCount(1)));
    }

    private static ItemGroup createItemGroup() {
        return FabricItemGroup.builder()
                .displayName(Text.translatable("itemGroup.first_mod.script_tokens"))
                .icon(() -> new ItemStack(CHIPPED_ATTENDANCE_TAG))
                .entries((context, entries) -> {
                    entries.add(CHIPPED_ATTENDANCE_TAG);
                    entries.add(BROKEN_WRISTBAND);
                    entries.add(BROKEN_EXAM_PAPER);
                    entries.add(CRUMPLED_WITNESS_NOTE);
                    entries.add(CRACKED_PHONE_CHARM);
                    entries.add(STAINED_PAINTBRUSH);
                    entries.add(BURNT_NOTE);
                    entries.add(WRONG_TIME_WATCH);
                    entries.add(EQUIPMENT_HOOK);
                    entries.add(GRADING_RULE);
                    entries.add(STRANGE_SPYGLASS);
                    entries.add(TIME_CHIP);
                    entries.add(MIRROR_PIGMENT);
                    entries.add(EVACUATION_POINTER);
                    entries.add(ROOM_LOCKER);
                })
                .build();
    }
}
