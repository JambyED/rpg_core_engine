package ru.rpgcore.core.storage;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * Central factory for building well-known storage owner refs.
 *
 * IMPORTANT:
 * - Players never build these refs manually in gameplay.
 * - Addons / core systems use these helpers to access storages consistently.
 * - This is an address/access layer, not a gameplay permission layer.
 */
public final class RpgStorageRefs {

    private RpgStorageRefs() {}

    /* =========================
       Generic builder
       ========================= */

    public static RpgStorageOwnerRef named(RpgStorageOwnerType ownerType, String ownerId, String suffix) {
        Objects.requireNonNull(ownerType, "ownerType");
        return new RpgStorageOwnerRef(ownerType, compose(ownerId, suffix));
    }

    /* =========================
       Player storages
       ========================= */

    public static RpgStorageOwnerRef playerTest(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return playerNamed(player, "test");
    }

    public static RpgStorageOwnerRef playerBank(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return playerNamed(player, "bank");
    }

    public static RpgStorageOwnerRef playerMailbox(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return playerNamed(player, "mailbox");
    }

    public static RpgStorageOwnerRef playerQuestStorage(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return playerNamed(player, "quest");
    }

    public static RpgStorageOwnerRef playerNamed(ServerPlayer player, String suffix) {
        Objects.requireNonNull(player, "player");
        return playerNamed(player.getUUID().toString(), suffix);
    }

    public static RpgStorageOwnerRef playerNamed(String playerId, String suffix) {
        return named(RpgStorageOwnerType.PLAYER, playerId, suffix);
    }

    /* =========================
       Guild storages
       ========================= */

    public static RpgStorageOwnerRef guildMain(String guildId) {
        return guildNamed(guildId, "main");
    }

    public static RpgStorageOwnerRef guildTreasury(String guildId) {
        return guildNamed(guildId, "treasury");
    }

    public static RpgStorageOwnerRef guildOfficers(String guildId) {
        return guildNamed(guildId, "officers");
    }

    public static RpgStorageOwnerRef guildNamed(String guildId, String suffix) {
        return named(RpgStorageOwnerType.GUILD, guildId, suffix);
    }

    /* =========================
       Faction storages
       ========================= */

    public static RpgStorageOwnerRef factionMain(String factionId) {
        return factionNamed(factionId, "main");
    }

    public static RpgStorageOwnerRef factionTreasury(String factionId) {
        return factionNamed(factionId, "treasury");
    }

    public static RpgStorageOwnerRef factionWarehouse(String factionId) {
        return factionNamed(factionId, "warehouse");
    }

    public static RpgStorageOwnerRef factionNamed(String factionId, String suffix) {
        return named(RpgStorageOwnerType.FACTION, factionId, suffix);
    }

    /* =========================
       NPC storages
       ========================= */

    public static RpgStorageOwnerRef npcStorage(String npcId) {
        return npcNamed(npcId, "storage");
    }

    public static RpgStorageOwnerRef npcShop(String npcId) {
        return npcNamed(npcId, "shop");
    }

    public static RpgStorageOwnerRef npcBank(String npcId) {
        return npcNamed(npcId, "bank");
    }

    public static RpgStorageOwnerRef npcNamed(String npcId, String suffix) {
        return named(RpgStorageOwnerType.NPC, npcId, suffix);
    }

    /* =========================
       World storages
       ========================= */

    public static RpgStorageOwnerRef worldStorage(String worldId) {
        return worldNamed(worldId, "storage");
    }

    public static RpgStorageOwnerRef worldBank(String worldId) {
        return worldNamed(worldId, "bank");
    }

    public static RpgStorageOwnerRef worldNamed(String worldId, String suffix) {
        return named(RpgStorageOwnerType.WORLD, worldId, suffix);
    }

    /* =========================
       Generic helpers
       ========================= */

    public static String ownerKey(RpgStorageOwnerRef ref) {
        Objects.requireNonNull(ref, "ref");
        return ref.asKey();
    }

    public static String storageId(RpgStorageOwnerRef ref) {
        Objects.requireNonNull(ref, "ref");
        return RpgStorage.defaultStorageId(ref);
    }

    private static String compose(String baseId, String suffix) {
        String a = normalizePart(baseId, "baseId");
        String b = normalizePart(suffix, "suffix");
        return a + ":" + b;
    }

    private static String normalizePart(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        String s = value.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }

        return s;
    }
}