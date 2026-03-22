package ru.rpgcore.core.access;

import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Central persistent registry for access rules.
 *
 * IMPORTANT:
 * - Rules are stored per world via SavedData.
 * - This replaces the old in-memory-only registry behavior.
 */
public final class RpgAccessRuleRegistry {

    private RpgAccessRuleRegistry() {}

    private static RpgAccessRuleSavedData getData(ServerLevel level) {
        Objects.requireNonNull(level, "level");

        return level.getDataStorage().computeIfAbsent(
                RpgAccessRuleSavedData::load,
                RpgAccessRuleSavedData::new,
                RpgAccessRuleSavedData.DATA_NAME
        );
    }

    /**
     * Registers a new persistent access rule.
     */
    public static void addRule(ServerLevel level, RpgAccessRule rule) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(rule, "rule");

        getData(level).put(rule);
    }

    /**
     * Removes a persistent rule.
     */
    public static void removeRule(ServerLevel level, RpgAccessRule rule) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(rule, "rule");

        getData(level).remove(rule);
    }

    /**
     * Clears all rules in the current world.
     */
    public static void clear(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        getData(level).clearAll();
    }

    /**
     * Returns all registered rules for the current world.
     */
    public static Collection<RpgAccessRule> getAllRules(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return List.copyOf(getData(level).rules());
    }

    /**
     * Returns rules affecting a specific target.
     */
    public static List<RpgAccessRule> getRulesForTarget(ServerLevel level, RpgAccessTarget target) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(target, "target");

        List<RpgAccessRule> result = new ArrayList<>();

        for (RpgAccessRule rule : getData(level).rules()) {
            if (rule.target().equals(target)) {
                result.add(rule);
            }
        }

        return result;
    }

    /**
     * Returns rules assigned to a specific subject.
     */
    public static List<RpgAccessRule> getRulesForSubject(ServerLevel level, RpgAccessSubject subject) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(subject, "subject");

        List<RpgAccessRule> result = new ArrayList<>();

        for (RpgAccessRule rule : getData(level).rules()) {
            if (rule.subject().equals(subject)) {
                result.add(rule);
            }
        }

        return result;
    }

    public static int size(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return getData(level).size();
    }
}