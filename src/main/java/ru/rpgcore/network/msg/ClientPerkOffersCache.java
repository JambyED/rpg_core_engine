package ru.rpgcore.network.msg;

public final class ClientPerkOffersCache {
    private ClientPerkOffersCache() {}

    private static volatile S2C_PerkOffers LAST;

    public static void set(S2C_PerkOffers msg) {
        LAST = msg;
    }

    public static S2C_PerkOffers get() {
        return LAST;
    }

    public static void clear() {
        LAST = null;
    }
}