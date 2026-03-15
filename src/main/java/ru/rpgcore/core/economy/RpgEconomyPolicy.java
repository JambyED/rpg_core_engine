package ru.rpgcore.core.economy;

/**
 * Policy hook for economy operations.
 *
 * Policies may allow or deny an operation before balance changes happen.
 * This is the foundation for future limits, commissions, faction rules,
 * shop restrictions and anti-abuse logic.
 */
@FunctionalInterface
public interface RpgEconomyPolicy {

    RpgEconomyPolicyResult evaluate(RpgEconomyPolicyContext context);
}