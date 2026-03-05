package ru.rpgcore.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import ru.rpgcore.api.xp.RpgMobXpAwardEvent;
import ru.rpgcore.core.config.RpgGameRules;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.level.RpgLevelingService;

public final class MobXpEvents {
    private MobXpEvents() {}

    private static final double XP_RADIUS_BLOCKS = 64.0;
    private static final double XP_RADIUS_SQR = XP_RADIUS_BLOCKS * XP_RADIUS_BLOCKS;

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (dead.level().isClientSide()) return;

        ServerLevel level = (ServerLevel) dead.level();

        // gamerule
        if (!level.getGameRules().getBoolean(RpgGameRules.RPG_MOB_XP_ENABLED)) return;

        // resolve "owner player" (melee / projectile / tame)
        ServerPlayer owner = resolveOwnerPlayer(event.getSource());
        if (owner == null) return;

        // radius check (anti-abuse): owner must be within 64 blocks of the killed mob
        if (!isWithinRadius(owner, dead)) return;

        // entity registry id (vanilla + modded)
        ResourceLocation entityId = dead.getType().builtInRegistryHolder().key().location();

        // snapshot NBT for predicate matching
        CompoundTag entityTag = new CompoundTag();
        dead.saveWithoutId(entityTag);

        int baseXp = RpgWorldConfigData.get(level).resolveMobXp(entityId, entityTag);
        if (baseXp <= 0) return;

        // Hook for addons (party split etc.)
        RpgMobXpAwardEvent xpEvent = new RpgMobXpAwardEvent(level, dead, owner, baseXp);
        MinecraftForge.EVENT_BUS.post(xpEvent);
        if (xpEvent.isCanceled()) return;

        // Apply XP to recipients, BUT enforce same radius rule for everyone
        for (RpgMobXpAwardEvent.Recipient r : xpEvent.getRecipients()) {
            if (r.player() == null || r.xp() <= 0) continue;
            if (r.player().serverLevel() != level) continue;      // same dimension/world
            if (!isWithinRadius(r.player(), dead)) continue;       // within 64 blocks
            RpgLevelingService.addXp(r.player(), r.xp());
        }
    }

    private static boolean isWithinRadius(ServerPlayer player, LivingEntity dead) {
        // use coordinate form to avoid any method-signature differences
        return player.distanceToSqr(dead.getX(), dead.getY(), dead.getZ()) <= XP_RADIUS_SQR;
    }

    /**
     * Resolve which ServerPlayer should be considered the "XP owner" of the kill.
     * Supports:
     * - direct player melee
     * - projectiles (arrow, trident, etc.) -> owner
     * - tamed animals -> owner
     */
    private static ServerPlayer resolveOwnerPlayer(DamageSource source) {
        Entity attacker = source.getEntity();       // "owner" of damage (often player)
        Entity direct = source.getDirectEntity();   // direct object (arrow, mob body, etc.)

        // 1) direct attacker is player
        if (attacker instanceof ServerPlayer sp) return sp;

        // 2) projectile owner is player
        if (direct instanceof Projectile proj) {
            Entity projOwner = proj.getOwner();
            if (projOwner instanceof ServerPlayer sp) return sp;
        }

        // 3) tamed animal attacker -> owner
        if (attacker instanceof TamableAnimal tam) {
            Entity o = tam.getOwner();
            if (o instanceof ServerPlayer sp) return sp;
        }

        // 4) edge-case: direct entity is the tamable
        if (direct instanceof TamableAnimal tam) {
            Entity o = tam.getOwner();
            if (o instanceof ServerPlayer sp) return sp;
        }

        return null;
    }
}