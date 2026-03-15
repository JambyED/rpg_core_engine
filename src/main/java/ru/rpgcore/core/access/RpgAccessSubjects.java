package ru.rpgcore.core.access;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/**
 * Factory/helper methods for common access subjects.
 */
public final class RpgAccessSubjects {

    private RpgAccessSubjects() {}

    public static RpgAccessSubject player(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        return new RpgAccessSubject(
                RpgAccessSubjectType.PLAYER,
                player.getUUID().toString()
        );
    }

    public static RpgAccessSubject player(String playerId) {
        return new RpgAccessSubject(RpgAccessSubjectType.PLAYER, playerId);
    }

    public static RpgAccessSubject guild(String guildId) {
        return new RpgAccessSubject(RpgAccessSubjectType.GUILD, guildId);
    }

    public static RpgAccessSubject faction(String factionId) {
        return new RpgAccessSubject(RpgAccessSubjectType.FACTION, factionId);
    }

    public static RpgAccessSubject npc(String npcId) {
        return new RpgAccessSubject(RpgAccessSubjectType.NPC, npcId);
    }

    public static RpgAccessSubject world(String worldId) {
        return new RpgAccessSubject(RpgAccessSubjectType.WORLD, worldId);
    }

    public static RpgAccessSubject server(String id) {
        return new RpgAccessSubject(RpgAccessSubjectType.SERVER, id);
    }

    public static RpgAccessSubject serverGlobal() {
        return server("global");
    }
}