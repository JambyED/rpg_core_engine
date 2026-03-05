package ru.rpgcore.network.msg;

public final class ClientProfileCache {
    private ClientProfileCache() {}

    private static volatile S2C_ProfileData LAST;

    public static void clear() {
        LAST = null;
    }

    public static void set(S2C_ProfileData data) {
        LAST = data;
    }

    public static S2C_ProfileData get() {
        return LAST;
    }
}