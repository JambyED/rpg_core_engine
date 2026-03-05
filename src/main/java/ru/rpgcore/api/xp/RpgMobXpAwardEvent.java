package ru.rpgcore.api.xp;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Cancelable
public final class RpgMobXpAwardEvent extends Event {

    public record Recipient(ServerPlayer player, int xp) {}

    private final ServerLevel level;
    private final LivingEntity dead;
    private final ServerPlayer owner;
    private final int baseXp;

    private final List<Recipient> recipients = new ArrayList<>();

    public RpgMobXpAwardEvent(ServerLevel level, LivingEntity dead, ServerPlayer owner, int baseXp) {
        this.level = level;
        this.dead = dead;
        this.owner = owner;
        this.baseXp = baseXp;

        // default behavior
        this.recipients.add(new Recipient(owner, baseXp));
    }

    public ServerLevel level() { return level; }
    public LivingEntity dead() { return dead; }
    public ServerPlayer owner() { return owner; }
    public int baseXp() { return baseXp; }

    /** Mutable list: addons may clear/replace/split XP. */
    public List<Recipient> getRecipients() {
        return recipients;
    }

    public List<Recipient> getRecipientsView() {
        return Collections.unmodifiableList(recipients);
    }

    public void clearRecipients() {
        recipients.clear();
    }

    public void addRecipient(ServerPlayer player, int xp) {
        if (player == null) return;
        if (xp <= 0) return;
        recipients.add(new Recipient(player, xp));
    }
}