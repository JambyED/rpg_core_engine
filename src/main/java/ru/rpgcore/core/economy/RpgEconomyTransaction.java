package ru.rpgcore.core.economy;

import net.minecraft.nbt.CompoundTag;

public record RpgEconomyTransaction(
        long timestamp,
        RpgTransactionType type,
        String sourceId,
        String targetId,
        long amount,
        String reason
) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("timestamp", timestamp);
        tag.putString("type", type.name());
        tag.putString("sourceId", sourceId == null ? "" : sourceId);
        tag.putString("targetId", targetId == null ? "" : targetId);
        tag.putLong("amount", amount);
        tag.putString("reason", reason == null ? "" : reason);
        return tag;
    }

    public static RpgEconomyTransaction load(CompoundTag tag) {
        long timestamp = tag.getLong("timestamp");

        RpgTransactionType type;
        try {
            type = RpgTransactionType.valueOf(tag.getString("type"));
        } catch (Exception e) {
            type = RpgTransactionType.OTHER;
        }

        String sourceId = tag.getString("sourceId");
        String targetId = tag.getString("targetId");
        long amount = tag.getLong("amount");
        String reason = tag.getString("reason");

        return new RpgEconomyTransaction(
                timestamp,
                type,
                sourceId,
                targetId,
                amount,
                reason
        );
    }
}