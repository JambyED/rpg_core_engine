package ru.rpgcore.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Set;

public final class NbtMatcher {
    private NbtMatcher() {}

    // pattern matches target if pattern ⊆ target
    public static boolean matches(CompoundTag pattern, CompoundTag target) {
        if (pattern == null) return true;
        if (target == null) return false;

        Set<String> keys = pattern.getAllKeys();
        for (String key : keys) {
            if (!target.contains(key)) return false;

            Tag p = pattern.get(key);
            Tag t = target.get(key);
            if (p == null || t == null) return false;

            if (p.getId() == Tag.TAG_COMPOUND) {
                if (t.getId() != Tag.TAG_COMPOUND) return false;
                if (!matches((CompoundTag) p, (CompoundTag) t)) return false;
            } else {
                if (!p.equals(t)) return false;
            }
        }
        return true;
    }
}