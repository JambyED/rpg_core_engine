package ru.rpgcore.network.msg;

public final class ClientClassListCache {
    private ClientClassListCache() {}

    private static volatile S2C_ClassList LAST;

    public static void clear() { LAST = null; }
    public static void set(S2C_ClassList msg) { LAST = msg; }
    public static S2C_ClassList get() { return LAST; }
}