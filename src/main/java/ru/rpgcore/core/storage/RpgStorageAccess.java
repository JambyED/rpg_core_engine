package ru.rpgcore.core.storage;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

import java.util.Objects;

/**
 * Central access/open helper for storages.
 *
 * This class does NOT decide gameplay permissions.
 * It only provides a unified way to resolve/open storages.
 */
public final class RpgStorageAccess {

    private RpgStorageAccess() {}

    /* =========================
       Generic open helpers
       ========================= */

    public static void open(ServerPlayer player, RpgStorageOwnerRef ownerRef, int defaultSize, Component title) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerRef, "ownerRef");
        Objects.requireNonNull(title, "title");

        ServerLevel level = player.serverLevel();
        RpgStorage storage = RpgStorageManager.getOrCreate(level, ownerRef, defaultSize);

        NetworkHooks.openScreen(
                player,
                new SimpleMenuProvider(
                        (id, inventory, p) -> new RpgStorageMenu(id, inventory, new RpgStorageContainer(level, storage)),
                        title
                ),
                buf -> buf.writeVarInt(storage.size())
        );
    }

    public static RpgStorage getOrCreate(ServerPlayer player, RpgStorageOwnerRef ownerRef, int defaultSize) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerRef, "ownerRef");
        return RpgStorageManager.getOrCreate(player.serverLevel(), ownerRef, defaultSize);
    }

    public static RpgStorage get(ServerPlayer player, RpgStorageOwnerRef ownerRef) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerRef, "ownerRef");
        return RpgStorageManager.get(player.serverLevel(), ownerRef);
    }

    public static void save(ServerPlayer player, RpgStorage storage) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(storage, "storage");
        RpgStorageManager.save(player.serverLevel(), storage);
    }

    /* =========================
       Known storage access paths
       ========================= */

    public static void openPlayerTest(ServerPlayer player, int defaultSize) {
        open(player, RpgStorageRefs.playerTest(player), defaultSize, Component.translatable("rpg_core.storage.title.test"));
    }

    public static void openPlayerBank(ServerPlayer player, int defaultSize) {
        open(player, RpgStorageRefs.playerBank(player), defaultSize, Component.translatable("rpg_core.storage.title.bank"));
    }

    public static RpgStorage getOrCreatePlayerTest(ServerPlayer player, int defaultSize) {
        return getOrCreate(player, RpgStorageRefs.playerTest(player), defaultSize);
    }

    public static RpgStorage getOrCreatePlayerBank(ServerPlayer player, int defaultSize) {
        return getOrCreate(player, RpgStorageRefs.playerBank(player), defaultSize);
    }
}