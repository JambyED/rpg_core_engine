package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import ru.rpgcore.api.class_.RpgClass;
import ru.rpgcore.core.class_.RpgClassRegistries;
import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_RequestProfile;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_ProfileData;

import java.util.List;

public final class RpgCharacterScreen extends Screen {

    private static final int PANEL_W = 432;
    private static final int PANEL_H = 300;

    private static final int LEFT_SECTION_W = 118;
    private static final int GAP = 10;

    public RpgCharacterScreen() {
        super(Component.translatable("rpg_core.gui.profile.title"));
    }

    @Override
    protected void init() {
        super.init();

        ClientProfileCache.clear();
        if (Minecraft.getInstance().player != null) {
            RpgNetwork.CHANNEL.sendToServer(new C2S_RequestProfile());
        }

        int centerX = this.width / 2;
        int bottomY = this.height / 2 + PANEL_H / 2 + 16;

        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.menu.class"),
                btn -> Minecraft.getInstance().setScreen(new RpgClassScreen())
        ).bounds(centerX - 160, bottomY, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.menu.perks"),
                btn -> Minecraft.getInstance().setScreen(new RpgPerksScreen())
        ).bounds(centerX - 50, bottomY, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.back"),
                btn -> Minecraft.getInstance().setScreen(null)
        ).bounds(centerX + 60, bottomY, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);

        int cx = this.width / 2;
        int cy = this.height / 2;

        int left = cx - PANEL_W / 2;
        int top = cy - PANEL_H / 2;
        int right = left + PANEL_W;
        int bottom = top + PANEL_H;

        drawMainPanel(gfx, left, top, right, bottom);
        gfx.drawCenteredString(this.font, this.title, cx, top + 8, 0xFFFFFF);

