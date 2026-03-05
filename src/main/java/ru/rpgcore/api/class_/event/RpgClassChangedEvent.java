package ru.rpgcore.api.class_.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired AFTER classId has been stored in the player's profile.
 * oldClass/newClass can be null.
 */
public final class RpgClassChangedEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation oldClassId;
    private final ResourceLocation newClassId;

    public RpgClassChangedEvent(ServerPlayer player, ResourceLocation oldClassId, ResourceLocation newClassId) {
        this.player = player;
        this.oldClassId = oldClassId;
        this.newClassId = newClassId;
    }

    public ServerPlayer player() { return player; }
    public ResourceLocation oldClassId() { return oldClassId; }
    public ResourceLocation newClassId() { return newClassId; }
}