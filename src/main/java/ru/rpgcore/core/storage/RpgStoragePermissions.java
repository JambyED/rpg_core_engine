package ru.rpgcore.core.storage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import ru.rpgcore.core.access.RpgAccessManager;
import ru.rpgcore.core.access.RpgAccessPermission;
import ru.rpgcore.core.access.RpgAccessSubject;
import ru.rpgcore.core.access.RpgAccessSubjects;
import ru.rpgcore.core.access.RpgAccessTarget;

import java.util.List;
import java.util.Objects;

/**
 * Utility bridge between Storage Core and Access Core.
 *
 * PURPOSE:
 * - centralize how storage permissions are granted/revoked
 * - avoid duplicating target-building logic across commands/addons/systems
 * - provide a stable API for future guild/NPC/world storage access workflows
 */
public final class RpgStoragePermissions {

    private RpgStoragePermissions() {}

    /* =========================
       Target helpers
       ========================= */

    public static RpgAccessTarget target(RpgStorageOwnerRef ownerRef) {
        Objects.requireNonNull(ownerRef, "ownerRef");
        return RpgStorageAccess.target(ownerRef);
    }

    /* =========================
       Generic grant/revoke
       ========================= */

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

        RpgAccessManager.grant(level, subject, permission, target(ownerRef));
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

        RpgAccessManager.revoke(level, subject, permission, target(ownerRef));
    }

    /* =========================
       OPEN helpers
       ========================= */

    public static void grantOpen(
            ServerLevel level,
            RpgAccessSubject subject,
            RpgStorageOwnerRef ownerRef
    ) {
        grant(level, subject, RpgAccessPermission.OPEN, ownerRef);
    }

    public static void revokeOpen(
            ServerLevel level,
            RpgAccessSubject subject,
            RpgStorageOwnerRef ownerRef
    ) {
        revoke(level, subject, RpgAccessPermission.OPEN, ownerRef);
    }

    /* =========================
       Player convenience helpers
       ========================= */

    public static void grantOpenToPlayer(
            ServerLevel level,
            ServerPlayer player,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(player, "player");
        grantOpen(level, RpgAccessSubjects.player(player), ownerRef);
    }

    public static void revokeOpenFromPlayer(
            ServerLevel level,
            ServerPlayer player,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(player, "player");
        revokeOpen(level, RpgAccessSubjects.player(player), ownerRef);
    }

    public static void grantPermissionToPlayer(
            ServerLevel level,
            ServerPlayer player,
            RpgAccessPermission permission,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(player, "player");
        grant(level, RpgAccessSubjects.player(player), permission, ownerRef);
    }

    public static void revokePermissionFromPlayer(
            ServerLevel level,
            ServerPlayer player,
            RpgAccessPermission permission,
            RpgStorageOwnerRef ownerRef
    ) {
        Objects.requireNonNull(player, "player");
        revoke(level, RpgAccessSubjects.player(player), permission, ownerRef);
    }

/* =========================
Common subject helpers
       ========================= */

    public static void grantOpenToGuild(
            ServerLevel level,
            String guildId,
            RpgStorageOwnerRef ownerRef
    ) {
        grantOpen(level, RpgAccessSubjects.guild(guildId), ownerRef);
    }

    public static void revokeOpenFromGuild(
            ServerLevel level,
            String guildId,
            RpgStorageOwnerRef ownerRef
    ) {
        revokeOpen(level, RpgAccessSubjects.guild(guildId), ownerRef);
    }

    public static void grantOpenToFaction(
            ServerLevel level,
            String factionId,
            RpgStorageOwnerRef ownerRef
    ) {
        grantOpen(level, RpgAccessSubjects.faction(factionId), ownerRef);
    }

    public static void revokeOpenFromFaction(
            ServerLevel level,
            String factionId,
            RpgStorageOwnerRef ownerRef
    ) {
        revokeOpen(level, RpgAccessSubjects.faction(factionId), ownerRef);
    }

    public static void grantOpenToNpc(
            ServerLevel level,
            String npcId,
            RpgStorageOwnerRef ownerRef
    ) {
        grantOpen(level, RpgAccessSubjects.npc(npcId), ownerRef);
    }

    public static void revokeOpenFromNpc(
            ServerLevel level,
            String npcId,
            RpgStorageOwnerRef ownerRef
    ) {
        revokeOpen(level, RpgAccessSubjects.npc(npcId), ownerRef);
    }

    public static void grantOpenToWorld(
            ServerLevel level,
            String worldId,
            RpgStorageOwnerRef ownerRef
    ) {
        grantOpen(level, RpgAccessSubjects.world(worldId), ownerRef);
    }

    public static void revokeOpenFromWorld(
            ServerLevel level,
            String worldId,
            RpgStorageOwnerRef ownerRef
    ) {
        revokeOpen(level, RpgAccessSubjects.world(worldId), ownerRef);
    }

    public static void grantOpenToServerGlobal(
            ServerLevel level,
            RpgStorageOwnerRef ownerRef
    ) {
        grantOpen(level, RpgAccessSubjects.serverGlobal(), ownerRef);
    }

    public static void revokeOpenFromServerGlobal(
            ServerLevel level,
            RpgStorageOwnerRef ownerRef
    ) {
        revokeOpen(level, RpgAccessSubjects.serverGlobal(), ownerRef);
    }

    /* =========================
       Query helpers
       ========================= */

    public static boolean canOpen(ServerPlayer player, RpgStorageOwnerRef ownerRef) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(ownerRef, "ownerRef");
        return RpgStorageAccess.canOpen(player, ownerRef);
    }

    public static List<?> rulesForStorage(ServerLevel level, RpgStorageOwnerRef ownerRef) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(ownerRef, "ownerRef");
        return RpgAccessManager.getRulesForTarget(level, target(ownerRef));
    }
}