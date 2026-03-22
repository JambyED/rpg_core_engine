package ru.rpgcore.core.storage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import ru.rpgcore.core.access.RpgAccessPermission;
import ru.rpgcore.core.access.RpgAccessRule;
import ru.rpgcore.core.access.RpgAccessSubject;

import java.util.List;
import java.util.Objects;

/**
 * High-level admin utility for storage access management.
 *
 * PURPOSE:
 * - centralize grant/revoke/list operations for storage permissions
 * - provide a stable bridge for command layer and future admin tools
 */
public final class RpgStorageAccessAdmin {

    private RpgStorageAccessAdmin() {}

    public static void grant(
            ServerLevel level,
            RpgAccessSubject subject,
            RpgAccessPermission permission,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(ownerRef, "ownerRef");

        RpgStoragePermissions.grant(level, subject, permission, ownerRef);
    }

    public static void revoke(
            ServerLevel level,
            RpgAccessSubject subject,
            RpgAccessPermission permission,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(permission, "permission");
        Objects.requireNonNull(ownerRef, "ownerRef");

        RpgStoragePermissions.revoke(level, subject, permission, ownerRef);
    }

    public static void grantOpenToPlayer(
            ServerLevel level,
            ServerPlayer targetPlayer,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(targetPlayer, "targetPlayer");
        Objects.requireNonNull(ownerRef, "ownerRef");

        RpgStoragePermissions.grantOpenToPlayer(level, targetPlayer, ownerRef);
    }

    public static void revokeOpenFromPlayer(
            ServerLevel level,
            ServerPlayer targetPlayer,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(targetPlayer, "targetPlayer");
        Objects.requireNonNull(ownerRef, "ownerRef");

        RpgStoragePermissions.revokeOpenFromPlayer(level, targetPlayer, ownerRef);
    }

    public static List<RpgAccessRule> rulesForStorage(
            ServerLevel level,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(ownerRef, "ownerRef");

        return RpgStoragePermissions.rulesForStorage(level, ownerRef).stream()
                .filter(RpgAccessRule.class::isInstance)
                .map(RpgAccessRule.class::cast)
                .toList();
    }

    public static boolean canPlayerOpen(
            ServerPlayer player,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerRef, "ownerRef");

        return RpgStoragePermissions.canOpen(player, ownerRef);
    }
}