package ru.rpgcore.core.perk;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.rpgcore.api.perk.RpgPerk;

/**
 * Minimal immutable perk implementation used by CORE debug content.
 * Real addons may provide their own implementations later.
 */
public final class SimplePerk implements RpgPerk {

    private final int tier;
    private final Component displayName;
    private final Component description;
    private final ResourceLocation iconTexture;

    public SimplePerk(int tier, Component displayName) {
        this(tier, displayName, Component.empty(), null);
    }

    public SimplePerk(int tier, Component displayName, Component description, ResourceLocation iconTexture) {
        this.tier = Math.max(1, tier);
        this.displayName = displayName == null ? Component.empty() : displayName;
        this.description = description == null ? Component.empty() : description;
        this.iconTexture = iconTexture;
    }

    @Override
    public int tier() {
        return tier;
    }

    @Override
    public Component displayName() {
        return displayName;
    }

    @Override
    public Component description() {
        return description;
    }

    @Override
    public ResourceLocation iconTexture() {
        return iconTexture;
    }
}