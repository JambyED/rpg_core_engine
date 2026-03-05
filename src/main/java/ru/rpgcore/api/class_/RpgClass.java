package ru.rpgcore.api.class_;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * RPG Class is DATA.
 * Content mods register implementations in the rpg_core:classes registry.
 *
 * IMPORTANT:
 * - This is the CORE API.
 * - Addons should be able to provide UI data (icon, stats) without CORE depending on addons.
 */
public interface RpgClass {

    /** Localized display name. */
    Component displayName();

    /** Optional localized description (may be empty). */
    default Component description() {
        return Component.empty();
    }

    /**
     * Optional icon texture for GUI cards.
     * Recommended size: 32x32 or 64x64.
     * If null -> GUI draws placeholder.
     */
    default ResourceLocation iconTexture() {
        return null;
    }

    /**
     * Optional short stat lines shown on the class card.
     * Example: "+2 HP", "+5% Speed", "Starts with: Sword".
     */
    default List<Component> statsLines() {
        return List.of();
    }
}