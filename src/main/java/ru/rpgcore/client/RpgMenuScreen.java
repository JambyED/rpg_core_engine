package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RpgMenuScreen extends Screen {

    public RpgMenuScreen() {
        super(Component.translatable("rpg_core.gui.menu.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 32;

        // Profile
        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.menu.profile"),
                btn -> Minecraft.getInstance().setScreen(new RpgProfileScreen())
        ).bounds(centerX - 60, y, 120, 20).build());

        // Classes
        y += 24;
        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.menu.class"),
                btn -> Minecraft.getInstance().setScreen(new RpgClassScreen())
        ).bounds(centerX - 60, y, 120, 20).build());

        // Perks
        y += 24;
        addRenderableWidget(Button.builder(
                Component.translatable("rpg_core.gui.menu.perks"),
                btn -> Minecraft.getInstance().setScreen(new RpgPerksScreen())
        ).bounds(centerX - 60, y, 120, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}