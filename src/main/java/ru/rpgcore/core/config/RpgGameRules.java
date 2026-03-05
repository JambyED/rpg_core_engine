package ru.rpgcore.core.config;

import net.minecraft.world.level.GameRules;

public final class RpgGameRules {
    private RpgGameRules() {}

    // Включение/выключение начисления RPG XP за мобов
    public static GameRules.Key<GameRules.BooleanValue> RPG_MOB_XP_ENABLED;

    // Включение/выключение нашего HUD (top-left)
    public static GameRules.Key<GameRules.BooleanValue> RPG_HUD_ENABLED;

    // NEW: скрывать ванильные полоски снизу (HP/еда/броня/воздух/XP-bar), когда включен наш HUD
    public static GameRules.Key<GameRules.BooleanValue> RPG_HIDE_VANILLA_HUD;

    /** Вызывается из RpgCore (у тебя уже так сделано). */
    public static void init() {
        RPG_MOB_XP_ENABLED =
                GameRules.register("rpgMobXpEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        RPG_HUD_ENABLED =
                GameRules.register("rpgHudEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        RPG_HIDE_VANILLA_HUD =
                GameRules.register("rpgHideVanillaHud", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    }
}