package ru.rpgcore.network.msg;

public final class ClientActionMessageCache {
    private ClientActionMessageCache() {}

    private static volatile S2C_ActionMessage LAST;

    public static void set(S2C_ActionMessage msg) {
        LAST = msg;
    }

    /** Забираем и очищаем, чтобы не висело вечно. */
    public static S2C_ActionMessage consume() {
        S2C_ActionMessage m = LAST;
        LAST = null;
        return m;
    }
}