package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_ChooseClass;
import ru.rpgcore.network.msg.C2S_RequestClassList;
import ru.rpgcore.network.msg.ClientClassListCache;
import ru.rpgcore.network.msg.S2C_ClassList;

import java.util.ArrayList;
import java.util.List;

public final class RpgClassScreen extends Screen {

    private static final int CARD_W = 320;
    private static final int CARD_H = 86;
    private static final int GAP = 8;

    private static final int ICON_SIZE = 46;
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

        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.back"),
                b -> Minecraft.getInstance().setScreen(new RpgMenuScreen())
        ).bounds(cx - 110, this.height - 34, 100, 20).build());

        chooseBtn = addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.class.choose"),
                b -> onChoose()
        ).bounds(cx + 10, this.height - 34, 100, 20).build());

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
        int listTop = 56;
        int listBottom = this.height - 60;
        int space = Math.max(1, listBottom - listTop);
        int per = CARD_H + GAP;
        return Math.max(1, space / per);
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

        List<ResourceLocation> ids = getClassIds();
        int visible = getVisibleCount();

        int startX = this.width / 2 - CARD_W / 2;
        int startY = 56;

        for (int i = 0; i < visible; i++) {
            int idx = scrollIndex + i;
            if (idx >= ids.size()) break;

            int y = startY + i * (CARD_H + GAP);

            if (mouseX >= startX && mouseX <= startX + CARD_W && mouseY >= y && mouseY <= y + CARD_H) {
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
        gfx.drawCenteredString(this.font, this.title, cx, 16, 0xFFFFFF);

        S2C_ClassList data = ClientClassListCache.get();
        if (data == null) {
            gfx.drawCenteredString(this.font, Component.translatable("rpg_core.gui.class.loading"), cx, 36, 0xAAAAAA);
            return;
        }

        if (!data.currentClassId.isBlank()) {
            ResourceLocation rl = ResourceLocation.tryParse(data.currentClassId);
            Component name = (rl != null) ? className(rl) : Component.literal(data.currentClassId);

            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.class.current", name),
                    cx, 34, 0x55FFFF
            );
        } else if (!data.canChoose) {
            gfx.drawCenteredString(this.font,
                    Component.translatable("rpg_core.gui.class.cannot_choose"),
                    cx, 34, 0xFF5555);
        }

        List<ResourceLocation> ids = getClassIds();
        if (ids.isEmpty()) {
            gfx.drawCenteredString(this.font, Component.translatable("rpg_core.gui.class.none"), cx, 78, 0xAAAAAA);
            chooseBtn.active = false;
            return;
        }

        int visible = getVisibleCount();
        int startX = cx - CARD_W / 2;
        int y = 56;

        for (int i = 0; i < visible; i++) {
            int idx = scrollIndex + i;
            if (idx >= ids.size()) break;

            ResourceLocation id = ids.get(idx);
            boolean isSel = (selected != null && selected.equals(id));

            int border = isSel ? 0xFF55FF55 : 0xFF555555;
            int fill = 0xAA000000;

            gfx.fill(startX, y, startX + CARD_W, y + CARD_H, fill);
            gfx.fill(startX, y, startX + CARD_W, y + 1, border);
            gfx.fill(startX, y + CARD_H - 1, startX + CARD_W, y + CARD_H, border);
            gfx.fill(startX, y, startX + 1, y + CARD_H, border);
            gfx.fill(startX + CARD_W - 1, y, startX + CARD_W, y + CARD_H, border);

            // Icon placeholder (пока без текстур)
            int iconX = startX + ICON_PAD;
            int iconY = y + (CARD_H - ICON_SIZE) / 2;
            gfx.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0xFF222222);

            int textX = iconX + ICON_SIZE + 12;

            // Name
            gfx.drawString(this.font, className(id), textX, y + 10, 0xFFFFFF, false);
            // Desc
            gfx.drawString(this.font, classDesc(id), textX, y + 26, 0xAAAAAA, false);
            // Stats
            gfx.drawString(this.font, classStat(id, 1), textX, y + 44, 0x55FF55, false);
            gfx.drawString(this.font, classStat(id, 2), textX, y + 56, 0x55FF55, false);

            y += CARD_H + GAP;
        }

        // ✅ Scrollbar + hint (only when needed)
        if (ids.size() > visible) {
            drawScrollBar(gfx, ids.size(), visible);

            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.class.scroll_hint"),
                    cx,
                    this.height - 56,
                    0xAAAAAA
            );
        }

        chooseBtn.active = data.canChoose && selected != null;
    }

    private void drawScrollBar(GuiGraphics gfx, int total, int visible) {
        if (total <= visible) return;

        int cx = this.width / 2;
        int listXRight = cx + CARD_W / 2;

        int barX = listXRight + 6;
        int top = 56;
        int bottom = this.height - 60;

        // track
        gfx.fill(barX, top, barX + 4, bottom, 0x66000000);

        int trackH = Math.max(1, bottom - top);
        int maxStart = Math.max(1, total - visible);

        int thumbH = Math.max(10, (int) ((visible / (double) total) * trackH));
        int thumbY = top + (int) ((scrollIndex / (double) maxStart) * (trackH - thumbH));

        gfx.fill(barX, thumbY, barX + 4, thumbY + thumbH, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}