package ru.rpgcore.core.access;

/**
 * Standard access permissions used by RPG Core.
 *
 * IMPORTANT:
 * - These are generic permission actions, not tied only to storages.
 * - The same permission vocabulary should be reusable for:
 *   storage, service GUIs, NPC services, guild systems, banks, etc.
 */
public enum RpgAccessPermission {

    /**
     * Subject may see that an object/service exists.
     */
    VIEW,

    /**
     * Subject may open or enter the object/service.
     */
    OPEN,

    /**
     * Subject may place or contribute something into the target.
     */
    DEPOSIT,

    /**
     * Subject may take or receive something from the target.
     */
    WITHDRAW,

    /**
     * Subject may manage settings or operational state.
     */
    MANAGE,

    /**
     * Subject may change size/capacity of the target, where applicable.
     */
    RESIZE,

    /**
     * Subject may assign or modify access rights for other subjects.
     */
    ASSIGN
}