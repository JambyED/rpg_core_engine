package ru.rpgcore.core.access;

import java.util.Objects;

/**
 * Immutable protected target for access control.
 *
 * Examples:
 * - STORAGE : player:uuid:bank
 * - SERVICE : npc:banker_01:bank_menu
 * - NPC     : banker_01
 * - GUILD   : iron_legion
 * - BANK    : spawn_bank
 * - SYSTEM  : admin_panel
 */
public record RpgAccessTarget(
        RpgAccessTargetType type,
        String id
) {
    public RpgAccessTarget {
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