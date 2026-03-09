package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import ru.rpgcore.api.perk.RpgPerk;
import ru.rpgcore.core.perk.RpgPerkRegistries;
import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_ChoosePerk;
import ru.rpgcore.network.msg.C2S_RequestPerkOffers;
import ru.rpgcore.network.msg.C2S_RequestProfile;
import ru.rpgcore.network.msg.ClientPerkOffersCache;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_PerkOffers;
import ru.rpgcore.network.msg.S2C_ProfileData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RpgPerksScreen extends Screen {

    private static final int PANEL_W = 432;
    private static final int PANEL_H = 300;

    private static final int LIST_PADDING = 12;
    private static final int CARD_W = 118;
    private static final int CARD_H = 86;
    private static final int CARD_GAP_X = 8;
    private static final int CARD_GAP_Y = 8;
    private static final int TIER_GAP = 10;

    private Button confirm;
    private Button clear;
    private Button back;

    private boolean waitingOffers = true;
    private boolean waitingChooseResult = false;

    private int currentTier = 0;
    private List<ResourceLocation> currentOffers = List.of();

    private ResourceLocation pendingSelection = null;
    private ResourceLocation confirmedSelection = null;

    // Это строка состояния внизу, а не "фатальная ошибка", скрывающая дерево.
    private Component statusMessage = null;
    private int statusMessageColor = 0xAAAAAA;

    private int scrollOffset = 0;

    public RpgPerksScreen() {
        super(Component.translatable("rpg_core.gui.perks.title"));
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.width / 2;
        int buttonsY = this.height / 2 + PANEL_H / 2 + 16;

        back = addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.back"),
                btn -> onBack()
        ).bounds(cx - 160, buttonsY, 100, 20).build());

        clear = addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.perks.clear"),
                btn -> onClear()
        ).bounds(cx - 50, buttonsY, 100, 20).build());

        confirm = addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.perks.confirm"),
                btn -> onConfirm()
        ).bounds(cx + 60, buttonsY, 100, 20).build());

        confirm.active = false;
        clear.active = false;

        requestProfileAndOffers();
    }

    private void onBack() {
        Minecraft.getInstance().setScreen(new RpgCharacterScreen());
    }

    private void requestProfileAndOffers() {
        waitingOffers = true;
        waitingChooseResult = false;

        currentTier = 0;
        currentOffers = List.of();
        pendingSelection = null;
        confirmedSelection = null;
        statusMessage = null;
        statusMessageColor = 0xAAAAAA;
        scrollOffset = 0;

        ClientPerkOffersCache.clear();
        ClientProfileCache.clear();

        if (Minecraft.getInstance().player != null) {
            RpgNetwork.CHANNEL.sendToServer(new C2S_RequestProfile());
            RpgNetwork.CHANNEL.sendToServer(new C2S_RequestPerkOffers());
        }

        confirm.active = false;
        clear.active = false;
    }

    private void requestOffersOnly() {
        waitingOffers = true;

        currentTier = 0;
        currentOffers = List.of();
        statusMessage = null;
        statusMessageColor = 0xAAAAAA;

        ClientPerkOffersCache.clear();
        if (Minecraft.getInstance().player != null) {
            RpgNetwork.CHANNEL.
                    sendToServer(new C2S_RequestPerkOffers());
        }

        confirm.active = false;
        clear.active = false;
    }

    private void onClear() {
        if (waitingOffers || waitingChooseResult) return;
        if (confirmedSelection != null) return;

        pendingSelection = null;
        confirm.active = false;
        clear.active = false;
    }

    private void onConfirm() {
        if (waitingOffers || waitingChooseResult) return;
        if (confirmedSelection != null) return;
        if (pendingSelection == null) return;
        if (currentTier <= 0) return;

        waitingChooseResult = true;
        confirmedSelection = pendingSelection;
        pendingSelection = null;

        confirm.active = false;
        clear.active = false;

        ClientProfileCache.clear();
        RpgNetwork.CHANNEL.sendToServer(new C2S_ChoosePerk(currentTier, confirmedSelection.toString()));
    }

    @Override
    public void tick() {
        super.tick();

        S2C_PerkOffers offersMsg = ClientPerkOffersCache.get();
        if (waitingOffers && offersMsg != null) {
            waitingOffers = false;

            if (offersMsg.errorKey != null && !offersMsg.errorKey.isBlank()) {
                currentTier = 0;
                currentOffers = List.of();

                if ("rpg_core.perks.offers.none_level".equals(offersMsg.errorKey)) {
                    statusMessage = Component.translatable(offersMsg.errorKey, offersMsg.errorArg, offersMsg.tier);
                    statusMessageColor = 0xFF5555;
                } else if ("rpg_core.perks.offers.none_left".equals(offersMsg.errorKey)) {
                    statusMessage = Component.translatable(offersMsg.errorKey);
                    statusMessageColor = 0xAAAAAA;
                } else if ("rpg_core.perks.offers.not_enough".equals(offersMsg.errorKey)) {
                    statusMessage = Component.translatable(offersMsg.errorKey);
                    statusMessageColor = 0xFFAA55;
                } else {
                    statusMessage = Component.translatable(offersMsg.errorKey, offersMsg.errorArg);
                    statusMessageColor = 0xFF5555;
                }

                confirm.active = false;
                clear.active = false;
                return;
            }

            statusMessage = null;
            statusMessageColor = 0xAAAAAA;

            currentTier = offersMsg.tier;
            currentOffers = List.copyOf(offersMsg.offers);

            pendingSelection = null;
            confirmedSelection = null;

            confirm.active = false;
            clear.active = false;
        }

        if (waitingChooseResult) {
            if (ClientProfileCache.get() != null) {
                waitingChooseResult = false;
                confirmedSelection = null;
                requestOffersOnly();
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, getContentHeight() - getVisibleListHeight());
        if (maxScroll <= 0) return super.mouseScrolled(mouseX, mouseY, delta);

        int step = 20;
        if (delta < 0) scrollOffset = Math.min(maxScroll, scrollOffset + step);
        if (delta > 0) scrollOffset = Math.max(0, scrollOffset - step);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (waitingOffers || waitingChooseResult) return super.mouseClicked(mouseX, mouseY, button);

        S2C_ProfileData profile = ClientProfileCache.get();
        if (profile == null) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2;
        int cy = this.height / 2;

        int panelLeft = cx - PANEL_W / 2;
        int panelTop = cy - PANEL_H / 2;
        int panelRight = panelLeft + PANEL_W;

        int listLeft = panelLeft + LIST_PADDING;
        int listTop = panelTop + 72;
        int listRight = panelRight - LIST_PADDING - 8;
        int drawY = listTop + 6 - scrollOffset;

        for (Map.Entry<Integer, List<ResourceLocation>> entry : getPerksByTier().entrySet()) {
            int tier = entry.getKey();
            List<ResourceLocation> perks = entry.getValue();

            int tierHeight = getTierBlockHeight(perks.size());

            if (drawY + tierHeight >= listTop && drawY <= listTop + getVisibleListHeight()) {
                int cardsTop = drawY + 22;

                for (int i = 0; i < perks.size(); i++) {
                    ResourceLocation perkId = perks.get(i);

                    int col = i % 3;
                    int row = i / 3;

                    int cardX = listLeft + 8 + col * (CARD_W + CARD_GAP_X);
                    int cardY = cardsTop + row * (CARD_H + CARD_GAP_Y);

                    if (mouseX >= cardX && mouseX <= cardX + CARD_W && mouseY >= cardY && mouseY <= cardY + CARD_H) {
                        if (isSelectable(profile, tier, perkId)) {
                            pendingSelection = perkId;
                            confirm.active = true;
                            clear.active = true;
                            return true;
                        }
                    }
                }
            }

            drawY += tierHeight + TIER_GAP;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);

        int cx = this.width / 2;
        int cy = this.height / 2;

        int panelLeft = cx - PANEL_W / 2;
        int panelTop = cy - PANEL_H / 2;
        int panelRight = panelLeft + PANEL_W;
        int panelBottom = panelTop + PANEL_H;

        drawMainPanel(gfx, panelLeft, panelTop, panelRight, panelBottom);
        gfx.drawCenteredString(this.font, this.title, cx, panelTop + 8, 0xFFFFFF);

        int headerLeft = panelLeft + LIST_PADDING;
        int headerTop = panelTop + 26;
        int headerRight = panelRight - LIST_PADDING;
        int headerBottom = headerTop + 38;

        int listLeft = panelLeft + LIST_PADDING;
        int listTop = panelTop + 72;
        int listRight = panelRight - LIST_PADDING - 8;
        int listBottom = panelBottom - 16;

        drawSubPanel(gfx, headerLeft, headerTop, headerRight, headerBottom);
        drawSubPanel(gfx, listLeft, listTop, listRight, listBottom);

        S2C_ProfileData profile = ClientProfileCache.get();
        drawHeader(gfx, profile, headerLeft + 8, headerTop + 8);

        if (waitingOffers && profile == null) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.perks.loading"),
                    cx,
                    cy,
                    0xAAAAAA
            );
            return;
        }

        if (profile == null) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.loading"),
                    cx,
                    cy,
                    0xAAAAAA
            );
            return;
        }

        int drawY = listTop + 6 - scrollOffset;
        int visibleTop = listTop;
        int visibleBottom = listBottom;

        for (Map.Entry<Integer, List<ResourceLocation>> entry : getPerksByTier().entrySet()) {
            int tier = entry.getKey();
            List<ResourceLocation> perks = entry.getValue();

            int tierHeight = getTierBlockHeight(perks.size());

            if (drawY + tierHeight >= visibleTop && drawY <= visibleBottom) {
                drawTierBlock(gfx, profile, tier, perks, listLeft + 6, drawY, listRight - listLeft - 12);
            }

            drawY += tierHeight + TIER_GAP;
        }

        int maxScroll = Math.max(0, getContentHeight() - getVisibleListHeight());
        if (maxScroll > 0) {
            drawScrollBar(gfx, listRight + 4, listTop + 4, listBottom - 4, maxScroll);
        }
        if (statusMessage != null) {
            gfx.drawCenteredString(this.font, statusMessage, cx, panelBottom - 8, statusMessageColor);
        } else if (pendingSelection != null && confirmedSelection == null) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.perks.click_confirm"),
                    cx,
                    panelBottom - 8,
                    0x55FFFF
            );
        }
    }

    private void drawHeader(GuiGraphics gfx, S2C_ProfileData profile, int x, int y) {
        if (profile == null) {
            gfx.drawString(this.font, Component.translatable("rpg_core.gui.profile.loading"), x, y, 0xAAAAAA, false);
            return;
        }

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.gui.profile.level", profile.level, profile.maxLevel),
                x,
                y,
                0xFFFFFF,
                false
        );

        gfx.drawString(
                this.font,
                Component.translatable(
                        "rpg_core.gui.profile.tokens",
                        profile.tokensTotal,
                        profile.tokensSpent,
                        profile.tokensAvailable
                ),
                x + 140,
                y,
                0x55FF55,
                false
        );

        int maxTierByLevelCap = Math.max(1, profile.maxLevel / 5);

        if (profile.level >= profile.maxLevel) {
            gfx.drawString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next_max"),
                    x,
                    y + 14,
                    0xAAAAAA,
                    false
            );
            return;
        }

        int nextTier = Math.min(maxTierByLevelCap, (profile.level / 5) + 1);
        int requiredLevel = nextTier * 5;

        gfx.drawString(
                this.font,
                Component.translatable("rpg_core.perks.status.next_tier", nextTier, requiredLevel),
                x,
                y + 14,
                0xAAAAAA,
                false
        );
    }

    private Map<Integer, List<ResourceLocation>> getPerksByTier() {
        Map<Integer, List<ResourceLocation>> map = new LinkedHashMap<>();

        List<ResourceLocation> ids = new ArrayList<>(RpgPerkRegistries.registry().getKeys());
        ids.sort(
                Comparator
                        .comparingInt((ResourceLocation id) -> {
                            RpgPerk perk = RpgPerkRegistries.registry().getValue(id);
                            return perk == null ? Integer.MAX_VALUE : perk.tier();
                        })
                        .thenComparing(ResourceLocation::toString)
        );

        for (ResourceLocation id : ids) {
            RpgPerk perk = RpgPerkRegistries.registry().getValue(id);
            if (perk == null) continue;

            map.computeIfAbsent(perk.tier(), t -> new ArrayList<>()).add(id);
        }

        return map;
    }

    private int getTierBlockHeight(int perkCount) {
        int rows = Math.max(1, (int) Math.ceil(perkCount / 3.0));
        return 22 + rows * CARD_H + (rows - 1) * CARD_GAP_Y + 10;
    }

    private int getContentHeight() {
        int total = 0;
        boolean first = true;

        for (List<ResourceLocation> perks : getPerksByTier().values()) {
            if (!first) total += TIER_GAP;
            total += getTierBlockHeight(perks.size());
            first = false;
        }

        return total + 12;
    }

    private int getVisibleListHeight() {
        return PANEL_H - 72 - 16;
    }

    private void drawTierBlock(GuiGraphics gfx, S2C_ProfileData profile, int tier, List<ResourceLocation> perks, int x, int y, int w) {
        int h = getTierBlockHeight(perks.size());

        int fill = 0x33000000;
        int border = 0x55333333;

        gfx.fill(x, y, x + w, y + h, fill);
        gfx.fill(x, y, x + w, y + 1, border);
        gfx.
                fill(x, y + h - 1, x + w, y + h, border);
        gfx.fill(x, y, x + 1, y + h, border);
        gfx.fill(x + w - 1, y, x + w, y + h, border);

        int requiredLevel = tier * 5;
        boolean unlockedByLevel = profile.level >= requiredLevel;

        int titleColor = unlockedByLevel ? 0xFFFFFF : 0x888888;
        gfx.drawString(
                this.font,
                Component.literal("Тир " + tier + " • уровень " + requiredLevel),
                x + 8,
                y + 6,
                titleColor,
                false
        );

        int cardsTop = y + 22;

        for (int i = 0; i < perks.size(); i++) {
            ResourceLocation perkId = perks.get(i);

            int col = i % 3;
            int row = i / 3;

            int cardX = x + 8 + col * (CARD_W + CARD_GAP_X);
            int cardY = cardsTop + row * (CARD_H + CARD_GAP_Y);

            drawPerkCard(gfx, profile, tier, perkId, cardX, cardY);
        }
    }

    private void drawPerkCard(GuiGraphics gfx, S2C_ProfileData profile, int tier, ResourceLocation perkId, int x, int y) {
        RpgPerk perk = RpgPerkRegistries.registry().getValue(perkId);
        if (perk == null) return;

        CardState state = getCardState(profile, tier, perkId);

        int fill;
        int border;
        int textColor;

        switch (state) {
            case CHOSEN -> {
                fill = 0x665A4710;
                border = 0xFFFFD54A;
                textColor = 0xFFFFF2B0;
            }
            case PENDING -> {
                fill = 0x66102A5A;
                border = 0xFF55FFFF;
                textColor = 0xFFFFFFFF;
            }
            case AVAILABLE -> {
                fill = 0x66182E18;
                border = 0xFF55FF55;
                textColor = 0xFFFFFFFF;
            }
            case BLOCKED -> {
                fill = 0x44202020;
                border = 0x66555555;
                textColor = 0xFFAAAAAA;
            }
            case FUTURE -> {
                fill = 0x33101010;
                border = 0x55333333;
                textColor = 0xFF777777;
            }
            default -> {
                fill = 0x44000000;
                border = 0x66555555;
                textColor = 0xFFFFFFFF;
            }
        }

        gfx.fill(x, y, x + CARD_W, y + CARD_H, fill);
        gfx.fill(x, y, x + CARD_W, y + 1, border);
        gfx.fill(x, y + CARD_H - 1, x + CARD_W, y + CARD_H, border);
        gfx.fill(x, y, x + 1, y + CARD_H, border);
        gfx.fill(x + CARD_W - 1, y, x + CARD_W, y + CARD_H, border);

        int iconX = x + 8;
        int iconY = y + 8;
        int iconSize = 16;

        if (perk.iconTexture() != null) {
            gfx.blit(perk.iconTexture(), iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        } else {
            gfx.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0xFF222222);
        }

        int textX = iconX + iconSize + 8;
        int textW = CARD_W - (textX - x) - 6;

        List<FormattedCharSequence> nameLines = this.font.split(perk.displayName(), textW);
        int textY = y + 8;
        if (!nameLines.isEmpty()) {
            gfx.drawString(this.font, nameLines.get(0), textX, textY, textColor, false);
            textY += 10;
        }

        List<FormattedCharSequence> descLines = this.font.split(perk.description(), textW);
        int maxDesc = Math.min(3, descLines.size());
        for (int i = 0; i < maxDesc; i++) {
            gfx.drawString(this.font, descLines.get(i), textX, textY, 0xFFAAAAAA, false);
            textY += 10;
        }

        if (state == CardState.CHOSEN) {
            gfx.drawString(this.font, Component.literal("V"), x + CARD_W - 10, y + 6, 0xFFFFD54A, false);
        } else if (state == CardState.PENDING) {
            gfx.drawString(this.font, Component.literal(">"), x + CARD_W - 10, y + 6, 0xFF55FFFF, false);
        }
    }

    private CardState getCardState(S2C_ProfileData profile, int tier, ResourceLocation perkId) {
        if (profile == null) return CardState.FUTURE;
        String chosenId = profile.chosenPerkByTier.get(tier);
        if (chosenId != null && !chosenId.isBlank()) {
            if (chosenId.equals(perkId.toString())) return CardState.CHOSEN;
            return CardState.BLOCKED;
        }

        if (confirmedSelection != null) {
            if (confirmedSelection.equals(perkId)) return CardState.CHOSEN;
            if (perkIdInCurrentOffers(perkId) && tier == currentTier) return CardState.BLOCKED;
        }

        if (pendingSelection != null && pendingSelection.equals(perkId)) {
            return CardState.PENDING;
        }

        if (isSelectable(profile, tier, perkId)) {
            return CardState.AVAILABLE;
        }

        if (profile.level < tier * 5) {
            return CardState.FUTURE;
        }

        return CardState.BLOCKED;
    }

    private boolean isSelectable(S2C_ProfileData profile, int tier, ResourceLocation perkId) {
        if (profile == null) return false;
        if (profile.tokensAvailable <= 0) return false;
        if (profile.level < tier * 5) return false;
        if (profile.chosenPerkByTier.containsKey(tier)) return false;
        if (tier != currentTier) return false;
        return perkIdInCurrentOffers(perkId);
    }

    private boolean perkIdInCurrentOffers(ResourceLocation perkId) {
        for (ResourceLocation id : currentOffers) {
            if (id.equals(perkId)) return true;
        }
        return false;
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

    private void drawScrollBar(GuiGraphics gfx, int x, int top, int bottom, int maxScroll) {
        gfx.fill(x, top, x + 4, bottom, 0x66000000);

        int trackH = Math.max(1, bottom - top);
        int visible = getVisibleListHeight();

        int thumbH = Math.max(10, (int) ((visible / (double) (visible + maxScroll)) * trackH));
        int thumbY = top + (int) ((scrollOffset / (double) maxScroll) * (trackH - thumbH));

        gfx.fill(x, thumbY, x + 4, thumbY + thumbH, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum CardState {
        AVAILABLE,
        PENDING,
        CHOSEN,
        BLOCKED,
        FUTURE
    }
}