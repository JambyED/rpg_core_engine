package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_ChooseClass;
import ru.rpgcore.network.msg.C2S_RequestClassList;
import ru.rpgcore.network.msg.ClientClassListCache;
import ru.rpgcore.network.msg.S2C_ClassList;

import java.util.ArrayList;
import java.util.List;

public final class RpgClassScreen extends Screen {

    private static final int PANEL_W = 432;
    private static final int PANEL_H = 258;

    private static final int LIST_PADDING = 12;
    private static final int CARD_H = 78;
    private static final int GAP = 8;

    private static final int ICON_SIZE = 42;
    private static final int ICON_PAD = 10;

    private int scrollIndex = 0;
    private ResourceLocation selected = null;

    private Button chooseBtn;

    public RpgClassScreen() {
        super(Component.translatable("rpg_core.gui.class.title"));
    }

    @Override
    protected void init() {
        super.init();

        ClientClassListCache.clear();
        if (Minecraft.getInstance().player != null) {
            RpgNetwork.CHANNEL.sendToServer(new C2S_RequestClassList());
        }

        int cx = this.width / 2;
        int buttonsY = this.height / 2 + PANEL_H / 2 + 16;

        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.back"),
                b -> Minecraft.getInstance().setScreen(new RpgCharacterScreen())
        ).bounds(cx - 110, buttonsY, 100, 20).build());

        chooseBtn = addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.class.choose"),
                b -> onChoose()
        ).bounds(cx + 10, buttonsY, 100, 20).build());

        chooseBtn.active = false;
    }

    private void onChoose() {
        S2C_ClassList data = ClientClassListCache.get();
        if (data == null) return;
        if (!data.canChoose) return;
        if (selected == null) return;

        RpgNetwork.CHANNEL.sendToServer(new C2S_ChooseClass(selected.toString()));
        chooseBtn.active = false;
    }

    private static String baseKey(ResourceLocation id) {
        return "class." + id.getNamespace() + "." + id.getPath();
    }

    private static Component className(ResourceLocation id) {
        return Component.translatable(baseKey(id));
    }

    private static Component classDesc(ResourceLocation id) {
        return Component.translatable(baseKey(id) + ".desc");
    }

    private static Component classStat(ResourceLocation id, int idx1Based) {
        return Component.translatable(baseKey(id) + ".stat" + idx1Based);
    }

    private List<ResourceLocation> getClassIds() {
        S2C_ClassList data = ClientClassListCache.get();
        if (data == null) return List.of();

        List<ResourceLocation> out = new ArrayList<>();
        for (String s : data.classIds) {
            ResourceLocation id = ResourceLocation.tryParse(s);
            if (id != null) out.add(id);
        }
        return out;
    }

    private int getVisibleCount() {
        int listTop = this.height / 2 - PANEL_H / 2 + 54;
        int listBottom = this.height / 2 + PANEL_H / 2 - 18;
        int available = Math.max(1, listBottom - listTop);
        return Math.max(1, available / (CARD_H + GAP));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<ResourceLocation> ids = getClassIds();
        if (ids.isEmpty()) return super.mouseScrolled(mouseX, mouseY, delta);

        int visible = getVisibleCount();
        int maxStart = Math.max(0, ids.size() - visible);

        if (delta < 0) scrollIndex = Math.min(maxStart, scrollIndex + 1);
        if (delta > 0) scrollIndex = Math.max(0, scrollIndex - 1);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        S2C_ClassList data = ClientClassListCache.get();
        if (data == null) return super.mouseClicked(mouseX, mouseY, button);

        int cx = this.width / 2;
        int cy = this.height / 2;

        int panelLeft = cx - PANEL_W / 2;
        int panelTop = cy - PANEL_H / 2;

        int listLeft = panelLeft + LIST_PADDING;
        int listRight = panelLeft + PANEL_W - LIST_PADDING - 8;
        int listTop = panelTop + 54;

        List<ResourceLocation> ids = getClassIds();
        int visible = getVisibleCount();

        for (int i = 0; i < visible; i++) {
            int idx = scrollIndex + i;
            if (idx >= ids.size()) break;

            int y = listTop + i * (CARD_H + GAP);

            if (mouseX >= listLeft && mouseX <= listRight && mouseY >= y && mouseY <= y + CARD_H) {
                selected = ids.get(idx);
                chooseBtn.active = data.canChoose;
                return true;
            }
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

        S2C_ClassList data = ClientClassListCache.get();
        if (data == null) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.class.loading"),
                    cx,
                    cy,
                    0xAAAAAA
            );
            return;
        }

        if (!data.currentClassId.isBlank()) {
            ResourceLocation rl = ResourceLocation.tryParse(data.currentClassId);
            Component currentName = (rl != null) ? className(rl) : Component.literal(data.currentClassId);

            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.class.current", currentName),
                    cx,
                    panelTop + 28,
                    0x55FFFF
            );
        } else if (!data.canChoose) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.class.cannot_choose"),
                    cx,
                    panelTop + 28,
                    0xFF5555
            );
        }

        int listLeft = panelLeft + LIST_PADDING;
        int listRight = panelRight - LIST_PADDING - 8;
        int listTop = panelTop + 54;
        int listBottom = panelBottom - 18;

        drawSubPanel(gfx, listLeft, listTop, listRight, listBottom);

        List<ResourceLocation> ids = getClassIds();
        if (ids.isEmpty()) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.class.none"),
                    cx,
                    cy,
                    0xAAAAAA
            );
            chooseBtn.active = false;
            return;
        }

        int visible = getVisibleCount();

        for (int i = 0; i < visible; i++) {
            int idx = scrollIndex + i;
            if (idx >= ids.size()) break;

            int cardY = listTop + 6 + i * (CARD_H + GAP);
            drawClassCard(gfx, ids.get(idx), listLeft + 6, cardY, listRight - listLeft - 12, CARD_H);
        }

        if (ids.size() > visible) {
            drawScrollBar(gfx, listRight + 4, listTop + 4, listBottom - 4, ids.size(), visible);
        }

        chooseBtn.active = data.canChoose && selected != null;
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

    private void drawClassCard(GuiGraphics gfx, ResourceLocation id, int x, int y, int w, int h) {
        boolean isSelected = selected != null && selected.equals(id);

        int fill = isSelected ? 0x66182E18 : 0x55000000;
        int border = isSelected ? 0xFF55FF55 : 0x66555555;

        gfx.fill(x, y, x + w, y + h, fill);
        gfx.fill(x, y, x + w, y + 1, border);
        gfx.fill(x, y + h - 1, x + w, y + h, border);
        gfx.fill(x, y, x + 1, y + h, border);
        gfx.fill(x + w - 1, y, x + w, y + h, border);

        int iconX = x + ICON_PAD;
        int iconY = y + (h - ICON_SIZE) / 2;

        gfx.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0xFF222222);

        int textX = iconX + ICON_SIZE + 12;
        int textY = y + 10;
        int textW = w - (textX - x) - 10;

        gfx.drawString(this.font, className(id), textX, textY, 0xFFFFFF, false);
        textY += 14;

        List<FormattedCharSequence> descLines = this.font.split(classDesc(id), textW);
        if (!descLines.isEmpty()) {
            gfx.drawString(this.font, descLines.get(0), textX, textY, 0xAAAAAA, false);
            textY += 10;
        }

        gfx.drawString(this.font, classStat(id, 1), textX, textY, 0x55FF55, false);
        textY += 10;
        gfx.drawString(this.font, classStat(id, 2), textX, textY, 0x55FF55, false);
    }

    private void drawScrollBar(GuiGraphics gfx, int x, int top, int bottom, int total, int visible) {
        if (total <= visible) return;

        gfx.fill(x, top, x + 4, bottom, 0x66000000);

        int trackH = Math.max(1, bottom - top);
        int maxStart = Math.max(1, total - visible);

        int thumbH = Math.max(10, (int) ((visible / (double) total) * trackH));
        int thumbY = top + (int) ((scrollIndex / (double) maxStart) * (trackH - thumbH));

        gfx.fill(x, thumbY, x + 4, thumbY + thumbH, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}