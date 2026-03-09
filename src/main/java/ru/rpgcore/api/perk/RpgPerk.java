package ru.rpgcore.api.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Perk is DATA. Effects will be added later (Stage 4+).
 * Registered in Forge registry: rpg_core:perks
 *
 * IMPORTANT:
 * - This is the CORE API.
 * - Addons should be able to provide UI data (icon, description) without CORE depending on addon internals.
 */
public interface RpgPerk {

    /** Tier 1..N (every 5 levels in current design). */
    int tier();

    /** Localized display name. */
    Component displayName();

    /** Optional localized description shown in perk cards / tooltips. */
    default Component description() {
        return Component.empty();
    }

    /**
     * Optional icon texture for GUI cards.
     * Recommended size: 16x16, 32x32 or 64x64.
     * If null -> GUI draws placeholder.
     */
    default ResourceLocation iconTexture() {
        return null;
    }
}