package ru.rpgcore.core.access;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory registry for access rules.
 *
 * IMPORTANT:
 * This is the foundation layer for permissions.
 * Persistence and world storage will be added later.
 */
public final class RpgAccessRuleRegistry {

    private static final Map<String, RpgAccessRule> RULES = new ConcurrentHashMap<>();

    private RpgAccessRuleRegistry() {}

    /**
     * Registers a new access rule.
     */
    public static void addRule(RpgAccessRule rule) {
        Objects.requireNonNull(rule, "rule");
        RULES.put(rule.asKey(), rule);
    }

    /**
     * Removes a rule.
     */
    public static void removeRule(RpgAccessRule rule) {
        Objects.requireNonNull(rule, "rule");
        RULES.remove(rule.asKey());
    }

    /**
     * Clears all rules (mainly for testing).
     */
    public static void clear() {
        RULES.clear();
    }

    /**
     * Returns all registered rules.
     */
    public static Collection<RpgAccessRule> getAllRules() {
        return Collections.unmodifiableCollection(RULES.values());
    }

    /**
     * Returns rules affecting a specific target.
     */
    public static List<RpgAccessRule> getRulesForTarget(RpgAccessTarget target) {
        Objects.requireNonNull(target, "target");

        List<RpgAccessRule> result = new ArrayList<>();

        for (RpgAccessRule rule : RULES.values()) {
            if (rule.target().equals(target)) {
                result.add(rule);
            }
        }

        return result;
    }

    /**
     * Returns rules assigned to a specific subject.
     */
    public static List<RpgAccessRule> getRulesForSubject(RpgAccessSubject subject) {
        Objects.requireNonNull(subject, "subject");

        List<RpgAccessRule> result = new ArrayList<>();

        for (RpgAccessRule rule : RULES.values()) {
            if (rule.subject().equals(subject)) {
                result.add(rule);
            }
        }

        return result;
    }
}