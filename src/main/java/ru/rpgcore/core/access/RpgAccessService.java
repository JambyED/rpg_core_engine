package ru.rpgcore.core.access;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Foundation service for access subject resolution.
 *
 * IMPORTANT:
 * - This step does NOT yet implement permissions/rules.
 * - It only resolves which access identities are associated with a player.
 * - Later steps will evaluate permissions against these identities.
 */
public final class RpgAccessService {

    private RpgAccessService() {}

    /**
     * Returns all known access subjects for a player at the current stage.
     *
     * Current foundation:
     * - the player identity itself
     * - the global server identity
     *
     * Future extensions may add:
     * - guild membership identities
     * - faction identities
     * - temporary role identities
     * - event/scenario identities
     */
    public static List<RpgAccessSubject> resolveSubjects(ServerPlayer player) {
        Objects.requireNonNull(player, "player");

        List<RpgAccessSubject> result = new ArrayList<>();
        result.add(RpgAccessSubjects.player(player));
        result.add(RpgAccessSubjects.serverGlobal());

        return List.copyOf(result);
    }

    public static boolean hasSubject(ServerPlayer player, RpgAccessSubject subject) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(subject, "subject");

        return resolveSubjects(player).contains(subject);
    }
}