package ru.rpgcore.core.economy;

import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/**
 * Immutable context for evaluating an economy operation.
 */
public record RpgEconomyPolicyContext(
        ServerLevel level,
        RpgTransactionType transactionType,
        String sourceId,
        String targetId,
        long amount,
        String reason
) {
    public RpgEconomyPolicyContext {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(transactionType, "transactionType");
        sourceId = sourceId == null ? "" : sourceId;
        targetId = targetId == null ? "" : targetId;
        reason = reason == null ? "" : reason;
    }
}