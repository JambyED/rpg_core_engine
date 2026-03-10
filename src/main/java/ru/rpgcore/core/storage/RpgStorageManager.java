package ru.rpgcore.core.storage;

import net.minecraft.server.level.ServerLevel;

import java.util.Objects;

/**
 * Runtime service for accessing storages.
 */
public final class RpgStorageManager {

    private RpgStorageManager() {}

    private static RpgStorageSavedData getData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                RpgStorageSavedData::load,
                RpgStorageSavedData::new,
                RpgStorageSavedData.DATA_NAME
        );
    }

    public static RpgStorage get(ServerLevel level, String storageId) {
        if (storageId == null) return null;

        RpgStorageSavedData data = getData(level);
        return data.get(storageId);
    }

    public static RpgStorage get(ServerLevel level, RpgStorageOwnerRef ownerRef) {
        if (ownerRef == null) return null;
        return get(level, RpgStorage.defaultStorageId(ownerRef));
    }

    public static RpgStorage getOrCreate(ServerLevel level, RpgStorageOwnerRef ownerRef, int size) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(ownerRef);

        String id = RpgStorage.defaultStorageId(ownerRef);

        RpgStorageSavedData data = getData(level);
        RpgStorage storage = data.get(id);

        if (storage != null) {
            return storage;
        }

        storage = new RpgStorage(id, ownerRef, size);
        data.put(storage);
        return storage;
    }

    public static void save(ServerLevel level, RpgStorage storage) {
        if (level == null || storage == null) return;

        RpgStorageSavedData data = getData(level);
        data.put(storage);
        storage.clearDirty();
    }
}