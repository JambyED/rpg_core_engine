package ru.rpgcore.core.xp;

import ru.rpgcore.core.config.RpgWorldConfigData;

public final class RpgXpCurve {
    private RpgXpCurve() {}

    private static final long QUAD_SCALE = 1000L;

    /** XP needed to go from level -> level+1 (level starts at 0). */
    public static int xpToNextLevel(int level, RpgWorldConfigData cfg) {
        long l = Math.max(0, level);

        long base = Math.max(0, (long) cfg.getXpBase());
        long linear = Math.max(0, (long) cfg.getXpLinear());
        long quadP = Math.max(0, (long) cfg.getXpQuadPermille()); // /1000

        long xp = base + linear * l + (quadP * l * l) / QUAD_SCALE;

        if (xp < 1) xp = 1;
        if (xp > Integer.MAX_VALUE) xp = Integer.MAX_VALUE;
        return (int) xp;
    }

    /**
     * Total XP required to REACH 'level' from level 0.
     * total(0)=0; total(1)=xpToNext(0); total(L)=sum_{i=0..L-1} xpToNext(i)
     *
     * Uses closed-form sums (fast):
     * sum i = (L-1)L/2
     * sum i^2 = (L-1)L(2L-1)/6
     */
    public static int totalXpForLevel(int level, RpgWorldConfigData cfg) {
        int Lint = Math.max(0, level);
        if (Lint == 0) return 0;

        long L = Lint;

        long base = Math.max(0, (long) cfg.getXpBase());
        long linear = Math.max(0, (long) cfg.getXpLinear());
        long quadP = Math.max(0, (long) cfg.getXpQuadPermille()); // /1000

        // base * L
        long total = safeAdd(0, safeMul(base, L));

        // linear * sum_{i=0..L-1} i = linear * (L-1)*L/2
        long sumI = (L - 1) * L / 2;
        total = safeAdd(total, safeMul(linear, sumI));

        // quad term: (quadP/1000) * sum i^2, where sum i^2 = (L-1)*L*(2L-1)/6
        long sumI2 = ((L - 1) * L * (2 * L - 1)) / 6;
        long quad = (safeMul(quadP, sumI2)) / QUAD_SCALE;
        total = safeAdd(total, quad);

        if (total > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (total < 0) return 0;
        return (int) total;
    }

    private static long safeMul(long a, long b) {
        if (a == 0 || b == 0) return 0;
        if (a > 0 && b > 0 && a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        return a * b;
    }

    private static long safeAdd(long a, long b) {
        if (a > 0 && b > 0 && a > Long.MAX_VALUE - b) return Long.MAX_VALUE;
        return a + b;
    }
}