        S2C_ProfileData d = ClientProfileCache.get();
        if (d == null) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.loading"),
                    cx,
                    cy,
                    0xAAAAAA
            );
            return;
        }

        int leftSectionX = left + 12;
        int leftSectionY = top + 24;
        int leftSectionRight = leftSectionX + LEFT_SECTION_W;
        int leftSectionBottom = top + PANEL_H - 16;

        int rightSectionX = leftSectionRight + GAP;
        int rightSectionY = top + 24;
        int rightSectionRight = right - 12;
        int rightSectionW = rightSectionRight - rightSectionX;

        int profileTop = rightSectionY;
        int profileBottom = profileTop + 88;

        int classTop = profileBottom + 8;
        int classBottom = classTop + 70;

        int tokensTop = classBottom + 8;
        int tokensBottom = tokensTop + 34;

        int walletTop = tokensBottom + 8;
        int walletBottom = walletTop + 30;

        drawSubPanel(gfx, leftSectionX, leftSectionY, leftSectionRight, leftSectionBottom);
        drawSubPanel(gfx, rightSectionX, profileTop, rightSectionRight, profileBottom);
        drawSubPanel(gfx, rightSectionX, classTop, rightSectionRight, classBottom);
        drawSubPanel(gfx, rightSectionX, tokensTop, rightSectionRight, tokensBottom);
        drawSubPanel(gfx, rightSectionX, walletTop, rightSectionRight, walletBottom);

        drawLeftSection(gfx, leftSectionX, leftSectionY, leftSectionRight, leftSectionBottom);

        drawProfileBlock(gfx, d, rightSectionX + 12, profileTop + 10, rightSectionW - 24);
        drawClassBlock(gfx, d, rightSectionX + 12, classTop + 10, rightSectionW - 24);
        drawTokensBlock(gfx, d, rightSectionX + 12, tokensTop + 10, rightSectionW - 24);
        drawWalletBlock(gfx, d, rightSectionX + 12, walletTop + 8, rightSectionW - 24);
    }

    private void drawMainPanel(GuiGraphics gfx, int left, int top, int right, int bottom) {
        int outer = 0xAA111111;
        int inner = 0xAA2A2A2A;
        int bg = 0x88000000;

        gfx.fill(left - 2, top - 2, right + 2, bottom + 2, outer);
        gfx.fill(left - 1, top - 1, right + 1, bottom + 1, inner);
        gfx.fill(left, top, right, bottom, bg);
    }

    private void drawSubPanel(GuiGraphics gfx, int left, int top, int right, int bottom) {
        int outer = 0x66303030;
        int inner = 0x55202020;
        int bg = 0x44000000;

        gfx.fill(left - 1, top - 1, right + 1, bottom + 1, outer);
        gfx.fill(left, top, right, bottom, inner);
        gfx.fill(left + 1, top + 1, right - 1, bottom - 1, bg);
    }

    private void drawLeftSection(GuiGraphics gfx, int left, int top, int right, int bottom) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int centerX = (left + right) / 2;
        int modelBottomY = top + 118;
        int modelScale = 58;

        InventoryScreen.renderEntityInInventoryFollowsAngle(
                gfx,
                centerX,
                modelBottomY,
                modelScale,
                0.0F,
                0.0F,
                mc.player
        );
    }

    private void drawProfileBlock(GuiGraphics gfx, S2C_ProfileData d, int x, int y, int w) {
        String playerName = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getGameProfile().getName()
                : "?";

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.profile.player", playerName),
                x,
                y,
                0xFFFFFF,
                false
        );
        y += 14;

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.level", d.level, d.maxLevel),
                x,
                y,
                0xE0E0E0,
                false
        );
        y += 14;

        int barW = Math.min(180, w - 34);
        int barH = 8;
        drawXpBar(gfx, x, y, barW, barH, d);
        gfx.drawString(this.font, String.valueOf(d.level), x + barW + 8, y - 1, 0x55FFFF, false);
        y += 16;

        if (d.xpToNext < 0) {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next_max"),
                    x,
                    y,
                    0xAAAAAA,
                    false
            );
        } else {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next", d.xpToNext),
                    x,
                    y,
                    0xAAAAAA,
                    false
            );
        }
        y += 12;

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.total_xp", d.xp),
                x,
                y,
                0xAAAAAA,
                false
        );
    }

    private void drawClassBlock(GuiGraphics gfx, S2C_ProfileData d, int x, int y, int w) {
        if (d.classId == null || d.classId.isBlank()) {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.class_none"),
                    x,
                    y,
                    0xFF5555,
                    false
            );
            return;
        }

        ResourceLocation rl = ResourceLocation.tryParse(d.classId);
        if (rl == null) {
            gfx.drawString(this.font, Component.literal(d.classId), x, y, 0xFF5555, false);
            return;
        }

        RpgClass clazz = RpgClassRegistries.registry().getValue(rl);
        if (clazz == null) {
            gfx.drawString(this.font, Component.literal(d.classId), x, y, 0xFF5555, false);
            return;
        }

        int iconSize = 18;

        if (clazz.iconTexture() != null) {
            gfx.blit(clazz.iconTexture(), x, y + 1, 0, 0, iconSize, iconSize, iconSize, iconSize);
        } else {
            gfx.fill(x, y + 1, x + iconSize, y + 1 + iconSize, 0xFF222222);
        }

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.class", clazz.displayName()),
                x + iconSize + 8,
                y,
                0x55FFFF,
                false
        );

        int textY = y + 14;
        int textW = w - iconSize - 8;

        List<FormattedCharSequence> descLines = this.font.split(clazz.description(), textW);
        if (!descLines.isEmpty()) {
            gfx.drawString(this.font, descLines.get(0), x + iconSize + 8, textY, 0xAAAAAA, false);
            textY += 10;
        }

        List<Component> stats = clazz.statsLines();
        if (stats.size() > 0) {
            gfx.drawString(this.font, stats.get(0), x + iconSize + 8, textY, 0x55FF55, false);
            textY += 10;
        }
        if (stats.size() > 1) {
            gfx.drawString(this.font, stats.get(1), x + iconSize + 8, textY, 0x55FF55, false);
        }
    }

    private void drawTokensBlock(GuiGraphics gfx, S2C_ProfileData d, int x, int y, int w) {
        gfx.drawString(
                this.font,
                Component.translatable(
                        "rpg_core.gui.profile.tokens",
                        d.tokensTotal,
                        d.tokensSpent,
                        d.tokensAvailable
                ),
                x,
                y,
                0x55FF55,
                false
        );
    }

    private void drawWalletBlock(GuiGraphics gfx, S2C_ProfileData d, int x, int y, int w) {
        int coinX = x;
        int coinY = y + 1;

        gfx.fill(coinX, coinY, coinX + 10, coinY + 10, 0xFF6B4E00);
        gfx.fill(coinX + 1, coinY + 1, coinX + 9, coinY + 9, 0xFFFFD54A);
        gfx.fill(coinX + 3, coinY + 3, coinX + 7, coinY + 7, 0xFFFFEE88);

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.wallet", Long.toString(d.balance)),
                x + 16,
                y,
                0xFFD54A,
                false
        );
    }

    private void drawXpBar(GuiGraphics gfx, int x, int y, int w, int h, S2C_ProfileData d) {
        float pct = 0.0f;
        if (d.xpToNext < 0) {
            pct = 1.0f;
        } else if (d.xpNeededThisLevel > 0) {
            pct = (float) d.xpIntoLevel / (float) d.xpNeededThisLevel;
        }

        if (pct < 0f) pct = 0f;
        if (pct > 1f) pct = 1f;

        int filled = (int) ((w - 2) * pct);

        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF111111);
        gfx.fill(x, y, x + w, y + h, 0xFF2A2A2A);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x55000000);

        if (filled > 0) {
            gfx.fill(x + 1, y + 1, x + 1 + filled, y + h - 1, 0xFF2563EB);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}