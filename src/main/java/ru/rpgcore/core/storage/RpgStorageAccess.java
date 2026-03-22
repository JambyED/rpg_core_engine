package ru.rpgcore.core.storage;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;
import ru.rpgcore.core.access.RpgAccessManager;
import ru.rpgcore.core.access.RpgAccessPermission;
import ru.rpgcore.core.access.RpgAccessTarget;
import ru.rpgcore.core.access.RpgAccessTargetType;

import java.util.Objects;
import java.util.UUID;

/**
 * Central access/open helper for storages.
 *
 * This class resolves/open storages and now also integrates
 * basic access control for storage opening.
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

        if (!canOpen(player, ownerRef)) {
            player.sendSystemMessage(Component.translatable("rpg_core.storage.access.denied"));
            return;
        }

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
       Access helpers
       ========================= */

    public static RpgAccessTarget target(RpgStorageOwnerRef ownerRef) {
        Objects.requireNonNull(ownerRef, "ownerRef");
        return new RpgAccessTarget(
                RpgAccessTargetType.STORAGE,
                RpgStorage.defaultStorageId(ownerRef)
        );
    }

    public static boolean canOpen(ServerPlayer player, RpgStorageOwnerRef ownerRef) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerRef, "ownerRef");

        // Admin/server operators always keep debug/control access.
        if (player.hasPermissions(2)) {
            return true;
        }

        // Player-owned storages remain usable by their owner
        // even before custom access rules are assigned.
        if (isOwnedByPlayer(player, ownerRef)) {
            return true;
        }

        return RpgAccessManager.hasPermission(
                player,
                RpgAccessPermission.OPEN,
                target(ownerRef)
        );
    }

    private static boolean isOwnedByPlayer(ServerPlayer player, RpgStorageOwnerRef ownerRef) {
        if (ownerRef.ownerType() != RpgStorageOwnerType.PLAYER) {
            return false;
        }

        String ownerId = ownerRef.ownerId();
        if (ownerId == null || ownerId.isBlank()) {
            return false;
        }

        String uuid = player.getUUID().toString();

        if (ownerId.equals(uuid)) {
            return true;
        }

        return ownerId.startsWith(uuid + ":");
    }

    /* =========================
       Known storage access paths
       ========================= */

    public static void openPlayerTest(ServerPlayer player, int defaultSize) {
        open(
                player,
                RpgStorageRefs.playerTest(player),
                defaultSize,
                Component.translatable("rpg_core.storage.title.test")
        );
    }

    public static void openPlayerBank(ServerPlayer player, int defaultSize) {
        open(
                player,
                RpgStorageRefs.playerBank(player),
                defaultSize,
                Component.translatable("rpg_core.storage.title.bank")
        );
    }

    public static RpgStorage getOrCreatePlayerTest(ServerPlayer player, int defaultSize) {
        return getOrCreate(player, RpgStorageRefs.playerTest(player), defaultSize);
    }

    public static RpgStorage getOrCreatePlayerBank(ServerPlayer player, int defaultSize) {
        return getOrCreate(player, RpgStorageRefs.playerBank(player), defaultSize);
    }
}