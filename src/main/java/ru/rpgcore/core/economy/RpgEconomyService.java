package ru.rpgcore.core.economy;

import net.minecraft.server.level.ServerPlayer;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.profile.RpgProfileStorage;

import java.util.Objects;

/**
 * Central service layer for RPG currency operations.
 *
 * IMPORTANT:
 * - This service does not replace the current wallet stored in RpgProfile.
 * - It wraps profile balance access into one stable API for future systems.
 * - Future systems (bank, shop, guild treasury, taxes, fees, logs) should use this service.
 */
public final class RpgEconomyService {

    private RpgEconomyService() {}

    public enum ResultType {
        SUCCESS,
        INVALID_AMOUNT,
        INSUFFICIENT_FUNDS,
        SAME_PLAYER,
        FAILURE
    }

    public record Result(
            ResultType type,
            long amount,
            long sourceBalance,
            long targetBalance,
            String messageKey
    ) {
        public boolean success() {
            return type == ResultType.SUCCESS;
        }
    }

    /* =========================
       Balance getters
       ========================= */

    public static long getBalance(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return RpgProfileStorage.load(player).balance();
    }

    public static boolean canAfford(ServerPlayer player, long amount) {
        Objects.requireNonNull(player, "player");
        if (amount < 0L) return false;
        return RpgProfileStorage.load(player).canAfford(amount);
    }

    /* =========================
       Direct balance operations
       ========================= */

    public static Result setBalance(ServerPlayer player, long amount) {
        Objects.requireNonNull(player, "player");

        if (amount < 0L) {
            return new Result(
                    ResultType.INVALID_AMOUNT,
                    amount,
                    getBalance(player),
                    getBalance(player),
                    "rpg_core.economy.invalid_amount"
            );
        }

        RpgProfile profile = RpgProfileStorage.load(player);
        profile.setBalance(amount);
        RpgProfileStorage.save(player, profile);

        return new Result(
                ResultType.SUCCESS,
                amount,
                profile.balance(),
                profile.balance(),
                "rpg_core.economy.set.success"
        );
    }

    public static Result deposit(ServerPlayer player, long amount) {
        Objects.requireNonNull(player, "player");

        if (amount < 0L) {
            long current = getBalance(player);
            return new Result(
                    ResultType.INVALID_AMOUNT,
                    amount,
                    current,
                    current,
                    "rpg_core.economy.invalid_amount"
            );
        }

        RpgProfile profile = RpgProfileStorage.load(player);
        long before = profile.balance();

        profile.addBalance(amount);
        RpgProfileStorage.save(player, profile);

        return new Result(
                ResultType.SUCCESS,
                amount,
                before,
                profile.balance(),
                "rpg_core.economy.deposit.success"
        );
    }

    public static Result withdraw(ServerPlayer player, long amount) {
        Objects.requireNonNull(player, "player");

        if (amount < 0L) {
            long current = getBalance(player);
            return new Result(
                    ResultType.INVALID_AMOUNT,
                    amount,
                    current,
                    current,
                    "rpg_core.economy.invalid_amount"
            );
        }

        RpgProfile profile = RpgProfileStorage.load(player);
        long before = profile.balance();

        if (!profile.removeBalance(amount)) {
            return new Result(
                    ResultType.INSUFFICIENT_FUNDS,
                    amount,
                    before,
                    before,
                    "rpg_core.economy.insufficient_funds"
            );
        }
        RpgProfileStorage.save(player, profile);

        return new Result(
                ResultType.SUCCESS,
                amount,
                before,
                profile.balance(),
                "rpg_core.economy.withdraw.success"
        );
    }

    /* =========================
       Transfer operations
       ========================= */

    public static Result transfer(ServerPlayer from, ServerPlayer to, long amount) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        if (amount < 0L) {
            long current = getBalance(from);
            return new Result(
                    ResultType.INVALID_AMOUNT,
                    amount,
                    current,
                    current,
                    "rpg_core.economy.invalid_amount"
            );
        }

        if (from.getUUID().equals(to.getUUID())) {
            long current = getBalance(from);
            return new Result(
                    ResultType.SAME_PLAYER,
                    amount,
                    current,
                    current,
                    "rpg_core.economy.same_player"
            );
        }

        RpgProfile fromProfile = RpgProfileStorage.load(from);
        RpgProfile toProfile = RpgProfileStorage.load(to);

        long fromBefore = fromProfile.balance();
        long toBefore = toProfile.balance();

        if (!fromProfile.removeBalance(amount)) {
            return new Result(
                    ResultType.INSUFFICIENT_FUNDS,
                    amount,
                    fromBefore,
                    toBefore,
                    "rpg_core.economy.insufficient_funds"
            );
        }

        toProfile.addBalance(amount);

        RpgProfileStorage.save(from, fromProfile);
        RpgProfileStorage.save(to, toProfile);

        return new Result(
                ResultType.SUCCESS,
                amount,
                fromProfile.balance(),
                toProfile.balance(),
                "rpg_core.economy.transfer.success"
        );
    }
}