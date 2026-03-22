package ru.rpgcore.core.access;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Objects;

/**
 * Core permission evaluation engine.
 *
 * Determines whether a player has a specific permission for a target.
 */
public final class RpgAccessEvaluator {

    private RpgAccessEvaluator() {}

    /**
     * Checks if the player has the given permission for the target.
     */
    public static boolean hasPermission(
            ServerPlayer player,
            RpgAccessPermission permission,
            RpgAccessTarget target
    ) {

        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(target, "target");

        List<RpgAccessSubject> subjects =
                RpgAccessService.resolveSubjects(player);

        for (RpgAccessSubject subject : subjects) {

            List<RpgAccessRule> rules =
                    RpgAccessRuleRegistry.getRulesForSubject(subject);

            for (RpgAccessRule rule : rules) {

                if (rule.permission() == permission &&
                        rule.target().equals(target)) {

                    return true;
                }
            }
        }

        return false;
    }
}