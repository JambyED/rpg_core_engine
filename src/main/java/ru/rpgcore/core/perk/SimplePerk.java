package ru.rpgcore.core.perk;

import net.minecraft.network.chat.Component;
import ru.rpgcore.api.perk.RpgPerk;

public final class SimplePerk implements RpgPerk {
    private final int tier;
    private final Component displayName;

    public SimplePerk(int tier, Component displayName) {
        this.tier = tier;
        this.displayName = displayName;
    }

    @Override
    public int tier() {
        return tier;
    }

    @Override
    public Component displayName() {
        return displayName;
    }
}