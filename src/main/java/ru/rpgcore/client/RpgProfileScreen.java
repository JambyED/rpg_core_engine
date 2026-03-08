package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.rpgcore.api.class_.RpgClass;
import ru.rpgcore.core.class_.RpgClassRegistries;
import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_RequestProfile;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_ProfileData;

public final class RpgProfileScreen extends Screen {

    public RpgProfileScreen() {
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
        int bottomY = this.height - 34;

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

    private static Component classNameFromId(String classId) {
        if (classId == null || classId.isBlank()) return Component.empty();

        ResourceLocation rl = ResourceLocation.tryParse(classId);
        if (rl == null) return Component.literal(classId);

        RpgClass c = RpgClassRegistries.registry().getValue(rl);
        return (c != null) ? c.displayName() : Component.literal(classId);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);

        int center = this.width / 2;
        int y = this.height / 2 - 80;

        gfx.drawCenteredString(this.font, this.title, center, y, 0xFFFFFF);
        y += 18;

        S2C_ProfileData d = ClientProfileCache.get();
        if (d == null) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.loading"),
                    center, y, 0xAAAAAA
            );
            return;
        }

        gfx.drawCenteredString(
                this.font,
                Component.translatable("rpg_core.gui.profile.level", d.level, d.maxLevel),
                center, y, 0xFFFFFF
        );
        y += 14;

        gfx.drawCenteredString(
                this.font,
                Component.translatable("rpg_core.gui.profile.total_xp", d.xp),
                center, y, 0xAAAAAA
        );
        y += 14;

        if (d.xpToNext < 0) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next_max"),
                    center, y, 0xAAAAAA
            );
        } else {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.xp_to_next", d.xpToNext),
                    center, y, 0xAAAAAA
            );
        }
        y += 14;

        gfx.drawCenteredString(
                this.font,
                Component.translatable(
                        "rpg_core.gui.profile.tokens",
                        d.tokensTotal,
                        d.tokensSpent,
                        d.tokensAvailable
                ),
                center, y, 0x55FF55
        );
        y += 14;

        if (d.classId != null && !d.classId.isBlank()) {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.class", classNameFromId(d.classId)),
                    center, y, 0x55FFFF
            );
        } else {
            gfx.drawCenteredString(
                    this.font,
                    Component.translatable("rpg_core.gui.profile.class_none"),
                    center, y, 0xFF5555
            );
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}