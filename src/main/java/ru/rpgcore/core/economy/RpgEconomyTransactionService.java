package ru.rpgcore.core.economy;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * In-memory transaction foundation for economy operations.
 *
 * IMPORTANT:
 * - This is the first foundation layer, not final persistent banking history.
 * - It provides a unified record format and simple runtime storage.
 * - Later it can be moved to SavedData / per-player history / guild treasury logs.
 */
public final class RpgEconomyTransactionService {

    private static final int MAX_HISTORY = 500;
    private static final List<RpgEconomyTransaction> HISTORY = new ArrayList<>();

    private RpgEconomyTransactionService() {}

    public static void record(
            RpgTransactionType type,
            String sourceId,
            String targetId,
            long amount,
            String reason
    ) {
        Objects.requireNonNull(type, "type");

        RpgEconomyTransaction tx = new RpgEconomyTransaction(
                System.currentTimeMillis(),
                type,
                sourceId == null ? "" : sourceId,
                targetId == null ? "" : targetId,
                amount,
                reason == null ? "" : reason
        );

        HISTORY.add(tx);

        if (HISTORY.size() > MAX_HISTORY) {
            HISTORY.remove(0);
        }
    }

    public static void recordPlayerToPlayer(
            RpgTransactionType type,
            ServerPlayer source,
            ServerPlayer target,
            long amount,
            String reason
    ) {
        record(
                type,
                playerId(source),
                playerId(target),
                amount,
                reason
        );
    }

    public static void recordForPlayer(
            RpgTransactionType type,
            ServerPlayer player,
            long amount,
            String reason
    ) {
        record(
                type,
                playerId(player),
                "",
                amount,
                reason
        );
    }

    public static List<RpgEconomyTransaction> recent() {
        return List.copyOf(HISTORY);
    }

    public static List<RpgEconomyTransaction> recentReversed() {
        List<RpgEconomyTransaction> copy = new ArrayList<>(HISTORY);
        Collections.reverse(copy);
        return copy;
    }

    public static void clearAll() {
        HISTORY.clear();
    }

    public static int size() {
        return HISTORY.size();
    }

    private static String playerId(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return "player:" + player.getUUID();
    }
}