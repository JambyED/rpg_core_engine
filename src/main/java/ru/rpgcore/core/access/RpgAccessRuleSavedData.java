package ru.rpgcore.core.access;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * World-level persistent storage for access rules.
 */
public class RpgAccessRuleSavedData extends SavedData {

    public static final String DATA_NAME = "rpg_core_access_rules";

    private static final String KEY_RULES = "rules";

    private final Map<String, RpgAccessRule> rules = new LinkedHashMap<>();

    public RpgAccessRuleSavedData() {
    }

    public static RpgAccessRuleSavedData load(CompoundTag tag) {
        RpgAccessRuleSavedData data = new RpgAccessRuleSavedData();

        if (tag.contains(KEY_RULES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(KEY_RULES, Tag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);

                try {
                    RpgAccessSubjectType subjectType =
                            RpgAccessSubjectType.valueOf(entry.getString("subjectType"));
                    String subjectId = entry.getString("subjectId");

                    RpgAccessPermission permission =
                            RpgAccessPermission.valueOf(entry.getString("permission"));

                    RpgAccessTargetType targetType =
                            RpgAccessTargetType.valueOf(entry.getString("targetType"));
                    String targetId = entry.getString("targetId");

                    RpgAccessRule rule = new RpgAccessRule(
                            new RpgAccessSubject(subjectType, subjectId),
                            permission,
                            new RpgAccessTarget(targetType, targetId)
                    );

                    data.rules.put(rule.asKey(), rule);
                } catch (Exception ignored) {
                    // Skip invalid entries to avoid breaking world load
                }
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (RpgAccessRule rule : rules.values()) {
            CompoundTag entry = new CompoundTag();

            entry.putString("subjectType", rule.subject().type().name());
            entry.putString("subjectId", rule.subject().id());

            entry.putString("permission", rule.permission().name());

            entry.putString("targetType", rule.target().type().name());
            entry.putString("targetId", rule.target().id());

            list.add(entry);
        }

        tag.put(KEY_RULES, list);
        return tag;
    }

    public Collection<RpgAccessRule> rules() {
        return rules.values();
    }

    public void put(RpgAccessRule rule) {
        rules.put(rule.asKey(), rule);
        setDirty();
    }

    public void remove(RpgAccessRule rule) {
        rules.remove(rule.asKey());
        setDirty();
    }

    public void clearAll() {
        rules.clear();
        setDirty();
    }

    public int size() {
        return rules.size();
    }
}