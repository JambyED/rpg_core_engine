package ru.rpgcore.core.profile;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public final class RpgProfile {
    private int level;
    private int xp; // total XP

    // Currency / wallet
    private long balance;

    // Class (stored as string "modid:class", null = not chosen)
    private String classId;

    // Perks (Stage 3)
    private int perkTokensSpent;            // how many tokens were spent
    private final List<String> chosenPerks; // perk ids (e.g. "rpg_content:toughness")

    // Tier picks: one chosen perk per tier
    // key = tier (>=1), value = perkId string ("modid:perk")
    private final Map<Integer, String> chosenPerkByTier;

    /** Backward compatible constructor (very old saves). */
    public RpgProfile(int level, int xp, int perkTokensSpent, List<String> chosenPerks) {
        this(level, xp, 0L, null, perkTokensSpent, chosenPerks, null);
    }

    /** Backward compatible constructor (older saves with class, but without wallet). */
    public RpgProfile(int level,
                      int xp,
                      String classId,
                      int perkTokensSpent,
                      List<String> chosenPerks,
                      Map<Integer, String> chosenPerkByTier) {
        this(level, xp, 0L, classId, perkTokensSpent, chosenPerks, chosenPerkByTier);
    }

    /** Full constructor (used by storage). */
    public RpgProfile(int level,
                      int xp,
                      long balance,
                      String classId,
                      int perkTokensSpent,
                      List<String> chosenPerks,
                      Map<Integer, String> chosenPerkByTier) {
        this.level = Math.max(0, level);
        this.xp = Math.max(0, xp);

        this.balance = Math.max(0L, balance);

        this.classId = normalizeIdOrNull(classId);

        this.perkTokensSpent = Math.max(0, perkTokensSpent);
        this.chosenPerks = new ArrayList<>(chosenPerks == null ? List.of() : chosenPerks);

        this.chosenPerkByTier = new HashMap<>();
        if (chosenPerkByTier != null) {
            for (var e : chosenPerkByTier.entrySet()) {
                Integer tier = e.getKey();
                String perkId = e.getValue();
                if (tier == null) continue;
                if (tier < 1) continue;
                perkId = normalizeIdOrNull(perkId);
                if (perkId == null) continue;
                this.chosenPerkByTier.put(tier, perkId);
            }
        }
    }

    private static String normalizeIdOrNull(String id) {
        if (id == null) return null;
        id = id.trim();
        if (id.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        return (rl == null) ? null : rl.toString();
    }

    /* ===== Level / XP ===== */

    public int level() {
        return level;
    }

    public int xp() {
        return xp;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }

    public void setXp(int xp) {
        this.xp = Math.max(0, xp);
    }

    /* ===== Currency / Wallet ===== */

    public long balance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = Math.max(0L, balance);
    }

    public boolean canAfford(long amount) {
        return amount >= 0L && balance >= amount;
    }

    public void addBalance(long amount) {
        if (amount <= 0L) return;

        long next;
        if (Long.MAX_VALUE - balance < amount) next = Long.MAX_VALUE;
        else next = balance + amount;

        this.balance = Math.max(0L, next);
    }

    /**
     * Removes up to the requested amount.
     * @return true if the full amount was removed, false if not enough balance
     */
    public boolean removeBalance(long amount) {
        if (amount < 0L) return false;
        if (amount == 0L) return true;
        if (!canAfford(amount)) return false;

        this.balance -= amount;
        if (this.balance < 0L) this.balance = 0L;
        return true;
    }
    public void clearBalance() {
        this.balance = 0L;
    }

    /* ===== Class ===== */

    public String classId() {
        return classId;
    }

    public ResourceLocation classIdAsRL() {
        return classId == null ? null : ResourceLocation.tryParse(classId);
    }

    public boolean hasClass() {
        return classId != null;
    }

    public void setClassId(String classId) {
        this.classId = normalizeIdOrNull(classId);
    }

    public void clearClass() {
        this.classId = null;
    }

    /* ===== Perk Tokens ===== */

    /** Total tokens granted by level. 1 token every 5 levels. */
    public int totalPerkTokensGranted() {
        if (level <= 0) return 0;
        return level / 5;
    }

    /** Tokens currently available to spend. */
    public int perkTokensAvailable() {
        return Math.max(0, totalPerkTokensGranted() - perkTokensSpent);
    }

    /** How many tokens have been spent. */
    public int perkTokensSpent() {
        return perkTokensSpent;
    }

    /** Increase spent tokens by N (used when choosing perks). */
    public void spendPerkTokens(int amount) {
        if (amount <= 0) return;
        this.perkTokensSpent = Math.max(0, this.perkTokensSpent + amount);
    }

    /** Set spent tokens directly (used on reset/recalc). */
    public void setPerkTokensSpent(int spent) {
        this.perkTokensSpent = Math.max(0, spent);
    }

    /* ===== Chosen Perks ===== */

    /** Immutable view (read-only). */
    public List<String> chosenPerks() {
        return Collections.unmodifiableList(chosenPerks);
    }

    public boolean hasPerk(String perkId) {
        return chosenPerks.contains(perkId);
    }

    public void addPerk(String perkId) {
        perkId = normalizeIdOrNull(perkId);
        if (perkId == null) return;
        if (!chosenPerks.contains(perkId)) chosenPerks.add(perkId);
    }

    public void removePerk(String perkId) {
        chosenPerks.remove(perkId);
        if (perkId != null && !perkId.isBlank()) {
            chosenPerkByTier.entrySet().removeIf(e -> perkId.equals(e.getValue()));
        }
    }

    public void clearPerks() {
        chosenPerks.clear();
        chosenPerkByTier.clear();
    }

    /* ===== Tier Picks ===== */

    /** Read-only map view: tier -> perkId */
    public Map<Integer, String> chosenPerksByTier() {
        return Collections.unmodifiableMap(chosenPerkByTier);
    }

    public boolean hasChosenTier(int tier) {
        if (tier < 1) return false;
        return chosenPerkByTier.containsKey(tier);
    }

    public String chosenPerkForTier(int tier) {
        if (tier < 1) return null;
        return chosenPerkByTier.get(tier);
    }

    public void setChosenPerkForTier(int tier, String perkId) {
        if (tier < 1) return;
        perkId = normalizeIdOrNull(perkId);
        if (perkId == null) return;
        chosenPerkByTier.put(tier, perkId);
    }

    public void clearPerksByTier() {
        chosenPerkByTier.clear();
    }
}