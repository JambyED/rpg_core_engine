package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_ProfileData;

public final class RpgHudOverlay {
    private RpgHudOverlay() {}

    public static void render(GuiGraphics gfx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.hideGui) return;
        if (mc.options.renderDebug) return;

        S2C_ProfileData d = ClientProfileCache.get();
        if (d == null) return;

        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // ===== Placement =====
        // Vanilla XP bar sits above hotbar. We'll draw our bar slightly above it.
        int barW = 182;
        int barH = 5; // a bit slimmer than before

        int x = (w / 2) - (barW / 2);
        int y = h - 32 - 12; // slightly above vanilla XP bar

        // ===== Colors (vanilla-ish) =====
        int frameOuter = 0xFF1B1B1B; // dark border
        int frameInner = 0xFF3A3A3A; // inner border
        int bg = 0xAA000000;         // translucent background

        // Fill: blue with subtle top highlight
        int fillTop = 0xFF60A5FA;    // lighter blue
        int fillBottom = 0xFF2563EB; // darker blue

        // ===== Compute progress =====
        float progress = 0f;
        if (d.xpNeededThisLevel > 0) {
            progress = (float) d.xpIntoLevel / (float) d.xpNeededThisLevel;
            if (progress < 0f) progress = 0f;
            if (progress > 1f) progress = 1f;
        }

        int filled = (int) (barW * progress);
        if (filled < 0) filled = 0;
        if (filled > barW) filled = barW;

        // ===== Draw bar frame =====
        // outer
        gfx.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, frameOuter);
        // inner
        gfx.fill(x, y, x + barW, y + barH, frameInner);
        // background inset
        gfx.fill(x + 1, y + 1, x + barW - 1, y + barH - 1, bg);

        // ===== Draw fill (inset) =====
        if (filled > 2) {
            int fx1 = x + 1;
            int fy1 = y + 1;
            int fx2 = x + 1 + filled - 2;
            int fy2 = y + barH - 1;

            // bottom part
            gfx.fill(fx1, fy1 + 1, fx2, fy2, fillBottom);
            // top highlight line
            gfx.fill(fx1, fy1, fx2, fy1 + 1, fillTop);
        } else if (filled > 0) {
            // tiny fill: just a small block
            int fx1 = x + 1;
            int fy1 = y + 1;
            int fx2 = x + 1 + filled;
            int fy2 = y + barH - 1;
            gfx.fill(fx1, fy1, fx2, fy2, fillBottom);
        }

        // ===== Level badge =====
        String levelStr = String.valueOf(d.level);
        int textW = mc.font.width(levelStr);

        int padX = 4;
        int padY = 2;

        int bxW = textW + padX * 2;
        int bxH = mc.font.lineHeight + padY * 2;

        int bxX = (w / 2) - (bxW / 2);
        int bxY = y - bxH - 3;

        // outer
        gfx.fill(bxX - 1, bxY - 1, bxX + bxW + 1, bxY + bxH + 1, frameOuter);
        // inner
        gfx.fill(bxX, bxY, bxX + bxW, bxY + bxH, frameInner);
        // bg inset
        gfx.fill(bxX + 1, bxY + 1, bxX + bxW - 1, bxY + bxH - 1, bg);

        // centered text inside badge
        int tx = bxX + (bxW - textW) / 2;
        int ty = bxY + padY;
        gfx.drawString(mc.font, levelStr, tx, ty, 0xFFFFFF, false);
    }
}