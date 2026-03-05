package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_ProfileData;

public final class RpgTopLeftHudOverlay {
    private RpgTopLeftHudOverlay() {}

    // Vanilla GUI icons texture (temporary; later we’ll switch to our own atlas)
    private static final ResourceLocation GUI_ICONS =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/icons.png");

    public static void render(GuiGraphics gfx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;
        if (mc.options.renderDebug) return;

        Player p = mc.player;
        S2C_ProfileData prof = ClientProfileCache.get();
        if (prof == null) return;

        // ===== Layout =====
        int x0 = 8;
        int y0 = 8;

        int portrait = 24;

        // Two badges under portrait
        int badgeH = 10;
        int badgeGap = 2;

        // Bars block
        int iconColW = 10;
        int barW = 112;
        int barH = 6;
        int barGapY = 3;
        int barsCount = 5;
        int barsH = (barH * barsCount) + (barGapY * (barsCount - 1));

        // Air dots below everything
        int maxAir = p.getMaxAirSupply();
        int air = p.getAirSupply();
        boolean showAir = (air < maxAir) || p.isUnderWater();
        int airH = showAir ? 9 : 0;

        int rightW = iconColW + 4 + barW;
        int blockW = portrait + 6 + rightW;

        int portraitPlusBadgesH = portrait + badgeGap + badgeH;
        int contentH = Math.max(portraitPlusBadgesH, barsH) + airH;
        int blockH = contentH;

        // ===== Background =====
        int bg = 0x44000000;
        int border = 0x661B1B1B;
        gfx.fill(x0 - 2, y0 - 2, x0 + blockW + 2, y0 + blockH + 2, border);
        gfx.fill(x0 - 1, y0 - 1, x0 + blockW + 1, y0 + blockH + 1, bg);

        // ===== Portrait =====
        drawPlayerHead(gfx, mc, x0, y0, portrait);

        // ===== Two numeric badges under portrait (no text) =====
        int badgesY = y0 + portrait + badgeGap;

        int badgeW = (portrait - 1) / 2; // two squares fit under portrait
        int leftBadgeX = x0;
        int rightBadgeX = x0 + portrait - badgeW;

        int mcLevel = Math.max(0, p.experienceLevel);
        int rpgLevel = Math.max(0, prof.level);

        drawNumericBadge(gfx, mc, leftBadgeX, badgesY, badgeW, badgeH, mcLevel);
        drawNumericBadge(gfx, mc, rightBadgeX, badgesY, badgeW, badgeH, rpgLevel);

        // ===== Bars start (to the right) =====
        int barsX = x0 + portrait + 6;
        int iconX = barsX;
        int barX = barsX + iconColW + 4;
        int barY = y0;

        // 1) HP
        float hp = p.getHealth();
        float maxHp = p.getMaxHealth();
        float absorption = p.getAbsorptionAmount();
        drawHudIcon(gfx, iconX, barY - 1, HudIcon.HEART);
        drawHpBar(gfx, barX, barY, barW, barH, hp, maxHp, absorption);
        barY += barH + barGapY;

        // 2) RPG XP (MAX => full)
        float rpgPct;
        if (prof.xpToNext < 0) rpgPct = 1.0f;
        else if (prof.xpNeededThisLevel > 0) rpgPct = clamp01((float) prof.xpIntoLevel / (float) prof.xpNeededThisLevel);
        else rpgPct = 0f;

        drawHudIcon(gfx, iconX, barY - 1, HudIcon.RPG);
        drawBar(gfx, barX, barY, barW, barH, 0x55000000, 0xFF2563EB, rpgPct);
        barY += barH + barGapY;

        // 3) Vanilla XP progress
        float mcXpPct = clamp01(p.experienceProgress);
        drawHudIcon(gfx, iconX, barY - 1, HudIcon.XP);
        drawBar(gfx, barX, barY, barW, barH, 0x55000000, 0xFF22C55E, mcXpPct);
        barY += barH + barGapY;

        // 4) Hunger
        FoodData food = p.getFoodData();
        float foodPct = clamp01(food.getFoodLevel() / 20f);
        drawHudIcon(gfx, iconX, barY - 1, HudIcon.FOOD);
        drawBar(gfx, barX, barY, barW, barH, 0x55000000, 0xFFF59E0B, foodPct);
        barY += barH + barGapY;

        // 5) Armor
        float armorPct = clamp01(p.getArmorValue() / 20f);
        drawHudIcon(gfx, iconX, barY - 1, HudIcon.ARMOR);
        drawBar(gfx, barX, barY, barW, barH, 0x55000000, 0xFF9CA3AF, armorPct);

        // Air dots below
        if (showAir) {
            int dotsY = y0 + Math.max(portraitPlusBadgesH, barsH) + 2;
            drawAirDots(gfx, x0, dotsY, blockW, air, maxAir);
        }
    }

    private static void drawPlayerHead(GuiGraphics gfx, Minecraft mc, int x, int y, int size) {
        AbstractClientPlayer ap = (AbstractClientPlayer) mc.player;
        ResourceLocation skin = ap.getSkinTextureLocation();
        gfx.blit(skin, x, y, size, size, 8f, 8f, 8, 8, 64, 64);
        gfx.blit(skin, x, y, size, size, 40f, 8f, 8, 8, 64, 64);
    }

