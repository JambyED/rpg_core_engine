package ru.rpgcore.core.access;

/**
 * Subject category for access control.
 *
 * IMPORTANT:
 * - This is not a permission itself.
 * - This identifies WHO or WHAT participates in access resolution.
 */
public enum RpgAccessSubjectType {
    PLAYER,
    GUILD,
    FACTION,
    NPC,
    WORLD,
    SERVER
}