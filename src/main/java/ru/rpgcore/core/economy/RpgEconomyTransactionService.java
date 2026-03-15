package ru.rpgcore.core.economy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Persistent transaction service for economy operations.
 */
public final class RpgEconomyTransactionService {

    private static final int MAX_HISTORY = 500;

    private RpgEconomyTransactionService() {}

    private static RpgEconomyTransactionSavedData getData(ServerLevel level) {
        Objects.requireNonNull(level);

        return level.getDataStorage().computeIfAbsent(
                RpgEconomyTransactionSavedData::load,
                RpgEconomyTransactionSavedData::new,
                RpgEconomyTransactionSavedData.DATA_NAME
        );
    }

    public static void record(
            ServerLevel level,
            RpgTransactionType type,
            String sourceId,
            String targetId,
            long amount,
            String reason
    ) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(type);

        RpgEconomyTransaction tx = new RpgEconomyTransaction(
                System.currentTimeMillis(),
                type,
                sourceId == null ? "" : sourceId,
                targetId == null ? "" : targetId,
                amount,
                reason == null ? "" : reason
        );

        RpgEconomyTransactionSavedData data = getData(level);

        data.add(tx);

        while (data.size() > MAX_HISTORY) {
            data.removeFirst();
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
                source.serverLevel(),
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
                player.serverLevel(),
                type,
                playerId(player),
                "",
                amount,
                reason
        );
    }

    /* =========================
       HISTORY API
       ========================= */

    public static List<RpgEconomyTransaction> getRecent(ServerLevel level) {
        return List.copyOf(getData(level).transactions());
    }

    public static List<RpgEconomyTransaction> getRecent(ServerLevel level, int limit) {

        List<RpgEconomyTransaction> all = getData(level).transactions();

        int size = all.size();

        if (size <= limit) {
            return List.copyOf(all);
        }

        return List.copyOf(all.subList(size - limit, size));
    }

    public static List<RpgEconomyTransaction> getPlayerHistory(ServerLevel level, String playerId) {

        List<RpgEconomyTransaction> result = new ArrayList<>();

        for (RpgEconomyTransaction tx : getData(level).transactions()) {

            if (playerId.equals(tx.sourceId()) || playerId.equals(tx.targetId())) {
                result.add(tx);
            }
        }

        return result;
    }

    public static List<RpgEconomyTransaction> getPlayerHistory(ServerPlayer player) {

        String id = playerId(player);

        return getPlayerHistory(player.serverLevel(), id);
    }

    public static List<RpgEconomyTransaction> getPlayerHistory(ServerPlayer player, int limit) {

        List<RpgEconomyTransaction> history = getPlayerHistory(player);

        int size = history.size();

        if (size <= limit) {
            return history;
        }

        return history.subList(size - limit, size);
    }

    public static void clearAll(ServerLevel level) {
        getData(level).clearAll();
    }

    public static int size(ServerLevel level) {
        return getData(level).size();
    }
    private static String playerId(ServerPlayer player) {
        Objects.requireNonNull(player);
        return "player:" + player.getUUID();
    }
}