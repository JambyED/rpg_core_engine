package ru.rpgcore.core.storage;

/**
 * Storage owner category.
 *
 * IMPORTANT:
 * - This is NOT a registry type.
 * - This is a runtime ownership discriminator for concrete storages.
 */
public enum RpgStorageOwnerType {
    PLAYER,
    NPC,
    GUILD,
    FACTION,
    WORLD
}