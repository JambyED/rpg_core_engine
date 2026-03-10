package ru.rpgcore.core.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Locale;
import java.util.Objects;

/**
 * Runtime reference to a storage owner.
 *
 * Examples:
 * - PLAYER + UUID string
 * - GUILD + "iron_legion"
 * - NPC + "village_banker_01"
 */
public final class RpgStorageOwnerRef {
    private static final String KEY_TYPE = "type";
    private static final String KEY_ID = "id";

    private final RpgStorageOwnerType ownerType;
    private final String ownerId;

    public RpgStorageOwnerRef(RpgStorageOwnerType ownerType, String ownerId) {
        this.ownerType = Objects.requireNonNull(ownerType, "ownerType");
        this.ownerId = normalizeOwnerId(ownerId);
        if (this.ownerId == null) {
            throw new IllegalArgumentException("ownerId cannot be null/blank");
        }
    }

    public RpgStorageOwnerType ownerType() {
        return ownerType;
    }

    public String ownerId() {
        return ownerId;
    }

    /**
     * Stable composite key for maps / persistence.
     */
    public String asKey() {
        return ownerType.name().toLowerCase(Locale.ROOT) + ":" + ownerId;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString(KEY_TYPE, ownerType.name());
        tag.putString(KEY_ID, ownerId);
        return tag;
    }

    public static RpgStorageOwnerRef fromNbt(CompoundTag tag) {
        if (tag == null) return null;
        if (!tag.contains(KEY_TYPE, Tag.TAG_STRING)) return null;
        if (!tag.contains(KEY_ID, Tag.TAG_STRING)) return null;

        String rawType = tag.getString(KEY_TYPE);
        String rawId = tag.getString(KEY_ID);

        if (rawType == null || rawType.isBlank()) return null;
        if (rawId == null || rawId.isBlank()) return null;

        try {
            RpgStorageOwnerType type = RpgStorageOwnerType.valueOf(rawType);
            return new RpgStorageOwnerRef(type, rawId);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String normalizeOwnerId(String id) {
        if (id == null) return null;
        id = id.trim();
        if (id.isEmpty()) return null;
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RpgStorageOwnerRef other)) return false;
        return ownerType == other.ownerType && ownerId.equals(other.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerType, ownerId);
    }

    @Override
    public String toString() {
        return asKey();
    }
}