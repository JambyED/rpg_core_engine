package ru.rpgcore.core.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * World-level persistence for all RPG storages.
 */
public class RpgStorageSavedData extends SavedData {

    public static final String DATA_NAME = "rpg_core_storages";

    private static final String KEY_STORAGES = "storages";

    private final Map<String, RpgStorage> storages = new HashMap<>();

    public RpgStorageSavedData() {
    }

    public static RpgStorageSavedData load(CompoundTag tag) {
        RpgStorageSavedData data = new RpgStorageSavedData();

        if (tag.contains(KEY_STORAGES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(KEY_STORAGES, Tag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);

                RpgStorage storage = RpgStorage.fromNbt(entry);
                if (storage == null) continue;

                data.storages.put(storage.storageId(), storage);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {

        ListTag list = new ListTag();

        for (RpgStorage storage : storages.values()) {
            list.add(storage.toNbt());
        }

        tag.put(KEY_STORAGES, list);

        return tag;
    }

    public Map<String, RpgStorage> storages() {
        return storages;
    }

    public RpgStorage get(String id) {
        return storages.get(id);
    }

    public void put(RpgStorage storage) {
        storages.put(storage.storageId(), storage);
        setDirty();
    }

}