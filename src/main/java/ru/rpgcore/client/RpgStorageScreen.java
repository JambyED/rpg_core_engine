package ru.rpgcore.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import ru.rpgcore.core.storage.RpgStorageMenu;

public class RpgStorageScreen extends AbstractContainerScreen<RpgStorageMenu> {

    private static final ResourceLocation CHEST_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public RpgStorageScreen(RpgStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;

        int rows = Math.max(1, menu.storageSize() / 9);
        this.imageHeight = 114 + rows * 18;

        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        int rows = Math.max(1, this.menu.storageSize() / 9);
        int topHeight = 17 + rows * 18;
        int bottomHeight = 96;

        gfx.blit(CHEST_TEXTURE, x, y, 0, 0, this.imageWidth, topHeight, 256, 256);
        gfx.blit(CHEST_TEXTURE, x, y + topHeight, 0, 126, this.imageWidth, bottomHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}