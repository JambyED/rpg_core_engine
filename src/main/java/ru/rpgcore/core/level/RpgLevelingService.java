package ru.rpgcore.core.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.profile.RpgProfileStorage;
import ru.rpgcore.core.xp.RpgXpCurve;

public final class RpgLevelingService {
    private RpgLevelingService() {}

    /** Recomputes player's level from stored total XP using current world maxLevel + curve. */
    public static RpgProfile syncLevel(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        RpgWorldConfigData cfg = RpgWorldConfigData.get(level);
        int maxLevel = cfg.getMaxLevel();

        RpgProfile p = RpgProfileStorage.load(player);
        int computed = computeLevelFromTotalXp(p.xp(), maxLevel, cfg);

        if (computed != p.level()) {
            p.setLevel(computed);
            RpgProfileStorage.save(player, p);
        }
        return p;
    }

    public static RpgProfile addXp(ServerPlayer player, int amount) {
        if (amount <= 0) return RpgProfileStorage.load(player);

        ServerLevel level = player.serverLevel();
        RpgWorldConfigData cfg = RpgWorldConfigData.get(level);
        int maxLevel = cfg.getMaxLevel();

        RpgProfile p = RpgProfileStorage.load(player);

        long newTotal = (long) p.xp() + amount;
        if (newTotal > Integer.MAX_VALUE) newTotal = Integer.MAX_VALUE;
        p.setXp((int) newTotal);

        int newLevel = computeLevelFromTotalXp(p.xp(), maxLevel, cfg);
        p.setLevel(newLevel);

        RpgProfileStorage.save(player, p);
        return p;
    }

    public static void reset(ServerPlayer player) {
        RpgProfileStorage.reset(player);
    }

    public static int computeLevelFromTotalXp(int totalXp, int maxLevel, RpgWorldConfigData cfg) {
        int level = 0;
        for (int l = 1; l <= maxLevel; l++) {
            if (totalXp >= RpgXpCurve.totalXpForLevel(l, cfg)) level = l;
            else break;
        }
        return level;
    }
}