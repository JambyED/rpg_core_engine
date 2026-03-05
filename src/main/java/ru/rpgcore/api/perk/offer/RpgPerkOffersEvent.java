package ru.rpgcore.api.perk.offer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fired when core needs to show/validate perk offers for a given tier.
 * Addons may replace the offers list (e.g., class-based trees).
 *
 * Contract:
 * - tier is the requested tier (>=1)
 * - defaultOffers contains the core's best-effort offers (usually 3)
 * - offers list is mutable; addons may clear/replace it
 */
public final class RpgPerkOffersEvent extends Event {
    private final ServerPlayer player;
    private final int tier;

    private final List<ResourceLocation> defaultOffers;
    private final List<ResourceLocation> offers;

    public RpgPerkOffersEvent(ServerPlayer player, int tier, List<ResourceLocation> defaultOffers) {
        this.player = player;
        this.tier = tier;
        this.defaultOffers = List.copyOf(defaultOffers);
        this.offers = new ArrayList<>(defaultOffers);
    }

    public ServerPlayer player() { return player; }
    public int tier() { return tier; }

    /** Read-only view of core-provided defaults. */
    public List<ResourceLocation> defaultOffers() { return defaultOffers; }

    /** Mutable offers list. Addons can clear/replace/sort it. */
    public List<ResourceLocation> offersMutable() { return offers; }

    /** Read-only view of current offers after modifications. */
    public List<ResourceLocation> offersView() { return Collections.unmodifiableList(offers); }

    public void setOffers(List<ResourceLocation> newOffers) {
        offers.clear();
        if (newOffers != null) offers.addAll(newOffers);
    }

    public void clearOffers() {
        offers.clear();
    }

    public void addOffer(ResourceLocation id) {
        if (id != null) offers.add(id);
    }
}