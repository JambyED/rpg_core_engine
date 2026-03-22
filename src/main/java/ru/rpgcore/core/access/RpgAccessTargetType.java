package ru.rpgcore.core.access;

/**
 * Target category for access rules.
 *
 * IMPORTANT:
 * - This identifies WHAT is being protected by access control.
 * - It is intentionally generic so the same model can be reused for:
 *   storages, service GUIs, NPC services, guild systems, banks, etc.
 */
public enum RpgAccessTargetType {
    STORAGE,
    SERVICE,
    NPC,
    GUILD,
    BANK,
    WORLD_OBJECT,
    SYSTEM
}