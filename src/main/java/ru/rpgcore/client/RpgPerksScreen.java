package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.rpgcore.api.perk.RpgPerk;
import ru.rpgcore.core.perk.RpgPerkRegistries;
import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_ChoosePerk;
import ru.rpgcore.network.msg.C2S_RequestPerkOffers;
import ru.rpgcore.network.msg.ClientPerkOffersCache;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_PerkOffers;

import java.util.List;

public final class RpgPerksScreen extends Screen {

    private static final int BTN_W = 220;
    private static final int BTN_H = 20;
    private static final int GAP = 6;

    private Button b1, b2, b3;
    private Button confirm, clear, back;

    private boolean waitingOffers = true;
    private boolean waitingChooseResult = false;

    private int currentTier = 0;
    private List<ResourceLocation> currentOffers = List.of();

    private ResourceLocation pendingSelection = null;
    private ResourceLocation confirmedSelection = null;

    private Component errorMessage = null;

    public RpgPerksScreen() {
        super(Component.translatable("rpg_core.gui.perks.title"));
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.width / 2;
        int startY = this.height / 2 - (BTN_H * 2) - GAP;

        b1 = addRenderableWidget(Button.builder(
                        Component.translatable("rpg_core.gui.perks.loading"),
                        btn -> onPick(0))
                .bounds(cx - BTN_W / 2, startY, BTN_W, BTN_H)
                .build());

        b2 = addRenderableWidget(Button.builder(
                        Component.translatable("rpg_core.gui.perks.loading"),
                        btn -> onPick(1))
                .bounds(cx - BTN_W / 2, startY + (BTN_H + GAP), BTN_W, BTN_H)
                .build());

        b3 = addRenderableWidget(Button.builder(
                        Component.translatable("rpg_core.gui.perks.loading"),
                        btn -> onPick(2))
                .bounds(cx - BTN_W / 2, startY + 2 * (BTN_H + GAP), BTN_W, BTN_H)
                .build());

        int yBack = this.height - 58;
        int yBottom = this.height - 34;

        back = addRenderableWidget(Button.builder(
                        Component.translatable("rpg_core.gui.back"),
                        btn -> onBack())
                .bounds(cx - 60, yBack, 120, 20)
                .build());

        confirm = addRenderableWidget(Button.builder(
                        Component.translatable("rpg_core.gui.perks.confirm"),
                        btn -> onConfirm())
                .bounds(cx - 110, yBottom, 100, 20)
                .build());

        clear = addRenderableWidget(Button.builder(
                        Component.translatable("rpg_core.gui.perks.clear"),
                        btn -> onClear())
                .bounds(cx + 10, yBottom, 100, 20)
                .build());

        setPerkButtonsActive(false);
        confirm.active = false;
        clear.active = false;

        requestOffers();
    }

    private void onBack() {
        Minecraft.getInstance().setScreen(new RpgCharacterScreen());
    }

    private void requestOffers() {
        waitingOffers = true;
        waitingChooseResult = false;

        currentTier = 0;
        currentOffers = List.of();
        pendingSelection = null;
        confirmedSelection = null;
        errorMessage = null;

        ClientPerkOffersCache.clear();
        if (Minecraft.getInstance().player != null) {
            RpgNetwork.CHANNEL.sendToServer(new C2S_RequestPerkOffers());
        }

        setPerkButtonsActive(false);
        confirm.active = false;
        clear.active = false;
        setLoadingLabels();
    }

    private void setLoadingLabels() {
        Component t = Component.translatable("rpg_core.gui.perks.loading");
                b1.setMessage(t);
        b2.setMessage(t);
        b3.setMessage(t);
    }

    private void setPerkButtonsActive(boolean active) {
        b1.active = active;
        b2.active = active;
        b3.active = active;
    }

