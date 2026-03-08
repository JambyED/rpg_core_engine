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

    private static final int PANEL_W = 410;
    private static final int PANEL_H = 245;

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
        int bottomY = this.height / 2 + PANEL_H / 2 - 24;

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

        drawPanel(gfx, left, top, right, bottom);

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

        int modelCenterX = left + 68;
        int modelBottomY = top + 145;
        int modelScale = 52;

        drawPlayerModel(gfx, modelCenterX, modelBottomY, modelScale);

        int infoX = left + 135;
        int infoY = top + 26;
        int infoW = right - infoX - 16;

        String playerName = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getGameProfile().getName()
                : "?";

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.profile.player", playerName),
                infoX,
                infoY,
                0xFFFFFF,
                false
        );
        infoY += 15;

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.level", d.level, d.maxLevel),
                infoX,
                infoY,
                0xE0E0E0,
                false
        );
        infoY += 15;

        int barW = Math.min(175, infoW - 16);
        int barH = 8;
        drawXpBar(gfx, infoX, infoY, barW, barH, d);
        gfx.drawString(this.font, String.valueOf(d.
                level), infoX + barW + 10, infoY - 1, 0x55FFFF, false);
        infoY += 17;

        if (d.xpToNext < 0) {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next_max"),
                    infoX,
                    infoY,
                    0xAAAAAA,
                    false
            );
        } else {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next", d.xpToNext),
                    infoX,
                    infoY,
                    0xAAAAAA,
                    false
            );
        }
        infoY += 12;

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.total_xp", d.xp),
                infoX,
                infoY,
                0xAAAAAA,
                false
        );
        infoY += 18;

        int classBlockHeight = drawClassBlock(gfx, d, infoX, infoY, infoW);
        infoY += classBlockHeight + 14;

        gfx.drawString(
                this.font,
                Component.translatable(
                        "rpg_core.gui.profile.tokens",
                        d.tokensTotal,
                        d.tokensSpent,
                        d.tokensAvailable
                ),
                infoX,
                infoY,
                0x55FF55,
                false
        );

        infoY += 18;
        drawWalletBlock(gfx, infoX, infoY);
    }

    private void drawPanel(GuiGraphics gfx, int left, int top, int right, int bottom) {
        int outer = 0xAA111111;
        int inner = 0xAA2A2A2A;
        int bg = 0x88000000;

        gfx.fill(left - 2, top - 2, right + 2, bottom + 2, outer);
        gfx.fill(left - 1, top - 1, right + 1, bottom + 1, inner);
        gfx.fill(left, top, right, bottom, bg);
    }

    private void drawPlayerModel(GuiGraphics gfx, int centerX, int bottomY, int scale) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        InventoryScreen.renderEntityInInventoryFollowsAngle(
                gfx,
                centerX,
                bottomY,
                scale,
                0.0F,
                0.0F,
                mc.player
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

    private int drawClassBlock(GuiGraphics gfx, S2C_ProfileData d, int x, int y, int w) {
        if (d.classId == null || d.classId.isBlank()) {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.class_none"),
                    x,
                    y,
                    0xFF5555,
                    false
            );
            return 12;
        }

        ResourceLocation rl = ResourceLocation.tryParse(d.classId);
        if (rl == null) {
            gfx.drawString(this.font, Component.literal(d.classId), x, y, 0xFF5555, false);
            return 12;
        }

        RpgClass clazz = RpgClassRegistries.registry().getValue(rl);
        if (clazz == null) {
            gfx.drawString(this.font, Component.literal(d.classId), x, y, 0xFF5555, false);
            return 12;
        }

        int iconSize = 18;
        gfx.fill(x, y + 1, x + iconSize, y + 1 + iconSize, 0xFF222222);

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
        int startY = y;

        List<FormattedCharSequence> descLines = this.font.split(clazz.description(), textW);
        int maxDescLines = Math.min(2, descLines.size());
        for (int i = 0; i < maxDescLines; i++) {
            gfx.drawString(this.font, descLines.get(i), x + iconSize + 8, textY, 0xAAAAAA, false);
            textY += 10;
        }

        List<Component> stats = clazz.statsLines();
        if (stats.size() > 0) {
            gfx.drawString(this.font, stats.get(0), x + iconSize + 8, textY, 0x55FF55, false);
            textY += 10;
        }
        if (stats.size() > 1) {
            gfx.drawString(this.font, stats.get(1), x + iconSize + 8, textY, 0x55FF55, false);
            textY += 10;
        }

        return Math.max(24, textY - startY);
    }

    private void drawWalletBlock(GuiGraphics gfx, int x, int y) {
        int coinX = x;
        int coinY = y + 1;

        // простая заглушка-иконка монеты до финального арта
        gfx.fill(coinX, coinY, coinX + 10, coinY + 10, 0xFF6B4E00);
        gfx.fill(coinX + 1, coinY + 1, coinX + 9, coinY + 9, 0xFFFFD54A);
        gfx.fill(coinX + 3, coinY + 3, coinX + 7, coinY + 7, 0xFFFFEE88);

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.wallet", "—"),
                x + 16,
                y,
                0xFFD54A,
                false
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}