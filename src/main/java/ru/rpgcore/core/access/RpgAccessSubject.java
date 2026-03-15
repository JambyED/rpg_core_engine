package ru.rpgcore.core.access;

import java.util.Objects;

/**
 * Immutable identity for access resolution.
 *
 * Examples:
 * - PLAYER : <player-uuid>
 * - GUILD  : iron_legion
 * - FACTION: north_realm
 * - NPC    : banker_01
 * - WORLD  : spawn
 * - SERVER : global
 */
public record RpgAccessSubject(
        RpgAccessSubjectType type,
        String id
) {
    public RpgAccessSubject {
        Objects.requireNonNull(type, "type");

        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        id = id.trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
    }

    public String asKey() {
        return type.name().toLowerCase() + ":" + id;
    }
}