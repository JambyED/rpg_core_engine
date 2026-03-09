package ru.rpgcore.core.profile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import ru.rpgcore.RpgCore;

import java.util.*;

public final class RpgProfileStorage {
    private RpgProfileStorage() {}

    private static final String ROOT = RpgCore.MODID;

    private static final String KEY_LEVEL = "level";
    private static final String KEY_XP = "xp";

    // Currency / wallet
    private static final String KEY_BALANCE = "balance";

    // Class
    private static final String KEY_CLASS_ID = "classId"; // String "modid:class"

    // Perks
    private static final String KEY_PERK_SPENT = "perkTokensSpent";
    private static final String KEY_PERKS = "chosenPerks"; // ListTag of StringTag

    // Tier picks
    private static final String KEY_PERKS_BY_TIER = "chosenPerksByTier"; // CompoundTag: "<tier>" -> "modid:perk"

    public static RpgProfile load(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT, Tag.TAG_COMPOUND) ? persistent.getCompound(ROOT) : new CompoundTag();

        int level = root.contains(KEY_LEVEL, Tag.TAG_INT) ? root.getInt(KEY_LEVEL) : 0;
        int xp = root.contains(KEY_XP, Tag.TAG_INT) ? root.getInt(KEY_XP) : 0;

        long balance = 0L;
        if (root.contains(KEY_BALANCE, Tag.TAG_LONG)) {
            balance = Math.max(0L, root.getLong(KEY_BALANCE));
        } else if (root.contains(KEY_BALANCE, Tag.TAG_INT)) {
            // Safety for any temporary/older int-based experiments
            balance = Math.max(0L, root.getInt(KEY_BALANCE));
        }

        String classId = root.contains(KEY_CLASS_ID, Tag.TAG_STRING) ? root.getString(KEY_CLASS_ID) : null;

        int spent = root.contains(KEY_PERK_SPENT, Tag.TAG_INT) ? root.getInt(KEY_PERK_SPENT) : 0;

        // chosenPerks list
        List<String> perks = new ArrayList<>();
        if (root.contains(KEY_PERKS, Tag.TAG_LIST)) {
            ListTag list = root.getList(KEY_PERKS, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                String id = list.getString(i);
                if (id != null && !id.isBlank()) perks.add(id);
            }
        }

        // chosenPerksByTier compound
        Map<Integer, String> byTier = new HashMap<>();
        if (root.contains(KEY_PERKS_BY_TIER, Tag.TAG_COMPOUND)) {
            CompoundTag t = root.getCompound(KEY_PERKS_BY_TIER);
            for (String key : t.getAllKeys()) {
                try {
                    int tier = Integer.parseInt(key);
                    if (tier < 1) continue;
                    String perkId = t.getString(key);
                    if (perkId != null && !perkId.isBlank()) {
                        byTier.put(tier, perkId);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return new RpgProfile(level, xp, balance, classId, spent, perks, byTier);
    }

    public static void save(ServerPlayer player, RpgProfile profile) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT, Tag.TAG_COMPOUND) ? persistent.getCompound(ROOT) : new CompoundTag();

        root.putInt(KEY_LEVEL, profile.level());
        root.putInt(KEY_XP, profile.xp());

        // balance / wallet
        root.putLong(KEY_BALANCE, Math.max(0L, profile.balance()));

        // classId
        if (profile.classId() != null) root.putString(KEY_CLASS_ID, profile.classId());
        else root.remove(KEY_CLASS_ID);

        root.putInt(KEY_PERK_SPENT, profile.perkTokensSpent());

        // chosenPerks list
        ListTag list = new ListTag();
        for (String perkId : profile.chosenPerks()) {
            list.add(StringTag.valueOf(perkId));
        }
        root.put(KEY_PERKS, list);

        // chosenPerksByTier compound
        CompoundTag tiers = new CompoundTag();
        for (var e : profile.
                chosenPerksByTier().entrySet()) {
            Integer tier = e.getKey();
            String perkId = e.getValue();
            if (tier == null || tier < 1) continue;
            if (perkId == null || perkId.isBlank()) continue;
            tiers.putString(Integer.toString(tier), perkId);
        }
        root.put(KEY_PERKS_BY_TIER, tiers);

        persistent.put(ROOT, root);
    }

    public static void reset(ServerPlayer player) {
        save(player, new RpgProfile(0, 0, 0L, null, 0, List.of(), Map.of()));
    }
}