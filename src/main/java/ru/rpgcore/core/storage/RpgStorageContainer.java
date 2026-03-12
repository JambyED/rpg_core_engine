package ru.rpgcore.core.storage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class RpgStorageContainer extends SimpleContainer {

    private final RpgStorage storage;
    private final ServerLevel level;

    public RpgStorageContainer(ServerLevel level, RpgStorage storage) {
        super(storage.size());
        this.level = level;
        this.storage = storage;

        for (int i = 0; i < storage.size(); i++) {
            super.setItem(i, storage.getItem(i));
        }
    }

    public RpgStorage storage() {
        return storage;
    }

    public ServerLevel level() {
        return level;
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return super.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return super.removeItemNoUpdate(slot);
    }

    @Override
    public void clearContent() {
        super.clearContent();
    }
}