package ru.rpgcore.core.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Concrete runtime storage instance.
 *
 * This is NOT registry content.
 * This is persistent state bound to a specific owner.
 */
public final class RpgStorage {
    private static final String KEY_STORAGE_ID = "storageId";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_SIZE = "size";
    private static final String KEY_ITEMS = "items";

    private final String storageId;
    private final RpgStorageOwnerRef ownerRef;

    private int size;
    private final List<ItemStack> items;

    private boolean dirty;

    public RpgStorage(String storageId, RpgStorageOwnerRef ownerRef, int size) {
        this.storageId = normalizeStorageId(storageId);
        this.ownerRef = Objects.requireNonNull(ownerRef, "ownerRef");
        this.size = Math.max(1, size);
        this.items = createEmptyItems(this.size);
        this.dirty = false;

        if (this.storageId == null) {
            throw new IllegalArgumentException("storageId cannot be null/blank");
        }
    }

    private RpgStorage(String storageId,
                       RpgStorageOwnerRef ownerRef,
                       int size,
                       List<ItemStack> items,
                       boolean dirty) {
        this.storageId = storageId;
        this.ownerRef = ownerRef;
        this.size = Math.max(1, size);
        this.items = new ArrayList<>(this.size);

        for (int i = 0; i < this.size; i++) {
            if (items != null && i < items.size()) {
                ItemStack stack = items.get(i);
                this.items.add(stack == null ? ItemStack.EMPTY : stack.copy());
            } else {
                this.items.add(ItemStack.EMPTY);
            }
        }

        this.dirty = dirty;
    }

    public String storageId() {
        return storageId;
    }

    public RpgStorageOwnerRef ownerRef() {
        return ownerRef;
    }

    public int size() {
        return size;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public boolean isValidSlot(int slot) {
        return slot >= 0 && slot < size;
    }

    public ItemStack getItem(int slot) {
        if (!isValidSlot(slot)) return ItemStack.EMPTY;
        ItemStack stack = items.get(slot);
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    public List<ItemStack> itemsView() {
        List<ItemStack> out = new ArrayList<>(items.size());
        for (ItemStack stack : items) {
            out.add(stack == null ? ItemStack.EMPTY : stack.copy());
        }
        return Collections.unmodifiableList(out);
    }

    public void setItem(int slot, ItemStack stack) {
        if (!isValidSlot(slot)) return;
        items.set(slot, stack == null ? ItemStack.EMPTY : stack.copy());
        markDirty();
    }

    public void clearSlot(int slot) {
        if (!isValidSlot(slot)) return;
        items.set(slot, ItemStack.EMPTY);
        markDirty();
    }

    public void clearAll() {
        for (int i = 0; i < size; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        markDirty();
    }

    /**
     * Resize storage preserving existing items up to new size.
     */
    public void resize(int newSize) {
        newSize = Math.max(1, newSize);
        if (newSize == this.size) return;

        if (newSize > this.size) {
            for (int i = this.size; i < newSize; i++) {
                items.add(ItemStack.EMPTY);
            }
        } else {
            for (int i = items.size() - 1; i >= newSize; i--) {
                items.remove(i);
            }
        }

        this.size = newSize;
        markDirty();
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();

        tag.putString(KEY_STORAGE_ID, storageId);
        tag.put(KEY_OWNER, ownerRef.toNbt());
        tag.putInt(KEY_SIZE, size);

        ListTag itemList = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack == null || stack.isEmpty()) continue;

            CompoundTag entry = new CompoundTag();
            entry.putInt("slot", slot);
            entry.put("stack", stack.save(new CompoundTag()));
            itemList.add(entry);
        }

        tag.put(KEY_ITEMS, itemList);
        return tag;
    }

    public static RpgStorage fromNbt(CompoundTag tag) {
        if (tag == null) return null;
        if (!tag.contains(KEY_STORAGE_ID, Tag.TAG_STRING)) return null;
        if (!tag.contains(KEY_OWNER, Tag.TAG_COMPOUND)) return null;

        String storageId = normalizeStorageId(tag.getString(KEY_STORAGE_ID));
        if (storageId == null) return null;

        RpgStorageOwnerRef ownerRef = RpgStorageOwnerRef.fromNbt(tag.getCompound(KEY_OWNER));
        if (ownerRef == null) return null;

        int size = tag.contains(KEY_SIZE, Tag.TAG_INT) ? Math.max(1, tag.getInt(KEY_SIZE)) : 1;
        List<ItemStack> items = createEmptyItems(size);

        if (tag.contains(KEY_ITEMS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(KEY_ITEMS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                if (!entry.contains("slot", Tag.TAG_INT)) continue;
                if (!entry.contains("stack", Tag.TAG_COMPOUND)) continue;

                int slot = entry.getInt("slot");
                if (slot < 0 || slot >= size) continue;

                ItemStack stack = ItemStack.of(entry.getCompound("stack"));
                items.set(slot, stack == null ? ItemStack.EMPTY : stack);
            }
        }

        return new RpgStorage(storageId, ownerRef, size, items, false);
    }

    public static String defaultStorageId(RpgStorageOwnerRef ownerRef) {
        return ownerRef.asKey();
    }

    private static List<ItemStack> createEmptyItems(int size) {
        List<ItemStack> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(ItemStack.EMPTY);
        }
        return list;
    }

    private static String normalizeStorageId(String storageId) {
        if (storageId == null) return null;
        storageId = storageId.trim();
        if (storageId.isEmpty()) return null;
        return storageId;
    }
}