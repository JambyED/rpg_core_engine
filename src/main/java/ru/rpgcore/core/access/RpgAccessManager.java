package ru.rpgcore.core.access;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Objects;

/**
 * High-level API for managing access rules.
 *
 * This is the main entry point for:
 * - adding/removing permissions
 * - querying access rules
 * - bridging core systems (storage, NPC, guild, services)
 */
public final class RpgAccessManager {

    private RpgAccessManager() {}

    /* =========================
       Rule Management
       ========================= */

    public static void grant(
            ServerLevel level,
            RpgAccessSubject subject,
            RpgAccessPermission permission,
            RpgAccessTarget target
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(target, "target");

        RpgAccessRule rule = new RpgAccessRule(subject, permission, target);
        RpgAccessRuleRegistry.addRule(level, rule);
    }

    public static void revoke(
            ServerLevel level,
            RpgAccessSubject subject,
            RpgAccessPermission permission,
            RpgAccessTarget target
    ) {
        Objects.requireNonNull(level, "level");

        RpgAccessRule rule = new RpgAccessRule(subject, permission, target);
        RpgAccessRuleRegistry.removeRule(level, rule);
    }

    public static void clearAll(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        RpgAccessRuleRegistry.clear(level);
    }

    /* =========================
       Queries
       ========================= */

    public static List<RpgAccessRule> getRulesForTarget(
            ServerLevel level,
            RpgAccessTarget target
    ) {
        return RpgAccessRuleRegistry.getRulesForTarget(level, target);
    }

    public static List<RpgAccessRule> getRulesForSubject(
            ServerLevel level,
            RpgAccessSubject subject
    ) {
        return RpgAccessRuleRegistry.getRulesForSubject(level, subject);
    }

    public static int size(ServerLevel level) {
        return RpgAccessRuleRegistry.size(level);
    }

    /* =========================
       Permission Check
       ========================= */

    public static boolean hasPermission(
            ServerPlayer player,
            RpgAccessPermission permission,
            RpgAccessTarget target
    ) {
        return RpgAccessEvaluator.hasPermission(player, permission, target);
    }
}