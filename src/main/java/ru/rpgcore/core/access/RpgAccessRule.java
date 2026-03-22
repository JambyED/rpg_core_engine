package ru.rpgcore.core.access;

import java.util.Objects;

/**
 * Immutable access rule:
 * subject -> permission -> target
 *
 * Example:
 * - guild:iron_legion -> OPEN -> storage:guild_main
 * - player:<uuid>     -> WITHDRAW -> storage:player_bank
 */
public record RpgAccessRule(
        RpgAccessSubject subject,
        RpgAccessPermission permission,
        RpgAccessTarget target
) {
    public RpgAccessRule {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(target, "target");
    }

    public String asKey() {
        return subject.asKey() + "|" + permission.name() + "|" + target.asKey();
    }
}