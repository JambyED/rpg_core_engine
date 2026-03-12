package ru.rpgcore.core.storage;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RpgStorageMenu extends AbstractContainerMenu {

    private final RpgStorageContainer container;
    private final int storageSize;

    public RpgStorageMenu(int id, Inventory playerInventory, RpgStorageContainer container) {
        super(RpgStorageMenuType.STORAGE_MENU.get(), id);

        this.container = container;
        this.storageSize = container.getContainerSize();

        int rows = Math.max(1, storageSize / 9);
        int startY = 18;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9;
                if (index >= storageSize) break;

                addSlot(new Slot(
                        container,
                        index,
                        8 + col * 18,
                        startY + row * 18
                ));
            }
        }

        int playerInvY = startY + rows * 18 + 14;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        playerInvY + row * 18
                ));
            }
        }

        int hotbarY = playerInvY + 58;

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    hotbarY
            ));
        }
    }

    public static RpgStorageMenu client(int id, Inventory playerInventory, int size) {
        RpgStorageOwnerRef owner = new RpgStorageOwnerRef(
                RpgStorageOwnerType.WORLD,
                "client_dummy"
        );
        RpgStorage storage = new RpgStorage("client_dummy", owner, size);
        return new RpgStorageMenu(id, playerInventory, new RpgStorageContainer(null, storage));
    }

    public int storageSize() {
        return storageSize;
    }

    public RpgStorageContainer storageContainer() {
        return container;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        super.clicked(slotId, button, clickType, player);
        saveStorageFromSlots();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        saveStorageFromSlots();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        int containerSlots = storageSize;

        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, containerSlots, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        saveStorageFromSlots();
        return result;
    }

    @Override
    public void slotsChanged(Container changedContainer) {
        super.slotsChanged(changedContainer);
        saveStorageFromSlots();
    }

    @Override
    public void removed(Player player) {
        saveStorageFromSlots();
        super.removed(player);
    }

    private void saveStorageFromSlots() {
        RpgStorage storage = container.storage();

        for (int i = 0; i < storageSize; i++) {
            ItemStack stack = this.slots.get(i).getItem();
            storage.setItem(i, stack);
        }

        storage.markDirty();

        if (container.level() != null) {
            RpgStorageManager.save(container.level(), storage);
        }
    }
}