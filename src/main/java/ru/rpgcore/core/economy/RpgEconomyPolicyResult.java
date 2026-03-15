package ru.rpgcore.core.economy;

/**
 * Result of policy evaluation.
 */
public record RpgEconomyPolicyResult(
        boolean allowed,
        String messageKey
) {
    public static RpgEconomyPolicyResult allow() {
        return new RpgEconomyPolicyResult(true, "");
    }

    public static RpgEconomyPolicyResult deny(String messageKey) {
        return new RpgEconomyPolicyResult(
                false,
                messageKey == null || messageKey.isBlank()
                        ? "rpg_core.economy.policy_denied"
                        : messageKey
        );
    }
}