    private static void drawNumericBadge(GuiGraphics gfx, Minecraft mc, int x, int y, int w, int h, int value) {
        int border = 0xAA111111;
        int fill = 0x88000000;

        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        gfx.fill(x, y, x + w, y + h, fill);

        String txt = String.valueOf(value);

        // auto-scale for 2-3 digits in tiny badge
        float scale = 1.0f;
        int textW = mc.font.width(txt);
        if (textW > w - 2) {
            scale = (float) (w - 2) / (float) Math.max(1, textW);
            if (scale > 1.0f) scale = 1.0f;
            if (scale < 0.65f) scale = 0.65f; // keep readable
        }

        int tx = x + (w - (int)(textW * scale)) / 2;
        int ty = y + (h - (int)(mc.font.lineHeight * scale)) / 2;

        gfx.pose().pushPose();
        gfx.pose().translate(tx, ty, 0);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(mc.font, txt, 0, 0, 0xE6FFFFFF, false);
        gfx.pose().popPose();
    }

    // ===== Icons column =====
    private enum HudIcon { HEART, RPG, XP, FOOD, ARMOR }

    private static void drawHudIcon(GuiGraphics gfx, int x, int y, HudIcon icon) {
        int size = 9;

        // socket bg
        gfx.fill(x, y, x + 10, y + 10, 0x22000000);

        int u, v;
        switch (icon) {
            case HEART -> { u = 16; v = 0; }
            case FOOD  -> { u = 16; v = 27; }
            case ARMOR -> { u = 34; v = 9; }
            case XP    -> { u = 0;  v = 0; } // placeholder (we will replace with our atlas)
            case RPG   -> { u = 0;  v = 0; } // placeholder (we will replace with our atlas)
            default    -> { u = 0;  v = 0; }
        }

        gfx.blit(GUI_ICONS, x + 1, y + 1, size, size, u, v, size, size, 256, 256);
    }

    // ===== Bars =====

    // HP bar: base vs extra maxHP zone + absorption must show
    private static void drawHpBar(GuiGraphics gfx, int x, int y, int w, int h, float hp, float maxHp, float absorption) {
        int border = 0x661B1B1B;
        int inner = 0x662A2A2A;

        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        gfx.fill(x, y, x + w, y + h, inner);

        int innerW = w - 2;
        int innerH = h - 2;

        int ix1 = x + 1;
        int iy1 = y + 1;
        int ix2 = ix1 + innerW;
        int iy2 = iy1 + innerH;

        if (maxHp <= 0f) {
            gfx.fill(ix1, iy1, ix2, iy2, 0x55000000);
            return;
        }

        float abs = Math.max(0f, absorption);
        float totalCap = maxHp + abs;
        if (totalCap <= 0f) totalCap = maxHp;

        // background zones (based on maxHp only)
        float baseZonePct = clamp01(20f / maxHp);
        int baseZonePx = Math.max(0, Math.min(innerW, (int) (innerW * baseZonePct)));

        int bgBase = 0x55000000;
        int bgExtra = 0x55200000;

        if (baseZonePx > 0) gfx.fill(ix1, iy1, ix1 + baseZonePx, iy2, bgBase);
        if (baseZonePx < innerW) gfx.fill(ix1 + baseZonePx, iy1, ix2, iy2, bgExtra);

        // HP fill scaled to totalCap
        int hpPx = (int) (innerW * clamp01(hp / totalCap));
        int hpEnd = ix1 + hpPx;
        int red = 0xFFB91C1C;
        int redDark = 0xFF7F1D1D;

        int baseEnd = Math.min(ix1 + baseZonePx, hpEnd);
        if (baseEnd > ix1) gfx.fill(ix1, iy1, baseEnd, iy2, red);

        if (hpEnd > ix1 + baseZonePx) {
            int extraStart = ix1 + baseZonePx;
            gfx.fill(extraStart, iy1, hpEnd, iy2, redDark);
        }

        // absorption appended
        if (abs > 0f) {
            int absPx = (int) (innerW * clamp01(abs / totalCap));
            int start = hpEnd;
            int end = Math.min(ix2, start + absPx);
            if (end > start) gfx.fill(start, iy1, end, iy2, 0xFFF4C542);
        }
    }

    private static void drawBar(GuiGraphics gfx, int x, int y, int w, int h, int bgColor, int fillColor, float fillPct) {
        int border = 0x661B1B1B;
        int inner = 0x662A2A2A;

        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        gfx.fill(x, y, x + w, y + h, inner);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bgColor);

        int innerW = w - 2;
        int innerH = h - 2;

        int filled = (int) (innerW * clamp01(fillPct));
        if (filled > 0) {
            gfx.fill(x + 1, y + 1, x + 1 + filled, y + 1 + innerH, fillColor);
        }
    }

    private static void drawAirDots(GuiGraphics gfx, int x, int y, int width, int air, int maxAir) {
        int dots = 10;
        float pct = (maxAir <= 0) ? 0f : clamp01((float) air / (float) maxAir);
        int filled = (int) Math.ceil(dots * pct);

        int dotSize = 5;
        int gap = 2;

        int totalW = dots * dotSize + (dots - 1) * gap;
        int startX = x + (width - totalW) / 2;

        int cEmpty = 0x4427C6FF;
        int cFull = 0xFF27C6FF;

        for (int i = 0; i < dots; i++) {
            int dx = startX + i * (dotSize + gap);
            int color = (i < filled) ? cFull : cEmpty;
            gfx.fill(dx, y, dx + dotSize, y + dotSize, color);
        }
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}