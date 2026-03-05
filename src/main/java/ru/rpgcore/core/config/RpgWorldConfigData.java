package ru.rpgcore.core.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public final class RpgWorldConfigData extends SavedData {
    private static final String DATA_NAME = "rpg_core_world_config";

    // NBT keys
    private static final String KEY_MAX_LEVEL = "maxLevel";
    private static final String KEY_MAX_TIERS = "maxTiers";
    private static final String KEY_MOB_XP_RULES = "mobXpRules";

    // NEW: XP curve keys
    private static final String KEY_XP_BASE = "xpBase";
    private static final String KEY_XP_LINEAR = "xpLinear";
    private static final String KEY_XP_QUAD_PERMILLE = "xpQuadPermille"; // quad coefficient scaled by 1000

    private int maxLevel = 40; // default, must be multiple of 5
    private int maxTiers = 8;  // default tiers (40 levels -> 8 tiers)

    // XP curve coefficients (world-configurable)
    // xpToNext(level) = base + linear*level + (quadPermille/1000.0) * level^2
    // Defaults match your old behavior: xpToNext = 200 + 100*level
    private int xpBase = 200;
    private int xpLinear = 100;
    private int xpQuadPermille = 0;

    private final Map<ResourceLocation, List<MobXpRule>> mobXpRules = new HashMap<>();

    private RpgWorldConfigData() {}

    public static RpgWorldConfigData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(RpgWorldConfigData::load, RpgWorldConfigData::new, DATA_NAME);
    }

    /* ===== Max Level ===== */

    public int getMaxLevel() { return maxLevel; }

    /** NOTE: validation (multiple of 5) is enforced by command layer. */
    public void setMaxLevel(int value) {
        this.maxLevel = value;
        setDirty();
    }

    /* ===== Max Tiers ===== */

    public int getMaxTiers() {
        return Math.max(1, maxTiers);
    }

    public void setMaxTiers(int value) {
        this.maxTiers = Math.max(1, value);
        setDirty();
    }

    /* ===== XP Curve ===== */

    public int getXpBase() { return xpBase; }
    public int getXpLinear() { return xpLinear; }
    public int getXpQuadPermille() { return xpQuadPermille; }

    /** base must be >= 0 */
    public void setXpBase(int value) {
        this.xpBase = Math.max(0, value);
        setDirty();
    }

    /** linear must be >= 0 */
    public void setXpLinear(int value) {
        this.xpLinear = Math.max(0, value);
        setDirty();
    }

    /**
     * quad coefficient scaled by 1000.
     * Example: 250 means 0.25 * level^2
     */
    public void setXpQuadPermille(int value) {
        this.xpQuadPermille = Math.max(0, value);
        setDirty();
    }

    /* ===== Mob XP rules ===== */

    public List<MobXpRule> getRules(ResourceLocation entityId) {
        return mobXpRules.getOrDefault(entityId, List.of());
    }

    public void upsertRule(MobXpRule rule) {
        List<MobXpRule> list = mobXpRules.computeIfAbsent(rule.entityId(), k -> new ArrayList<>());

        for (int i = 0; i < list.size(); i++) {
            MobXpRule existing = list.get(i);
            if (existing.sameKey(rule.entityId(), rule.nbtPredicate())) {
                list.set(i, rule);
                setDirty();
                return;
            }
        }

        list.add(rule);
        setDirty();
    }

    public boolean removeRule(ResourceLocation entityId, CompoundTag predicateOrNull) {
        List<MobXpRule> list = mobXpRules.get(entityId);
        if (list == null || list.isEmpty()) return false;

        boolean removed = list.removeIf(r -> r.sameKey(entityId, predicateOrNull));
        if (removed) {
            if (list.isEmpty()) mobXpRules.remove(entityId);
            setDirty();
        }
        return removed;
    }

    // choose best matching predicate rule; fallback to default (no predicate)
    public int resolveMobXp(ResourceLocation entityId, CompoundTag entityNbt) {
        List<MobXpRule> list = mobXpRules.
                get(entityId);
        if (list == null || list.isEmpty()) return 0;

        MobXpRule bestPredicate = null;
        int bestSize = -1;
        MobXpRule defaultRule = null;

        for (MobXpRule rule : list) {
            if (!rule.hasPredicate()) {
                defaultRule = rule;
                continue;
            }
            if (rule.matchesEntityNbt(entityNbt)) {
                int size = rule.nbtPredicate().getAllKeys().size();
                if (size > bestSize) {
                    bestSize = size;
                    bestPredicate = rule;
                }
            }
        }

        if (bestPredicate != null) return bestPredicate.xp();
        if (defaultRule != null) return defaultRule.xp();
        return 0;
    }

    public Map<ResourceLocation, List<MobXpRule>> snapshotAllRules() {
        Map<ResourceLocation, List<MobXpRule>> out = new TreeMap<>(Comparator.comparing(ResourceLocation::toString));
        for (var e : mobXpRules.entrySet()) out.put(e.getKey(), List.copyOf(e.getValue()));
        return out;
    }

    /* ===== SavedData ===== */

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(KEY_MAX_LEVEL, maxLevel);
        tag.putInt(KEY_MAX_TIERS, getMaxTiers());

        // XP curve
        tag.putInt(KEY_XP_BASE, getXpBase());
        tag.putInt(KEY_XP_LINEAR, getXpLinear());
        tag.putInt(KEY_XP_QUAD_PERMILLE, getXpQuadPermille());

        ListTag all = new ListTag();
        for (var e : mobXpRules.entrySet()) {
            for (MobXpRule rule : e.getValue()) {
                all.add(rule.toNbt());
            }
        }
        tag.put(KEY_MOB_XP_RULES, all);
        return tag;
    }

    private static RpgWorldConfigData load(CompoundTag tag) {
        RpgWorldConfigData data = new RpgWorldConfigData();

        if (tag.contains(KEY_MAX_LEVEL, Tag.TAG_INT)) data.maxLevel = tag.getInt(KEY_MAX_LEVEL);
        if (tag.contains(KEY_MAX_TIERS, Tag.TAG_INT)) data.maxTiers = Math.max(1, tag.getInt(KEY_MAX_TIERS));

        // XP curve (defaults preserved if keys missing)
        if (tag.contains(KEY_XP_BASE, Tag.TAG_INT)) data.xpBase = Math.max(0, tag.getInt(KEY_XP_BASE));
        if (tag.contains(KEY_XP_LINEAR, Tag.TAG_INT)) data.xpLinear = Math.max(0, tag.getInt(KEY_XP_LINEAR));
        if (tag.contains(KEY_XP_QUAD_PERMILLE, Tag.TAG_INT)) data.xpQuadPermille = Math.max(0, tag.getInt(KEY_XP_QUAD_PERMILLE));

        if (tag.contains(KEY_MOB_XP_RULES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(KEY_MOB_XP_RULES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                MobXpRule rule = MobXpRule.fromNbt(list.getCompound(i));
                data.mobXpRules.computeIfAbsent(rule.entityId(), k -> new ArrayList<>()).add(rule);
            }
        }

        return data;
    }
}