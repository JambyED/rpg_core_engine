package ru.rpgcore.core.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Central registry/service for economy policies.
 *
 * By default, when no policies are registered, all operations are allowed.
 * Future systems may register limits, fees, bank rules, shop rules, etc.
 */
public final class RpgEconomyPolicyService {

    private static final List<RpgEconomyPolicy> POLICIES = new ArrayList<>();

    private RpgEconomyPolicyService() {}

    public static void register(RpgEconomyPolicy policy) {
        Objects.requireNonNull(policy, "policy");
        POLICIES.add(policy);
    }

    public static void clearAll() {
        POLICIES.clear();
    }

    public static int size() {
        return POLICIES.size();
    }

    public static RpgEconomyPolicyResult evaluate(RpgEconomyPolicyContext context) {
        Objects.requireNonNull(context, "context");

        for (RpgEconomyPolicy policy : POLICIES) {
            RpgEconomyPolicyResult result = policy.evaluate(context);
            if (result != null && !result.allowed()) {
                return result;
            }
        }

        return RpgEconomyPolicyResult.allow();
    }
}