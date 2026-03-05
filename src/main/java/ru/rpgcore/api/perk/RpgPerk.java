package ru.rpgcore.api.perk;

import net.minecraft.network.chat.Component;

/**
 * Perk is data. Effects will be added later (Stage 4+).
 * Registered in Forge registry: rpg_core:perks
 */
public interface RpgPerk {
    /** Tier 1..8 (every 5 levels). */
    int tier();

    /** Localized display name. */
    Component displayName();
}