    private void onPick(int index) {
        if (waitingOffers || waitingChooseResult) return;
        if (confirmedSelection != null) return;
        if (errorMessage != null) return;

        if (index < 0 || index >= currentOffers.size()) return;
        pendingSelection = currentOffers.get(index);

        confirm.active = true;
        clear.active = true;

        updateButtonCaptions();
    }

    private void onClear() {
        if (waitingOffers || waitingChooseResult) return;
        if (confirmedSelection != null) return;
        if (errorMessage != null) return;

        pendingSelection = null;
        confirm.active = false;
        clear.active = false;

        updateButtonCaptions();
    }

    private void onConfirm() {
        if (waitingOffers || waitingChooseResult) return;
        if (confirmedSelection != null) return;
        if (errorMessage != null) return;

        if (pendingSelection == null) return;
        if (currentTier <= 0) return;

        waitingChooseResult = true;

        confirmedSelection = pendingSelection;
        pendingSelection = null;

        setPerkButtonsActive(false);
        confirm.active = false;
        clear.active = false;

        updateButtonCaptions();

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
                if ("rpg_core.perks.offers.none_level".equals(offersMsg.errorKey)) {
                    errorMessage = Component.translatable(offersMsg.errorKey, offersMsg.errorArg, offersMsg.tier);
                } else {
                    errorMessage = Component.translatable(offersMsg.errorKey, offersMsg.errorArg);
                }

                setPerkButtonsActive(false);
                confirm.active = false;
                clear.active = false;

                b1.setMessage(Component.empty());
                b2.setMessage(Component.empty());
                b3.setMessage(Component.empty());
                return;
            }

            errorMessage = null;

            currentTier = offersMsg.tier;
            currentOffers = List.copyOf(offersMsg.offers);

            pendingSelection = null;
            confirmedSelection = null;

            boolean ok = currentOffers.size() >= 3;
            setPerkButtonsActive(ok);
            confirm.active = false;
            clear.active = false;

            updateButtonCaptions();
        }

        if (waitingChooseResult) {
            if (ClientProfileCache.get() != null) {
                waitingChooseResult = false;
                requestOffers();
            }
        }
    }

    private void updateButtonCaptions() {
        if (currentOffers == null || currentOffers.size() < 3) return;

        setCaption(b1, currentOffers.get(0));
        setCaption(b2, currentOffers.get(1));
        setCaption(b3, currentOffers.get(2));
    }

    private void setCaption(Button btn, ResourceLocation id) {
        Component perkName = getPerkDisplayName(id);

        boolean isPending = (pendingSelection != null && pendingSelection.equals(id));
        boolean isConfirmed = (confirmedSelection != null && confirmedSelection.equals(id));
        boolean blockedByConfirmed = (confirmedSelection != null && !confirmedSelection.equals(id));

        Component text;
        if (isConfirmed) {
            text = Component.literal("✔️ ").append(perkName);
        } else if (blockedByConfirmed) {
            text = Component.literal("✖️ ").append(perkName);
        } else if (isPending) {
            text = Component.literal("» ").append(perkName);
        } else {
            text = perkName;
        }

        btn.setMessage(text);

        if (confirmedSelection != null) {
            btn.active = false;
        }
    }

    private static Component getPerkDisplayName(ResourceLocation id) {
        if (id == null) return Component.empty();

        RpgPerk perk = RpgPerkRegistries.registry().getValue(id);
        if (perk != null && perk.displayName() != null) {
            return perk.displayName();
        }
        return Component.literal(id.toString());
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);

        int cx = this.width / 2;
        int y = 18;

        gfx.drawCenteredString(this.font, this.title, cx, y, 0xFFFFFF);
        y += 14;

        if (errorMessage != null) {
            gfx.drawCenteredString(this.font, errorMessage, cx, y, 0xFF5555);
            return;
        }

        if (pendingSelection != null && confirmedSelection == null) {
            gfx.drawCenteredString(this.font,
                    Component.translatable("rpg_core.gui.perks.click_confirm"),
                    cx, y, 0x55FFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}